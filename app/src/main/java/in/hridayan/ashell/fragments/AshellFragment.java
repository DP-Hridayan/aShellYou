package in.hridayan.ashell.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.Hold;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.BehaviorFAB;
import in.hridayan.ashell.UI.BehaviorFAB.FabExtendingOnScrollListener;
import in.hridayan.ashell.UI.BehaviorFAB.FabLocalScrollDownListener;
import in.hridayan.ashell.UI.BehaviorFAB.FabLocalScrollUpListener;
import in.hridayan.ashell.UI.KeyboardUtils;
import in.hridayan.ashell.UI.Transitions;
import in.hridayan.ashell.activities.MainActivity;
import in.hridayan.ashell.adapters.CommandsAdapter;
import in.hridayan.ashell.adapters.ShellOutputAdapter;
import in.hridayan.ashell.databinding.FragmentAshellBinding;
import in.hridayan.ashell.utils.BasicShell;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.RootShell;
import in.hridayan.ashell.utils.ShizukuShell;
import in.hridayan.ashell.utils.ThemeUtils;
import in.hridayan.ashell.utils.ToastUtils;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.AshellFragmentViewModel;
import in.hridayan.ashell.viewmodels.ExamplesViewModel;
import in.hridayan.ashell.viewmodels.MainViewModel;
import in.hridayan.ashell.viewmodels.SettingsViewModel;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import rikka.shizuku.Shizuku;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 28, 2022
 */

/*
 * Modified by DP-Hridayan <hridayanofficial@gmail.com> starting from January 24 , 2024
 */

public class AshellFragment extends Fragment {

  private BottomNavigationView mNav;
  private CommandsAdapter mCommandAdapter;
  private ShellOutputAdapter mShellOutputAdapter;
  private ShizukuShell mShizukuShell;
  private RootShell mRootShell;
  private BasicShell mBasicShell;
  private boolean isKeyboardVisible = false, sendButtonClicked = false, isEndIconVisible = false;
  private int mPosition = 1;
  private final int ic_help = 10, ic_send = 11, ic_stop = 12;
  private List<String> mHistory = null, mResult = null, mRecentCommands, shellOutput, history;
  private View view;
  private Context context;
  private AshellFragmentViewModel viewModel;
  private MainViewModel mainViewModel;
  private FragmentAshellBinding binding;
  private Pair<Integer, Integer> mRVPositionAndOffset;
  private String shell;
  private SettingsViewModel settingsViewModel;
  private ExamplesViewModel examplesViewModel;
  private static WeakReference<View> settingsButtonRef;

  public AshellFragment() {}

  @Override
  public void onPause() {
    super.onPause();

    mainViewModel.setPreviousFragment(Preferences.LOCAL_FRAGMENT);

    viewModel.setSaveButtonVisible(isSaveButtonVisible());

    // Saves the viewing position of the recycler view
    if (binding.rvOutput != null) {
      LinearLayoutManager layoutManager = (LinearLayoutManager) binding.rvOutput.getLayoutManager();

      int currentPosition = layoutManager.findLastVisibleItemPosition();
      View currentView = layoutManager.findViewByPosition(currentPosition);

      if (currentView != null) {
        mRVPositionAndOffset = new Pair<>(currentPosition, currentView.getTop());
        viewModel.setRVPositionAndOffset(mRVPositionAndOffset);
      }
    }

    // Gets the already saved output and command history from viewmodel in case no new output has
    // been made after config change
    shellOutput = viewModel.getShellOutput();
    history = viewModel.getHistory();
    viewModel.setHistory(mHistory == null && history != null ? history : mHistory);
    viewModel.setShellOutput(mResult == null ? shellOutput : mResult);

    // If there are some text in edit text, then we save it
    if (binding.commandEditText.getText().toString() != null)
      viewModel.setCommandText(binding.commandEditText.getText().toString());

    // Saves the visibility of the end icon of edit text
    if (binding.commandInputLayout.isEndIconVisible()) viewModel.setEndIconVisible(true);
    else isEndIconVisible = false;

    // If keyboard is visible then we close it before leaving fragment
    if (isKeyboardVisible) KeyboardUtils.closeKeyboard(requireActivity(), view);
  }

