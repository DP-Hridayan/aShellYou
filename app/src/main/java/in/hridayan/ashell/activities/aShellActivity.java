package in.hridayan.ashell.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.content.DialogInterface;
import android.view.ViewTreeObserver;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import in.hridayan.ashell.R;
import in.hridayan.ashell.fragments.aShellFragment;
import in.hridayan.ashell.fragments.otgFragment;
import in.hridayan.ashell.utils.ShizukuShell;

public class aShellActivity extends AppCompatActivity {
  private BottomNavigationView mNav;
  private View mContentView;

  private ShizukuShell mShizukuShell;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ashell);

    mContentView = findViewById(android.R.id.content);
    mNav = findViewById(R.id.bottom_nav_bar);

    mNav.setVisibility(View.VISIBLE);

    mContentView
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                int heightDiff = mContentView.getRootView().getHeight() - mContentView.getHeight();
                if (heightDiff > 200) {
                  mNav.setVisibility(View.GONE);
                } else {
                  new Handler(Looper.getMainLooper())
                      .postDelayed(
                          new Runnable() {
                            @Override
                            public void run() {
                              mNav.setVisibility(View.VISIBLE);
                            }
                          },
                          100);
                }
              }
            });

    BadgeDrawable badge = mNav.getOrCreateBadge(R.id.nav_otgShell);
    badge.setVisible(true);
    badge.setText("Beta");
    mNav.setOnItemSelectedListener(
        item -> {
          switch (item.getItemId()) {
            case R.id.nav_localShell:
              if ((getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                  instanceof otgFragment)) {
                getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new aShellFragment())
                    .commit();
              }

              return true;
            case R.id.nav_otgShell:
              if ((getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                  instanceof aShellFragment)) {
                // Don't show again logic
                if (PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean("Don't show beta otg warning", true)) {
                  MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                  builder
                            .setCancelable(false)
                      .setTitle("Warning")
                      .setMessage(getString(R.string.beta_warning))
                      .setPositiveButton(
                          "Accept",
                          (dialogInterface, i) -> {
                            getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, new otgFragment())
                                .commit();
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

                            getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, new otgFragment())
                                .commit();
                          })
                      .show();

                } else {
                  getSupportFragmentManager()
                      .beginTransaction()
                      .replace(R.id.fragment_container, new otgFragment())
                      .commit();
                }
              }
              return true;
            default:
              return false;
          }
        });
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, new aShellFragment())
        .commit();
  }
}
