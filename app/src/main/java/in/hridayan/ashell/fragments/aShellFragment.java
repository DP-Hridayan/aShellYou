package in.hridayan.ashell.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.BottomNavOnScrollListener;
import in.hridayan.ashell.UI.KeyboardVisibilityChecker;
import in.hridayan.ashell.activities.ExamplesActivity;
import in.hridayan.ashell.activities.FabExtendingOnScrollListener;
import in.hridayan.ashell.activities.FabOnScrollDownListener;
import in.hridayan.ashell.activities.FabOnScrollUpListener;
import in.hridayan.ashell.activities.SettingsActivity;
import in.hridayan.ashell.activities.aShellActivity;
import in.hridayan.ashell.adapters.CommandsAdapter;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.adapters.ShellOutputAdapter;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.SettingsItem;
import in.hridayan.ashell.utils.ShizukuShell;
import in.hridayan.ashell.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
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
 * Modified by DP-Hridayan <hridayanofficial@gmail.com> since January 24 , 2024
 */

public class aShellFragment extends Fragment {

  private AppCompatImageButton localShellSymbol;
  private ExtendedFloatingActionButton mSaveButton;
  private FloatingActionButton mBottomButton, mSendButton, mTopButton;
  private MaterialButton mClearButton, mHistoryButton, mSearchButton, mBookMarks, mSettingsButton;
  private FrameLayout mAppNameLayout;
  private BottomNavigationView mNav;
  private CommandsAdapter mCommandsAdapter;
  private boolean isKeyboardVisible;
  private ShellOutputAdapter mShellOutputAdapter;
  private RecyclerView mRecyclerViewOutput, mRecyclerViewCommands;
  private SettingsAdapter adapter;
  private SettingsItem settingsList;
  private ShizukuShell mShizukuShell;
  private TextInputLayout mCommandInput;
  private TextInputEditText mCommand, mSearchWord;
  private boolean mExit;
  private final Handler mHandler = new Handler(Looper.getMainLooper());
  private int mPosition = 1;
  private List<String> mHistory = null, mResult = null;

  public aShellFragment() {}

