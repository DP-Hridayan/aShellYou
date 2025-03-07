package in.hridayan.ashell.activities;

import static in.hridayan.ashell.config.Const.LOCAL_FRAGMENT;
import static in.hridayan.ashell.config.Const.OTG_FRAGMENT;
import static in.hridayan.ashell.config.Const.WIFI_ADB_FRAGMENT;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.ThemeUtils;
import in.hridayan.ashell.UI.bottomsheets.ChangelogBottomSheet;
import in.hridayan.ashell.UI.bottomsheets.UpdateCheckerBottomSheet;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.databinding.ActivityMainBinding;
import in.hridayan.ashell.fragments.home.AshellFragment;
import in.hridayan.ashell.fragments.home.HomeFragment;
import in.hridayan.ashell.fragments.home.OtgFragment;
import in.hridayan.ashell.fragments.home.WifiAdbFragment;
import in.hridayan.ashell.fragments.setup.StartFragment;
import in.hridayan.ashell.items.SettingsItem;
import in.hridayan.ashell.utils.AppUpdater;
import in.hridayan.ashell.utils.CrashHandler;
import in.hridayan.ashell.utils.DeviceUtils;
import in.hridayan.ashell.utils.DeviceUtils.FetchLatestVersionCodeCallback;
import in.hridayan.ashell.utils.FetchLatestVersionCode;
import java.io.File;

public class MainActivity extends AppCompatActivity
    implements OtgFragment.OnFragmentInteractionListener, FetchLatestVersionCodeCallback {
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
    if (result == Const.UPDATE_AVAILABLE){
       UpdateCheckerBottomSheet updateChecker = new UpdateCheckerBottomSheet(this, this);
updateChecker.show();
    } 
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    handleIncomingIntent(intent);
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (Preferences.getUnknownSourcePermAskStatus()) {
      Preferences.setUnknownSourcePermAskStatus(false);

      String apkFileName = Preferences.getUpdateApkFileName();

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
          && getPackageManager().canRequestPackageInstalls()) {
        // Permission granted, retry installation

        if (apkFileName != null) {
          File apkFile = new File(getExternalFilesDir(null), apkFileName);
          if (apkFile.exists()) {
            AppUpdater.promptInstall(this, apkFile);
          }
        }
      }
    }
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

    initialFragment();

    runAutoUpdateCheck();

    showChangelogs();

    handlePendingSharedText();

    handleIncomingIntent(getIntent());

    Preferences.setActivityRecreated(false);

    hasAppRestarted = false;
  }

  // Set the initial fragment upon launch
  private void initialFragment() {
    replaceFragment(Preferences.getFirstLaunch() ? new StartFragment() : new HomeFragment());
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

  // show update available bottom sheet
  private void runAutoUpdateCheck() {
    if (Preferences.getAutoUpdateCheck()
        && hasAppRestarted
        && !Preferences.getActivityRecreated()
        && !Preferences.getFirstLaunch()) {
      new FetchLatestVersionCode(this, this).execute(Const.URL_BUILD_GRADLE);
    }
  }

  private void handlePendingSharedText() {
    if (pendingSharedText != null) {
      switch (Preferences.getCurrentFragment()) {
        case LOCAL_FRAGMENT:
          if (!(fragment instanceof AshellFragment)) replaceFragment(new AshellFragment());
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

        case WIFI_ADB_FRAGMENT:
          WifiAdbFragment fragmentWifiAdb =
              (WifiAdbFragment)
                  getSupportFragmentManager().findFragmentById(R.id.fragment_container);
          if (fragmentWifiAdb != null) {
            fragmentWifiAdb.updateInputField(pendingSharedText);
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
        if (!(fragment instanceof AshellFragment)) replaceFragment(new AshellFragment());
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
      replaceFragment(new OtgFragment());
    }
  }

  // show bottom sheet for changelog after an update
  private void showChangelogs() {
    if (DeviceUtils.isAppUpdated(this)){
        ChangelogBottomSheet changelogSheet = new ChangelogBottomSheet(this);
changelogSheet.show();
    }
    /* we save the current version code and then when the app updates it compares the saved version code to the updated app's version code to determine whether to show changelogs */
    Preferences.setSavedVersionCode(DeviceUtils.currentVersion());
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
