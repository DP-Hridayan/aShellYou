package in.hridayan.ashell.fragments;

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

import java.util.ArrayList;
import java.util.List;

import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.Category;
import in.hridayan.ashell.adapters.AboutAdapter;
import in.hridayan.ashell.databinding.FragmentAboutBinding;
import in.hridayan.ashell.utils.FetchLatestVersionCode;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.ToastUtils;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.AboutViewModel;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;

public class AboutFragment extends Fragment
    implements AboutAdapter.AdapterListener, Utils.FetchLatestVersionCodeCallback {

  private AboutViewModel viewModel;
  private FragmentAboutBinding binding;
  private Pair<Integer, Integer> mRVPositionAndOffset;
    private LottieAnimationView loadingDots;
    private Drawable updateButtonIcon;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    binding = FragmentAboutBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(requireActivity()).get(AboutViewModel.class);

    setupRecyclerView();
    setupListeners();

    return binding.getRoot();
  }

  private void setupRecyclerView() {
    binding.rvAbout.setLayoutManager(new LinearLayoutManager(getContext()));
    AboutAdapter adapter = new AboutAdapter(initializeItems());
    adapter.setAdapterListener(this);
    binding.rvAbout.setAdapter(adapter);
  }

  private void setupListeners() {
    binding.arrowBack.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, getContext());
          requireActivity().getSupportFragmentManager().popBackStack();
        });
  }

  private List<Object> initializeItems() {
    List<Object> items = new ArrayList<>();
    items.add(new Category(getString(R.string.lead_developer)));
    items.add(
        new Category.LeadDeveloperItem(
            "Hridayan", getString(R.string.hridayan_about), R.mipmap.dp_hridayan));

    items.add(new Category(getString(R.string.contributors)));
    items.add(
        new Category.ContributorsItem(
            "id_krishna", "Krishna", getString(R.string.krishna_about), R.mipmap.dp_krishna));
    items.add(
        new Category.ContributorsItem(
            "id_shivam", "Stɑrry Shivɑm", getString(R.string.shivam_about), R.mipmap.dp_shivam));
    items.add(
        new Category.ContributorsItem(
            "id_drDisagree",
            "DrDisagree",
            getString(R.string.drDisagree_about),
            R.mipmap.dp_drdisagree));
    items.add(
        new Category.ContributorsItem(
            "id_rikka", "RikkaApps", getString(R.string.rikka_about), R.mipmap.dp_shizuku));
    items.add(
        new Category.ContributorsItem(
            "id_sunilpaulmathew",
            "Sunilpaulmathew",
            getString(R.string.sunilpaulmathew_about),
            R.mipmap.dp_sunilpaulmathew));
    items.add(
        new Category.ContributorsItem(
            "id_khun_htetz",
            "Khun Htetz Naing",
            getString(R.string.khun_htetz_about),
            R.mipmap.dp_adb_otg));
    items.add(
        new Category.ContributorsItem(
            "id_marciozomb13",
            "marciozomb13",
            getString(R.string.marciozomb13_about),
            R.mipmap.dp_marciozomb13));
    items.add(
        new Category.ContributorsItem(
            "id_weiguangtwk",
            "weiguangtwk",
            getString(R.string.weiguangtwk_about),
            R.mipmap.dp_weiguangtwk));
    items.add(
        new Category.ContributorsItem(
            "id_winzort", "WINZORT", getString(R.string.winzort_about), R.mipmap.dp_winzort));

    items.add(new Category(getString(R.string.app)));
    try {
      PackageInfo pInfo =
          requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
      items.add(
          new Category.AppItem(
              "id_version",
              getString(R.string.version),
              pInfo.versionName,
              R.drawable.ic_version_tag));
    } catch (PackageManager.NameNotFoundException ignored) {
    }

    items.add(
        new Category.AppItem(
            "id_changelogs",
            getString(R.string.changelogs),
            getString(R.string.des_changelogs),
            R.drawable.ic_changelog));
    items.add(
        new Category.AppItem(
            "id_report",
            getString(R.string.report_issue),
            getString(R.string.des_report_issue),
            R.drawable.ic_report));
    items.add(
        new Category.AppItem(
            "id_feature",
            getString(R.string.feature_request),
            getString(R.string.des_feature_request),
            R.drawable.ic_feature));
    items.add(
        new Category.AppItem(
            "id_github",
            getString(R.string.github),
            getString(R.string.des_github),
            R.drawable.ic_github));
    items.add(
        new Category.AppItem(
            "id_telegram",
            getString(R.string.telegram_channel),
            getString(R.string.des_telegram_channel),
            R.drawable.ic_telegram));
    items.add(
        new Category.AppItem(
            "id_license",
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
    updateButtonIcon = button.getCompoundDrawables()[0]; // Save the original icon
    button.setText(null);
    // casting button to MaterialButton to use setIcon method.
    ((MaterialButton) button).setIcon(null);
    loadingDots.setVisibility(View.VISIBLE);
    new FetchLatestVersionCode(getContext(), this).execute(Preferences.buildGradleUrl);
  }

  @Override
  public void onResult(int result) {
    // Restore the original icon and text
    loadingDots.setVisibility(View.GONE);
    Button button = getView().findViewById(R.id.check_update_button);
    button.setText(R.string.update);
    // casting button to MaterialButton to use setIcon method.
    ((MaterialButton) button).setIcon(updateButtonIcon);

    int message;
    switch (result) {
      case Preferences.UPDATE_AVAILABLE:
        Utils.showBottomSheetUpdate(requireActivity(), getContext());
        return;
      case Preferences.UPDATE_NOT_AVAILABLE:
        message = R.string.already_latest_version;
        break;
      case Preferences.CONNECTION_ERROR:
        message = R.string.check_internet;
        break;
      default:
        return;
    }
    ToastUtils.showToast(getContext(), message, ToastUtils.LENGTH_SHORT);
  }
}
