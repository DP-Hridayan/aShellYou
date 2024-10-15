package in.hridayan.ashell.fragments.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import androidx.activity.OnBackPressedDispatcher;
import android.util.Pair;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.ThemeUtils;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.databinding.SettingsLookAndFeelBinding;
import in.hridayan.ashell.utils.DeviceUtils;
import in.hridayan.ashell.utils.HapticUtils;
import android.content.Context;
import android.content.SharedPreferences;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.utils.MiuiCheck;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.SettingsItemViewModel;

public class LookAndFeel extends Fragment {

  private SettingsLookAndFeelBinding binding;
  private View view;
  private BottomNavigationView mNav;
  private Context context;
  private SettingsItemViewModel viewModel;

  @Override
  public void onPause() {
    super.onPause();

    viewModel.setToolbarExpanded(Utils.isToolbarExpanded(binding.appBarLayout));

    int scrollX = binding.nestedScrollView.getScrollX();
    int scrollY = binding.nestedScrollView.getScrollY();
    Pair<Integer, Integer> scrollPosition = new Pair<>(scrollX, scrollY);
    viewModel.setScrollPosition(scrollPosition);
  }

  @Override
  public void onResume() {
    super.onResume();

    binding.appBarLayout.setExpanded(viewModel.isToolbarExpanded());

    Pair<Integer, Integer> savedScrollPosition = viewModel.getScrollPosition();
    if (savedScrollPosition != null) {
      // Ensure the NestedScrollView is fully laid out before restoring the scroll position
      binding
          .nestedScrollView
          .getViewTreeObserver()
          .addOnGlobalLayoutListener(
              () ->
                  binding.nestedScrollView.scrollTo(
                      savedScrollPosition.first, savedScrollPosition.second));
    }
  }

  public LookAndFeel() {}

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = SettingsLookAndFeelBinding.inflate(inflater, container, false);
    context = requireContext();
    viewModel = new ViewModelProvider(requireActivity()).get(SettingsItemViewModel.class);
    mNav = requireActivity().findViewById(R.id.bottom_nav_bar);
    mNav.setVisibility(View.GONE);
    view = binding.getRoot();

    onBackPressedDispatcher();
    setupThemeOptions();
    setupAmoledSwitch();
    setupDynamicColorsSwitch();
    setupDefaultLanguageOnClick();

    return view;
  }

  private void onBackPressedDispatcher() {
    OnBackPressedDispatcher dispatcher = requireActivity().getOnBackPressedDispatcher();

    binding.arrowBack.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          dispatcher.onBackPressed();
        });
  }

  // Setting up the theme options
  private void setupThemeOptions() {
    setRadioButtonState(binding.system, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    setRadioButtonState(binding.on, AppCompatDelegate.MODE_NIGHT_YES);
    setRadioButtonState(binding.off, AppCompatDelegate.MODE_NIGHT_NO);

    // Handle click events for alternative views (dark versions)
    binding.darkSystem.setOnClickListener(v -> binding.system.performClick());
    binding.darkOn.setOnClickListener(v -> binding.on.performClick());
    binding.darkOff.setOnClickListener(v -> binding.off.performClick());
  }

  // Setting up the amoled switch
  private void setupAmoledSwitch() {
    binding.switchHighContrastDarkTheme.setChecked(Preferences.getAmoledTheme());
    binding.switchHighContrastDarkTheme.setOnCheckedChangeListener(
        (view, isChecked) -> {
          HapticUtils.weakVibrate(view);
          saveSwitchState(Const.PREF_AMOLED_THEME, isChecked);
          if (ThemeUtils.isNightMode(context)) {
            Preferences.setActivityRecreated(true);
            requireActivity().recreate();
          }
        });
    binding.highContrastDarkTheme.setOnClickListener(
        v -> binding.switchHighContrastDarkTheme.performClick());
  }

  // Setting up the amoled switch
  private void setupDynamicColorsSwitch() {
    binding.dynamicColors.setVisibility(
        DeviceUtils.androidVersion() >= Build.VERSION_CODES.S ? View.VISIBLE : View.GONE);
    binding.switchDynamicColors.setChecked(Preferences.getDynamicColors());
    binding.switchDynamicColors.setOnCheckedChangeListener(
        (view, isChecked) -> {
          HapticUtils.weakVibrate(view);
          saveSwitchState(Const.PREF_DYNAMIC_COLORS, isChecked);
          Preferences.setActivityRecreated(true);
          requireActivity().recreate();
        });
    binding.dynamicColors.setOnClickListener(v -> binding.switchDynamicColors.performClick());
  }

  private void setupDefaultLanguageOnClick() {
    // App locale setting is only available on Android 13+
    // Also, it's not functional on MIUI devices even on Android 13,
    // Thanks to Xiaomi's broken implementation of standard Android APIs.
    // See: https://github.com/Pool-Of-Tears/GreenStash/issues/130 for more information.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !MiuiCheck.isMiui())
      binding.defaultLanguage.setVisibility(View.VISIBLE);

    binding.defaultLanguage.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent intent = new Intent(Settings.ACTION_APP_LOCALE_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
          }
        });
  }

  // Method to handle RadioButton states and clicks
  private void setRadioButtonState(RadioButton button, int mode) {
    button.setChecked(Preferences.getThemeMode() == mode);
    button.setOnClickListener(
        v -> {
          if (Preferences.getThemeMode() != mode) {
            HapticUtils.weakVibrate(v);
            handleRadioButtonSelection(button, mode);
          }
        });
  }

  private void handleRadioButtonSelection(RadioButton button, int mode) {
    clearRadioButtons(); // Uncheck all radio buttons
    button.setChecked(true);
    Preferences.setThemeMode(mode);
    AppCompatDelegate.setDefaultNightMode(mode);
  }

  // Uncheck all radio buttons
  private void clearRadioButtons() {
    binding.system.setChecked(false);
    binding.on.setChecked(false);
    binding.off.setChecked(false);
  }

  private void saveSwitchState(String prefId, boolean isChecked) {
    SharedPreferences.Editor editor = Preferences.prefs.edit();
    editor.putBoolean(prefId, isChecked);
    editor.apply();
  }
}