  @Override
  public void onResume() {
    super.onResume();
    setExitTransition(null);
    KeyboardUtils.disableKeyboard(context, requireActivity(), view);

    // This function is for restoring the Run button's icon after a configuration change
    switch (viewModel.getSendDrawable()) {
      case ic_help:
        binding.sendButton.setImageDrawable(
            Utils.getDrawable(R.drawable.ic_help, requireActivity()));
        break;

      case ic_send:
        binding.sendButton.setImageDrawable(
            Utils.getDrawable(R.drawable.ic_send, requireActivity()));
        break;

      case ic_stop:
        binding.sendButton.setColorFilter(
            Utils.androidVersion() >= Build.VERSION_CODES.S
                ? ThemeUtils.colorError(context)
                : Utils.getColor(R.color.red, context));
        binding.sendButton.setImageDrawable(
            Utils.getDrawable(R.drawable.ic_stop, requireActivity()));
        break;

      default:
        break;
    }

    handleModeButtonTextAndCommandHint();

    handleUseCommand();

    // Handles save button visibility across config changes
    if (!viewModel.isSaveButtonVisible()) binding.saveButton.setVisibility(View.GONE);
    else {
      binding.saveButton.setVisibility(View.VISIBLE);
      if (binding.search.getVisibility() == View.GONE) {
        binding.clearButton.setVisibility(View.VISIBLE);
        binding.searchButton.setVisibility(View.VISIBLE);
        binding.historyButton.setVisibility(View.VISIBLE);
      }
      binding.shareButton.setVisibility(View.VISIBLE);
      binding.pasteButton.setVisibility(View.GONE);
    }

    binding.rvOutput.setLayoutManager(new LinearLayoutManager(requireActivity()));

    // Get the scroll position of recycler view from viewmodel and set it
    if (binding.rvOutput != null && binding.rvOutput.getLayoutManager() != null) {
      mRVPositionAndOffset = viewModel.getRVPositionAndOffset();
      if (mRVPositionAndOffset != null) {

        int position = viewModel.getRVPositionAndOffset().first;
        int offset = viewModel.getRVPositionAndOffset().second;

        // Restore recyclerView scroll position
        ((LinearLayoutManager) binding.rvOutput.getLayoutManager())
            .scrollToPositionWithOffset(position, offset);
      }
    }
    isEndIconVisible = viewModel.isEndIconVisible();

    // If the end icon of edit text is visible then set its icon accordingly
    if (!binding.commandEditText.getText().toString().isEmpty() && isEndIconVisible) {
      binding.commandInputLayout.setEndIconDrawable(
          Utils.getDrawable(
              Utils.isBookmarked(binding.commandEditText.getText().toString(), requireActivity())
                  ? R.drawable.ic_bookmark_added
                  : R.drawable.ic_add_bookmark,
              requireActivity()));
    }

    // Update edit text when text is shared to the app
    MainActivity activity = (MainActivity) getActivity();
    if (activity != null) {
      String pendingSharedText = activity.getPendingSharedText();
      if (pendingSharedText != null) {
        updateInputField(pendingSharedText);
        activity.clearPendingSharedText();
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (mBasicShell != null) BasicShell.destroy();

    if (mShizukuShell != null) mShizukuShell.destroy();

    if (mRootShell != null) RootShell.destroy();
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    setExitTransition(null);

    binding = FragmentAshellBinding.inflate(inflater, container, false);

    context = requireContext();

    view = binding.getRoot();

    mNav = requireActivity().findViewById(R.id.bottom_nav_bar);

    settingsButtonRef = new WeakReference<>(binding.settingsButton);

    initializeViewModels();

    binding.rvOutput.setLayoutManager(new LinearLayoutManager(requireActivity()));
    binding.rvCommands.setLayoutManager(new LinearLayoutManager(requireActivity()));

    binding.rvCommands.addOnScrollListener(new FabExtendingOnScrollListener(binding.pasteButton));
    binding.rvOutput.addOnScrollListener(new FabExtendingOnScrollListener(binding.pasteButton));

    binding.rvOutput.addOnScrollListener(new FabExtendingOnScrollListener(binding.saveButton));
    binding.rvOutput.addOnScrollListener(new FabLocalScrollUpListener(binding.scrollUpButton));

    binding.rvOutput.addOnScrollListener(new FabLocalScrollDownListener(binding.scrollDownButton));

    binding.rvOutput.setAdapter(mShellOutputAdapter);

    setupRecyclerView();

    // Toggles certain buttons visibility according to keyboard's visibility
    KeyboardUtils.attachVisibilityListener(
        requireActivity(),
        visible -> {
          isKeyboardVisible = visible;
          if (visible) buttonsVisibilityGone();
          else buttonsVisibilityVisible();
        });

    // Set the bottom navigation
    if (!isKeyboardVisible) mNav.setVisibility(View.VISIBLE);

    // When there is any text in edit text , focus the edit text
    if (!binding.commandEditText.getText().toString().isEmpty())
      binding.commandEditText.requestFocus();

    // Handles text changing events in the Input Field
    binding.commandEditText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            binding.commandInputLayout.setError(null);
          }

          @SuppressLint("SetTextI18n")
          @Override
          public void afterTextChanged(Editable s) {

            binding.commandEditText.requestFocus();

            // If shizuku is busy return
            if (mShizukuShell != null && ShizukuShell.isBusy()) return;
            else if (s.toString().trim().isEmpty()) {

              binding.commandInputLayout.setEndIconVisible(false);
              binding.rvCommands.setVisibility(View.GONE);
              viewModel.setSendDrawable(ic_help);
              binding.sendButton.setImageDrawable(
                  Utils.getDrawable(R.drawable.ic_help, requireActivity()));
              binding.sendButton.clearColorFilter();
            } else {
              viewModel.setSendDrawable(ic_send);
              binding.sendButton.setImageDrawable(
                  Utils.getDrawable(R.drawable.ic_send, requireActivity()));
              binding.commandInputLayout.setEndIconDrawable(
                  Utils.getDrawable(
                      Utils.isBookmarked(s.toString().trim(), requireActivity())
                          ? R.drawable.ic_bookmark_added
                          : R.drawable.ic_add_bookmark,
                      requireActivity()));

              binding.commandInputLayout.setEndIconVisible(true);

              commandInputLayoutEndIconOnClickListener(s);

              commandSuggestion(s);
            }
          }
        });

    // Handles the onclick listener of the top and bottom scrolling arrows
    BehaviorFAB.handleTopAndBottomArrow(
        binding.scrollUpButton,
        binding.scrollDownButton,
        binding.rvOutput,
        null,
        context,
        "local_shell");

    // Paste and undo button onClickListener
    BehaviorFAB.pasteAndUndo(
        binding.pasteButton, binding.undoButton, binding.commandEditText, context);
    pasteAndSaveButtonVisibility();

    handleModeButtonTextAndCommandHint();

    modeButtonOnClickListener();

    sendButtonOnClickListener();

    settingsButtonOnClickListener();

    clearButtonOnClickListener();

    historyButtonOnClickListener();

    bookmarksButtonOnClickListener();

    searchButtonOnClickListener();

    searchWordChangeListener();

    saveButtonOnClickListener();

    shareButtonOnClickListener();

    shareButtonVisibilityHandler();

    appNameLayoutOnClickListener();

