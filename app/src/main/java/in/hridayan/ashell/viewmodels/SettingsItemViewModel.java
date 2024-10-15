package in.hridayan.ashell.viewmodels;

import android.util.Pair;
import androidx.lifecycle.ViewModel;

public class SettingsItemViewModel extends ViewModel {
  private boolean isToolbarExpanded = true;
  private Pair<Integer, Integer> scrollPosition;

  public void setScrollPosition(Pair<Integer, Integer> pair) {
    this.scrollPosition = pair;
  }

  public Pair<Integer, Integer> getScrollPosition() {
    return scrollPosition;
  }

  public boolean isToolbarExpanded() {
    return isToolbarExpanded;
  }

  public void setToolbarExpanded(boolean toolbarExpanded) {
    isToolbarExpanded = toolbarExpanded;
  }
}
