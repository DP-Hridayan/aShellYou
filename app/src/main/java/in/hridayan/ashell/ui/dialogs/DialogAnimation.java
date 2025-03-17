package in.hridayan.ashell.ui.dialogs;

import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.transition.MaterialContainerTransform;
import android.graphics.Color;
import androidx.transition.TransitionManager;

public class DialogAnimation {
    public static void materialContainerTransform(View startView, View endView, boolean toDialog) {
        MaterialContainerTransform transform = new MaterialContainerTransform();
        transform.setStartView(startView);
        transform.setEndView(endView);
        transform.addTarget(endView);
        transform.setScrimColor(Color.TRANSPARENT);

        TransitionManager.beginDelayedTransition((ViewGroup) startView.getRootView(), transform);

        startView.setVisibility(toDialog ? View.INVISIBLE : View.VISIBLE);
        endView.setVisibility(View.VISIBLE);
    }

    public static void showDialogWithTransition(
        View startView, View dialogCard, View dimBackground, boolean modeDialog) {
        dimBackground.setVisibility(View.VISIBLE);
        dimBackground.animate().alpha(1f).setDuration(modeDialog ? 0 : 300).start();
        materialContainerTransform(startView, dialogCard, true);
    }

    public static void dismissDialogWithTransition(
        View startView, View dialogCard, View dimBackground, ViewGroup rootView, boolean modeDialog) {
        dimBackground.animate()
            .alpha(0f)
            .setDuration(modeDialog ? 0 : 300)
            .withEndAction(() -> {
                dimBackground.setVisibility(View.GONE);
                rootView.removeView(dimBackground);
            })
            .start();
        materialContainerTransform(dialogCard, startView, false);
    }
}