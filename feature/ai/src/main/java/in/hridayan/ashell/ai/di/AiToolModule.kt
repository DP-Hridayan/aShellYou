package `in`.hridayan.ashell.ai.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.ai.domain.tool.builtin.ExecuteCommandTool
import `in`.hridayan.ashell.ai.domain.tool.builtin.GetStructuredLogcatTool
import `in`.hridayan.ashell.ai.domain.tool.builtin.QueryInstalledAppsTool
import `in`.hridayan.ashell.core.common.domain.model.ai.AiSkill
import `in`.hridayan.ashell.core.common.domain.model.ai.AiSkillBundle
import `in`.hridayan.ashell.core.common.domain.tools.FindAppPackageTool

@Module
@InstallIn(SingletonComponent::class)
object AiToolModule {

    @Provides
    @IntoSet
    fun provideCommandExecutionSkillBundle(
        executeCommandTool: ExecuteCommandTool,
        getStructuredLogcatTool: GetStructuredLogcatTool
    ): AiSkillBundle = object : AiSkillBundle {
        override val skill = AiSkill.COMMAND_EXECUTION
        override val tools = listOf(executeCommandTool, getStructuredLogcatTool)
    }

    @Provides
    @IntoSet
    fun providePackagesSkillBundle(
        queryInstalledAppsTool: QueryInstalledAppsTool,
        findAppPackageTool: FindAppPackageTool
    ): AiSkillBundle = object : AiSkillBundle {
        override val skill = AiSkill.PACKAGES
        override val tools = listOf(queryInstalledAppsTool, findAppPackageTool)
    }
}


