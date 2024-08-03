package in.hridayan.ashell.viewmodels;

import in.hridayan.ashell.utils.SettingsItem;
import java.util.List;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
  private boolean isToolbarExpanded = true;

  private List<SettingsItem> settingsData;
  private int scrollPosition;

  public List<SettingsItem> getSettingsData() {
    return settingsData;
  }

  public void setSettingsData(List<SettingsItem> settingsData) {
    this.settingsData = settingsData;
  }

  public int getScrollPosition() {
    return scrollPosition;
  }

  public void setScrollPosition(int scrollPosition) {
    this.scrollPosition = scrollPosition;
  }

  public boolean isToolbarExpanded() {
    return isToolbarExpanded;
  }

  public void setToolbarExpanded(boolean toolbarExpanded) {
    isToolbarExpanded = toolbarExpanded;
  }
}
