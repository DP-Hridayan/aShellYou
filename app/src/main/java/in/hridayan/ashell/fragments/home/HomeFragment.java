package in.hridayan.ashell.fragments.home;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.Hold;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.ToastUtils;
import in.hridayan.ashell.UI.bottomsheets.WifiAdbBottomSheet;
import in.hridayan.ashell.UI.dialogs.ErrorDialogs;
import in.hridayan.ashell.UI.dialogs.PermissionDialogs;
import in.hridayan.ashell.activities.MainActivity;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.databinding.FragmentHomeBinding;
import in.hridayan.ashell.fragments.PairingFragment;
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

    instructionsButtonWifiAdb();

    pairButtonOnClickListener();

    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    restoreScrollViewPosition();
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

  private void goToPairingFragment() {
    PairingFragment fragment = new PairingFragment();

    // Reset previous exit transition to avoid conflicts
    setExitTransition(null);

    requireActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .setCustomAnimations(
            R.anim.fragment_enter,
            R.anim.fragment_exit,
            R.anim.fragment_pop_enter,
            R.anim.fragment_pop_exit)
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
    if (!Shizuku.pingBinder()) ErrorDialogs.shizukuUnavailableDialog(requireContext());
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

  private void instructionsButtonWifiAdb() {
    binding.instructionWireless.setOnClickListener(
        v -> {
          wifiAdbInstructions();
        });
  }

  private void wifiAdbInstructions() {}

  private void pairButtonOnClickListener() {
    binding.pairWirelessDebugging.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);

          chooseWifiAdbModeDialog();
        });
  }

  private void chooseWifiAdbModeDialog() {
    View dialogView = inflateDialogView(context, R.layout.dialog_wifi_adb_mode);
    AlertDialog dialog = createDialog(context, dialogView);

    MaterialCardView thisDeviceCard = dialogView.findViewById(R.id.modeThisDevice);
    MaterialCardView otherDeviceCard = dialogView.findViewById(R.id.modeOtherDevice);

    thisDeviceCard.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          pairThisDevice();
          goToPairingFragment();
          dialog.dismiss();
        });

    otherDeviceCard.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          pairOtherDevice();
          dialog.dismiss();
        });
  }

  private void pairOtherDevice() {
    WifiAdbBottomSheet.showPairAndConnectBottomSheet(context, requireActivity());
  }

  private void pairThisDevice() {
    // We start the pairing from the activity to avoid destroying when fragment destroys
    ((MainActivity) requireActivity()).pairThisDevice();
  }

  private void restoreScrollViewPosition() {
    viewModel
        .getScrollY()
        .observe(
            getViewLifecycleOwner(),
            y -> {
              if (binding != null && binding.scrollView != null && y != null) {
                binding.scrollView.post(() -> binding.scrollView.scrollTo(0, y));
              }
            });
  }

  private void saveScrollViewPosition() {
    viewModel.setScrollY(binding.scrollView.getScrollY());
  }

  private View inflateDialogView(Context context, int layoutRes) {
    return LayoutInflater.from(context).inflate(layoutRes, null);
  }

  private AlertDialog createDialog(Context context, View view) {
    return new MaterialAlertDialogBuilder(context).setView(view).show();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
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
