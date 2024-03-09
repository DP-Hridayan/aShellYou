package in.hridayan.ashell.UI;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavOnScrollListener extends RecyclerView.OnScrollListener {

  private final BottomNavigationView mNav;

  public BottomNavOnScrollListener(BottomNavigationView mNav) {
    this.mNav = mNav;
  }

  @Override
  public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
    super.onScrollStateChanged(recyclerView, newState);

    int totalScrollRange = recyclerView.computeVerticalScrollRange();
    int currentScrollOffset = recyclerView.computeVerticalScrollOffset();
    int visibleScrollRange = recyclerView.computeVerticalScrollExtent();

    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
      if (currentScrollOffset + visibleScrollRange >= totalScrollRange) {
        // Bottom of the list
        mNav.animate().translationY(mNav.getHeight());
      }
    }
  }

  private static final int SCROLL_THRESHOLD = 50;

  @Override
  public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
    super.onScrolled(recyclerView, dx, dy);

    if (dy > SCROLL_THRESHOLD) {
      mNav.animate().translationY(mNav.getHeight());
    } else if (dy < -SCROLL_THRESHOLD) {
      mNav.animate().translationY(0);
    }
  }
}
