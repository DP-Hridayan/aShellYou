package `in`.hridayan.ashell.fragments.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedDispatcher
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialContainerTransform
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.adapters.SettingsAdapter
import `in`.hridayan.ashell.config.Const
import `in`.hridayan.ashell.config.Preferences
import `in`.hridayan.ashell.databinding.FragmentSettingsBinding
import `in`.hridayan.ashell.items.SettingsItem
import `in`.hridayan.ashell.utils.DocumentTreeUtil
import `in`.hridayan.ashell.utils.HapticUtils
import `in`.hridayan.ashell.utils.Utils
import `in`.hridayan.ashell.viewmodels.AboutViewModel
import `in`.hridayan.ashell.viewmodels.ExamplesViewModel
import `in`.hridayan.ashell.viewmodels.SettingsItemViewModel
import `in`.hridayan.ashell.viewmodels.SettingsViewModel

class SettingsFragment : Fragment() {
    private lateinit var settingsData: MutableList<SettingsItem>
    private val currentTheme = 0
    private val viewModel: SettingsViewModel by viewModels()
    private val aboutViewModel: AboutViewModel by viewModels()
    private val examplesViewModel: ExamplesViewModel by viewModels()
    private val itemViewModel: SettingsItemViewModel by viewModels()
    private lateinit var mRVPositionAndOffset: Pair<Int, Int>
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var view: View

    override fun onPause() {
        super.onPause()
        val layoutManager =
            binding.rvSettings.layoutManager as LinearLayoutManager?

        val currentPosition = layoutManager?.findLastVisibleItemPosition() ?: 0
        val currentView = layoutManager?.findViewByPosition(currentPosition)

        if (currentView != null) {
            mRVPositionAndOffset = Pair(currentPosition, currentView.top)
            viewModel.rvPositionAndOffset = mRVPositionAndOffset
        }
        // Save toolbar state
        viewModel.isToolbarExpanded = Utils.isToolbarExpanded(binding.appBarLayout)
    }

