package in.hridayan.ashell.activities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class FabExtendingOnScrollListener extends RecyclerView.OnScrollListener {

    private final ExtendedFloatingActionButton fab;
    private static final int FAST_SCROLL_THRESHOLD = 25; // Adjust as needed

    public FabExtendingOnScrollListener(ExtendedFloatingActionButton fab) {
        this.fab = fab;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if (recyclerView.computeVerticalScrollOffset() == 0) { // Top of the list
                fab.extend();
            }
        }
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (dy > 0 && fab.isExtended() && Math.abs(dy) >= FAST_SCROLL_THRESHOLD) { // Scroll down
            fab.shrink();
        } else if (dy < 0 && !fab.isExtended() && Math.abs(dy) >= FAST_SCROLL_THRESHOLD) { // Fast scroll up
            fab.extend();
        }
    }
   

}
