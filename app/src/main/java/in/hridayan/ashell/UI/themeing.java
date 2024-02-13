package in.hridayan.ashell;

import android.app.Application;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.color.utilities.Contrast;

public class themeing extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    DynamicColors.applyToActivitiesIfAvailable(this);
  }
}
