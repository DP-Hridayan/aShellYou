package `in`.hridayan.ashell.qstiles.data.usecase

import `in`.hridayan.ashell.core.common.domain.model.TileExecutionMode
import `in`.hridayan.ashell.qstiles.domain.executor.CommandExecutor
import `in`.hridayan.ashell.qstiles.domain.usecase.CollapseQsPanelUseCase
import javax.inject.Inject
import javax.inject.Named

class CollapseQsPanelUseCaseImpl @Inject constructor(
    @Named("shizuku") private val shizukuExecutor: CommandExecutor,
    @Named("root") private val rootExecutor: CommandExecutor,
    @Named("tcpip") private val tcpIpExecutor: CommandExecutor,
) : CollapseQsPanelUseCase {

    override suspend fun invoke(executionMode: Int): Boolean {
        val executor = when (executionMode) {
            TileExecutionMode.SHIZUKU -> shizukuExecutor
            TileExecutionMode.ROOT -> rootExecutor
            TileExecutionMode.TCPIP -> tcpIpExecutor
            else -> return false
        }

        return executor.execute(COLLAPSE_STATUSBAR_COMMAND).isSuccess
    }

    private companion object {
        const val COLLAPSE_STATUSBAR_COMMAND = "cmd statusbar collapse"
    }
}
