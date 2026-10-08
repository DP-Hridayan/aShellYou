package `in`.hridayan.ashell.mirror.domain.protocol

import `in`.hridayan.ashell.mirror.domain.model.MirrorOptions

private const val SERVER_CLASS = "com.genymobile.scrcpy.Server"

/**
 * The process name the server runs under, so it is recognisable in `ps` on the device. Kept under
 * 15 characters, the kernel's limit for a process name.
 */
private const val SERVER_PROCESS_NAME = "ashellyou-srv"

/** The line the launch command prints first, followed by the pid the server will run as. */
private const val PID_ANNOUNCEMENT = "ASHELLYOU_SERVER_PID="

/** Part of the jar path in every server's CLASSPATH, including servers from builds before the rename. */
private const val JAR_MARKER = "ashellyou-scrcpy-server"

/** Gives the system a moment to release the encoder and display a killed server held. */
private const val RELEASE_WAIT_SECONDS = 1

private const val SOCKET_SERVICE_FORMAT = "localabstract:scrcpy_%08x"
private const val SCID_FORMAT = "%08x"

/**
 * Only names that cannot change the meaning of a shell command line may be spliced into it.
 * Device encoder names such as `c2.qti.avc.encoder` all fit.
 */
private val SAFE_TOKEN = Regex("^[A-Za-z0-9._-]+$")

/**
 * Builds the shell command that starts the scrcpy server on the other device.
 *
 * Every value is a typed number or enum except the optional encoder name, which is allow-listed, so
 * nothing a device or user supplies can inject shell syntax.
 *
 * @param serverPath where the server jar was pushed on the device.
 * @param serverVersion the version the bundled jar was built as; the server refuses any other.
 */
class ServerCommandBuilder(
    private val serverPath: String,
    private val serverVersion: String
) {

    /**
     * Fixed for every session: forward tunnelling because this app has no adb server to listen
     * with, no audio until it is supported, no clipboard sync unless asked for, and keep_active so
     * the device stays awake while mirrored.
     *
     * Cleanup stays off. Its helper process deletes the server jar as it starts, about a second
     * after its server, and it outlives the server; leaving the mirror and opening it again let the
     * old helper delete the jar the new session had just pushed, so the new server could not start.
     * It restores nothing this app changes, and without it the jar persists, so later sessions skip
     * the upload.
     */
    private val fixedOptions = listOf(
        "log_level=info",
        "tunnel_forward=true",
        "audio=false",
        "control=true",
        "cleanup=false",
        "clipboard_autosync=false",
        "keep_active=true"
    )

    fun build(scid: Int, options: MirrorOptions): Result<String> {
        if (scid < 0) return Result.failure(IllegalArgumentException("scid must not be negative"))

        val encoder = options.videoEncoder
        if (encoder != null && !SAFE_TOKEN.matches(encoder)) {
            return Result.failure(IllegalArgumentException("Unsafe encoder name: $encoder"))
        }

        val arguments = buildList {
            add("scid=${SCID_FORMAT.format(scid)}")
            addAll(fixedOptions)
            add("video_codec=${options.videoCodec.serverName}")
            add("max_size=${options.maxSize}")
            add("video_bit_rate=${options.videoBitRate}")
            add("max_fps=${options.maxFps}")
            encoder?.let { add("video_encoder=$it") }
        }

        val command = "echo $PID_ANNOUNCEMENT\$\$; export CLASSPATH=$serverPath; " +
            "exec app_process / --nice-name=$SERVER_PROCESS_NAME $SERVER_CLASS $serverVersion " +
            arguments.joinToString(" ")
        return Result.success(command)
    }

    companion object {
        /** The ADB service that connects to the abstract socket the server listens on. */
        fun socketService(scid: Int): String = SOCKET_SERVICE_FORMAT.format(scid)

        /**
         * The pid announced by the launch command, or null for any other line. The command `exec`s
         * the server from the announcing shell, so the server keeps exactly this pid.
         */
        fun parseServerPid(line: String): Int? =
            line.takeIf { it.startsWith(PID_ANNOUNCEMENT) }?.removePrefix(PID_ANNOUNCEMENT)?.trim()?.toIntOrNull()

        /**
         * Ends one session's server. A server is meant to exit once its sockets close, but one that
         * does not keeps the encoder and display, and later sessions fail until it goes; desktop
         * scrcpy kills its server for the same reason. Only this pid is killed, because a new session
         * may already be starting while the old one is still cleaning up.
         *
         * After killing a live server it waits, as the sweep does, for the system to release the
         * encoder. The next session's sweep finds nothing left to kill and so does not wait, and a
         * restart straight after this, as a quality change makes, otherwise failed to get an encoder.
         */
        fun stopCommand(pid: Int): String = "kill -9 $pid 2>/dev/null && sleep $RELEASE_WAIT_SECONDS; true"

        /**
         * Kills every server this app left behind, reporting each one, then waits briefly, only if
         * something was killed, for the system to release its encoder.
         *
         * Servers are matched by kernel process name, which any process may read. Only processes
         * still named `app_process` or `main`, as servers from builds before the rename are, have
         * their environment checked for the jar path. The loop uses shell built-ins so that scanning
         * hundreds of processes does not start hundreds of commands.
         */
        /**
         * The sweep, then the target probe, as the one shell command that runs before every launch,
         * so the probe opens no stream of its own and runs only once old servers are gone.
         */
        fun prepareCommand(): String = sweepCommand() + "; " + TargetProbeParser.COMMAND

        fun sweepCommand(): String =
            "k=0; for p in /proc/[0-9]*; do n=; read -r n < \"\$p/comm\" 2>/dev/null; case \"\$n\" in " +
                "$SERVER_PROCESS_NAME) m=1;; " +
                "app_process|main) grep -q $JAR_MARKER \"\$p/environ\" 2>/dev/null && m=1 || m=0;; " +
                "*) m=0;; esac; " +
                "[ \$m = 1 ] && kill -9 \"\${p##*/}\" 2>/dev/null && echo killed \"\${p##*/}\" && k=1; " +
                "done; [ \$k = 1 ] && sleep $RELEASE_WAIT_SECONDS; true"
    }
}
