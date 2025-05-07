package in.hridayan.ashell.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.cgutman.adblib.AdbBase64;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbStream;
import in.hridayan.ashell.activities.MainActivity;
import in.hridayan.ashell.utils.OtgUtils.ByteUtils;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.utils.OtgUtils.MessageOtg;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.charset.Charset;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.io.OutputStream;

public class OtgUtils {

  public static class MessageOtg {
    public static final int DEVICE_NOT_FOUND = 0;
    public static final int CONNECTING = 1;
    public static final int DEVICE_FOUND = 2;
    public static final int FLASHING = 3;
    public static final int INSTALLING_PROGRESS = 4;
    public static final int PUSH_PART = 5;
    public static final int PM_INST_PART = 6;
    public static final String USB_PERMISSION = "hridayan.usb.permission";
  }

  public static class ByteUtils {

    public static byte[] concat(byte[]... arrays) {
      // Determine the length of the result array
      int totalLength = 0;
      for (int i = 0; i < arrays.length; i++) {
        totalLength += arrays[i].length;
      }

      // create the result array
      byte[] result = new byte[totalLength];

      // copy the source arrays into the result array
      int currentIndex = 0;
      for (int i = 0; i < arrays.length; i++) {
        System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].length);
        currentIndex += arrays[i].length;
      }

      return result;
    }

    public static final byte[] intToByteArray(int value) {
      return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }
  }

  public static class UsbReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

      if (device == null) return;

      String manufacturer = device.getManufacturerName();
      String product = device.getProductName();

      if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
        showToast(context, "USB Device Attached: " + manufacturer + " " + product);
      } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
        showToast(context, "USB Device Detached: " + manufacturer + " " + product);
        sendIntentUponDetached(context);
      }
    }

    private void showToast(Context context, String message) {
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void sendIntentUponDetached(Context context) {
      Intent intent = new Intent(context, MainActivity.class);
      intent.setAction("in.hridayan.ashell.ACTION_USB_DETACHED");
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
    }
  }

  public static class Push {

    private AdbConnection adbConnection;
    private File local;
    private String remotePath;

    public Push(AdbConnection adbConnection, File local, String remotePath) {
      this.adbConnection = adbConnection;
      this.local = local;
      this.remotePath = remotePath;
    }

    public void execute(Handler handler) throws InterruptedException, IOException {

      AdbStream stream = adbConnection.open("sync:");

      String sendId = "SEND";

      String mode = ",0644";

      int length = (remotePath + mode).length();

      stream.write(ByteUtils.concat(sendId.getBytes(), ByteUtils.intToByteArray(length)));

      stream.write(remotePath.getBytes());

      stream.write(mode.getBytes());

      byte[] buff = new byte[adbConnection.getMaxData()-1];
      InputStream is = new FileInputStream(local);

      long sent = 0;
      long total = local.length();
      int lastProgress = 0;
      while (true) {
        int read = is.read(buff);
        if (read < 0) {
          break;
        }

        stream.write(ByteUtils.concat("DATA".getBytes(), ByteUtils.intToByteArray(read)));

        if (read == buff.length) {
          stream.write(buff);
        } else {
          byte[] tmp = new byte[read];
          System.arraycopy(buff, 0, tmp, 0, read);
          stream.write(tmp);
        }

        sent += read;

        final int progress = (int) (sent * 100 / total);
        if (lastProgress != progress) {
          handler.sendMessage(
              handler.obtainMessage(
                  MessageOtg.INSTALLING_PROGRESS, MessageOtg.PUSH_PART, progress));
          lastProgress = progress;
        }
      }

      stream.write(
          ByteUtils.concat(
              "DONE".getBytes(), ByteUtils.intToByteArray((int) System.currentTimeMillis())));

      byte[] res = stream.read();
      // TODO: test if res contains "OKEY" or "FAIL"
      Log.d(Const.TAG, new String(res));

      stream.write(ByteUtils.concat("QUIT".getBytes(), ByteUtils.intToByteArray(0)));
    }
  }
public static class Pull {

        private AdbConnection adbConnection;
        private File local;
        private String remotePath;
        AdbStream stream;

