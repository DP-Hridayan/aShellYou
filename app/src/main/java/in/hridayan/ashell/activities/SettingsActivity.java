package in.hridayan.ashell.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.SettingsAdapter;
import com.google.android.material.materialswitch.MaterialSwitch;
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

    settingsData = new ArrayList<>();
    settingsData.add(
        new SettingsItem(
            "Smooth Scrolling",
            "Enables smooth scrolling in the shell output when top or bottom arrow is clicked",
            true));
    
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
