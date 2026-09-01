package `in`.hridayan.ashell.qstiles.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.core.common.domain.model.ai.AiSkill
import `in`.hridayan.ashell.core.common.domain.model.ai.AiSkillBundle
import `in`.hridayan.ashell.qstiles.tool.CreateQsTileTool
import `in`.hridayan.ashell.qstiles.tool.GetQsTileSlotsTool
import `in`.hridayan.ashell.qstiles.tool.UpdateQsTileTool

@Module
@InstallIn(SingletonComponent::class)
object QsTileToolModule {

    @Provides
    @IntoSet
    fun provideQsTileSkillBundle(
        createTool: CreateQsTileTool,
        getSlotsTool: GetQsTileSlotsTool,
        updateTool: UpdateQsTileTool
    ): AiSkillBundle = object : AiSkillBundle {
        override val skill = AiSkill.QUICK_SETTINGS
        override val tools = listOf(createTool, getSlotsTool, updateTool)
    }
}


