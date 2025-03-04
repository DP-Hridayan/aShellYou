package in.hridayan.ashell.fragments.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.Hold;
import in.hridayan.ashell.AshellYou;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.BehaviorFAB;
import in.hridayan.ashell.UI.BehaviorFAB.FabExtendingOnScrollListener;
import in.hridayan.ashell.UI.BehaviorFAB.FabLocalScrollDownListener;
import in.hridayan.ashell.UI.BehaviorFAB.FabLocalScrollUpListener;
import in.hridayan.ashell.UI.BottomNavUtils;
import in.hridayan.ashell.UI.BottomSheets;
import in.hridayan.ashell.UI.DialogUtils;
import in.hridayan.ashell.UI.KeyboardUtils;
import in.hridayan.ashell.UI.ThemeUtils;
import in.hridayan.ashell.UI.ToastUtils;
import in.hridayan.ashell.UI.Transitions;
import in.hridayan.ashell.activities.MainActivity;
import in.hridayan.ashell.adapters.CommandsAdapter;
import in.hridayan.ashell.adapters.ShellOutputAdapter;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.databinding.FragmentWifiAdbBinding;
import in.hridayan.ashell.fragments.ExamplesFragment;
import in.hridayan.ashell.fragments.settings.SettingsFragment;
import in.hridayan.ashell.shell.RootShell;
import in.hridayan.ashell.shell.ShizukuShell;
import in.hridayan.ashell.shell.WifiAdbShell;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.DeviceUtils;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.SystemMountHelper;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.ExamplesViewModel;
import in.hridayan.ashell.viewmodels.MainViewModel;
import in.hridayan.ashell.viewmodels.SettingsViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * Created by DP-Hridayan <hridayanofficial@gmail.com> on March 3 , 2025
 */

public class WifiAdbFragment extends Fragment {

  private BottomNavigationView mNav;
  private CommandsAdapter mCommandAdapter;
  private ShellOutputAdapter mShellOutputAdapter;
  private ShizukuShell mShizukuShell;
  private WifiAdbShell mWifiAdbShell;
  private boolean isKeyboardVisible = false, sendButtonClicked = false, isEndIconVisible = false;
  private int mPosition = 1;
  private final int ic_help = 10, ic_send = 11, ic_stop = 12;
  private List<String> mHistory = null, mResult = null, mRecentCommands, shellOutput, history;
  private View view;
  private Context context;
  private MainViewModel mainViewModel;
  private FragmentWifiAdbBinding binding;
  private Pair<Integer, Integer> mRVPositionAndOffset;
  private String shell;
  private SettingsViewModel settingsViewModel;
  private ExamplesViewModel examplesViewModel;

  public WifiAdbFragment() {}

  @Override
  public void onPause() {
    super.onPause();

    mainViewModel.setPreviousFragment(Const.WIFI_ADB_FRAGMENT);

    // If keyboard is visible then we close it before leaving fragment
    if (isKeyboardVisible) KeyboardUtils.closeKeyboard(requireActivity(), view);

    BottomNavUtils.hideNavSmoothly(mNav);
  }

