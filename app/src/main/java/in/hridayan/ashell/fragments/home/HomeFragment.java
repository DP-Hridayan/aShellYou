package in.hridayan.ashell.fragments.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.transition.Hold;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.ToastUtils;
import in.hridayan.ashell.UI.bottomsheets.WifiAdbBottomSheet;
import in.hridayan.ashell.UI.dialogs.ErrorDialogs;
import in.hridayan.ashell.UI.dialogs.PermissionDialogs;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.databinding.FragmentHomeBinding;
import in.hridayan.ashell.fragments.settings.SettingsFragment;
import in.hridayan.ashell.shell.ShizukuShell;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.HomeViewModel;
import in.hridayan.ashell.viewmodels.MainViewModel;
import in.hridayan.ashell.viewmodels.SettingsViewModel;
import rikka.shizuku.Shizuku;

public class HomeFragment extends Fragment implements ShizukuShell.ShizukuPermCallback {
  private FragmentHomeBinding binding;
  private Context context;
  private SettingsViewModel settingsViewModel;
  private MainViewModel mainViewModel;
  private HomeViewModel viewModel;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentHomeBinding.inflate(inflater, container, false);

    context = requireContext();

    initializeViewModels();

    settingsOnClickListener();

    localAdbCardOnClickListener();

    setupAccessCards();

    otgAdbCardOnClickListener();

    instructionsOtgButtonOnClickListener();

    wirelessAdbCardOnClickListener();

    pairButtonOnClickListener();

    restoreScrollViewPosition();

    return binding.getRoot();
  }

  // initialize viewModels
  private void initializeViewModels() {
    viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
  }

  private void settingsOnClickListener() {
    binding.settings.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);

          goToSettings();
        });
  }

  //  Open the settings fragment
  private void goToSettings() {
    if (settingsViewModel != null) {
      settingsViewModel.setRVPositionAndOffset(null);
      settingsViewModel.setToolbarExpanded(true);
    }

    setExitTransition(new Hold());
    SettingsFragment fragment = new SettingsFragment();

    requireActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .addSharedElement(binding.settings, Const.SETTINGS_TO_SETTINGS)
        .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
        .addToBackStack(fragment.getClass().getSimpleName())
        .commit();
  }

  private void localAdbCardOnClickListener() {
    binding.localAdbCard.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          goToAshellFragment();
        });
  }

  private void otgAdbCardOnClickListener() {
    binding.otgAdbCard.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          goToOtgFragment();
        });
  }

  private void wirelessAdbCardOnClickListener() {
    binding.wirelessAdbCard.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          goToWifiAdbFragment();
        });
  }

  private void goToAshellFragment() {
    setExitTransition(new Hold());
    AshellFragment fragment = new AshellFragment();

    requireActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .addSharedElement(binding.localAdbCard, Const.FRAGMENT_LOCAL_SHELL)
        .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
        .addToBackStack(fragment.getClass().getSimpleName())
        .commit();
  }

  private void goToOtgFragment() {
    setExitTransition(new Hold());
    OtgFragment fragment = new OtgFragment();

    requireActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .addSharedElement(binding.otgAdbCard, Const.FRAGMENT_OTG_SHELL)
        .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
        .addToBackStack(fragment.getClass().getSimpleName())
        .commit();
  }

  private void instructionsOtgButtonOnClickListener() {
    binding.instructionOtg.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          Utils.openUrl(context, Const.URL_OTG_INSTRUCTIONS);
        });
  }

  private void goToWifiAdbFragment() {
    setExitTransition(new Hold());
    WifiAdbFragment fragment = new WifiAdbFragment();

    requireActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .addSharedElement(binding.wirelessAdbCard, Const.FRAGMENT_WIFI_ADB_SHELL)
        .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
        .addToBackStack(fragment.getClass().getSimpleName())
        .commit();
  }

  private void setupAccessCards() {
    shizukuAccessCard();
    // rootAccessCard();
  }

  private void shizukuAccessCard() {
    String accessStatus = getString(R.string.shizuku_access) + ": " + getString(R.string.none);
    String shizukuVersion = getString(R.string.version) + ": " + getString(R.string.none);
    Drawable shizukuIcon = Utils.getDrawable(R.drawable.ic_error, context);

    if (Shizuku.pingBinder() && ShizukuShell.hasPermission()) {
      accessStatus = getString(R.string.shizuku_access) + ": " + getString(R.string.granted);
      shizukuVersion = getString(R.string.version) + ": " + Double.toString(Shizuku.getVersion());
      shizukuIcon = Utils.getDrawable(R.drawable.ic_shizuku, context);
    }

    binding.shizukuIcon.setImageDrawable(shizukuIcon);
    binding.shizukuAccessText.setText(accessStatus);
    binding.shizukuVersionText.setText(shizukuVersion);
    binding.shizukuAccessCard.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          ShizukuShell shizukuShell = new ShizukuShell(context, this);
          requestShizukuPermission();
        });
  }

  private void requestShizukuPermission() {
    // Shizuku is not installed or running
    if (!Shizuku.pingBinder()) ErrorDialogs.shizukuUnavailableDialog(requireContext());
    // Shizuku is running but havenot granted permission to aShellYou
    else if (!ShizukuShell.hasPermission()) PermissionDialogs.shizukuPermissionDialog(context);
  }

  @Override
  public void onShizukuPermGranted() {
    requireActivity()
        .runOnUiThread(
            () -> {
              shizukuAccessCard();
              permGrantedToast();
              // set default local adb mode to shizuku
              Preferences.setLocalAdbMode(Const.SHIZUKU_MODE);
            });
  }

  private void permGrantedToast() {
    ToastUtils.showToast(requireContext(), getString(R.string.granted), ToastUtils.LENGTH_SHORT);
  }

  private void rootAccessCard() {}

  private void pairButtonOnClickListener() {
    binding.pairWirelessDebugging.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          WifiAdbBottomSheet.showPairAndConnectBottomSheet(context, requireActivity());
        });
  }

  private void restoreScrollViewPosition() {
    viewModel
        .getScrollY()
        .observe(
            getViewLifecycleOwner(),
            y -> {
              if (y != null) {
                binding.scrollView.post(() -> binding.scrollView.scrollTo(0, y));
              }
            });
  }

  private void saveScrollViewPosition() {
    viewModel.setScrollY(binding.scrollView.getScrollY());
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onResume() {
    super.onResume();
    setupAccessCards();
    restoreScrollViewPosition();
  }

  @Override
  public void onPause() {
    super.onPause();
    saveScrollViewPosition();
  }
}
