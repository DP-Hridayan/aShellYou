package `in`.hridayan.ashell.shell.file_browser.data.shell

import java.io.File
import java.util.concurrent.TimeUnit

private const val SCRIPT_TIMEOUT_SECONDS = 30L

private val WINDOWS_SHELLS = listOf(
    "C:\\Program Files\\Git\\bin\\sh.exe",
    "C:\\Program Files\\Git\\usr\\bin\\sh.exe"
)

/**
 * A POSIX shell on the machine running the tests, standing in for the device shell.
 *
 * Scripts go in through stdin rather than as an argument, because Windows mangles double quotes
 * inside process arguments and the commands under test are full of them.
 */
internal class LocalShell private constructor(private val executable: String) {

    fun run(script: String): String {
        val process = ProcessBuilder(executable, "-s").redirectErrorStream(true).start()
        process.outputStream.use { it.write(script.toByteArray()) }
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor(SCRIPT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        return output
    }

    companion object {

        fun findOrNull(): LocalShell? =
            (shellsOnPath() + WINDOWS_SHELLS)
                .map(::File)
                .firstOrNull { it.isFile && it.canExecute() }
                ?.let { LocalShell(it.path) }

        private fun shellsOnPath(): List<String> =
            System.getenv("PATH").orEmpty()
                .split(File.pathSeparator)
                .filter { it.isNotBlank() }
                .flatMap { listOf("$it${File.separator}sh", "$it${File.separator}sh.exe") }
    }
}
