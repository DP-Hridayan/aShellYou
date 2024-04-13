package in.hridayan.ashell.utils;

import android.content.Context;
import java.io.Serializable;
import in.hridayan.ashell.utils.Preferences;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 05, 2022
 */
public class CommandItems implements Serializable {

  private final String mTitle, mSummary, mExample;
  private int mUseCounter;
  private Context context;

  public CommandItems(String title, String summary, String example, Context context) {
    this.mTitle = title;
    this.mSummary = summary;
    this.mExample = example;
    this.context = context;
    this.mUseCounter = Preferences.getUseCounter(context, mTitle);
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
}
