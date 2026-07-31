# ─────────────────────────────────────────────────────────────────────────────
# SettingsKeys – keep sealed subclasses for kotlin-reflect auto-discovery
# ─────────────────────────────────────────────────────────────────────────────
-keep class in.hridayan.ashell.core.common.settings.SettingsKeys { *; }
-keep class in.hridayan.ashell.core.common.settings.SettingsKeys$* { *; }

-keep enum in.hridayan.ashell.core.common.domain.model.AdbFileBrowserConnectionMode { *; }
