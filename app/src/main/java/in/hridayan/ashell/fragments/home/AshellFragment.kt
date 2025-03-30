package `in`.hridayan.ashell.fragments.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Pair
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialContainerTransform
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.activities.MainActivity
import `in`.hridayan.ashell.adapters.CommandsAdapter
import `in`.hridayan.ashell.adapters.ShellOutputAdapter
import `in`.hridayan.ashell.config.Const
import `in`.hridayan.ashell.config.Const.Companion.isPackageSensitive
import `in`.hridayan.ashell.config.Preferences
import `in`.hridayan.ashell.databinding.FragmentAshellBinding
import `in`.hridayan.ashell.fragments.ExamplesFragment
import `in`.hridayan.ashell.fragments.settings.SettingsFragment
import `in`.hridayan.ashell.shell.localadb.BasicShell
import `in`.hridayan.ashell.shell.localadb.RootShell
import `in`.hridayan.ashell.shell.localadb.ShizukuShell
import `in`.hridayan.ashell.ui.BehaviorFAB
import `in`.hridayan.ashell.ui.BehaviorFAB.FabLocalScrollDownListener
import `in`.hridayan.ashell.ui.KeyboardUtils
import `in`.hridayan.ashell.ui.ThemeUtils
import `in`.hridayan.ashell.ui.ToastUtils
import `in`.hridayan.ashell.ui.Transitions
import `in`.hridayan.ashell.ui.dialogs.ActionDialogs
import `in`.hridayan.ashell.ui.dialogs.ErrorDialogs
import `in`.hridayan.ashell.ui.dialogs.FeedbackDialogs
import `in`.hridayan.ashell.ui.dialogs.PermissionDialogs
import `in`.hridayan.ashell.utils.Commands
import `in`.hridayan.ashell.utils.DeviceUtils
import `in`.hridayan.ashell.utils.HapticUtils
import `in`.hridayan.ashell.utils.Utils
import `in`.hridayan.ashell.viewmodels.AshellFragmentViewModel
import `in`.hridayan.ashell.viewmodels.ExamplesViewModel
import `in`.hridayan.ashell.viewmodels.MainViewModel
import `in`.hridayan.ashell.viewmodels.SettingsViewModel
import rikka.shizuku.Shizuku
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/*
 * Created by DP-Hridayan <hridayanofficial@gmail.com> on March , 2025
 */
class AshellFragment : Fragment() {
    private lateinit var binding: FragmentAshellBinding
    private lateinit var mCommandAdapter: CommandsAdapter
    private lateinit var mShellOutputAdapter: ShellOutputAdapter
    private lateinit var mShizukuShell: ShizukuShell
    private lateinit var mRootShell: RootShell
    private lateinit var mBasicShell: BasicShell
    private var isKeyboardVisible = false
    private var sendButtonClicked = false
    private var isEndIconVisible = false
    private var mPosition = 1
    private val ic_help = 10
    private val ic_send = 11
    private val ic_stop = 12
    private var mHistory: MutableList<String>? = null
    private var mResult: MutableList<String>? = null
    private var mRecentCommands: MutableList<String>? = null
    private var shellOutput: MutableList<String>? = null
    private var history: MutableList<String>? = null
    private var view: View? = null
    private val viewModel: AshellFragmentViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val examplesViewModel: ExamplesViewModel by viewModels()
    private var mRVPositionAndOffset: Pair<Int, Int>? = null
    private var shell: String? = null


    override fun onPause() {
        super.onPause()

        mainViewModel.setPreviousFragment(Const.LOCAL_FRAGMENT)

        viewModel.isSaveButtonVisible = isSaveButtonVisible

        // Saves the viewing position of the recycler view
        if (binding.rvOutput.layoutManager != null) {
            val layoutManager = binding.rvOutput.layoutManager as LinearLayoutManager?

            val currentPosition = layoutManager!!.findLastVisibleItemPosition()
            val currentView = layoutManager.findViewByPosition(currentPosition)

            if (currentView != null) {
                mRVPositionAndOffset = Pair(currentPosition, currentView.top)
                viewModel.rvPositionAndOffset = mRVPositionAndOffset
            }
        }

        // Gets the already saved output and command history from viewmodel in case no new output has
        // been made after config change
        shellOutput = viewModel.shellOutput
        history = viewModel.history
        viewModel.history = if (mHistory == null && history != null) {
            history
        } else {
            mHistory
        }
        viewModel.shellOutput = if (mResult == null) shellOutput else mResult

        // If there are some text in edit text, then we save it
        viewModel.commandText = binding.commandEditText.text.toString()

        // Saves the visibility of the end icon of edit text
        if (binding.commandInputLayout.isEndIconVisible) viewModel.isEndIconVisible = true
        else isEndIconVisible = false

        // If keyboard is visible then we close it before leaving fragment
        if (isKeyboardVisible) KeyboardUtils.closeKeyboard(requireActivity(), view)
    }

