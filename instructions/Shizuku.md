# 🔥🔥 Shizuku Setup Guide

Welcome to the Shizuku setup guide! Shizuku is a powerful tool that allows you to manage app permissions with ease. Follow the steps below to set up Shizuku using your preferred method.

## 🚀 Installation

1. Download and install the Shizuku app from the [Google Play Store](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api).


## 🔧 Setup Methods

### 1. 🔓 Using Root Access

1. **Root Your Device:**
   - Ensure your device is rooted. (Use tools like Magisk for rooting.)

2. **Grant Root Permissions:**
   - Open the Shizuku app.
   - Click Start By Root Option.
   - Grant Root Access.
   
3. **Start Shizuku:**
   - Tap on **Start**.

### 2. 📶 Using Wireless Debugging (Android 11+)

1. **Enable Developer Options:**
   - Go to **Settings** > **About phone**.
   - Tap on **Build number** 7 times to enable Developer Options.

2. **Enable Wireless Debugging:**
   - Go to **Settings** > **Developer options**.
   - Toggle on **Wireless debugging**.

3. **Pair Device for Wireless Debugging:**
   - Open **Developer options** > **Wireless debugging** > **Pair device with pairing code**. (It will popup in Notification Shade)
   - Follow the on-screen instructions to pair your device.

4. **Start Shizuku:**
   - Open the Shizuku app on your device.
   - Tap on **Start**.

### 3. 💻 Using ADB (Android Debug Bridge)

1. **Enable Developer Options:**
   - Go to **Settings** > **About phone**.
   - Tap on **Build number** 7 times to enable Developer Options.

2. **Enable USB Debugging:**
   - Go to **Settings** > **Developer options**.
   - Toggle on **USB debugging**.

3. **Install ADB:**
   - Download the [SDK Platform Tools](https://developer.android.com/studio/releases/platform-tools) from Google and extract it.

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

## 💡 Tips

- Make sure to restart Shizuku after a reboot.
- Check the [Shizuku documentation](https://shizuku.rikka.app/guide/setup/) for more detailed instructions and troubleshooting.

## 🛠️ Support

If you encounter any issues, feel free to open an issue on our [GitHub repository](https://github.com/dp-hridayan/ashellyou/issues).🎉