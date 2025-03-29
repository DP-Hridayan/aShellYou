package `in`.hridayan.ashell.fragments.setup

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import `in`.hridayan.ashell.config.Const
import `in`.hridayan.ashell.config.Preferences
import `in`.hridayan.ashell.databinding.FragmentOnboardingItem1Binding
import `in`.hridayan.ashell.databinding.FragmentOnboardingItem3Binding
import `in`.hridayan.ashell.shell.localadb.RootShell
import `in`.hridayan.ashell.shell.localadb.RootShell.RootPermCallback
import `in`.hridayan.ashell.shell.localadb.ShizukuShell
import `in`.hridayan.ashell.shell.localadb.ShizukuShell.ShizukuPermCallback
import `in`.hridayan.ashell.ui.ToastUtils
import `in`.hridayan.ashell.ui.dialogs.ErrorDialogs
import `in`.hridayan.ashell.utils.HapticUtils
import rikka.shizuku.Shizuku
import java.util.concurrent.Executors

class OnboardingItem3Fragment : Fragment(), ShizukuPermCallback,
    RootPermCallback {
    private lateinit var shizukuShell: ShizukuShell
    private lateinit var rootShell: RootShell
    private lateinit var binding: FragmentOnboardingItem3Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingItem3Binding.inflate(inflater, container, false)

        shizukuShell = ShizukuShell(requireContext(), this)
        rootShell = RootShell(requireContext(), this)

        binding.root.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            // we donot select the widget unless we get root permission
            binding.root.isSelected = false
            // request root permission
            requestRootPermission()
            binding.shizuku.isSelected = false
        }

        binding.shizuku.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            binding.shizuku.isSelected = false
            // start permission check
            shizukuShell.startPermissionCheck()
            // request shizuku permission
            requestShizukuPermission()
            binding.root.isSelected = false
        }

        return binding.getRoot()
    }

    // request root permission
    private fun requestRootPermission() {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            RootShell.refresh()
            if (!RootShell.isDeviceRooted()) {
                requireActivity().runOnUiThread { this.handleRootUnavailability() }
            } else {
                rootShell.startPermissionCheck() // Starts permission check
            }
        }
        executor.shutdown()
    }

    override fun onRootPermGranted() {
        requireActivity()
            .runOnUiThread {
                binding.root.isSelected = true
                permGrantedToast()
                // set default local adb mode to root
                Preferences.setLocalAdbMode(Const.ROOT_MODE)
            }
    }

    // show this dialog if device is not rooted
    private fun handleRootUnavailability() {
        binding.root.isSelected = false
        ErrorDialogs.rootUnavailableDialog(requireContext())
    }

    // request shizuku permission
    private fun requestShizukuPermission() {
        // Shizuku is not installed or running
        if (!Shizuku.pingBinder()) handleShizukuUnavailability()
        else if (!ShizukuShell.hasPermission()) Shizuku.requestPermission(0)
    }

    // this block executes immediately after permission is granted
    override fun onShizukuPermGranted() {
        requireActivity()
            .runOnUiThread {
                binding.shizuku.isSelected = true
                permGrantedToast()
                // set default local adb mode to shizuku
                Preferences.setLocalAdbMode(Const.SHIZUKU_MODE)
            }
    }

    private fun handleShizukuUnavailability() {
        binding.shizuku.isSelected = false
        // Show dialog that shizuku is unavailable
        ErrorDialogs.shizukuUnavailableDialog(requireContext())
    }

    private fun permGrantedToast() {
        ToastUtils.showToast(requireContext(), "Granted", ToastUtils.LENGTH_SHORT)
    }

    val isShizukuSelected: Boolean
        get() = binding.shizuku.isSelected

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var binding: FragmentOnboardingItem3Binding
        val isRootSelected: Boolean
            get() = binding.root.isSelected
    }
}
