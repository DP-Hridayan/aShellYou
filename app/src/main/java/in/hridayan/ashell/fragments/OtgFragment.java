package in.hridayan.ashell.fragments;

import in.hridayan.ashell.UI.BottomNavUtils;
import in.hridayan.ashell.config.Const;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.cgutman.adblib.AdbBase64;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AdbStream;
import com.cgutman.adblib.UsbChannel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.Hold;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.BehaviorFAB;
import in.hridayan.ashell.UI.BehaviorFAB.FabExtendingOnScrollListener;
import in.hridayan.ashell.UI.BehaviorFAB.FabExtendingOnScrollViewListener;
import in.hridayan.ashell.UI.BehaviorFAB.FabOtgScrollDownListener;
import in.hridayan.ashell.UI.BehaviorFAB.FabOtgScrollUpListener;
import in.hridayan.ashell.UI.BehaviorFAB.OtgShareButtonListener;
import in.hridayan.ashell.UI.DialogUtils;
import in.hridayan.ashell.UI.KeyboardUtils;
import in.hridayan.ashell.UI.ToastUtils;
import in.hridayan.ashell.activities.MainActivity;
import in.hridayan.ashell.adapters.CommandsAdapter;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.databinding.FragmentOtgBinding;
import in.hridayan.ashell.items.SettingsItem;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.DeviceUtils;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.OtgUtils;
import in.hridayan.ashell.utils.OtgUtils.MessageOtg;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.AboutViewModel;
import in.hridayan.ashell.viewmodels.ExamplesViewModel;
import in.hridayan.ashell.viewmodels.MainViewModel;
import in.hridayan.ashell.viewmodels.SettingsViewModel;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// This fragment is taken from ADB OTG by Khun Htetz Naing and then modified by DP Hridayan to match
// aShell You theme

