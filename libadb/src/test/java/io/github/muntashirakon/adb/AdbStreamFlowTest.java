package io.github.muntashirakon.adb;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.KeyPairGenerator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Drives a real {@link AdbConnection} against a scripted adbd on loopback, so stream behaviour is
 * checked through the same socket path the app uses.
 */
public class AdbStreamFlowTest {

    private static final int API = 30;
    private static final int DEVICE_MAX_DATA = 1024;
    private static final int REMOTE_ID = 77;
    private static final long STEP_TIMEOUT_MS = 2_000;

    private static final int LARGE_MAX_DATA = 64 * 1024 * 1024;
    private static final int SMALL_RECEIVE_BUFFER = 4 * 1024;
    private static final int REMOTE_ID_B = 78;
    private static final String REPLY_THREAD = "libadb-replies";
    private static final int GATED_READ_BYTES = 1024;

    private FakeAdbd adbd;
    private AdbConnection connection;
    private ExecutorService threads;

    @Before
    public void connect() throws Exception {
        connectTo(new FakeAdbd(DEVICE_MAX_DATA));
    }

    private void connectTo(FakeAdbd fake) throws Exception {
        if (connection != null) {
            connection.close();
            adbd.close();
        }
        if (threads == null) {
            threads = Executors.newCachedThreadPool();
        }
        adbd = fake;
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        java.security.KeyPair rsa = generator.generateKeyPair();
        KeyPair keyPair = new KeyPair(rsa.getPrivate(), new StubCertificate(rsa.getPublic()));
        connection = AdbConnection.create(InetAddress.getLoopbackAddress().getHostAddress(), adbd.port(), keyPair, API);
        assertTrue(connection.connect(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS, false));
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
        adbd.close();
        threads.shutdownNow();
    }

