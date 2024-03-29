package in.hridayan.ashell.UI;

import java.util.List;
import androidx.lifecycle.ViewModel;

public class aShellFragmentViewModel extends ViewModel {

  private boolean isEditTextFocused, isSaveButtonVisible, isSendDrawable;
  private String commandText;

  private List<String> shellOutput, history;

  private int scrollPosition;

  public List<String> getShellOutput() {
    return shellOutput;
  }

  public void setShellOutput(List<String> shellOutput) {
    this.shellOutput = shellOutput;
  }


  public List<String> getHistory() {
    return history;
  }

  public void setHistory(List<String> history) {
    this.history = history;
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

  public String getCommandText() {
    return commandText;
  }

  public void setCommandText(String commandText) {
    this.commandText = commandText;
  }

  public boolean isSaveButtonVisible() {
    return isSaveButtonVisible;
  }

  public void setSaveButtonVisible(boolean saveButtonVisible) {
    isSaveButtonVisible = saveButtonVisible;
  }

  public boolean isSendDrawable() {
    return isSendDrawable;
  }

  public void setSendDrawable(boolean sendDrawable) {
    isSendDrawable = sendDrawable;
  }
}
