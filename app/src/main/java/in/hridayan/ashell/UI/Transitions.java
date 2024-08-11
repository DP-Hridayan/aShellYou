package in.hridayan.ashell.UI;

import android.view.View;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.MaterialArcMotion;
import android.graphics.Color;
import android.view.ViewGroup;
import androidx.transition.TransitionManager;

public class Transitions {
    
    public static void materialContainerTransformViewToView(View startView, View endView) {
    // Set up the MaterialContainerTransform
    MaterialContainerTransform transform = new MaterialContainerTransform();

    // Manually tell the container transform which Views to transform between.
    transform.setStartView(startView);
    transform.setEndView(endView);

    // Ensure the container transform only runs on a single target
    transform.addTarget(endView);

    // Optionally add a curved path to the transform
    transform.setPathMotion(new MaterialArcMotion());

    // Since View to View transforms often are not transforming into full screens,
    // remove the transition's scrim.
    transform.setScrimColor(Color.TRANSPARENT);

    // Begin the transition by changing properties on the start and end views
    TransitionManager.beginDelayedTransition((ViewGroup) startView.getRootView(), transform);

    // Hide the start view and show the end view
    startView.setVisibility(View.GONE);
    endView.setVisibility(View.VISIBLE);
  }
}
