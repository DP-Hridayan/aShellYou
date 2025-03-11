package in.hridayan.ashell.UI;

import android.view.View;
import com.google.android.material.transition.MaterialContainerTransform;
import android.graphics.Color;
import android.view.ViewGroup;
import androidx.transition.TransitionManager;

public class Transitions {

  public static void materialContainerTransformViewToView(View startView, View endView) {
    MaterialContainerTransform transform = new MaterialContainerTransform();

    transform.setStartView(startView);
    transform.setEndView(endView);
    transform.addTarget(endView);

    // Optionally add a curved path to the transform
    // transform.setPathMotion(new MaterialArcMotion());

    transform.setScrimColor(Color.TRANSPARENT);

    TransitionManager.beginDelayedTransition((ViewGroup) startView.getRootView(), transform);

    startView.setVisibility(View.GONE);
    endView.setVisibility(View.VISIBLE);
  }
}
