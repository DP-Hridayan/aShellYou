package in.hridayan.ashell.items;

import androidx.annotation.Nullable;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import in.hridayan.ashell.config.Preferences;

public class SettingsItem {
  private int symbolResId;
  private String description, title, id;
  private boolean hasSwitch, isChecked;

  public SettingsItem(
      String id,
      @Nullable @DrawableRes int symbolResId,
      String title,
      String description,
      boolean hasSwitch,
      boolean isChecked) {
    this.id = id;
    this.symbolResId = symbolResId;
    this.title = title;
    this.description = description;
    this.hasSwitch = hasSwitch;
    this.isChecked = isChecked;
  }

  public String getId() {
    return id;
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

  public boolean isChecked() {
    return isChecked;
  }

  public void setChecked(boolean isChecked) {
    this.isChecked = isChecked;
  }

  public void saveSwitchState() {
    // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    SharedPreferences.Editor editor = Preferences.prefs.edit();

    editor.putBoolean(id, isChecked);
    editor.apply();
  }
}
