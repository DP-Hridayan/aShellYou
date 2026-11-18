package `in`.hridayan.ashell.qstiles.domain.usecase

interface CollapseQsPanelUseCase {
    suspend operator fun invoke(executionMode: Int): Boolean
}