    override fun onResume() {
        super.onResume()

        // we refresh the text in the editText onResume to update the newly set file path
        if (adapter.textViewSaveDir != null && adapter.textViewSaveDir.isVisible
        ) {
            val outputSaveDirectory = Preferences.getSavedOutputDir()
            if (outputSaveDirectory != "") {
                val outputSaveDirPath: String =
                    DocumentTreeUtil.getFullPathFromTreeUri(
                        outputSaveDirectory.toUri(),
                        requireContext()
                    )
                        .toString()
                adapter.textViewSaveDir.text = outputSaveDirPath
            }
        }

        if (binding.rvSettings.layoutManager != null) {
            binding.appBarLayout.setExpanded(viewModel.isToolbarExpanded)

            mRVPositionAndOffset = viewModel.rvPositionAndOffset
            val position: Int = viewModel.rvPositionAndOffset.first ?: 0
            val offset: Int = viewModel.rvPositionAndOffset.second ?: 0

            // Restore recyclerView scroll position
            (binding.rvSettings.layoutManager as LinearLayoutManager)
                .scrollToPositionWithOffset(position, offset)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedElementEnterTransition = MaterialContainerTransform()

        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        view = binding.root

        val dispatcher: OnBackPressedDispatcher = requireActivity().onBackPressedDispatcher

        binding.arrowBack.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            dispatcher.onBackPressed()
        }

        settingsData = ArrayList<SettingsItem>()

        settingsData.add(
            SettingsItem(
                Const.ID_LOOK_AND_FEEL,
                R.drawable.ic_pallete,
                getString(R.string.look_and_feel),
                getString(R.string.des_look_and_feel),
                false,
                false
            )
        )

        settingsData.add(
            SettingsItem(
                Const.PREF_CLEAR,
                R.drawable.ic_clear,
                getString(R.string.ask_to_clean),
                getString(R.string.des_ask_to_clean),
                true,
                Preferences.getClear()
            )
        )

        /*  settingsData.add(
    new SettingsItem(
        Const.PREF_SHARE_AND_RUN,
        R.drawable.ic_share,
        getString(R.string.share_and_run),
        getString(R.string.des_share_and_run),
        true,
        Preferences.getShareAndRun()));*/
        settingsData.add(
            SettingsItem(
                Const.PREF_AUTO_UPDATE_CHECK,
                R.drawable.ic_auto_update,
                getString(R.string.auto_update_check),
                getString(R.string.des_auto_update_check),
                true,
                Preferences.getAutoUpdateCheck()
            )
        )

        settingsData.add(
            SettingsItem(
                Const.ID_CONFIG_SAVE_DIR,
                R.drawable.ic_directory,
                getString(R.string.configure_save_directory),
                getString(R.string.des_configure_save_directory),
                false,
                false
            )
        )

        settingsData.add(
            SettingsItem(
                Const.PREF_LOCAL_ADB_MODE,
                R.drawable.ic_terminal,
                getString(R.string.local_adb_mode),
                getString(R.string.des_local_adb_mode),
                false,
                false
            )
        )

        settingsData.add(
            SettingsItem(
                Const.PREF_DISABLE_SOFTKEY,
                R.drawable.ic_disable_keyboard,
                getString(R.string.disable_softkey),
                getString(R.string.des_disable_softkey),
                true,
                Preferences.getDisableSoftkey()
            )
        )

        settingsData.add(
            SettingsItem(
                Const.PREF_EXAMPLES_LAYOUT_STYLE,
                R.drawable.ic_styles,
                getString(R.string.examples_layout_style),
                getString(R.string.des_examples_layout_style),
                false,
                false
            )
        )

        settingsData.add(
            SettingsItem(
                Const.PREF_OVERRIDE_BOOKMARKS,
                R.drawable.ic_warning,
                getString(R.string.override_bookmarks_limit),
                getString(R.string.des_override_bookmarks),
                true,
                Preferences.getOverrideBookmarks()
            )
        )

        settingsData.add(
            SettingsItem(
                Const.PREF_SAVE_PREFERENCE,
                R.drawable.ic_save_24px,
                getString(R.string.save_preference),
                getString(R.string.des_save_preference),
                false,
                false
            )
        )

        settingsData.add(
            SettingsItem(
                Const.PREF_SMOOTH_SCROLL,
                R.drawable.ic_scroll,
                getString(R.string.smooth_scrolling),
                getString(R.string.des_smooth_scroll),
                true,
                Preferences.getSmoothScroll()
            )
        )

        /*  settingsData.add(
              SettingsItem(
                  Const.ID_EXAMPLES,
                  R.drawable.ic_numbers,
                  getString(R.string.commands),
                  getString(R.string.des_examples),
                  false,
                  false
              )
          )*/

        settingsData.add(
            SettingsItem(
                Const.ID_ABOUT,
                R.drawable.ic_info,
                getString(R.string.about),
                getString(R.string.des_about),
                false,
                false
            )
        )

        adapter =
            SettingsAdapter(
                settingsData,
                context,
                requireActivity(),
                itemViewModel,
                aboutViewModel,
                examplesViewModel
            )
        binding.rvSettings.adapter = adapter
        binding.rvSettings.layoutManager = LinearLayoutManager(context)

        // After recyclerview is drawn, start the transition
        binding.rvSettings.viewTreeObserver.addOnDrawListener { this.startPostponedEnterTransition() }

        // intentional crash with a long message
        ///throwLongException();
        return view

    }

    private fun throwLongException() {
        val longMessage = generateLongMessage()
        throw RuntimeException(longMessage)
    }

    private fun generateLongMessage(): String {
        val message = StringBuilder("Long Exception Message: ")
        for (i in 0..999) { // Adjust the loop count for longer messages
            message.append("This is an intentional crash ").append(i).append(". ")
        }
        return message.toString()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var adapter: SettingsAdapter
    }
}
