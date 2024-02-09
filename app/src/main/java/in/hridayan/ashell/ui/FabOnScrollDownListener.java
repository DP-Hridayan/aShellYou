package in.hridayan.ashell.activities;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FabOnScrollDownListener extends RecyclerView.OnScrollListener {

  private final FloatingActionButton fab;
  private Handler visibilityHandler = new Handler(Looper.getMainLooper());
  private Runnable hideFabRunnable =
      new Runnable() {
        @Override
        public void run() {
          fab.hide();
        }
      };

  private static final int FAST_SCROLL_THRESHOLD = 90; // Adjust as needed
  private static final int VISIBILITY_DELAY_MILLIS = 2000; // Delay before hiding FAB

  public FabOnScrollDownListener(FloatingActionButton fab) {
    this.fab = fab;
  }

  @Override
  public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
    super.onScrolled(recyclerView, dx, dy);

    if (dy > 0 && Math.abs(dy) >= FAST_SCROLL_THRESHOLD) {
      visibilityHandler.removeCallbacks(hideFabRunnable);
      fab.show();

    } else if (dy < 0 && Math.abs(dy) >= 30) {

      fab.hide();

    } else {
      visibilityHandler.postDelayed(hideFabRunnable, VISIBILITY_DELAY_MILLIS);
    }
  }
}
