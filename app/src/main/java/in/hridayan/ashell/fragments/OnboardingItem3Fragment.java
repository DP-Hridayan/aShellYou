package in.hridayan.ashell.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import in.hridayan.ashell.R;
import in.hridayan.ashell.databinding.FragmentOnboardingItem3Binding;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.RootShell;
import in.hridayan.ashell.utils.ShizukuShell;
import in.hridayan.ashell.utils.ToastUtils;
import in.hridayan.ashell.utils.Utils;
import rikka.shizuku.Shizuku;

public class OnboardingItem3Fragment extends Fragment implements ShizukuShell.ShizukuPermCallback {

  private static FragmentOnboardingItem3Binding binding;
  private ShizukuShell shizukuShell;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentOnboardingItem3Binding.inflate(inflater, container, false);

    shizukuShell = new ShizukuShell(requireContext(), this);

    binding.root.setOnClickListener(
        v -> {
          permGrantedToast();
          // requestRootPermission();
          binding.shizuku.setSelected(false);
        });

    binding.shizuku.setOnClickListener(
        v -> {
          // start permission check
          shizukuShell.startPermissionCheck();
          // request shizuku permission
          requestShizukuPermission();

          binding.root.setSelected(false);
        });

    return binding.getRoot();
  }

  // request root permission
  private void requestRootPermission() {
    RootShell.exec("su", true);
    RootShell.refresh();
    if (!RootShell.hasPermission()) {
      // if permission not granted , then ask the user to manually grant root permission
      // unselect the root permission widget
      binding.root.setSelected(false);
    } else {
      // Set root mode as preferred mode for running local adb commands
      Preferences.setLocalAdbMode(requireContext(), Preferences.ROOT_MODE);
      permGrantedToast();
    }
  }

  // request shizuku permission
  private void requestShizukuPermission() {
    // Shizuku is not installed or running
    if (!Shizuku.pingBinder()) handleShizukuUnavailability();
    // Shizuku is running but havenot granted permission to aShellYou
    else if (!ShizukuShell.hasPermission())
      Utils.shizukuPermRequestDialog(requireActivity(), requireContext());
  }

  // this block executes immediately after permission is granted
  @Override
  public void onShizukuPermGranted() {
    requireActivity()
        .runOnUiThread(
            () -> {
              permGrantedToast();
              // set default local adb mode to shizuku
              Preferences.setLocalAdbMode(requireContext(), Preferences.SHIZUKU_MODE);
            });
  }

  private void handleShizukuUnavailability() {
    binding.shizuku.setSelected(false);
    // Show dialog that shizuku is unavailable
    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.warning))
        .setMessage(getString(R.string.shizuku_unavailable_message))
        .setNegativeButton(
            getString(R.string.shizuku_about),
            (dialogInterface, i) -> Utils.openUrl(requireContext(), "https://shizuku.rikka.app/"))
        .setPositiveButton(getString(R.string.ok), null)
        .show();
  }

  private void permGrantedToast() {
    ToastUtils.showToast(requireContext(), "Granted", ToastUtils.LENGTH_SHORT);
  }

  public static boolean isRootSelected() {
    return binding.root.isSelected();
  }

  public boolean isShizukuSelected() {
    return binding.shizuku.isSelected();
  }
}
