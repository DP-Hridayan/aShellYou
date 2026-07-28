package `in`.hridayan.ashell.qstiles.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import `in`.hridayan.ashell.qstiles.tool.CreateQsTileTool
import `in`.hridayan.ashell.qstiles.tool.GetQsTileSlotsTool
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool

@Module
@InstallIn(SingletonComponent::class)
abstract class QsTileToolModule {
    @Binds
    @IntoSet
    abstract fun bindCreateQsTileTool(
        tool: CreateQsTileTool
    ): AiTool

    @Binds
    @IntoSet
    abstract fun bindGetQsTileSlotsTool(
        tool: GetQsTileSlotsTool
    ): AiTool
}
