package in.hridayan.ashell.activities;

import android.widget.Toast;
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

public class MainActivity extends AppCompatActivity
    implements otgShellFragment.OnFragmentInteractionListener {
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
        
    // handle intent for "Use" feature
    if (intent.hasExtra("use_command")) {
      String useCommand = intent.getStringExtra("use_command");
      handleUseCommandIntent(useCommand, intent);
    }

    // handle intent for text shared to aShell You
    if (intent.hasExtra(Intent.EXTRA_TEXT)) {
      String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
      if (sharedText != null) {
        sharedText = sharedText.trim().replaceAll("^\"|\"$", "");
        handleSharedTextIntent(sharedText, intent);
      }
    }

    // handle intent when usb is disconnected
    if (intent != null && "com.example.ACTION_USB_DETACHED".equals(intent.getAction())) {
      onUsbDetached();
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

    isBlackThemeEnabled = isAmoledTheme;

    setupNavigation();

    // Displaying badges on navigation bar
    setBadge(R.id.nav_otgShell, "Beta");
    setBadge(R.id.nav_wireless, "Soon");
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

  // Main navigation setup
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

  // Takes the fragment we want to navigate to as argument and then starts that fragment
  private void replaceFragment(Fragment fragment) {
    setCurrentFragment();
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit();
  }

  // If not on OtgShell then go to OtgShell
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

  // Reset the OtgFragment
  @Override
  public void onRequestReset() {
    currentFragment = OTG_FRAGMENT;
    replaceFragment(new otgShellFragment());
  }
}
