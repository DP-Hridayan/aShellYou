package in.hridayan.ashell.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import java.util.Objects;

public class AboutItem {
  private int symbolResId;
  private String title;
  private String description;

  public AboutItem(@DrawableRes int symbolResId, String title, String description) {
    this.symbolResId = symbolResId;
    this.title = title;
    this.description = description;
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
}
