package in.hridayan.ashell.viewmodels;

import androidx.lifecycle.ViewModel;
import in.hridayan.ashell.config.Const;

public class MainViewModel extends ViewModel {
  private static final int nullValue = 2004;
  private int currentFragment = nullValue,
      previousFragment = Const.LOCAL_FRAGMENT,
      whichHomeFragment;
  private String useCommand, selectedWifiAdbDevice;

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

  public int previousFragment() {
    return previousFragment;
  }

  public void setPreviousFragment(int fragment) {
    previousFragment = fragment;
  }

  public int whichHomeFragment() {
    return whichHomeFragment;
  }

  public void setHomeFragment(int fragment) {
    whichHomeFragment = fragment;
  }

  public String getSelectedWifiAdbDevice() {
    return selectedWifiAdbDevice;
  }

  public void setSelectedWifiAdbDevice(String value) {
    this.selectedWifiAdbDevice = value;
  }
}