    override fun onResume() {
        super.onResume()

        // we set exit transition to null
        exitTransition = null

        KeyboardUtils.disableKeyboard(context, requireActivity(), view)

        // This function is for restoring the Run button's icon after a configuration change
        when (viewModel.sendDrawable) {
            ic_help -> binding.sendButton.setImageDrawable(
                Utils.getDrawable(R.drawable.ic_help, requireActivity())
            )

            ic_send -> binding.sendButton.setImageDrawable(
                Utils.getDrawable(R.drawable.ic_send, requireActivity())
            )

            ic_stop -> {
                binding.sendButton.setColorFilter(
                    if (DeviceUtils.androidVersion() >= Build.VERSION_CODES.S)
                        ThemeUtils.colorError(context)
                    else
                        ThemeUtils.getColor(R.color.red, context)
                )
                binding.sendButton.setImageDrawable(
                    Utils.getDrawable(R.drawable.ic_stop, requireActivity())
                )
            }

            else -> {}
        }

        /* stop button doesnot appears when coming back to fragment while running continuous commands in root or basic shell mode. So we put this extra method to make sure it does */
        if (isShellBusy) {
            binding.sendButton.setColorFilter(
                if (DeviceUtils.androidVersion() >= Build.VERSION_CODES.S)
                    ThemeUtils.colorError(context)
                else
                    ThemeUtils.getColor(R.color.red, context)
            )
            binding.sendButton.setImageDrawable(
                Utils.getDrawable(
                    R.drawable.ic_stop,
                    requireActivity()
                )
            )
        }
        handleModeButtonTextAndCommandHint()

        handleUseCommand()

        // Handles save button visibility across config changes
        if (!viewModel.isSaveButtonVisible) binding.saveButton.visibility =
            View.GONE
        else {
            binding.saveButton.visibility = View.VISIBLE
            if (binding.search.isGone) {
                binding.clearButton.visibility = View.VISIBLE
                binding.searchButton.visibility = View.VISIBLE
                binding.historyButton.visibility = View.VISIBLE
            }
            binding.shareButton.visibility = View.VISIBLE
            binding.pasteButton.visibility = View.GONE
        }

        binding.rvOutput.layoutManager = LinearLayoutManager(requireActivity())

        // Get the scroll position of recycler view from viewmodel and set it
        if (binding.rvOutput.layoutManager != null) {
            mRVPositionAndOffset = viewModel.rvPositionAndOffset
            if (mRVPositionAndOffset != null) {
                val position: Int = viewModel.rvPositionAndOffset?.first ?: 0
                val offset: Int = viewModel.rvPositionAndOffset?.second ?: 0

                // Restore recyclerView scroll position
                (binding.rvOutput.layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(position, offset)
            }
        }
        isEndIconVisible = viewModel.isEndIconVisible

        // If the end icon of edit text is visible then set its icon accordingly
        if (binding.commandEditText.text.toString().isNotEmpty() && isEndIconVisible) {
            binding.commandInputLayout.endIconDrawable =
                Utils.getDrawable(
                    if (Utils.isBookmarked(
                            binding.commandEditText.text.toString(),
                            requireActivity()
                        )
                    )
                        R.drawable.ic_bookmark_added
                    else
                        R.drawable.ic_add_bookmark,
                    requireActivity()
                )
        }

        // Update edit text when text is shared to the app
        val activity: MainActivity? = activity as MainActivity?
        if (activity != null) {
            val pendingSharedText: String? = activity.pendingSharedText
            if (pendingSharedText != null) {
                updateInputField(pendingSharedText)
                activity.clearPendingSharedText()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BasicShell.destroy()
        ShizukuShell.destroy()
        RootShell.destroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        sharedElementEnterTransition = MaterialContainerTransform()

        binding = FragmentAshellBinding.inflate(inflater, container, false)

        view = binding.root

        if (binding.rvOutput.layoutManager == null) {
            binding.rvOutput.layoutManager = LinearLayoutManager(requireActivity())
        }

        binding.rvCommands.layoutManager = LinearLayoutManager(requireActivity())

        binding.rvCommands.addOnScrollListener(BehaviorFAB.FabExtendingOnScrollListener(binding.pasteButton))
        binding.rvOutput.addOnScrollListener(BehaviorFAB.FabExtendingOnScrollListener(binding.pasteButton))

        binding.rvOutput.addOnScrollListener(BehaviorFAB.FabExtendingOnScrollListener(binding.saveButton))
        binding.rvOutput.addOnScrollListener(BehaviorFAB.FabLocalScrollUpListener(binding.scrollUpButton))

        binding.rvOutput.addOnScrollListener(FabLocalScrollDownListener(binding.scrollDownButton))

        setupRecyclerView()

        // Toggles certain buttons visibility according to keyboard's visibility
        KeyboardUtils.attachVisibilityListener(
            requireActivity()
        ) { visible: Boolean ->
            isKeyboardVisible = visible
            if (visible) buttonsVisibilityGone()
            else buttonsVisibilityVisible()
        }

        // When there is any text in edit text , focus the edit text
        if (binding.commandEditText.text.toString().isNotEmpty()
        ) binding.commandEditText.requestFocus()

        // Handles text changing events in the Input Field
        binding.commandEditText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    binding.commandInputLayout.error = null
                }

                @SuppressLint("SetTextI18n")
                override fun afterTextChanged(s: Editable) {
                    binding.commandEditText.requestFocus()

                    // If shizuku is busy return
                    if ((ShizukuShell.isBusy())
                        || (RootShell.isBusy())
                        || (BasicShell.isBusy())
                    ) return
                    else if (s.toString().trim { it <= ' ' }.isEmpty()) {
                        binding.commandInputLayout.isEndIconVisible = false
                        binding.rvCommands.visibility = View.GONE
                        viewModel.sendDrawable = ic_help
                        binding.sendButton.setImageDrawable(
                            Utils.getDrawable(R.drawable.ic_help, requireActivity())
                        )
                        binding.sendButton.clearColorFilter()
                    } else {
                        viewModel.sendDrawable = ic_send
                        binding.sendButton.setImageDrawable(
                            Utils.getDrawable(R.drawable.ic_send, requireActivity())
                        )
                        binding.commandInputLayout.endIconDrawable = Utils.getDrawable(
                            if (Utils.isBookmarked(
                                    s.toString().trim { it <= ' ' },
                                    requireActivity()
                                )
                            )
                                R.drawable.ic_bookmark_added
                            else
                                R.drawable.ic_add_bookmark,
                            requireActivity()
                        )

                        binding.commandInputLayout.isEndIconVisible = true

                        commandInputLayoutEndIconOnClickListener(s)

                        commandSuggestion(s)
                    }
                }
            })

        // Handles the onclick listener of the top and bottom scrolling arrows
        BehaviorFAB.handleTopAndBottomArrow(
            binding.scrollUpButton,
            binding.scrollDownButton,
            binding.rvOutput,
            null,
            context,
            "local_shell"
        )

        // Paste and undo button onClickListener
        BehaviorFAB.pasteAndUndo(
            binding.pasteButton, binding.undoButton, binding.commandEditText, context
        )
        pasteAndSaveButtonVisibility()

        handleModeButtonTextAndCommandHint()

        modeButtonOnClickListener()

        settingsButtonOnClickListener()

        clearButtonOnClickListener()

        historyButtonOnClickListener()

        bookmarksButtonOnClickListener()

        searchButtonOnClickListener()

        searchWordChangeListener()

        searchBarEndIconOnClickListener()

        saveButtonOnClickListener()

        shareButtonOnClickListener()

        shareButtonVisibilityHandler()

        interceptOnBackPress()

        commandEditTextOnEditorActionListener()

        sendButtonOnClickListener()

        mainViewModel.setHomeFragment(Const.LOCAL_FRAGMENT)
        return binding.getRoot()
    }

