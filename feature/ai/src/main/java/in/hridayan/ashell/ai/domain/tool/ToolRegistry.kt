package `in`.hridayan.ashell.ai.domain.tool

import `in`.hridayan.ashell.core.common.domain.model.ai.AiSkill
import `in`.hridayan.ashell.core.common.domain.model.ai.AiSkillBundle
import `in`.hridayan.ashell.core.common.domain.model.ai.AiTool
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    private val skillBundles: Set<@JvmSuppressWildcards AiSkillBundle>,
    private val settingsRepository: SettingsRepository
) {
    suspend fun getEnabledSkills(): Map<AiSkill, List<AiTool>> {
        val groupedBundles = skillBundles.groupBy { it.skill }
        val result = mutableMapOf<AiSkill, List<AiTool>>()

        for ((skill, bundles) in groupedBundles) {
            val isEnabled = settingsRepository.getBoolean(skill.settingsKey).firstOrNull()
                ?: skill.settingsKey.default
            if (isEnabled) {
                result[skill] = bundles.flatMap { it.tools }
            }
        }
        return result
    }

    suspend fun getEnabledTools(): List<AiTool> {
        return getEnabledSkills().values.flatten()
    }

    fun getSkillForToolName(toolName: String): AiSkill? {
        return skillBundles.find { bundle ->
            bundle.tools.any { it.name == toolName }
        }?.skill
    }

    fun getToolByName(toolName: String): AiTool? {
        return skillBundles.flatMap { it.tools }.find { it.name == toolName }
    }
    fun getAllSkillsWithTools(): Map<AiSkill, List<AiTool>> {
        val groupedBundles = skillBundles.groupBy { it.skill }
        val result = mutableMapOf<AiSkill, List<AiTool>>()
        for ((skill, bundles) in groupedBundles) {
            result[skill] = bundles.flatMap { it.tools }
        }
        return result
    }
}


