package in.hridayan.ashell.UI;

import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
  private int currentFragment;

  public int currentFragment() {
    return currentFragment;
  }

  public void setCurrentFragment(int fragment) {
    currentFragment = fragment;
  }
}
