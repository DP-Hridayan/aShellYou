package in.hridayan.ashell.ui.dialogs;

import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.WifiAdbDevicesAdapter;
import in.hridayan.ashell.fragments.PairingFragment;
import in.hridayan.ashell.fragments.home.HomeFragment;
import in.hridayan.ashell.fragments.home.WifiAdbFragment;
import in.hridayan.ashell.items.WifiAdbDevicesItem;
import in.hridayan.ashell.ui.bottomsheets.WifiAdbBottomSheet;
import java.util.List;

public class WifiAdbDialogUtils {
  public static void updateDeviceList(
      List<WifiAdbDevicesItem> deviceList, WifiAdbDevicesAdapter adapter, List<String> devices) {
    deviceList.clear();
    for (String ipPort : devices) {
      deviceList.add(new WifiAdbDevicesItem(ipPort));
    }
    adapter.notifyDataSetChanged();
  }

  public static void pairOtherDevice(Context context, Activity activity) {
    WifiAdbBottomSheet.showPairAndConnectBottomSheet(context, activity);
  }

  public static void goToPairingFragment(AppCompatActivity activity, Fragment homeFragment) {
    PairingFragment fragment = new PairingFragment();

    homeFragment.setExitTransition(null); // Avoid conflicts

    activity
        .getSupportFragmentManager()
        .beginTransaction()
        .setCustomAnimations(
            R.anim.fragment_enter, R.anim.fragment_exit,
            R.anim.fragment_pop_enter, R.anim.fragment_pop_exit)
        .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
        .addToBackStack(fragment.getClass().getSimpleName())
        .commit();
  }

  public static void goToWifiAdbFragment(AppCompatActivity activity) {
    WifiAdbFragment fragment = new WifiAdbFragment();

    // Get HomeFragment using the provided activity instance
    HomeFragment homeFragment =
        (HomeFragment)
            activity
                .getSupportFragmentManager()
                .findFragmentByTag(HomeFragment.class.getSimpleName());

    if (homeFragment != null) {
      homeFragment.setExitTransition(null); // Avoid conflicts
    }

    activity
        .getSupportFragmentManager()
        .beginTransaction()
        .setReorderingAllowed(true)
        .setCustomAnimations(
            R.anim.fragment_enter, R.anim.fragment_exit_fast,
            R.anim.fragment_pop_enter, R.anim.fragment_pop_exit)
        .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
        .addToBackStack(fragment.getClass().getSimpleName())
        .commit();
  }
}
