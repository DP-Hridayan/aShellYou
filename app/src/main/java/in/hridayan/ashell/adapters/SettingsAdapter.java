package in.hridayan.ashell.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.materialswitch.MaterialSwitch;
import in.hridayan.ashell.R;
import in.hridayan.ashell.fragments.ExamplesFragment;
import in.hridayan.ashell.fragments.AboutFragment;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.SettingsItem;
import in.hridayan.ashell.utils.Utils;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

  private final List<SettingsItem> settingsList;
  private final Context context;
  private int currentTheme;
  private Activity activity;

  public SettingsAdapter(
      List<SettingsItem> settingsList, Context context, int currentTheme, Activity activity) {
    this.settingsList = settingsList;
    this.context = context;
    this.currentTheme = currentTheme;
    this.activity = activity;
  }

  public SettingsAdapter(List<SettingsItem> settingsList, Context context) {
    this.settingsList = settingsList;
    this.context = context;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_settings, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    SettingsItem settingsItem = settingsList.get(position);
    Drawable symbolDrawable = settingsItem.getSymbol(context);

    holder.symbolImageView.setImageDrawable(symbolDrawable);
    holder.titleTextView.setText(settingsItem.getTitle());
    holder.descriptionTextView.setText(settingsItem.getDescription());
    holder.descriptionTextView.setVisibility(
        TextUtils.isEmpty(settingsItem.getDescription()) ? View.GONE : View.VISIBLE);

    // Set the switch state without triggering the listener
    holder.switchView.setOnCheckedChangeListener(null);
    holder.switchView.setVisibility(settingsItem.hasSwitch() ? View.VISIBLE : View.GONE);
    holder.switchView.setChecked(settingsItem.isChecked());
    holder.switchView.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          settingsItem.setChecked(isChecked);
          settingsItem.saveSwitchState(context);

          if (settingsItem.getId().equals("id_amoled_theme")) {
            if ((context.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES) {
              applyTheme(isChecked);
            }
          }
        });

    View.OnClickListener clickListener =
        v -> {
          Intent intent;
          HapticUtils.weakVibrate(v, context);
          switch (settingsItem.getId()) {
            case "id_unhide_cards":
              Preferences.setSpecificCardVisibility(context, "warning_usb_debugging", true);
              Toast.makeText(
                      context, context.getString(R.string.unhide_cards_message), Toast.LENGTH_SHORT)
                  .show();
              break;

            case "id_examples":
              ((FragmentActivity) activity)
                  .getSupportFragmentManager()
                  .beginTransaction()
                  .replace(R.id.fragment_container, new ExamplesFragment())
                  .addToBackStack(null)
                  .commit();
              break;

            case "id_about":
              ((FragmentActivity) activity)
                  .getSupportFragmentManager()
                  .beginTransaction()
                  .replace(R.id.fragment_container, new AboutFragment())
                  .addToBackStack(null)
                  .commit();
              break;

            case "id_default_language":
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent = new Intent(Settings.ACTION_APP_LOCALE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
              }
              break;

            case "id_default_launch_mode":
              Utils.defaultWorkingModeDialog(context);
              break;

            case "id_save_preference":
              Utils.savePreferenceDialog(context);
              break;

            case "id_local_adb_mode":
              Utils.localAdbModeDialog(context);
              break;

            default:
              return;
          }
        };

    holder.settingsItemLayout.setOnClickListener(clickListener);
    int paddingInPixels = (int) (Utils.convertDpToPixel(30, context));
    ViewGroup.MarginLayoutParams layoutParams =
        (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
    layoutParams.bottomMargin = position == getItemCount() - 1 ? paddingInPixels : 0;
    holder.itemView.setLayoutParams(layoutParams);
  }

  private void applyTheme(boolean isAmoledTheme) {
    int themeId =
        isAmoledTheme ? R.style.ThemeOverlay_aShellYou_AmoledTheme : R.style.aShellYou_AppTheme;
    context.setTheme(themeId);
    currentTheme = themeId;
    ((AppCompatActivity) context).recreate();
  }

  @Override
  public int getItemCount() {
    return settingsList.size();
  }

  public boolean getSavedSwitchState(String id) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getBoolean(id, false);
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    ImageView symbolImageView;
    TextView titleTextView, descriptionTextView;
    MaterialSwitch switchView;
    ConstraintLayout settingsItemLayout;

    public ViewHolder(View itemView) {
      super(itemView);
      symbolImageView = itemView.findViewById(R.id.symbol_image_view);
      titleTextView = itemView.findViewById(R.id.setting_title);
      descriptionTextView = itemView.findViewById(R.id.setting_description);
      switchView = itemView.findViewById(R.id.setting_switch);
      settingsItemLayout = itemView.findViewById(R.id.settings_item_layout);
    }
  }
}
