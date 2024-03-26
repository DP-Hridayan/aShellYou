package in.hridayan.ashell.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
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
    EdgeToEdge.enable(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    ImageView imageView = findViewById(R.id.arrow_back);

    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    settingsList = findViewById(R.id.settings_list);

    settingsData = new ArrayList<>();

    // switches

    settingsData.add(
        new SettingsItem(
            "id_clear",
            R.drawable.ic_clear,
            getString(R.string.ask_to_clean),
            getString(R.string.des_ask_to_clean),
            true,
            getSavedSwitchState("id_clear")));

    settingsData.add(
        new SettingsItem(
            "id_share_and_run",
            R.drawable.ic_share,
            getString(R.string.share_and_run),
            getString(R.string.des_share_and_run),
            true,
            getSavedSwitchState("id_share_and_run")));

    settingsData.add(
        new SettingsItem(
            "id_disable_softkey",
            R.drawable.ic_disable_keyboard,
            getString(R.string.disable_softkey),
            getString(R.string.des_disable_softkey),
            true,
            getSavedSwitchState("id_disable_softkey")));

    settingsData.add(
        new SettingsItem(
            "id_override_bookmarks",
            R.drawable.ic_warning,
            getString(R.string.override_bookmarks_limit),
            getString(R.string.des_override_bookmarks),
            true,
            getSavedSwitchState("id_override_bookmarks")));

    settingsData.add(
        new SettingsItem(
            "id_smooth_scroll",
            R.drawable.ic_scroll,
            getString(R.string.smooth_scrolling),
            getString(R.string.des_smooth_scroll),
            true,
            getSavedSwitchState("id_smooth_scroll")));

    // no switches
    settingsData.add(
        new SettingsItem(
            "id_examples",
            R.drawable.ic_numbers,
            getString(R.string.examples),
            getString(R.string.des_examples),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            "id_changelogs",
            R.drawable.ic_changelog,
            getString(R.string.changelogs),
            getString(R.string.des_changelogs),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            "id_about",
            R.drawable.ic_info,
            getString(R.string.about),
            getString(R.string.des_about),
            false,
            false));

    adapter = new SettingsAdapter(settingsData, this);

    settingsList.setAdapter(adapter);
    settingsList.setLayoutManager(new LinearLayoutManager(this));
  }

  private boolean getSavedSwitchState(String id) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    return prefs.getBoolean(id, false);
  }
}