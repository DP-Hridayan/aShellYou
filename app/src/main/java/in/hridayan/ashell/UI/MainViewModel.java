package in.hridayan.ashell.UI;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
  private static final int nullValue = 2004;
  private int currentFragment = nullValue;

  private String useCommand;

  public void setUseCommand(String text) {
    this.useCommand = text;
  }

  public String getUseCommand() {
    return useCommand;
  }

  public int currentFragment() {
    return currentFragment;
  }

  public void setCurrentFragment(int fragment) {
    currentFragment = fragment;
  }

  public boolean isFragmentSaved() {
    return currentFragment != nullValue;
  }
}
