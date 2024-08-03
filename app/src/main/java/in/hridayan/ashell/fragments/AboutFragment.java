package in.hridayan.ashell.fragments;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.Category;
import in.hridayan.ashell.adapters.AboutAdapter;
import in.hridayan.ashell.databinding.FragmentAboutBinding;
import in.hridayan.ashell.utils.FetchLatestVersionCode;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.ToastUtils;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.utils.Utils.FetchLatestVersionCodeCallback;
import in.hridayan.ashell.viewmodels.AboutViewModel;
import java.util.ArrayList;
import java.util.List;

public class AboutFragment extends Fragment
    implements AboutAdapter.AdapterListener, FetchLatestVersionCodeCallback {
  private AboutAdapter adapter;
  private List<Object> items;
  private AboutViewModel viewModel;
  private Context context;
  private FragmentAboutBinding binding;
  private View view;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    binding = FragmentAboutBinding.inflate(inflater, container, false);

    view = binding.getRoot();

    context = requireContext();

    binding.rvAbout.setLayoutManager(new LinearLayoutManager(getContext()));
    items = new ArrayList<>();

    viewModel = new ViewModelProvider(requireActivity()).get(AboutViewModel.class);

    binding.arrowBack.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, getContext());
          requireActivity().getSupportFragmentManager().popBackStack();
        });

    initializeItems();
    adapter = new AboutAdapter(items, context, requireActivity());
    adapter.setAdapterListener(this);
    binding.rvAbout.setAdapter(adapter);

    return view;
  }

  private void initializeItems() {
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
      String version = pInfo.versionName;
      items.add(
          new Category.AppItem(
              "id_version", getString(R.string.version), version, R.drawable.ic_version_tag));
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

    /* No currently running discord server , uncomment this if want to add discord server link
    items.add(new Category.AppItem("id_discord", getString(R.string.discord), getString(R.string.des_discord), R.drawable.ic_discord)); */
  }

  @Override
  public void onCheckUpdate() {
    new FetchLatestVersionCode(context, this).execute(Preferences.buildGradleUrl);
  }

  @Override
  public void onResult(int result) {
    switch (result) {
      case Preferences.UPDATE_AVAILABLE:
        Utils.showBottomSheetUpdate(requireActivity(), context);
        break;
      case Preferences.UPDATE_NOT_AVAILABLE:
        ToastUtils.showToast(context, R.string.already_latest_version, ToastUtils.LENGTH_SHORT);
        break;
      case Preferences.CONNECTION_ERROR:
        ToastUtils.showToast(context, R.string.check_internet, ToastUtils.LENGTH_SHORT);
        break;
      default:
        break;
    }
  }
}
