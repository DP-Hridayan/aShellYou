package in.hridayan.ashell.fragments;

import static in.hridayan.ashell.utils.MessageOtg.CONNECTING;
import static in.hridayan.ashell.utils.MessageOtg.DEVICE_FOUND;
import static in.hridayan.ashell.utils.MessageOtg.DEVICE_NOT_FOUND;
import static in.hridayan.ashell.utils.MessageOtg.FLASHING;
import static in.hridayan.ashell.utils.MessageOtg.INSTALLING_PROGRESS;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cgutman.adblib.AdbBase64;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AdbStream;
import com.cgutman.adblib.UsbChannel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.MyAdbBase64;
import in.hridayan.ashell.R;
import in.hridayan.ashell.activities.ExamplesActivity;
import in.hridayan.ashell.activities.SettingsActivity;
import in.hridayan.ashell.adapters.CommandsAdapter;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.Const;
import in.hridayan.ashell.utils.MessageOtg;
import in.hridayan.ashell.utils.SettingsItem;
import in.hridayan.ashell.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class otgFragment extends Fragment
    implements TextView.OnEditorActionListener, View.OnKeyListener {
  private Handler handler;
  private UsbDevice mDevice;
  private TextView tvStatus;
  private MaterialTextView logs;
  private AppCompatImageButton mCable, mBookMark;
  private AdbCrypto adbCrypto;
  private AdbConnection adbConnection;
  private UsbManager mManager;
  private BottomNavigationView mNav;

  private CommandsAdapter mCommandsAdapter;
  private LinearLayoutCompat terminalView;
  private MaterialButton mSettingsButton, mBookMarks, mHistoryButton;
  private RecyclerView mRecyclerViewCommands;
  private SettingsAdapter adapter;

  private TextInputEditText mCommand;
  private FloatingActionButton mSendButton;
  private ScrollView scrollView;
  private AlertDialog mWaitingDialog;
  private String user = null;
  private List<String> mHistory = null, mResult = null;

  private AdbStream stream;

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_otg, container, false);

    int statusBarColor = getResources().getColor(R.color.StatusBar);
    double brightness = getBrightness(statusBarColor);
    boolean isLightStatusBar = brightness > 0.5;

    View decorView = requireActivity().getWindow().getDecorView();
    if (isLightStatusBar) {
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    } else {
      decorView.setSystemUiVisibility(0);
    }

    List<SettingsItem> settingsList = new ArrayList<>();
    adapter = new SettingsAdapter(settingsList, requireContext());

    mCable = view.findViewById(R.id.otg_cable);
    mNav = view.findViewById(R.id.bottom_nav_bar);
    mBookMarks = view.findViewById(R.id.bookmarksOtg);
    logs = view.findViewById(R.id.logs);
    mBookMark = view.findViewById(R.id.bookmarkOtg);
    mSettingsButton = view.findViewById(R.id.settingsOtg);
    mHistoryButton = view.findViewById(R.id.historyOtg);
    terminalView = view.findViewById(R.id.terminalView);
    mCommand = view.findViewById(R.id.edCommand);
    mRecyclerViewCommands = view.findViewById(R.id.rv_commandsOtg);

    mSendButton = view.findViewById(R.id.sendCommandOtg);
    scrollView = view.findViewById(R.id.scrollView);
    mManager = (UsbManager) requireActivity().getSystemService(Context.USB_SERVICE);

    mRecyclerViewCommands.setLayoutManager(new LinearLayoutManager(requireActivity()));

    // Logic for changing the command send button depending on the text on the EditText

    mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_help, requireActivity()));
    mSendButton.setOnClickListener(
        v -> {
          Intent examples = new Intent(requireActivity(), ExamplesActivity.class);
          startActivity(examples);
        });

    mCommand.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            mBookMarks.setVisibility(
                Utils.getBookmarks(requireActivity()).size() > 0 ? View.VISIBLE : View.GONE);
          }

          @Override
          public void afterTextChanged(Editable s) {
            String inputText = s.toString();
            if (inputText.isEmpty()) {

              mBookMarks.setVisibility(
                  Utils.getBookmarks(requireActivity()).size() > 0 ? View.VISIBLE : View.GONE);

              mBookMark.setVisibility(View.GONE);
              mSendButton.setImageDrawable(
                  Utils.getDrawable(R.drawable.ic_help, requireActivity()));

              mSendButton.setOnClickListener(
                  new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                      Intent examples = new Intent(requireActivity(), ExamplesActivity.class);
                      startActivity(examples);
                    }
                  });

            } else {

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
                              new CommandsAdapter(Commands.getPackageInfo(packageNamePrefix + "."));
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
                          mCommandsAdapter = new CommandsAdapter(Commands.getCommand(s.toString()));
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

              mBookMark.setImageDrawable(
                  Utils.getDrawable(
                      Utils.isBookmarked(s.toString().trim(), requireActivity())
                          ? R.drawable.ic_bookmark_added
                          : R.drawable.ic_add_bookmark,
                      requireActivity()));

              mBookMark.setVisibility(View.VISIBLE);

              mBookMark.setOnClickListener(
                  v -> {
                    if (Utils.isBookmarked(s.toString().trim(), requireActivity())) {
                      Utils.deleteFromBookmark(s.toString().trim(), requireActivity());
                      Utils.snackBar(
                              view,
                              getString(R.string.bookmark_removed_message, s.toString().trim()))
                          .show();
                    } else {
                      addBookmark(s.toString().trim(), view);
                    }
                    mBookMark.setImageDrawable(
                        Utils.getDrawable(
                            Utils.isBookmarked(s.toString().trim(), requireActivity())
                                ? R.drawable.ic_bookmark_added
                                : R.drawable.ic_add_bookmark,
                            requireActivity()));

                    mBookMarks.setVisibility(
                        Utils.getBookmarks(requireActivity()).size() > 0
                            ? View.VISIBLE
                            : View.GONE);
                  });

              mSendButton.setImageDrawable(
                  Utils.getDrawable(R.drawable.ic_send, requireActivity()));
              mSendButton.setOnClickListener(
                  new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      if (adbConnection != null) {
                        putCommand();
                      } else {

                        mHistoryButton.setVisibility(View.VISIBLE);

                        if (mHistory == null) {
                          mHistory = new ArrayList<>();
                        }
                        mHistory.add(mCommand.getText().toString());

                        new MaterialAlertDialogBuilder(requireActivity())
                            .setTitle("Error")
                            .setMessage(getString(R.string.otg_not_connected))
                            .setPositiveButton("OK", (dialogInterface, i) -> {})
                            .show();
                      }
                    }
                  });
            }
          }
        });

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

    // Glow otg symbol when adb connection successfull
    if (adbConnection != null) {
      mCable.setColorFilter(Utils.getColor(R.color.colorGreen, requireActivity()));
    } else {
      mCable.clearColorFilter();
    }

    mSettingsButton.setTooltipText("Settings");
    mSettingsButton.setOnClickListener(
        v -> {
          Intent settingsIntent = new Intent(requireActivity(), SettingsActivity.class);
          startActivity(settingsIntent);
        });

    handler =
        new Handler(Looper.getMainLooper()) {
          @Override
          public void handleMessage(@NonNull android.os.Message msg) {
            switch (msg.what) {
              case DEVICE_FOUND:
                closeWaiting();
                terminalView.setVisibility(View.VISIBLE);
                initCommand();
                showKeyboard();
                break;

              case CONNECTING:
                waitingDialog();
                closeKeyboard();
                terminalView.setVisibility(View.VISIBLE);
                break;

              case DEVICE_NOT_FOUND:
                closeWaiting();
                closeKeyboard();
                terminalView.setVisibility(View.VISIBLE);
                adbConnection = null; // Fix this issue
                break;

              case FLASHING:
                Toast.makeText(requireContext(), "Flashing", Toast.LENGTH_SHORT).show();
                break;

              case INSTALLING_PROGRESS:
                Toast.makeText(requireContext(), "Progress", Toast.LENGTH_SHORT).show();
                break;
            }
          }
        };

    /*------------------------------------------------------*/

    AdbBase64 base64 = new MyAdbBase64();
    try {
      adbCrypto =
          AdbCrypto.loadAdbKeyPair(
              base64,
              new File(requireActivity().getFilesDir(), "private_key"),
              new File(requireActivity().getFilesDir(), "public_key"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (adbCrypto == null) {
      try {
        adbCrypto = AdbCrypto.generateAdbKeyPair(base64);
        adbCrypto.saveAdbKeyPair(
            new File(requireActivity().getFilesDir(), "private_key"),
            new File(requireActivity().getFilesDir(), "public_key"));
      } catch (Exception e) {
        Log.w(Const.TAG, "fail to generate and save key-pair", e);
      }
    }

    IntentFilter filter = new IntentFilter();
    filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
    filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
    filter.addAction(MessageOtg.USB_PERMISSION);

    ContextCompat.registerReceiver(
        requireContext(), mUsbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

    // Check USB
    UsbDevice device = requireActivity().getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
    if (device != null) {
      System.out.println("From Intent!");
      asyncRefreshAdbConnection(device);
    } else {
      System.out.println("From onCreate!");
      for (String k : mManager.getDeviceList().keySet()) {
        UsbDevice usbDevice = mManager.getDeviceList().get(k);
        handler.sendEmptyMessage(CONNECTING);
        if (mManager.hasPermission(usbDevice)) {
          asyncRefreshAdbConnection(usbDevice);
        } else {
          mManager.requestPermission(
              usbDevice,
              PendingIntent.getBroadcast(
                  requireActivity().getApplicationContext(),
                  0,
                  new Intent(MessageOtg.USB_PERMISSION),
                  PendingIntent.FLAG_IMMUTABLE));
        }
      }
    }

    mCommand.setOnEditorActionListener(this);
    mCommand.setOnKeyListener(this);

    return view;
  }

  private void closeWaiting() {
    mWaitingDialog.dismiss();
  }

  private void waitingDialog() {
    View dialogView =
        LayoutInflater.from(requireActivity()).inflate(R.layout.loading_dialog_layout, null);
    ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

    mWaitingDialog =
        new MaterialAlertDialogBuilder(requireActivity())
            .setCancelable(false)
            .setView(dialogView)
            .setTitle("Waiting for device")
            .show();

    progressBar.setVisibility(View.VISIBLE);
  }

  public void asyncRefreshAdbConnection(final UsbDevice device) {
    if (device != null) {
      new Thread() {
        @Override
        public void run() {
          final UsbInterface intf = findAdbInterface(device);
          try {
            setAdbInterface(device, intf);
          } catch (Exception e) {
            Log.w(Const.TAG, "setAdbInterface(device, intf) fail", e);
          }
        }
      }.start();
    }
  }

  BroadcastReceiver mUsbReceiver =
      new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
          String action = intent.getAction();
          Log.d(Const.TAG, "mUsbReceiver onReceive => " + action);
          if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            String deviceName = device.getDeviceName();
            if (mDevice != null && mDevice.getDeviceName().equals(deviceName)) {
              try {
                Log.d(Const.TAG, "setAdbInterface(null, null)");
                setAdbInterface(null, null);
              } catch (Exception e) {
                Log.w(Const.TAG, "setAdbInterface(null,null) failed", e);
              }
            }
          } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            asyncRefreshAdbConnection(device);
          } else if (MessageOtg.USB_PERMISSION.equals(action)) {
            System.out.println("From receiver!");
            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            handler.sendEmptyMessage(CONNECTING);
            if (mManager.hasPermission(usbDevice)) asyncRefreshAdbConnection(usbDevice);
            else
              mManager.requestPermission(
                  usbDevice,
                  PendingIntent.getBroadcast(
                      requireContext().getApplicationContext(),
                      0,
                      new Intent(MessageOtg.USB_PERMISSION),
                      PendingIntent.FLAG_IMMUTABLE));
          }
        }
      };

  // searches for an adb interface on the given USB device
  private UsbInterface findAdbInterface(UsbDevice device) {
    int count = device.getInterfaceCount();
    for (int i = 0; i < count; i++) {
      UsbInterface intf = device.getInterface(i);
      if (intf.getInterfaceClass() == 255
          && intf.getInterfaceSubclass() == 66
          && intf.getInterfaceProtocol() == 1) {
        return intf;
      }
    }
    return null;
  }

  // Sets the current USB device and interface
  private synchronized boolean setAdbInterface(UsbDevice device, UsbInterface intf)
      throws IOException, InterruptedException {
    if (adbConnection != null) {
      adbConnection.close();
      adbConnection = null;
      mDevice = null;
    }

    if (device != null && intf != null) {
      UsbDeviceConnection connection = mManager.openDevice(device);
      if (connection != null) {
        if (connection.claimInterface(intf, false)) {
          handler.sendEmptyMessage(CONNECTING);
          adbConnection = AdbConnection.create(new UsbChannel(connection, intf), adbCrypto);
          adbConnection.connect();
          // TODO: DO NOT DELETE IT, I CAN'T EXPLAIN WHY
          adbConnection.open("shell:exec date");

          mDevice = device;
          handler.sendEmptyMessage(DEVICE_FOUND);
          return true;
        } else {
          connection.close();
        }
      }
    }

    handler.sendEmptyMessage(DEVICE_NOT_FOUND);

    mDevice = null;
    return false;
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mUsbReceiver != null) {
      requireContext().unregisterReceiver(mUsbReceiver);
    }
    try {
      if (adbConnection != null) {
        adbConnection.close();
        adbConnection = null;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Define a Handler instance

  private void initCommand() {
    // Open the shell stream of ADB
    logs.setText("");
    try {
      stream = adbConnection.open("shell:");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return;
    } catch (IOException e) {
      e.printStackTrace();
      return;
    } catch (InterruptedException e) {
      e.printStackTrace();
      return;
    }

    // Start the receiving thread
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                while (!stream.isClosed()) {
                  try {
                    // Print each thing we read from the shell stream
                    final String[] output = {new String(stream.read(), "US-ASCII")};
                    handler.post(
                        new Runnable() {
                          @Override
                          public void run() {
                            if (user == null) {
                              user = output[0].substring(0, output[0].lastIndexOf("/") + 1);
                            } else if (output[0].contains(user)) {
                              System.out.println("End => " + user);
                            }

                            logs.append(output[0]);

                            scrollView.post(
                                new Runnable() {
                                  @Override
                                  public void run() {
                                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                    mCommand.requestFocus();
                                  }
                                });
                          }
                        });
                  } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return;
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                  } catch (IOException e) {
                    e.printStackTrace();
                    return;
                  }
                }
              }
            })
        .start();
  }

  private void putCommand() {

    if (!mCommand.getText().toString().isEmpty()) {
      // We become the sending thread

      try {
        String cmd = mCommand.getText().toString();
        if (cmd.equalsIgnoreCase("clear")) {
          String log = logs.getText().toString();
          String[] logSplit = log.split("\n");
          logs.setText(logSplit[logSplit.length - 1]);
        } else if (cmd.equalsIgnoreCase("exit")) {
          requireActivity().finish();
        } else {
          stream.write((cmd + "\n").getBytes("UTF-8"));
        }
        mCommand.setText("");
      } catch (IOException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } else Toast.makeText(requireContext(), "No command", Toast.LENGTH_SHORT).show();
  }

  public void open(View view) {}

  public void showKeyboard() {
    mCommand.requestFocus();
    InputMethodManager imm =
        (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
  }

  public void closeKeyboard() {
    View view = requireActivity().getCurrentFocus();
    if (view != null) {
      InputMethodManager imm =
          (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  @Override
  public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    /* We always return false because we want to dismiss the keyboard */
    if (adbConnection != null && actionId == EditorInfo.IME_ACTION_DONE) {
      putCommand();
    }

    return true;
  }

  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_ENTER) {
      /* Just call the onEditorAction function to handle this for us */
      return onEditorAction((TextView) v, EditorInfo.IME_ACTION_DONE, event);
    } else {
      return false;
    }
  }

  public double getBrightness(int color) {
    int red = Color.red(color);
    int green = Color.green(color);
    int blue = Color.blue(color);
    return 0.299 * red + 0.587 * green + 0.114 * blue;
  }

  private List<String> getRecentCommands() {
    List<String> mRecentCommands = new ArrayList<>(mHistory);
    Collections.reverse(mRecentCommands);
    return mRecentCommands;
  }

  private int lastIndexOf(String s, String splitTxt) {
    return s.lastIndexOf(splitTxt);
  }

  private String splitPrefix(String s, int i) {
    String[] splitPrefix = {s.substring(0, lastIndexOf(s, " ")), s.substring(lastIndexOf(s, " "))};
    return splitPrefix[i].trim();
  }

  private void addBookmark(String bookmark, View view) {

    boolean switchState = adapter.getSavedSwitchState("Override maximum bookmarks limit");

    if (Utils.getBookmarks(requireActivity()).size() <= 4) {
      Utils.addToBookmark(bookmark, requireActivity());
      Utils.snackBar(view, getString(R.string.bookmark_added_message, bookmark)).show();
    } else {
      if (switchState) {
        Utils.addToBookmark(bookmark, requireActivity());
        Utils.snackBar(view, getString(R.string.bookmark_added_message, bookmark)).show();
      } else {
        Utils.snackBar(view, getString(R.string.bookmark_limit_reached)).show();
      }
    }
  }
}
