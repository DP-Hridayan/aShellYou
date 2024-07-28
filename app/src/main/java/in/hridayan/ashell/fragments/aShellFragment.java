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
import in.hridayan.ashell.utils.ShizukuShell;
import in.hridayan.ashell.utils.ThemeUtils;
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
  private TextInputLayout mCommandInput;
  private TextInputEditText mCommand, mSearchWord;
  private boolean isKeyboardVisible,
      sendButtonClicked = false,
      isEndIconVisible = false;
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

    viewModel.setSendDrawable(
        viewModel.isSendDrawableSaved() ? viewModel.getSendDrawable() : sendDrawable);

    viewModel.setEditTextFocused(isEditTextFocused());
    viewModel.setSaveButtonVisible(isSaveButtonVisible());
    viewModel.setScrollPosition(
        ((LinearLayoutManager) mRecyclerViewOutput.getLayoutManager())
            .findFirstVisibleItemPosition());
    List<String> shellOutput = viewModel.getShellOutput();
    List<String> history = viewModel.getHistory();
    viewModel.setHistory(mHistory == null && history != null ? history : mHistory);
    viewModel.setShellOutput(mResult == null ? shellOutput : mResult);

    if (mCommand.getText().toString() != null) {
      viewModel.setCommandText(mCommand.getText().toString());
    }

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

    mBookMarks.setVisibility(!Utils.getBookmarks(context).isEmpty() ? View.VISIBLE : View.GONE);

    if (viewModel.isEditTextFocused()) {
      mCommand.requestFocus();
    } else {
      mCommand.clearFocus();
    }

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

    if (viewModel.isSaveButtonVisible()) {
      mSaveButton.setVisibility(View.VISIBLE);
      if (mSearchWord.getVisibility() == View.GONE) {
        mClearButton.setVisibility(View.VISIBLE);
        mSearchButton.setVisibility(View.VISIBLE);
        mHistoryButton.setVisibility(View.VISIBLE);
      }
      mShareButton.setVisibility(View.VISIBLE);
      mPasteButton.setVisibility(View.GONE);
    } else {
      mSaveButton.setVisibility(View.GONE);
    }

    mRecyclerViewOutput = view.findViewById(R.id.recycler_view_output);
    mRecyclerViewOutput.setLayoutManager(new LinearLayoutManager(requireActivity()));

    int scrollPosition = viewModel.getScrollPosition();
    mRecyclerViewOutput.scrollToPosition(scrollPosition);

    isEndIconVisible = viewModel.isEndIconVisible();
    String s = mCommand.getText().toString().trim();

    if (!s.isEmpty() && isEndIconVisible) {
      mCommandInput.setEndIconDrawable(
          Utils.getDrawable(
              Utils.isBookmarked(s, requireActivity())
                  ? R.drawable.ic_bookmark_added
                  : R.drawable.ic_add_bookmark,
              requireActivity()));
    }

    MainActivity activity = (MainActivity) getActivity();
    if (activity != null) {
      String pendingSharedText = activity.getPendingSharedText();
      if (pendingSharedText != null) {
        updateInputField(pendingSharedText);
        activity.clearPendingSharedText();
      }
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    context = requireContext();

    view = inflater.inflate(R.layout.fragment_ashell, container, false);

    /*------------------------------------------------------*/

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
    /*------------------------------------------------------*/

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

    mNav.setVisibility(View.VISIBLE);

    /*------------------------------------------------------*/

    BehaviorFAB.pasteAndUndo(mPasteButton, mUndoButton, mCommand);

    KeyboardUtils.attachVisibilityListener(
        requireActivity(),
            visible -> {
              isKeyboardVisible = visible;
              if (isKeyboardVisible) {
                mPasteButton.setVisibility(View.GONE);
                mUndoButton.setVisibility(View.GONE);
                mSaveButton.setVisibility(View.GONE);
                mShareButton.setVisibility(View.GONE);

              } else {

                if (mRecyclerViewOutput.getHeight() != 0) {
                  setVisibilityWithDelay(mSaveButton, 100);
                }
                if (mShareButton.getVisibility() == View.GONE
                    && mRecyclerViewOutput.getHeight() != 0) {
                  setVisibilityWithDelay(mShareButton, 100);
                }

                if (mPasteButton.getVisibility() == View.GONE
                    && !sendButtonClicked
                    && mResult == null) {
                  setVisibilityWithDelay(mPasteButton, 100);
                }
              }
            });

    /*------------------------------------------------------*/

    BehaviorFAB.handleTopAndBottomArrow(
        mTopButton, mBottomButton, mRecyclerViewOutput, null, context, "local_shell");

    // Display the connected device name when clicking the chip
    Utils.chipOnClickListener(context, mChip, Utils.getDeviceName());
    /*------------------------------------------------------*/

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

            /*------------------------------------------------------*/
            mCommand.requestFocus();

            if (mShizukuShell != null && mShizukuShell.isBusy()) {
              return;
            } else {

              /*------------------------------------------------------*/

              if (!s.toString().trim().isEmpty()) {
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

                mCommandInput.setEndIconOnClickListener(
                    v -> {
                      if (Utils.isBookmarked(s.toString().trim(), requireActivity())) {
                        Utils.deleteFromBookmark(s.toString().trim(), requireActivity());
                        Utils.snackBar(
                                view,
                                getString(R.string.bookmark_removed_message, s.toString().trim()))
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
                      if (mSearchWord.getVisibility() == View.GONE) {
                        mBookMarks.setVisibility(
                                !Utils.getBookmarks(requireActivity()).isEmpty()
                                ? View.VISIBLE
                                : View.GONE);
                      }
                    });

                /*------------------------------------------------------*/

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
                                new CommandsAdapter(
                                    Commands.getPackageInfo(packageNamePrefix + ".", context));
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
                            mCommandsAdapter =
                                new CommandsAdapter(Commands.getCommand(s.toString(), context));
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
              } else {
                mCommandInput.setEndIconVisible(false);
                mRecyclerViewCommands.setVisibility(View.GONE);
                viewModel.setSendDrawable(ic_help);
                mSendButton.setImageDrawable(
                    Utils.getDrawable(R.drawable.ic_help, requireActivity()));

                mSendButton.clearColorFilter();
              }
            }
          }
        });

    /*------------------------------------------------------*/

    mCommand.setOnEditorActionListener(
            (v, actionId, event) -> {
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

    /*------------------------------------------------------*/

    // Action to perform when clicking send button in various scenerios
    mSendButton.setOnClickListener(
        v -> {
          sendButtonClicked = true;
          HapticUtils.weakVibrate(v);
          if (mShizukuShell != null && mShizukuShell.isBusy()) {
            mShizukuShell.destroy();
            viewModel.setSendDrawable(ic_help);
            mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_help, requireActivity()));
            mSendButton.clearColorFilter();

          } else if (mCommand.getText() == null || mCommand.getText().toString().trim().isEmpty()) {
            Intent examples = new Intent(requireActivity(), ExamplesActivity.class);
            startActivity(examples);
          } else if (!Shizuku.pingBinder()) {

            handleShizukuAvailability(context);
          } else {
            mPasteButton.hide();
            mUndoButton.hide();

            if (isAdded()) {
              mCommandInput.setError(null);
              initializeShell(requireActivity());
              KeyboardUtils.closeKeyboard(requireActivity(), v);}
          }
        });

    /*------------------------------------------------------*/

    mSettingsButton.setTooltipText(getString(R.string.settings));

    mSettingsButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          Intent settingsIntent = new Intent(requireActivity(), SettingsActivity.class);
          startActivity(settingsIntent);
        });

    /*------------------------------------------------------*/

    mClearButton.setTooltipText(getString(R.string.clear_screen));

    mClearButton.setOnClickListener(
        v -> {
          viewModel.setShellOutput(null);
          boolean switchState = Preferences.getClear(context);
          if (switchState) {
            new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(getString(R.string.clear_everything))
                .setMessage(getString(R.string.clear_all_message))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {})
                .setPositiveButton(
                    getString(R.string.yes),
                    (dialogInterface, i) -> clearAll())
                .show();
          } else {
            clearAll();
          }
        });

    /*------------------------------------------------------*/

    mSearchButton.setTooltipText(getString(R.string.search));

    mSearchButton.setOnClickListener(
        v -> {
          if (mHistoryButton.getVisibility() == View.VISIBLE) {
            mHistoryButton.setVisibility(View.GONE);
          }
          if (mClearButton.getVisibility() == View.VISIBLE) {
            mClearButton.setVisibility(View.GONE);
          }
          mBookMarks.setVisibility(View.GONE);
          mSettingsButton.setVisibility(View.GONE);
          mSearchButton.setVisibility(View.GONE);
          mSearchWord.setVisibility(View.VISIBLE);
          mSearchWord.requestFocus();
          mCommand.setText(null);
        });

    /*------------------------------------------------------*/

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

    /*------------------------------------------------------*/

    if (mSearchWord.getVisibility() == View.GONE) {
      mBookMarks.setVisibility(
              !Utils.getBookmarks(requireActivity()).isEmpty() ? View.VISIBLE : View.GONE);
    }

    mBookMarks.setTooltipText(getString(R.string.bookmarks));

    mBookMarks.setOnClickListener(
        v -> Utils.bookmarksDialog(context, requireActivity(), mCommand, mCommandInput, mBookMarks));

    /*------------------------------------------------------*/

    mHistoryButton.setTooltipText(getString(R.string.history));

    mHistoryButton.setOnClickListener(
        v -> {
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
        });

    /*------------------------------------------------------*/

    mAppNameLayout.setOnClickListener(
        v -> {
          if (mSearchWord.getVisibility() == View.VISIBLE) {
            mSearchWord.setVisibility(View.GONE);
            mBookMarks.setVisibility(
                    !Utils.getBookmarks(requireActivity()).isEmpty() ? View.VISIBLE : View.GONE);
            mSettingsButton.setVisibility(View.VISIBLE);
            mSearchButton.setVisibility(View.VISIBLE);
            mHistoryButton.setVisibility(View.VISIBLE);
            mClearButton.setVisibility(View.VISIBLE);
          }
        });

    /*------------------------------------------------------*/

    mShareButton.setOnClickListener(
        v -> {
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

    // Logic to hide and show share button
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

    /*------------------------------------------------------*/

    mSaveButton.setOnClickListener(
        v -> {
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
      mSearchWord.setText(null);
      mSearchWord.setVisibility(View.GONE);
      mBookMarks.setVisibility(View.VISIBLE);
      mSettingsButton.setVisibility(View.VISIBLE);
    } else {
      mHistoryButton.setVisibility(View.GONE);
      mClearButton.setVisibility(View.GONE);
      mSearchButton.setVisibility(View.GONE);
    }

    /*------------------------------------------------------*/

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
    // Fun Commands

    if (finalCommand.equals("exit")) {
      new MaterialAlertDialogBuilder(activity)
          .setCancelable(false)
          .setTitle("Confirm exit")
          .setMessage(getString(R.string.quit_app_message))
          .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {})
          .setPositiveButton(getString(R.string.quit), (dialogInterface, i) -> activity.finish())
          .show();
      return;
    }

    if (finalCommand.startsWith("su")) {
      mCommandInput.setError(getString(R.string.su_warning));
      mCommandInput.setErrorIconDrawable(Utils.getDrawable(R.drawable.ic_error, requireActivity()));
      Utils.alignMargin(mSendButton);
      Utils.alignMargin(localShellSymbol);
      mCommand.requestFocus();
      Utils.snackBar(
              activity.findViewById(android.R.id.content), getString(R.string.su_warning_message))
          .show();
      if (mResult != null && !mResult.isEmpty()) {
        mClearButton.setVisibility(View.VISIBLE);
        mSearchButton.setVisibility(View.VISIBLE);
      }

      return;
    }

    /*------------------------------------------------------*/

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

                    /*------------------------------------------------------*/

                    if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                      if (mHistory != null
                          && !mHistory.isEmpty()
                          && mHistoryButton.getVisibility() != View.VISIBLE) {
                        mHistoryButton.setVisibility(View.VISIBLE);
                      }
                      if (mResult != null && !mResult.isEmpty()) {

                        mClearButton.setVisibility(View.VISIBLE);

                        mSearchButton.setVisibility(View.VISIBLE);
                        mResult.add("<i></i>");
                        mResult.add("Shell is dead");
                        if (!isKeyboardVisible) {
                          mSaveButton.show();
                          mShareButton.show();
                        }
                      }
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

                    /*------------------------------------------------------*/

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

                    /*------------------------------------------------------*/

                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    if (!mCommand.isFocused()) mCommand.requestFocus();
                  });
          if (!mExecutors.isShutdown()) mExecutors.shutdown();
        });
  }

  /*------------------ Functions-----------------*/

  /*------------------------------------------------------*/

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

  /*------------------------------------------------------*/

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mShizukuShell != null) mShizukuShell.destroy();
  }

  /*------------------------------------------------------*/

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
    mSearchButton.setVisibility(View.GONE);
    mSaveButton.setVisibility(View.GONE);
    mShareButton.setVisibility(View.GONE);
    mClearButton.setVisibility(View.GONE);
    showBottomNav();
    mCommand.clearFocus();
    if (!mCommand.isFocused()) mCommand.requestFocus();
  }

  /*------------------------------------------------------*/

  private void hideSearchBar() {
    mSearchWord.setText(null);
    mSearchWord.setVisibility(View.GONE);
    if (!mCommand.isFocused()) mCommand.requestFocus();
    mBookMarks.setVisibility(View.VISIBLE);
    mSettingsButton.setVisibility(View.VISIBLE);
    if (mHistory != null && !mHistory.isEmpty()) {
      mHistoryButton.setVisibility(View.VISIBLE);
    }
    if (mResult != null && !mResult.isEmpty() && !mShizukuShell.isBusy()) {
      mClearButton.setVisibility(View.VISIBLE);
      mSearchButton.setVisibility(View.VISIBLE);
    }
  }

  /*------------------------------------------------------*/

  private void showBottomNav() {
    if (getActivity() != null && getActivity() instanceof MainActivity) {
      ((MainActivity) getActivity()).mNav.animate().translationY(0);
    }
  }

  /*------------------------------------------------------*/

  private void setVisibilityWithDelay(View view, int delayMillis) {
    new Handler(Looper.getMainLooper())
        .postDelayed(
            () -> view.setVisibility(View.VISIBLE),
            delayMillis);
  }

  /*------------------------------------------------------*/

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

  public void updateInputField(String text) {
    if (text != null) {
      mCommand.setText(text);
      mCommand.requestFocus();
      mCommand.setSelection(mCommand.getText().length());
      viewModel.setSendDrawable(ic_send);
      mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_send, requireActivity()));
      viewModel.setEditTextFocused(true);
      viewModel.setSendDrawable(ic_send);
    }
  }

  /*------------------------------------------------------*/

  // error handling when shizuku is unavailable or permission isn't granted
  private void handleShizukuAvailability(Context context) {

    mCommandInput.setError(getString(R.string.shizuku_unavailable));
    if (mCommand.getText() != null) {
      mCommandInput.setErrorIconDrawable(
          Utils.getDrawable(R.drawable.ic_cancel, requireActivity()));
      mCommandInput.setErrorIconOnClickListener(
          t -> mCommand.setText(null));
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

  /*------------------------------------------------------*/

  private boolean isEditTextFocused() {
    return mCommand.hasFocus();
  }

  private boolean isSaveButtonVisible() {
    return mSaveButton.getVisibility() == View.VISIBLE;
  }

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
}
