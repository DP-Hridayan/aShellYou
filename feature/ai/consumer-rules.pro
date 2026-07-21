# ─────────────────────────────────────────────────────────────────────────────
# AI Command Analysis – JNI bridge and serializable models
# ─────────────────────────────────────────────────────────────────────────────
-keep class in.hridayan.ashell.ai.native.LlamaCppBridge { *; }
-keep enum in.hridayan.ashell.ai.domain.model.** { *; }
-keep class in.hridayan.ashell.ai.data.local.database.AiCacheEntity { *; }
-keep class in.hridayan.ashell.ai.domain.model.AnalysisResult { *; }
-keep class in.hridayan.ashell.ai.domain.model.CorrectionSuggestion { *; }
