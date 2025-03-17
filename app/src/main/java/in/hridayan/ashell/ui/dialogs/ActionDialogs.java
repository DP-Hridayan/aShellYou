package in.hridayan.ashell.ui.dialogs;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.WifiAdbDevicesAdapter;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.items.WifiAdbDevicesItem;
import in.hridayan.ashell.shell.wifiadb.WifiAdbConnectedDevices;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.MainViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles dialogs for bookmark-related actions, such as viewing, sorting, and deleting bookmarks.
 */
public class ActionDialogs {

  private static MaterialCardView connectNewDevice;
  private static MaterialCardView devicesDialog;
  private static MaterialCardView modeDialog;
  private static View devicesDialogView;
  private static View modeDialogView;
  private static View startViewDeviceDialog;
  private static ViewGroup rootViewDevicesDialog;
  private static ViewGroup rootViewModeDialog;

  /** Displays a dialog listing all bookmarked items. */
  public static void bookmarksDialog(
      Context context, TextInputEditText mCommand, TextInputLayout mCommandInput) {
    List<String> bookmarks = Utils.getBookmarks(context);
    int totalBookmarks = bookmarks.size();

    String title = context.getString(R.string.bookmarks) + " (" + totalBookmarks + ")";
    CharSequence[] bookmarkItems = bookmarks.toArray(new CharSequence[0]);

    new MaterialAlertDialogBuilder(context)
        .setTitle(title)
        .setItems(
            bookmarkItems,
            (dialog, which) -> {
              mCommand.setText(bookmarks.get(which));
              mCommand.setSelection(mCommand.getText().length());
            })
        .setPositiveButton(context.getString(R.string.cancel), null)
        .setNegativeButton(
            context.getString(R.string.sort),
            (dialog, i) -> sortingDialog(context, mCommand, mCommandInput))
        .setNeutralButton(
            context.getString(R.string.delete_all),
            (dialog, i) -> deleteDialog(context, mCommand, mCommandInput))
        .show();
  }

