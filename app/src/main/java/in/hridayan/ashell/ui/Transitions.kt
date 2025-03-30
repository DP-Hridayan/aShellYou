package `in`.hridayan.ashell.ui

import android.graphics.Color
import android.view.View
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialContainerTransform

object Transitions {
    @JvmStatic
    fun materialContainerTransformViewToView(startView: View, endView: View) {
        val transform = MaterialContainerTransform()

        transform.startView = startView
        transform.endView = endView
        transform.addTarget(endView)

        // Optionally add a curved path to the transform
        // transform.setPathMotion(new MaterialArcMotion());
        transform.scrimColor = Color.TRANSPARENT

        TransitionManager.beginDelayedTransition(
            (startView.getRootView() as android.view.ViewGroup),
            transform
        )

        startView.visibility = View.GONE
        endView.visibility = View.VISIBLE
    }
}
