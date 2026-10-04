package `in`.hridayan.ashell.mirror.domain.protocol

import `in`.hridayan.ashell.mirror.domain.model.MirrorOptions
import `in`.hridayan.ashell.mirror.domain.model.VideoCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerCommandBuilderTest {

    private val builder = ServerCommandBuilder(
        serverPath = "/data/local/tmp/ashellyou-scrcpy-server-v4.1.jar",
        serverVersion = "4.1"
    )

    private val options = MirrorOptions(maxSize = 1920, videoBitRate = 8_000_000, maxFps = 60)

    @Test
    fun `builds the full launch command`() {
        val command = builder.build(scid = 0x1a2b3c, options = options).getOrThrow()

        assertEquals(
            "echo ASHELLYOU_SERVER_PID=$$; " +
                "export CLASSPATH=/data/local/tmp/ashellyou-scrcpy-server-v4.1.jar; " +
                "exec app_process / --nice-name=ashellyou-srv com.genymobile.scrcpy.Server 4.1 " +
                "scid=001a2b3c log_level=info " +
                "tunnel_forward=true audio=false control=true cleanup=false " +
                "clipboard_autosync=false keep_active=true video_codec=h264 max_size=1920 " +
                "video_bit_rate=8000000 max_fps=60",
            command
        )
    }

    @Test
    fun `includes an allowed encoder name`() {
        val command = builder.build(
            scid = 1,
            options = options.copy(videoCodec = VideoCodec.H265, videoEncoder = "c2.qti.hevc.encoder")
        ).getOrThrow()

        val expectedTail = "video_codec=h265 max_size=1920 video_bit_rate=8000000 max_fps=60 " +
            "video_encoder=c2.qti.hevc.encoder"

        assertTrue(command.endsWith(expectedTail))
    }

    @Test
    fun `rejects an encoder name that could reach the shell`() {
        val result = builder.build(scid = 1, options = options.copy(videoEncoder = "x; rm -rf /sdcard"))

        assertTrue(result.isFailure)
    }

    @Test
    fun `rejects a negative scid`() {
        assertTrue(builder.build(scid = -5, options = options).isFailure)
    }

    @Test
    fun `the server announces the pid it keeps after exec`() {
        assertEquals(4321, ServerCommandBuilder.parseServerPid("ASHELLYOU_SERVER_PID=4321"))
        assertNull(ServerCommandBuilder.parseServerPid("[server] INFO: Device: Pixel"))
    }

    @Test
    fun `stopping a session kills exactly its own server`() {
        assertEquals("kill -9 4321 2>/dev/null && sleep 1; true", ServerCommandBuilder.stopCommand(pid = 4321))
    }

    @Test
    fun `the sweep matches this app's servers by kernel name without a process per entry`() {
        val command = ServerCommandBuilder.sweepCommand()

        assertTrue(ServerCommandBuilder.prepareCommand().startsWith(command + "; "))
        assertTrue(ServerCommandBuilder.prepareCommand().endsWith(TargetProbeParser.COMMAND))

        assertTrue(command.contains("read -r n < \"\$p/comm\""))
        assertTrue(command.contains("ashellyou-srv)"))
        assertTrue(command.contains("grep -q ashellyou-scrcpy-server"))
        assertTrue(command.contains("echo killed"))
    }

    @Test
    fun `socket name matches the server format`() {
        assertEquals("localabstract:scrcpy_0000002a", ServerCommandBuilder.socketService(42))
    }
}