  @Override
  public void onResume() {
    super.onResume();

    // we set exit transition to null
    setExitTransition(null);

    // if bottom navigation is not visible , then make it visible
    BottomNavUtils.showNavSmoothly(mNav);

    KeyboardUtils.disableKeyboard(context, requireActivity(), view);

    handleUseCommand();

    binding.rvOutput.setLayoutManager(new LinearLayoutManager(requireActivity()));

    // If the end icon of edit text is visible then set its icon accordingly
    if (!binding.commandEditText.getText().toString().isEmpty()) {
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
    if (mWifiAdbShell != null) WifiAdbShell.destroy();
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    setExitTransition(null);

    binding = FragmentWifiAdbBinding.inflate(inflater, container, false);

    context = requireContext();

    view = binding.getRoot();

    mNav = requireActivity().findViewById(R.id.bottom_nav_bar);

    initializeViewModels();

    WifiAdbShell.copyAdbBinaryToData(context);

    // Run in a background thread to avoid blocking the UI
    new CheckAndRemountTask().execute();

    if (binding.rvOutput.getLayoutManager() == null) {
      binding.rvOutput.setLayoutManager(new LinearLayoutManager(requireActivity()));
    }

    binding.rvCommands.setLayoutManager(new LinearLayoutManager(requireActivity()));

    binding.rvCommands.addOnScrollListener(new FabExtendingOnScrollListener(binding.pasteButton));
    binding.rvOutput.addOnScrollListener(new FabExtendingOnScrollListener(binding.pasteButton));

    binding.rvOutput.addOnScrollListener(new FabExtendingOnScrollListener(binding.saveButton));
    binding.rvOutput.addOnScrollListener(new FabLocalScrollUpListener(binding.scrollUpButton));

    binding.rvOutput.addOnScrollListener(new FabLocalScrollDownListener(binding.scrollDownButton));

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
              binding.sendButton.setImageDrawable(
                  Utils.getDrawable(R.drawable.ic_help, requireActivity()));
              binding.sendButton.clearColorFilter();
            } else {
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

    modeButtonOnClickListener();

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

    sendButtonOnClickListener();

    mainViewModel.setHomeFragment(Const.WIFI_ADB_FRAGMENT);
    return view;
  }

  // Functions

  // initialize viewModels
  private void initializeViewModels() {
    mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
    examplesViewModel = new ViewModelProvider(requireActivity()).get(ExamplesViewModel.class);
  }

  private int lastIndexOf(String s, String splitTxt) {
    return s.lastIndexOf(splitTxt);
  }

  private List<String> getRecentCommands() {
    mRecentCommands = new ArrayList<>(mHistory);
    Collections.reverse(mRecentCommands);
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
          mShellOutputAdapter = new ShellOutputAdapter(mData);
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
      boolean switchState = Preferences.getShareAndRun();
      updateInputField(sharedText);
      if (switchState) {

        if (!RootShell.isDeviceRooted()) handleRootUnavailability();
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
      binding.sendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_send, requireActivity()));
    }
  }

  // Setup the recycler view
  private void setupRecyclerView() {
    binding.rvOutput.setLayoutManager(new LinearLayoutManager(requireActivity()));
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
          HapticUtils.weakVibrate(v);

          BottomSheets.showBottomSheetPairAndConnect(context, requireActivity());
        });
  }

  // OnClick listener for the settings button
  private void settingsButtonOnClickListener() {
    binding.settingsButton.setTooltipText(getString(R.string.settings));

    binding.settingsButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);

          goToSettings();
        });
  }

  // OnClick listener for bookmarks button
  private void bookmarksButtonOnClickListener() {
    binding.bookmarksButton.setTooltipText(getString(R.string.bookmarks));
    binding.bookmarksButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);

          if (Utils.getBookmarks(context).isEmpty())
            ToastUtils.showToast(context, R.string.no_bookmarks, ToastUtils.LENGTH_SHORT);
          else
            DialogUtils.bookmarksDialog(
                context, binding.commandEditText, binding.commandInputLayout);
        });
  }

  // OnClick listener for the history button
  private void historyButtonOnClickListener() {
    binding.historyButton.setTooltipText(getString(R.string.history));

    binding.historyButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);

          if (mHistory == null)
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
          HapticUtils.weakVibrate(v);

          if (mResult == null || mResult.isEmpty()) {
            ToastUtils.showToast(context, R.string.nothing_to_clear, ToastUtils.LENGTH_SHORT);
          } else if (isShellBusy()) {
            ToastUtils.showToast(context, R.string.abort_command, ToastUtils.LENGTH_SHORT);
            return;
          } else {
            boolean switchState = Preferences.getClear();
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
    if (mWifiAdbShell != null) return WifiAdbShell.isBusy();
    return false;
  }

  // This function is called when we want to clear the screen
  private void clearAll() {
    handleClearExceptions();

    mResult = null;

    if (binding.scrollUpButton.getVisibility() == View.VISIBLE)
      binding.scrollUpButton.setVisibility(View.GONE);

    if (binding.scrollDownButton.getVisibility() == View.VISIBLE)
      binding.scrollDownButton.setVisibility(View.GONE);

    binding.sendButton.setImageDrawable(
        hasTextInEditText()
            ? Utils.getDrawable(R.drawable.ic_send, requireActivity())
            : Utils.getDrawable(R.drawable.ic_help, requireActivity()));
    binding.rvOutput.setAdapter(null);
    binding.saveButton.setVisibility(View.GONE);
    binding.shareButton.setVisibility(View.GONE);
    showBottomNav();
    binding.commandEditText.clearFocus();
    if (!binding.commandEditText.isFocused()) binding.commandEditText.requestFocus();
  }

  private void handleClearExceptions() {
    if (mResult == null || mResult.isEmpty()) {
      ToastUtils.showToast(context, R.string.nothing_to_clear, ToastUtils.LENGTH_SHORT);
      return;

    } else if (isShellBusy()) {
      ToastUtils.showToast(context, R.string.abort_command, ToastUtils.LENGTH_SHORT);
      return;
    }
  }

  // OnClick listener for the search button
  private void searchButtonOnClickListener() {
    binding.searchButton.setTooltipText(getString(R.string.search));

    binding.searchButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);

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
          HapticUtils.weakVibrate(v);
          initializeResults();
          String sb = null, fileName = null;

          switch (Preferences.getSavePreference()) {
            case Const.ALL_OUTPUT:
              sb = Utils.convertListToString(mResult);
              fileName = "shellOutput" + DeviceUtils.getCurrentDateTime();
              break;

            case Const.LAST_COMMAND_OUTPUT:
              sb = buildResultsString().toString();
              fileName = Utils.generateFileName(mHistory) + DeviceUtils.getCurrentDateTime();
              break;

            default:
              break;
          }

          boolean saved = Utils.saveToFile(sb, requireActivity(), fileName);
          // We add .txt after the final file name to give text format
          if (saved) Preferences.setLastSavedFileName(fileName + ".txt");

          // Dialog showing if the output has been saved or not
          DialogUtils.outputSavedDialog(context, saved);
        });
  }

  // Onclick listener for share button
  private void shareButtonOnClickListener() {
    binding.shareButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          initializeResults();

          StringBuilder sb = new StringBuilder();
          for (int i = mPosition; i < mResult.size(); i++) {
            String result = mResult.get(i);
            if (!Utils.shellDeadError().equals(result)) sb.append(result).append("\n");
          }
          // We add .txt after the final file name to give it text format
          String fileName = Utils.generateFileName(mHistory) + ".txt";
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
          HapticUtils.weakVibrate(v);
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
          HapticUtils.weakVibrate(v);
          if (actionId == EditorInfo.IME_ACTION_SEND && hasTextInEditText()) {
            sendButtonClicked = true;

            // If shell is not busy and there is not any text in input field then go to examples
            if (!hasTextInEditText() && !isShellBusy()) goToExamples();
            else {
              if (!RootShell.isDeviceRooted()) handleRootUnavailability();
              else {
                // We perform root shell permission check on a new thread
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(
                    () -> {
                      boolean hasPermission = RootShell.hasPermission();

                      requireActivity()
                          .runOnUiThread(
                              () -> {
                                if (!hasPermission) DialogUtils.rootPermRequestDialog(context);
                                else if (mWifiAdbShell != null && WifiAdbShell.isBusy())
                                  abortWifiAdbShell();
                                else execShell(v);
                              });
                    });
                executor.shutdown();
              }
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

          HapticUtils.weakVibrate(v);

          // If shell is not busy and there is not any text in input field then go to examples
          if (!hasTextInEditText() && !isShellBusy()) goToExamples();

          // Need root for Wifi adb
          else {
            if (!RootShell.isDeviceRooted()) handleRootUnavailability();
            else {
              // We perform root shell permission check on a new thread
              ExecutorService executor = Executors.newSingleThreadExecutor();
              executor.execute(
                  () -> {
                    boolean hasPermission = RootShell.hasPermission();

                    requireActivity()
                        .runOnUiThread(
                            () -> {
                              if (!hasPermission) DialogUtils.rootPermRequestDialog(context);
                              else if (mWifiAdbShell != null && WifiAdbShell.isBusy())
                                abortWifiAdbShell();
                              else execShell(v);
                            });
                  });
              executor.shutdown();
            }
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
    String command = binding.commandEditText.getText().toString().replace("\n", "");
    if (Const.isPackageSensitive(command)) sensitivePackageWarningDialog(command);
    else runShellCommand(command);
  }

  private void sensitivePackageWarningDialog(String command) {
    LayoutInflater inflater = LayoutInflater.from(context);
    View dialogView = inflater.inflate(R.layout.dialog_sensitive_package_warning, null);

    MaterialButton okButton = dialogView.findViewById(R.id.ok);
    MaterialButton cancelButton = dialogView.findViewById(R.id.cancel);

    AlertDialog dialog =
        new MaterialAlertDialogBuilder(context).setView(dialogView).setCancelable(false).create();

    dialog.show();

    // Start countdown timer (10 seconds)
    new CountDownTimer(10000, 1000) {
      int timeLeft = 10;

      @Override
      public void onTick(long millisUntilFinished) {
        okButton.setText("OK (" + timeLeft + "s)");
        timeLeft--;
      }

      @Override
      public void onFinish() {
        okButton.setText("OK");
        okButton.setEnabled(true);
      }
    }.start();

    okButton.setOnClickListener(
        v -> {
          // Run the command containing sensitive package name
          runShellCommand(command);
          dialog.dismiss();
        });

    cancelButton.setOnClickListener(v -> dialog.dismiss());
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
    if (finalCommand.equals("clear")) {
      clearAll();
      return;
    }

    // Command to exit the app
    if (finalCommand.equals("exit")) {
      DialogUtils.confirmExitDialog(context, requireActivity());
      return;
    }

    // Initialize mHistory if necessary
    if (mHistory == null) mHistory = new ArrayList<>();

    mHistory.add(finalCommand);

    // Hide buttons and update send button
    binding.saveButton.hide();
    binding.shareButton.hide();
    binding.sendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_stop, requireActivity()));
    binding.sendButton.setColorFilter(
        DeviceUtils.androidVersion() >= Build.VERSION_CODES.S
            ? ThemeUtils.colorError(context)
            : ThemeUtils.getColor(R.color.red, context));

    // Create mTitleText and add it to mResult
    if (mResult == null) mResult = new ArrayList<>();

    String mTitleText =
        "<font color=\""
            + ThemeUtils.getColor(
                DeviceUtils.androidVersion() >= Build.VERSION_CODES.S
                    ? android.R.color.system_accent1_500
                    : R.color.blue,
                requireActivity())
            + "WifiAdbShell@"
            + DeviceUtils.getDeviceName()
            + " | "
            + "</font><font color=\""
            + ThemeUtils.getColor(
                DeviceUtils.androidVersion() >= Build.VERSION_CODES.S
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
          runWifiAdbShell(finalCommand);

          new Handler(Looper.getMainLooper())
              .post(
                  () -> {
                    if (!isAdded() || getActivity() == null || binding == null) return;

                    postExec();

                    // Update send button based on command text presence
                    if (!hasTextInEditText()) {
                      binding.sendButton.setImageDrawable(
                          Utils.getDrawable(R.drawable.ic_help, requireActivity()));
                      binding.sendButton.clearColorFilter();
                    } else {
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

    // Post UI updates back to the main thread
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
  }

  // Method to run commands using root
  private void runWifiAdbShell(String finalCommand) {
    mPosition = mResult.size();
    mWifiAdbShell = new WifiAdbShell(mResult, finalCommand);
    WifiAdbShell.exec(context);
    try {
      TimeUnit.MILLISECONDS.sleep(250);
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

  // boolean that checks if the current set mode is root mode
  private boolean isRootMode() {
    return Preferences.getLocalAdbMode() == Const.ROOT_MODE;
  }

  // This methods checks if there is valid text in the edit text
  private boolean hasTextInEditText() {
    return binding.commandEditText.getText() != null
        && !binding.commandEditText.getText().toString().trim().isEmpty();
  }

  // Call this method to abort or stop running shell command
  public void abortWifiAdbShell() {
    WifiAdbShell.destroy();
    binding.sendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_help, requireActivity()));
    binding.sendButton.clearColorFilter();
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

    DialogUtils.rootUnavailableDialog(context);
  }

  // Open command examples fragment
  private void goToExamples() {
    if (examplesViewModel != null) {
      examplesViewModel.setRVPositionAndOffset(null);
      examplesViewModel.setToolbarExpanded(true);
    }

    setExitTransition(new Hold());

    ExamplesFragment fragment = new ExamplesFragment();

    requireActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .addSharedElement(binding.sendButton, Const.SEND_TO_EXAMPLES)
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
        .addSharedElement(binding.settingsButton, Const.SETTINGS_TO_SETTINGS)
        .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
        .addToBackStack(fragment.getClass().getSimpleName())
        .commit();
  }

  // Get the command history
  private List<String> getHistory() {
    return mHistory;
  }

  // Get the command when using Use feature
  private void handleUseCommand() {
    if (mainViewModel.getUseCommand() != null) {
      updateInputField(mainViewModel.getUseCommand());
      mainViewModel.setUseCommand(null);
    }
  }

  // control visibility of paste and save button
  private void pasteAndSaveButtonVisibility() {
    if (mResult != null) {
      binding.pasteButton.setVisibility(View.GONE);
      binding.saveButton.setVisibility(View.VISIBLE);
    }
  }

  private static class CheckAndRemountTask extends AsyncTask<Void, Void, Boolean> {
    @Override
    protected Boolean doInBackground(Void... voids) {
      if (SystemMountHelper.isSystemReadOnly()) {
        return SystemMountHelper.remountSystemRW();
      }
      return true; // Already RW
    }

    /*   @Override
    protected void onPostExecute(Boolean success) {
      if (success) {
        Toast.makeText(AshellYou.getAppContext(), "System is now Read-Write", Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(AshellYou.getAppContext(), "Failed to change to Read-Write", Toast.LENGTH_SHORT)
            .show();
      }
    } */
  }
}
