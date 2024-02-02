package in.hridayan.ashell.utils;

import androidx.annotation.NonNull;
import java.util.Objects;

public class SettingsItem {

  private String title;
  private String description;
  private boolean isEnabled;

  public SettingsItem(@NonNull String title, @NonNull String description, boolean isEnabled) {
    this.title = title;
    this.description = description;
    this.isEnabled = isEnabled;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(@NonNull String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(@NonNull String description) {
    this.description = description;
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
        && title.equals(that.title)
        && description.equals(that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, description, isEnabled);
  }
}
