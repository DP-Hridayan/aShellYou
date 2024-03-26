package in.hridayan.ashell.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.KeyboardUtils;
import in.hridayan.ashell.fragments.StartFragment;
import in.hridayan.ashell.fragments.aShellFragment;
import in.hridayan.ashell.fragments.otgShellFragment;

public class MainActivity extends AppCompatActivity {
  private boolean isKeyboardVisible;
  public BottomNavigationView mNav;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    super.onCreate(savedInstanceState);
    setTheme(R.style.AppTheme);
    setContentView(R.layout.activity_main);

    mNav = findViewById(R.id.bottom_nav_bar);

    mNav.setSelectedItemId(R.id.nav_localShell);

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
    setBadge(R.id.nav_otgShell, "Beta");
    setBadge(R.id.nav_wireless, "Soon");

    handleSharedTextIntent(getIntent());
  }

  private void handleSharedTextIntent(Intent intent) {
    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
    if (sharedText != null) {
      aShellFragment fragment =
          (aShellFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
      if (fragment != null) {
        fragment.updateInputField(sharedText);
      }
    }
  }

  private void setupNavigation() {
    mNav.setVisibility(View.VISIBLE);
    mNav.setOnItemSelectedListener(
        item -> {
          switch (item.getItemId()) {
            case R.id.nav_localShell:
              showaShellFragment();
              return true;
            case R.id.nav_otgShell:
              showotgShellFragment();
              return true;
            default:
              return false;
          }
        });

    initialFragment();
  }

  private void replaceFragment(Fragment fragment) {

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
        replaceFragment(new otgShellFragment());
      }
    }
  }

  private void showaShellFragment() {
    if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container)
        instanceof aShellFragment)) {
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
      replaceFragment(new aShellFragment());
    }
  }
}
