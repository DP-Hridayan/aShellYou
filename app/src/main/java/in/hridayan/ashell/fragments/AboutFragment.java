package in.hridayan.ashell.fragments;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialContainerTransform;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.BottomSheets;
import in.hridayan.ashell.UI.CategoryAbout;
import in.hridayan.ashell.UI.ToastUtils;
import in.hridayan.ashell.adapters.AboutAdapter;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.databinding.FragmentAboutBinding;
import in.hridayan.ashell.utils.DeviceUtils;
import in.hridayan.ashell.utils.FetchLatestVersionCode;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.AboutViewModel;
import java.util.ArrayList;
import java.util.List;
import in.hridayan.ashell.config.Const.Contributors;

public class AboutFragment extends Fragment
    implements AboutAdapter.AdapterListener, DeviceUtils.FetchLatestVersionCodeCallback {

  private AboutViewModel viewModel;
  private FragmentAboutBinding binding;
  private Pair<Integer, Integer> mRVPositionAndOffset;
  private LottieAnimationView loadingDots;
  private Drawable updateButtonIcon;
  private BottomNavigationView mNav;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    binding = FragmentAboutBinding.inflate(inflater, container, false);
    setSharedElementEnterTransition(new MaterialContainerTransform());
    postponeEnterTransition();
    setExitTransition(new Hold());

    mNav = requireActivity().findViewById(R.id.bottom_nav_bar);

    viewModel = new ViewModelProvider(requireActivity()).get(AboutViewModel.class);

    setupRecyclerView();
    setupListeners();

    return binding.getRoot();
  }

  private void setupRecyclerView() {
    mNav.setVisibility(View.GONE);
    binding.rvAbout.setLayoutManager(new LinearLayoutManager(getContext()));
    AboutAdapter adapter = new AboutAdapter(initializeItems(), requireActivity());
    adapter.setAdapterListener(this);
    binding.rvAbout.setAdapter(adapter);
    binding.rvAbout.getViewTreeObserver().addOnDrawListener(this::startPostponedEnterTransition);
  }

  private void setupListeners() {
    binding.arrowBack.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          requireActivity().getSupportFragmentManager().popBackStack();
        });
  }

  private List<Object> initializeItems() {
    List<Object> items = new ArrayList<>();
    items.add(new CategoryAbout(getString(R.string.lead_developer)));
    items.add(
        new CategoryAbout.LeadDeveloperItem(
            Contributors.HRIDAYAN.getName(),
            getString(R.string.hridayan_about),
            R.mipmap.dp_hridayan));

    items.add(new CategoryAbout(getString(R.string.contributors)));
    items.add(
        new CategoryAbout.ContributorsItem(
            Contributors.KRISHNA,
            Contributors.KRISHNA.getName(),
            getString(R.string.krishna_about),
            R.mipmap.dp_krishna));
    items.add(
        new CategoryAbout.ContributorsItem(
            Contributors.STARRY,
            Contributors.STARRY.getName(),
            getString(R.string.shivam_about),
            R.mipmap.dp_shivam));
    items.add(
        new CategoryAbout.ContributorsItem(
            Contributors.DISAGREE,
            Contributors.DISAGREE.getName(),
            getString(R.string.drDisagree_about),
            R.mipmap.dp_drdisagree));
    items.add(
        new CategoryAbout.ContributorsItem(
            Contributors.RIKKA,
            Contributors.RIKKA.getName(),
            getString(R.string.rikka_about),
            R.mipmap.dp_shizuku));
    items.add(
        new CategoryAbout.ContributorsItem(
            Contributors.SUNILPAULMATHEW,
            Contributors.SUNILPAULMATHEW.getName(),
            getString(R.string.sunilpaulmathew_about),
            R.mipmap.dp_sunilpaulmathew));
    items.add(
        new CategoryAbout.ContributorsItem(
            Contributors.KHUN_HTETZ,
            Contributors.KHUN_HTETZ.getName(),
            getString(R.string.khun_htetz_about),
            R.mipmap.dp_adb_otg));
    items.add(
        new CategoryAbout.ContributorsItem(
            Contributors.MARCIOZOMB,
            Contributors.MARCIOZOMB.getName(),
            getString(R.string.marciozomb13_about),
            R.mipmap.dp_marciozomb13));
    items.add(
        new CategoryAbout.ContributorsItem(
            Contributors.WEIGUANGTWK,
            Contributors.WEIGUANGTWK.getName(),
            getString(R.string.weiguangtwk_about),
            R.mipmap.dp_weiguangtwk));
    items.add(
        new CategoryAbout.ContributorsItem(
            Contributors.WINZORT,
            Contributors.WINZORT.getName(),
            getString(R.string.winzort_about),
            R.mipmap.dp_winzort));

    items.add(new CategoryAbout(getString(R.string.app)));
    try {
      PackageInfo pInfo =
          requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
      items.add(
          new CategoryAbout.AppItem(
              Const.ID_VERSION,
              getString(R.string.version),
              pInfo.versionName,
              R.drawable.ic_version_tag));
    } catch (PackageManager.NameNotFoundException ignored) {
    }

    items.add(
        new CategoryAbout.AppItem(
            Const.ID_CHANGELOGS,
            getString(R.string.changelogs),
            getString(R.string.des_changelogs),
            R.drawable.ic_changelog));
    items.add(
        new CategoryAbout.AppItem(
            Const.ID_REPORT,
            getString(R.string.report_issue),
            getString(R.string.des_report_issue),
            R.drawable.ic_report));
    items.add(
        new CategoryAbout.AppItem(
            Const.ID_FEATURE,
            getString(R.string.feature_request),
            getString(R.string.des_feature_request),
            R.drawable.ic_feature));
    items.add(
        new CategoryAbout.AppItem(
            Const.ID_GITHUB,
            getString(R.string.github),
            getString(R.string.des_github),
            R.drawable.ic_github));
    items.add(
        new CategoryAbout.AppItem(
            Const.ID_TELEGRAM,
            getString(R.string.telegram_channel),
            getString(R.string.des_telegram_channel),
            R.drawable.ic_telegram));
    items.add(
        new CategoryAbout.AppItem(
            Const.ID_LICENSE,
            getString(R.string.license),
            getString(R.string.des_license),
            R.drawable.ic_license));

    return items;
  }

  @Override
  public void onPause() {
    super.onPause();
    saveRecyclerViewState();
    viewModel.setToolbarExpanded(Utils.isToolbarExpanded(binding.appBarLayout));
  }

  @Override
  public void onResume() {
    super.onResume();
    restoreRecyclerViewState();
    binding.appBarLayout.setExpanded(viewModel.isToolbarExpanded());
  }

  private void saveRecyclerViewState() {
    LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rvAbout.getLayoutManager();
    if (layoutManager != null) {
      int currentPosition = layoutManager.findLastVisibleItemPosition();
      View currentView = layoutManager.findViewByPosition(currentPosition);
      if (currentView != null) {
        mRVPositionAndOffset = new Pair<>(currentPosition, currentView.getTop());
        viewModel.setRVPositionAndOffset(mRVPositionAndOffset);
      }
    }
  }

  private void restoreRecyclerViewState() {
    mRVPositionAndOffset = viewModel.getRVPositionAndOffset();
    if (mRVPositionAndOffset != null) {
      LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rvAbout.getLayoutManager();
      if (layoutManager != null) {
        layoutManager.scrollToPositionWithOffset(
            mRVPositionAndOffset.first, mRVPositionAndOffset.second);
      }
    }
  }

  @Override
  public void onCheckUpdate(Button button, LottieAnimationView animation) {
    this.loadingDots = animation;
    if (button != null) {
      updateButtonIcon = button.getCompoundDrawables()[0]; // Save the original icon
      button.setText(null);
      // casting button to MaterialButton to use setIcon method.
      ((MaterialButton) button).setIcon(null);
    }

    loadingDots.setVisibility(View.VISIBLE);
    new FetchLatestVersionCode(getContext(), this).execute(Const.URL_BUILD_GRADLE);
  }

  @Override
  public void onResult(int result) {
    // Restore the original icon and text
    if (getView() != null) {
      loadingDots.setVisibility(View.GONE);
      Button button = getView().findViewById(R.id.check_update_button);
      button.setText(R.string.update);
      // casting button to MaterialButton to use setIcon method.
      ((MaterialButton) button).setIcon(updateButtonIcon);
    }

    if (getContext() != null) {
      switch (result) {
        case Const.UPDATE_AVAILABLE:
          BottomSheets.showBottomSheetUpdate(requireActivity(), getContext());
          return;
        case Const.UPDATE_NOT_AVAILABLE:
          latestVersionDialog(getContext());
          break;
        case Const.CONNECTION_ERROR:
          ToastUtils.showToast(getContext(), R.string.check_internet, ToastUtils.LENGTH_SHORT);
          break;
        default:
          return;
      }
    }
  }

  private void latestVersionDialog(Context context) {
    if (isAdded()) {
      View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_latest_version, null);

      new MaterialAlertDialogBuilder(context).setView(dialogView).show();
    }
  }
}
