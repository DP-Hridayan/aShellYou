package in.hridayan.ashell.activities;

import static in.hridayan.ashell.config.Const.LOCAL_FRAGMENT;
import static in.hridayan.ashell.config.Const.MODE_REMEMBER_LAST_MODE;
import static in.hridayan.ashell.config.Const.OTG_FRAGMENT;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.BottomSheets;
import in.hridayan.ashell.UI.KeyboardUtils;
import in.hridayan.ashell.UI.ThemeUtils;
import in.hridayan.ashell.UI.ToastUtils;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.databinding.ActivityMainBinding;
import in.hridayan.ashell.fragments.home.AshellFragment;
import in.hridayan.ashell.fragments.home.OtgFragment;
import in.hridayan.ashell.fragments.setup.StartFragment;
import in.hridayan.ashell.items.SettingsItem;
import in.hridayan.ashell.utils.CrashHandler;
import in.hridayan.ashell.utils.DeviceUtils;
import in.hridayan.ashell.utils.DeviceUtils.FetchLatestVersionCodeCallback;
import in.hridayan.ashell.utils.FetchLatestVersionCode;
import in.hridayan.ashell.utils.HapticUtils;

public class MainActivity extends AppCompatActivity
    implements OtgFragment.OnFragmentInteractionListener, FetchLatestVersionCodeCallback {
  public BottomNavigationView mNav;
  private SettingsItem settingsList;
  private static int currentFragment;
  private boolean isKeyboardVisible, hasAppRestarted = true;
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
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    Fragment currentFragment =
        getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    if (currentFragment != null)
      getSupportFragmentManager().putFragment(outState, Const.CURRENT_FRAGMENT, currentFragment);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);

    if (savedInstanceState != null) {
      try {
        Fragment currentFragment =
            getSupportFragmentManager().getFragment(savedInstanceState, Const.CURRENT_FRAGMENT);
        if (currentFragment != null) replaceFragment(currentFragment);
      } catch (IllegalStateException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    ThemeUtils.updateTheme(this);
    EdgeToEdge.enable(this);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Catch exceptions
    Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));

    mNav = findViewById(R.id.bottom_nav_bar);

    setupNavigation();

    handlePendingSharedText();

    handleIncomingIntent(getIntent());

    keyboardVisibilityListener();

    showChangelogs();

    runAutoUpdateCheck();

    Preferences.setActivityRecreated(false);

    hasAppRestarted = false;
  }

  // Main navigation setup
  private void setupNavigation() {
    initialFragment();

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

    setBadge(R.id.nav_wireless, "Soon");
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

  private void setBadge(int id, String text) {
    BadgeDrawable badge = mNav.getOrCreateBadge(id);
    badge.setVisible(true);
    badge.setText(text);
    badge.setHorizontalOffset(0);
  }

  private void initialFragment() {
    if (Preferences.getFirstLaunch()) {
      replaceFragment(new StartFragment());
    } else {
      int currentFragment = Preferences.getCurrentFragment();
      int launchMode = Preferences.getLaunchMode();
      defaultHomeFragment(launchMode == MODE_REMEMBER_LAST_MODE ? currentFragment : launchMode);
    }
  }

  private void defaultHomeFragment(int fragmentId) {
    if (fragmentId == Const.LOCAL_FRAGMENT) {
      mNav.setSelectedItemId(R.id.nav_localShell);
      replaceFragment(new AshellFragment());
    } else if (fragmentId == Const.OTG_FRAGMENT) {
      mNav.setSelectedItemId(R.id.nav_otgShell);
      replaceFragment(new OtgFragment());
    }
  }

  // Takes the fragment we want to navigate to as argument and then starts that fragment
  public void replaceFragment(Fragment fragment) {
    if (!getSupportFragmentManager().isStateSaved()) {
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
          .commit();
    }
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
                        mNav.setVisibility(View.VISIBLE);
                      },
                      100);
          }
        });
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
