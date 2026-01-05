package `in`.hridayan.ashell.shell.common.domain.usecase

import `in`.hridayan.ashell.core.utils.DateTimeUtils

class GetSaveOutputFileNameUseCase {
    operator fun invoke(saveWholeOutput: Boolean, lastCommand: String?): String {
        val currentDateTime = DateTimeUtils.getCurrentDateTime()

        val wholeOutputFileName = "aShellYou_$currentDateTime.txt"
        val lastCommandOutputFileName = lastCommand + "_" + currentDateTime + ".txt"

        return if (saveWholeOutput) wholeOutputFileName else lastCommandOutputFileName
    }
}