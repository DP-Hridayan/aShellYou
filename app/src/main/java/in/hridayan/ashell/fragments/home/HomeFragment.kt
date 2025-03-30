package `in`.hridayan.ashell.fragments.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.transition.Hold
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.adapters.WifiAdbDevicesAdapter
import `in`.hridayan.ashell.config.Const
import `in`.hridayan.ashell.config.Preferences
import `in`.hridayan.ashell.databinding.FragmentHomeBinding
import `in`.hridayan.ashell.fragments.settings.SettingsFragment
import `in`.hridayan.ashell.items.WifiAdbDevicesItem
import `in`.hridayan.ashell.shell.localadb.RootShell
import `in`.hridayan.ashell.shell.localadb.RootShell.RootPermCallback
import `in`.hridayan.ashell.shell.localadb.ShizukuShell
import `in`.hridayan.ashell.shell.localadb.ShizukuShell.ShizukuPermCallback
import `in`.hridayan.ashell.shell.wifiadb.WifiAdbConnectedDevices
import `in`.hridayan.ashell.ui.dialogs.ActionDialogs
import `in`.hridayan.ashell.ui.dialogs.ErrorDialogs
import `in`.hridayan.ashell.ui.dialogs.PermissionDialogs
import `in`.hridayan.ashell.utils.HapticUtils
import `in`.hridayan.ashell.utils.Utils
import `in`.hridayan.ashell.viewmodels.HomeViewModel
import `in`.hridayan.ashell.viewmodels.MainViewModel
import `in`.hridayan.ashell.viewmodels.SettingsViewModel
import rikka.shizuku.Shizuku
import java.util.concurrent.Executors

class HomeFragment : Fragment(), ShizukuPermCallback, RootPermCallback {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var rootShell: RootShell
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        exitTransition = null

        rootShell = RootShell(requireContext(), this)

        settingsOnClickListener()

        localAdbCardOnClickListener()

        setupAccessCards()

        otgAdbCardOnClickListener()

        instructionsOtgButtonOnClickListener()

        wirelessAdbCardOnClickListener()

        instructionsButtonWifiAdb()

        startButtonOnClickListener()

        fetchAndUpdateDeviceList()

        interceptOnBackPress()

