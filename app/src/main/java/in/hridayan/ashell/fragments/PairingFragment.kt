package `in`.hridayan.ashell.fragments

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.activities.MainActivity
import `in`.hridayan.ashell.databinding.FragmentPairingBinding
import `in`.hridayan.ashell.utils.HapticUtils
import `in`.hridayan.ashell.utils.PermissionUtils
import `in`.hridayan.ashell.utils.Utils

class PairingFragment : Fragment() {
    private lateinit var binding: FragmentPairingBinding
    private lateinit var view: View
    private lateinit var context: Context

    override fun onResume() {
        super.onResume()
        handleNotificationAccess()
        handleWifiAccess()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPairingBinding.inflate(inflater, container, false)
        view = binding.root
        context = requireContext()

        handleNotificationAccess()
        handleWifiAccess()
        notificationSettingsButton()
        wifiEnableButton()
        developerOptionsButton()
        pairThisDevice()
        backPressDispatcher()

        return view
    }

    private fun handleNotificationAccess() {
        if (PermissionUtils.hasNotificationPermission(context)) {
            binding.notificationHint.visibility = View.VISIBLE
            binding.notificationAccess.visibility = View.GONE
        } else {
            binding.notificationHint.visibility = View.GONE
            binding.notificationAccess.visibility = View.VISIBLE
        }
    }

    private fun notificationSettingsButton() {
        binding.notificationButton.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            PermissionUtils.openAppNotificationSettings(context)
        }
    }

    private fun handleWifiAccess() {
        binding.wifiConnectionRequired.visibility =
            if (Utils.isConnectedToWifi(context)) View.GONE else View.VISIBLE
    }

    private fun wifiEnableButton() {
        binding.wifiPromptButton.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            Utils.askUserToEnableWifi(context)
        }
    }

    private fun developerOptionsButton() {
        binding.developerOptionsButton.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            openDeveloperOptions()
        }
    }

    private fun openDeveloperOptions() {
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context, getString(R.string.developer_options_not_available), Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun pairThisDevice() {
        // We start the pairing from the activity to avoid destroying when fragment destroys
        (requireActivity() as MainActivity).pairThisDevice()
    }

    private fun backPressDispatcher() {
        val dispatcher: OnBackPressedDispatcher = requireActivity().onBackPressedDispatcher

        binding.arrowBack.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            dispatcher.onBackPressed()
        }
    }
}
