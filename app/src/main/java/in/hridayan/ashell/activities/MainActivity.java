package in.hridayan.ashell.activities;

import static in.hridayan.ashell.utils.Preferences.LOCAL_FRAGMENT;
import static in.hridayan.ashell.utils.Preferences.MODE_REMEMBER_LAST_MODE;
import static in.hridayan.ashell.utils.Preferences.OTG_FRAGMENT;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
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
import in.hridayan.ashell.utils.FetchLatestVersionCode;
import in.hridayan.ashell.utils.CrashHandler;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.SettingsItem;
import in.hridayan.ashell.utils.ThemeUtils;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.utils.Utils.FetchLatestVersionCodeCallback;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
    implements otgShellFragment.OnFragmentInteractionListener, FetchLatestVersionCodeCallback {
  private boolean isKeyboardVisible, hasAppRestarted = true;
  public BottomNavigationView mNav;
  private SettingsAdapter adapter;
  private SettingsItem settingsList;
  private static int currentFragment;
  private boolean isBlackThemeEnabled, isAmoledTheme;
  private MainViewModel viewModel;

  private String pendingSharedText = null;

  // Reset the OtgFragment
  @Override
  public void onRequestReset() {
    if ((getSupportFragmentManager().findFragmentById(R.id.fragment_container)
        instanceof otgShellFragment)) {
      currentFragment = OTG_FRAGMENT;
      mNav.setSelectedItemId(R.id.nav_otgShell);
      replaceFragment(new otgShellFragment());
    }
  }

  // This funtion is run to perform actions if there is an update available or not
  @Override
  public void onResult(int result) {
    if (result == Preferences.UPDATE_AVAILABLE) {

      Utils.showBottomSheetUpdate(this, this);
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    handleIncomingIntent(intent);
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
    // Amoled theme
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
    // Catch exceptions
    Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));

    viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    isAmoledTheme = Preferences.getAmoledTheme(this);

    mNav = findViewById(R.id.bottom_nav_bar);

    // Hide the navigation bar when the keyboard is visible
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

    setupNavigation();
    // Show What's new bottom sheet on opening the app after an update
    if (Utils.isAppUpdated(this)) {
      Utils.showBottomSheetChangelog(this);
    }
    Preferences.setSavedVersionCode(this, Utils.currentVersion());

    isBlackThemeEnabled = isAmoledTheme;

    // Displaying badges on navigation bar
    setBadge(R.id.nav_wireless, "Soon");

    // Auto check for updates when app launches
    if (Preferences.getAutoUpdateCheck(this)
        && hasAppRestarted
        && !(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("firstLaunch", true))) {
      new FetchLatestVersionCode(this, this).execute(Preferences.buildGradleUrl);
    }
    hasAppRestarted = false;
  }

  // Intent to get the text shared to aShell You app
  private void handleSharedTextIntent(String sharedText, Intent intent) {
    setTextOnEditText(sharedText, intent);
  }

  // Intent to get the text when we use the "Use" feature in command examples
  private void handleUseCommandIntent(String useCommand, Intent intent) {
    setTextOnEditText(useCommand, intent);
  }

  // Set the text in the Input Field
  private void setTextOnEditText(String text, Intent intent) {
    int currentFragment = Preferences.getCurrentFragment(this);

    switch (currentFragment) {
      case LOCAL_FRAGMENT:
        aShellFragment fragmentLocalAdb =
            (aShellFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragmentLocalAdb != null) {
          if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            fragmentLocalAdb.handleSharedTextIntent(getIntent(), text);
          } else {
            fragmentLocalAdb.updateInputField(text);
          }
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

  private void handlePendingSharedText() {
    if (pendingSharedText != null) {
      switch (Preferences.getCurrentFragment(this)) {
        case LOCAL_FRAGMENT:
          aShellFragment fragmentLocalAdb =
              (aShellFragment)
                  getSupportFragmentManager().findFragmentById(R.id.fragment_container);
          if (fragmentLocalAdb != null) {
            fragmentLocalAdb.updateInputField(pendingSharedText);
            clearPendingSharedText();
          }
          break;

        case OTG_FRAGMENT:
          otgShellFragment fragmentOtg =
              (otgShellFragment)
                  getSupportFragmentManager().findFragmentById(R.id.fragment_container);
          if (fragmentOtg != null) {
            fragmentOtg.updateInputField(pendingSharedText);
            clearPendingSharedText();
          }
          break;

        default:
          break;
      }
    }
  }

  // Main navigation setup
  private void setupNavigation() {
    mNav.setVisibility(View.VISIBLE);
    mNav.setOnItemSelectedListener(
        item -> {
          HapticUtils.weakVibrate(mNav, this);
          switch (item.getItemId()) {
            case R.id.nav_localShell:
              showaShellFragment();
              Preferences.setCurrentFragment(this, LOCAL_FRAGMENT);
              return true;

            case R.id.nav_otgShell:
              showotgShellFragment();
              Preferences.setCurrentFragment(this, OTG_FRAGMENT);
              return true;

            case R.id.nav_wireless:
              Toast.makeText(this, "Soon", Toast.LENGTH_SHORT).show();
              return false;

            default:
              return false;
          }
        });

    initialFragment();
    handleIncomingIntent(getIntent());
  }

  // Takes the fragment we want to navigate to as argument and then starts that fragment
  public void replaceFragment(Fragment fragment) {
    if (!getSupportFragmentManager().isStateSaved()) {
      setCurrentFragment();
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.fragment_container, fragment)
          .commit();
    }
  }

  // If not on OtgShell then go to OtgShell
  private void showotgShellFragment() {
    if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container)
        instanceof otgShellFragment)) {
      /* Don't show again logic
      if (PreferenceManager.getDefaultSharedPreferences(this)
          .getBoolean("Don't show beta otg warning", true)) {
        showBetaWarning();
      } else { */

      currentFragment = OTG_FRAGMENT;
      replaceFragment(new otgShellFragment());

      /*   } */
    }
  }

  // If not on LocalShell then go to LocalShell (aShellFragment)
  private void showaShellFragment() {
    if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container)
        instanceof aShellFragment)) {
      currentFragment = LOCAL_FRAGMENT;
      replaceFragment(new aShellFragment());
    }
  }

  // Experimental feature warning for OtgShell
  private void showBetaWarning() {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    builder
        .setCancelable(false)
        .setTitle(getString(R.string.warning))
        .setMessage(getString(R.string.beta_warning))
        .setPositiveButton(
            getString(R.string.accept),
            (dialogInterface, i) -> replaceFragment(new otgShellFragment()))
        .setNegativeButton(
            getString(R.string.go_back),
            (dialogInterface, i) -> mNav.setSelectedItemId(R.id.nav_localShell))
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

  // Function to set a Badge on the Navigation Bar
  private void setBadge(int id, String text) {
    BadgeDrawable badge = mNav.getOrCreateBadge(id);
    badge.setVisible(true);
    badge.setText(text);
    badge.setHorizontalOffset(0);
  }

  // Since there is option to set which working mode you want to display when app is launched , this
  // piece of code handles the logic for initial fragment
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
        switchFragments(workingMode == MODE_REMEMBER_LAST_MODE ? currentFragment : workingMode + 1);
      }
      handlePendingSharedText();
    }
  }

  private void setCurrentFragment() {
    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    if (fragment instanceof aShellFragment) {
      currentFragment = LOCAL_FRAGMENT;
    } else if (fragment instanceof otgShellFragment) {
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

  // Execute functions when the Usb connection is removed
  public void onUsbDetached() {
    // Reset the OtgShellFragment in this case
    onRequestReset();
  }

  private void handleIncomingIntent(Intent intent) {
    if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.hasExtra(Intent.EXTRA_TEXT)) {
      String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
      if (sharedText != null) {
        sharedText = sharedText.trim().replaceAll("^\"|\"$", "");
        pendingSharedText = sharedText;
        handleSharedTextIntent(sharedText, intent);
      }
    } else if (intent.hasExtra("use_command")) {
      String useCommand = intent.getStringExtra("use_command");
      handleUseCommandIntent(useCommand, intent);
    } else if ("com.example.ACTION_USB_DETACHED".equals(intent.getAction())) {
      onUsbDetached();
    }
  }

  public String getPendingSharedText() {
    return pendingSharedText;
  }

  public void clearPendingSharedText() {
    pendingSharedText = null;
  }
}
