package in.hridayan.ashell.activities;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.ChangelogAdapter;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.utils.ChangelogItem;
import in.hridayan.ashell.utils.SettingsItem;
import java.util.ArrayList;
import java.util.List;

public class ChangelogActivity extends AppCompatActivity {

  private SettingsAdapter adapterSettings;
  private SettingsItem settingsList;

  @Override
  protected void onResume() {
    super.onResume();
    updateTheme();
  }

  private final String[] versionNumbers = {
    "3.7.0", "3.6.0", "3.5.1", "3.5.0", "3.4.0", "3.3.0", "3.2.0", "3.1.0", "3.0.0", "2.0.2",
    "2.0.1", "2.0.0", "1.3.0", "1.2.0", "1.1.1", "1.1.0", "1.0.0", "0.9.1", "0.9.0"
  };

  private Resources resources;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);

    List<SettingsItem> settingsList = new ArrayList<>();
    adapterSettings = new SettingsAdapter(settingsList, this);

    updateTheme();

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_changelog);

    resources = getResources();

    ImageView imageView = findViewById(R.id.arrow_back);
    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    RecyclerView recyclerViewChangelogs = findViewById(R.id.recycler_view_changelogs);

    List<ChangelogItem> changelogItems = new ArrayList<>();

    for (String versionNumber : versionNumbers) {
      changelogItems.add(
          new ChangelogItem("Version " + versionNumber, loadChangelogText(versionNumber)));
    }

    ChangelogAdapter adapter = new ChangelogAdapter(changelogItems, this);
    recyclerViewChangelogs.setAdapter(adapter);
    recyclerViewChangelogs.setLayoutManager(new LinearLayoutManager(this));
  }

  private String loadChangelogText(String versionNumber) {
    int resourceId =
        resources.getIdentifier(
            "changelog_v" + versionNumber.replace(".", "_"), "string", getPackageName());
    return resources.getString(resourceId);
  }

  private void updateTheme() {

    boolean switchState = adapterSettings.getSavedSwitchState("id_amoled_theme");

    int currentMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

    if (switchState && currentMode == Configuration.UI_MODE_NIGHT_YES) {
      setTheme(R.style.ThemeOverlay_aShellYou_AmoledTheme);
    } else {
      setTheme(R.style.aShellYou_AppTheme);
    }
  }
}
