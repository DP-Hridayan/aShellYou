package in.hridayan.ashell.fragments.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.DialogUtils;
import in.hridayan.ashell.UI.ToastUtils;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.databinding.FragmentOnboardingItem3Binding;
import in.hridayan.ashell.shell.RootShell;
import in.hridayan.ashell.shell.ShizukuShell;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import rikka.shizuku.Shizuku;

public class OnboardingItem3Fragment extends Fragment
    implements ShizukuShell.ShizukuPermCallback, RootShell.RootPermCallback {
  private static FragmentOnboardingItem3Binding binding;
  private ShizukuShell shizukuShell;
  private RootShell rootShell;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentOnboardingItem3Binding.inflate(inflater, container, false);

    shizukuShell = new ShizukuShell(requireContext(), this);
    rootShell = new RootShell(requireContext(), this);

    binding.root.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          // we donot select the widget unless we get root permission
          binding.root.setSelected(false);
          // request root permission
          requestRootPermission();

          binding.shizuku.setSelected(false);
        });

    binding.shizuku.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);

          binding.shizuku.setSelected(false);
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
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.execute(
        () -> {
          RootShell.refresh();
          if (!RootShell.isDeviceRooted()) {
            requireActivity().runOnUiThread(this::handleRootUnavailability);
          } else {
            rootShell.startPermissionCheck(); // Starts permission check
          }
        });
    executor.shutdown();
  }

  @Override
  public void onRootPermGranted() {
    requireActivity()
        .runOnUiThread(
            () -> {
              binding.root.setSelected(true);
              permGrantedToast();
              // set default local adb mode to root
              Preferences.setLocalAdbMode(Const.ROOT_MODE);
            });
  }

  // show this dialog if device is not rooted
  private void handleRootUnavailability() {
    binding.root.setSelected(false);
    DialogUtils.rootUnavailableDialog(requireContext());
  }

  // request shizuku permission
  private void requestShizukuPermission() {
    // Shizuku is not installed or running
    if (!Shizuku.pingBinder()) handleShizukuUnavailability();
    // Shizuku is running but havenot granted permission to aShellYou
    else if (!ShizukuShell.hasPermission()) Shizuku.requestPermission(0);
  }

  // this block executes immediately after permission is granted
  @Override
  public void onShizukuPermGranted() {
    requireActivity()
        .runOnUiThread(
            () -> {
              binding.shizuku.setSelected(true);
              permGrantedToast();
              // set default local adb mode to shizuku
              Preferences.setLocalAdbMode(Const.SHIZUKU_MODE);
            });
  }

  private void handleShizukuUnavailability() {
    binding.shizuku.setSelected(false);
    // Show dialog that shizuku is unavailable
    DialogUtils.shizukuUnavailableDialog(requireContext());
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
