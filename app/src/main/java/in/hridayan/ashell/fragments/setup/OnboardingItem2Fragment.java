package in.hridayan.ashell.fragments.setup;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import in.hridayan.ashell.databinding.FragmentOnboardingItem2Binding;

public class OnboardingItem2Fragment extends Fragment {

  private FragmentOnboardingItem2Binding binding;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentOnboardingItem2Binding.inflate(inflater, container, false);

    // We take 10% of the screen height as top margin an then assign a top margin to the disclaimer
    // title
    int screenHeight = getResources().getDisplayMetrics().heightPixels;
    int topMargin = (int) (screenHeight * 0.1);

    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.topMargin = topMargin;
    params.gravity = Gravity.CENTER_HORIZONTAL;

    binding.disclaimer.setLayoutParams(params);

    return binding.getRoot();
  }
}