  /** Displays a confirmation dialog before deleting all bookmarks. */
  public static void deleteDialog(
      Context context, TextInputEditText mCommand, TextInputLayout mCommandInput) {
    new MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.confirm_delete))
        .setMessage(context.getString(R.string.confirm_delete_message))
        .setPositiveButton(
            context.getString(R.string.ok),
            (dialog, i) -> deleteAllBookmarks(context, mCommand, mCommandInput))
        .setNegativeButton(
            context.getString(R.string.cancel),
            (dialog, i) -> bookmarksDialog(context, mCommand, mCommandInput))
        .setOnCancelListener(
            dialog -> restoreBookmarksDialogIfNotEmpty(context, mCommand, mCommandInput))
        .show();
  }

  /** Displays a sorting options dialog for bookmarks. */
  public static void sortingDialog(
      Context context, TextInputEditText mCommand, TextInputLayout mCommandInput) {
    CharSequence[] sortingOptions = {
      context.getString(R.string.sort_A_Z),
      context.getString(R.string.sort_Z_A),
      context.getString(R.string.sort_newest),
      context.getString(R.string.sort_oldest)
    };

    int currentSortingOption = Preferences.getSortingOption();
    final int[] selectedOption = {currentSortingOption};

    new MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.sort))
        .setSingleChoiceItems(
            sortingOptions, currentSortingOption, (dialog, which) -> selectedOption[0] = which)
        .setPositiveButton(
            context.getString(R.string.ok),
            (dialog, which) -> {
              Preferences.setSortingOption(selectedOption[0]);
              bookmarksDialog(context, mCommand, mCommandInput);
            })
        .setNegativeButton(
            context.getString(R.string.cancel),
            (dialog, i) -> bookmarksDialog(context, mCommand, mCommandInput))
        .setOnCancelListener(dialog -> bookmarksDialog(context, mCommand, mCommandInput))
        .show();
  }

  /** Deletes all bookmarks and updates the UI accordingly. */
  private static void deleteAllBookmarks(
      Context context, TextInputEditText mCommand, TextInputLayout mCommandInput) {
    List<String> bookmarks = Utils.getBookmarks(context);
    for (String item : bookmarks) {
      Utils.deleteFromBookmark(item, context);
    }

    if (mCommand.getText().length() != 0) {
      mCommandInput.setEndIconDrawable(R.drawable.ic_add_bookmark);
    } else {
      mCommandInput.setEndIconVisible(false);
    }
  }

  /** Restores the bookmarks dialog if there are still bookmarks after cancellation. */
  private static void restoreBookmarksDialogIfNotEmpty(
      Context context, TextInputEditText mCommand, TextInputLayout mCommandInput) {
    if (!Utils.getBookmarks(context).isEmpty()) {
      bookmarksDialog(context, mCommand, mCommandInput);
    }
  }

  public static void wifiAdbDevicesDialog(
      Context context,
      AppCompatActivity activity,
      View startView,
      MainViewModel viewModel,
      Fragment homeFragment) {

    rootViewDevicesDialog = activity.findViewById(android.R.id.content);
    devicesDialogView = DialogUtils.inflateDialogView(activity, R.layout.dialog_wifi_adb_devices);

    FrameLayout dialogContainer = devicesDialogView.findViewById(R.id.dialog_container);
    devicesDialog = devicesDialogView.findViewById(R.id.dialog_card);
    connectNewDevice = devicesDialogView.findViewById(R.id.connectDevice);
    MaterialCardView cardNoDevicesWarning =
        devicesDialogView.findViewById(R.id.noDevicesWarningCard);
    MaterialCardView cardDevicesHint = devicesDialogView.findViewById(R.id.devicesHintCard);
    startViewDeviceDialog = startView;
    RecyclerView recyclerView = devicesDialogView.findViewById(R.id.rv_wifi_adb_devices);

    recyclerView.setLayoutManager(new LinearLayoutManager(activity));

    List<WifiAdbDevicesItem> deviceList = new ArrayList<>();
    WifiAdbDevicesAdapter adapter = new WifiAdbDevicesAdapter(activity, deviceList, viewModel);
    recyclerView.setAdapter(adapter);

    // Initially hide the dialog to prevent flickering
    devicesDialogView.setAlpha(0f);
    rootViewDevicesDialog.addView(devicesDialogView);

    WifiAdbConnectedDevices.getConnectedDevices(
        activity,
        new WifiAdbConnectedDevices.ConnectedDevicesCallback() {
          @Override
          public void onDevicesListed(@NonNull List<String> devices) {
            WifiAdbDialogUtils.updateDeviceList(deviceList, adapter, devices);

            updateInfoCard(deviceList, cardNoDevicesWarning, cardDevicesHint);

            recyclerView.post(
                () -> {
                  DialogAnimation.showDialogWithTransition(
                      startViewDeviceDialog, devicesDialog, devicesDialogView, false);
                });
          }

          @Override
          public void onFailure(String errorMessage) {
            updateInfoCard(deviceList, cardNoDevicesWarning, cardDevicesHint);
                    
            recyclerView.post(
                () ->
                    DialogAnimation.showDialogWithTransition(
                        startViewDeviceDialog, devicesDialog, devicesDialogView, false));
          }
        });

    dialogContainer.setOnClickListener(
        v ->
            DialogAnimation.dismissDialogWithTransition(
                startViewDeviceDialog,
                devicesDialog,
                devicesDialogView,
                rootViewDevicesDialog,
                false));

    connectNewDevice.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          chooseWifiAdbModeDialog(context, activity, homeFragment, startView);
        });
  }

  private static void chooseWifiAdbModeDialog(
      Context context, AppCompatActivity activity, Fragment homeFragment, View startButton) {

    rootViewModeDialog = activity.findViewById(android.R.id.content);
    modeDialogView = DialogUtils.inflateDialogView(activity, R.layout.dialog_wifi_adb_mode);

    FrameLayout dialogContainer = modeDialogView.findViewById(R.id.dialog_container);
    modeDialog = modeDialogView.findViewById(R.id.dialog);
    MaterialCardView thisDeviceCard = modeDialogView.findViewById(R.id.modeThisDevice);
    MaterialCardView otherDeviceCard = modeDialogView.findViewById(R.id.modeOtherDevice);

    modeDialogView.setAlpha(0f);
    rootViewModeDialog.addView(modeDialogView);

    DialogAnimation.showDialogWithTransition(connectNewDevice, modeDialog, modeDialogView, true);
    devicesDialogView.setVisibility(View.INVISIBLE);

    thisDeviceCard.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          devicesDialogView.setVisibility(View.GONE);
          modeDialogView.setVisibility(View.GONE);
          WifiAdbDialogUtils.goToPairingFragment(activity, homeFragment);
        });

    otherDeviceCard.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          modeDialogView.setVisibility(View.GONE);
          devicesDialogView.setVisibility(View.GONE);
          startButton.setVisibility(View.VISIBLE);
          WifiAdbDialogUtils.pairOtherDevice(context, activity);
        });

    dialogContainer.setOnClickListener(
        v -> {
          devicesDialogView.setVisibility(View.VISIBLE);
          DialogAnimation.dismissDialogWithTransition(
              connectNewDevice, modeDialog, modeDialogView, rootViewModeDialog, true);
        });
  }

  public static void updateInfoCard(
      List<WifiAdbDevicesItem> deviceList,
      MaterialCardView cardNoDevicesWarning,
      MaterialCardView cardDevicesHint) {
    if (deviceList.isEmpty()) {
      cardNoDevicesWarning.setVisibility(View.VISIBLE);
      cardDevicesHint.setVisibility(View.GONE);
    } else {
      cardNoDevicesWarning.setVisibility(View.GONE);
      cardDevicesHint.setVisibility(View.VISIBLE);
    }
  }

  public static void dismissDevicesDialog() {
    DialogAnimation.dismissDialogWithTransition(
        startViewDeviceDialog, devicesDialog, devicesDialogView, rootViewDevicesDialog, false);
  }

  public static void dismissModeDialog() {
    devicesDialogView.setVisibility(View.VISIBLE);
    DialogAnimation.dismissDialogWithTransition(
        connectNewDevice, modeDialog, modeDialogView, rootViewModeDialog, true);
  }

  public static boolean isWifiAdbDevicesDialogVisible() {
    return devicesDialogView != null && devicesDialogView.getVisibility() == View.VISIBLE;
  }

  public static boolean isWifiAdbModeDialogVisible() {
    return modeDialogView != null && modeDialogView.getVisibility() == View.VISIBLE;
  }

  public static boolean isAnyDialogVisible() {
    return isWifiAdbDevicesDialogVisible() || isWifiAdbModeDialogVisible();
  }

  public static View getWifiAdbModeDialogView() {
    return modeDialogView;
  }

  public static View getWifiAdbDevicesDialogView() {
    return devicesDialogView;
  }
}
