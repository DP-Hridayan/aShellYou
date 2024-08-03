package in.hridayan.ashell.viewmodels;

import androidx.lifecycle.ViewModel;

public class ChangelogViewModel extends ViewModel {
  private boolean isToolbarExpanded = true;

  public boolean isToolbarExpanded() {
    return isToolbarExpanded;
  }

  public void setToolbarExpanded(boolean toolbarExpanded) {
    isToolbarExpanded = toolbarExpanded;
  }
}
