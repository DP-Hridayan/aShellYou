package `in`.hridayan.ashell.ai.domain.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.ai.domain.tool.ToolRegistry
import `in`.hridayan.ashell.core.common.domain.model.ai.AiSkill
import `in`.hridayan.ashell.core.common.domain.model.localadb.LocalAdbWorkingMode
import `in`.hridayan.ashell.core.common.domain.repository.AiConnectionStateProvider
import `in`.hridayan.ashell.core.common.domain.repository.OtgRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import kotlinx.coroutines.flow.firstOrNull
import java.util.Locale
import javax.inject.Inject

private const val NO_TOOLS_MAPPED = "None mapped yet"
private const val NOT_CONNECTED = "Not connected"
private const val NONE = "None"

/**
 * Assembles the chat system prompt from the device's current capabilities.
 *
 * The prompt is rebuilt every turn because it reports live state: which execution modes are
 * connected, which skills the user has enabled, and which tools each skill contributes.
 */
class ChatSystemPromptBuilder @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val toolRegistry: ToolRegistry,
    private val settingsRepository: SettingsRepository,
    private val otgRepository: OtgRepository,
    private val aiConnectionStateProvider: AiConnectionStateProvider,
) {
    suspend fun build(): String = buildString {
        append(INTRO)
        append(skillsSection())
        append(connectionSection())
        append(QS_TILE_RULES)
        append(languageSection())
        append(CODE_BLOCK_RULE)
    }

    private suspend fun skillsSection(): String {
        val allSkillsWithTools = toolRegistry.getAllSkillsWithTools()

        return buildString {
            append("APP SKILLS CONTEXT (Capabilities you potentially have):\n")
            AiSkill.entries.forEach { skill ->
                val isEnabled = settingsRepository.getBoolean(skill.settingsKey).firstOrNull()
                    ?: skill.settingsKey.default
                val status = if (isEnabled) "ENABLED" else "DISABLED"
                val toolNames = allSkillsWithTools[skill]
                    ?.joinToString { it.name }
                    ?.ifEmpty { NO_TOOLS_MAPPED }
                    ?: NO_TOOLS_MAPPED
                val name = context.getString(skill.displayNameRes)
                val description = context.getString(skill.descriptionRes)
                append("- [$status] $name: $description (Provides tools: $toolNames)\n")
            }
            append(SKILL_RULES)
        }
    }

    private suspend fun connectionSection(): String {
        val localContext = localModeName()
        val otgContext = otgDescription()
        val wifiContext = wifiDescription()
        val pairedDevices = aiConnectionStateProvider.getWifiPairedDevices()
            .takeIf { it.isNotEmpty() }
            ?.joinToString(", ")
            ?: NONE

        return buildString {
            append("CONNECTION CAPABILITIES:\n")
            append(TARGET_MODE_RULE)
            append("- LOCAL: The default execution mode (currently set to $localContext by the ")
            append("user). IMPORTANT: If the user explicitly asks to execute a command in ROOT, ")
            append("SHIZUKU, or TCPIP mode, you MUST set the `local_adb_mode` parameter in your ")
            append("tool call (options: ROOT, SHIZUKU, TCPIP, BASIC) to explicitly override it!\n")
            append("- WIRELESS: $wifiContext. Paired devices: $pairedDevices.\n")
            append("- OTG: $otgContext.\n")
            append(TARGET_CHOICE_RULE)
        }
    }

    private suspend fun localModeName(): String {
        val modeId = settingsRepository.getInt(SettingsKeys.LocalAdbWorkingMode).firstOrNull()
            ?: SettingsKeys.LocalAdbWorkingMode.default

        return when (modeId) {
            LocalAdbWorkingMode.ROOT -> "Root"
            LocalAdbWorkingMode.SHIZUKU -> "Shizuku"
            LocalAdbWorkingMode.TCPIP -> "TCP/IP ADB"
            else -> "Basic"
        }
    }

    private suspend fun otgDescription(): String = if (otgRepository.isConnected()) {
        val device = otgRepository.getAdbConnection()
        "Connected (Max Payload: ${device?.maxData ?: "Unknown"})"
    } else {
        NOT_CONNECTED
    }

    private fun wifiDescription(): String {
        val deviceName = aiConnectionStateProvider.getWifiConnectedDeviceName()
            ?: return NOT_CONNECTED
        val type = if (aiConnectionStateProvider.isWifiOwnDevice()) "Own Device" else "Other Device"
        return "Connected to $deviceName ($type)"
    }

    private fun languageSection(): String {
        val localeName = Locale.getDefault().displayName

        return "CRITICAL: You must communicate and provide all your conversational responses " +
            "and explanations in the user's local language ($localeName). However, all shell " +
            "commands, ADB commands, tool names, and technical function names must remain " +
            "strictly in English.\n\n"
    }

    private companion object {
        const val INTRO = "You are a highly capable AI shell assistant. You can execute " +
            "commands on the user's Android device and answer questions. YOU MUST USE YOUR " +
            "TOOLS TO EXECUTE COMMANDS WHENEVER POSSIBLE INSTEAD OF MERELY SUGGESTING THEM.\n\n"

        const val SKILL_RULES = "\nIMPORTANT RULES ABOUT SKILLS:\n" +
            "1. If the user asks you to do something that requires a DISABLED skill, you MUST " +
            "inform them that the skill is currently disabled and explicitly suggest they turn " +
            "it on in the AI Agent Settings.\n" +
            "2. If you HAVE an ENABLED skill for a task, you MUST use your tools to execute it " +
            "yourself rather than just telling the user how to do it or suggesting commands. " +
            "Never be lazy!\n\n"

        const val TARGET_MODE_RULE = "You can execute commands on different targets by " +
            "specifying the `target_mode` parameter in your `execute_command` tool " +
            "(options: LOCAL, WIRELESS, OTG). Defaults to LOCAL if omitted.\n"

        const val TARGET_CHOICE_RULE = "Always choose the appropriate target and mode based on " +
            "the user's request. If the user doesn't specify, default to the most capable " +
            "connected device.\n\n"

        const val QS_TILE_RULES = "QUICK SETTINGS (QS) TILES RULES:\n" +
            "1. If the user asks to create a Quick Settings (QS) tile, ALWAYS call " +
            "`get_qs_tile_slots` first to see which of the 10 fixed slots (1-10) are " +
            "empty/available.\n" +
            "2. If the user did not specify an execution mode for the tile, ASK the user which " +
            "execution mode they prefer (0 for Shizuku [default], 1 for Root) before calling " +
            "`create_qs_tile`.\n" +
            "3. Decide whether the tile should be toggleable (`is_toggleable = true` with " +
            "active/inactive commands) or a simple tap action based on the user's request.\n" +
            "4. After creating the tile, tell the user which slot number (1-10) it was created " +
            "in and confirm that the system prompt dialog was triggered to add the tile to " +
            "their Quick Settings panel.\n\n"

        const val CODE_BLOCK_RULE = "IMPORTANT: When you suggest a command for the user to run " +
            "themselves, you MUST format it as a markdown code block " +
            "(e.g., ```bash\\ncommand\\n```). The UI will automatically detect this and render " +
            "a 'Use' button for the user."
    }
}
