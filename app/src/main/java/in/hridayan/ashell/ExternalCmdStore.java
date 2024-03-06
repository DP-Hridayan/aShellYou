package in.hridayan.ashell.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ExternalCmdStore {
    private static SharedPreferences sharedPreferences;
    private static String CMD_KEY = "cmd_key";
    private static void initShared(Context context){
        if (sharedPreferences==null)
            sharedPreferences = context.getSharedPreferences("cmd",Context.MODE_PRIVATE);
    }

    public static void put(Context context,String cmd){
        initShared(context);
        sharedPreferences.edit().putString(CMD_KEY,cmd).apply();
    }

    public static String get(Context context){
        initShared(context);
        return sharedPreferences.getString(CMD_KEY,null);
    }
}
