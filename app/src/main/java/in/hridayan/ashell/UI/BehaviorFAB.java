package in.hridayan.ashell.UI;

import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.preference.Preference;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.Utils;
import java.util.Objects;

public class BehaviorFAB {

  private static final int FAST_SCROLL_THRESHOLD = 90;
  private static final int VISIBILITY_DELAY_MILLIS = 2000;
  private static final int FAST_SCROLL_THRESHOLD_EXTENDED = 25;

  public static class FabOnScrollUpListener extends RecyclerView.OnScrollListener {

    private final FloatingActionButton fab;
    private Handler visibilityHandler = new Handler(Looper.getMainLooper());
    private Runnable hideFabRunnable =
        new Runnable() {
          @Override
          public void run() {
            fab.hide();
          }
        };

    public FabOnScrollUpListener(FloatingActionButton fab) {
      this.fab = fab;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
      super.onScrolled(recyclerView, dx, dy);

      LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
      int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

      if (firstVisibleItemPosition == 0) {
        fab.hide();
      } else if (dy < 0 && Math.abs(dy) >= FAST_SCROLL_THRESHOLD) {
        visibilityHandler.removeCallbacks(hideFabRunnable);
        fab.show();
      } else if (dy > 0 && Math.abs(dy) >= 30) {
        fab.hide();
      } else {
        visibilityHandler.postDelayed(hideFabRunnable, VISIBILITY_DELAY_MILLIS);
      }
    }
  }

  private FabOnScrollUpListener fabOnScrollUpListener;

  public BehaviorFAB(FloatingActionButton fab) {
    fabOnScrollUpListener = new FabOnScrollUpListener(fab);
  }

  public static class FabOnScrollDownListener extends RecyclerView.OnScrollListener {

    private final FloatingActionButton fab;
    private Handler visibilityHandler = new Handler(Looper.getMainLooper());
    private Runnable hideFabRunnable =
        new Runnable() {
          @Override
          public void run() {
            fab.hide();
          }
        };

    public FabOnScrollDownListener(FloatingActionButton fab) {
      this.fab = fab;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
      super.onScrolled(recyclerView, dx, dy);

      LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
      int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
      int totalItemCount = layoutManager.getItemCount();

      if (lastVisibleItemPosition == totalItemCount - 1) {
        fab.hide();
      } else if (dy > 0 && Math.abs(dy) >= FAST_SCROLL_THRESHOLD) {
        visibilityHandler.removeCallbacks(hideFabRunnable);
        fab.show();
      } else if (dy < 0 && Math.abs(dy) >= 30) {
        fab.hide();
      } else {
        visibilityHandler.postDelayed(hideFabRunnable, VISIBILITY_DELAY_MILLIS);
      }
    }
  }

  public static class FabExtendingOnScrollListener extends RecyclerView.OnScrollListener {

    private final ExtendedFloatingActionButton fab;

    public FabExtendingOnScrollListener(ExtendedFloatingActionButton fab) {
      this.fab = fab;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      if (newState == RecyclerView.SCROLL_STATE_IDLE) {
        if (recyclerView.computeVerticalScrollOffset() == 0) {
          fab.extend();
        }
      }
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
      super.onScrolled(recyclerView, dx, dy);
      if (dy > 0 && fab.isExtended() && Math.abs(dy) >= FAST_SCROLL_THRESHOLD_EXTENDED) {
        fab.shrink();
      } else if (dy < 0 && !fab.isExtended() && Math.abs(dy) >= FAST_SCROLL_THRESHOLD_EXTENDED) {
        fab.extend();
      }
    }
  }

  public static void handleTopAndBottomArrow(
      FloatingActionButton topButton,
      FloatingActionButton bottomButton,
      RecyclerView recyclerView,
      Context context) {
    topButton.setOnClickListener(
        new View.OnClickListener() {
          private long lastClickTime = 0;

          @Override
          public void onClick(View v) {

            long currentTime = System.currentTimeMillis();

            long timeDifference = currentTime - lastClickTime;

            if (timeDifference < 200) {
              recyclerView.scrollToPosition(0);
            } else {

              boolean switchState = Preferences.getSmoothScroll(context);
              if (switchState) {

                recyclerView.smoothScrollToPosition(0);
              } else {
                recyclerView.scrollToPosition(0);
              }
            }

            lastClickTime = currentTime;
          }
        });

    bottomButton.setOnClickListener(
        new View.OnClickListener() {
          private long lastClickTime = 0;

          @Override
          public void onClick(View v) {

            long currentTime = System.currentTimeMillis();

            long timeDifference = currentTime - lastClickTime;

            if (timeDifference < 200) {
              recyclerView.scrollToPosition(
                  Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 1);
            } else {

              boolean switchState = Preferences.getSmoothScroll(context);
              if (switchState) {
                recyclerView.smoothScrollToPosition(
                    Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 1);

              } else {
                recyclerView.scrollToPosition(
                    Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 1);
              }
            }

            lastClickTime = currentTime;
          }
        });
  }

  public static void pasteAndUndo(
      ExtendedFloatingActionButton paste, FloatingActionButton undo, TextInputEditText editText) {
    Handler mHandler = new Handler(Looper.getMainLooper());

    paste.setOnClickListener(
        v -> {
          undo.show();
          mHandler.postDelayed(
              () -> {
                undo.hide();
                mHandler.removeCallbacksAndMessages(null);
              },
              3000);
          Utils.pasteFromClipboard(editText);
        });

    undo.setOnClickListener(
        v -> {
          editText.setText(null);
          undo.hide();
          mHandler.removeCallbacksAndMessages(null);
        });
  }
}