    private fun lastIndexOf(s: String, splitTxt: String): Int {
        return s.lastIndexOf(splitTxt)
    }

    private val recentCommands: MutableList<String>?
        get() {
            if (mHistory == null && viewModel.history != null) {
                mRecentCommands = viewModel.history
                mHistory = mRecentCommands
            } else {
                mRecentCommands = mHistory?.let { ArrayList(it) }
                (mRecentCommands as ArrayList<String>).reverse()
            }
            return mRecentCommands
        }

    private fun splitPrefix(s: String, i: Int): String {
        val splitPrefix =
            arrayOf(s.substring(0, lastIndexOf(s, " ")), s.substring(lastIndexOf(s, " ")))
        return splitPrefix[i].trim { it <= ' ' }
    }

    // Keep the recycler view scrolling when running continuous commands
    private fun updateUI(data: List<String>?) {
        if (data == null) return

        val mData: MutableList<String> = ArrayList()
        try {
            for (result in data) {
                if (!TextUtils.isEmpty(result) && result != Utils.shellDeadError()) mData.add(result)
            }
        } catch (ignored: ConcurrentModificationException) {
            // Handle concurrent modification gracefully
        }

        val mExecutors = Executors.newSingleThreadExecutor()
        mExecutors.execute {
            mShellOutputAdapter = ShellOutputAdapter(mData)
            Handler(Looper.getMainLooper())
                .post {
                    if (isAdded) {
                        binding.rvOutput.adapter = mShellOutputAdapter
                        binding.rvOutput.scrollToPosition(mData.size - 1)
                    }
                }
            if (!mExecutors.isShutdown) mExecutors.shutdown()
        }
    }

    /*Calling this function hides the search bar and makes other buttons visible again*/
    private fun hideSearchBar() {
        binding.search.text = null

        Transitions.materialContainerTransformViewToView(binding.search, binding.searchButton)
        binding.searchButton.icon = Utils.getDrawable(R.drawable.ic_search, context)
        if (!binding.commandEditText.isFocused) binding.commandEditText.requestFocus()
        Handler(Looper.getMainLooper())
            .postDelayed(
                {
                    binding.bookmarksButton.visibility = View.VISIBLE
                    binding.settingsButton.visibility = View.VISIBLE
                    binding.historyButton.visibility = View.VISIBLE
                    binding.clearButton.visibility = View.VISIBLE
                },
                200
            )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun searchBarEndIconOnClickListener() {
        binding.search.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.search.compoundDrawablesRelative[2]
                if (drawableEnd != null) {
                    val drawableStartX =
                        (binding.search.width
                                - binding.search.paddingEnd
                                - drawableEnd.intrinsicWidth)
                    if (event.x >= drawableStartX) {
                        hideSearchBar()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    // Call to set the visibility of elements with a delay
    private fun setVisibilityWithDelay(view: View, delayMillis: Int) {
        Handler(Looper.getMainLooper())
            .postDelayed({ view.visibility = View.VISIBLE }, delayMillis.toLong())
    }

    // Get the command when using Use feature
    private fun handleUseCommand() {
        if (mainViewModel.useCommand != null) {
            updateInputField(mainViewModel.useCommand)
            mainViewModel.useCommand = null
        }
    }

    // handles text shared to ashell you
    fun handleSharedTextIntent(sharedText: String?) {
        if (sharedText != null) {
            val switchState = Preferences.getShareAndRun()
            updateInputField(sharedText)
            if (switchState) {
                if (!Shizuku.pingBinder() && isShizukuMode) handleShizukuUnavailability()
                else if (!RootShell.isDeviceRooted() && isRootMode) handleRootUnavailability()
                else {
                    binding.commandEditText.setText(sharedText)
                    initializeShell()
                }
            }
        }
    }

    // Call to update the edit Text with a text
    fun updateInputField(text: String?) {
        if (text != null) {
            binding.commandEditText.setText(text)
            binding.commandEditText.requestFocus()
            binding.commandEditText.setSelection(binding.commandEditText.text!!.length)
            viewModel.sendDrawable = ic_send
            binding.sendButton.setImageDrawable(
                Utils.getDrawable(
                    R.drawable.ic_send,
                    requireActivity()
                )
            )
            viewModel.sendDrawable = ic_send
        }
    }

    private val isSaveButtonVisible: Boolean
        // Boolean that returns the visibility of Save button
        get() = binding.saveButton.isVisible

    // Setup the recycler view
    private fun setupRecyclerView() {
        binding.rvOutput.layoutManager = LinearLayoutManager(requireActivity())

        val shellOutput: MutableList<String>? = viewModel.shellOutput
        if (shellOutput != null) {
            mShellOutputAdapter = ShellOutputAdapter(shellOutput)
            mResult = shellOutput
            binding.rvOutput.adapter = mShellOutputAdapter
        }

        val scrollPosition: Int = viewModel.scrollPosition
        binding.rvOutput.scrollToPosition(scrollPosition)
        val command: String? = viewModel.commandText
        binding.commandEditText.setText(command)
    }

    // Call to initialize the shell output and command history
    private fun initializeResults() {
        if (mResult == null) mResult = shellOutput

        if (mHistory == null) mHistory = history
    }

    // Converts the List<String> mResult to String
    private fun buildResultsString(): StringBuilder {
        val sb = StringBuilder()
        for (i in mPosition..<mResult!!.size) {
            val result = mResult!![i]
            if (Utils.shellDeadError() != result && "<i></i>" != result) sb.append(result)
                .append("\n")
        }
        return sb
    }

    // Hide buttons when keyboard is visible
    private fun buttonsVisibilityGone() {
        binding.pasteButton.visibility = View.GONE
        binding.undoButton.visibility = View.GONE
        binding.saveButton.visibility = View.GONE
        binding.shareButton.visibility = View.GONE
    }

    // Show buttons again when keyboard is gone
    private fun buttonsVisibilityVisible() {
        if (binding.rvOutput.height != 0) setVisibilityWithDelay(binding.saveButton, 100)

        if (binding.shareButton.isGone && binding.rvOutput.height != 0) setVisibilityWithDelay(
            binding.shareButton, 100
        )

        if (binding.pasteButton.isGone && !sendButtonClicked && mResult == null) setVisibilityWithDelay(
            binding.pasteButton, 100
        )
    }

    // Onclick listener for the button indicating working mode
    private fun modeButtonOnClickListener() {
        binding.modeButton.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            if (isBasicMode) connectedDeviceDialog(DeviceUtils.getDeviceName())
            else if (isShizukuMode) {
                val hasShizuku = Shizuku.pingBinder() && ShizukuShell.hasPermission()
                connectedDeviceDialog(
                    if (hasShizuku) DeviceUtils.getDeviceName() else getString(
                        R.string.none
                    )
                )
            } else if (isRootMode) {
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    val hasRoot = RootShell.isDeviceRooted() && RootShell.hasPermission()
                    requireActivity()
                        .runOnUiThread {
                            connectedDeviceDialog(
                                if (hasRoot) DeviceUtils.getDeviceName() else getString(
                                    R.string.none
                                )
                            )
                        }
                }
                executor.shutdown()
            }
        }
    }

    // Method to show a dialog showing the device name on which shell is being executed
    private fun connectedDeviceDialog(connectedDevice: String) {
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_connected_device, null)

        val device = dialogView.findViewById<MaterialTextView>(R.id.device)
        val switchMode = dialogView.findViewById<MaterialButton>(R.id.switchMode)
        val icon = switchMode.icon
        val confirm = dialogView.findViewById<Button>(R.id.confirm)
        val cancel = dialogView.findViewById<Button>(R.id.cancel)
        val dialogLayout = dialogView.findViewById<LinearLayout>(R.id.dialog_layout)
        val expandableLayout = dialogView.findViewById<LinearLayout>(R.id.options_expanded)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)

        val dialog = MaterialAlertDialogBuilder(requireContext()).setView(dialogView).show()

        radioGroup.check(checkedId)

        radioGroup.setOnCheckedChangeListener { group, checkedId -> HapticUtils.weakVibrate(group as View) }

        device.text = connectedDevice

        switchMode.visibility = View.VISIBLE

        switchMode.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            Utils.startAnim(icon)
            if (!isShellBusy) toggleExpandableLayout(dialogLayout, expandableLayout)
            else ToastUtils.showToast(
                context,
                getString(R.string.abort_command),
                ToastUtils.LENGTH_SHORT
            )
        }

