package in.hridayan.ashell.fragments;

import static in.hridayan.ashell.utils.OtgUtils.MessageOtg.CONNECTING;
import static in.hridayan.ashell.utils.OtgUtils.MessageOtg.DEVICE_FOUND;
import static in.hridayan.ashell.utils.OtgUtils.MessageOtg.DEVICE_NOT_FOUND;
import static in.hridayan.ashell.utils.OtgUtils.MessageOtg.FLASHING;
import static in.hridayan.ashell.utils.OtgUtils.MessageOtg.INSTALLING_PROGRESS;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Button;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cgutman.adblib.AdbBase64;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AdbStream;
import com.cgutman.adblib.UsbChannel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.BehaviorFAB;
import in.hridayan.ashell.UI.BehaviorFAB.FabExtendingOnScrollListener;
import in.hridayan.ashell.UI.BehaviorFAB.FabExtendingOnScrollViewListener;
import in.hridayan.ashell.UI.BehaviorFAB.FabOtgScrollDownListener;
import in.hridayan.ashell.UI.BehaviorFAB.FabOtgScrollUpListener;
import in.hridayan.ashell.UI.BehaviorFAB.OtgShareButtonListener;
import in.hridayan.ashell.UI.CoordinatedNestedScrollView;
import in.hridayan.ashell.UI.KeyboardUtils;
import in.hridayan.ashell.activities.MainActivity;
import in.hridayan.ashell.adapters.CommandsAdapter;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.OtgUtils;
import in.hridayan.ashell.utils.OtgUtils.Const;
import in.hridayan.ashell.utils.OtgUtils.MessageOtg;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.SettingsItem;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.MainViewModel;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// This fragment is taken from ADB OTG by Khun Htetz Naing and then modified by DP Hridayan to match
// aShell You theme