    commandEditTextOnEditorActionListener();

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleWithFixedDelay(
        () -> {
          if (mResult != null
              && !mResult.isEmpty()
              && !mResult.get(mResult.size() - 1).equals(Utils.shellDeadError())
              && isShellBusy()) {
            updateUI(mResult);
          }
        },
        0,
        250,
        TimeUnit.MILLISECONDS);

    mainViewModel.setHomeFragment(Preferences.LOCAL_FRAGMENT);
    return view;
  }

  // Functions

  private int lastIndexOf(String s, String splitTxt) {
    return s.lastIndexOf(splitTxt);
  }

  private List<String> getRecentCommands() {

    if (mHistory == null && viewModel.getHistory() != null) {
      mRecentCommands = viewModel.getHistory();
      mHistory = mRecentCommands;
    } else {
      mRecentCommands = new ArrayList<>(mHistory);
      Collections.reverse(mRecentCommands);
    }

    return mRecentCommands;
  }

  private String splitPrefix(String s, int i) {
    String[] splitPrefix = {s.substring(0, lastIndexOf(s, " ")), s.substring(lastIndexOf(s, " "))};
    return splitPrefix[i].trim();
  }

  // Keep the recycler view scrolling when running continuous commands
  private void updateUI(List<String> data) {
    if (data == null) return;

    List<String> mData = new ArrayList<>();
    try {
      for (String result : data) {
        if (!TextUtils.isEmpty(result) && !result.equals(Utils.shellDeadError())) mData.add(result);
      }
    } catch (ConcurrentModificationException ignored) {
      // Handle concurrent modification gracefully
    }

    ExecutorService mExecutors = Executors.newSingleThreadExecutor();
    mExecutors.execute(
        () -> {
          ShellOutputAdapter mShellOutputAdapter = new ShellOutputAdapter(mData);
          new Handler(Looper.getMainLooper())
              .post(
                  () -> {
                    if (isAdded() && binding.rvOutput != null) {
                      binding.rvOutput.setAdapter(mShellOutputAdapter);
                      binding.rvOutput.scrollToPosition(mData.size() - 1);
                    }
                  });

          if (!mExecutors.isShutdown()) mExecutors.shutdown();
        });
  }

  /*Calling this function hides the search bar and makes other buttons visible again*/
  private void hideSearchBar() {
    binding.search.setText(null);

    Transitions.materialContainerTransformViewToView(binding.search, binding.searchButton);
    binding.searchButton.setIcon(Utils.getDrawable(R.drawable.ic_search, context));
    if (!binding.commandEditText.isFocused()) binding.commandEditText.requestFocus();
    new Handler()
        .postDelayed(
            () -> {
              binding.bookmarksButton.setVisibility(View.VISIBLE);
              binding.settingsButton.setVisibility(View.VISIBLE);
              binding.historyButton.setVisibility(View.VISIBLE);
              binding.clearButton.setVisibility(View.VISIBLE);
            },
            200);
  }

  // Call to show the bottom navigation view
  private void showBottomNav() {
    if (getActivity() != null && getActivity() instanceof MainActivity)
      ((MainActivity) getActivity()).mNav.animate().translationY(0);
  }

  // Call to set the visibility of elements with a delay
  private void setVisibilityWithDelay(View view, int delayMillis) {
    new Handler(Looper.getMainLooper())
        .postDelayed(() -> view.setVisibility(View.VISIBLE), delayMillis);
  }

  // handles text shared to ashell you
  public void handleSharedTextIntent(Intent intent, String sharedText) {
    if (sharedText != null) {
      boolean switchState = Preferences.getShareAndRun(context);
      updateInputField(sharedText);
      if (switchState) {
        if (!Shizuku.pingBinder() && isShizukuMode()) handleShizukuUnavailability();
        else if (!RootShell.isDeviceRooted() && isRootMode()) handleRootUnavailability();
        else {
          binding.commandEditText.setText(sharedText);
          initializeShell();
        }
      }
    }
  }

  // Call to update the edit Text with a text
  public void updateInputField(String text) {
    if (text != null) {
      binding.commandEditText.setText(text);
      binding.commandEditText.requestFocus();
      binding.commandEditText.setSelection(binding.commandEditText.getText().length());
      viewModel.setSendDrawable(ic_send);
      binding.sendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_send, requireActivity()));
      viewModel.setSendDrawable(ic_send);
    }
  }

  // Boolean that returns the visibility of Save button
  private boolean isSaveButtonVisible() {
    return binding.saveButton.getVisibility() == View.VISIBLE;
  }

  // Setup the recycler view
  private void setupRecyclerView() {
    binding.rvOutput.setLayoutManager(new LinearLayoutManager(requireActivity()));

    List<String> shellOutput = viewModel.getShellOutput();
    if (shellOutput != null) {
      mShellOutputAdapter = new ShellOutputAdapter(shellOutput);
      mResult = shellOutput;
    }
    binding.rvOutput.setAdapter(mShellOutputAdapter);

    int scrollPosition = viewModel.getScrollPosition();
    binding.rvOutput.scrollToPosition(scrollPosition);
    String command = viewModel.getCommandText();
    if (command != null) binding.commandEditText.setText(command);
  }

  // Call to initialize the shell output and command history
  private void initializeResults() {
    if (mResult == null) mResult = shellOutput;

    if (mHistory == null) mHistory = history;
  }

  // Converts the List<String> mResult to String
  private StringBuilder buildResultsString() {
    StringBuilder sb = new StringBuilder();
    for (int i = mPosition; i < mResult.size(); i++) {
      String result = mResult.get(i);
      if (!Utils.shellDeadError().equals(result) && !"<i></i>".equals(result))
        sb.append(result).append("\n");
    }
    return sb;
  }

  // Hide buttons when keyboard is visible
  private void buttonsVisibilityGone() {
    binding.pasteButton.setVisibility(View.GONE);
    binding.undoButton.setVisibility(View.GONE);
    binding.saveButton.setVisibility(View.GONE);
    binding.shareButton.setVisibility(View.GONE);
  }

  // Show buttons again when keyboard is gone
  private void buttonsVisibilityVisible() {
    if (binding.rvOutput.getHeight() != 0) setVisibilityWithDelay(binding.saveButton, 100);

    if (binding.shareButton.getVisibility() == View.GONE && binding.rvOutput.getHeight() != 0)
      setVisibilityWithDelay(binding.shareButton, 100);

    if (binding.pasteButton.getVisibility() == View.GONE && !sendButtonClicked && mResult == null)
      setVisibilityWithDelay(binding.pasteButton, 100);
  }

  // Onclick listener for the button indicating working mode
  private void modeButtonOnClickListener() {
    binding.modeButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          if (isBasicMode()) connectedDeviceDialog(Utils.getDeviceName());
          else if (isShizukuMode()) {
            boolean hasShizuku = Shizuku.pingBinder() && ShizukuShell.hasPermission();
            connectedDeviceDialog(hasShizuku ? Utils.getDeviceName() : getString(R.string.none));
          } else if (isRootMode()) {
            boolean hasRoot = RootShell.isDeviceRooted() && RootShell.hasPermission();
            connectedDeviceDialog(hasRoot ? Utils.getDeviceName() : getString(R.string.none));
          }
        });
  }

  // Method to show a dialog showing the device name on which shell is being executed
  private void connectedDeviceDialog(String connectedDevice) {
    String device = connectedDevice;
    new MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.connected_device))
        .setMessage(device)
        .setNegativeButton(getString(R.string.cancel), null)
        .setPositiveButton(
            getString(R.string.change_mode),
            (dialog, i) -> {
              if (!isShellBusy()) localAdbModeDialog();
              else
                ToastUtils.showToast(
                    context, getString(R.string.abort_command), ToastUtils.LENGTH_SHORT);
            })
        .show();
  }

  // Dialog asking to choose preferred local adb commands executing mode
  private void localAdbModeDialog() {
    final CharSequence[] preferences = {
      getString(R.string.basic_shell), getString(R.string.shizuku), getString(R.string.root)
    };

    int savePreference = Preferences.getLocalAdbMode(context);
    final int[] preference = {savePreference};

    String title = getString(R.string.local_adb) + " " + getString(R.string.mode).toLowerCase();

    new MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setSingleChoiceItems(
            preferences,
            savePreference,
            (dialog, which) -> {
              preference[0] = which;
            })
        .setPositiveButton(
            getString(R.string.choose),
            (dialog, which) -> {
              Preferences.setLocalAdbMode(context, preference[0]);
              binding.commandInputLayout.setError(null);
              handleModeButtonTextAndCommandHint();
            })
        .setNegativeButton(getString(R.string.cancel), null)
        .show();
  }

  // OnClick listener for the settings button
  private void settingsButtonOnClickListener() {
    binding.settingsButton.setTooltipText(getString(R.string.settings));

    binding.settingsButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, getContext());

          goToSettings();
        });
  }

  // OnClick listener for bookmarks button
  private void bookmarksButtonOnClickListener() {
    binding.bookmarksButton.setTooltipText(getString(R.string.bookmarks));
    binding.bookmarksButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);

          if (Utils.getBookmarks(context).isEmpty())
            ToastUtils.showToast(context, R.string.no_bookmarks, ToastUtils.LENGTH_SHORT);
          else
            Utils.bookmarksDialog(
                context, requireActivity(), binding.commandEditText, binding.commandInputLayout);
        });
  }

  // OnClick listener for the history button
  private void historyButtonOnClickListener() {
    binding.historyButton.setTooltipText(getString(R.string.history));

    binding.historyButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);

          if (mHistory == null && viewModel.getHistory() == null)
            ToastUtils.showToast(context, R.string.no_history, ToastUtils.LENGTH_SHORT);
          else {
            PopupMenu popupMenu = new PopupMenu(context, binding.commandEditText);
            Menu menu = popupMenu.getMenu();
            for (int i = 0; i < getRecentCommands().size(); i++) {
              menu.add(Menu.NONE, i, Menu.NONE, getRecentCommands().get(i));
            }
            popupMenu.setOnMenuItemClickListener(
                item -> {
                  for (int i = 0; i < getRecentCommands().size(); i++) {
                    if (item.getItemId() == i) {
                      binding.commandEditText.setText(getRecentCommands().get(i));
                      binding.commandEditText.setSelection(
                          binding.commandEditText.getText().length());
                    }
                  }
                  return false;
                });
            popupMenu.show();
          }
        });
  }

  // Onclick listener for the clear button
  private void clearButtonOnClickListener() {
    binding.clearButton.setTooltipText(getString(R.string.clear_screen));

    binding.clearButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          if (mResult == null || mResult.isEmpty())
            ToastUtils.showToast(context, R.string.nothing_to_clear, ToastUtils.LENGTH_SHORT);
          else if (isShellBusy())
            ToastUtils.showToast(context, R.string.abort_command, ToastUtils.LENGTH_SHORT);
          else {
            boolean switchState = Preferences.getClear(context);
            if (switchState)
              new MaterialAlertDialogBuilder(requireActivity())
                  .setTitle(getString(R.string.clear_everything))
                  .setMessage(getString(R.string.clear_all_message))
                  .setNegativeButton(getString(R.string.cancel), null)
                  .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> clearAll())
                  .show();
            else clearAll();
          }
        });
  }

  // Method to check if shell is busy root or shizuku
  public boolean isShellBusy() {
    if (isBasicMode() && mBasicShell != null) return BasicShell.isBusy();
    if (isShizukuMode() && mShizukuShell != null) return ShizukuShell.isBusy();
    if (isRootMode() && mRootShell != null) return RootShell.isBusy();
    return false;
  }

  // This function is called when we want to clear the screen
  private void clearAll() {
    if (mBasicShell != null && BasicShell.isBusy()) abortBasicShell();

    if (mShizukuShell != null && ShizukuShell.isBusy()) abortShizukuShell();

    if (mRootShell != null && RootShell.isBusy()) abortRootShell();

    viewModel.setShellOutput(null);
    mResult = null;

    if (binding.scrollUpButton.getVisibility() == View.VISIBLE)
      binding.scrollUpButton.setVisibility(View.GONE);

    if (binding.scrollDownButton.getVisibility() == View.VISIBLE)
      binding.scrollDownButton.setVisibility(View.GONE);

    binding.sendButton.setImageDrawable(
        hasTextInEditText()
            ? Utils.getDrawable(R.drawable.ic_send, requireActivity())
            : Utils.getDrawable(R.drawable.ic_help, requireActivity()));
    viewModel.setSendDrawable(hasTextInEditText() ? ic_send : ic_help);
    binding.rvOutput.setAdapter(null);
    binding.saveButton.setVisibility(View.GONE);
    binding.shareButton.setVisibility(View.GONE);
    showBottomNav();
    binding.commandEditText.clearFocus();
    if (!binding.commandEditText.isFocused()) binding.commandEditText.requestFocus();
  }

  // OnClick listener for the search button
  private void searchButtonOnClickListener() {
    binding.searchButton.setTooltipText(getString(R.string.search));

    binding.searchButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);

          if (mResult == null || mResult.isEmpty())
            ToastUtils.showToast(context, R.string.nothing_to_search, ToastUtils.LENGTH_SHORT);
          else if (isShellBusy())
            ToastUtils.showToast(context, R.string.abort_command, ToastUtils.LENGTH_SHORT);
          else {
            binding.historyButton.setVisibility(View.GONE);
            binding.clearButton.setVisibility(View.GONE);
            binding.bookmarksButton.setVisibility(View.GONE);
            binding.settingsButton.setVisibility(View.GONE);
            binding.commandEditText.setText(null);
            binding.searchButton.setIcon(null);
            Transitions.materialContainerTransformViewToView(binding.searchButton, binding.search);
            binding.search.requestFocus();
          }
        });
  }

  // Logic for searching text in the output
  private void searchWordChangeListener() {
    binding.search.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (s == null || s.toString().trim().isEmpty()) updateUI(mResult);
            else {
              List<String> mResultSorted = new ArrayList<>();
              for (int i = mPosition; i < mResult.size(); i++) {
                if (mResult
                    .get(i)
                    .toLowerCase(Locale.getDefault())
                    .contains(s.toString().toLowerCase(Locale.getDefault())))
                  mResultSorted.add(mResult.get(i));
              }
              updateUI(mResultSorted);
            }
          }
        });
  }

  // OnClick listener for save Button
  private void saveButtonOnClickListener() {

    binding.saveButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          shellOutput = viewModel.getShellOutput();

          history = viewModel.getHistory();
          initializeResults();
          String sb = null, fileName = null;

          switch (Preferences.getSavePreference(context)) {
            case Preferences.ALL_OUTPUT:
              sb = Utils.convertListToString(mResult);
              fileName = "shizukuOutput" + Utils.getCurrentDateTime();
              break;

            case Preferences.LAST_COMMAND_OUTPUT:
              sb = buildResultsString().toString();
              fileName = Utils.generateFileName(mHistory) + Utils.getCurrentDateTime();
              break;

            default:
              break;
          }

          boolean saved = Utils.saveToFile(sb, requireActivity(), fileName);
          if (saved) Preferences.setLastSavedFileName(context, fileName + ".txt");

          // Dialog showing if the output has been saved or not
          Utils.outputSavedDialog(requireActivity(), context, saved);
        });
  }

  // Onclick listener for share button
  private void shareButtonOnClickListener() {
    binding.shareButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          shellOutput = viewModel.getShellOutput();
          history = viewModel.getHistory();
          initializeResults();

          StringBuilder sb = new StringBuilder();
          for (int i = mPosition; i < mResult.size(); i++) {
            String result = mResult.get(i);
            if (!Utils.shellDeadError().equals(result)) sb.append(result).append("\n");
          }
          String fileName = Utils.generateFileName(mHistory);
          Utils.shareOutput(requireActivity(), context, fileName, sb.toString());
        });
  }

  // Logic to hide and show share button
  private void shareButtonVisibilityHandler() {
    binding.rvOutput.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          private final Handler handler = new Handler(Looper.getMainLooper());
          private final int delayMillis = 1600;

          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {

              handler.postDelayed(
                  () -> {
                    if (!isKeyboardVisible) binding.shareButton.show();
                  },
                  delayMillis);
            } else handler.removeCallbacksAndMessages(null);
          }

          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (dy > 0 || dy < 0 && binding.shareButton.isShown()) {
              if (Math.abs(dy) >= 90) binding.shareButton.hide();
            }
          }
        });
  }

  // To dismiss the search I have not found other way than add an onclick listener on the app name
  // layout itself
  private void appNameLayoutOnClickListener() {
    binding.appNameLayout.setOnClickListener(
        v -> {
          if (binding.search.getVisibility() == View.VISIBLE) hideSearchBar();
        });
  }

  // The edit text end icon which is responsible for adding /removing bookmarks
  private void commandInputLayoutEndIconOnClickListener(Editable s) {
    binding.commandInputLayout.setEndIconOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          if (Utils.isBookmarked(s.toString().trim(), requireActivity())) {
            Utils.deleteFromBookmark(s.toString().trim(), requireActivity());
            Utils.snackBar(view, getString(R.string.bookmark_removed_message, s.toString().trim()))
                .show();
          } else Utils.addBookmarkIconOnClickListener(s.toString().trim(), view, context);

          binding.commandInputLayout.setEndIconDrawable(
              Utils.getDrawable(
                  Utils.isBookmarked(s.toString().trim(), requireActivity())
                      ? R.drawable.ic_bookmark_added
                      : R.drawable.ic_add_bookmark,
                  requireActivity()));
        });
  }

  // Show command suggestions while typing in the edit text
  private void commandSuggestion(Editable s) {

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              if (s.toString().contains(" ") && s.toString().contains(".")) {
                String[] splitCommands = {
                  s.toString().substring(0, lastIndexOf(s.toString(), ".")),
                  s.toString().substring(lastIndexOf(s.toString(), "."))
                };

                String packageNamePrefix;
                if (splitCommands[0].contains(" "))
                  packageNamePrefix = splitPrefix(splitCommands[0], 1);
                else packageNamePrefix = splitCommands[0];

                mCommandAdapter =
                    new CommandsAdapter(Commands.getPackageInfo(packageNamePrefix + ".", context));
                if (isAdded())
                  binding.rvCommands.setLayoutManager(new LinearLayoutManager(requireActivity()));
                if (isAdded()) binding.rvCommands.setAdapter(mCommandAdapter);

                binding.rvCommands.setVisibility(View.VISIBLE);
                mCommandAdapter.setOnItemClickListener(
                    (command, v) -> {
                      binding.commandEditText.setText(
                          splitCommands[0].contains(" ")
                              ? splitPrefix(splitCommands[0], 0) + " " + command
                              : command);
                      binding.commandEditText.setSelection(
                          binding.commandEditText.getText().length());
                      binding.rvCommands.setVisibility(View.GONE);
                    });
              } else {
                mCommandAdapter = new CommandsAdapter(Commands.getCommand(s.toString(), context));
                if (isAdded())
                  binding.rvCommands.setLayoutManager(new LinearLayoutManager(requireActivity()));

                binding.rvCommands.setAdapter(mCommandAdapter);
                binding.rvCommands.setVisibility(View.VISIBLE);
                mCommandAdapter.setOnItemClickListener(
                    (command, v) -> {
                      if (command.contains(" <"))
                        binding.commandEditText.setText(command.split("<")[0]);
                      else binding.commandEditText.setText(command);

                      binding.commandEditText.setSelection(
                          binding.commandEditText.getText().length());
                    });
              }
            });
  }

  // binding.commandEditText on editor action listener
  private void commandEditTextOnEditorActionListener() {
    binding.commandEditText.setOnEditorActionListener(
        (v, actionId, event) -> {
          HapticUtils.weakVibrate(v, context);
          if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendButtonClicked = true;

            /*This block will run if basic shell mode is selected*/
            if (isBasicMode()) {
              if (isShellBusy()) shellWorkingDialog();
              else execShell(v);
            }

            /*This block will run if shizuku mode is selected*/
            else if (isShizukuMode()) {
              if (!Shizuku.pingBinder()) handleShizukuUnavailability();
              else if (!ShizukuShell.hasPermission())
                Utils.shizukuPermRequestDialog(requireActivity(), context);
              else if (mShizukuShell != null && ShizukuShell.isBusy()) shellWorkingDialog();
              else execShell(v);
            }

            /*This block w if root mode is selected*/
            else if (isRootMode()) {
              if (!RootShell.isDeviceRooted()) handleRootUnavailability();
              else if (!RootShell.hasPermission())
                Utils.rootPermRequestDialog(requireActivity(), context);
              else if (mRootShell != null && RootShell.isBusy()) shellWorkingDialog();
              else execShell(v);
            }

            return true;
          }
          return false;
        });
  }

  // Send button onclick listener
  private void sendButtonOnClickListener() {
    binding.sendButton.setOnClickListener(
        v -> {
          sendButtonClicked = true;
          if (isShellBusy()) ToastUtils.showToast(context, "busy", ToastUtils.LENGTH_SHORT);
          else ToastUtils.showToast(context, "free", ToastUtils.LENGTH_SHORT);
          HapticUtils.weakVibrate(v, context);

          // If shell is not busy and there is not any text in input field then go to examples
          if (!hasTextInEditText() && !isShellBusy()) goToExamples();

          /*This block will run if basic shell mode is selected*/
          else if (isBasicMode()) {
            if (mBasicShell != null && BasicShell.isBusy()) abortBasicShell();
            else execShell(v);
          }

          /*This block will run if shizuku mode is selected*/
          else if (isShizukuMode()) {
            if (!Shizuku.pingBinder()) handleShizukuUnavailability();
            else if (!ShizukuShell.hasPermission())
              Utils.shizukuPermRequestDialog(requireActivity(), context);
            else if (mShizukuShell != null && ShizukuShell.isBusy()) abortShizukuShell();
            else execShell(v);
          }

          /*This block w if root mode is selected*/
          else if (isRootMode()) {
            if (!RootShell.isDeviceRooted()) handleRootUnavailability();
            else if (!RootShell.hasPermission())
              Utils.rootPermRequestDialog(requireActivity(), context);
            else if (mRootShell != null && RootShell.isBusy()) abortRootShell();
            else execShell(v);
          }
        });
  }

  // Call this method to execute shell
  private void execShell(View v) {
    binding.pasteButton.hide();
    binding.undoButton.hide();
    if (isAdded()) {
      binding.commandInputLayout.setError(null);
      initializeShell();
      KeyboardUtils.closeKeyboard(requireActivity(), v);
    }
  }

  // initialize the shell command execution
  private void initializeShell() {
    if (!hasTextInEditText()) return;

    runShellCommand(binding.commandEditText.getText().toString().replace("\n", ""));
  }

  // This function is called when we want to run the shell after entering an adb command
  private void runShellCommand(String command) {
    if (!isAdded() || getActivity() == null) return;

    // Set up adapter if not already done
    if (binding.rvOutput.getAdapter() == null) binding.rvOutput.setAdapter(mShellOutputAdapter);

    // Lock the screen orientation
    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

    // Clear input and hide the search bar if visible
    binding.commandEditText.setText(null);
    binding.commandEditText.clearFocus();
    if (binding.search.getVisibility() == View.VISIBLE) hideSearchBar();

    // Process the command
    String finalCommand = command.replaceAll("^adb(?:\\s+-d)?\\s+shell\\s+", "");

    // Command to clear the shell output
    if (finalCommand.equals("clear") && mResult != null) {
      clearAll();
      return;
    }

    // Command to exit the app
    if (finalCommand.equals("exit")) {
      confirmExitDialog();
      return;
    }

    // show warning for su command in non rooted methods
    if (finalCommand.startsWith("su") && (isShizukuMode() || isBasicMode())) {
      suWarning();
      return;
    }

    // Initialize mHistory if necessary
    if (mHistory == null) mHistory = new ArrayList<>();

    mHistory.add(finalCommand);

    // Hide buttons and update send button
    binding.saveButton.hide();
    binding.shareButton.hide();
    viewModel.setSendDrawable(ic_stop);
    binding.sendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_stop, requireActivity()));
    binding.sendButton.setColorFilter(
        Utils.androidVersion() >= Build.VERSION_CODES.S
            ? ThemeUtils.colorError(context)
            : Utils.getColor(R.color.red, context));

    // Determine shell type
    if (isBasicMode()) shell = "\">BasicShell@";
    else if (isShizukuMode()) shell = "\">ShizukuShell@";
    else if (isRootMode()) shell = "\">RootShell@";

    // Create mTitleText and add it to mResult
    if (mResult == null) mResult = new ArrayList<>();

    String mTitleText =
        "<font color=\""
            + Utils.getColor(
                Utils.androidVersion() >= Build.VERSION_CODES.S
                    ? android.R.color.system_accent1_500
                    : R.color.blue,
                requireActivity())
            + shell
            + Utils.getDeviceName()
            + " | "
            + "</font><font color=\""
            + Utils.getColor(
                Utils.androidVersion() >= Build.VERSION_CODES.S
                    ? android.R.color.system_accent3_500
                    : R.color.green,
                requireActivity())
            + "\"> # "
            + finalCommand;
    mResult.add(mTitleText);

    // Execute the shell command in a background thread
    ExecutorService mExecutors = Executors.newSingleThreadExecutor();
    mExecutors.execute(
        () -> {
          switch (Preferences.getLocalAdbMode(context)) {
            case Preferences.BASIC_MODE:
              runBasicShell(finalCommand);
              break;
            case Preferences.SHIZUKU_MODE:
              runWithShizuku(finalCommand);
              break;
            case Preferences.ROOT_MODE:
              runWithRoot(finalCommand);
              break;
            default:
              return;
          }

          // Post UI updates back to the main thread
          new Handler(Looper.getMainLooper())
              .post(
                  () -> {
                    if (!isAdded()) return;

                    postExec();

                    // Update send button based on command text presence
                    if (!hasTextInEditText()) {
                      viewModel.setSendDrawable(ic_help);
                      binding.sendButton.setImageDrawable(
                          Utils.getDrawable(R.drawable.ic_help, requireActivity()));
                      binding.sendButton.clearColorFilter();
                    } else {
                      viewModel.setSendDrawable(ic_send);
                      binding.sendButton.setImageDrawable(
                          Utils.getDrawable(R.drawable.ic_send, requireActivity()));
                      binding.sendButton.clearColorFilter();
                    }

                    // Unlock the screen orientation
                    requireActivity()
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

                    // Ensure focus is back on the command input
                    if (!binding.commandEditText.isFocused())
                      binding.commandEditText.requestFocus();
                  });

          // Shutdown the executor service
          if (!mExecutors.isShutdown()) mExecutors.shutdown();
        });
  }

  // Method to run commands using root
  private void runBasicShell(String finalCommand) {
    mPosition = mResult.size();
    mBasicShell = new BasicShell(mResult, finalCommand);
    BasicShell.exec();
    try {
      TimeUnit.MILLISECONDS.sleep(250);
    } catch (InterruptedException ignored) {
    }
  }

  // Method to run commands using Shizuku
  private void runWithShizuku(String finalCommand) {
    mPosition = mResult.size();
    mShizukuShell = new ShizukuShell(mResult, finalCommand);
    mShizukuShell.exec();
    try {
      TimeUnit.MILLISECONDS.sleep(250);
    } catch (InterruptedException ignored) {
    }
  }

  // Method to run commands using root
  private void runWithRoot(String finalCommand) {
    mPosition = mResult.size();
    mRootShell = new RootShell(mResult, finalCommand);
    RootShell.exec();
    try {
      TimeUnit.MILLISECONDS.sleep(500);
    } catch (InterruptedException ignored) {
    }
  }

  // Call this post command execution
  private void postExec() {
    if (mResult != null && !mResult.isEmpty()) {
      mResult.add(Utils.shellDeadError());
      if (!isKeyboardVisible) {
        binding.saveButton.show();
        binding.shareButton.show();
      }
    }
  }

  // boolean that checks if the current set mode is basic mode
  private boolean isBasicMode() {
    return Preferences.getLocalAdbMode(context) == Preferences.BASIC_MODE;
  }

  // boolean that checks if the current set mode is shizuku mode
  private boolean isShizukuMode() {
    return Preferences.getLocalAdbMode(context) == Preferences.SHIZUKU_MODE;
  }

  // boolean that checks if the current set mode is root mode
  private boolean isRootMode() {
    return Preferences.getLocalAdbMode(context) == Preferences.ROOT_MODE;
  }

  // This methods checks if there is valid text in the edit text
  private boolean hasTextInEditText() {
    return binding.commandEditText.getText() != null
        && !binding.commandEditText.getText().toString().trim().isEmpty();
  }

  // Call this method to abort or stop running shell command
  public void abortBasicShell() {
    BasicShell.destroy();
    viewModel.setSendDrawable(ic_help);
    binding.sendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_help, requireActivity()));
    binding.sendButton.clearColorFilter();
  }

  // Call this method to abort or stop running shizuku command
  public void abortShizukuShell() {
    if (mShizukuShell != null) mShizukuShell.destroy();
    viewModel.setSendDrawable(ic_help);
    binding.sendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_help, requireActivity()));
    binding.sendButton.clearColorFilter();
  }

  // Call this method to abort or stop running root command
  public void abortRootShell() {
    RootShell.destroy();
    viewModel.setSendDrawable(ic_help);
    binding.sendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_help, requireActivity()));
    binding.sendButton.clearColorFilter();
  }

  // show shell working dialog
  private void shellWorkingDialog() {
    new MaterialAlertDialogBuilder(requireActivity())
        .setCancelable(false)
        .setTitle(getString(R.string.shell_working))
        .setMessage(getString(R.string.app_working_message))
        .setPositiveButton(getString(R.string.cancel), null)
        .show();
  }

  // asks confirmation dialog before exiting the app
  private void confirmExitDialog() {
    new MaterialAlertDialogBuilder(requireActivity())
        .setCancelable(false)
        .setTitle(R.string.confirm_exit)
        .setMessage(getString(R.string.quit_app_message))
        .setNegativeButton(getString(R.string.cancel), null)
        .setPositiveButton(
            getString(R.string.quit), (dialogInterface, i) -> requireActivity().finish())
        .show();
  }

  // error handling when shizuku is unavailable
  private void handleShizukuUnavailability() {
    binding.commandInputLayout.setError(getString(R.string.shizuku_unavailable));
    if (binding.commandEditText.getText() != null) {
      binding.commandInputLayout.setErrorIconDrawable(
          Utils.getDrawable(R.drawable.ic_cancel, requireActivity()));
      binding.commandInputLayout.setErrorIconOnClickListener(
          t -> binding.commandEditText.setText(null));
    }
    Utils.alignMargin(binding.sendButton);

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.warning))
        .setMessage(getString(R.string.shizuku_unavailable_message))
        .setNegativeButton(
            getString(R.string.shizuku_about),
            (dialogInterface, i) -> Utils.openUrl(context, "https://shizuku.rikka.app/"))
        .setPositiveButton(getString(R.string.ok), null)
        .show();
  }

  // error handling when root is unavailable
  private void handleRootUnavailability() {
    binding.commandInputLayout.setError(getString(R.string.root_unavailable));
    if (binding.commandEditText.getText() != null) {
      binding.commandInputLayout.setErrorIconDrawable(
          Utils.getDrawable(R.drawable.ic_cancel, requireActivity()));
      binding.commandInputLayout.setErrorIconOnClickListener(
          t -> binding.commandEditText.setText(null));
    }
    Utils.alignMargin(binding.sendButton);

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.warning))
        .setMessage(getString(R.string.root_unavailable_message))
        .setPositiveButton(getString(R.string.ok), null)
        .show();
  }

  // Show warning when running su commands with shizuku
  private void suWarning() {
    binding.commandInputLayout.setError(getString(R.string.su_warning));
    binding.commandInputLayout.setErrorIconDrawable(
        Utils.getDrawable(R.drawable.ic_error, requireActivity()));
    Utils.alignMargin(binding.sendButton);
    binding.commandEditText.requestFocus();
    Utils.snackBar(
            requireActivity().findViewById(android.R.id.content),
            getString(R.string.su_warning_message))
        .show();
  }

  // Open command examples fragment
  private void goToExamples() {

    examplesViewModel.setRVPositionAndOffset(null);
    examplesViewModel.setToolbarExpanded(true);

    setExitTransition(new Hold());
    ExamplesFragment fragment = new ExamplesFragment();

    requireActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .addSharedElement(binding.sendButton, "sendButtonToExamples")
        .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
        .addToBackStack(fragment.getClass().getSimpleName())
        .commit();
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
        .addSharedElement(binding.settingsButton, "settingsButtonToSettings")
        .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
        .addToBackStack(fragment.getClass().getSimpleName())
        .commit();
  }

  // Get the command history
  private List<String> getHistory() {
    if (mHistory != null) return mHistory;
    else if (viewModel.getHistory() != null) return viewModel.getHistory();

    return mHistory;
  }

  /* This method handles the text on the button and the text input layout hint in various cases */
  private void handleModeButtonTextAndCommandHint() {
    if (isBasicMode()) {
      binding.modeButton.setText("Basic shell");
      binding.commandInputLayout.setHint(R.string.command_title);
    } else if (isShizukuMode()) {
      binding.modeButton.setText("Shizuku");
      binding.commandInputLayout.setHint(R.string.command_title);
    } else if (isRootMode()) {
      binding.modeButton.setText("Root");
      binding.commandInputLayout.setHint(R.string.command_title_root);
    }
  }

  // Get the command when using Use feature
  private void handleUseCommand() {
    if (mainViewModel.getUseCommand() != null) {
      updateInputField(mainViewModel.getUseCommand());
      mainViewModel.setUseCommand(null);
    }
  }

  // initialize viewModels
  private void initializeViewModels() {
    viewModel = new ViewModelProvider(requireActivity()).get(AshellFragmentViewModel.class);
    mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
    examplesViewModel = new ViewModelProvider(requireActivity()).get(ExamplesViewModel.class);
  }

  // we refer the settings button view to use in activity
  public static View getSettingsButtonView() {
    return settingsButtonRef != null ? settingsButtonRef.get() : null;
  }

  // control visibility of paste and undo button
  private void pasteAndSaveButtonVisibility() {
    if (mResult != null || viewModel.getShellOutput() != null)
      binding.pasteButton.setVisibility(View.GONE);
    else binding.saveButton.setVisibility(View.VISIBLE);
  }
}
