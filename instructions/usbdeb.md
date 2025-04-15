# USB Debugging Setup Guide

Enable USB Debugging to unlock advanced developer features and connect your Android device for ADB operations.

</br>

## Prerequisites

- An Android device
- A USB cable

</br>

## Steps to Enable USB Debugging

1. **Open your device settings**
   - Launch the `Settings` app on your Android device.

2. **Go to About Phone**
   - Scroll to the bottom and tap on **About Phone**.

3. **Locate the Build Number**
   - Look for the **Build Number** entry.  
     *Note: On some devices, this might be under `Settings > About Phone > Software Information`.*

4. **Enable Developer Mode**
   - Tap the **Build Number** entry **seven times**.
   - You should see a toast saying:  
     `"You are now a developer!"`

5. **Open Developer Options**
   - Go back to the main `Settings` menu.
   - Navigate to **`System > Developer Options`**  
     *(or just **`Developer Options`**, depending on your device).*

6. **Enable USB Debugging**
   - Scroll down to find **`USB Debugging`**.
   - Toggle it **ON**.

7. **Confirm the Action**
   - A prompt will appear. Tap **OK** to confirm and enable USB Debugging.

</br>

## You're All Set

Your device is now ready to communicate over ADB. You can connect it to another device or PC and start issuing ADB commands using tools like `aShellYou`.

For more help, join our [Telegram Community](https://t.me/aShellYou) or check out the [full documentation](https://github.com/DP-Hridayan/aShellYou).
