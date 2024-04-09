package in.hridayan.ashell.UI;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
  private static final int nullValue = 2004;
  private int currentFragment = nullValue;

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
