package in.hridayan.ashell.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.BehaviorFAB;
import in.hridayan.ashell.UI.BehaviorFAB.FabExtendingOnScrollListener;
import in.hridayan.ashell.UI.BehaviorFAB.FabLocalScrollDownListener;
import in.hridayan.ashell.UI.BehaviorFAB.FabLocalScrollUpListener;
import in.hridayan.ashell.UI.KeyboardUtils;
import in.hridayan.ashell.UI.aShellFragmentViewModel;
import in.hridayan.ashell.activities.ExamplesActivity;
import in.hridayan.ashell.activities.MainActivity;
import in.hridayan.ashell.activities.SettingsActivity;
import in.hridayan.ashell.adapters.CommandsAdapter;
import in.hridayan.ashell.adapters.ShellOutputAdapter;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.RootShell;
import in.hridayan.ashell.utils.ShizukuShell;
import in.hridayan.ashell.utils.ThemeUtils;
import in.hridayan.ashell.utils.ToastUtils;
import in.hridayan.ashell.utils.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

public class aShellFragment extends Fragment {

  private AppCompatImageButton localShellSymbol;
  private ExtendedFloatingActionButton mSaveButton, mPasteButton;
  private FloatingActionButton mBottomButton, mSendButton, mTopButton, mShareButton, mUndoButton;
  private MaterialButton mClearButton, mHistoryButton, mSearchButton, mBookMarks, mSettingsButton;
  private FrameLayout mAppNameLayout;
  private BottomNavigationView mNav;
  private CommandsAdapter mCommandsAdapter;
  private ShellOutputAdapter mShellOutputAdapter;
  private RecyclerView mRecyclerViewOutput, mRecyclerViewCommands;
  private ShizukuShell mShizukuShell;
  private RootShell mRootShell;
  private TextInputLayout mCommandInput;
  private TextInputEditText mCommand, mSearchWord;
  private boolean isKeyboardVisible, sendButtonClicked = false, isEndIconVisible = false;
  private int mPosition = 1, sendDrawable;
  private final int ic_help = 10, ic_send = 11, ic_stop = 12;
  private List<String> mHistory = null, mResult = null, mRecentCommands, shellOutput, history;
  private View view;
  private Context context;
  private aShellFragmentViewModel viewModel;
  private Chip mChip;

  public aShellFragment() {}

