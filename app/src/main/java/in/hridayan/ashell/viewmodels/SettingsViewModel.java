package in.hridayan.ashell.viewmodels;

import android.util.Pair;
import in.hridayan.ashell.utils.SettingsItem;
import java.util.List;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
  private boolean isToolbarExpanded = true;

  private List<SettingsItem> settingsData;
  private int scrollPosition;
  private Pair<Integer, Integer> rvPositionAndOffset;

  public List<SettingsItem> getSettingsData() {
    return settingsData;
  }

  public void setSettingsData(List<SettingsItem> settingsData) {
    this.settingsData = settingsData;
  }

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
