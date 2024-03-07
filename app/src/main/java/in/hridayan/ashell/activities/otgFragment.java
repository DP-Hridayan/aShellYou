package in.hridayan.ashell.fragments;

import android.telecom.InCallService;
import androidx.preference.PreferenceManager;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
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
import in.hridayan.ashell.UI.SpinnerDialog;
import in.hridayan.ashell.activities.SettingsActivity;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.utils.Const;
import in.hridayan.ashell.utils.MessageOtg;
import in.hridayan.ashell.utils.SettingsItem;
import in.hridayan.ashell.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class otgFragment extends Fragment
    implements TextView.OnEditorActionListener, View.OnKeyListener {
  private Handler handler;
  private UsbDevice mDevice;
  private TextView tvStatus;
  private MaterialTextView logs;
  private AppCompatImageButton mCable;
  private AdbCrypto adbCrypto;
  private AdbConnection adbConnection;
  private UsbManager mManager;
  private BottomNavigationView mNav;
  private LinearLayoutCompat terminalView;
  private MaterialButton mSettingsButton;
  private TextInputEditText edCommand;
  private FloatingActionButton btnRun;
  private ScrollView scrollView;
  private SettingsAdapter adapter;
  private SettingsItem settingsList;
  private String user = null;
  private List<String> mHistory = null, mResult = null;

  private boolean doubleBackToExitPressedOnce = false;
  private AdbStream stream;
  private SpinnerDialog waitingDialog;

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_otg, container, false);

    List<SettingsItem> settingsList = new ArrayList<>();
    SettingsAdapter adapter = new SettingsAdapter(settingsList, requireContext());

    int statusBarColor = getResources().getColor(R.color.StatusBar);
    double brightness = getBrightness(statusBarColor);
    boolean isLightStatusBar = brightness > 0.5;

    View decorView = requireActivity().getWindow().getDecorView();
    if (isLightStatusBar) {
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    } else {
      decorView.setSystemUiVisibility(0);
    }

    mCable = view.findViewById(R.id.otg_cable);
    mNav = view.findViewById(R.id.bottom_nav_bar);
    logs = view.findViewById(R.id.logs);
    mSettingsButton = view.findViewById(R.id.settings_otg);
    terminalView = view.findViewById(R.id.terminalView);
    edCommand = view.findViewById(R.id.edCommand);
    btnRun = view.findViewById(R.id.btnRun);
    scrollView = view.findViewById(R.id.scrollView);
    mManager = (UsbManager) requireActivity().getSystemService(Context.USB_SERVICE);



    if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("Don't show beta otg warning", true)) 
        {
      new MaterialAlertDialogBuilder(requireActivity())
          .setTitle("Warning")
          .setMessage(getString(R.string.otg_not_connected))
          .setPositiveButton("Accept", (dialogInterface, i) -> {})
          .setNegativeButton("Don't show again", (dialogInterface, i) -> {
              
                             PreferenceManager.getDefaultSharedPreferences(requireContext())
              .edit()
              .putBoolean("Don't show beta otg warning", false)
              .apply();
                    
          })
          .show();
    }

    btnRun.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (adbConnection != null) {

              putCommand();
            } else {
              new MaterialAlertDialogBuilder(requireActivity())
                  .setTitle("Warning")
                  .setMessage(getString(R.string.otg_not_connected))
                  .setPositiveButton("OK", (dialogInterface, i) -> {})
                  .show();
            }
          }
        });

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

    edCommand.setImeActionLabel("Run", EditorInfo.IME_ACTION_DONE);
    edCommand.setOnEditorActionListener(this);
    edCommand.setOnKeyListener(this);

    return view;
  }

  private void closeWaiting() {
    if (waitingDialog != null) waitingDialog.dismiss();
  }

  private void waitingDialog() {
    closeWaiting();
    waitingDialog =
        SpinnerDialog.displayDialog(
            requireActivity(),
            "Important",
            "You may need to accept a prompt on the target device if you are connecting "
                + "to it for the first time from this device.",
            false);
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
                                    edCommand.requestFocus();
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

    if (!edCommand.getText().toString().isEmpty()) {
      // We become the sending thread
      try {
        String cmd = edCommand.getText().toString();
        if (cmd.equalsIgnoreCase("clear")) {
          String log = logs.getText().toString();
          String[] logSplit = log.split("\n");
          logs.setText(logSplit[logSplit.length - 1]);
        } else if (cmd.equalsIgnoreCase("exit")) {
          requireActivity().finish();
        } else {
          stream.write((cmd + "\n").getBytes("UTF-8"));
        }
        edCommand.setText("");
      } catch (IOException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } else Toast.makeText(requireContext(), "No command", Toast.LENGTH_SHORT).show();
  }

  public void open(View view) {}

  public void showKeyboard() {
    edCommand.requestFocus();
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
}
