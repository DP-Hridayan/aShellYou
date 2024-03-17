package in.hridayan.ashell.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

    int statusBarColor = getColor(R.color.StatusBar);
    double brightness = Color.luminance(statusBarColor);
    boolean isLightStatusBar = brightness > 0.5;

    View decorView = getWindow().getDecorView();
    if (isLightStatusBar) {
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    ImageView imageView = findViewById(R.id.arrow_back);

    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    settingsList = findViewById(R.id.settings_list);

    settingsData = new ArrayList<>();

    // switches
    settingsData.add(
        new SettingsItem(
            R.drawable.ic_scroll,
                getString(R.string.smooth_scrolling)
            "Smooth scrolling",
            "Enables smooth scrolling in the shell output when top or bottom arrow is clicked",
            true,
            getSavedSwitchState("Smooth scrolling")));

    settingsData.add(
        new SettingsItem(
            R.drawable.ic_clear,
                getString(R.strings.clear_shell)
            "Ask before clearing shell output",
            "If enabled a confirmation popup will show after you click the Clear screen button",
            true,
            getSavedSwitchState("Ask before clearing shell output")));

    settingsData.add(
        new SettingsItem(
            R.drawable.ic_warning,
                getString(R.strings.override_bookmarks)
            "Override maximum bookmarks limit",
            "Enabling this option might cause performance issue if a large number of bookmarks are added! Low end devices should keep this option turned off",
            true,
            getSavedSwitchState("Override maximum bookmarks limit")));

    // no switches
    settingsData.add(
        new SettingsItem(
            R.drawable.ic_numbers,
                getString(R.strings.show_examples)
            "Examples",
            "Collection of some command templates.",
            false,
            false));

    settingsData.add(
        new SettingsItem(
            R.drawable.ic_changelog,
                getString(R.strings.show_history)
            "Changelogs",
            "History of all the changes made to the app",
            false,
            false));

    settingsData.add(
        new SettingsItem(R.drawable.ic_info,
                getString(R.strings.Credits)
                 "About", "Version , Credits",
                 false, false));

    adapter = new SettingsAdapter(settingsData, this);

    settingsList.setAdapter(adapter);
    settingsList.setLayoutManager(new LinearLayoutManager(this));
  }

  private boolean getSavedSwitchState(String title) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    return prefs.getBoolean(title, false);
  }
}
