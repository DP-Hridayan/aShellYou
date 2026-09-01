package `in`.hridayan.ashell.shell.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.core.common.domain.model.ai.AiSkill
import `in`.hridayan.ashell.core.common.domain.model.ai.AiSkillBundle
import `in`.hridayan.ashell.shell.common.tool.DeleteBookmarkTool
import `in`.hridayan.ashell.shell.common.tool.SaveBookmarkTool
import `in`.hridayan.ashell.shell.common.tool.SearchBookmarksTool

@Module
@InstallIn(SingletonComponent::class)
object ShellToolModule {

    @Provides
    @IntoSet
    fun provideShellBookmarksSkillBundle(
        deleteTool: DeleteBookmarkTool,
        saveTool: SaveBookmarkTool,
        searchTool: SearchBookmarksTool
    ): AiSkillBundle = object : AiSkillBundle {
        override val skill = AiSkill.DATABASE
        override val tools = listOf(deleteTool, saveTool, searchTool)
    }
}


