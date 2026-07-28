package `in`.hridayan.ashell.commandexamples.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.commandexamples.tool.DeleteCommandExampleTool
import `in`.hridayan.ashell.commandexamples.tool.SaveCommandExampleTool
import `in`.hridayan.ashell.commandexamples.tool.SearchCommandExamplesTool
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool

@Module
@InstallIn(SingletonComponent::class)
abstract class CommandToolModule {
    @Binds
    @IntoSet
    abstract fun bindSaveCommandExampleTool(
        tool: SaveCommandExampleTool
    ): AiTool

    @Binds
    @IntoSet
    abstract fun bindSearchCommandExamplesTool(
        tool: SearchCommandExamplesTool
    ): AiTool

    @Binds
    @IntoSet
    abstract fun bindDeleteCommandExampleTool(
        tool: DeleteCommandExampleTool
    ): AiTool
}
