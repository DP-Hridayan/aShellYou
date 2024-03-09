package in.hridayan.ashell.UI;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

public class KeyboardVisibilityChecker {

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

                // Calculate the height difference between the screen height and visible window
                // height
                int heightDiff = screenHeight - (r.bottom - r.top);

                // If the height difference is greater than 200 pixels, assume the keyboard is
                // visible
                boolean isVisible = heightDiff > 200;

                if (isVisible != wasOpened) {
                  wasOpened = isVisible;
                  listener.onKeyboardVisibilityChanged(isVisible);
                }
              }
            });
  }
}