public class OtgFragment extends Fragment
    implements TextView.OnEditorActionListener, View.OnKeyListener {
  private Handler handler;
  private UsbDevice mDevice;
  private AdbCrypto adbCrypto;
  private AdbConnection adbConnection;
  private UsbManager mManager;
  private BottomNavigationView mNav;
  private SettingsAdapter adapter;
  private AlertDialog mWaitingDialog;
  private String user = null, deviceName;
  private final Handler mHandler = new Handler(Looper.getMainLooper());
  private boolean isKeyboardVisible,
      sendButtonClicked = false,
      isSendDrawable = false,
      isReceiverRegistered = false;
  private List<String> mHistory = null, shellOutput, history;
  private View view;
  private AdbStream stream;
  private Context context;
  private MainViewModel mainViewModel;
  private FragmentOtgBinding binding;
  private CommandsAdapter mCommandsAdapter;
  private OnFragmentInteractionListener mListener;
  private SettingsViewModel settingsViewModel;
  private ExamplesViewModel examplesViewModel;
  private AboutViewModel aboutViewModel;
  private static WeakReference<View> settingsButtonRef, sendButtonRef;

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
  public void onPause() {
    super.onPause();

    mainViewModel.setPreviousFragment(Const.OTG_FRAGMENT);

    if (isKeyboardVisible) KeyboardUtils.closeKeyboard(requireActivity(), view);

    BottomNavUtils.hideNavSmoothly(mNav);
  }

  @Override
  public void onResume() {
    super.onResume();

    setExitTransition(null);

    // if bottom navigation is not visible , then make it visible
    BottomNavUtils.showNavSmoothly(mNav);

    KeyboardUtils.disableKeyboard(context, requireActivity(), view);

    if (Preferences.getSpecificCardVisibility("warning_usb_debugging") && adbConnection == null)
      binding.usbWarningCard.setVisibility(View.VISIBLE);
    else if (binding.usbWarningCard.getVisibility() == View.VISIBLE)
      binding.usbWarningCard.setVisibility(View.GONE);

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
    if (mUsbReceiver != null && isReceiverRegistered) {
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

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    setExitTransition(null);

    binding = FragmentOtgBinding.inflate(inflater, container, false);

    view = binding.getRoot();

    context = requireContext();

    mManager = (UsbManager) requireActivity().getSystemService(Context.USB_SERVICE);
    mNav = requireActivity().findViewById(R.id.bottom_nav_bar);
    settingsButtonRef = new WeakReference<>(binding.settingsButton);
    sendButtonRef = new WeakReference<>(binding.sendButton);

    // initialize viewmodel
    initializeViewModels();

    binding.rvCommands.addOnScrollListener(new FabExtendingOnScrollListener(binding.pasteButton));

    binding.rvCommands.setLayoutManager(new LinearLayoutManager(requireActivity()));

    List<SettingsItem> settingsList = new ArrayList<>();
    adapter =
        new SettingsAdapter(
            settingsList, context, requireActivity(), aboutViewModel, examplesViewModel);

    new FabExtendingOnScrollViewListener(binding.scrollView, binding.saveButton);
    new FabOtgScrollUpListener(binding.scrollView, binding.scrollUpButton);
    new FabOtgScrollDownListener(binding.scrollView, binding.scrollDownButton);
    new OtgShareButtonListener(binding.scrollView, binding.shareButton);
    BehaviorFAB.pasteAndUndo(
        binding.pasteButton, binding.undoButton, binding.commandEditText, context);

    BehaviorFAB.handleTopAndBottomArrow(
        binding.scrollUpButton,
        binding.scrollDownButton,
        null,
        binding.scrollView,
        context,
        "otg_shell");

    // Method to hide and show floating action buttons when keyboard is showing or not
    KeyboardUtils.attachVisibilityListener(
        requireActivity(),
        visible -> {
          isKeyboardVisible = visible;
          if (visible) buttonsVisibilityGone();
          else buttonsVisibilityVisible();
        });

    if (!isKeyboardVisible) {
      mNav.setVisibility(View.VISIBLE);
    }

    // Show the info card by checking preferences
    if (Preferences.getSpecificCardVisibility("warning_usb_debugging") && adbConnection == null) {
      binding.usbWarningCard.setVisibility(View.VISIBLE);
    } else if (binding.usbWarningCard.getVisibility() == View.VISIBLE) {
      binding.usbWarningCard.setVisibility(View.GONE);
    }

    if (isSendDrawable) {
      binding.sendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_send, requireActivity()));

    } else {
      binding.sendButton.setImageDrawable(Utils.getDrawable(R.drawable.ic_help, requireActivity()));
      binding.sendButton.setOnClickListener(
          v -> {
            HapticUtils.weakVibrate(v);
            goToExamples();
          });
    }

    // Logic for changing the command send button depending on the text on the EditText

    binding.commandEditText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {

            isSendDrawable = binding.commandEditText.getText() != null;

            binding.commandInputLayout.setError(null);
          }

          @Override
          public void afterTextChanged(Editable s) {
            binding.commandEditText.requestFocus();

            if (s.toString().trim().isEmpty()) {
              binding.commandInputLayout.setEndIconVisible(false);
              binding.rvCommands.setVisibility(View.GONE);
              binding.sendButton.setImageDrawable(
                  Utils.getDrawable(R.drawable.ic_help, requireActivity()));

              binding.sendButton.setOnClickListener(
                  v -> {
                    HapticUtils.weakVibrate(v);
                    goToExamples();
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
                            binding.rvCommands.setLayoutManager(
                                new LinearLayoutManager(requireActivity()));
                          }

                          if (isAdded()) {
                            binding.rvCommands.setAdapter(mCommandsAdapter);
                          }
                          binding.rvCommands.setVisibility(View.VISIBLE);
                          mCommandsAdapter.setOnItemClickListener(
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
                          mCommandsAdapter =
                              new CommandsAdapter(Commands.getCommand(s.toString(), context));
                          if (isAdded()) {
                            binding.rvCommands.setLayoutManager(
                                new LinearLayoutManager(requireActivity()));
                          }

                          binding.rvCommands.setAdapter(mCommandsAdapter);
                          binding.rvCommands.setVisibility(View.VISIBLE);
                          mCommandsAdapter.setOnItemClickListener(
                              (command, v) -> {
                                binding.commandEditText.setText(
                                    command.contains(" <") ? command.split("<")[0] : command);

                                binding.commandEditText.setSelection(
                                    binding.commandEditText.getText().length());
                              });
                        }
                      });

              binding.commandInputLayout.setEndIconDrawable(
                  Utils.getDrawable(
                      Utils.isBookmarked(s.toString().trim(), requireActivity())
                          ? R.drawable.ic_bookmark_added
                          : R.drawable.ic_add_bookmark,
                      requireActivity()));

              binding.commandInputLayout.setEndIconVisible(true);

              binding.commandInputLayout.setEndIconOnClickListener(
                  v -> {
                    HapticUtils.weakVibrate(v);
                    if (Utils.isBookmarked(s.toString().trim(), requireActivity())) {
                      Utils.deleteFromBookmark(s.toString().trim(), requireActivity());
                      Utils.snackBar(
                              view,
                              getString(R.string.bookmark_removed_message, s.toString().trim()))
                          .show();
                    } else {
                      Utils.addBookmarkIconOnClickListener(s.toString().trim(), view, context);
                    }
                    binding.commandInputLayout.setEndIconDrawable(
                        Utils.getDrawable(
                            Utils.isBookmarked(s.toString().trim(), requireActivity())
                                ? R.drawable.ic_bookmark_added
                                : R.drawable.ic_add_bookmark,
                            requireActivity()));
                  });

              binding.sendButton.setImageDrawable(
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
                Toast.makeText(context, getString(R.string.connected), Toast.LENGTH_SHORT).show();
                break;

              case CONNECTING:
                //   Toast.makeText(context, "connecting", Toast.LENGTH_SHORT).show();
                if (adbConnection == null) {
                  binding.usbWarningCard.setVisibility(View.GONE);
                  waitingDialog(context);
                }

                break;

              case DEVICE_NOT_FOUND:
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
    isReceiverRegistered = true;

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
        if (mManager.hasPermission(usbDevice)) asyncRefreshAdbConnection(usbDevice);
        else {
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
    crossOnClickListener();

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

    binding.commandEditText.setOnEditorActionListener(this);
    binding.commandEditText.setOnKeyListener(this);
    mainViewModel.setHomeFragment(Const.OTG_FRAGMENT);

    return view;
  }

  // Waiting dialog that asks user to accept the usb debugging prompt on the other device
  private void waitingDialog(Context context) {
    if (isAdded()) {
      View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
      ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

      mWaitingDialog =
          new MaterialAlertDialogBuilder(context)
              .setCancelable(false)
              .setView(dialogView)
              .setTitle(context.getString(R.string.waiting_device))
              .setPositiveButton(
                  getString(R.string.ok),
                  (dialogInterface, i) -> {
                    if (mListener != null) mListener.onRequestReset();
                  })
              .show();
      progressBar.setVisibility(View.VISIBLE);
    }
  }

  // Close the waiting dialog
  private void closeWaiting() {
    if (mWaitingDialog != null && mWaitingDialog.isShowing()) mWaitingDialog.dismiss();
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
          && intf.getInterfaceProtocol() == 1) return intf;
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
        } else connection.close();
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
                          if (user == null)
                            user = output[0].substring(0, output[0].lastIndexOf("/") + 1);
                          else if (output[0].contains(user)) System.out.println("End => " + user);

                          binding.shellOutput.append(output[0]);

                          binding.scrollView.post(
                              () -> {
                                binding.scrollView.fullScroll(binding.scrollView.FOCUS_DOWN);
                                binding.commandEditText.requestFocus();
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

    binding.shellOutput.setText("");
    binding.outputBgCardView.setVisibility(View.VISIBLE);
    modeButtonOnClickListener();
  }

  @Override
  public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
    HapticUtils.weakVibrate(v);
    /* We always return false because we want to dismiss the keyboard */
    if (adbConnection != null && actionId == EditorInfo.IME_ACTION_DONE) putCommand();

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
      binding.commandEditText.setText(sharedText);
      binding.commandEditText.requestFocus();
      binding.commandEditText.setSelection(binding.commandEditText.getText().length());
    }
  }

  // Clear the shell output
  private void clearAll() {
    String log = binding.shellOutput.getText().toString();
    String[] logSplit = log.split("\n");
    binding.shellOutput.setText(logSplit[logSplit.length - 1]);

    if (binding.scrollUpButton.getVisibility() == View.VISIBLE)
      binding.scrollUpButton.setVisibility(View.GONE);

    if (binding.scrollDownButton.getVisibility() == View.VISIBLE)
      binding.scrollDownButton.setVisibility(View.GONE);

    binding.saveButton.setVisibility(View.GONE);
    binding.shareButton.setVisibility(View.GONE);
    showBottomNav();
  }

  // Button showing the mode and connected device
  private void modeButtonOnClickListener() {
    binding.modeButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          if (mDevice != null) {
            String connectedDevice = mDevice.getProductName();
            DialogUtils.connectedDeviceDialog(
                context, adbConnection == null ? getString(R.string.none) : connectedDevice);

          } else DialogUtils.connectedDeviceDialog(context, getString(R.string.none));
        });
  }

  // Animate the bottom navigation bar to appear
  private void showBottomNav() {
    if (getActivity() != null && getActivity() instanceof MainActivity)
      ((MainActivity) getActivity()).mNav.animate().translationY(0);
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
    if (binding.pasteButton.getVisibility() == View.GONE) {
      if (!sendButtonClicked) setVisibilityWithDelay(binding.pasteButton, 100);
      else if (binding.scrollView.getChildAt(0).getHeight() != 0) {
        setVisibilityWithDelay(binding.saveButton, 100);
        setVisibilityWithDelay(binding.shareButton, 100);
      }
    }
  }

  // Onclick listener for save button
  private void saveButtonOnClickListener() {
    binding.saveButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          history = mHistory;
          String sb = null, fileName = null;

          switch (Preferences.getSavePreference()) {
            case Const.ALL_OUTPUT:
              sb = binding.shellOutput.getText().toString();
              fileName = "otg_output" + DeviceUtils.getCurrentDateTime();
              break;
            case Const.LAST_COMMAND_OUTPUT:
              sb = Utils.lastCommandOutput(binding.shellOutput.getText().toString());
              fileName = Utils.generateFileName(mHistory) + DeviceUtils.getCurrentDateTime();
              break;
            default:
              break;
          }
          boolean saved = Utils.saveToFile(sb, requireActivity(), fileName);
          if (saved) Preferences.setLastSavedFileName(fileName + ".txt");

          // Dialog showing if the output has been saved or not
          DialogUtils.outputSavedDialog(context, saved);
        });
  }

  // OnClick listener for share button
  private void shareButtonOnClickListener() {
    binding.shareButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          String fileName = Utils.generateFileName(mHistory);
          Utils.shareOutput(
              requireActivity(),
              context,
              fileName,
              Utils.lastCommandOutput(binding.shellOutput.getText().toString()));
        });
  }

  // OnClick listener for settings button
  private void settingsButtonOnClickListener() {
    binding.settingsButton.setTooltipText(getString(R.string.settings));
    binding.settingsButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);

          goToSettings();
        });
  }

  // OnClick listener for history button
  private void historyButtonOnClickListener() {
    binding.historyButton.setTooltipText(getString(R.string.history));
    binding.historyButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);

          if (history == null)
            ToastUtils.showToast(context, R.string.no_history, ToastUtils.LENGTH_SHORT);
          else {
            PopupMenu popupMenu = new PopupMenu(requireContext(), binding.commandEditText);
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

  // Clear button onClick listener
  private void clearButtonOnClickListener() {
    binding.clearButton.setTooltipText(getString(R.string.clear_screen));
    binding.clearButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          boolean switchState = Preferences.getClear();

          if (binding.shellOutput.getText().toString().isEmpty())
            ToastUtils.showToast(context, R.string.nothing_to_clear, ToastUtils.LENGTH_SHORT);
          else if (switchState)
            new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(getString(R.string.clear_everything))
                .setMessage(getString(R.string.clear_all_message))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> clearAll())
                .show();
          else clearAll();
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

  // Send button onClick listener
  private void sendButtonOnClickListener() {
    binding.sendButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          KeyboardUtils.closeKeyboard(requireActivity(), v);
          modeButtonOnClickListener();
          sendButtonClicked = true;
          binding.pasteButton.hide();
          binding.undoButton.hide();

          if (binding.rvCommands.getVisibility() == View.VISIBLE)
            binding.rvCommands.setVisibility(View.GONE);

          if (adbConnection != null) {

            if (mHistory == null) mHistory = new ArrayList<>();

            mHistory.add(binding.commandEditText.getText().toString());
            putCommand();
          } else deviceConnectionErrorMessage();
        });
  }

  private void putCommand() {

    if (!binding.commandEditText.getText().toString().isEmpty()) {
      binding.outputBgCardView.setVisibility(View.VISIBLE);

      // We become the sending thread
      try {
        String cmd = binding.commandEditText.getText().toString();

        if (cmd.equalsIgnoreCase("clear")) clearAll();
        else if (cmd.equalsIgnoreCase("logcat"))
          ToastUtils.showToast(
              context,
              "currently continous running operations are not working properly",
              ToastUtils.LENGTH_LONG);
        else if (cmd.equalsIgnoreCase("exit")) requireActivity().finish();
        else stream.write((cmd + "\n").getBytes(StandardCharsets.UTF_8));

        binding.commandEditText.setText("");
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
    binding.commandInputLayout.setError(getString(R.string.device_not_connected));
    binding.commandInputLayout.setErrorIconDrawable(
        Utils.getDrawable(R.drawable.ic_cancel, requireActivity()));
    binding.commandInputLayout.setErrorIconOnClickListener(
        t -> binding.commandEditText.setText(null));

    DialogUtils.otgConnectionErrDialog(context);
  }

  // The cross which dismisses the card asking to turn on usb debugging
  private void crossOnClickListener() {
    binding.cross.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          binding.usbWarningCard.setVisibility(View.GONE);
          Preferences.setSpecificCardVisibility("warning_usb_debugging", false);
        });
  }

  // Onclick listener of Instruction button on the card
  private void instructionsButtonOnClickListener() {
    binding.instructionsButton.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          Utils.openUrl(context, Const.URL_OTG_INSTRUCTIONS);
        });
  }

  // Get the command when using Use feature
  private void handleUseCommand() {
    if (mainViewModel.getUseCommand() != null) {
      updateInputField(mainViewModel.getUseCommand());
      mainViewModel.setUseCommand(null);
    }
  }

  // Open command examples fragment
  private void goToExamples() {
    setExitTransition(new Hold());
    examplesViewModel.setRVPositionAndOffset(null);
    examplesViewModel.setToolbarExpanded(true);
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
    setExitTransition(new Hold());
    settingsViewModel.setRVPositionAndOffset(null);
    settingsViewModel.setToolbarExpanded(true);
    SettingsFragment fragment = new SettingsFragment();

    requireActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .addSharedElement(binding.settingsButton, Const.SETTINGS_TO_SETTINGS)
        .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
        .addToBackStack(fragment.getClass().getSimpleName())
        .commit();
  }

  // initialize viewModels
  private void initializeViewModels() {
    mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

    settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

    examplesViewModel = new ViewModelProvider(requireActivity()).get(ExamplesViewModel.class);

    aboutViewModel = new ViewModelProvider(requireActivity()).get(AboutViewModel.class);
  }

  public static View getSettingsButtonView() {
    return settingsButtonRef != null ? settingsButtonRef.get() : null;
  }

  public static View getSendButtonView() {
    return sendButtonRef != null ? sendButtonRef.get() : null;
  }
}
