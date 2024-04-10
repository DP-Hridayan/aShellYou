package in.hridayan.ashell.activities;

import static in.hridayan.ashell.utils.Preferences.LOCAL_FRAGMENT;
import static in.hridayan.ashell.utils.Preferences.MODE_REMEMBER_LAST_MODE;
import static in.hridayan.ashell.utils.Preferences.OTG_FRAGMENT;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.KeyboardUtils;
import in.hridayan.ashell.UI.MainViewModel;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.fragments.StartFragment;
import in.hridayan.ashell.fragments.aShellFragment;
import in.hridayan.ashell.fragments.otgShellFragment;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.SettingsItem;
import in.hridayan.ashell.utils.ThemeUtils;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private boolean isKeyboardVisible;
  public BottomNavigationView mNav;
  private SettingsAdapter adapter;
  private SettingsItem settingsList;
  private static int currentFragment;
  private boolean isBlackThemeEnabled, isAmoledTheme;
  private MainViewModel viewModel;

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    if (intent.hasExtra("use_command")) {
      String useCommand = intent.getStringExtra("use_command");
      handleUseCommandIntent(useCommand);
    }
    if (intent.hasExtra(Intent.EXTRA_TEXT)) {
      String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
      if (sharedText != null) {
        handleSharedTextIntent(sharedText);
      }
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    setCurrentFragment();
    viewModel.setCurrentFragment(currentFragment);
  }

  @Override
  protected void onResume() {
    super.onResume();

    isAmoledTheme = Preferences.getAmoledTheme(this);
    boolean currentTheme = isAmoledTheme;
    if (currentTheme != isBlackThemeEnabled) {
      recreate();
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    ThemeUtils.updateTheme(this);

    List<SettingsItem> settingsList = new ArrayList<>();
    adapter = new SettingsAdapter(settingsList, this);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    viewModel = new ViewModelProvider(this).get(MainViewModel.class);

    isAmoledTheme = Preferences.getAmoledTheme(this);

    mNav = findViewById(R.id.bottom_nav_bar);

    KeyboardUtils.attachVisibilityListener(
        this,
        new KeyboardUtils.KeyboardVisibilityListener() {
          @Override
          public void onKeyboardVisibilityChanged(boolean visible) {
            isKeyboardVisible = visible;
            if (isKeyboardVisible) {
              mNav.setVisibility(View.GONE);
            } else {
              new Handler(Looper.getMainLooper())
                  .postDelayed(
                      () -> {
                        mNav.setVisibility(View.VISIBLE);
                      },
                      100);
            }
          }
        });

    isBlackThemeEnabled = isAmoledTheme;
    setupNavigation();
    setBadge(R.id.nav_otgShell, "Beta");
    setBadge(R.id.nav_wireless, "Soon");
  }

  private void handleSharedTextIntent(String sharedText) {
    setTextOnEditText(sharedText);
  }

  private void handleUseCommandIntent(String useCommand) {
    setTextOnEditText(useCommand);
  }

  private void setTextOnEditText(String text) {

    int currentFragment = Preferences.getCurrentFragment(this);
    switch (currentFragment) {
      case LOCAL_FRAGMENT:
        aShellFragment fragmentLocalAdb =
            (aShellFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragmentLocalAdb != null) {
          fragmentLocalAdb.updateInputField(text);
        }
        break;

      case OTG_FRAGMENT:
        otgShellFragment fragmentOtg =
            (otgShellFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragmentOtg != null) {
          fragmentOtg.updateInputField(text);
        }

        break;
      default:
        break;
    }
  }

  private void setupNavigation() {
    mNav.setVisibility(View.VISIBLE);
    mNav.setOnItemSelectedListener(
        item -> {
          switch (item.getItemId()) {
            case R.id.nav_localShell:
              showaShellFragment();
              Preferences.setCurrentFragment(this, LOCAL_FRAGMENT);
              return true;

            case R.id.nav_otgShell:
              showotgShellFragment();
              Preferences.setCurrentFragment(this, OTG_FRAGMENT);
              return true;

            default:
              return false;
          }
        });

    initialFragment();
  }

  private void replaceFragment(Fragment fragment) {

    setCurrentFragment();
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit();
  }

  private void showotgShellFragment() {
    if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container)
        instanceof otgShellFragment)) {
      // Don't show again logic
      if (PreferenceManager.getDefaultSharedPreferences(this)
          .getBoolean("Don't show beta otg warning", true)) {
        showBetaWarning();
      } else {
        currentFragment = OTG_FRAGMENT;
        replaceFragment(new otgShellFragment());
      }
    }
  }

  private void showaShellFragment() {
    if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container)
        instanceof aShellFragment)) {
      currentFragment = LOCAL_FRAGMENT;
      replaceFragment(new aShellFragment());
    }
  }

  private void showBetaWarning() {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    builder
        .setCancelable(false)
        .setTitle(getString(R.string.warning))
        .setMessage(getString(R.string.beta_warning))
        .setPositiveButton(
            getString(R.string.accept),
            (dialogInterface, i) -> {
              replaceFragment(new otgShellFragment());
            })
        .setNegativeButton(
            getString(R.string.go_back),
            (dialogInterface, i) -> {
              mNav.setSelectedItemId(R.id.nav_localShell);
            })
        .setNeutralButton(
            getString(R.string.donot_show_again),
            (dialogInterface, i) -> {
              PreferenceManager.getDefaultSharedPreferences(this)
                  .edit()
                  .putBoolean("Don't show beta otg warning", false)
                  .apply();
              replaceFragment(new otgShellFragment());
            })
        .show();
  }

  private void setBadge(int id, String text) {
    BadgeDrawable badge = mNav.getOrCreateBadge(id);
    badge.setVisible(true);
    badge.setText(text);
    badge.setHorizontalOffset(0);
  }

  private void initialFragment() {
    if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("firstLaunch", true)) {
      mNav.setVisibility(View.GONE);
      replaceFragment(new StartFragment());
    } else {
      boolean isFragmentSaved = viewModel.isFragmentSaved();
      if (isFragmentSaved) {
        int currentFragment = viewModel.currentFragment();
        switchFragments(currentFragment);
      } else {
        int currentFragment = Preferences.getCurrentFragment(this);
        int workingMode = Preferences.getWorkingMode(this);

        if (workingMode == MODE_REMEMBER_LAST_MODE) {
          switchFragments(currentFragment);
        } else {
          switchFragments(workingMode + 1);
        }
      }
    }
  }

  private void setCurrentFragment() {

    if ((getSupportFragmentManager().findFragmentById(R.id.fragment_container)
        instanceof aShellFragment)) {
      currentFragment = LOCAL_FRAGMENT;
    } else if ((getSupportFragmentManager().findFragmentById(R.id.fragment_container)
        instanceof otgShellFragment)) {
      currentFragment = OTG_FRAGMENT;
    }
  }

  private void switchFragments(int currentFragment) {
    switch (currentFragment) {
      case LOCAL_FRAGMENT:
        mNav.setSelectedItemId(R.id.nav_localShell);
        replaceFragment(new aShellFragment());
        break;
      case OTG_FRAGMENT:
        mNav.setSelectedItemId(R.id.nav_otgShell);
        replaceFragment(new otgShellFragment());
        break;
      default:
        break;
    }
  }
}
