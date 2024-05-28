package in.hridayan.ashell.utils;

import android.content.Context;
import java.io.Serializable;
import in.hridayan.ashell.utils.Preferences;

public class CommandItems implements Serializable {

  private final String mTitle, mSummary, mExample;
  private int mUseCounter;
  private Context context;
  private boolean isChecked, isPinned;

  public CommandItems(String title, String example, Context context) {
    this.mTitle = title;
    this.mSummary = summary(title, context);
    this.mExample = example;
    this.context = context;
    this.mUseCounter = Preferences.getUseCounter(context, mTitle);
    this.isPinned = Preferences.getPinned(context, mTitle);
  }

  public String getTitle() {
    return mTitle;
  }

  public String getSummary() {
    return mSummary;
  }

  public String getExample() {
    return mExample;
  }

  public int getUseCounter() {
    return mUseCounter;
  }

  public void setUseCounter(int counter) {
    this.mUseCounter = counter;
    Preferences.setUseCounter(context, mTitle, counter);
  }

  public boolean isPinned() {
    return isPinned;
  }

  public void setPinned(boolean pinned) {
    this.isPinned = pinned;
    Preferences.setPinned(context, mTitle, pinned);
  }

  public boolean isChecked() {
    return isChecked;
  }

  public void setChecked(boolean checked) {
    isChecked = checked;
  }

  private String summary(String title, Context context) {
    String trimmedTitle =
        title
            .replaceAll("cd -", "cd_hyphen")
            .replaceAll("<[^>]*>", "")
            .replaceAll("/", "slash")
            .replaceAll("~", "tilde")
            .trim()
            .replaceAll("[ -]+", "_")
            .replaceAll("-", "_")
            .replaceAll("_+", "_");
    int resourceId =
        context.getResources().getIdentifier(trimmedTitle, "string", context.getPackageName());
    return context.getResources().getString(resourceId);
  }
}
