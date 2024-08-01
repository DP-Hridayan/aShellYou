package in.hridayan.ashell.UI;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
  private static final int nullValue = 2004;
  private int currentFragment = nullValue;
    
   private final MutableLiveData<String> useCommand = new MutableLiveData<>();

    public void setUseCommand(String text) {
        useCommand.setValue(text);
    }

    public LiveData<String> getUseCommand() {
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
