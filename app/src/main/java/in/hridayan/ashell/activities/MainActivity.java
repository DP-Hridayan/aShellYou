package in.hridayan.ashell.activities;

import static in.hridayan.ashell.config.Const.ABOUT_FRAGMENT;
import static in.hridayan.ashell.config.Const.CHANGELOG_FRAGMENT;
import static in.hridayan.ashell.config.Const.EXAMPLES_FRAGMENT;
import static in.hridayan.ashell.config.Const.LOCAL_FRAGMENT;
import static in.hridayan.ashell.config.Const.MODE_REMEMBER_LAST_MODE;
import static in.hridayan.ashell.config.Const.OTG_FRAGMENT;
import static in.hridayan.ashell.config.Const.SETTINGS_FRAGMENT;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.BottomSheets;
import in.hridayan.ashell.UI.KeyboardUtils;
import in.hridayan.ashell.UI.ThemeUtils;
import in.hridayan.ashell.UI.ToastUtils;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.databinding.ActivityMainBinding;
import in.hridayan.ashell.fragments.AboutFragment;
import in.hridayan.ashell.fragments.AshellFragment;
import in.hridayan.ashell.fragments.ChangelogFragment;
import in.hridayan.ashell.fragments.ExamplesFragment;
import in.hridayan.ashell.fragments.OtgFragment;
import in.hridayan.ashell.fragments.SettingsFragment;
import in.hridayan.ashell.fragments.StartFragment;
import in.hridayan.ashell.items.SettingsItem;
import in.hridayan.ashell.utils.CrashHandler;
import in.hridayan.ashell.utils.DeviceUtils;
import in.hridayan.ashell.utils.DeviceUtils.FetchLatestVersionCodeCallback;
import in.hridayan.ashell.utils.FetchLatestVersionCode;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.MainViewModel;

