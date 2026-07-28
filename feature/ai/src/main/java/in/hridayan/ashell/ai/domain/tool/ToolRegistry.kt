package `in`.hridayan.ashell.ai.domain.tool

import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    private val tools: Set<@JvmSuppressWildcards AiTool>
) {
    fun getAllTools(): List<AiTool> = tools.toList()
    
    fun getToolByName(name: String): AiTool? {
        return tools.find { it.name == name }
    }
}
