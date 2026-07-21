# ─────────────────────────────────────────────────────────────────────────────
# SettingsKeys – keep sealed subclasses for kotlin-reflect auto-discovery
# ─────────────────────────────────────────────────────────────────────────────
-keep class in.hridayan.ashell.core.common.SettingsKeys { *; }
-keep class in.hridayan.ashell.core.common.SettingsKeys$* { *; }

-keep enum in.hridayan.ashell.core.domain.model.AdbFileBrowserConnectionMode { *; }
