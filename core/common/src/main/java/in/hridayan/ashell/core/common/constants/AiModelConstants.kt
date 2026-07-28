package `in`.hridayan.ashell.core.common.constants

object AiModelConstants {
    // Models ordered from highest quality to lowest quality
    val geminiModelsHighestToLowest = listOf(
        "gemini-3.6-flash",
        "gemini-3.5-flash",
        "gemini-2.5-flash",
        "gemini-3.5-flash-lite",
        "gemini-3.1-flash-lite"
    )

    // Models ordered from lowest quality to highest quality
    val geminiModelsLowestToHighest = geminiModelsHighestToLowest.reversed()

    // Lite models for fast, cheap command analysis
    val geminiLiteModels = listOf(
        "gemini-3.5-flash-lite",
        "gemini-3.1-flash-lite"
    )
}