        public Pull(AdbConnection adbConnection, File local, String remotePath) {
            this.adbConnection = adbConnection;
            this.local = local;
            this.remotePath = remotePath;
        }

        public void execute(Handler handler)  {
            try {
                stream = adbConnection.open("sync:");
                String s=remotePath;
                OutputStream instance=new FileOutputStream(local);
                String sendId = "RECV";
                stream.write(ByteUtils.concat(sendId.getBytes(), ByteUtils.intToByteArray(remotePath.length())));
                stream.write(remotePath.getBytes());
                int i=0;
                long sent = 0;
                while (true) {
                    //log.a("loop=" + i);
                    i++;
                    byte[] bArr = null;
                    byte[] b=stream.read();
                    //log.a(b.length + "islength");
                    sent += b.length;
                        bArr = Arrays.copyOfRange(b, 0, b.length);
                    if (bArr != null) {
                        if (bArr.length == -1) {
                            break;
                        }
                    }
                    try {
                        byte[] bArr2=null;
                        if (bArr != null) {
                            if (bArr.length > 8) {
                                bArr2 = Arrays.copyOfRange(bArr, bArr.length - 8, bArr.length - 4);
                            }
                        }
                        String decodeToString=new String(bArr2, Charset.forName("UTF-8"));
                        //log.a(decodeToString);
                        if (decodeToString.equals("DONE")) {
                            if (bArr != null) {
                                if (bArr.length > 8) {
                                    instance.write(bArr, 0, bArr.length - 8);
                                    break;
                                }
                            }
                        } else {
                            if (bArr != null) {
                                instance.write(bArr, 0, bArr.length);
                            }
                        }
                    } catch (Exception e) {
                        //log.a(e);
                    }
                    //log.a("pulled" + i);
                }
                //log.a("done");
            } catch (Exception e) {
                //log.a(e);
            } finally {
              
                    try {
                        stream.close();
                    } catch (Exception e) {
                        //log.a(e);
                    }
                    //log.a("cloce stream");
            }
        }
	}
  public static class ExternalCmdStore {
    private static SharedPreferences sharedPreferences;
    private static String CMD_KEY = "cmd_key";

    private static void initShared(Context context) {
      if (sharedPreferences == null)
        sharedPreferences = context.getSharedPreferences("cmd", Context.MODE_PRIVATE);
    }

    public static void put(Context context, String cmd) {
      initShared(context);
      sharedPreferences.edit().putString(CMD_KEY, cmd).apply();
    }

    public static String get(Context context) {
      initShared(context);
      return sharedPreferences.getString(CMD_KEY, null);
    }
  }

  public static class Install {
    private AdbConnection adbConnection;
    private String remotePath;
    private long installTimeAssumption = 0;

    public Install(AdbConnection adbConnection, String remotePath, long installTimeAssumption) {
      this.adbConnection = adbConnection;
      this.remotePath = remotePath;
      this.installTimeAssumption = installTimeAssumption;
    }

    public void execute(final Handler handler) throws IOException, InterruptedException {
      final AtomicBoolean done = new AtomicBoolean(false);
      try {
        AdbStream stream = adbConnection.open("shell:pm install -r " + remotePath);
        // we assume installation will take installTimeAssumption milliseconds.
        new Thread() {
          @Override
          public void run() {
            int percent = 0;

            while (!done.get()) {
              handler.sendMessage(
                  handler.obtainMessage(
                      MessageOtg.INSTALLING_PROGRESS, MessageOtg.PM_INST_PART, percent));

              if (percent < 95) {
                percent += 1;
                try {
                  Thread.sleep(installTimeAssumption / 100);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
            }
          }
        }.start();

        while (!stream.isClosed()) {
          try {
            Log.d(Const.TAG, new String(stream.read()));
          } catch (IOException e) {
            // there must be a Stream Close Exception
            break;
          }
        }
      } finally {
        done.set(true);
        handler.sendMessage(
            handler.obtainMessage(MessageOtg.INSTALLING_PROGRESS, MessageOtg.PM_INST_PART, 100));
      }
    }
  }

  public static class MyAdbBase64 implements AdbBase64 {
    @Override
    public String encodeToString(byte[] data) {
      return Base64.encodeToString(data, Base64.NO_WRAP);
    }
  }
}
