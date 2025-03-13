package in.hridayan.ashell.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.animation.content.Content;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.transition.Hold;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.fragments.home.HomeFragment;
import in.hridayan.ashell.fragments.home.WifiAdbFragment;
import in.hridayan.ashell.items.WifiAdbDevicesItem;
import in.hridayan.ashell.shell.wifiadb.WifiAdbShell;
import in.hridayan.ashell.ui.ToastUtils;
import in.hridayan.ashell.ui.dialogs.ActionDialogs;
import in.hridayan.ashell.ui.dialogs.WifiAdbDialogUtils;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.viewmodels.MainViewModel;
import java.util.List;
import in.hridayan.ashell.R;

public class WifiAdbDevicesAdapter
    extends RecyclerView.Adapter<WifiAdbDevicesAdapter.DeviceViewHolder> {

  private final List<WifiAdbDevicesItem> deviceList;
  private final Context context;
  private MainViewModel mainViewModel;

  public WifiAdbDevicesAdapter(
      Context context, List<WifiAdbDevicesItem> deviceList, MainViewModel mainViewModel) {
    this.context = context;
    this.deviceList = deviceList;
    this.mainViewModel = mainViewModel;
  }

  @NonNull
  @Override
  public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.rv_wifi_adb_devices, parent, false);
    return new DeviceViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
    WifiAdbDevicesItem device = deviceList.get(position);

    holder.deviceTextView.setText(device.getIpPort());

    holder.cardConnectedDevices.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          mainViewModel.setSelectedWifiAdbDevice(device.getIpPort());
          View devicesDialogView = ActionDialogs.getWifiAdbDevicesDialogView();
          if (devicesDialogView != null) devicesDialogView.setVisibility(View.GONE);
          WifiAdbDialogUtils.goToWifiAdbFragment((AppCompatActivity) context);
        });

    holder.iconDisconnect.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          new Thread(
                  () -> {
                    ((Activity) context)
                        .runOnUiThread(
                            () -> {
                              if (isDisconnected(context, device.getIpPort())) {
                                deviceList.remove(position);
                                notifyDataSetChanged();
                                ToastUtils.showToast(
                                    context,
                                    context.getString(R.string.disconnected),
                                    ToastUtils.LENGTH_SHORT);
                              } else {
                                ToastUtils.showToast(
                                    context,
                                    context.getString(R.string.failed),
                                    ToastUtils.LENGTH_SHORT);
                              }
                            });
                  })
              .start();
        });
  }

  private static boolean isDisconnected(Context context, String ipPort) {
    boolean isDisconnected =
        WifiAdbShell.exec(context, WifiAdbShell.adbPath(context), "disconnect", ipPort);
    return isDisconnected;
  }

  @Override
  public int getItemCount() {
    return deviceList.size();
  }

  static class DeviceViewHolder extends RecyclerView.ViewHolder {
    MaterialTextView deviceTextView;
    MaterialCardView cardConnectedDevices;
    ImageView iconDisconnect;

    DeviceViewHolder(View itemView) {
      super(itemView);
      deviceTextView = itemView.findViewById(R.id.device);
      cardConnectedDevices = itemView.findViewById(R.id.wifi_adb_devices_card);
      iconDisconnect = itemView.findViewById(R.id.iconDisconnect);
    }
  }
}
