# Privacy Policy for aShell You

**Effective Date:** September 2, 2026</br>
**Last Updated:** September 2, 2026</br>
**Contact:** [hridayanofficial@gmail.com](mailto:hridayanofficial@gmail.com)

DP Hridayan ("we," "us," or "our") is committed to protecting your privacy. This Privacy Policy
explains our practices regarding the information that the **aShell You** Android application (the "
App") processes on your device. Please read this document carefully.

**Core principle:** aShell You is designed with a privacy-first, offline-first architecture. We do
not operate any backend servers, and we do not collect, transmit, or sell your personal data to
ourselves or to any third party for analytics, advertising, or profiling purposes. All data
described in this policy resides on your device or, where applicable, in services you explicitly
choose and control (e.g., your own Google Drive).

---

## 1. Information Stored Locally on Your Device

aShell You stores several categories of data in local databases and files. All of this data remains
on your device unless you explicitly trigger a backup.

### 1.1 Command History & Bookmarks

The App maintains two local databases:

- **Command Database:** Stores each shell command you run, including a user-readable description, a
  usage count, favourite status, and any category labels you apply.
- **Bookmark Database:** Stores only the text of commands you explicitly save as bookmarks.

None of this data is transmitted to any server by the App.

### 1.2 AI Assistant Data

If you choose to use the AI Assistant feature, the following data is stored locally:

- **Chat Session Metadata:** Session title, creation timestamp, last-updated timestamp, pinned
  status, and whether you renamed the session.
- **Chat Message History:** The complete content of every message in a session including your
  prompts and the AI model's responses stored locally in a Room database. This data is not uploaded
  to our servers.
- **AI Analysis Cache:** A locally cached copy of AI-generated analyses of shell commands, indexed
  by a SHA-256 hash of the command text, the model identifier, the raw analysis JSON, and a
  timestamp. This caching is purely local and avoids redundant network calls.
- **Command Execution Permissions:** A local record of your per-command decisions on whether the AI
  is always permitted to execute a specific shell command without further prompting.
- **API Key Storage:** Your AI provider API key (e.g., Google Gemini API key) is stored in a private
  SharedPreferences file (`ai_api_keys`) encrypted using Google Tink (AES256_GCM) backed by the
  Android Keystore. This key is never transmitted to our servers.

### 1.3 Crash Reports

If the App crashes, the Crash Reporter module records the following device and app metadata locally
in a Room database:

- Timestamp of the crash
- Device hardware information: brand, model, manufacturer, CPU ABI, SoC manufacturer
- Android OS version
- App package name, version name, and version code
- Full exception stack trace

**This data is never automatically uploaded or transmitted.** If you choose to share a crash report,
the App opens your device's email client (via a `mailto:` intent) with the crash data pre-filled in
the email body. You have full control to review, edit, or discard it before sending.

### 1.4 Quick Settings (QS) Tile Logs

The App supports up to 10 configurable Quick Settings tiles. For each tile execution, the App
locally stores:

- The tile identifier
- The ADB/shell command that was run
- The command output text
- Whether the execution succeeded or failed
- The execution mode (e.g., Shizuku, Root)
- Execution duration in milliseconds
- A timestamp

These logs remain on your device only and are not transmitted to any server.

### 1.5 Wi-Fi ADB Device History

When you successfully connect to or pair with an ADB target device over Wi-Fi, the App stores a
local record containing:

- The device's IP address and port
- A human-readable device name
- The device's ADB serial number
- Last-connected timestamp
- Pairing and ownership status

This information is used only to provide a convenient "recent devices" list on your device and is
never uploaded.

### 1.6 Custom Themes

Custom color schemes you create or import are stored in a local database containing a name, 39
individual Material You color values (e.g., `primary`, `secondary`, `background`, `error`), a
dark/light theme flag, and a creation timestamp.

Custom themes can be exported to `.ashellyoucolorscheme` files (a JSON format) and shared with
others. These files contain only color values and a theme name no personal information.

### 1.7 Custom Fonts

If you import custom `.ttf` font files, they are copied to the App's private internal storage
directory. The App stores a local record with the font's display name, internal file path, and
import timestamp. Font files are not uploaded anywhere.

### 1.8 ADB Cryptographic Keys

To authenticate ADB connections, the App generates an RSA key pair on first use and stores the
resulting private key and public key files in the App's private internal storage. These keys are
used solely to establish trusted ADB connections and are never transmitted to our servers.

### 1.9 Application Settings

All user preferences are stored in a local DataStore preferences file. The following categories of
settings are stored locally:

- **Appearance:** Theme mode, dynamic colors, color palette, font family, font size, UI density.
- **Behavior:** Haptics/vibration, soft keyboard handling, smooth scrolling, output save directory.
- **Backup:** Auto-backup schedule, backup type (local/cloud), last backup timestamps and error
  states.
- **AI:** Selected cloud AI provider, AI cache enabled status, which AI tools are active (command
  execution, QS tiles, package querying, database), cache duration in days. *(GitHub build only --
  not present in the F-Droid build.)*
- **Account (Google):** Your Google account email address and profile photo URL are stored locally
  when you sign in for Google Drive backup. The profile photo itself is also cached locally as a
  file on your device for offline display. *(GitHub build only -- not present in the F-Droid
  build.)*

### 1.10 Android System Backup

The App has Android's standard backup feature enabled (`android:allowBackup="true"`). This means
Android's Backup Service may include the App's data including databases and DataStore settings in
your device's regular system backups to Google's infrastructure, subject to your device's backup
settings and [Google's Privacy Policy](https://policies.google.com/privacy). You can disable App
data backup via your device's system settings.

---

## 2. Build Flavours

aShell You is distributed in two distinct build variants with different feature sets and library
dependencies. Understanding which build you are using is important for understanding this policy.

### 2.1 GitHub Build (`github` flavour)

Available from: [GitHub Releases](https://github.com/DP-Hridayan/aShellYou/releases)

This is the full-featured build. It includes all features described in this Privacy Policy,
including:

- The **AI Assistant** (powered by third-party LLM providers such as Google Gemini).
- **Google Drive** cloud backup and restore (via Google Play Services and the Google Drive API).
- **In-app update** functionality (APK download from GitHub).
- Proprietary Google libraries: `play-services-auth`, `credentials`, `google-api-services-drive`,
  `google-api-client-android`, and `google-http-client-gson`.

### 2.2 F-Droid Build (`fdroid` flavour)

Available from: [F-Droid](https://f-droid.org/en/packages/in.hridayan.ashell)
and [IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/in.hridayan.ashell)

This build is **100% Free and Open-Source Software (FOSS)**. It contains **no proprietary libraries
** and no Google Play Services dependencies. The following features and their associated data
practices are **completely absent** in this build -- not hidden or disabled, but entirely excluded
from the compiled APK:

| Feature removed                      | Data practices that do NOT apply                                                                                         |
|--------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| AI Assistant                         | No LLM API calls, no chat history, no AI analysis cache, no API key storage, no logcat/package data sent to AI providers |
| Google Drive Backup                  | No Google Sign-In, no OAuth, no Google account email/photo stored, no `AutoBackupWorker` cloud uploads                   |
| Google Gemini / Google Play Services | No proprietary Google libraries included in the APK                                                                      |

**What the F-Droid build retains:** All core shell features (Shizuku, Root, Wireless ADB, USB OTG),
local backup/restore, crash reporter, QS tiles, logcat, ADB sideload, command history, bookmarks,
and custom themes. Local backup functionality uses only local storage (no cloud option).

> **Note:** The `QUERY_ALL_PACKAGES` permission in the F-Droid build is used solely for command
> auto-complete suggestions (the AI tool use case is absent).

---

## 3. Android Permissions

The App requests the following Android permissions. Each is strictly necessary for the stated
purpose:

| Permission                                         | Why It Is Required                                                                                                                                                                                                                                                                                                                       |
|----------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` | To save shell command outputs to `.txt` files, import local shell scripts, and manage local backup files on external storage on older Android versions.                                                                                                                                                                                  |
| `MANAGE_EXTERNAL_STORAGE`                          | Required on Android 11+ to allow the backup and restore feature to read and write backup archive files from user-selected directories.                                                                                                                                                                                                   |
| `INTERNET`                                         | For Wireless ADB connections over a local TCP network; sending prompts to AI providers (Gemini API); fetching release metadata from GitHub; and syncing backups with Google Drive.                                                                                                                                                       |
| `ACCESS_NETWORK_STATE` / `ACCESS_WIFI_STATE`       | To detect network availability before initiating Wireless ADB connections or other network operations, and to surface network status in the UI.                                                                                                                                                                                          |
| `NEARBY_WIFI_DEVICES`                              | To discover other Android devices available for Wireless ADB pairing and connection on the local network using mDNS/NSD (Network Service Discovery).                                                                                                                                                                                     |
| `CHANGE_WIFI_MULTICAST_STATE`                      | Required by the JmDNS library to enable Multicast DNS packet reception for discovering ADB targets on the local Wi-Fi network.                                                                                                                                                                                                           |
| `POST_NOTIFICATIONS`                               | To display persistent foreground notifications for active Wireless ADB connections, running background logcat streams, and background backup operations, and to provide quick-action controls for them.                                                                                                                                  |
| `FOREGROUND_SERVICE`                               | Base permission required to run any foreground service, enabling long-running operations to continue while the app is not in the foreground.                                                                                                                                                                                             |
| `FOREGROUND_SERVICE_CONNECTED_DEVICE`              | Specifically required to run foreground services that maintain active ADB connections to connected devices over Wi-Fi or OTG, preventing the OS from terminating the TCP socket.                                                                                                                                                         |
| `FOREGROUND_SERVICE_DATA_SYNC`                     | Specifically required for the background Logcat streaming service to capture continuous logcat data without interruption.                                                                                                                                                                                                                |
| `USB_PERMISSION` / `android.hardware.usb.host`     | To communicate with secondary Android devices physically connected via a USB OTG cable for ADB command execution and file sideloading.                                                                                                                                                                                                   |
| `REQUEST_INSTALL_PACKAGES`                         | Used exclusively for the in-app update flow: after you explicitly choose to download and install a new release APK fetched from GitHub, this permission allows the App to initiate the installation.                                                                                                                                     |
| `QUERY_ALL_PACKAGES`                               | Required for two purposes: (1) to provide intelligent auto-complete suggestions for installed package names when you type commands like `pm grant <package>`; and (2) to allow the AI Assistant's `query_installed_apps` tool to retrieve a structured list of installed apps so it can generate accurate, context-aware shell commands. |
| `READ_PHONE_STATE`                                 | This permission is **explicitly removed** in the App manifest (`tools:node="remove"`). It is not requested or used. It appears only because a transitively included library declares it, and the App actively suppresses it.                                                                                                             |

---

## 4. Third-Party Services and Data Transmission

> **F-Droid build:** Sections 4.1 (AI), 4.2 (Google Drive), and 4.4 (Coil " Google profile photo) do
> not apply. The F-Droid build makes no contact with Google's servers and includes no Google
> proprietary libraries. The only external network call in the F-Droid build is to the GitHub API (
> Section 4.3) for checking updates, if you use that feature.

### 4.1 Google Gemini AI API

**GitHub build only. Not present in the F-Droid build.**

When you submit a query to the AI Assistant, the App constructs an HTTPS POST request to Google's
Gemini API (`https://generativelanguage.googleapis.com`). The request body includes:

- A system prompt instructing the model how to respond.
- Your message and the full conversation history of the current session.
- Definitions of available AI tools (schemas for `execute_command`, `get_structured_logcat`, and
  `query_installed_apps`).
- A generation configuration (output format, etc.).

**Contextual data sent by AI tools (only when the AI invokes them and only with your permission):**

- `execute_command`: The command string and target device mode. The App will always display a
  permission prompt before executing any command on behalf of the AI, unless you have previously
  granted blanket permission for that specific command.
- `get_structured_logcat`: Up to 500 lines of raw logcat output from your device, filtered by log
  level and an optional grep pattern. **Important:** Logcat output can contain sensitive data
  printed by other applications running on your device, including authentication tokens, session
  IDs, URLs, or other app-specific information. Review carefully before enabling this tool.
- `query_installed_apps`: A structured list of apps installed on your device (app name, package
  name, system/user classification).

Your API key is transmitted directly to Google's servers only (as a query parameter in the HTTPS
request) and is never sent to our servers. The AI's response is stored locally in your chat history
as described in Section 1.2.

For how Google handles this data, refer to
the [Google Gemini API Terms](https://ai.google.dev/gemini-api/terms)
and [Google's Privacy Policy](https://policies.google.com/privacy).

### 4.2 Google Drive (Backup and Restore)

**GitHub build only. Not present in the F-Droid build.**

The App uses Google Sign-In (`play-services-auth`) to authenticate with your Google Account. The
only OAuth scope requested is `https://www.googleapis.com/auth/drive.appdata`. This scope is *
*strictly limited** to a hidden `appDataFolder` inside your Drive that is invisible to you and
inaccessible to other apps. The App cannot read, list, modify, or delete any other files in your
Google Drive.

Data uploaded to this folder is an encrypted binary backup archive containing your settings (
DataStore preferences), command history, bookmarks, and QS tile configurations. The App's
`AutoBackupWorker` background task may perform this upload automatically and silently according to
the backup schedule you configure in the App's settings.

Refer to the [Google Privacy Policy](https://policies.google.com/privacy) for how Google handles
data stored in Drive.

### 4.3 GitHub API

The App sends standard unauthenticated HTTPS GET requests to
`https://api.github.com/repos/DP-Hridayan/aShellYou` (and the sub-paths `/releases` and
`/releases/latest`) to:

- Check for newer versions of the App.
- Download the release APK asset if you choose to update in-app.

No personal data or payload is sent. Only your device's IP address is visible to GitHub as part of
the standard HTTPS connection, subject
to [GitHub's Privacy Statement](https://docs.github.com/en/site-policy/privacy-policies/github-privacy-statement).

### 4.4 Coil (Image Loading)

**GitHub build only (used to display your Google account profile photo).**

The App uses the Coil3 image loading library to fetch and display images from URLs (such as your
Google account profile photo from Google's servers). Coil may cache these images locally on your
device to improve performance. No image data is sent to our servers.

---

## 5. Privileged Execution Engines (Shizuku and Root)

aShell You supports three local execution modes to run ADB shell commands on your own device:

### 5.1 Shizuku

[Shizuku](https://shizuku.rikka.app/) is a separate application that grants aShell You the ability
to execute ADB-level shell commands on your device without a connected PC. When Shizuku mode is
active, the App can run any command the `adb shell` user is authorized to execute, including reading
system logs, querying package information, and modifying device settings. All command execution is
initiated by you or by the AI Assistant with your explicit permission. No data is transmitted
externally as a result of using Shizuku.

### 5.2 Root (libsu)

If you grant the App root access, it executes commands as the root user (UID 0), giving it
theoretically unrestricted access to your device's filesystem, all installed app data, system
configurations, and protected system resources. This access is used solely to execute the commands
you type or authorize. No data is transmitted externally by the App as a result of root command
execution.

> **Caution:** Root access carries significant inherent risks. aShell You is not responsible for any
> damage caused by the execution of commands using root privileges. Only execute commands you
> understand and trust.

---

## 6. Local Network Activity

### 6.1 mDNS / Multicast DNS

When using the Wireless ADB feature, the App uses the JmDNS library to broadcast and listen for mDNS
packets on your local Wi-Fi network to discover other Android devices advertising ADB services. As
part of standard mDNS, your device's local IP address and the ADB service name may be visible to
other devices on the same local network segment. No data is sent to the internet through this
mechanism.

### 6.2 SPAKE2 Wireless Pairing

When pairing with a device for Wireless Debugging, the App uses the SPAKE2 (Password Authenticated
Key Exchange) cryptographic protocol to establish a mutually authenticated, encrypted channel using
your 6-digit pairing code. This ensures the pairing code and session keys are never transmitted in
plaintext, even on the local network.

### 6.3 ADB Sideload

The ADB Sideload feature allows you to select a file (e.g., a `.zip` OTA package) from your device's
storage using Android's standard file picker, and transfer it to a connected ADB target device (over
Wi-Fi or USB OTG) using the ADB sideload protocol. The App does not inspect or transmit the contents
of sideloaded files to any external server.

---

## 7. Files Written to Your Device

At your explicit request, the App may write the following to your device's storage:

- **Command output text files:** Saved to a directory of your choosing on external storage.
- **Local backup archives:** Encrypted backup files saved to a user-selected directory.
- **Downloaded release APKs:** Saved to the App's external files directory prior to installation.

These files are entirely under your control.

---

## 8. Data Retention and Deletion

Because all data is stored locally on your device or in your own Google Drive account, you have
complete control:

- **Delete all App data:** Android Settings Apps aShell You Storage Clear Data. This permanently
  removes all databases, preferences, ADB keys, cached files, profile photos, and API keys.
- **Delete AI chat history:** Use the session management options within the AI chat interface.
- **Delete crash logs:** Use the Crash History screen within the App.
- **Delete cloud backups:** Use the App's Backup & Restore screen, or access your Google Drive's app
  data directly.
- **Disable Android System Backup:** Android Settings Google Backup, or your device manufacturer's
  equivalent.

---

## 9. Children's Privacy

aShell You is a developer tool intended for users with technical knowledge of ADB and Linux shell
commands. The App is not directed at, nor intended for use by, children under the age of 13 (or the
applicable age in your jurisdiction). We do not knowingly process personal information from
children. If you believe a child has used this App in a way that involves personal information,
please contact us.

---

## 10. Security Measures

We take reasonable technical measures to protect data processed by the App:

- AI provider API keys are encrypted at rest using AES256_GCM (Google Tink) backed by the Android
  Keystore hardware security module.
- Local and cloud backup archives are encrypted before being written to disk or uploaded to Google
  Drive.
- ADB RSA key files are stored in the App's private internal storage, inaccessible to other
  applications.
- Wireless ADB pairing uses the SPAKE2 cryptographic protocol to prevent credential interception.
- All outbound network connections use TLS (HTTPS).

---

## 11. Changes to This Policy

We may update this Privacy Policy to reflect changes in the App's features or applicable law. When
we do, we will update the "Last Updated" date at the top of this document and note the change in the
App's release changelog. We encourage you to review this policy periodically. Continued use of the
App after changes are posted constitutes your acceptance of the updated policy.

---

## 12. Contact

If you have any questions, concerns, or requests regarding this Privacy Policy, please contact:

**Developer:** DP Hridayan</br>
**Email:** [hridayanofficial@gmail.com](mailto:hridayanofficial@gmail.com)</br>
**Project Repository:
** [https://github.com/DP-Hridayan/aShellYou](https://github.com/DP-Hridayan/aShellYou)

---

*This Privacy Policy is based on a thorough technical analysis of the aShell You application source
code and reflects the actual data handling behavior of the App as implemented.*





