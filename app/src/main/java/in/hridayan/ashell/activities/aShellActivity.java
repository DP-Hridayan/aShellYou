package in.hridayan.ashell.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import in.hridayan.ashell.R;
import in.hridayan.ashell.fragments.aShellFragment;
import in.hridayan.ashell.fragments.otgFragment;

public class aShellActivity extends AppCompatActivity {
  public BottomNavigationView mNav;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ashell);

    mNav = findViewById(R.id.bottom_nav_bar);

    setupKeyboardVisibilityListener();
    setupNavigation();
    setBadge(R.id.nav_otgShell, "Beta");
    setBadge(R.id.nav_wireless, "Soon");
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
              showOtgFragment();
              return true;
            default:
              return false;
          }
        });
    // Set initial fragment
    replaceFragment(new aShellFragment(mNav));
  }

  private void replaceFragment(Fragment fragment) {

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit();
  }

  private void showOtgFragment() {
    if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container)
        instanceof otgFragment)) {
      // Don't show again logic
      if (PreferenceManager.getDefaultSharedPreferences(this)
          .getBoolean("Don't show beta otg warning", true)) {
        showBetaWarning();
      } else {
        replaceFragment(new otgFragment());
      }
    }
  }

  private void showaShellFragment() {
    if (!(getSupportFragmentManager().findFragmentById(R.id.fragment_container)
        instanceof aShellFragment)) {
      replaceFragment(new aShellFragment(mNav));
    }
  }

  private void showBetaWarning() {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    builder
        .setCancelable(false)
        .setTitle("Warning")
        .setMessage(getString(R.string.beta_warning))
        .setPositiveButton(
            "Accept",
            (dialogInterface, i) -> {
              replaceFragment(new otgFragment());
            })
        .setNegativeButton(
            "Go back",
            (dialogInterface, i) -> {
              mNav.setSelectedItemId(R.id.nav_localShell);
            })
        .setNeutralButton(
            "Don't show again",
            (dialogInterface, i) -> {
              PreferenceManager.getDefaultSharedPreferences(this)
                  .edit()
                  .putBoolean("Don't show beta otg warning", false)
                  .apply();
              replaceFragment(new otgFragment());
            })
        .show();
  }

  private void setBadge(int id, String text) {
    BadgeDrawable badge = mNav.getOrCreateBadge(id);
    badge.setVisible(true);
    badge.setText(text);
    badge.setHorizontalOffset(0);
  }

  private void setupKeyboardVisibilityListener() {
    View contentView = findViewById(android.R.id.content);
    contentView
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(new KeyboardVisibilityListener(contentView));
  }

  private class KeyboardVisibilityListener implements ViewTreeObserver.OnGlobalLayoutListener {
    private final View contentView;

    KeyboardVisibilityListener(View contentView) {
      this.contentView = contentView;
    }

    @Override
    public void onGlobalLayout() {
      int heightDiff = contentView.getRootView().getHeight() - contentView.getHeight();
      if (heightDiff > 200) {
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
  }
}
