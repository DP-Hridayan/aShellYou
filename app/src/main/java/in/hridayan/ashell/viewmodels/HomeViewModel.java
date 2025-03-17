package in.hridayan.ashell.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {
  private final MutableLiveData<Integer> scrollY = new MutableLiveData<>(0);
  private boolean isDeviceRooted = false;

  public void setScrollY(int y) {
    scrollY.setValue(y);
  }

  public LiveData<Integer> getScrollY() {
    return scrollY;
  }

  public void setDeviceRooted(boolean value) {
    isDeviceRooted = value;
  }

  public boolean isDeviceRooted() {
    return isDeviceRooted;
  }
}
