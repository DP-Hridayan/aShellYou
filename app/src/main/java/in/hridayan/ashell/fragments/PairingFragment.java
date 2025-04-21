package in.hridayan.ashell.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.OnBackPressedDispatcher;
import androidx.fragment.app.Fragment;
import in.hridayan.ashell.R;
import in.hridayan.ashell.activities.MainActivity;
import in.hridayan.ashell.databinding.FragmentPairingBinding;
import in.hridayan.ashell.ui.dialogs.ErrorDialogs;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.PermissionUtils;
import in.hridayan.ashell.utils.Utils;

public class PairingFragment extends Fragment {

  private FragmentPairingBinding binding;
  private View view;
  private Context context;

  @Override
  public void onResume() {
    super.onResume();
    handleNotificationAccess();
    handleWifiAccess();
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentPairingBinding.inflate(inflater, container, false);
    view = binding.getRoot();
    context = requireContext();

    handleNotificationAccess();
    handleWifiAccess();
    notificationSettingsButton();
    wifiEnableButton();
    developerOptionsButton();
    pairThisDevice();
    backPressDispatcher();

    return view;
  }

  private void handleNotificationAccess() {
    if (PermissionUtils.hasNotificationPermission(context)) {
      binding.notificationHint.setVisibility(View.VISIBLE);
      binding.notificationAccess.setVisibility(View.GONE);
    } else {
      binding.notificationHint.setVisibility(View.GONE);
      binding.notificationAccess.setVisibility(View.VISIBLE);
    }
  }

  private void notificationSettingsButton() {
    binding.notificationButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          PermissionUtils.openAppNotificationSettings(context);
        });
  }

  private void handleWifiAccess() {
    binding.wifiConnectionRequired.setVisibility(
        Utils.isConnectedToWifi(context) ? View.GONE : View.VISIBLE);
  }

  private void wifiEnableButton() {
    binding.wifiPromptButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          Utils.askUserToEnableWifi(context);
        });
  }

  private void developerOptionsButton() {
    binding.developerOptionsButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          openDeveloperOptions();
        });
  }

  private void openDeveloperOptions() {
    Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    try {
      if (!PermissionUtils.hasNotificationPermission(context))
        ErrorDialogs.grantNotificationPermDialog(context);
      else startActivity(intent);
    } catch (ActivityNotFoundException e) {
      Toast.makeText(
              context, getString(R.string.developer_options_not_available), Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void pairThisDevice() {
    // We start the pairing from the activity to avoid destroying when fragment destroys
    ((MainActivity) requireActivity()).pairThisDevice();
  }

  private void backPressDispatcher() {
    OnBackPressedDispatcher dispatcher = requireActivity().getOnBackPressedDispatcher();

    binding.arrowBack.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          dispatcher.onBackPressed();
        });
  }
}
