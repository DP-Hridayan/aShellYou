package in.hridayan.ashell;

import android.app.Application;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.color.utilities.Contrast;
import com.google.android.material.color.utilities.DynamicColor;
import com.google.android.material.color.utilities.MaterialDynamicColors;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.utils.SettingsItem;
import java.util.ArrayList;
import java.util.List;

public class themeing extends Application {

  private SettingsAdapter adapter;
  private SettingsItem settingsList;
  private List<String> mHistory = null, mResult = null;

  @Override
  public void onCreate() {
    super.onCreate();
    List<SettingsItem> settingsList = new ArrayList<>();
    adapter = new SettingsAdapter(settingsList, this);

    DynamicColors.applyToActivitiesIfAvailable(this);
  }
}
