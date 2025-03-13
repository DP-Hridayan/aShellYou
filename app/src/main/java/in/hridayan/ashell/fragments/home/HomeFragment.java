package in.hridayan.ashell.fragments.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.transition.Hold;
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.WifiAdbDevicesAdapter;
import in.hridayan.ashell.shell.localadb.RootShell;
import in.hridayan.ashell.shell.wifiadb.WifiAdbConnectedDevices;
import in.hridayan.ashell.ui.dialogs.ActionDialogs;
import in.hridayan.ashell.ui.dialogs.ErrorDialogs;
import in.hridayan.ashell.ui.dialogs.PermissionDialogs;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.databinding.FragmentHomeBinding;
import in.hridayan.ashell.fragments.settings.SettingsFragment;
import in.hridayan.ashell.shell.localadb.ShizukuShell;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.HomeViewModel;
import in.hridayan.ashell.viewmodels.MainViewModel;
import in.hridayan.ashell.viewmodels.SettingsViewModel;
import java.util.List;
import in.hridayan.ashell.items.WifiAdbDevicesItem;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import rikka.shizuku.Shizuku;

public class HomeFragment extends Fragment
    implements ShizukuShell.ShizukuPermCallback, RootShell.RootPermCallback {
  private FragmentHomeBinding binding;
  private Context context;
  private SettingsViewModel settingsViewModel;
  private MainViewModel mainViewModel;
  private HomeViewModel viewModel;
  private RootShell rootShell;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentHomeBinding.inflate(inflater, container, false);

    setExitTransition(null);

    context = requireContext();

    rootShell = new RootShell(requireContext(), this);

    initializeViewModels();

    settingsOnClickListener();

    localAdbCardOnClickListener();

    setupAccessCards();

    otgAdbCardOnClickListener();

    instructionsOtgButtonOnClickListener();

    wirelessAdbCardOnClickListener();

    instructionsButtonWifiAdb();

    startButtonOnClickListener();

    fetchAndUpdateDeviceList();

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

  private void fetchAndUpdateDeviceList() {
    List<WifiAdbDevicesItem> deviceList = new ArrayList<>();
    WifiAdbDevicesAdapter adapter = new WifiAdbDevicesAdapter(context, deviceList, mainViewModel);

    // Fetch connected devices
    WifiAdbConnectedDevices.getConnectedDevices(
        context,
        new WifiAdbConnectedDevices.ConnectedDevicesCallback() {
          @Override
          public void onDevicesListed(@NonNull List<String> devices) {
            updateDeviceList(deviceList, adapter, devices);
          }

          @Override
          public void onFailure(String errorMessage) {}
        });
  }

  private void updateDeviceList(
      List<WifiAdbDevicesItem> deviceList, WifiAdbDevicesAdapter adapter, List<String> devices) {
    deviceList.clear();
    for (String ipPort : devices) {
      deviceList.add(new WifiAdbDevicesItem(ipPort));
    }
    adapter.notifyDataSetChanged();
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
          ActionDialogs.wifiAdbDevicesDialog(
              context, (AppCompatActivity) context, binding.wirelessAdbCard, mainViewModel, this);
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

  private void setupAccessCards() {
    shizukuAccessCard();
    rootAccessCard();
  }

  private void shizukuAccessCard() {
    String accessStatus = getString(R.string.shizuku_access) + ": " + getString(R.string.denied);
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
              // set default local adb mode to shizuku
              Preferences.setLocalAdbMode(Const.SHIZUKU_MODE);
            });
  }

  private void rootAccessCard() {
    updateRootStatus();

    binding.rootAccessCard.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          requestRootPermission();
        });
  }

  // Update root access status
  private void updateRootStatus() {
    boolean isRooted = viewModel.isDeviceRooted();
    binding.rootAccessText.setText(
        getString(R.string.root_access)
            + ": "
            + getString(isRooted ? R.string.granted : R.string.denied));
    binding.rootProviderText.setText(
        getString(R.string.root_provider)
            + ": "
            + (isRooted ? RootShell.getRootProvider() : getString(R.string.none)));
    binding.rootVersionText.setText(
        getString(R.string.version)
            + ": "
            + (isRooted ? RootShell.getRootVersion() : getString(R.string.none)));
    binding.rootIcon.setImageDrawable(
        Utils.getDrawable(isRooted ? R.drawable.magisk_logo : R.drawable.ic_error, context));
  }

  // Request root permission
  private void requestRootPermission() {
    Executors.newSingleThreadExecutor()
        .execute(
            () -> {
              if (RootShell.isDeviceRooted()) rootShell.startPermissionCheck();
            });
  }

  // Handle root permission granted
  @Override
  public void onRootPermGranted() {
    requireActivity()
        .runOnUiThread(
            () -> {
              viewModel.setDeviceRooted(true);
              updateRootStatus();
              Preferences.setLocalAdbMode(Const.ROOT_MODE);
            });
  }

  private void instructionsButtonWifiAdb() {
    binding.instructionWireless.setOnClickListener(
        v -> {
          wifiAdbInstructions();
        });
  }

  private void wifiAdbInstructions() {}

  private void startButtonOnClickListener() {
    binding.startWirelessDebugging.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          ActionDialogs.wifiAdbDevicesDialog(
              context,
              (AppCompatActivity) context,
              binding.startWirelessDebugging,
              mainViewModel,
              this);
        });
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
