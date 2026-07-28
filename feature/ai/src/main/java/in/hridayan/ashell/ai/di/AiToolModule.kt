package `in`.hridayan.ashell.ai.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.ai.domain.tool.builtin.ExecuteCommandTool
import `in`.hridayan.ashell.ai.domain.tool.builtin.GetStructuredLogcatTool
import `in`.hridayan.ashell.ai.domain.tool.builtin.QueryInstalledAppsTool
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool

@Module
@InstallIn(SingletonComponent::class)
abstract class AiToolModule {

    @Binds
    @IntoSet
    abstract fun bindExecuteCommandTool(
        executeCommandTool: ExecuteCommandTool
    ): AiTool

    @Binds
    @IntoSet
    abstract fun bindQueryInstalledAppsTool(
        queryInstalledAppsTool: QueryInstalledAppsTool
    ): AiTool

    @Binds
    @IntoSet
    abstract fun bindGetStructuredLogcatTool(
        getStructuredLogcatTool: GetStructuredLogcatTool
    ): AiTool
}
