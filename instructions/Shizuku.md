# Shizuku Setup Guide

Shizuku is a powerful service that allows apps to use system-level ADB permissions without root (or with root, if available). This guide walks you through the three main setup methods.

</br>

## Installation

1. Download the Shizuku app from the [Google Play Store](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api).
2. Install it on your device like any regular app.

</br>

## Setup Methods

### Method 1: Using Root Access

1. **Ensure your device is rooted:**
   - Use tools like **Magisk** to root your device if it's not rooted yet.

2. **Grant Root Permissions:**
   - Open the Shizuku app.
   - Tap **Start By Root** and grant root access when prompted.

3. **Start Shizuku:**
   - Tap on **Start** in the Shizuku app to initiate it.

</br>

### Method 2: Using Wireless Debugging (Android 11+)

1. **Enable Developer Options:**
   - Go to **`Settings > About phone`**.
   - Tap on **`Build number`** 7 times to enable Developer Options.

2. **Enable Wireless Debugging:**
   - Go to **`Settings > Developer options`**.
   - Toggle on **`Wireless debugging`**.

3. **Pair Device for Wireless Debugging:**
   - Open **`Developer options > Wireless debugging > Pair device with pairing code`**. (A pairing code will appear in the notification shade).
   - Follow the on-screen instructions to pair your device.

4. **Start Shizuku:**
   - Open the Shizuku app on your device.
   - Tap on **Start**.

</br>

### Method 3: Using ADB (Android Debug Bridge)

1. **Enable Developer Options:**
   - Go to **`Settings > About phone`**.
   - Tap on **`Build number`** 7 times to enable Developer Options.

2. **Enable USB Debugging:**
   - Go to **`Settings > Developer options`**.
   - Toggle on **`USB debugging`**.

3. **Install ADB:**
   - Download the [SDK Platform Tools](https://developer.android.com/studio/releases/platform-tools) from Google.
   - Extract the downloaded zip to a folder on your computer.

4. **Connect to ADB:**
   - Connect your device to your computer via USB.
   - Open a terminal/command prompt in the SDK Platform Tools directory.
   - Run the following command:
     ```sh
     adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
     ```

5. **Start Shizuku:**
   - Open the Shizuku app on your device.
   - Tap on **Start**.

</br>

## Tips

- Make sure to restart Shizuku after a device reboot.
- For detailed instructions and troubleshooting, check the official [Shizuku documentation](https://shizuku.rikka.app/guide/setup/).

</br>

## Support

If you run into any issues or want to ask questions, feel free to:

- Join our [Telegram Community](https://t.me/aShellYou)
- Open an [Issue on GitHub](https://github.com/DP-Hridayan/aShellYou/issues)
