package in.hridayan.ashell.UI;

import java.util.List;
import androidx.lifecycle.ViewModel;

public class aShellFragmentViewModel extends ViewModel {

  private boolean isEditTextFocused;

  private List<String> shellOutput;
  private int scrollPosition;

  public List<String> getShellOutput() {
    return shellOutput;
  }

  public void setShellOutput(List<String> shellOutput) {
    this.shellOutput = shellOutput;
  }

  public int getScrollPosition() {
    return scrollPosition;
  }

  public void setScrollPosition(int scrollPosition) {
    this.scrollPosition = scrollPosition;
  }

  public boolean isEditTextFocused() {
    return isEditTextFocused;
  }

  public void setEditTextFocused(boolean editTextFocused) {
    isEditTextFocused = editTextFocused;
  }
}