public class otgShellFragment extends Fragment
    implements TextView.OnEditorActionListener, View.OnKeyListener {
  private Handler handler;
  private UsbDevice mDevice;
  private MaterialTextView logs;
  private AppCompatImageButton mCable, dismissCard;
  private AdbCrypto adbCrypto;
  private AdbConnection adbConnection;
  private UsbManager mManager;
  private BottomNavigationView mNav;
  private CommandsAdapter mCommandsAdapter;
  private Button mModeButton;
  private LinearLayoutCompat terminalView;
  private MaterialButton mSettingsButton,
      mBookMarks,
      mHistoryButton,
      mClearButton,
      instructionsButton;
  private RecyclerView mRecyclerViewCommands;
  private SettingsAdapter adapter;
  private MaterialCardView mShellCard, mWarningUsbDebugging;
  private TextInputLayout mCommandInput;
  private TextInputEditText mCommand;
  private FloatingActionButton mSendButton, mUndoButton, mTopButton, mBottomButton, mShareButton;
  private ExtendedFloatingActionButton mPasteButton, mSaveButton;
  private CoordinatedNestedScrollView scrollView;
  private AlertDialog mWaitingDialog;
  private String user = null, deviceName;
  private final Handler mHandler = new Handler(Looper.getMainLooper());
  private boolean isKeyboardVisible, sendButtonClicked = false, isSendDrawable = false;
  private List<String> mHistory = null, shellOutput, history;
  private View view;
  private AdbStream stream;
  private Context context;
  private MainViewModel mainViewModel;

  private OnFragmentInteractionListener mListener;

  public interface OnFragmentInteractionListener {
    void onRequestReset();
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Override
  public void onAttach(@NonNull Context mContext) {
    super.onAttach(mContext);
    context = mContext;
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    KeyboardUtils.disableKeyboard(context, requireActivity(), view);
    if (Preferences.getSpecificCardVisibility(context, "warning_usb_debugging")
        && adbConnection == null) {
      mWarningUsbDebugging.setVisibility(View.VISIBLE);
    } else if (mWarningUsbDebugging.getVisibility() == View.VISIBLE) {
      mWarningUsbDebugging.setVisibility(View.GONE);
    }

    handleUseCommand();

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
    if (mUsbReceiver != null) {
      requireContext().unregisterReceiver(mUsbReceiver);
    }
    try {
      if (adbConnection != null) {
        adbConnection.close();
        adbConnection = null;
      }
    } catch (IOException e) {
      Log.e("OTGShellFragment", "Error closing ADB connection", e);
    }
  }

  /*------------------------------------------------------*/

  // USB Receiver

  BroadcastReceiver mUsbReceiver =
      new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
          String action = intent.getAction();
          Log.d(Const.TAG, "mUsbReceiver onReceive => " + action);

          if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
              String deviceName = device.getDeviceName();
              if (mDevice != null && mDevice.getDeviceName().equals(deviceName)) {
                try {
                  Log.d(Const.TAG, "setAdbInterface(null, null)");
                  setAdbInterface(null, null);
                } catch (Exception e) {
                  Log.w(Const.TAG, "setAdbInterface(null, null) failed", e);
                }
              }
            }
          } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
              asyncRefreshAdbConnection(device);
              mListener.onRequestReset();
            }
          } else if (MessageOtg.USB_PERMISSION.equals(action)) {
            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (usbDevice != null) {
              handler.sendEmptyMessage(CONNECTING);
              if (mManager.hasPermission(usbDevice)) {
                asyncRefreshAdbConnection(usbDevice);
              } else {
                PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(
                        requireContext().getApplicationContext(),
                        0,
                        new Intent(MessageOtg.USB_PERMISSION),
                        PendingIntent.FLAG_IMMUTABLE);
                mManager.requestPermission(usbDevice, pendingIntent);
              }
            } else {
              Log.w(Const.TAG, "USB_DEVICE permission action received but usbDevice is null");
            }
          }
        }
      };

  /*------------------------------------------------------*/

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    context = getContext();
    if (context == null) {
      return view;
    }
    view = inflater.inflate(R.layout.fragment_otg, container, false);

    List<SettingsItem> settingsList = new ArrayList<>();
    adapter = new SettingsAdapter(settingsList, requireContext());
    logs = view.findViewById(R.id.logs);
    mBookMarks = view.findViewById(R.id.bookmarks);
    mBottomButton = view.findViewById(R.id.fab_down);
    mCable = view.findViewById(R.id.otg_cable);
    mClearButton = view.findViewById(R.id.clear);
    mModeButton = view.findViewById(R.id.mode_button);
    mCommand = view.findViewById(R.id.shell_command);
    mCommandInput = view.findViewById(R.id.shell_command_layout);
    dismissCard = view.findViewById(R.id.dimiss_card);
    mHistoryButton = view.findViewById(R.id.history);
    mManager = (UsbManager) requireActivity().getSystemService(Context.USB_SERVICE);
    mNav = requireActivity().findViewById(R.id.bottom_nav_bar);
    mPasteButton = view.findViewById(R.id.paste_button);
    mRecyclerViewCommands = view.findViewById(R.id.rv_commands);
    mSaveButton = view.findViewById(R.id.save_button);
    mSendButton = view.findViewById(R.id.send);
    mSettingsButton = view.findViewById(R.id.settings);
    mShellCard = view.findViewById(R.id.otg_shell_card);
    scrollView = view.findViewById(R.id.scrollView);
    mShareButton = view.findViewById(R.id.fab_share);
    terminalView = view.findViewById(R.id.terminalView);
    mTopButton = view.findViewById(R.id.fab_up);
    mUndoButton = view.findViewById(R.id.fab_undo);
    mWarningUsbDebugging = view.findViewById(R.id.warning_usb_debugging);
    instructionsButton = view.findViewById(R.id.instructions_button);

    // initialize viewmodel
    mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

    mRecyclerViewCommands.addOnScrollListener(new FabExtendingOnScrollListener(mPasteButton));

    mRecyclerViewCommands.setLayoutManager(new LinearLayoutManager(requireActivity()));

    new FabExtendingOnScrollViewListener(scrollView, mSaveButton);
    new FabOtgScrollUpListener(scrollView, mTopButton);
    new FabOtgScrollDownListener(scrollView, mBottomButton);
    new OtgShareButtonListener(scrollView, mShareButton);
    BehaviorFAB.pasteAndUndo(mPasteButton, mUndoButton, mCommand, context);

    BehaviorFAB.handleTopAndBottomArrow(
        mTopButton, mBottomButton, null, scrollView, context, "otg_shell");

    // Method to hide and show floating action buttons when keyboard is showing or not
    KeyboardUtils.attachVisibilityListener(
        requireActivity(),
        visible -> {
          isKeyboardVisible = visible;
          if (visible) buttonsVisibilityGone();
          else buttonsVisibilityVisible();
        });

    mNav.setVisibility(View.VISIBLE);

    // Show the info card by checking preferences
    if (Preferences.getSpecificCardVisibility(context, "warning_usb_debugging")
        && adbConnection == null) {
      mWarningUsbDebugging.setVisibility(View.VISIBLE);
    } else if (mWarningUsbDebugging.getVisibility() == View.VISIBLE) {
      mWarningUsbDebugging.setVisibility(View.GONE);
    }

    if (isSendDrawable) {
      mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_send, requireActivity()));

    } else {
      mSendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_help, requireActivity()));
      mSendButton.setOnClickListener(
          v -> {
            HapticUtils.weakVibrate(v, context);
            requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                    .setCustomAnimations(
                            R.anim.fragment_enter,
                            R.anim.fragment_exit,
                            R.anim.fragment_pop_enter,
                            R.anim.fragment_pop_exit
                    )
                .replace(R.id.fragment_container, new ExamplesFragment())
                .addToBackStack(null)
                .commit();
          });
    }

    // Logic for changing the command send button depending on the text on the EditText

    mBookMarks.setVisibility(
        !Utils.getBookmarks(context).isEmpty() ? View.VISIBLE : View.GONE);

    mCommand.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {

            isSendDrawable = mCommand.getText() != null;

            mCommandInput.setError(null);

            mBookMarks.setVisibility(
                !Utils.getBookmarks(context).isEmpty() ? View.VISIBLE : View.GONE);
          }

          @Override
          public void afterTextChanged(Editable s) {
            mCommand.requestFocus();

            String inputText = s.toString();
            if (inputText.isEmpty()) {

              mBookMarks.setVisibility(
                  !Utils.getBookmarks(context).isEmpty() ? View.VISIBLE : View.GONE);

              mCommandInput.setEndIconVisible(false);
              mSendButton.setImageDrawable(
                  Utils.getDrawable(R.drawable.ic_help, requireActivity()));

              mSendButton.setOnClickListener(
                  v -> {
                    HapticUtils.weakVibrate(v, context);
                    requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                            .setCustomAnimations(
                                    R.anim.fragment_enter,
                                    R.anim.fragment_exit,
                                    R.anim.fragment_pop_enter,
                                    R.anim.fragment_pop_exit
                            )
                        .replace(R.id.fragment_container, new ExamplesFragment())
                        .addToBackStack(null)
                        .commit();
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
                                mCommand.setText(
                                    command.contains(" <") ? command.split("<")[0] : command);

                                mCommand.setSelection(mCommand.getText().length());
                              });
                        }
                      });

              mCommandInput.setEndIconDrawable(
                  Utils.getDrawable(
                      Utils.isBookmarked(s.toString().trim(), requireActivity())
                          ? R.drawable.ic_bookmark_added
                          : R.drawable.ic_add_bookmark,
                      requireActivity()));

              mCommandInput.setEndIconVisible(true);

              mCommandInput.setEndIconOnClickListener(
                  v -> {
                    HapticUtils.weakVibrate(v, context);
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

                    mBookMarks.setVisibility(
                        !Utils.getBookmarks(context).isEmpty()
                            ? View.VISIBLE
                            : View.GONE);
                  });

              mSendButton.setImageDrawable(
                  Utils.getDrawable(R.drawable.ic_send, requireActivity()));
              sendButtonOnClickListener();
            }
          }
        });

    handler =
        new Handler(Looper.getMainLooper()) {
          @Override
          public void handleMessage(@NonNull android.os.Message msg) {
            if (!isAdded()) {
              return;
            }
            switch (msg.what) {
              case DEVICE_FOUND:
                initCommand();
                if (adbConnection != null) {
                  // Glow otg symbol when adb connection successfull
                  mCable.setColorFilter(Utils.getColor(R.color.green, requireActivity()));
                }
                Toast.makeText(context, getString(R.string.connected), Toast.LENGTH_SHORT).show();
                break;

              case CONNECTING:
                //   Toast.makeText(context, "connecting", Toast.LENGTH_SHORT).show();
                if (adbConnection == null) {
                  mWarningUsbDebugging.setVisibility(View.GONE);
                  waitingDialog(context);
                }

                break;

              case DEVICE_NOT_FOUND:
                mCable.clearColorFilter();
                // Toast.makeText(context, "device not found!", Toast.LENGTH_SHORT).show();
                adbConnection = null; // Fix this issue
                break;

              case FLASHING:
                Toast.makeText(requireContext(), getString(R.string.flashing), Toast.LENGTH_SHORT)
                    .show();
                break;

              case INSTALLING_PROGRESS:
                Toast.makeText(requireContext(), getString(R.string.progress), Toast.LENGTH_SHORT)
                    .show();
                break;
            }
          }
        };

    /*------------------------------------------------------*/

    AdbBase64 base64 = new OtgUtils.MyAdbBase64();
    try {
      adbCrypto =
          AdbCrypto.loadAdbKeyPair(
              base64,
              new File(requireActivity().getFilesDir(), "private_key"),
              new File(requireActivity().getFilesDir(), "public_key"));
    } catch (Exception e) {
      Log.e("OTGShellFragment", "Error loading keypair", e);
    }

    if (adbCrypto == null) {
      try {
        adbCrypto = AdbCrypto.generateAdbKeyPair(base64);
        adbCrypto.saveAdbKeyPair(
            new File(requireActivity().getFilesDir(), "private_key"),
            new File(requireActivity().getFilesDir(), "public_key"));
      } catch (Exception e) {
        Log.w(Const.TAG, getString(R.string.generate_key_failed), e);
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

    // OnClickListener of the Instruction button on the info card
    instructionsButtonOnClickListener();

    // The cross to dismiss the info card
    dismissCardOnClickListener();

    // Button to view the connected device
    modeButtonOnClickListener();

    // Settings button onClick listener
    settingsButtonOnClickListener();

    // Bookmarks button onClick listener
    bookmarksButtonOnClickListener();

    // History button onClick listener
    historyButtonOnClickListener();

    // Clear button onClick listener
    clearButtonOnClickListener();

    // Save button onclick listener
    saveButtonOnClickListener();

    // Share button onclickListener
    shareButtonOnClickListener();

    mCommand.setOnEditorActionListener(this);
    mCommand.setOnKeyListener(this);

    return view;
  }

  // Waiting dialog that asks user to accept the usb debugging prompt on the other device
  private void waitingDialog(Context context) {
    if (isAdded()) {
      View dialogView = LayoutInflater.from(context).inflate(R.layout.loading_dialog_layout, null);
      ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

      mWaitingDialog =
          new MaterialAlertDialogBuilder(context)
              .setCancelable(false)
              .setView(dialogView)
              .setTitle(context.getString(R.string.waiting_device))
              .setPositiveButton(
                  getString(R.string.ok),
                  (dialogInterface, i) -> {
                    if (mListener != null) {
                      mListener.onRequestReset();
                    }
                  })
              .show();
      progressBar.setVisibility(View.VISIBLE);
    }
  }

  // Close the waiting dialog
  private void closeWaiting() {
    if (mWaitingDialog != null && mWaitingDialog.isShowing()) {
      mWaitingDialog.dismiss();
    }
  }

  public void asyncRefreshAdbConnection(final UsbDevice device) {
    if (device != null) {
      new Thread(
              () -> {
                final UsbInterface intf = findAdbInterface(device);
                try {
                  setAdbInterface(device, intf);
                } catch (Exception e) {
                  Log.w(Const.TAG, getString(R.string.set_adb_interface_fail), e);
                }
              })
          .start();
    }
  }

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

  // Define a Handler instance

  private void initCommand() {
    // Open the shell stream of ADB

    try {
      stream = adbConnection.open("shell:");
    } catch (UnsupportedEncodingException e) {
      Log.e("OTGShellFragment", "Unsupported encoding", e);
      return;
    } catch (IOException e) {
      Log.e("OTGShellFragment", "Error opening shell stream", e);
      return;
    } catch (InterruptedException e) {
      Log.e("OTGShellFragment", "Interrupted opening shell stream", e);
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
                    final String[] output = {new String(stream.read(), StandardCharsets.US_ASCII)};
                    handler.post(
                        () -> {
                          if (user == null) {
                            user = output[0].substring(0, output[0].lastIndexOf("/") + 1);
                          } else if (output[0].contains(user)) {
                            System.out.println("End => " + user);
                          }

                          logs.append(output[0]);

                          scrollView.post(
                              () -> {
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                mCommand.requestFocus();
                              });
                        });
                  } catch (UnsupportedEncodingException e) {
                    Log.e("OTGShellFragment", "Unsupported encoding", e);
                    return;
                  } catch (InterruptedException e) {
                    Log.e("OTGShellFragment", "Interrupted reading shell stream", e);
                    return;
                  } catch (IOException e) {
                    Log.e("OTGShellFragment", "Error reading shell stream", e);
                    return;
                  }
                }
              }
            })
        .start();

    mClearButton.setVisibility(View.VISIBLE);
    logs.setText("");
    mShellCard.setVisibility(View.VISIBLE);
    modeButtonOnClickListener();
  }

  

  @Override
  public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    HapticUtils.weakVibrate(v, context);
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

  private void setVisibilityWithDelay(View view, int delayMillis) {
    new Handler(Looper.getMainLooper())
        .postDelayed(() -> view.setVisibility(View.VISIBLE), delayMillis);
  }

  // Put the text shared from outside the app into the input field (edit text)
  public void updateInputField(String sharedText) {
    if (sharedText != null) {
      mCommand.setText(sharedText);
      mCommand.requestFocus();
      mCommand.setSelection(mCommand.getText().length());
    }
  }

  // Clear the shell output
  private void clearAll() {
    String log = logs.getText().toString();
    String[] logSplit = log.split("\n");
    logs.setText(logSplit[logSplit.length - 1]);

    if (mTopButton.getVisibility() == View.VISIBLE) {
      mTopButton.setVisibility(View.GONE);
    }
    if (mBottomButton.getVisibility() == View.VISIBLE) {
      mBottomButton.setVisibility(View.GONE);
    }
    mClearButton.setVisibility(View.GONE);
    mSaveButton.setVisibility(View.GONE);
    mShareButton.setVisibility(View.GONE);
    showBottomNav();
  }

  // Button showing the mode and connected device
  private void modeButtonOnClickListener() {
    mModeButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          if (mDevice != null) {
            String connectedDevice = mDevice.getProductName();
            Utils.connectedDeviceDialog(
                context, adbConnection == null ? getString(R.string.none) : connectedDevice);

          } else {
            Utils.connectedDeviceDialog(context, getString(R.string.none));
          }
        });
  }

  // Animate the bottom navigation bar to appear
  private void showBottomNav() {
    if (getActivity() != null && getActivity() instanceof MainActivity) {
      ((MainActivity) getActivity()).mNav.animate().translationY(0);
    }
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
    if (mPasteButton.getVisibility() == View.GONE) {
      if (!sendButtonClicked) {
        setVisibilityWithDelay(mPasteButton, 100);
      } else if (scrollView.getChildAt(0).getHeight() != 0) {
        setVisibilityWithDelay(mSaveButton, 100);
        setVisibilityWithDelay(mShareButton, 100);
      }
    }
  }

  // Onclick listener for save button
  private void saveButtonOnClickListener() {
    mSaveButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          history = mHistory;
          String sb = null, fileName = null;

          switch (Preferences.getSavePreference(context)) {
            case Preferences.ALL_OUTPUT:
              sb = logs.getText().toString();
              fileName = "otg_output" + Utils.getCurrentDateTime();
              break;
            case Preferences.LAST_COMMAND_OUTPUT:
              sb = Utils.lastCommandOutput(logs.getText().toString());
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

  // OnClick listener for share button
  private void shareButtonOnClickListener() {
    mShareButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          String fileName = Utils.generateFileName(mHistory);
          Utils.shareOutput(
              requireActivity(),
              context,
              fileName,
              Utils.lastCommandOutput(logs.getText().toString()));
        });
  }

  // OnClick listener for settings button
  private void settingsButtonOnClickListener() {
    mSettingsButton.setTooltipText(getString(R.string.settings));
    mSettingsButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, getContext());
          requireActivity()
              .getSupportFragmentManager()
              .beginTransaction()
                  .setCustomAnimations(
                          R.anim.fragment_enter,
                          R.anim.fragment_exit,
                          R.anim.fragment_pop_enter,
                          R.anim.fragment_pop_exit
                  )
              .replace(R.id.fragment_container, new SettingsFragment())
              .addToBackStack(null)
              .commit();
        });
  }

  // OnClick listener for history button
  private void historyButtonOnClickListener() {
    mHistoryButton.setTooltipText(getString(R.string.history));
    mHistoryButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
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
  }

  // Clear button onClick listener
  private void clearButtonOnClickListener() {
    mClearButton.setTooltipText(getString(R.string.clear_screen));
    mClearButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
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
        });
  }

  // Bookmarks button onClick listener
  private void bookmarksButtonOnClickListener() {
    mBookMarks.setTooltipText(getString(R.string.bookmarks));
    mBookMarks.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          Utils.bookmarksDialog(context, requireActivity(), mCommand, mCommandInput);
        });
  }

  // Send button onClick listener
  private void sendButtonOnClickListener() {
    mSendButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          KeyboardUtils.closeKeyboard(requireActivity(), v);
          modeButtonOnClickListener();
          sendButtonClicked = true;
          mPasteButton.hide();
          mUndoButton.hide();

          if (mRecyclerViewCommands.getVisibility() == View.VISIBLE) {
            mRecyclerViewCommands.setVisibility(View.GONE);
          }
          if (adbConnection != null) {
            mHistoryButton.setVisibility(View.VISIBLE);

            if (mHistory == null) {
              mHistory = new ArrayList<>();
            }

            mHistory.add(mCommand.getText().toString());
            putCommand();
          } else {
            deviceConnectionErrorMessage();
          }
        });
  }
    
   private void putCommand() {

    if (!mCommand.getText().toString().isEmpty()) {
      mShellCard.setVisibility(View.VISIBLE);
      mClearButton.setVisibility(View.VISIBLE);

      // We become the sending thread
      try {
        String cmd = mCommand.getText().toString();
        if (cmd.equalsIgnoreCase("clear")) {
          clearAll();
        } else if (cmd.equalsIgnoreCase("logcat")) {
          Toast.makeText(
                  context,
                  "currently continous running operations are not working properly",
                  Toast.LENGTH_LONG)
              .show();
        } else if (cmd.equalsIgnoreCase("exit")) {
          requireActivity().finish();
        } else {
          stream.write((cmd + "\n").getBytes(StandardCharsets.UTF_8));
        }
                
        mCommand.setText("");
      } catch (IOException e) {
        Log.e("OTGShellFragment", "Error writing command", e);
      } catch (InterruptedException e) {
        Log.e("OTGShellFragment", "Interrupted writing command", e);
      }
    } else
      Toast.makeText(requireContext(), getString(R.string.no_command), Toast.LENGTH_SHORT).show();
  }

  // Handles ui and feedback when otg device is not connected
  private void deviceConnectionErrorMessage() {
    mCommandInput.setError(getString(R.string.device_not_connected));
    mCommandInput.setErrorIconDrawable(Utils.getDrawable(R.drawable.ic_cancel, requireActivity()));
    mCommandInput.setErrorIconOnClickListener(t -> mCommand.setText(null));

    Utils.alignMargin(mSendButton);
    Utils.alignMargin(mCable);

    new MaterialAlertDialogBuilder(requireActivity())
        .setTitle(requireActivity().getString(R.string.error))
        .setMessage(requireActivity().getString(R.string.otg_not_connected))
        .setPositiveButton(requireActivity().getString(R.string.ok), (dialogInterface, i) -> {})
        .show();
  }

  // The cross which dismisses the card asking to turn on usb debugging
  private void dismissCardOnClickListener() {
    dismissCard.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          mWarningUsbDebugging.setVisibility(View.GONE);
          Preferences.setSpecificCardVisibility(context, "warning_usb_debugging", false);
        });
  }

  // Onclick listener of Instruction button on the card
  private void instructionsButtonOnClickListener() {
    instructionsButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v, context);
          Utils.openUrl(context, Preferences.otgInstructions);
        });
  }

  // Get the command when using Use feature
  private void handleUseCommand() {
    if (mainViewModel.getUseCommand() != null) {
      updateInputField(mainViewModel.getUseCommand());
      mainViewModel.setUseCommand(null);
    }
  }
}
