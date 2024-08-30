package in.hridayan.ashell.UI;

import android.view.View;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavUtils {

  public static void hideNavSmoothly(BottomNavigationView mNav) {
    if (mNav != null) {
      // Calculate the height of the BottomNavigationView
      int height = mNav.getHeight();

      // Animate translation to the bottom of the screen
      mNav.animate()
          .translationY(height)
          .setDuration(200) // Duration of the animation
          .withEndAction(() -> mNav.setVisibility(View.GONE))
          .start();
    }
  }

  public static void showNavSmoothly(BottomNavigationView mNav) {
    if (mNav != null) {
      // Set the visibility to visible before starting the animation
      mNav.setVisibility(View.VISIBLE);

      // Animate translation to its original position
      mNav.animate()
          .translationY(0)
          .setDuration(200) // Duration of the animation
          .start();
    }
  }
}