        return binding.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreScrollViewPosition()
    }

    private fun settingsOnClickListener() {
        binding.settings.setOnClickListener(
            View.OnClickListener { v: View? ->
                HapticUtils.weakVibrate(v)
                goToSettings()
            })
    }

    //  Open the settings fragment
    private fun goToSettings() {
        if (true) {
            settingsViewModel.rvPositionAndOffset = null
            settingsViewModel.isToolbarExpanded = true
        }

        exitTransition = Hold()
        val fragment = SettingsFragment()

        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .addSharedElement(binding.settings, Const.SETTINGS_TO_SETTINGS)
            .replace(R.id.fragment_container, fragment, fragment.javaClass.getSimpleName())
            .addToBackStack(fragment.javaClass.getSimpleName())
            .commit()
    }

    private fun localAdbCardOnClickListener() {
        binding.localAdbCard.setOnClickListener(
            View.OnClickListener { v: View? ->
                HapticUtils.weakVibrate(v)
                goToAshellFragment()
            })
    }

    private fun goToAshellFragment() {
        exitTransition = Hold()
        val fragment = AshellFragment()

        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .addSharedElement(binding.localAdbCard, Const.FRAGMENT_LOCAL_SHELL)
            .replace(R.id.fragment_container, fragment, fragment.javaClass.getSimpleName())
            .addToBackStack(fragment.javaClass.getSimpleName())
            .commit()
    }

    private fun setupAccessCards() {
        shizukuAccessCard()
        rootAccessCard()
    }

    private fun shizukuAccessCard() {
        var accessStatus = getString(R.string.shizuku_access) + ": " + getString(R.string.denied)
        var shizukuVersion = getString(R.string.version) + ": " + getString(R.string.none)
        var shizukuIcon = Utils.getDrawable(R.drawable.ic_error, requireContext())

        if (Shizuku.pingBinder() && ShizukuShell.hasPermission()) {
            accessStatus = getString(R.string.shizuku_access) + ": " + getString(R.string.granted)
            shizukuVersion =
                getString(R.string.version) + ": " + Shizuku.getVersion().toDouble().toString()
            shizukuIcon = Utils.getDrawable(R.drawable.ic_shizuku, requireContext())
        }

        binding.shizukuIcon.setImageDrawable(shizukuIcon)
        binding.shizukuAccessText.text = accessStatus
        binding.shizukuVersionText.text = shizukuVersion
        binding.shizukuAccessCard.setOnClickListener(
            View.OnClickListener { v: View? ->
                HapticUtils.weakVibrate(v)
                ShizukuShell(context, this)
                requestShizukuPermission()
            })
    }

    private fun requestShizukuPermission() {
        if (!Shizuku.pingBinder()) ErrorDialogs.shizukuUnavailableDialog(requireContext())
        else if (!ShizukuShell.hasPermission()) PermissionDialogs.shizukuPermissionDialog(context)
    }

    override fun onShizukuPermGranted() {
        requireActivity()
            .runOnUiThread(
                Runnable {
                    shizukuAccessCard()
                    // set default local adb mode to shizuku
                    Preferences.setLocalAdbMode(Const.SHIZUKU_MODE)
                })
    }

    private fun rootAccessCard() {
        updateRootStatus()

        binding.rootAccessCard.setOnClickListener(
            View.OnClickListener { v: View? ->
                HapticUtils.weakVibrate(v)
                requestRootPermission()
            })
    }

    // Update root access status
    @SuppressLint("SetTextI18n")
    private fun updateRootStatus() {
        val isRooted = viewModel.isDeviceRooted
        binding.rootAccessText.setText(
            (getString(R.string.root_access)
                    + ": "
                    + getString(if (isRooted) R.string.granted else R.string.denied))
        )
        binding.rootProviderText.setText(
            (getString(R.string.root_provider)
                    + ": "
                    + (if (isRooted) RootShell.getRootProvider() else getString(R.string.none)))
        )
        binding.rootVersionText.setText(
            (getString(R.string.version)
                    + ": "
                    + (if (isRooted) RootShell.getRootVersion() else getString(R.string.none)))
        )
        binding.rootIcon.setImageDrawable(
            Utils.getDrawable(
                if (isRooted) R.drawable.magisk_logo else R.drawable.ic_error,
                requireContext()
            )
        )
    }

    // Request root permission
    private fun requestRootPermission() {
        Executors.newSingleThreadExecutor()
            .execute(
                Runnable {
                    if (RootShell.isDeviceRooted()) rootShell.startPermissionCheck()
                })
    }

    // Handle root permission granted
    override fun onRootPermGranted() {
        requireActivity()
            .runOnUiThread(
                Runnable {
                    viewModel.isDeviceRooted = true
                    updateRootStatus()
                    Preferences.setLocalAdbMode(Const.ROOT_MODE)
                })
    }

    private fun otgAdbCardOnClickListener() {
        binding.otgAdbCard.setOnClickListener(
            View.OnClickListener { v: View? ->
                HapticUtils.weakVibrate(v)
                goToOtgFragment()
            })
    }

    private fun goToOtgFragment() {
        exitTransition = Hold()
        val fragment = OtgFragment()

        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .addSharedElement(binding.otgAdbCard, Const.FRAGMENT_OTG_SHELL)
            .replace(R.id.fragment_container, fragment, fragment.javaClass.getSimpleName())
            .addToBackStack(fragment.javaClass.getSimpleName())
            .commit()
    }

    private fun instructionsOtgButtonOnClickListener() {
        binding.instructionOtg.setOnClickListener(
            View.OnClickListener { v: View? ->
                HapticUtils.weakVibrate(v)
                Utils.openUrl(requireContext(), Const.URL_OTG_INSTRUCTIONS)
            })
    }

    private fun wirelessAdbCardOnClickListener() {
        binding.wirelessAdbCard.setOnClickListener(
            View.OnClickListener { v: View? ->
                HapticUtils.weakVibrate(v)
                ActionDialogs.wifiAdbDevicesDialog(
                    requireContext(),
                    requireContext() as AppCompatActivity?,
                    binding.wirelessAdbCard,
                    mainViewModel,
                    this
                )
            })
    }

    private fun instructionsButtonWifiAdb() {
        binding.instructionWireless.setOnClickListener(
            View.OnClickListener { v: View? ->
                HapticUtils.weakVibrate(v)
                wifiAdbInstructions()
            })
    }

    private fun wifiAdbInstructions() {
        Utils.openUrl(requireContext(), Const.URL_WIRELESS_DEBUGGING_INSTRUCTIONS)
    }

    private fun startButtonOnClickListener() {
        binding.startWirelessDebugging.setOnClickListener(
            View.OnClickListener { v: View? ->
                HapticUtils.weakVibrate(v)
                ActionDialogs.wifiAdbDevicesDialog(
                    context,
                    context as AppCompatActivity?,
                    binding.startWirelessDebugging,
                    mainViewModel,
                    this
                )
            })
    }

    private fun fetchAndUpdateDeviceList() {
        val deviceList: MutableList<WifiAdbDevicesItem?> = ArrayList<WifiAdbDevicesItem?>()
        val adapter =
            WifiAdbDevicesAdapter(context, deviceList, mainViewModel)

        // Fetch connected devices
        WifiAdbConnectedDevices.getConnectedDevices(
            context,
            object : WifiAdbConnectedDevices.ConnectedDevicesCallback {
                override fun onDevicesListed(devices: MutableList<String?>) {
                    updateDeviceList(deviceList, adapter, devices)
                }

                override fun onFailure(errorMessage: String?) {}
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateDeviceList(
        deviceList: MutableList<WifiAdbDevicesItem?>,
        adapter: WifiAdbDevicesAdapter,
        devices: MutableList<String?>
    ) {
        deviceList.clear()
        for (ipPort in devices) {
            deviceList.add(WifiAdbDevicesItem(ipPort))
        }
        adapter.notifyDataSetChanged()
    }

    private fun restoreScrollViewPosition() {
        viewModel
            .scrollY
            .observe(
                getViewLifecycleOwner(),
                Observer { y: Int? ->
                    if (y != null) {
                        binding.scrollView.post(Runnable { binding.scrollView.scrollTo(0, y) })
                    }
                })
    }

    private fun saveScrollViewPosition() {
        viewModel.setScrollY(binding.scrollView.scrollY)
    }

    private fun interceptOnBackPress() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                getViewLifecycleOwner(),
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (ActionDialogs.isAnyDialogVisible()) {
                            if (ActionDialogs.isWifiAdbDevicesDialogVisible()) ActionDialogs.dismissDevicesDialog()
                            if (ActionDialogs.isWifiAdbModeDialogVisible()) ActionDialogs.dismissModeDialog()
                        } else {
                            isEnabled = false // Remove this callback
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    }
                })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        setupAccessCards()
        restoreScrollViewPosition()
    }

    override fun onPause() {
        super.onPause()
        saveScrollViewPosition()
    }
}