  public aShellFragment(BottomNavigationView nav) {
    this.mNav = nav;
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View mRootView = inflater.inflate(R.layout.fragment_ashell, container, false);

    /*------------------------------------------------------*/

    List<SettingsItem> settingsList = new ArrayList<>();
    adapter = new SettingsAdapter(settingsList, requireContext());

    int statusBarColor = getResources().getColor(R.color.StatusBar);
    double brightness = getBrightness(statusBarColor);
    boolean isLightStatusBar = brightness > 0.5;

    if (isAdded()) {
      View decorView = requireActivity().getWindow().getDecorView();
      if (isLightStatusBar) {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
      } else {
        decorView.setSystemUiVisibility(0);
      }
    }

    KeyboardVisibilityChecker.attachVisibilityListener(
        requireActivity(),
        new KeyboardVisibilityChecker.KeyboardVisibilityListener() {

          public void onKeyboardVisibilityChanged(boolean visible) {
            isKeyboardVisible = visible;
            if (isKeyboardVisible) {
              if (mSaveButton.getVisibility() == View.VISIBLE) mSaveButton.setVisibility(View.GONE);
            } else {
              if (mSaveButton.getVisibility() == View.GONE && mRecyclerViewOutput.getHeight() != 0)
                new Handler(Looper.getMainLooper())
                    .postDelayed(
                        () -> {
                          mSaveButton.setVisibility(View.VISIBLE);
                        },
                        100);
            }
          }
        });

    /*------------------------------------------------------*/
    localShellSymbol = mRootView.findViewById(R.id.local_shell_symbol);
    mAppNameLayout = mRootView.findViewById(R.id.app_name_layout);
    mBookMarks = mRootView.findViewById(R.id.bookmarks);
    mBottomButton = mRootView.findViewById(R.id.fab_down);
    mClearButton = mRootView.findViewById(R.id.clear);
    mCommand = mRootView.findViewById(R.id.shell_command);
    mCommandInput = mRootView.findViewById(R.id.shell_command_layout);
    mNav = requireActivity().findViewById(R.id.bottom_nav_bar);
    mHistoryButton = mRootView.findViewById(R.id.history);
    mRecyclerViewCommands = mRootView.findViewById(R.id.recycler_view_commands);
    mRecyclerViewOutput = mRootView.findViewById(R.id.recycler_view_output);
    mSaveButton = mRootView.findViewById(R.id.save_button);
    mSearchButton = mRootView.findViewById(R.id.search);
    mSearchWord = mRootView.findViewById(R.id.search_word);
    mSendButton = mRootView.findViewById(R.id.send);
    mSettingsButton = mRootView.findViewById(R.id.settings);
    mTopButton = mRootView.findViewById(R.id.fab_up);

    /*------------------------------------------------------*/

    mRecyclerViewOutput.setLayoutManager(new LinearLayoutManager(requireActivity()));
    mRecyclerViewCommands.setLayoutManager(new LinearLayoutManager(requireActivity()));
    mRecyclerViewOutput.addOnScrollListener(new FabExtendingOnScrollListener(mSaveButton));
    mRecyclerViewOutput.addOnScrollListener(new FabOnScrollUpListener(mTopButton));
    mRecyclerViewOutput.addOnScrollListener(new FabOnScrollDownListener(mBottomButton));
    mRecyclerViewOutput.addOnScrollListener(new BottomNavOnScrollListener(mNav));

    mRecyclerViewOutput.setAdapter(mShellOutputAdapter);
    /*------------------------------------------------------*/

    mTopButton.setOnClickListener(
        new View.OnClickListener() {
          private long lastClickTime = 0;

          @Override
          public void onClick(View v) {

            long currentTime = System.currentTimeMillis();

            long timeDifference = currentTime - lastClickTime;

            if (timeDifference < 200) {
              mRecyclerViewOutput.scrollToPosition(0);
            } else {

              boolean switchState = adapter.getSavedSwitchState("Smooth scrolling");

              if (switchState) {

                mRecyclerViewOutput.smoothScrollToPosition(0);
              } else {
                mRecyclerViewOutput.scrollToPosition(0);
              }
            }

            lastClickTime = currentTime;
          }
        });

    /*------------------------------------------------------*/

    mBottomButton.setOnClickListener(
        new View.OnClickListener() {
          private long lastClickTime = 0;

          @Override
          public void onClick(View v) {

            long currentTime = System.currentTimeMillis();

            long timeDifference = currentTime - lastClickTime;

            if (timeDifference < 200) {
              mRecyclerViewOutput.scrollToPosition(
                  Objects.requireNonNull(mRecyclerViewOutput.getAdapter()).getItemCount() - 1);
            } else {

              boolean switchState = adapter.getSavedSwitchState("Smooth scrolling");

              if (switchState) {
                mRecyclerViewOutput.smoothScrollToPosition(
                    Objects.requireNonNull(mRecyclerViewOutput.getAdapter()).getItemCount() - 1);

              } else {
                mRecyclerViewOutput.scrollToPosition(
                    Objects.requireNonNull(mRecyclerViewOutput.getAdapter()).getItemCount() - 1);
              }
            }

            lastClickTime = currentTime;
          }
        });

    /*------------------------------------------------------*/

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

            if (s.toString().contains("\n")) {
              if (!s.toString().endsWith("\n")) {
                mCommand.setText(s.toString().replace("\n", ""));
              }
              if (isAdded()) {
                initializeShell(requireActivity());
              }

            } else {
              if (mShizukuShell != null && mShizukuShell.isBusy()) {
                return;
              }

              /*------------------------------------------------------*/

              mBookMarks.setVisibility(
                  Utils.getBookmarks(requireActivity()).size() > 0 ? View.VISIBLE : View.GONE);

              if (!s.toString().trim().isEmpty()) {
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
                                mRootView,
                                getString(R.string.bookmark_removed_message, s.toString().trim()))
                            .show();
                      } else {
                        addBookmark(s.toString().trim(), mRootView);
                      }

                      mCommandInput.setEndIconDrawable(
                          Utils.getDrawable(
                              Utils.isBookmarked(s.toString().trim(), requireActivity())
                                  ? R.drawable.ic_bookmark_added
                                  : R.drawable.ic_add_bookmark,
                              requireActivity()));
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
                                    Commands.getPackageInfo(packageNamePrefix + "."));
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
                                new CommandsAdapter(Commands.getCommand(s.toString()));
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
                mSendButton.setImageDrawable(
                    Utils.getDrawable(R.drawable.ic_help, requireActivity()));
                mSendButton.clearColorFilter();
              }
            }
          }
        });

    /*------------------------------------------------------*/

    mCommand.setOnEditorActionListener(
        new TextView.OnEditorActionListener() {

          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
              if (mShizukuShell != null && mShizukuShell.isBusy()) {
                mShizukuShell.destroy();
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
          }
        });

    /*------------------------------------------------------*/

    mSendButton.setOnClickListener(
        v -> {
          if (mShizukuShell != null && mShizukuShell.isBusy()) {
            mShizukuShell.destroy();
            mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_help, requireActivity()));
            mSendButton.clearColorFilter();

          } else if (mCommand.getText() == null || mCommand.getText().toString().trim().isEmpty()) {
            Intent examples = new Intent(requireActivity(), ExamplesActivity.class);
            startActivity(examples);
          } else if (!Shizuku.pingBinder()) {

            if (isAdded()) {
              mCommandInput.setError("Shizuku unavailable");

              alignMargin(mSendButton);
              alignMargin(localShellSymbol);

              new MaterialAlertDialogBuilder(requireActivity())
                  .setTitle("Warning")
                  .setMessage(getString(R.string.shizuku_unavailable_message))
                  .setNegativeButton(
                      getString(R.string.shizuku_about),
                      (dialogInterface, i) -> {
                        Utils.openUrl(requireContext(), "https://shizuku.rikka.app/");
                      })
                  .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {})
                  .show();
            }

          } else {
            if (isAdded()) {
              mCommandInput.setError(null);
              initializeShell(requireActivity());
            }
          }
        });

    /*------------------------------------------------------*/

    mSettingsButton.setTooltipText("Settings");

    mSettingsButton.setOnClickListener(
        v -> {
          Intent settingsIntent = new Intent(requireActivity(), SettingsActivity.class);
          startActivity(settingsIntent);
        });

    /*------------------------------------------------------*/

    mClearButton.setTooltipText("Clear screen");

    mClearButton.setOnClickListener(
        v -> {
          boolean switchState = adapter.getSavedSwitchState("Ask before clearing shell output");
          if (switchState) {
            new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(getString(R.string.clear_everything))
                .setMessage(getString(R.string.clear_all_message))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {})
                .setPositiveButton(
                    getString(R.string.yes),
                    (dialogInterface, i) -> {
                      clearAll();
                    })
                .show();
          } else {
            clearAll();
          }
        });

    /*------------------------------------------------------*/

    mSearchButton.setTooltipText("Search");

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
                if (mResult.get(i).toLowerCase().contains(s.toString().toLowerCase())) {
                  mResultSorted.add(mResult.get(i));
                }
              }
              updateUI(mResultSorted);
            }
          }
        });

    /*------------------------------------------------------*/

    mBookMarks.setTooltipText("Bookmarks");

    mBookMarks.setOnClickListener(
        v -> {
          PopupMenu popupMenu = new PopupMenu(requireContext(), mCommand);
          Menu menu = popupMenu.getMenu();

          for (int i = 0; i < Utils.getBookmarks(requireActivity()).size(); i++) {

            menu.add(Menu.NONE, i, Menu.NONE, Utils.getBookmarks(requireActivity()).get(i));
          }
          popupMenu.setOnMenuItemClickListener(
              item -> {
                for (int i = 0; i < Utils.getBookmarks(requireActivity()).size(); i++) {
                  if (item.getItemId() == i) {
                    mCommand.setText(Utils.getBookmarks(requireActivity()).get(i));
                    mCommand.setSelection(mCommand.getText().length());
                  }
                }
                return false;
              });
          popupMenu.show();
        });

    /*------------------------------------------------------*/

    mHistoryButton.setTooltipText("History");

    mHistoryButton.setOnClickListener(
        v -> {
          PopupMenu popupMenu = new PopupMenu(requireContext(), mCommand);
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
                Utils.getBookmarks(requireActivity()).size() > 0 ? View.VISIBLE : View.GONE);
            mSettingsButton.setVisibility(View.VISIBLE);
            mSearchButton.setVisibility(View.VISIBLE);
            mHistoryButton.setVisibility(View.VISIBLE);
            mClearButton.setVisibility(View.VISIBLE);
          }
        });

    /*------------------------------------------------------*/

    mSaveButton.setOnClickListener(
        v -> {
          StringBuilder sb = new StringBuilder();
          for (int i = mPosition; i < mResult.size(); i++) {
            String result = mResult.get(i);
            if (!"aShell: Finish".equals(result) && !"<i></i>".equals(result)) {
              sb.append(result).append("\n");
            }
          }

          boolean saved = false;
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
              ContentValues values = new ContentValues();
              String fileName =
                  mHistory.get(mHistory.size() - 1).replace("/", "-").replace(" ", "") + ".txt";
              values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
              values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
              values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
              Uri uri =
                  requireActivity()
                      .getContentResolver()
                      .insert(MediaStore.Files.getContentUri("external"), values);

              if (uri != null) {
                try (OutputStream outputStream =
                    requireActivity().getContentResolver().openOutputStream(uri)) {
                  outputStream.write(sb.toString().getBytes());
                  saved = true;
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          } else {
            if (requireActivity()
                    .checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
              ActivityCompat.requestPermissions(
                  requireActivity(),
                  new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                  0);
              return;
            }

            try {
              String fileName =
                  mHistory.get(mHistory.size() - 1).replace("/", "-").replace(" ", "") + ".txt";
              File file = new File(Environment.DIRECTORY_DOWNLOADS, fileName);
              Utils.create(sb.toString(), file);
              saved = true;
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          String message =
              saved
                  ? getString(R.string.shell_output_saved_message, Environment.DIRECTORY_DOWNLOADS)
                  : getString(R.string.shell_output_not_saved_message);
          String title = saved ? getString(R.string.success) : getString(R.string.failed);

          new MaterialAlertDialogBuilder(requireActivity())
              .setTitle(title)
              .setMessage(message)
              .setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {})
              .show();
        });
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(
        () -> {
          if (mResult != null
              && mResult.size() > 0
              && !mResult.get(mResult.size() - 1).equals("aShell: Finish")) {
            updateUI(mResult);
          }
        },
        0,
        250,
        TimeUnit.MILLISECONDS);

    return mRootView;
  }

  private int lastIndexOf(String s, String splitTxt) {
    return s.lastIndexOf(splitTxt);
  }

  private List<String> getRecentCommands() {
    List<String> mRecentCommands = new ArrayList<>(mHistory);
    Collections.reverse(mRecentCommands);
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

  private void runShellCommand(String command, Activity activity) {

    if (!isAdded() || getActivity() == null || getActivity().isFinishing()) {
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

    String finalCommand;
    if (command.startsWith("adb shell ")) {
      finalCommand = command.replace("adb shell ", "");
    } else if (command.startsWith("adb -d shell ")) {
      finalCommand = command.replace("adb -d shell ", "");
    } else {
      finalCommand = command;
    }

    if (finalCommand.equals("clear")) {
      if (mResult != null) {
        clearAll();
      }
      return;
    }

    // Fun Commands
    if (finalCommand.equals("goto top")) {
      if (mResult != null) {
        mRecyclerViewOutput.scrollToPosition(0);
      }
      return;
    }
    if (finalCommand.equals("goto bottom")) {
      if (mResult != null) {
        mRecyclerViewOutput.scrollToPosition(
            Objects.requireNonNull(mRecyclerViewOutput.getAdapter()).getItemCount() - 1);
      }
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
      mCommandInput.setError("Root commands not available");
      alignMargin(mSendButton);
      alignMargin(localShellSymbol);

      Utils.snackBar(
              activity.findViewById(android.R.id.content), getString(R.string.su_warning_message))
          .show();
      return;
    }

    /*------------------------------------------------------*/

    if (mHistory == null) {
      mHistory = new ArrayList<>();
    }
    mHistory.add(finalCommand);

    mSaveButton.setVisibility(View.GONE);
    mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_stop, requireActivity()));
    mSendButton.setColorFilter(Utils.getColor(R.color.colorErrorContainer, requireContext()));

    String mTitleText =
        "<font color=\""
            + Utils.getColor(R.color.colorBlue, activity)
            + "\">shell@"
            + Utils.getDeviceName()
            + "</font><font color=\""
            + Utils.getColor(R.color.colorGreen, activity)
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
                          && mHistory.size() > 0
                          && mHistoryButton.getVisibility() != View.VISIBLE) {
                        mHistoryButton.setVisibility(View.VISIBLE);
                      }
                      if (mResult != null && mResult.size() > 0) {

                        mClearButton.setVisibility(View.VISIBLE);

                        mSearchButton.setVisibility(View.VISIBLE);
                        mResult.add("<i></i>");
                        mResult.add("aShell: Finish");
                        if (!isKeyboardVisible) {
                          mSaveButton.setVisibility(View.VISIBLE);
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
                      mSendButton.setImageDrawable(
                          Utils.getDrawable(R.drawable.ic_help, requireActivity()));
                      mSendButton.clearColorFilter();
                    } else {
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

  /*------------------------------------------------------*/

  private void updateUI(List<String> data) {
    if (data == null) {
      return;
    }

    List<String> mData = new ArrayList<>();
    try {
      for (String result : data) {
        if (!TextUtils.isEmpty(result) && !result.equals("aShell: Finish")) {
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

  /*------------------ Functions-----------------*/

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mShizukuShell != null) mShizukuShell.destroy();
  }

  private void clearAll() {
    if (mShizukuShell != null) mShizukuShell.destroy();
    mResult = null;
    mRecyclerViewOutput.setAdapter(null);
    mSearchButton.setVisibility(View.GONE);
    mSaveButton.setVisibility(View.GONE);
    mClearButton.setVisibility(View.GONE);
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
    if (mHistory != null && mHistory.size() > 0) {
      mHistoryButton.setVisibility(View.VISIBLE);
    }
    if (mResult != null && mResult.size() > 0 && !mShizukuShell.isBusy()) {
      mClearButton.setVisibility(View.VISIBLE);
      mSearchButton.setVisibility(View.VISIBLE);
    }
  }

  public double getBrightness(int color) {
    int red = Color.red(color);
    int green = Color.green(color);
    int blue = Color.blue(color);
    return 0.299 * red + 0.587 * green + 0.114 * blue;
  }

  private void showBottomNav() {
    if (getActivity() != null && getActivity() instanceof aShellActivity) {
      ((aShellActivity) getActivity()).mNav.animate().translationY(0);
    }
  }

  private void addBookmark(String bookmark, View mRootView) {

    boolean switchState = adapter.getSavedSwitchState("Override maximum bookmarks limit");

    if (Utils.getBookmarks(requireActivity()).size() <= 4) {
      Utils.addToBookmark(bookmark, requireActivity());
      Utils.snackBar(mRootView, getString(R.string.bookmark_added_message, bookmark)).show();
    } else {
      if (switchState) {
        Utils.addToBookmark(bookmark, requireActivity());
        Utils.snackBar(mRootView, getString(R.string.bookmark_added_message, bookmark)).show();
      } else {
        Utils.snackBar(mRootView, getString(R.string.bookmark_limit_reached)).show();
      }
    }
  }

  private void alignMargin(View component) {

    ViewGroup.MarginLayoutParams params =
        (ViewGroup.MarginLayoutParams) component.getLayoutParams();
    params.bottomMargin = 29;
    component.setLayoutParams(params);
    component.requestLayout();
  }
}
