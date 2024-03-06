package in.hridayan.ashell.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.fragments.aShellFragment;
import in.hridayan.ashell.fragments.otgFragment;

public class aShellActivity extends AppCompatActivity {
  private BottomNavigationView mNav;
  private View mContentView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ashell);

    mContentView = findViewById(android.R.id.content);
    mNav = findViewById(R.id.bottom_nav_bar);

    // Initially, show the BottomNavigationView
    mNav.setVisibility(View.VISIBLE);

    // Listen for keyboard visibility changes
    mContentView
        .getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                int heightDiff = mContentView.getRootView().getHeight() - mContentView.getHeight();
                if (heightDiff > 200) { // arbitrary threshold to determine keyboard visibility
                  // Keyboard is visible, hide the BottomNavigationView
                  mNav.setVisibility(View.GONE);
                } else {
                  // Keyboard is hidden, show the BottomNavigationView
                  mNav.setVisibility(View.VISIBLE);
                }
              }
            });

    mNav.setOnItemSelectedListener(
        item -> {
          switch (item.getItemId()) {
            case R.id.nav_localShell:
              getSupportFragmentManager()
                  .beginTransaction()
                  .replace(R.id.fragment_container, new aShellFragment())
                  .commit();
              return true;
            case R.id.nav_otgShell:
              getSupportFragmentManager()
                  .beginTransaction()
                  .replace(R.id.fragment_container, new otgFragment())
                  .commit();
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
