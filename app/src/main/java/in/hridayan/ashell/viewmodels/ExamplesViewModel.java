package in.hridayan.ashell.viewmodels;

import android.util.Pair;
import androidx.lifecycle.ViewModel;

public class ExamplesViewModel extends ViewModel {
  private boolean isToolbarExpanded = true;
    private boolean isEnteringFromSettings;
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

    public void setEnteringFromSettings(boolean value){
        isEnteringFromSettings = value;
    }

    public boolean isEnteringFromSettings(){
        return isEnteringFromSettings;
    }
}
