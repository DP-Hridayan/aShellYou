package `in`.hridayan.ashell.commandexamples.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.commandexamples.tool.DeleteCommandExampleTool
import `in`.hridayan.ashell.commandexamples.tool.SaveCommandExampleTool
import `in`.hridayan.ashell.commandexamples.tool.SearchCommandExamplesTool
import `in`.hridayan.ashell.commandexamples.tool.UpdateCommandExampleTool
import `in`.hridayan.ashell.core.common.domain.model.ai.AiSkill
import `in`.hridayan.ashell.core.common.domain.model.ai.AiSkillBundle

@Module
@InstallIn(SingletonComponent::class)
object CommandToolModule {

    @Provides
    @IntoSet
    fun provideCommandExampleSkillBundle(
        deleteTool: DeleteCommandExampleTool,
        saveTool: SaveCommandExampleTool,
        searchTool: SearchCommandExamplesTool,
        updateTool: UpdateCommandExampleTool
    ): AiSkillBundle = object : AiSkillBundle {
        override val skill = AiSkill.DATABASE
        override val tools = listOf(deleteTool, saveTool, searchTool, updateTool)
    }
}


