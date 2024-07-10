package in.hridayan.ashell.UI;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.Utils;
import java.util.Objects;

public class BehaviorFAB {

  private static final int FAST_SCROLL_THRESHOLD = 70;
  private static final int VISIBILITY_DELAY_MILLIS = 2000;
  private static final int FAST_SCROLL_THRESHOLD_EXTENDED = 25;

  // Function class for visibility of Top Scroll Button in Local Shell Fragment
  public static class FabLocalScrollUpListener extends RecyclerView.OnScrollListener {

    private final FloatingActionButton fab;
    private Handler visibilityHandler = new Handler(Looper.getMainLooper());
    private Runnable hideFabRunnable =
        new Runnable() {
          @Override
          public void run() {
            fab.hide();
          }
        };

    public FabLocalScrollUpListener(FloatingActionButton fab) {
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

  // Function class for visibility of Top Scroll Button in Otg Shell Fragment
  public static class FabOtgScrollUpListener implements ViewTreeObserver.OnScrollChangedListener {

    private final CoordinatedNestedScrollView scrollView;
    private final FloatingActionButton fab;
    private int lastScrollY = 0;
    private Handler visibilityHandler = new Handler(Looper.getMainLooper());
    private Runnable hideFabRunnable =
        new Runnable() {
          @Override
          public void run() {
            fab.hide();
          }
        };

    public FabOtgScrollUpListener(
        CoordinatedNestedScrollView scrollView, FloatingActionButton fab) {
      this.scrollView = scrollView;
      this.fab = fab;
      this.scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
    }

    @Override
    public void onScrollChanged() {
      int scrollY = scrollView.getScrollY();

      if (scrollY == 0) {
        fab.hide();
      } else {
        int dy = scrollY - lastScrollY;

        if (dy < 0 && Math.abs(dy) >= FAST_SCROLL_THRESHOLD) {
          visibilityHandler.removeCallbacks(hideFabRunnable);
          fab.show();
        } else if (dy > 0 && Math.abs(dy) >= FAST_SCROLL_THRESHOLD) {
          fab.hide();
        } else {
          visibilityHandler.postDelayed(hideFabRunnable, VISIBILITY_DELAY_MILLIS);
        }

        lastScrollY = scrollY;
      }
    }
  }

  private FabLocalScrollUpListener FabLocalScrollUpListener;

  public BehaviorFAB(FloatingActionButton fab) {
    FabLocalScrollUpListener = new FabLocalScrollUpListener(fab);
  }

  // Function class for visibility of  Bottom Scroll Button in Local Shell Fragment
  public static class FabLocalScrollDownListener extends RecyclerView.OnScrollListener {

    private final FloatingActionButton fab;
    private Handler visibilityHandler = new Handler(Looper.getMainLooper());
    private Runnable hideFabRunnable =
        new Runnable() {
          @Override
          public void run() {
            fab.hide();
          }
        };

    public FabLocalScrollDownListener(FloatingActionButton fab) {
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

  // Function class for visibility of Bottom Scroll Button in Otg Shell Fragment
  public static class FabOtgScrollDownListener implements ViewTreeObserver.OnScrollChangedListener {

    private final CoordinatedNestedScrollView scrollView;
    private final FloatingActionButton fab;
    private int lastScrollY = 0;
    private Handler visibilityHandler = new Handler(Looper.getMainLooper());
    private Runnable hideFabRunnable =
        new Runnable() {
          @Override
          public void run() {
            fab.hide();
          }
        };

    public FabOtgScrollDownListener(
        CoordinatedNestedScrollView scrollView, FloatingActionButton fab) {
      this.scrollView = scrollView;
      this.fab = fab;
      this.scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
    }

    @Override
    public void onScrollChanged() {
      int scrollY = scrollView.getScrollY();
      int scrollRange = scrollView.getChildAt(0).getHeight() - scrollView.getHeight();
      int scrollDiff = scrollRange - scrollY;

      if (scrollDiff <= 0) {
        fab.hide();
      } else {
        int dy = scrollY - lastScrollY;

        if (dy > 0 && Math.abs(dy) >= FAST_SCROLL_THRESHOLD) {
          visibilityHandler.removeCallbacks(hideFabRunnable);
          fab.show();
        } else if (dy < 0 && Math.abs(dy) >= FAST_SCROLL_THRESHOLD) {
          fab.hide();
        } else {
          visibilityHandler.postDelayed(hideFabRunnable, VISIBILITY_DELAY_MILLIS);
        }

        lastScrollY = scrollY;
      }
    }
  }

  // This class controls the extend and shrink of the extending floating action button behavior in
  // Local Shell Fragment
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

  // This class controls the extend and shrink of the extending floating action button in OTG
  // Fragment
  public static class FabExtendingOnScrollViewListener
      implements ViewTreeObserver.OnScrollChangedListener {

    private final CoordinatedNestedScrollView scrollView;
    private final ExtendedFloatingActionButton fab;
    private int lastScrollY = 0;

    public FabExtendingOnScrollViewListener(
        CoordinatedNestedScrollView scrollView, ExtendedFloatingActionButton fab) {
      this.scrollView = scrollView;
      this.fab = fab;
      this.scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
    }

    @Override
    public void onScrollChanged() {
      int scrollY = scrollView.getScrollY();

      // ScrollView is at the top
      if (scrollY == 0) {
        fab.extend();
      } else {
        int dy = scrollY - lastScrollY;
        if (Math.abs(dy) >= FAST_SCROLL_THRESHOLD_EXTENDED) {
          if (dy > 0 && fab.isExtended()) {
            fab.shrink();
          } else if (dy < 0 && !fab.isExtended()) {
            fab.extend();
          }
        }
      }

      lastScrollY = scrollY;
    }
  }

  // Handles OnClickListener for Top and Bottom scrolling buttons
  public static void handleTopAndBottomArrow(
      FloatingActionButton topButton,
      FloatingActionButton bottomButton,
      RecyclerView recyclerView,
      CoordinatedNestedScrollView scrollView,
      Context context,
      String fragment) {
    topButton.setOnClickListener(
        new View.OnClickListener() {
          private long lastClickTime = 0;

          @Override
          public void onClick(View v) {

            long currentTime = System.currentTimeMillis();

            long timeDifference = currentTime - lastClickTime;

            if (timeDifference < 200) {
              topScroll(fragment, recyclerView, scrollView);

            } else {

              boolean switchState = Preferences.getSmoothScroll(context);
              if (switchState) {
                smoothTopScroll(fragment, recyclerView, scrollView);
              } else {
                topScroll(fragment, recyclerView, scrollView);
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
              bottomScroll(fragment, recyclerView, scrollView);

            } else {

              boolean switchState = Preferences.getSmoothScroll(context);
              if (switchState) {

                smoothBottomScroll(fragment, recyclerView, scrollView);

              } else {

                bottomScroll(fragment, recyclerView, scrollView);
              }
            }

            lastClickTime = currentTime;
          }
        });
  }

  // Function of Top Scrolling
  private static void topScroll(
      String fragment, RecyclerView recyclerView, CoordinatedNestedScrollView scrollView) {

    if (fragment == "local_shell") {
      recyclerView.scrollToPosition(0);
    } else {
      scrollView.scrollTo(0, 0);
    }
  }

  // Function of Smooth Top Scrolling
  private static void smoothTopScroll(
      String fragment, RecyclerView recyclerView, CoordinatedNestedScrollView scrollView) {

    if (fragment == "local_shell") {
      recyclerView.smoothScrollToPosition(0);
    } else {
      scrollView.smoothScrollTo(0, 0);
    }
  }

  // Function of Bottom Scrolling
  private static void bottomScroll(
      String fragment, RecyclerView recyclerView, CoordinatedNestedScrollView scrollView) {
    if (fragment == "local_shell") {
      recyclerView.scrollToPosition(
          Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 1);
    } else {
      scrollView.fullScroll(View.FOCUS_DOWN);
    }
  }

  // Function of Smooth Bottom Scrolling
  private static void smoothBottomScroll(
      String fragment, RecyclerView recyclerView, CoordinatedNestedScrollView scrollView) {
    if (fragment == "local_shell") {
      recyclerView.smoothScrollToPosition(
          Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 1);
    } else {
      scrollView.smoothScrollTo(0, scrollView.getChildAt(0).getBottom());
    }
  }

  // The onclick listener for paste and undo button

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
