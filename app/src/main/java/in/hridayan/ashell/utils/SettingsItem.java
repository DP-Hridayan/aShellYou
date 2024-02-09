package in.hridayan.ashell.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import java.util.Objects;

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

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean hasSwitch() {
    return hasSwitch;
  }

  public void setHasSwitch(boolean hasSwitch) {
    this.hasSwitch = hasSwitch;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void setEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }
    

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SettingsItem that = (SettingsItem) o;
    return isEnabled == that.isEnabled
        && hasSwitch == that.hasSwitch
        && title.equals(that.title)
        && description.equals(that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(symbolResId, title, description, hasSwitch, isEnabled);
  }
}
