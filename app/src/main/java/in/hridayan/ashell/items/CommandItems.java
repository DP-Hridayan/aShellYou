package in.hridayan.ashell.items;

import android.content.Context;
import java.io.Serializable;
import in.hridayan.ashell.config.Preferences;

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
    this.mUseCounter = Preferences.getUseCounter( mTitle);
    this.isPinned = Preferences.getPinned( mTitle);
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
    Preferences.setUseCounter( mTitle, counter);
  }

  public boolean isPinned() {
    return isPinned;
  }

  public void setPinned(boolean pinned) {
    this.isPinned = pinned;
    Preferences.setPinned( mTitle, pinned);
  }

  public boolean isChecked() {
    return isChecked;
  }

  public void setChecked(boolean checked) {
    isChecked = checked;
  }

  /*We use identifier to find the summary strings as they have been named exactly as the commands with some characters replaced as shown below*/
  private String summary(String title, Context context) {
    String trimmedTitle =
        title
            .replaceAll("cd -", "cd_hyphen")
            .replaceAll("<[^>]*>", "")
            .replaceAll("/", "slash")
            .replaceAll("~", "tilde")
            .replaceAll("\\*", "asterisk")
            .trim()
            .replaceAll("[ -]+", "_")
            .replaceAll("-", "_")
            .replaceAll("_+", "_");
    int resourceId =
        context.getResources().getIdentifier(trimmedTitle, "string", context.getPackageName());

    /*We haven't defined any summary strings for the package name suggestions , so we return null if resource id is not found */
    if (resourceId == 0) {
      return null;
    } else {
      return context.getResources().getString(resourceId);
    }
  }
}
