package `in`.hridayan.ashell.shell.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.shell.common.tool.DeleteBookmarkTool
import `in`.hridayan.ashell.shell.common.tool.SaveBookmarkTool
import `in`.hridayan.ashell.shell.common.tool.SearchBookmarksTool

@Module
@InstallIn(SingletonComponent::class)
abstract class ShellToolModule {
    @Binds
    @IntoSet
    abstract fun bindSaveBookmarkTool(
        tool: SaveBookmarkTool
    ): AiTool

    @Binds
    @IntoSet
    abstract fun bindSearchBookmarksTool(
        tool: SearchBookmarksTool
    ): AiTool

    @Binds
    @IntoSet
    abstract fun bindDeleteBookmarkTool(
        tool: DeleteBookmarkTool
    ): AiTool
}
