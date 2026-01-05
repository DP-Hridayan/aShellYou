package `in`.hridayan.ashell.shell.common.domain.usecase

class ExtractLastCommandOutputUseCase {
    operator fun invoke(fullOutput: String): String {
        val lines = fullOutput.lines()
        val lastCommandIndex = lines.indexOfLast { it.trim().startsWith("$ ") }

        return if (lastCommandIndex == -1) {
            fullOutput.trim()
        } else {
            lines.drop(lastCommandIndex).joinToString("\n").trim()
        }
    }
}
