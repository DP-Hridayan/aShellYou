package in.hridayan.ashell.activities;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.materialswitch.MaterialSwitch;
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.utils.SettingsItem;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

  private RecyclerView settingsList;
  private List<SettingsItem> settingsData;
  private SettingsAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    int statusBarColor = getResources().getColor(R.color.StatusBar);
    double brightness = getBrightness(statusBarColor);
    boolean isLightStatusBar = brightness > 0.5;

    View decorView = getWindow().getDecorView();
    if (isLightStatusBar) {
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    } else {
      decorView.setSystemUiVisibility(0);
    }

    settingsList = findViewById(R.id.settings_list);

    ImageView imageView = findViewById(R.id.arrow_back);

    imageView.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            onBackPressed();
          }
        });
    settingsData = new ArrayList<>();
    settingsData.add(
        new SettingsItem(
            R.drawable.ic_scroll,
            "Smooth Scrolling",
            "Enables smooth scrolling in the shell output when top or bottom arrow is clicked",
            true,
            true));
    settingsData.add(
        new SettingsItem(
            R.drawable.ic_clear,
            "Ask before clearing shell output",
            "If enabled a confirmation popup will show after you click the Clear screen button",
            true,
            true));

    settingsData.add(
        new SettingsItem(
            R.drawable.ic_numbers,
            "Examples",
            "Collection of some command templates.",
            false,
            false));

    settingsData.add(
        new SettingsItem(
            R.drawable.ic_changelog,
            "Changelogs",
            "History of all the changes made to the app",
            false,
            false));

    settingsData.add(
        new SettingsItem(R.drawable.ic_info, "About", "Version , Credits", false, false));

    adapter = new SettingsAdapter(settingsData, this);

    settingsList.setAdapter(adapter);
    settingsList.setLayoutManager(new LinearLayoutManager(this));
  }

  @Override
  protected void onResume() {
    super.onResume();
    adapter.notifyDataSetChanged();
  }

  public double getBrightness(int color) {
    int red = Color.red(color);
    int green = Color.green(color);
    int blue = Color.blue(color);
    return 0.299 * red + 0.587 * green + 0.114 * blue;
  }
}
