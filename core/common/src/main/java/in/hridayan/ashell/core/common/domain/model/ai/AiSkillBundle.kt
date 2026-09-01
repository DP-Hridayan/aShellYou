package `in`.hridayan.ashell.core.common.domain.model.ai

interface AiSkillBundle {
    val skill: AiSkill
    val tools: List<AiTool>
}
