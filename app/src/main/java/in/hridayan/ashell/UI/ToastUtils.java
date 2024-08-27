package in.hridayan.ashell.UI;

import android.widget.Toast;
import android.content.Context;

public class ToastUtils {
  private static Toast toast;
  public static int LENGTH_SHORT = 0, LENGTH_LONG = 1;

  public static void showToast(Context context, String message, int length) {
    if (toast != null) {
      toast.cancel();
    }
    toast = Toast.makeText(context, message, length);
    toast.show();
  }

  public static void showToast(Context context, int resId, int length) {
    showToast(context, context.getString(resId), length);
  }
}