        confirm.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            val checkedId = getCheckedIntValue(radioGroup.checkedRadioButtonId)
            Preferences.setLocalAdbMode(checkedId)
            binding.commandInputLayout.error = null
            handleModeButtonTextAndCommandHint()
            dialog.dismiss()
        }

        cancel.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            dialog.dismiss()
        }
    }

    private val checkedId: Int
        // returns the id of the radio button which is set as the adb mode
        get() {
            return when (Preferences.getLocalAdbMode()) {
                Const.BASIC_MODE -> R.id.basic

                Const.SHIZUKU_MODE -> R.id.shizuku

                Const.ROOT_MODE -> R.id.root

                else -> R.id.basic
            }
        }

    private fun getCheckedIntValue(checkedId: Int): Int {
        return when (checkedId) {
            R.id.basic -> Const.BASIC_MODE

            R.id.shizuku -> Const.SHIZUKU_MODE

            R.id.root -> Const.ROOT_MODE

            else -> Const.BASIC_MODE
        }
    }

    private fun toggleExpandableLayout(dialogLayout: LinearLayout, expandableLayout: LinearLayout) {
        val ANIMATION_DURATION = 250
        if (expandableLayout.isGone) {
            expandableLayout.visibility = View.VISIBLE
            expandableLayout.measure(
                View.MeasureSpec.makeMeasureSpec(dialogLayout.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )

            expandableLayout.pivotY = 0f
            val scaleYAnimator = ObjectAnimator.ofFloat(expandableLayout, "scaleY", 0f, 1f)
            scaleYAnimator.setDuration(ANIMATION_DURATION.toLong())
            scaleYAnimator.start()
        } else {
            val scaleYAnimator = ObjectAnimator.ofFloat(expandableLayout, "scaleY", 1f, 0f)
            scaleYAnimator.setDuration(ANIMATION_DURATION.toLong())
            scaleYAnimator.addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        expandableLayout.visibility = View.GONE
                    }
                })
            scaleYAnimator.start()
        }
    }

    // OnClick listener for the settings button
    private fun settingsButtonOnClickListener() {
        binding.settingsButton.tooltipText = getString(R.string.settings)

        binding.settingsButton.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            goToSettings()
        }
    }

    // OnClick listener for bookmarks button
    private fun bookmarksButtonOnClickListener() {
        binding.bookmarksButton.tooltipText = getString(R.string.bookmarks)
        binding.bookmarksButton.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            if (Utils.getBookmarks(context)
                    .isEmpty()
            ) ToastUtils.showToast(
                context,
                R.string.no_bookmarks,
                ToastUtils.LENGTH_SHORT
            )
            else ActionDialogs.bookmarksDialog(
                context, binding.commandEditText, binding.commandInputLayout
            )
        }
    }

    // OnClick listener for the history button
    private fun historyButtonOnClickListener() {
        binding.historyButton.tooltipText = getString(R.string.history)

        binding.historyButton.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            if (mHistory == null && viewModel.history == null) ToastUtils.showToast(
                context,
                R.string.no_history,
                ToastUtils.LENGTH_SHORT
            )
            else {
                val popupMenu = PopupMenu(
                    requireContext(), binding.commandEditText
                )
                val menu = popupMenu.menu
                for (i in recentCommands!!.indices) {
                    val add = menu.add(
                        Menu.NONE,
                        i,
                        Menu.NONE,
                        recentCommands!![i]
                    )
                }
                popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                    for (i in recentCommands!!.indices) {
                        if (item.itemId == i) {
                            binding.commandEditText.setText(recentCommands!![i])
                            binding.commandEditText.setSelection(
                                binding.commandEditText.text!!.length
                            )
                        }
                    }
                    false
                }
                popupMenu.show()
            }
        }
    }

    // Onclick listener for the clear button
    private fun clearButtonOnClickListener() {
        binding.clearButton.tooltipText = getString(R.string.clear_screen)

        binding.clearButton.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            if (mResult == null || mResult!!.isEmpty()) {
                ToastUtils.showToast(
                    context,
                    R.string.nothing_to_clear,
                    ToastUtils.LENGTH_SHORT
                )
            } else if (isShellBusy) {
                ToastUtils.showToast(
                    context,
                    R.string.abort_command,
                    ToastUtils.LENGTH_SHORT
                )
                return@setOnClickListener
            } else {
                val switchState = Preferences.getClear()
                if (switchState) MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(getString(R.string.clear_everything))
                    .setMessage(getString(R.string.clear_all_message))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setPositiveButton(
                        getString(R.string.yes)
                    ) { dialogInterface: DialogInterface?, i: Int -> clearAll() }
                    .show()
                else clearAll()
            }
        }
    }

    val isShellBusy: Boolean
        // Method to check if shell is busy root or shizuku
        get() {
            if (isBasicMode) return BasicShell.isBusy()
            if (isShizukuMode) return ShizukuShell.isBusy()
            if (isRootMode) return RootShell.isBusy()
            return false
        }

    // This function is called when we want to clear the screen
    private fun clearAll() {
        handleClearExceptions()

        viewModel.shellOutput = null
        mResult = null

        if (binding.scrollUpButton.isVisible) binding.scrollUpButton.visibility =
            View.GONE

        if (binding.scrollDownButton.isVisible) binding.scrollDownButton.visibility =
            View.GONE

        binding.sendButton.setImageDrawable(
            if (hasTextInEditText())
                Utils.getDrawable(R.drawable.ic_send, requireActivity())
            else
                Utils.getDrawable(R.drawable.ic_help, requireActivity())
        )
        viewModel.sendDrawable = if (hasTextInEditText()) ic_send else ic_help
        binding.rvOutput.adapter = null
        binding.saveButton.visibility = View.GONE
        binding.shareButton.visibility = View.GONE
        binding.commandEditText.clearFocus()
        if (!binding.commandEditText.isFocused) binding.commandEditText.requestFocus()
    }

    private fun handleClearExceptions() {
        if (mResult == null || mResult!!.isEmpty()) {
            ToastUtils.showToast(context, R.string.nothing_to_clear, ToastUtils.LENGTH_SHORT)
            return
        } else if (isShellBusy) {
            ToastUtils.showToast(context, R.string.abort_command, ToastUtils.LENGTH_SHORT)
            return
        }
    }

    // OnClick listener for the search button
    private fun searchButtonOnClickListener() {
        binding.searchButton.tooltipText = getString(R.string.search)

        binding.searchButton.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            if (mResult == null || mResult!!.isEmpty()) ToastUtils.showToast(
                context,
                R.string.nothing_to_search,
                ToastUtils.LENGTH_SHORT
            )
            else if (isShellBusy) ToastUtils.showToast(
                context,
                R.string.abort_command,
                ToastUtils.LENGTH_SHORT
            )
            else {
                binding.historyButton.visibility = View.GONE
                binding.clearButton.visibility = View.GONE
                binding.bookmarksButton.visibility = View.GONE
                binding.settingsButton.visibility = View.GONE
                binding.commandEditText.text = null
                binding.searchButton.icon = null
                Transitions.materialContainerTransformViewToView(
                    binding.searchButton,
                    binding.search
                )
                binding.search.requestFocus()
            }
        }
    }

    // Logic for searching text in the output
    private fun searchWordChangeListener() {
        binding.search.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable) {
                    if (s.toString().trim { it <= ' ' }.isEmpty()) updateUI(mResult)
                    else {
                        val mResultSorted: MutableList<String> = ArrayList()
                        for (i in mPosition..<mResult!!.size) {
                            if (mResult!![i]
                                    .lowercase(Locale.getDefault())
                                    .contains(s.toString().lowercase(Locale.getDefault()))
                            ) mResultSorted.add(
                                mResult!![i]
                            )
                        }
                        updateUI(mResultSorted)
                    }
                }
            })
    }

    // OnClick listener for save Button
    private fun saveButtonOnClickListener() {
        binding.saveButton.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            shellOutput = viewModel.shellOutput

            history = viewModel.history
            initializeResults()
            var sb: String? = null
            var fileName: String? = null

            when (Preferences.getSavePreference()) {
                Const.ALL_OUTPUT -> {
                    sb = Utils.convertListToString(mResult)
                    fileName =
                        "shellOutput" + DeviceUtils.getCurrentDateTime()
                }

                Const.LAST_COMMAND_OUTPUT -> {
                    sb = buildResultsString().toString()
                    fileName =
                        Utils.generateFileName(mHistory) + DeviceUtils.getCurrentDateTime()
                }

                else -> {}
            }

            val saved = Utils.saveToFile(sb, requireActivity(), fileName)
            // We add .txt after the final file name to give text format
            if (saved) Preferences.setLastSavedFileName("$fileName.txt")

            // Dialog showing if the output has been saved or not
            FeedbackDialogs.outputSavedDialog(context, saved)
        }
    }

    // Onclick listener for share button
    private fun shareButtonOnClickListener() {
        binding.shareButton.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            shellOutput = viewModel.shellOutput
            history = viewModel.history
            initializeResults()

            val sb = StringBuilder()
            for (i in mPosition..<mResult!!.size) {
                val result = mResult!![i]
                if (Utils.shellDeadError() != result) sb.append(result)
                    .append("\n")
            }
            // We add .txt after the final file name to give it text format
            val fileName = Utils.generateFileName(mHistory) + ".txt"
            Utils.shareOutput(
                requireActivity(),
                context,
                fileName,
                sb.toString()
            )
        }
    }

    // Logic to hide and show share button
    private fun shareButtonVisibilityHandler() {
        binding.rvOutput.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                private val handler = Handler(Looper.getMainLooper())
                private val delayMillis = 1600

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        handler.postDelayed(
                            {
                                if (!isKeyboardVisible) binding.shareButton.show()
                            },
                            delayMillis.toLong()
                        )
                    } else handler.removeCallbacksAndMessages(null)
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy > 0 || dy < 0 && binding.shareButton.isShown) {
                        if (abs(dy.toDouble()) >= 90) binding.shareButton.hide()
                    }
                }
            })
    }

    // The edit text end icon which is responsible for adding /removing bookmarks
    private fun commandInputLayoutEndIconOnClickListener(s: Editable) {
        binding.commandInputLayout.setEndIconOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            if (Utils.isBookmarked(
                    s.toString().trim { it <= ' ' },
                    requireActivity()
                )
            ) {
                Utils.deleteFromBookmark(
                    s.toString().trim { it <= ' ' },
                    requireActivity()
                )
                Utils.snackBar(
                    view,
                    getString(
                        R.string.bookmark_removed_message,
                        s.toString().trim { it <= ' ' })
                )
                    .show()
            } else Utils.addBookmarkIconOnClickListener(
                s.toString().trim { it <= ' ' }, view, context
            )
            binding.commandInputLayout.endIconDrawable =
                Utils.getDrawable(
                    if (Utils.isBookmarked(
                            s.toString().trim { it <= ' ' }, requireActivity()
                        )
                    )
                        R.drawable.ic_bookmark_added
                    else
                        R.drawable.ic_add_bookmark,
                    requireActivity()
                )
        }
    }

    // Show command suggestions while typing in the edit text
    private fun commandSuggestion(s: Editable) {
        Handler(Looper.getMainLooper())
            .post {
                if (s.toString().contains(" ") && s.toString().contains(".")) {
                    val splitCommands = arrayOf(
                        s.toString().substring(0, lastIndexOf(s.toString(), ".")),
                        s.toString().substring(lastIndexOf(s.toString(), "."))
                    )
                    val packageNamePrefix =
                        if (splitCommands[0].contains(" ")) splitPrefix(splitCommands[0], 1)
                        else splitCommands[0]

                    mCommandAdapter =
                        CommandsAdapter(
                            Commands.getPackageInfo(
                                "$packageNamePrefix.",
                                context
                            )
                        )
                    if (isAdded) binding.rvCommands.layoutManager =
                        LinearLayoutManager(requireActivity())
                    if (isAdded) binding.rvCommands.adapter = mCommandAdapter

                    binding.rvCommands.visibility = View.VISIBLE
                    mCommandAdapter.setOnItemClickListener { command: String, v: View? ->
                        binding.commandEditText.setText(
                            if (splitCommands[0].contains(" "))
                                splitPrefix(splitCommands[0], 0) + " " + command
                            else
                                command
                        )
                        binding.commandEditText.setSelection(
                            binding.commandEditText.text!!.length
                        )
                        binding.rvCommands.visibility = View.GONE
                    }
                } else {
                    mCommandAdapter = CommandsAdapter(
                        Commands.getCommand(
                            s.toString(),
                            context
                        )
                    )
                    if (isAdded) binding.rvCommands.layoutManager =
                        LinearLayoutManager(requireActivity())

                    binding.rvCommands.adapter = mCommandAdapter
                    binding.rvCommands.visibility = View.VISIBLE
                    mCommandAdapter.setOnItemClickListener { command, v ->
                        if (command.contains(" <")) binding.commandEditText.setText(
                            command.split("<".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()[0])
                        else binding.commandEditText.setText(command)
                        binding.commandEditText.setSelection(
                            binding.commandEditText.text!!.length
                        )
                    }
                }
            }
    }

    // binding.commandEditText on editor action listener
    private fun commandEditTextOnEditorActionListener() {
        binding.commandEditText.setOnEditorActionListener { v: TextView, actionId: Int, event: KeyEvent? ->
            HapticUtils.weakVibrate(v)
            if (actionId == EditorInfo.IME_ACTION_SEND && hasTextInEditText()) {
                sendButtonClicked = true

                // If shell is not busy and there is not any text in input field then go to examples
                if (!hasTextInEditText() && !isShellBusy) goToExamples()
                else if (isBasicMode) {
                    if (BasicShell.isBusy()) abortBasicShell()
                    else execShell(v)
                } else if (isShizukuMode) {
                    if (!Shizuku.pingBinder()) handleShizukuUnavailability()
                    else if (!ShizukuShell.hasPermission()) PermissionDialogs.shizukuPermissionDialog(
                        context
                    )
                    else if (ShizukuShell.isBusy()) abortShizukuShell()
                    else execShell(v)
                } else if (isRootMode) {
                    if (!RootShell.isDeviceRooted()) handleRootUnavailability()
                    else {
                        // We perform root shell permission check on a new thread
                        val executor = Executors.newSingleThreadExecutor()
                        executor.execute {
                            val hasPermission: Boolean = RootShell.hasPermission()
                            requireActivity()
                                .runOnUiThread {
                                    if (!hasPermission) PermissionDialogs.rootPermissionDialog(
                                        context
                                    )
                                    else if (RootShell.isBusy()) abortRootShell()
                                    else execShell(v)
                                }
                        }
                        executor.shutdown()
                    }
                }
                return@setOnEditorActionListener true
            }
            false
        }
    }

    // Send button onclick listener
    private fun sendButtonOnClickListener() {
        binding.sendButton.setOnClickListener { v: View ->
            sendButtonClicked = true
            HapticUtils.weakVibrate(v)

            // If shell is not busy and there is not any text in input field then go to examples
            if (!hasTextInEditText() && !isShellBusy) goToExamples()
            else if (isBasicMode) {
                if (BasicShell.isBusy()) abortBasicShell()
                else execShell(v)
            } else if (isShizukuMode) {
                if (!Shizuku.pingBinder()) handleShizukuUnavailability()
                else if (!ShizukuShell.hasPermission()) PermissionDialogs.shizukuPermissionDialog(
                    context
                )
                else if (ShizukuShell.isBusy()) abortShizukuShell()
                else execShell(v)
            } else if (isRootMode) {
                if (!RootShell.isDeviceRooted()) handleRootUnavailability()
                else {
                    // We perform root shell permission check on a new thread
                    val executor = Executors.newSingleThreadExecutor()
                    executor.execute {
                        val hasPermission: Boolean = RootShell.hasPermission()
                        requireActivity()
                            .runOnUiThread {
                                if (!hasPermission) PermissionDialogs.rootPermissionDialog(context)
                                else if (RootShell.isBusy()) abortRootShell()
                                else execShell(v)
                            }
                    }
                    executor.shutdown()
                }
            }
        }
    }

    // Call this method to execute shell
    private fun execShell(v: View) {
        binding.pasteButton.hide()
        binding.undoButton.hide()
        if (isAdded) {
            binding.commandInputLayout.error = null
            initializeShell()
            KeyboardUtils.closeKeyboard(requireActivity(), v)
        }
    }

    // initialize the shell command execution
    private fun initializeShell() {
        if (!hasTextInEditText()) return
        val command = binding.commandEditText.text.toString().replace("\n", "")
        if (isPackageSensitive(command)) sensitivePackageWarningDialog(command)
        else runShellCommand(command)
    }

    private fun sensitivePackageWarningDialog(command: String) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_sensitive_package_warning, null)

        val okButton = dialogView.findViewById<MaterialButton>(R.id.ok)
        val cancelButton = dialogView.findViewById<MaterialButton>(R.id.cancel)

        val dialog =
            MaterialAlertDialogBuilder(requireContext()).setView(dialogView).setCancelable(false)
                .create()

        dialog.show()

        // Start countdown timer (10 seconds)
        object : CountDownTimer(10000, 1000) {
            var timeLeft: Int = 10

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                okButton.text = "OK (" + timeLeft + "s)"
                timeLeft--
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                okButton.text = "OK"
                okButton.isEnabled = true
            }
        }.start()

        okButton.setOnClickListener {
            // Run the command containing sensitive package name
            runShellCommand(command)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener { dialog.dismiss() }
    }

    // This function is called when we want to run the shell after entering an adb command
    private fun runShellCommand(command: String) {
        if (!isAdded || activity == null) return

        // Set up adapter if not already done
        mShellOutputAdapter = ShellOutputAdapter(mResult)
        if (binding.rvOutput.adapter == null) binding.rvOutput.adapter = mShellOutputAdapter

        // Lock the screen orientation
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        // Clear input and hide the search bar if visible
        binding.commandEditText.text = null
        binding.commandEditText.clearFocus()
        if (binding.search.isVisible) hideSearchBar()

        // Process the command
        val finalCommand = command.replace("^adb(?:\\s+-d)?\\s+shell\\s+".toRegex(), "")

        // Command to clear the shell output
        if (finalCommand == "clear") {
            clearAll()
            return
        }

        // Command to exit the app
        if (finalCommand == "exit") {
            FeedbackDialogs.confirmExitDialog(context, requireActivity())
            return
        }

        // show warning for su command in non rooted methods
        if (finalCommand.startsWith("su") && (isShizukuMode || isBasicMode)) {
            suWarning()
            return
        }

        // Initialize mHistory if necessary
        if (mHistory == null) mHistory = ArrayList()

        mHistory!!.add(finalCommand)

        // Hide buttons and update send button
        binding.saveButton.hide()
        binding.shareButton.hide()
        viewModel.sendDrawable = ic_stop
        binding.sendButton.setImageDrawable(
            Utils.getDrawable(
                R.drawable.ic_stop,
                requireActivity()
            )
        )
        binding.sendButton.setColorFilter(
            if (DeviceUtils.androidVersion() >= Build.VERSION_CODES.S)
                ThemeUtils.colorError(context)
            else
                ThemeUtils.getColor(R.color.red, context)
        )

        // Determine shell type
        if (isBasicMode) shell = "\">BasicShell@"
        else if (isShizukuMode) shell = "\">ShizukuShell@"
        else if (isRootMode) shell = "\">RootShell@"

        // Create mTitleText and add it to mResult
        if (mResult == null) mResult = ArrayList()

        val mTitleText =
            ("<font color=\""
                    + ThemeUtils.getColor(
                if (DeviceUtils.androidVersion() >= Build.VERSION_CODES.S)
                    android.R.color.system_accent1_500
                else
                    R.color.blue,
                requireActivity()
            )
                    + shell
                    + DeviceUtils.getDeviceName()
                    + " | "
                    + "</font><font color=\""
                    + ThemeUtils.getColor(
                if (DeviceUtils.androidVersion() >= Build.VERSION_CODES.S)

                    android.R.color.system_accent3_500
                else
                    R.color.green,
                requireActivity()
            )
                    + "\"> # "
                    + finalCommand)
        mResult!!.add(mTitleText)

        // Execute the shell command in a background thread
        val mExecutors = Executors.newSingleThreadExecutor()
        mExecutors.execute {
            when (Preferences.getLocalAdbMode()) {
                Const.BASIC_MODE -> runBasicShell(finalCommand)
                Const.SHIZUKU_MODE -> runWithShizuku(finalCommand)
                Const.ROOT_MODE -> runWithRoot(finalCommand)
                else -> return@execute
            }
            Handler(Looper.getMainLooper())
                .post {
                    if (!isAdded || activity == null) return@post
                    postExec()

                    // Update send button based on command text presence
                    if (!hasTextInEditText()) {
                        viewModel.sendDrawable = ic_help
                        binding.sendButton.setImageDrawable(
                            Utils.getDrawable(
                                R.drawable.ic_help,
                                requireActivity()
                            )
                        )
                        binding.sendButton.clearColorFilter()
                    } else {
                        viewModel.sendDrawable = ic_send
                        binding.sendButton.setImageDrawable(
                            Utils.getDrawable(
                                R.drawable.ic_send,
                                requireActivity()
                            )
                        )
                        binding.sendButton.clearColorFilter()
                    }

                    // Unlock the screen orientation
                    requireActivity().requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                    // Ensure focus is back on the command input
                    if (!binding.commandEditText.isFocused) binding.commandEditText.requestFocus()
                }

            // Shutdown the executor service
            if (!mExecutors.isShutdown) mExecutors.shutdown()
        }

        // Post UI updates back to the main thread
        val executor = Executors.newSingleThreadScheduledExecutor()

        executor.scheduleWithFixedDelay(
            {
                if (mResult != null && mResult!!.isNotEmpty() && (mResult!![mResult!!.size - 1] != Utils.shellDeadError()) && isShellBusy) {
                    updateUI(mResult)
                }
            },
            0,
            250,
            TimeUnit.MILLISECONDS
        )
    }

    // Method to run commands using root
    private fun runBasicShell(finalCommand: String) {
        mPosition = mResult!!.size
        mBasicShell = BasicShell(mResult, finalCommand)
        BasicShell.exec()
        try {
            TimeUnit.MILLISECONDS.sleep(250)
        } catch (ignored: InterruptedException) {
        }
    }

    // Method to run commands using Shizuku
    private fun runWithShizuku(finalCommand: String) {
        mPosition = mResult!!.size
        mShizukuShell = ShizukuShell(mResult, finalCommand)
        mShizukuShell.exec()
        try {
            TimeUnit.MILLISECONDS.sleep(250)
        } catch (ignored: InterruptedException) {
        }
    }

    // Method to run commands using root
    private fun runWithRoot(finalCommand: String) {
        mPosition = mResult!!.size
        mRootShell = RootShell(mResult, finalCommand)
        RootShell.exec()
        try {
            TimeUnit.MILLISECONDS.sleep(250)
        } catch (ignored: InterruptedException) {
        }
    }

    // Call this post command execution
    private fun postExec() {
        if (mResult != null && mResult!!.isNotEmpty()) {
            mResult!!.add(Utils.shellDeadError())
            if (!isKeyboardVisible) {
                binding.saveButton.show()
                binding.shareButton.show()
            }
        }
    }

    private val isBasicMode: Boolean
        // boolean that checks if the current set mode is basic mode
        get() = Preferences.getLocalAdbMode() == Const.BASIC_MODE

    private val isShizukuMode: Boolean
        // boolean that checks if the current set mode is shizuku mode
        get() = Preferences.getLocalAdbMode() == Const.SHIZUKU_MODE

    private val isRootMode: Boolean
        // boolean that checks if the current set mode is root mode
        get() = Preferences.getLocalAdbMode() == Const.ROOT_MODE

    // This methods checks if there is valid text in the edit text
    private fun hasTextInEditText(): Boolean {
        return binding.commandEditText.text != null
                && binding.commandEditText.text.toString().trim { it <= ' ' }.isNotEmpty()
    }

    // Call this method to abort or stop running shell command
    private fun abortBasicShell() {
        BasicShell.destroy()
        viewModel.sendDrawable = ic_help
        binding.sendButton.setImageDrawable(
            Utils.getDrawable(
                R.drawable.ic_help,
                requireActivity()
            )
        )
        binding.sendButton.clearColorFilter()
    }

    // Call this method to abort or stop running shizuku command
    private fun abortShizukuShell() {
        ShizukuShell.destroy()
        viewModel.sendDrawable = ic_help
        binding.sendButton.setImageDrawable(
            Utils.getDrawable(
                R.drawable.ic_help,
                requireActivity()
            )
        )
        binding.sendButton.clearColorFilter()
    }

    // Call this method to abort or stop running root command
    private fun abortRootShell() {
        RootShell.destroy()
        viewModel.sendDrawable = ic_help
        binding.sendButton.setImageDrawable(
            Utils.getDrawable(
                R.drawable.ic_help,
                requireActivity()
            )
        )
        binding.sendButton.clearColorFilter()
    }

    // error handling when shizuku is unavailable
    private fun handleShizukuUnavailability() {
        binding.commandInputLayout.error = getString(R.string.shizuku_unavailable)
        if (binding.commandEditText.text != null) {
            binding.commandInputLayout.errorIconDrawable =
                Utils.getDrawable(R.drawable.ic_cancel, requireActivity())
            binding.commandInputLayout.setErrorIconOnClickListener {
                binding.commandEditText.text =
                    null
            }
        }
        ErrorDialogs.shizukuUnavailableDialog(context)
    }

    // error handling when root is unavailable
    private fun handleRootUnavailability() {
        binding.commandInputLayout.error = getString(R.string.root_unavailable)
        if (binding.commandEditText.text != null) {
            binding.commandInputLayout.errorIconDrawable =
                Utils.getDrawable(R.drawable.ic_cancel, requireActivity())
            binding.commandInputLayout.setErrorIconOnClickListener {
                binding.commandEditText.text =
                    null
            }
        }

        ErrorDialogs.rootUnavailableDialog(context)
    }

    // Show warning when running su commands with shizuku
    private fun suWarning() {
        binding.commandInputLayout.error = getString(R.string.su_warning)
        binding.commandInputLayout.errorIconDrawable =
            Utils.getDrawable(
                R.drawable.ic_error,
                requireActivity()
            )
        binding.commandEditText.requestFocus()
        Utils.snackBar(
            requireActivity().findViewById(android.R.id.content),
            getString(R.string.su_warning_message)
        )
            .show()
    }

    // Open command examples fragment
    private fun goToExamples() {
        examplesViewModel.rvPositionAndOffset = null
        examplesViewModel.isToolbarExpanded = true

        exitTransition = Hold()

        val fragment: ExamplesFragment = ExamplesFragment()

        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        transaction.addSharedElement(binding.sendButton, Const.SEND_TO_EXAMPLES)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            transaction.add(R.id.rightScreen, fragment, fragment.javaClass.getSimpleName())
        } else {
            transaction.replace(
                R.id.fragment_container,
                fragment,
                fragment.javaClass.getSimpleName()
            )
        }
        transaction
            .addToBackStack(fragment.javaClass.getSimpleName())
            .commit()
    }

    //  Open the settings fragment
    private fun goToSettings() {
        settingsViewModel.rvPositionAndOffset = null
        settingsViewModel.isToolbarExpanded = true

        exitTransition = Hold()
        val fragment: SettingsFragment = SettingsFragment()

        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .addSharedElement(binding.settingsButton, Const.SETTINGS_TO_SETTINGS)
            .replace(R.id.fragment_container, fragment, fragment.javaClass.getSimpleName())
            .addToBackStack(fragment.javaClass.getSimpleName())
            .commit()
    }

    /* This method handles the text on the button and the text input layout hint in various cases */
    @SuppressLint("SetTextI18n")
    private fun handleModeButtonTextAndCommandHint() {
        if (isBasicMode) {
            binding.modeButton.text = "Basic shell"
            binding.commandInputLayout.setHint(R.string.command_title)
        } else if (isShizukuMode) {
            binding.modeButton.text = "Shizuku"
            binding.commandInputLayout.setHint(R.string.command_title)
        } else if (isRootMode) {
            binding.modeButton.text = "Root"
            binding.commandInputLayout.setHint(R.string.command_title_root)
        }
    }

    // control visibility of paste and save button
    private fun pasteAndSaveButtonVisibility() {
        if (mResult != null || viewModel.shellOutput != null) {
            binding.pasteButton.visibility = View.GONE
            binding.saveButton.visibility = View.VISIBLE
        }
    }

    private fun interceptOnBackPress() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (isShellBusy) {
                            ToastUtils.showToast(
                                context, getString(R.string.abort_command), ToastUtils.LENGTH_SHORT
                            )
                        } else {
                            isEnabled = false // Remove this callback
                            requireActivity().onBackPressedDispatcher
                                .onBackPressed() // Go back
                        }
                    }
                })
    }
}