  @Override
  public void onPause() {
    super.onPause();

    /*Since send button also works as help button, we need to keep track of what was its last mode(send or help) to get correct mode across configuration changes */
    viewModel.setSendDrawable(
        viewModel.isSendDrawableSaved() ? viewModel.getSendDrawable() : sendDrawable);
    viewModel.setSaveButtonVisible(isSaveButtonVisible());

    // Saves the viewing position of the recycler view
    viewModel.setScrollPosition(
        ((LinearLayoutManager) mRecyclerViewOutput.getLayoutManager())
            .findFirstVisibleItemPosition());

    // Gets the already saved output and command history from viewmodel in case no new output has
    // been made after config change
    List<String> shellOutput = viewModel.getShellOutput();
    List<String> history = viewModel.getHistory();
    viewModel.setHistory(mHistory == null && history != null ? history : mHistory);
    viewModel.setShellOutput(mResult == null ? shellOutput : mResult);

    // If there are some text in edit text, then we save it
    if (mCommand.getText().toString() != null) {
      viewModel.setCommandText(mCommand.getText().toString());
    }

    // Saves the visibility of the end icon of edit text
    if (mCommandInput.isEndIconVisible()) {
      viewModel.setEndIconVisible(true);
    } else {
      isEndIconVisible = false;
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    KeyboardUtils.disableKeyboard(context, requireActivity(), view);

    // If there is bookmarks present , make the bookmarks button visible

    // This function is for restoring the Run button's icon after a configuration change
    switch (viewModel.getSendDrawable()) {
      case ic_help:
        mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_help, requireActivity()));
        break;

      case ic_send:
        mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_send, requireActivity()));
        break;

      case ic_stop:
        mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_stop, requireActivity()));
        break;

      default:
        break;
    }

    // Handles save button visibility across config changes
    if (!viewModel.isSaveButtonVisible()) {
      mSaveButton.setVisibility(View.GONE);
    } else {
      mSaveButton.setVisibility(View.VISIBLE);
      if (mSearchWord.getVisibility() == View.GONE) {
        mClearButton.setVisibility(View.VISIBLE);
        mSearchButton.setVisibility(View.VISIBLE);
        mHistoryButton.setVisibility(View.VISIBLE);
      }
      mShareButton.setVisibility(View.VISIBLE);
      mPasteButton.setVisibility(View.GONE);
    }

    mRecyclerViewOutput = view.findViewById(R.id.recycler_view_output);
    mRecyclerViewOutput.setLayoutManager(new LinearLayoutManager(requireActivity()));

    // Get the scroll position of recycler view from viewmodel and set it
    int scrollPosition = viewModel.getScrollPosition();
    mRecyclerViewOutput.scrollToPosition(scrollPosition);

    isEndIconVisible = viewModel.isEndIconVisible();

    // If the end icon of edit text is visible then set its icon accordingly
    if (!mCommand.getText().toString().isEmpty() && isEndIconVisible) {
      mCommandInput.setEndIconDrawable(
          Utils.getDrawable(
              Utils.isBookmarked(mCommand.getText().toString(), requireActivity())
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
    if (mShizukuShell != null) {
      mShizukuShell.destroy();
    }
    if (mRootShell != null) {
      mRootShell.destroy();
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    context = requireContext();

    view = inflater.inflate(R.layout.fragment_ashell, container, false);

    localShellSymbol = view.findViewById(R.id.local_shell_symbol);
    mAppNameLayout = view.findViewById(R.id.app_name_layout);
    mBookMarks = view.findViewById(R.id.bookmarks);
    mBottomButton = view.findViewById(R.id.fab_down);
    mClearButton = view.findViewById(R.id.clear);
    mChip = view.findViewById(R.id.local_adb_chip);
    mPasteButton = view.findViewById(R.id.paste_button);
    mCommand = view.findViewById(R.id.shell_command);
    mCommandInput = view.findViewById(R.id.shell_command_layout);
    mNav = requireActivity().findViewById(R.id.bottom_nav_bar);
    mHistoryButton = view.findViewById(R.id.history);
    mRecyclerViewCommands = view.findViewById(R.id.recycler_view_commands);
    mRecyclerViewOutput = view.findViewById(R.id.recycler_view_output);
    mSaveButton = view.findViewById(R.id.save_button);
    mSearchButton = view.findViewById(R.id.search);
    mSearchWord = view.findViewById(R.id.search_word);
    mSendButton = view.findViewById(R.id.send);
    mSettingsButton = view.findViewById(R.id.settings);
    mShareButton = view.findViewById(R.id.fab_share);
    mTopButton = view.findViewById(R.id.fab_up);
    mUndoButton = view.findViewById(R.id.fab_undo);

    // Viewmodel initialization
    viewModel = new ViewModelProvider(requireActivity()).get(aShellFragmentViewModel.class);

    mRecyclerViewOutput.setLayoutManager(new LinearLayoutManager(requireActivity()));
    mRecyclerViewCommands.setLayoutManager(new LinearLayoutManager(requireActivity()));

    mRecyclerViewCommands.addOnScrollListener(new FabExtendingOnScrollListener(mPasteButton));
    mRecyclerViewOutput.addOnScrollListener(new FabExtendingOnScrollListener(mPasteButton));

    mRecyclerViewOutput.addOnScrollListener(new FabExtendingOnScrollListener(mSaveButton));
    mRecyclerViewOutput.addOnScrollListener(new FabLocalScrollUpListener(mTopButton));

    mRecyclerViewOutput.addOnScrollListener(new FabLocalScrollDownListener(mBottomButton));

    mRecyclerViewOutput.setAdapter(mShellOutputAdapter);

    setupRecyclerView();

    // Set the bottom navigation
    mNav.setVisibility(View.VISIBLE);

    // Paste and undo button onClickListener
    BehaviorFAB.pasteAndUndo(mPasteButton, mUndoButton, mCommand, context);

    // Toggles certain buttons visibility according to keyboard's visibility
    KeyboardUtils.attachVisibilityListener(
        requireActivity(),
        visible -> {
          isKeyboardVisible = visible;
          if (visible) buttonsVisibilityGone();
          else buttonsVisibilityVisible();
        });

    // Handles the onclick listener of the top and bottom scrolling arrows
    BehaviorFAB.handleTopAndBottomArrow(
        mTopButton, mBottomButton, mRecyclerViewOutput, null, context, "local_shell");

    // Display the connected device name when clicking the chip
    Utils.chipOnClickListener(context, mChip, Utils.getDeviceName());
    /*------------------------------------------------------*/

    // When there is any text in edit text , focus the edit text
    if (!mCommand.getText().toString().isEmpty()) {
      mCommand.requestFocus();
    }

    // Handles text changing events in the Input Field
    mCommand.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            mCommandInput.setError(null);
          }

          @SuppressLint("SetTextI18n")
          @Override
          public void afterTextChanged(Editable s) {

            mCommand.requestFocus();

            // If shizuku is busy return
            if (mShizukuShell != null && mShizukuShell.isBusy()) {
              return;
            } else if (s.toString().trim().isEmpty()) {

              mCommandInput.setEndIconVisible(false);
              mRecyclerViewCommands.setVisibility(View.GONE);
              viewModel.setSendDrawable(ic_help);
              mSendButton.setImageDrawable(
                  Utils.getDrawable(R.drawable.ic_help, requireActivity()));
              mSendButton.clearColorFilter();
            } else {
              viewModel.setSendDrawable(ic_send);
              mSendButton.setImageDrawable(
                  Utils.getDrawable(R.drawable.ic_send, requireActivity()));
              mCommandInput.setEndIconDrawable(
                  Utils.getDrawable(
                      Utils.isBookmarked(s.toString().trim(), requireActivity())
                          ? R.drawable.ic_bookmark_added
                          : R.drawable.ic_add_bookmark,
                      requireActivity()));

              mCommandInput.setEndIconVisible(true);

              mCommandInputEndIconOnClickListener(s);

              commandSuggestion(s);
            }
          }
        });

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

    mCommandOnEditorActionListener();

    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleWithFixedDelay(
        () -> {
          if (mResult != null
              && !mResult.isEmpty()
              && !mResult.get(mResult.size() - 1).equals("Shell is dead")) {
            updateUI(mResult);
          }
        },
        0,
        250,
        TimeUnit.MILLISECONDS);

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

  private void initializeShell(Activity activity) {
    if (mCommand.getText() == null || mCommand.getText().toString().trim().isEmpty()) {
      return;
    }
    if (mShizukuShell != null && mShizukuShell.isBusy()) {
      new MaterialAlertDialogBuilder(activity)
          .setCancelable(false)
          .setTitle(getString(R.string.shell_working))
          .setMessage(getString(R.string.app_working_message))
          .setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {})
          .show();
      return;
    }

    if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
      runShellCommand(mCommand.getText().toString().replace("\n", ""), activity);
    } else {
      new MaterialAlertDialogBuilder(activity)
          .setCancelable(false)
          .setTitle(getString(R.string.access_denied))
          .setMessage(getString(R.string.shizuku_access_denied_message))
          .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {})
          .setPositiveButton(
              getString(R.string.request_permission),
              (dialogInterface, i) -> Shizuku.requestPermission(0))
          .show();
    }
  }

  // This function is called when we want to run the shell after entering an adb command
  private void runShellCommand(String command, Activity activity) {

    if (!isAdded()) {
      return;
    }
    if (mRecyclerViewOutput.getAdapter() == null) {
      mRecyclerViewOutput.setAdapter(mShellOutputAdapter);
    }

    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

    mCommand.setText(null);
    mCommand.clearFocus();
    if (mSearchWord.getVisibility() == View.VISIBLE) {
      hideSearchBar();
    }

    String finalCommand = command.replaceAll("^adb(?:\\s+-d)?\\s+shell\\s+", "");

    if (finalCommand.equals("clear") && mResult != null) {
      clearAll();
      return;
    }

    // Fun Commands
    if (finalCommand.equals("goto top") && mResult != null) {
      mRecyclerViewOutput.scrollToPosition(0);
      return;
    }
    if (finalCommand.equals("goto bottom") && mResult != null) {
      mRecyclerViewOutput.scrollToPosition(
          Objects.requireNonNull(mRecyclerViewOutput.getAdapter()).getItemCount() - 1);
      return;
    }

    if (finalCommand.equals("exit")) {
      new MaterialAlertDialogBuilder(activity)
          .setCancelable(false)
          .setTitle(R.string.confirm_exit)
          .setMessage(getString(R.string.quit_app_message))
          .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {})
          .setPositiveButton(getString(R.string.quit), (dialogInterface, i) -> activity.finish())
          .show();
      return;
    }

    // Shizuku mode doesn't allow su commands , so we show a warning
    if (finalCommand.startsWith("su") && Preferences.getLocalAdbMode(context) == Preferences.SHIZUKU_MODE) {
      mCommandInput.setError(getString(R.string.su_warning));
      mCommandInput.setErrorIconDrawable(Utils.getDrawable(R.drawable.ic_error, requireActivity()));
      Utils.alignMargin(mSendButton);
      Utils.alignMargin(localShellSymbol);
      mCommand.requestFocus();
      Utils.snackBar(
              activity.findViewById(android.R.id.content), getString(R.string.su_warning_message))
          .show();

      return;
    }

    // If history is null then create a new list and add the final command
    if (mHistory == null) {
      mHistory = new ArrayList<>();
    }
    mHistory.add(finalCommand);

    mSaveButton.hide();
    mShareButton.hide();
    viewModel.setSendDrawable(ic_stop);
    mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_stop, requireActivity()));

    mSendButton.setColorFilter(
        Utils.androidVersion() >= Build.VERSION_CODES.S
            ? ThemeUtils.colorError(context)
            : Utils.getColor(R.color.red, context));

    String mTitleText =
        "<font color=\""
            + Utils.getColor(
                Utils.androidVersion() >= Build.VERSION_CODES.S
                    ? android.R.color.system_accent1_500
                    : R.color.blue,
                activity)
            + "\">shell@"
            + Utils.getDeviceName()
            + " | "
            + "</font><font color=\""
            + Utils.getColor(
                Utils.androidVersion() >= Build.VERSION_CODES.S
                    ? android.R.color.system_accent3_500
                    : R.color.green,
                activity)
            + "\"> # "
            + finalCommand;

    if (mResult == null) {
      mResult = new ArrayList<>();
    }
    mResult.add(mTitleText);

    /*------------------------------------------------------*/

    ExecutorService mExecutors = Executors.newSingleThreadExecutor();
    mExecutors.execute(
        () -> {
          switch (Preferences.getLocalAdbMode(context)) {
            case Preferences.SHIZUKU_MODE:
              runWithShizuku(finalCommand);
              break;

            case Preferences.ROOT_MODE:
              runWithRoot(finalCommand);
              break;

            default:
              return;
          }

          new Handler(Looper.getMainLooper())
              .post(
                  () -> {
                    if (mResult != null && !mResult.isEmpty()) {
                      mResult.add("<i></i>");
                      mResult.add("Shell is dead");
                      if (!isKeyboardVisible) {
                        mSaveButton.show();
                        mShareButton.show();
                      }
                    }

                    // Handles sendButton icon changes
                    if (mCommand.getText() == null
                        || mCommand.getText().toString().trim().isEmpty()) {
                      viewModel.setSendDrawable(ic_help);
                      mSendButton.setImageDrawable(
                          Utils.getDrawable(R.drawable.ic_help, requireActivity()));
                      mSendButton.clearColorFilter();
                    } else {
                      viewModel.setSendDrawable(ic_send);
                      mSendButton.setImageDrawable(
                          Utils.getDrawable(R.drawable.ic_send, requireActivity()));
                      mSendButton.clearColorFilter();
                    }

                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    if (!mCommand.isFocused()) mCommand.requestFocus();
                  });

          if (!mExecutors.isShutdown()) mExecutors.shutdown();
        });
  }

  private void updateUI(List<String> data) {
    if (data == null) {
      return;
    }

    List<String> mData = new ArrayList<>();
    try {
      for (String result : data) {
        if (!TextUtils.isEmpty(result) && !result.equals("Shell is dead")) {
          mData.add(result);
        }
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
                    if (isAdded() && mRecyclerViewOutput != null) {
                      mRecyclerViewOutput.setAdapter(mShellOutputAdapter);
                      mRecyclerViewOutput.scrollToPosition(mData.size() - 1);
                    }
                  });

          if (!mExecutors.isShutdown()) mExecutors.shutdown();
        });
  }

  // This function is called when we want to clear the screen
  private void clearAll() {
    if (mShizukuShell != null) mShizukuShell.destroy();
    mResult = null;
    if (mTopButton.getVisibility() == View.VISIBLE) {
      mTopButton.setVisibility(View.GONE);
    }
    if (mBottomButton.getVisibility() == View.VISIBLE) {
      mBottomButton.setVisibility(View.GONE);
    }

    mRecyclerViewOutput.setAdapter(null);
    mSaveButton.setVisibility(View.GONE);
    mShareButton.setVisibility(View.GONE);
    showBottomNav();
    mCommand.clearFocus();
    if (!mCommand.isFocused()) mCommand.requestFocus();
  }

  private void hideSearchBar() {
    mSearchWord.setText(null);
    mSearchWord.setVisibility(View.GONE);
    if (!mCommand.isFocused()) mCommand.requestFocus();
    mBookMarks.setVisibility(View.VISIBLE);
    mSettingsButton.setVisibility(View.VISIBLE);
    mHistoryButton.setVisibility(View.VISIBLE);
    mClearButton.setVisibility(View.VISIBLE);
    mSearchButton.setVisibility(View.VISIBLE);
  }

  // Call to show the bottom navigation view
  private void showBottomNav() {
    if (getActivity() != null && getActivity() instanceof MainActivity) {
      ((MainActivity) getActivity()).mNav.animate().translationY(0);
    }
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
        if (!Shizuku.pingBinder()) {
          handleShizukuAvailability(context);
        } else {
          mCommand.setText(sharedText);
          initializeShell(requireActivity());
        }
      }
    }
  }

  // Call to update the edit Text with a text
  public void updateInputField(String text) {
    if (text != null) {
      mCommand.setText(text);
      mCommand.requestFocus();
      mCommand.setSelection(mCommand.getText().length());
      viewModel.setSendDrawable(ic_send);
      mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_send, requireActivity()));
      viewModel.setSendDrawable(ic_send);
    }
  }

  // error handling when shizuku is unavailable or permission isn't granted
  private void handleShizukuAvailability(Context context) {

    mCommandInput.setError(getString(R.string.shizuku_unavailable));
    if (mCommand.getText() != null) {
      mCommandInput.setErrorIconDrawable(
          Utils.getDrawable(R.drawable.ic_cancel, requireActivity()));
      mCommandInput.setErrorIconOnClickListener(t -> mCommand.setText(null));
    }
    Utils.alignMargin(mSendButton);
    Utils.alignMargin(localShellSymbol);

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(getString(R.string.warning))
        .setMessage(getString(R.string.shizuku_unavailable_message))
        .setNegativeButton(
            getString(R.string.shizuku_about),
            (dialogInterface, i) -> Utils.openUrl(context, "https://shizuku.rikka.app/"))
        .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {})
        .show();
  }

  // Boolean that returns the visibility of Save button
  private boolean isSaveButtonVisible() {
    return mSaveButton.getVisibility() == View.VISIBLE;
  }

  // Setup the recycler view
  private void setupRecyclerView() {
    mRecyclerViewOutput = view.findViewById(R.id.recycler_view_output);
    mRecyclerViewOutput.setLayoutManager(new LinearLayoutManager(requireActivity()));

    List<String> shellOutput = viewModel.getShellOutput();
    int scrollPosition = viewModel.getScrollPosition();
    if (shellOutput != null) {
      mShellOutputAdapter = new ShellOutputAdapter(shellOutput);
      mResult = shellOutput;
    }
    mRecyclerViewOutput.setAdapter(mShellOutputAdapter);
    mRecyclerViewOutput.scrollToPosition(scrollPosition);
    String mCommandText = viewModel.getCommandText();
    if (mCommandText != null) {
      mCommand.setText(mCommandText);
    }
  }

  // Call to initialize the shell output and command history
  private void initializeResults() {
    if (mResult == null) {
      mResult = shellOutput;
    }
    if (mHistory == null) {
      mHistory = history;
    }
  }

  private StringBuilder buildResultsString() {
    StringBuilder sb = new StringBuilder();
    for (int i = mPosition; i < mResult.size(); i++) {
      String result = mResult.get(i);
      if (!"Shell is dead".equals(result) && !"<i></i>".equals(result)) {
        sb.append(result).append("\n");
      }
    }
    return sb;
  }

  // Hide buttons when keyboard is visible
  private void buttonsVisibilityGone() {
    mPasteButton.setVisibility(View.GONE);
    mUndoButton.setVisibility(View.GONE);
    mSaveButton.setVisibility(View.GONE);
    mShareButton.setVisibility(View.GONE);
  }

  // Show buttons again when keyboard is gone
  private void buttonsVisibilityVisible() {
    if (mRecyclerViewOutput.getHeight() != 0) {
      setVisibilityWithDelay(mSaveButton, 100);
    }
    if (mShareButton.getVisibility() == View.GONE && mRecyclerViewOutput.getHeight() != 0) {
      setVisibilityWithDelay(mShareButton, 100);
    }

    if (mPasteButton.getVisibility() == View.GONE && !sendButtonClicked && mResult == null) {
      setVisibilityWithDelay(mPasteButton, 100);
    }
  }

  // Send button onclick listener
  private void sendButtonOnClickListener() {
    mSendButton.setOnClickListener(
        v -> {
          sendButtonClicked = true;
          HapticUtils.weakVibrate(v, context);

          if (mCommand.getText() == null || mCommand.getText().toString().trim().isEmpty()) {
            Intent examples = new Intent(requireActivity(), ExamplesActivity.class);
            startActivity(examples);
            return;
          }

          if (Preferences.getLocalAdbMode(context) == Preferences.SHIZUKU_MODE) {
            if (!Shizuku.pingBinder()) {
              handleShizukuAvailability(context);
            } else if (mShizukuShell != null && mShizukuShell.isBusy()) {
              mShizukuShell.destroy();
              viewModel.setSendDrawable(ic_help);
              mSendButton.setImageDrawable(
                  Utils.getDrawable(R.drawable.ic_help, requireActivity()));
              mSendButton.clearColorFilter();

            } else {
              mPasteButton.hide();
              mUndoButton.hide();

              if (isAdded()) {
                mCommandInput.setError(null);
                initializeShell(requireActivity());
                KeyboardUtils.closeKeyboard(requireActivity(), v);
              }
            }
          }
          if (Preferences.getLocalAdbMode(context) == Preferences.ROOT_MODE) {
            if (!RootShell.isDeviceRooted()) {
              handleShizukuAvailability(context);
            } else if (RootShell.isBusy()) {
              mRootShell.destroy();
              viewModel.setSendDrawable(ic_help);
              mSendButton.setImageDrawable(
                  Utils.getDrawable(R.drawable.ic_help, requireActivity()));
              mSendButton.clearColorFilter();
            } else {

              mPasteButton.hide();
              mUndoButton.hide();

              if (isAdded()) {
                mCommandInput.setError(null);
                initializeShell(requireActivity());
                KeyboardUtils.closeKeyboard(requireActivity(), v);
              }
            }
          }
        });
  }

  // OnClick listener for the settings button
  private void settingsButtonOnClickListener() {
    mSettingsButton.setTooltipText(getString(R.string.settings));

    mSettingsButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          Intent settingsIntent = new Intent(requireActivity(), SettingsActivity.class);
          startActivity(settingsIntent);
        });
  }

  // Onclick listener for the clear button
  private void clearButtonOnClickListener() {
    mClearButton.setTooltipText(getString(R.string.clear_screen));

    mClearButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          if (mResult == null || mResult.isEmpty()) {
            ToastUtils.showToast(context, R.string.nothing_to_clear, ToastUtils.LENGTH_SHORT);
          } else {
            viewModel.setShellOutput(null);
            boolean switchState = Preferences.getClear(context);
            if (switchState) {
              new MaterialAlertDialogBuilder(requireActivity())
                  .setTitle(getString(R.string.clear_everything))
                  .setMessage(getString(R.string.clear_all_message))
                  .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {})
                  .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> clearAll())
                  .show();
            } else {
              clearAll();
            }
          }
        });
  }

  // OnClick listener for bookmarks button
  private void bookmarksButtonOnClickListener() {
    mBookMarks.setTooltipText(getString(R.string.bookmarks));
    mBookMarks.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);

          if (Utils.getBookmarks(requireActivity()).isEmpty()) {
            ToastUtils.showToast(context, R.string.no_bookmarks, ToastUtils.LENGTH_SHORT);
          } else {
            Utils.bookmarksDialog(context, requireActivity(), mCommand, mCommandInput);
          }
        });
  }

  // OnClick listener for the history button
  private void historyButtonOnClickListener() {
    mHistoryButton.setTooltipText(getString(R.string.history));

    mHistoryButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);

          if (mHistory == null || mHistory.isEmpty()) {
            ToastUtils.showToast(context, R.string.no_history, ToastUtils.LENGTH_SHORT);
          } else {
            PopupMenu popupMenu = new PopupMenu(context, mCommand);
            Menu menu = popupMenu.getMenu();
            for (int i = 0; i < getRecentCommands().size(); i++) {
              menu.add(Menu.NONE, i, Menu.NONE, getRecentCommands().get(i));
            }
            popupMenu.setOnMenuItemClickListener(
                item -> {
                  for (int i = 0; i < getRecentCommands().size(); i++) {
                    if (item.getItemId() == i) {
                      mCommand.setText(getRecentCommands().get(i));
                      mCommand.setSelection(mCommand.getText().length());
                    }
                  }
                  return false;
                });
            popupMenu.show();
          }
        });
  }

  // OnClick listener for the search button
  private void searchButtonOnClickListener() {
    mSearchButton.setTooltipText(getString(R.string.search));

    mSearchButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);

          if (mResult == null || mResult.isEmpty()) {
            ToastUtils.showToast(context, R.string.nothing_to_search, ToastUtils.LENGTH_SHORT);
          } else if (mShizukuShell.isBusy()) {
            ToastUtils.showToast(context, R.string.abort_command, ToastUtils.LENGTH_SHORT);
          } else {
            mHistoryButton.setVisibility(View.GONE);
            mClearButton.setVisibility(View.GONE);
            mBookMarks.setVisibility(View.GONE);
            mSettingsButton.setVisibility(View.GONE);
            mSearchButton.setVisibility(View.GONE);
            mSearchWord.setVisibility(View.VISIBLE);
            mSearchWord.requestFocus();
            mCommand.setText(null);
          }
        });
  }

  // Logic for searching text in the output
  private void searchWordChangeListener() {
    mSearchWord.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (s == null || s.toString().trim().isEmpty()) {
              updateUI(mResult);
            } else {
              List<String> mResultSorted = new ArrayList<>();
              for (int i = mPosition; i < mResult.size(); i++) {
                if (mResult
                    .get(i)
                    .toLowerCase(Locale.getDefault())
                    .contains(s.toString().toLowerCase(Locale.getDefault()))) {
                  mResultSorted.add(mResult.get(i));
                }
              }
              updateUI(mResultSorted);
            }
          }
        });
  }

  // OnClick listener for save Button
  private void saveButtonOnClickListener() {

    mSaveButton.setOnClickListener(
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
          if (saved) {
            Preferences.setLastSavedFileName(context, fileName + ".txt");
          }

          // Dialog showing if the output has been saved or not
          Utils.outputSavedDialog(requireActivity(), context, saved);
        });
  }

  // Onclick listener for share button
  private void shareButtonOnClickListener() {
    mShareButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          shellOutput = viewModel.getShellOutput();
          history = viewModel.getHistory();
          initializeResults();

          StringBuilder sb = new StringBuilder();
          for (int i = mPosition; i < mResult.size(); i++) {
            String result = mResult.get(i);
            if (!"Shell is dead".equals(result) && !"<i></i>".equals(result)) {
              sb.append(result).append("\n");
            }
          }
          String fileName = Utils.generateFileName(mHistory);
          Utils.shareOutput(requireActivity(), context, fileName, sb.toString());
        });
  }

  // Logic to hide and show share button
  private void shareButtonVisibilityHandler() {
    mRecyclerViewOutput.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          private final Handler handler = new Handler(Looper.getMainLooper());
          private final int delayMillis = 1600;

          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {

              handler.postDelayed(
                  () -> {
                    if (!isKeyboardVisible) mShareButton.show();
                  },
                  delayMillis);
            } else {
              handler.removeCallbacksAndMessages(null);
            }
          }

          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (dy > 0 || dy < 0 && mShareButton.isShown()) {
              if (Math.abs(dy) >= 90) mShareButton.hide();
            }
          }
        });
  }

  // To dismiss the search I have not found other way than add an onclick listener on the app name
  // layout itself
  private void appNameLayoutOnClickListener() {
    mAppNameLayout.setOnClickListener(
        v -> {
          if (mSearchWord.getVisibility() == View.VISIBLE) {
            hideSearchBar();
          }
        });
  }

  // mCommand on editor action listener
  private void mCommandOnEditorActionListener() {
    mCommand.setOnEditorActionListener(
        (v, actionId, event) -> {
          HapticUtils.weakVibrate(v, context);
          if (actionId == EditorInfo.IME_ACTION_SEND) {
            if (mShizukuShell != null && mShizukuShell.isBusy()) {
              mShizukuShell.destroy();
              viewModel.setSendDrawable(ic_help);
              mSendButton.setImageDrawable(
                  Utils.getDrawable(R.drawable.ic_help, requireActivity()));
              mSendButton.clearColorFilter();
            } else if (mCommand.getText() == null
                || mCommand.getText().toString().trim().isEmpty()) {
              Intent examples = new Intent(requireActivity(), ExamplesActivity.class);
              startActivity(examples);
            } else {
              if (isAdded()) {
                initializeShell(requireActivity());
              }
            }
            return true;
          }
          return false;
        });
  }

  // The edit text end icon which is responsible for adding /removing bookmarks
  private void mCommandInputEndIconOnClickListener(Editable s) {
    mCommandInput.setEndIconOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          if (Utils.isBookmarked(s.toString().trim(), requireActivity())) {
            Utils.deleteFromBookmark(s.toString().trim(), requireActivity());
            Utils.snackBar(view, getString(R.string.bookmark_removed_message, s.toString().trim()))
                .show();
          } else {
            Utils.addBookmarkIconOnClickListener(s.toString().trim(), view, context);
          }

          mCommandInput.setEndIconDrawable(
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
                if (splitCommands[0].contains(" ")) {
                  packageNamePrefix = splitPrefix(splitCommands[0], 1);
                } else {
                  packageNamePrefix = splitCommands[0];
                }

                mCommandsAdapter =
                    new CommandsAdapter(Commands.getPackageInfo(packageNamePrefix + ".", context));
                if (isAdded()) {
                  mRecyclerViewCommands.setLayoutManager(
                      new LinearLayoutManager(requireActivity()));
                }

                if (isAdded()) {
                  mRecyclerViewCommands.setAdapter(mCommandsAdapter);
                }
                mRecyclerViewCommands.setVisibility(View.VISIBLE);
                mCommandsAdapter.setOnItemClickListener(
                    (command, v) -> {
                      mCommand.setText(
                          splitCommands[0].contains(" ")
                              ? splitPrefix(splitCommands[0], 0) + " " + command
                              : command);
                      mCommand.setSelection(mCommand.getText().length());
                      mRecyclerViewCommands.setVisibility(View.GONE);
                    });
              } else {
                mCommandsAdapter = new CommandsAdapter(Commands.getCommand(s.toString(), context));
                if (isAdded()) {
                  mRecyclerViewCommands.setLayoutManager(
                      new LinearLayoutManager(requireActivity()));
                }

                mRecyclerViewCommands.setAdapter(mCommandsAdapter);
                mRecyclerViewCommands.setVisibility(View.VISIBLE);
                mCommandsAdapter.setOnItemClickListener(
                    (command, v) -> {
                      if (command.contains(" <")) {
                        mCommand.setText(command.split("<")[0]);
                      } else {
                        mCommand.setText(command);
                      }
                      mCommand.setSelection(mCommand.getText().length());
                    });
              }
            });
  }

  // Method to run commands using Shizuku
  private void runWithShizuku(String finalCommand) {

    if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
      mPosition = mResult.size();
      mShizukuShell = new ShizukuShell(mResult, finalCommand);
      mShizukuShell.exec();
      try {
        TimeUnit.MILLISECONDS.sleep(250);
      } catch (InterruptedException ignored) {
      }
    }

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {

                if (mResult != null && !mResult.isEmpty()) {
                  mResult.add("<i></i>");
                  mResult.add("Shell is dead");
                  if (!isKeyboardVisible) {
                    mSaveButton.show();
                    mShareButton.show();
                  }
                }
              } else {
                new MaterialAlertDialogBuilder(requireActivity())
                    .setCancelable(false)
                    .setTitle(getString(R.string.access_denied))
                    .setMessage(getString(R.string.shizuku_access_denied_message))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {})
                    .setPositiveButton(
                        getString(R.string.request_permission),
                        (dialogInterface, i) -> Shizuku.requestPermission(0))
                    .show();
              }
            });
  }

  // Method to run commands using root
  private void runWithRoot(String finalCommand) {

    if (RootShell.checkRoot()) {
      mPosition = mResult.size();
      mRootShell = new RootShell(mResult, finalCommand);
      mRootShell.exec();
      try {
        TimeUnit.MILLISECONDS.sleep(250);
      } catch (InterruptedException ignored) {
      }
    }

    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              if (RootShell.checkRoot()) {
                if (mResult != null && !mResult.isEmpty()) {
                  mResult.add("<i></i>");
                  mResult.add("Shell is dead");
                  if (!isKeyboardVisible) {
                    mSaveButton.show();
                    mShareButton.show();
                  }
                }
              } else {
                new MaterialAlertDialogBuilder(requireActivity())
                    .setCancelable(false)
                    .setTitle(getString(R.string.access_denied))
                    .setMessage(getString(R.string.shizuku_access_denied_message))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {})
                    .setPositiveButton(
                        getString(R.string.request_permission),
                        (dialogInterface, i) ->
                        {
                            RootShell.refresh();
                            RootShell.exec("su", true);
                            
                        } )
                    .show();
              }
            });
  }
}
