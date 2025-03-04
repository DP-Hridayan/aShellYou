package in.hridayan.ashell.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SystemMountHelper {

    // Method to check if /system is read-only
    public static boolean isSystemReadOnly() {
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/mounts");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.contains("/system")) {
                    // If /system is mounted as "ro" (read-only), return true
                    if (line.contains("ro,")) {
                        return true;
                    }
                    break;
                }
            }
            reader.close();
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // If no read-only flag found, assume read-write
    }

    // Method to remount /system as read-write
    public static boolean remountSystemRW() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStreamWriter os = new OutputStreamWriter(process.getOutputStream());
            
            // Command to remount /system as read-write
            os.write("mount -o remount,rw /\n");
            os.write("exit\n");
            os.flush();
            os.close();

            int exitCode = process.waitFor();
            return exitCode == 0; // Return true if command executed successfully
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        if (isSystemReadOnly()) {
            System.out.println("System is read-only. Attempting to remount...");
            if (remountSystemRW()) {
                System.out.println("Successfully remounted /system as read-write.");
            } else {
                System.out.println("Failed to remount /system.");
            }
        } else {
            System.out.println("System is already read-write.");
        }
    }
}