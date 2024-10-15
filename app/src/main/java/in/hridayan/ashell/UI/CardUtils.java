package in.hridayan.ashell.UI;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.google.android.material.card.MaterialCardView;

public class CardUtils {

  public static void hideCardSmoothly(MaterialCardView card) {
    if (card != null) {
      // Calculate the width of the card
      float width = card.getWidth();

      // Animate translation to the left of the screen
      card.animate()
          .translationX(-width)
          .setDuration(200)
          .setInterpolator(new AccelerateInterpolator()) // Duration of the animation
          .withEndAction(() -> card.setVisibility(View.GONE))
          .start();
    }
  }

  public static void showCardSmoothly(MaterialCardView card) {
    if (card != null) {
      // Set the visibility to visible before starting the animation
      card.setVisibility(View.VISIBLE);

      // Animate translation to its original position
      card.animate()
          .translationX(0)
          .setDuration(200)
          .setInterpolator(new DecelerateInterpolator()) // Duration of the animation
          .start();
    }
  }
}
