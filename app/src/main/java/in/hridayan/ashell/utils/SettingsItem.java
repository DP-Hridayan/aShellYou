package in.hridayan.ashell.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class SettingsItem {
  private int symbolResId;
  private String title;
  private String description;
  private boolean isEnabled;
  private boolean hasSwitch;

  public SettingsItem(
      @DrawableRes int symbolResId,
      String title,
      String description,
      boolean hasSwitch,
      boolean isEnabled) {
    this.symbolResId = symbolResId;
    this.title = title;
    this.description = description;
    this.hasSwitch = hasSwitch;
    this.isEnabled = isEnabled;
  }

  public Drawable getSymbol(Context context) {
    return ContextCompat.getDrawable(context, symbolResId);
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public boolean hasSwitch() {
    return hasSwitch;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public void saveSwitchState(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putBoolean(title, isEnabled);
    editor.apply();
  }

  public void loadSwitchState(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    isEnabled = prefs.getBoolean(title, false);
  }
}
