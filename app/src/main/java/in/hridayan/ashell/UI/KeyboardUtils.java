package in.hridayan.ashell.UI;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import in.hridayan.ashell.adapters.SettingsAdapter;

public class KeyboardUtils {

  public interface KeyboardVisibilityListener {
    void onKeyboardVisibilityChanged(boolean isVisible);
  }

  public static void attachVisibilityListener(
      Activity activity, final KeyboardVisibilityListener listener) {
    final View contentView = activity.findViewById(android.R.id.content);
    contentView
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              private boolean wasOpened;

              @Override
              public void onGlobalLayout() {
                Rect r = new Rect();
                contentView.getWindowVisibleDisplayFrame(r);
                int screenHeight = contentView.getRootView().getHeight();

                int heightDiff = screenHeight - (r.bottom - r.top);

                boolean isVisible = heightDiff > 500;

                if (isVisible != wasOpened) {
                  wasOpened = isVisible;
                  listener.onKeyboardVisibilityChanged(isVisible);
                }
              }
            });
  }

  public static void disableKeyboard(SettingsAdapter adapter, Activity activity, View view) {

    InputMethodManager imm =
        (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

    boolean disableSoftKey = adapter.getSavedSwitchState("id_disable_softkey");
    if (disableSoftKey) {
      if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

      activity
          .getWindow()
          .setFlags(
              WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
              WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    } else {
      activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }
  }
}
