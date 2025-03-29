package in.hridayan.ashell.viewmodels;

import android.util.Pair;
import androidx.lifecycle.ViewModel;

public class AboutViewModel extends ViewModel {
    private boolean isToolbarExpanded = true;
    private Pair<Integer, Integer> rvPositionAndOffset;

    public void setRVPositionAndOffset(Pair<Integer, Integer> pair) {
        this.rvPositionAndOffset = pair;
    }

    public Pair<Integer, Integer> getRVPositionAndOffset() {
        return rvPositionAndOffset;
    }

    public boolean isToolbarExpanded() {
        return isToolbarExpanded;
    }

    public void setToolbarExpanded(boolean toolbarExpanded) {
        isToolbarExpanded = toolbarExpanded;
    }
}