package `in`.hridayan.ashell.fragments.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import `in`.hridayan.ashell.config.Const
import `in`.hridayan.ashell.config.Preferences
import `in`.hridayan.ashell.databinding.SettingsLookAndFeelBinding
import `in`.hridayan.ashell.ui.ThemeUtils
import `in`.hridayan.ashell.utils.DeviceUtils
import `in`.hridayan.ashell.utils.HapticUtils
import `in`.hridayan.ashell.utils.MiuiCheck
import `in`.hridayan.ashell.utils.Utils
import `in`.hridayan.ashell.viewmodels.SettingsItemViewModel

class LookAndFeel : Fragment() {
    private lateinit var binding: SettingsLookAndFeelBinding
    private lateinit var view: View
    private val viewModel: SettingsItemViewModel by viewModels()

    override fun onPause() {
        super.onPause()

        viewModel.isToolbarExpanded = Utils.isToolbarExpanded(binding.appBarLayout)

        val scrollX = binding.nestedScrollView.scrollX
        val scrollY = binding.nestedScrollView.scrollY
        val scrollPosition = Pair(scrollX, scrollY)
        viewModel.scrollPosition = scrollPosition
    }

    override fun onResume() {
        super.onResume()

        binding.appBarLayout.setExpanded(viewModel.isToolbarExpanded)

        val savedScrollPosition: Pair<Int, Int> = viewModel.scrollPosition ?: Pair(0, 0)
        binding
            .nestedScrollView
            .viewTreeObserver
            .addOnGlobalLayoutListener {
                binding.nestedScrollView.scrollTo(
                    savedScrollPosition.first, savedScrollPosition.second
                )
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = SettingsLookAndFeelBinding.inflate(inflater, container, false)
        view = binding.root

        onBackPressedDispatcher()
        setupThemeOptions()
        setupAmoledSwitch()
        setupDynamicColorsSwitch()
        setupHapticAndVibration()
        setupDefaultLanguageOnClick()

        return view
    }

    private fun onBackPressedDispatcher() {
        val dispatcher: OnBackPressedDispatcher = requireActivity().onBackPressedDispatcher

        binding.arrowBack.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            dispatcher.onBackPressed()
        }
    }

    // Setting up the theme options
    private fun setupThemeOptions() {
        setRadioButtonState(binding.system, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        setRadioButtonState(binding.on, AppCompatDelegate.MODE_NIGHT_YES)
        setRadioButtonState(binding.off, AppCompatDelegate.MODE_NIGHT_NO)

        // Handle click events for alternative views (dark versions)
        binding.darkSystem.setOnClickListener { binding.system.performClick() }
        binding.darkOn.setOnClickListener { binding.on.performClick() }
        binding.darkOff.setOnClickListener { binding.off.performClick() }
    }

    // Setting up the amoled switch
    private fun setupAmoledSwitch() {
        binding.switchHighContrastDarkTheme.isChecked = Preferences.getAmoledTheme()
        binding.switchHighContrastDarkTheme.setOnCheckedChangeListener { view: CompoundButton?, isChecked: Boolean ->
            HapticUtils.weakVibrate(view)
            saveSwitchState(Const.PREF_AMOLED_THEME, isChecked)
            if (ThemeUtils.isNightMode(requireContext())) {
                Preferences.setActivityRecreated(true)
                requireActivity().recreate()
            }
        }
        binding.highContrastDarkTheme.setOnClickListener { binding.switchHighContrastDarkTheme.performClick() }
    }

    // Setting up the dynamic color switch
    private fun setupDynamicColorsSwitch() {
        binding.dynamicColors.visibility =
            if (DeviceUtils.androidVersion() >= Build.VERSION_CODES.S) View.VISIBLE else View.GONE
        binding.switchDynamicColors.isChecked = Preferences.getDynamicColors()
        binding.switchDynamicColors.setOnCheckedChangeListener { view: CompoundButton?, isChecked: Boolean ->
            HapticUtils.weakVibrate(view)
            saveSwitchState(Const.PREF_DYNAMIC_COLORS, isChecked)
            Preferences.setActivityRecreated(true)
            requireActivity().recreate()
        }
        binding.dynamicColors.setOnClickListener { binding.switchDynamicColors.performClick() }
    }

    // Setting up the haptic and vibration switch
    private fun setupHapticAndVibration() {
        binding.switchHapticAndVibration.isChecked = Preferences.getHapticsAndVibration()
        binding.switchHapticAndVibration.setOnCheckedChangeListener { view: CompoundButton?, isChecked: Boolean ->
            HapticUtils.weakVibrate(view)
            saveSwitchState(Const.PREF_HAPTICS_AND_VIBRATION, isChecked)
        }
        binding.hapticAndVibration.setOnClickListener { binding.switchHapticAndVibration.performClick() }
    }

    private fun setupDefaultLanguageOnClick() {
        // App locale setting is only available on Android 13+
        // Also, it's not functional on MIUI devices even on Android 13,
        // Thanks to Xiaomi's broken implementation of standard Android APIs.
        // See: https://github.com/Pool-Of-Tears/GreenStash/issues/130 for more information.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !MiuiCheck.isMiui) binding.defaultLanguage.visibility =
            View.VISIBLE

        binding.defaultLanguage.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val intent =
                    Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                intent.setData(("package:" + requireContext().packageName).toUri())
                requireContext().startActivity(intent)
            }
        }
    }

    // Method to handle RadioButton states and clicks
    private fun setRadioButtonState(button: RadioButton, mode: Int) {
        button.isChecked = Preferences.getThemeMode() == mode
        button.setOnClickListener { v: View? ->
            if (Preferences.getThemeMode() != mode) {
                HapticUtils.weakVibrate(v)
                handleRadioButtonSelection(button, mode)
            }
        }
    }

    private fun handleRadioButtonSelection(button: RadioButton, mode: Int) {
        clearRadioButtons() // Uncheck all radio buttons
        button.isChecked = true
        Preferences.setActivityRecreated(true)
        Preferences.setThemeMode(mode)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    // Uncheck all radio buttons
    private fun clearRadioButtons() {
        binding.system.isChecked = false
        binding.on.isChecked = false
        binding.off.isChecked = false
    }

    private fun saveSwitchState(prefId: String, isChecked: Boolean) {
        Preferences.prefs.edit() {
            putBoolean(prefId, isChecked)
        }
    }
}
