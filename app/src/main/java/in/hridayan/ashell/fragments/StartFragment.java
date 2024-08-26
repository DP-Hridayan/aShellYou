package in.hridayan.ashell.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.OnboardingAdapter;
import in.hridayan.ashell.utils.Preferences;

public class StartFragment extends Fragment {

  private OnboardingAdapter adapter;
  private ViewPager2 viewPager;
  private MaterialButton btnNext, btnPrev;
  private OnBackPressedCallback onBackPressedCallback;

  public StartFragment() {}

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_start, container, false);
    initViews(view);
    return view;
  }

  private void initViews(View view) {
    viewPager = view.findViewById(R.id.viewPager);
    btnNext = view.findViewById(R.id.btn_next);
    btnPrev = view.findViewById(R.id.btn_prev);

    adapter = new OnboardingAdapter(getChildFragmentManager(), requireActivity().getLifecycle());

    adapter.addFragment(new OnboardingItem1Fragment());
    adapter.addFragment(new OnboardingItem2Fragment());
    adapter.addFragment(new OnboardingItem3Fragment());

    viewPager.setAdapter(adapter);

    viewPager.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {
          @Override
          public void onPageSelected(int position) {
            super.onPageSelected(position);

            animateBackButton(position);
            changeContinueButtonText(position);
          }
        });

    btnNext.setOnClickListener(
        v -> {
          if (viewPager.getCurrentItem() == adapter.getItemCount() - 1) {
            // this is the last page
            if (isBasicMode()) confirmationDialog();
            else enterHomeFragment();

          } else { // this is not the last page, so just go to next page
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
          }
        });

    btnPrev.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true));

    registerOnBackInvokedCallback();
  }

  private boolean isBasicMode() {
    return Preferences.getLocalAdbMode(requireContext()) == Preferences.BASIC_MODE;
  }

  private void confirmationDialog() {
    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.warning))
        .setMessage(getString(R.string.confirm_basic_mode))
        .setNegativeButton(getString(R.string.cancel), null)
        .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> enterHomeFragment())
        .show();
  }

  private void enterHomeFragment() {
    Preferences.setFirstLaunch(requireContext(), false);
    getParentFragmentManager()
        .beginTransaction()
        .setCustomAnimations(
            R.anim.fragment_enter,
            R.anim.fragment_exit,
            R.anim.fragment_pop_enter,
            R.anim.fragment_pop_exit)
        .replace(R.id.fragment_container, new AshellFragment())
        .commit();
  }

  private void animateBackButton(int position) {
    int duration = 300;

    if (position == 0 && btnPrev.getVisibility() == View.VISIBLE) {
      AlphaAnimation fadeOut = getFadeOutAnimation(duration);
      btnPrev.startAnimation(fadeOut);
    } else if (position != 0 && btnPrev.getVisibility() != View.VISIBLE) {
      AlphaAnimation fadeIn = getFadeInAnimation(duration);
      btnPrev.startAnimation(fadeIn);
    }
  }

  private @NonNull AlphaAnimation getFadeOutAnimation(int duration) {
    AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);

    fadeOut.setDuration(duration);

    fadeOut.setAnimationListener(
        new Animation.AnimationListener() {
          @Override
          public void onAnimationStart(Animation animation) {}

          @Override
          public void onAnimationEnd(Animation animation) {
            btnPrev.setVisibility(View.GONE);
          }

          @Override
          public void onAnimationRepeat(Animation animation) {}
        });

    return fadeOut;
  }

  private @NonNull AlphaAnimation getFadeInAnimation(int duration) {
    AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);

    fadeIn.setDuration(duration);

    fadeIn.setAnimationListener(
        new Animation.AnimationListener() {
          @Override
          public void onAnimationStart(Animation animation) {
            btnPrev.setVisibility(View.VISIBLE);
          }

          @Override
          public void onAnimationEnd(Animation animation) {}

          @Override
          public void onAnimationRepeat(Animation animation) {}
        });

    return fadeIn;
  }

  private void changeContinueButtonText(int position) {
    if (position == adapter.getItemCount() - 1) btnNext.setText(R.string.start);
    else btnNext.setText(R.string.btn_continue);
  }

  private void registerOnBackInvokedCallback() {
    onBackPressedCallback =
        new OnBackPressedCallback(true) {
          @Override
          public void handleOnBackPressed() {
            onBackPressed();
          }
        };

    requireActivity()
        .getOnBackPressedDispatcher()
        .addCallback(requireActivity(), onBackPressedCallback);
  }

  private void onBackPressed() {
    if (viewPager.getCurrentItem() == 0) requireActivity().finish();
    else viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (onBackPressedCallback != null) {
      onBackPressedCallback.remove(); // Deregister the callback
    }
  }
}