public class MainActivity extends AppCompatActivity
    implements OtgFragment.OnFragmentInteractionListener, FetchLatestVersionCodeCallback {
  public BottomNavigationView mNav;
  private SettingsItem settingsList;
  private static int currentFragment;
  private boolean isKeyboardVisible, hasAppRestarted = true;
  private MainViewModel viewModel;
  private Fragment fragment;
  private String pendingSharedText = null;
  private ActivityMainBinding binding;
  public static final Integer SAVE_DIRECTORY_CODE = 369126;

  // This funtion is run to perform actions if there is an update available or not
  @Override
  public void onResult(int result) {
    if (result == Const.UPDATE_AVAILABLE) BottomSheets.showBottomSheetUpdate(this, this);
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
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    ThemeUtils.updateTheme(this);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Catch exceptions
    Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));

    viewModel = new ViewModelProvider(this).get(MainViewModel.class);

    mNav = findViewById(R.id.bottom_nav_bar);

    // Hide the navigation bar when the keyboard is visible
    keyboardVisibilityListener();

    // The whole navigation setup
    setupNavigation();

    // Show What's new bottom sheet on opening the app after an update
    showChangelogs();

    // Auto check for updates when app launches
    runAutoUpdateCheck();

    // Always put these at the last of onCreate
    Preferences.setActivityRecreated(false);
    hasAppRestarted = false;
  }

  // Main navigation setup
  private void setupNavigation() {
    mNav.setVisibility(View.VISIBLE);
    mNav.setOnItemSelectedListener(
        item -> {
          HapticUtils.weakVibrate(mNav);
          switch (item.getItemId()) {
            case R.id.nav_localShell:
              showAshellFragment();
              Preferences.setCurrentFragment(LOCAL_FRAGMENT);
              return true;

            case R.id.nav_otgShell:
              fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
              if (fragment instanceof AshellFragment && ((AshellFragment) fragment).isShellBusy()) {
                ToastUtils.showToast(
                    this, getString(R.string.abort_command), ToastUtils.LENGTH_SHORT);
                return false;
              }

              showOtgFragment();
              Preferences.setCurrentFragment(OTG_FRAGMENT);
              return true;

            case R.id.nav_wireless:
              ToastUtils.showToast(this, "Soon", ToastUtils.LENGTH_SHORT);
              return false;

            default:
              return false;
          }
        });

    // Shows the "Soon" badge on Wireless ADB mode, will remove this as the feature is implemented
    setBadge(R.id.nav_wireless, "Soon");

    // This function determines what fragment to show when the app launches or activity recreates
    initialFragment();
  }

  // If not on OtgShell then go to OtgShell
  private void showOtgFragment() {
    if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container)
        instanceof OtgFragment)) replaceFragment(new OtgFragment());
  }

  // If not on LocalShell then go to LocalShell (AshellFragment)
  private void showAshellFragment() {
    if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container)
        instanceof AshellFragment)) replaceFragment(new AshellFragment());
  }

  private void setCurrentFragment() {
    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

    if (fragment instanceof SettingsFragment) currentFragment = SETTINGS_FRAGMENT;
    else if (fragment instanceof AshellFragment) currentFragment = LOCAL_FRAGMENT;
    else if (fragment instanceof OtgFragment) currentFragment = OTG_FRAGMENT;
    else if (fragment instanceof ExamplesFragment) currentFragment = EXAMPLES_FRAGMENT;
    else if (fragment instanceof AboutFragment) currentFragment = ABOUT_FRAGMENT;
    else if (fragment instanceof ChangelogFragment) currentFragment = CHANGELOG_FRAGMENT;
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
    if (Preferences.getFirstLaunch()) {
      mNav.setVisibility(View.GONE);
      replaceFragment(new StartFragment());
    } else {
      if (viewModel.isFragmentSaved()) switchFragments(viewModel.currentFragment());
      else {
        int currentFragment = Preferences.getCurrentFragment();
        int launchMode = Preferences.getLaunchMode();
        switchFragments(launchMode == MODE_REMEMBER_LAST_MODE ? currentFragment : launchMode);
      }
      handlePendingSharedText();
      handleIncomingIntent(getIntent());
    }
  }

  // Take the fragment value and switch to it accordingly
  private void switchFragments(int currentFragment) {
    switch (currentFragment) {
      case LOCAL_FRAGMENT:
        mNav.setSelectedItemId(R.id.nav_localShell);
        replaceFragment(new AshellFragment());
        break;
      case OTG_FRAGMENT:
        mNav.setSelectedItemId(R.id.nav_otgShell);
        replaceFragment(new OtgFragment());
        break;
      case SETTINGS_FRAGMENT:
        replaceFragment(new SettingsFragment());
        break;
      case EXAMPLES_FRAGMENT:
        replaceFragment(new ExamplesFragment());
        break;

      case CHANGELOG_FRAGMENT:
        replaceFragment(new ChangelogFragment());
        break;
      case ABOUT_FRAGMENT:
        replaceFragment(new AboutFragment());
        break;

      default:
        break;
    }
  }

  // Takes the fragment we want to navigate to as argument and then starts that fragment
  public void replaceFragment(Fragment fragment) {
    if (!getSupportFragmentManager().isStateSaved()) {
      // Clear the most recent fragment from the back stack
      getSupportFragmentManager().popBackStack();

      // Begin the fragment transaction
      FragmentTransaction transaction =
          getSupportFragmentManager()
              .beginTransaction()
              .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());

      // Handle the settings fragment case
      if (currentFragment == SETTINGS_FRAGMENT) {
        String tag = "settingsButtonToSettings";
        View settingsButton = getSettingsButtonView();

        if (settingsButton != null) {
          transaction
              .addSharedElement(settingsButton, tag)
              .addToBackStack(fragment.getClass().getSimpleName());
        }
      }
      if (currentFragment == EXAMPLES_FRAGMENT) {
        String tag = "sendButtonToExamples";
        View sendButton = getSendButtonView();

        if (sendButton != null) {
          transaction
              .addSharedElement(sendButton, tag)
              .addToBackStack(fragment.getClass().getSimpleName());
        }
      }

      transaction.commit();
      setCurrentFragment();
    }
  }

  private View getSettingsButtonView() {
    return viewModel.whichHomeFragment() == LOCAL_FRAGMENT
        ? AshellFragment.getSettingsButtonView()
        : OtgFragment.getSettingsButtonView();
  }

  private View getSendButtonView() {
    return viewModel.whichHomeFragment() == LOCAL_FRAGMENT
        ? AshellFragment.getSendButtonView()
        : OtgFragment.getSendButtonView();
  }

  private void handlePendingSharedText() {
    if (pendingSharedText != null) {
      switch (Preferences.getCurrentFragment()) {
        case LOCAL_FRAGMENT:
          AshellFragment fragmentLocalAdb =
              (AshellFragment)
                  getSupportFragmentManager().findFragmentById(R.id.fragment_container);
          if (fragmentLocalAdb != null) {
            fragmentLocalAdb.updateInputField(pendingSharedText);
            clearPendingSharedText();
          }
          break;

        case OTG_FRAGMENT:
          OtgFragment fragmentOtg =
              (OtgFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
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

  public void clearPendingSharedText() {
    pendingSharedText = null;
  }

  public String getPendingSharedText() {
    return pendingSharedText;
  }

  private void handleIncomingIntent(Intent intent) {
    if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.hasExtra(Intent.EXTRA_TEXT)) {
      String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
      if (sharedText != null) {
        sharedText = sharedText.trim().replaceAll("^\"|\"$", "");
        pendingSharedText = sharedText;
        handleSharedTextIntent(sharedText, intent);
      }
    } else if ("in.hridayan.ashell.ACTION_USB_DETACHED".equals(intent.getAction())) onUsbDetached();
  }

  private void handleSharedTextIntent(String sharedText, Intent intent) {
    setTextOnEditText(sharedText, intent);
  }

  // Set the text in the Input Field
  private void setTextOnEditText(String text, Intent intent) {
    setCurrentFragment();

    switch (currentFragment) {
      case LOCAL_FRAGMENT:
        AshellFragment fragmentLocalAdb =
            (AshellFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragmentLocalAdb != null) fragmentLocalAdb.handleSharedTextIntent(intent, text);
        break;

      case OTG_FRAGMENT:
        OtgFragment fragmentOtg =
            (OtgFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragmentOtg != null) fragmentOtg.updateInputField(text);
        break;

      default:
        return;
    }
  }

  // Execute functions when the Usb connection is removed
  public void onUsbDetached() {
    // Reset the OtgFragment in this case
    onRequestReset();
  }

  // Reset the OtgFragment
  @Override
  public void onRequestReset() {
    if ((getSupportFragmentManager().findFragmentById(R.id.fragment_container)
        instanceof OtgFragment)) {
      currentFragment = OTG_FRAGMENT;
      mNav.setSelectedItemId(R.id.nav_otgShell);
      replaceFragment(new OtgFragment());
    }
  }

  /*We set the currentFragment value before then if current fragment value is SETTINGS_FRAGMENT we donot show the bottom navigation*/
  private void showBottomNavUnderConditions() {
    setCurrentFragment();
    if (currentFragment == LOCAL_FRAGMENT || currentFragment == OTG_FRAGMENT)
      mNav.setVisibility(View.VISIBLE);
  }

  // show bottom sheet for changelog after an update
  private void showChangelogs() {
    if (DeviceUtils.isAppUpdated(this)) BottomSheets.showBottomSheetChangelog(this);
    /* we save the current version code and then when the app updates it compares the saved version code to the updated app's version code to determine whether to show changelogs */
    Preferences.setSavedVersionCode(DeviceUtils.currentVersion());
  }

  // show update available bottom sheet
  private void runAutoUpdateCheck() {
    if (Preferences.getAutoUpdateCheck()
        && hasAppRestarted
        && !Preferences.getActivityRecreated()
        && !Preferences.getFirstLaunch()) {
      new FetchLatestVersionCode(this, this).execute(Const.URL_BUILD_GRADLE);
    }
  }

  // keyboard visibility listener
  private void keyboardVisibilityListener() {
    KeyboardUtils.attachVisibilityListener(
        this,
        new KeyboardUtils.KeyboardVisibilityListener() {
          @Override
          public void onKeyboardVisibilityChanged(boolean visible) {
            isKeyboardVisible = visible;
            if (isKeyboardVisible) mNav.setVisibility(View.GONE);
            else
              new Handler(Looper.getMainLooper())
                  .postDelayed(
                      () -> {
                        showBottomNavUnderConditions();
                      },
                      100);
          }
        });
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == SAVE_DIRECTORY_CODE && resultCode == RESULT_OK) {
      Uri treeUri = data.getData();
      if (treeUri != null) {
        getContentResolver()
            .takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Preferences.setSavedOutputDir(String.valueOf(treeUri));
      }
    }
  }
}
