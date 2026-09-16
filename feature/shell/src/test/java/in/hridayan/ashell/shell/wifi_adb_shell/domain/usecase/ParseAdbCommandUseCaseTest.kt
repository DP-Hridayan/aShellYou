package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.usecase

import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.AdbCommand
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.UnsupportedHint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParseAdbCommandUseCaseTest {

    private val parse = ParseAdbCommandUseCase()

    @Test
    fun `input without the adb prefix stays a shell command`() {
        assertEquals(AdbCommand.Shell("ls /sdcard"), parse("ls /sdcard"))
    }

    @Test
    fun `input without the adb prefix keeps its inner quoting`() {
        val input = "pm grant com.example \"android.permission.READ_LOGS\""
        assertEquals(AdbCommand.Shell(input), parse(input))
    }

    @Test
    fun `surrounding whitespace is trimmed`() {
        assertEquals(AdbCommand.Shell("ls"), parse("   ls   "))
    }

    @Test
    fun `a word merely starting with adb is not an adb command`() {
        assertEquals(AdbCommand.Shell("adbd --help"), parse("adbd --help"))
    }

    @Test
    fun `adb shell unwraps to the remainder`() {
        assertEquals(AdbCommand.Shell("ls -la"), parse("adb shell ls -la"))
    }

    @Test
    fun `adb shell preserves quoting in the remainder`() {
        assertEquals(AdbCommand.Shell("sh -c 'echo  hi'"), parse("adb shell sh -c 'echo  hi'"))
    }

    @Test
    fun `bare adb shell is unsupported because it is interactive`() {
        val result = parse("adb shell")
        assertEquals(
            AdbCommand.Unsupported("shell", UnsupportedHint.INTERACTIVE_SHELL),
            result
        )
    }

    @Test
    fun `adb exec-out maps to the exec service`() {
        assertEquals(AdbCommand.Exec("screencap -p"), parse("adb exec-out screencap -p"))
    }

    @Test
    fun `adb logcat maps to a shell logcat`() {
        assertEquals(AdbCommand.Shell("logcat -d -v time"), parse("adb logcat -d -v time"))
    }

    @Test
    fun `bare adb logcat maps to a shell logcat`() {
        assertEquals(AdbCommand.Shell("logcat"), parse("adb logcat"))
    }

    @Test
    fun `adb tcpip maps to the tcpip service and expects a restart`() {
        assertEquals(
            AdbCommand.DeviceService("tcpip:5555", restartsDaemon = true),
            parse("adb tcpip 5555")
        )
    }

    @Test
    fun `adb tcpip without a port reports usage`() {
        val result = parse("adb tcpip")
        assertTrue(result is AdbCommand.InvalidUsage)
        assertEquals("tcpip", (result as AdbCommand.InvalidUsage).subcommand)
    }

    @Test
    fun `adb tcpip rejects a port outside the valid range`() {
        assertTrue(parse("adb tcpip 70000") is AdbCommand.InvalidUsage)
        assertTrue(parse("adb tcpip 0") is AdbCommand.InvalidUsage)
        assertTrue(parse("adb tcpip abc") is AdbCommand.InvalidUsage)
    }

    @Test
    fun `adb usb maps to the usb service and expects a restart`() {
        assertEquals(
            AdbCommand.DeviceService("usb:", restartsDaemon = true),
            parse("adb usb")
        )
    }

    @Test
    fun `adb reboot passes the mode through`() {
        assertEquals(
            AdbCommand.DeviceService("reboot:bootloader", restartsDaemon = true),
            parse("adb reboot bootloader")
        )
    }

    @Test
    fun `adb reboot without a mode is a plain reboot`() {
        assertEquals(
            AdbCommand.DeviceService("reboot:", restartsDaemon = true),
            parse("adb reboot")
        )
    }

    @Test
    fun `adb root and unroot expect a restart`() {
        assertEquals(
            AdbCommand.DeviceService("root:", restartsDaemon = true),
            parse("adb root")
        )
        assertEquals(
            AdbCommand.DeviceService("unroot:", restartsDaemon = true),
            parse("adb unroot")
        )
    }

    @Test
    fun `adb remount keeps the connection and forwards its arguments`() {
        assertEquals(
            AdbCommand.DeviceService("remount:-R", restartsDaemon = false),
            parse("adb remount -R")
        )
        assertEquals(
            AdbCommand.DeviceService("remount:", restartsDaemon = false),
            parse("adb remount")
        )
    }

    @Test
    fun `adb devices is a host command`() {
        assertEquals(AdbCommand.Host.Devices(verbose = false), parse("adb devices"))
        assertEquals(AdbCommand.Host.Devices(verbose = true), parse("adb devices -l"))
    }

    @Test
    fun `device state queries are host commands`() {
        assertEquals(AdbCommand.Host.GetSerialNo, parse("adb get-serialno"))
        assertEquals(AdbCommand.Host.GetState, parse("adb get-state"))
        assertEquals(AdbCommand.Host.GetDevPath, parse("adb get-devpath"))
        assertEquals(AdbCommand.Host.WaitForDevice, parse("adb wait-for-device"))
    }

    @Test
    fun `server commands collapse to a single no-op`() {
        assertEquals(AdbCommand.Host.ServerNoOp, parse("adb start-server"))
        assertEquals(AdbCommand.Host.ServerNoOp, parse("adb kill-server"))
    }

    @Test
    fun `bare adb shows help`() {
        assertEquals(AdbCommand.Host.Help, parse("adb"))
        assertEquals(AdbCommand.Host.Help, parse("adb help"))
    }

    @Test
    fun `adb version is a host command`() {
        assertEquals(AdbCommand.Host.Version, parse("adb version"))
    }

    @Test
    fun `adb connect accepts an address with a port`() {
        assertEquals(
            AdbCommand.Host.Connect("192.168.1.5", 5555),
            parse("adb connect 192.168.1.5:5555")
        )
    }

    @Test
    fun `adb connect defaults the port when it is omitted`() {
        assertEquals(
            AdbCommand.Host.Connect("192.168.1.5", 5555),
            parse("adb connect 192.168.1.5")
        )
    }

    @Test
    fun `an address with dots is not mistaken for a package name`() {
        assertTrue(parse("adb connect 192.168.1.5") is AdbCommand.Host.Connect)
    }

    @Test
    fun `adb connect without an address reports usage`() {
        assertTrue(parse("adb connect") is AdbCommand.InvalidUsage)
    }

    @Test
    fun `adb disconnect works with and without an address`() {
        assertEquals(AdbCommand.Host.Disconnect(null), parse("adb disconnect"))
        assertEquals(
            AdbCommand.Host.Disconnect("192.168.1.5:5555"),
            parse("adb disconnect 192.168.1.5:5555")
        )
    }

    @Test
    fun `adb pair takes an address and a code`() {
        assertEquals(
            AdbCommand.Host.PairDevice("192.168.1.5", 37000, "123456"),
            parse("adb pair 192.168.1.5:37000 123456")
        )
    }

    @Test
    fun `adb pair without a code reports usage`() {
        assertTrue(parse("adb pair 192.168.1.5:37000") is AdbCommand.InvalidUsage)
    }

    @Test
    fun `adb pair without an explicit port reports usage`() {
        assertTrue(parse("adb pair 192.168.1.5 123456") is AdbCommand.InvalidUsage)
    }

    @Test
    fun `global options taking a value are skipped`() {
        assertEquals(AdbCommand.Shell("ls"), parse("adb -s 192.168.1.5:5555 shell ls"))
        assertEquals(
            AdbCommand.DeviceService("tcpip:5555", restartsDaemon = true),
            parse("adb -t 3 tcpip 5555")
        )
    }

    @Test
    fun `global flags without a value are skipped`() {
        assertEquals(AdbCommand.Host.Devices(verbose = false), parse("adb -d devices"))
        assertEquals(AdbCommand.Shell("ls"), parse("adb -e shell ls"))
    }

    @Test
    fun `file transfer commands are unsupported with a browser hint`() {
        listOf("push", "pull", "sync").forEach { subcommand ->
            assertEquals(
                AdbCommand.Unsupported(subcommand, UnsupportedHint.USE_FILE_BROWSER),
                parse("adb $subcommand a b")
            )
        }
    }

    @Test
    fun `uninstall points at the package manager`() {
        assertEquals(
            AdbCommand.Unsupported("uninstall", UnsupportedHint.USE_PM_UNINSTALL),
            parse("adb uninstall com.example")
        )
    }

    @Test
    fun `sideload points at the sideload screen`() {
        assertEquals(
            AdbCommand.Unsupported("sideload", UnsupportedHint.USE_SIDELOAD_SCREEN),
            parse("adb sideload update.zip")
        )
    }

    @Test
    fun `server only commands are unsupported`() {
        listOf("forward", "reverse", "jdwp", "mdns").forEach { subcommand ->
            assertEquals(
                AdbCommand.Unsupported(subcommand, UnsupportedHint.HOST_ONLY),
                parse("adb $subcommand")
            )
        }
    }

    @Test
    fun `an unknown subcommand is unsupported without a hint`() {
        assertEquals(
            AdbCommand.Unsupported("frobnicate", UnsupportedHint.NONE),
            parse("adb frobnicate")
        )
    }

    @Test
    fun `blank input is a shell command so existing handling is untouched`() {
        assertEquals(AdbCommand.Shell(""), parse("   "))
    }
}
