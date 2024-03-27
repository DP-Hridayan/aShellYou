package in.hridayan.ashell.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.ExamplesAdapter;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.SettingsItem;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 05, 2022
 */
public class ExamplesActivity extends AppCompatActivity {

  private SettingsAdapter adapterSettings;
  private SettingsItem settingsList;

  @Override
  protected void onResume() {
    super.onResume();
    updateTheme();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    List<SettingsItem> settingsList = new ArrayList<>();
    adapterSettings = new SettingsAdapter(settingsList, this);

    updateTheme();

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_examples);

    ImageView imageView = findViewById(R.id.arrow_back);

    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    GridLayoutManager mLayoutManager =
        new GridLayoutManager(
            this,
            getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                ? 2
                : 1);
    mRecyclerView.setLayoutManager(mLayoutManager);
    ExamplesAdapter mRecycleViewAdapter = new ExamplesAdapter(Commands.commandList());
    mRecyclerView.setAdapter(mRecycleViewAdapter);
    mRecyclerView.setVisibility(View.VISIBLE);
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
