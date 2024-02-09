package in.hridayan.ashell.activities;

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
}
