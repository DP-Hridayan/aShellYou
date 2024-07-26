package in.hridayan.ashell.activities;

import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.SettingsViewModel;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.utils.MiuiCheck;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.SettingsItem;
import in.hridayan.ashell.utils.ThemeUtils;
import in.hridayan.ashell.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

  private RecyclerView settingsList;
  private List<SettingsItem> settingsData;
  private SettingsAdapter adapter;
  private int currentTheme;
  private SettingsViewModel viewModel;
  private AppBarLayout appBarLayout;

  @Override
  protected void onPause() {
    super.onPause();
    if (settingsList != null) {
      viewModel.setScrollPosition(
          ((LinearLayoutManager) settingsList.getLayoutManager()).findFirstVisibleItemPosition());
      // Save toolbar state
      viewModel.setToolbarExpanded(Utils.isToolbarExpanded(appBarLayout));
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    int position = Utils.recyclerViewPosition(settingsList);

    if (viewModel.isToolbarExpanded()) {
      if (position == 0) {
        Utils.expandToolbar(appBarLayout);
      }
    } else {
      Utils.collapseToolbar(appBarLayout);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    ThemeUtils.updateTheme(this);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    appBarLayout = findViewById(R.id.appBarLayout);

    viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

    setupRecyclerView();

    ImageView imageView = findViewById(R.id.arrow_back);

    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    settingsList = findViewById(R.id.settings_list);
    settingsData = new ArrayList<>();

    settingsData.add(
        new SettingsItem(
            "id_amoled_theme",
            R.drawable.ic_amoled_theme,
            getString(R.string.amoled_theme),
            getString(R.string.des_amoled_theme),
            true,
            Preferences.getAmoledTheme(this)));

    settingsData.add(
        new SettingsItem(
            "id_clear",
            R.drawable.ic_clear,
            getString(R.string.ask_to_clean),
            getString(R.string.des_ask_to_clean),
            true,
            Preferences.getClear(this)));

    settingsData.add(
        new SettingsItem(
            "id_share_and_run",
            R.drawable.ic_share,
            getString(R.string.share_and_run),
            getString(R.string.des_share_and_run),
            true,
            Preferences.getShareAndRun(this)));

    settingsData.add(
        new SettingsItem(
            "id_auto_update_check",
            R.drawable.ic_auto_update,
            getString(R.string.auto_update_check),
            getString(R.string.des_auto_update_check),
            true,
            Preferences.getAutoUpdateCheck(this)));

    // App locale setting is only available on Android 13+
    // Also, it's not functional on MIUI devices even on Android 13,
    // Thanks to Xiaomi's broken implementation of standard Android APIs.
    // See: https://github.com/Pool-Of-Tears/GreenStash/issues/130 for more information.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !MiuiCheck.isMiui()) {
      settingsData.add(
          new SettingsItem(
              "id_default_language",
              R.drawable.ic_language,
              getString(R.string.default_language),
              getString(R.string.des_default_language),
              false,
              false));
    }

    settingsData.add(
        new SettingsItem(
            "id_default_working_mode",
            R.drawable.ic_mode,
            getString(R.string.default_working_mode),
            getString(R.string.des_default_working_mode),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            "id_disable_softkey",
            R.drawable.ic_disable_keyboard,
            getString(R.string.disable_softkey),
            getString(R.string.des_disable_softkey),
            true,
            Preferences.getDisableSoftkey(this)));

    settingsData.add(
        new SettingsItem(
            "id_override_bookmarks",
            R.drawable.ic_warning,
            getString(R.string.override_bookmarks_limit),
            getString(R.string.des_override_bookmarks),
            true,
            Preferences.getOverrideBookmarks(this)));

    settingsData.add(
        new SettingsItem(
            "id_save_preference",
            R.drawable.ic_save_24px,
            getString(R.string.save_preference),
            getString(R.string.des_save_preference),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            "id_smooth_scroll",
            R.drawable.ic_scroll,
            getString(R.string.smooth_scrolling),
            getString(R.string.des_smooth_scroll),
            true,
            Preferences.getSmoothScroll(this)));

    settingsData.add(
        new SettingsItem(
            "id_unhide_cards",
            R.drawable.ic_cards,
            getString(R.string.unhide_cards),
            getString(R.string.des_unhide_cards),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            "id_examples",
            R.drawable.ic_numbers,
            getString(R.string.commands),
            getString(R.string.des_examples),
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

    adapter = new SettingsAdapter(settingsData, this, currentTheme);

    settingsList.setAdapter(adapter);
    settingsList.setLayoutManager(new LinearLayoutManager(this));
  }

  private void setupRecyclerView() {
    settingsList = findViewById(R.id.settings_list);
    settingsList.setLayoutManager(new LinearLayoutManager(this));

    List<SettingsItem> settingsData = viewModel.getSettingsData();
    int scrollPosition = viewModel.getScrollPosition();

    adapter = new SettingsAdapter(settingsData, this, currentTheme);
    settingsList.setAdapter(adapter);
    settingsList.scrollToPosition(scrollPosition);
  }
}