    @Test
    public void aReadAfterDataAndCloseArrivedTogetherEndsInsteadOfHanging() throws Exception {
        AdbStream stream = open();
        adbd.send(AdbProtocol.generateWrite(REMOTE_ID, adbd.lastLocalId, new byte[]{1, 2, 3}, 0, 3));
        adbd.send(AdbProtocol.generateClose(REMOTE_ID, adbd.lastLocalId));
        Thread.sleep(200);

        InputStream input = stream.openInputStream();
        byte[] buffer = new byte[16];
        assertEquals(3, input.read(buffer));

        Future<Integer> next = threads.submit(() -> {
            try {
                return input.read(buffer);
            } catch (IOException closed) {
                return -1;
            }
        });
        assertEquals(Integer.valueOf(-1), next.get(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void aLargeWriteWaitsForAnAcknowledgementBeforeEachChunk() throws Exception {
        AdbStream stream = open();
        byte[] payload = new byte[DEVICE_MAX_DATA * 2 + 10];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) i;
        }

        Future<?> writing = threads.submit(() -> {
            stream.write(payload, 0, payload.length);
            return null;
        });

        assertEquals(DEVICE_MAX_DATA, adbd.nextWrite().length);
        Thread.sleep(200);
        assertEquals("A second chunk was sent before the first was acknowledged", 1, adbd.writes.size());

        adbd.send(AdbProtocol.generateReady(REMOTE_ID, adbd.lastLocalId));
        assertEquals(DEVICE_MAX_DATA, adbd.nextWrite().length);
        adbd.send(AdbProtocol.generateReady(REMOTE_ID, adbd.lastLocalId));
        assertEquals(10, adbd.nextWrite().length);
        writing.get(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        byte[] received = new byte[payload.length];
        int offset = 0;
        for (byte[] chunk : adbd.writes) {
            System.arraycopy(chunk, 0, received, offset, chunk.length);
            offset += chunk.length;
        }
        assertArrayEquals(payload, received);
    }

    @Test
    public void anEmptyWriteKeepsTheStreamWritable() throws Exception {
        AdbStream stream = open();
        stream.write(new byte[0], 0, 0);

        Future<?> writing = threads.submit(() -> {
            stream.write(new byte[]{9}, 0, 1);
            return null;
        });

        assertEquals(1, adbd.nextWrite().length);
        writing.get(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    /*
     * A write that cannot drain holds the connection's write lock. If the reader thread had to take
     * that lock to acknowledge incoming data, it would stop reading, and every other stream on the
     * connection would stop receiving until the write finished.
     */
    @Test
    public void incomingDataKeepsFlowingWhileAWriteIsStuck() throws Exception {
        connectTo(new FakeAdbd(LARGE_MAX_DATA, SMALL_RECEIVE_BUFFER));
        AdbStream uploading = open();
        int uploadingId = adbd.lastLocalId;
        AdbStream receiving = open(REMOTE_ID_B);
        int receivingId = adbd.lastLocalId;

        adbd.stopReading();
        Future<?> upload = threads.submit(() -> {
            uploading.write(new byte[LARGE_MAX_DATA], 0, LARGE_MAX_DATA);
            return null;
        });
        Thread.sleep(500);
        assertFalse("The upload drained, so nothing was stuck", upload.isDone());

        adbd.send(AdbProtocol.generateWrite(REMOTE_ID_B, receivingId, new byte[]{1}, 0, 1));
        adbd.send(AdbProtocol.generateWrite(REMOTE_ID_B, receivingId, new byte[]{2}, 0, 1));

        InputStream input = receiving.openInputStream();
        byte[] buffer = new byte[1];
        Future<Integer> first = threads.submit(() -> input.read(buffer));
        assertEquals(Integer.valueOf(1), first.get(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS));
        Future<Integer> second = threads.submit(() -> input.read(buffer));
        assertEquals("The reader stopped while a write was stuck", Integer.valueOf(1),
                second.get(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS));
        assertTrue(uploadingId != receivingId);
    }

    @Test
    public void incomingDataIsStillAcknowledged() throws Exception {
        open();
        int localId = adbd.lastLocalId;

        adbd.send(AdbProtocol.generateWrite(REMOTE_ID, localId, new byte[]{1}, 0, 1));

        AdbProtocol.Message ready = adbd.nextReady();
        assertEquals(localId, ready.arg0);
        assertEquals(REMOTE_ID, ready.arg1);
    }

    @Test
    public void closingStopsTheReplyThread() throws Exception {
        Set<Thread> before = replyThreads();
        open();
        adbd.send(AdbProtocol.generateWrite(REMOTE_ID, adbd.lastLocalId, new byte[]{1}, 0, 1));
        adbd.nextReady();
        Set<Thread> started = replyThreads();
        started.removeAll(before);
        assertEquals("Expected this connection to start one reply thread", 1, started.size());
        Thread replies = started.iterator().next();

        connection.close();
        replies.join(STEP_TIMEOUT_MS);

        assertFalse("The reply thread outlived its connection", replies.isAlive());
    }

    private static Set<Thread> replyThreads() {
        Set<Thread> threads = new HashSet<>();
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().equals(REPLY_THREAD) && thread.isAlive()) {
                threads.add(thread);
            }
        }
        return threads;
    }

    private AdbStream open() throws Exception {
        return open(REMOTE_ID);
    }

    private AdbStream open(int remoteId) throws Exception {
        Future<AdbStream> opening = threads.submit(() -> connection.open("tcp:9222"));
        adbd.awaitOpen();
        adbd.send(AdbProtocol.generateReady(remoteId, adbd.lastLocalId));
        return opening.get(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Plays adbd: answers the handshake, records every WRTE payload, and sends what the test scripts.
     * Its CNXN advertises a small max payload so chunking is easy to observe.
     */
    private static final class FakeAdbd implements AutoCloseable {

        private final ServerSocket server;
        private final int maxData;
        private final BlockingQueue<AdbProtocol.Message> opens = new LinkedBlockingQueue<>();
        private final BlockingQueue<AdbProtocol.Message> readies = new LinkedBlockingQueue<>();
        private final BlockingQueue<byte[]> pendingWrites = new LinkedBlockingQueue<>();
        private final CountDownLatch readGate = new CountDownLatch(1);
        private volatile boolean reading = true;
        final List<byte[]> writes = new CopyOnWriteArrayList<>();
        volatile int lastLocalId;
        private volatile Socket client;

        FakeAdbd(int maxData) throws IOException {
            this(maxData, 0);
        }

        /** @param receiveBuffer the socket's receive buffer, or 0 for the default */
        FakeAdbd(int maxData, int receiveBuffer) throws IOException {
            this.maxData = maxData;
            server = new ServerSocket();
            if (receiveBuffer > 0) {
                server.setReceiveBufferSize(receiveBuffer);
            }
            server.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 1);
            Thread thread = new Thread(this::serve);
            thread.setDaemon(true);
            thread.start();
        }

        int port() {
            return server.getLocalPort();
        }

        void send(byte[] packet) throws IOException {
            OutputStream output = client.getOutputStream();
            synchronized (this) {
                output.write(packet);
                output.flush();
            }
        }

        void awaitOpen() throws InterruptedException {
            AdbProtocol.Message open = opens.poll(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertTrue("Expected an OPEN", open != null);
            lastLocalId = open.arg0;
        }

        /** Stops reading the socket, like a device whose transport stopped draining. */
        void stopReading() {
            reading = false;
        }

        AdbProtocol.Message nextReady() throws InterruptedException {
            AdbProtocol.Message ready = readies.poll(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertTrue("Expected an OKAY", ready != null);
            return ready;
        }

        byte[] nextWrite() throws InterruptedException {
            byte[] write = pendingWrites.poll(STEP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertTrue("Expected a WRTE", write != null);
            return write;
        }

        private void serve() {
            try {
                client = server.accept();
                InputStream input = new GatedInputStream(client.getInputStream());
                while (true) {
                    AdbProtocol.Message message = AdbProtocol.Message.parse(input, AdbProtocol.A_VERSION, Integer.MAX_VALUE);
                    handle(message);
                }
            } catch (IOException ignored) {
            }
        }

        private void handle(AdbProtocol.Message message) throws IOException {
            switch (message.command) {
                case AdbProtocol.A_CNXN:
                    send(AdbProtocol.generateMessage(AdbProtocol.A_CNXN, AdbProtocol.A_VERSION, maxData,
                            "device::\0".getBytes()));
                    break;
                case AdbProtocol.A_OPEN:
                    opens.add(message);
                    break;
                case AdbProtocol.A_OKAY:
                    readies.add(message);
                    break;
                case AdbProtocol.A_WRTE:
                    byte[] payload = message.payload == null ? new byte[0] : message.payload;
                    writes.add(payload);
                    pendingWrites.add(payload);
                    break;
                default:
                    break;
            }
        }

        /** Stops passing bytes on once reading stops, even in the middle of a message. */
        private final class GatedInputStream extends FilterInputStream {
            GatedInputStream(InputStream in) {
                super(in);
            }

            @Override
            public int read() throws IOException {
                awaitReading();
                return super.read();
            }

            @Override
            public int read(byte[] buffer, int offset, int length) throws IOException {
                awaitReading();
                return super.read(buffer, offset, Math.min(length, GATED_READ_BYTES));
            }

            private void awaitReading() throws IOException {
                if (reading) return;
                try {
                    readGate.await();
                } catch (InterruptedException e) {
                    throw new IOException(e);
                }
            }
        }

        @Override
        public void close() throws IOException {
            readGate.countDown();
            if (client != null) {
                client.close();
            }
            server.close();
        }
    }

    /** Never used for authentication: the scripted adbd accepts the connection without AUTH. */
    private static final class StubCertificate extends Certificate {

        private final PublicKey publicKey;

        StubCertificate(PublicKey publicKey) {
            super("X.509");
            this.publicKey = publicKey;
        }

        @Override
        public byte[] getEncoded() {
            return new byte[0];
        }

        @Override
        public void verify(PublicKey key) {
        }

        @Override
        public void verify(PublicKey key, String sigProvider) {
        }

        @Override
        public String toString() {
            return "StubCertificate";
        }

        @Override
        public PublicKey getPublicKey() {
            return publicKey;
        }
    }
}
