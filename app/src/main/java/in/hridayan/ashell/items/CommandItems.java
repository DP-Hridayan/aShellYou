package in.hridayan.ashell.items;

import android.content.Context;
import java.io.Serializable;
import in.hridayan.ashell.config.Preferences;

public class CommandItems implements Serializable {

  private final String command, description, example;
  private int useCounter;
  private Context context;
  private boolean isChecked, isPinned;

  public CommandItems(String command, String example, String description, Context context) {
    this.command = command;
    this.description = description;
    this.example = example;
    this.context = context;
    this.useCounter = Preferences.getUseCounter(command);
    this.isPinned = Preferences.getPinned(command);
  }

  public String getTitle() {
    return command;
  }

  public String getSummary() {
    return description;
  }

  public String getExample() {
    return example;
  }

  public int getUseCounter() {
    return useCounter;
  }

  public void setUseCounter(int counter) {
    this.useCounter = counter;
    Preferences.setUseCounter( command, counter);
  }

  public boolean isPinned() {
    return isPinned;
  }

  public void setPinned(boolean pinned) {
    this.isPinned = pinned;
    Preferences.setPinned( command, pinned);
  }

  public boolean isChecked() {
    return isChecked;
  }

  public void setChecked(boolean checked) {
    isChecked = checked;
  }
}
