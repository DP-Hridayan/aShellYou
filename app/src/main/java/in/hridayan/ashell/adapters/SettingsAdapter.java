package in.hridayan.ashell.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
    private Activity activity;

  public SettingsAdapter(
          List<SettingsItem> settingsList, Context context, Activity activity) {
    this.settingsList = settingsList;
    this.context = context;
      this.activity = activity;
  }

  public SettingsAdapter(List<SettingsItem> settingsList, Context context) {
    this.settingsList = settingsList;
    this.context = context;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_settings, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    SettingsItem settingsItem = settingsList.get(position);
    holder.bind(settingsItem, position == getItemCount() - 1);
  }

  @Override
  public int getItemCount() {
    return settingsList.size();
  }

  private void applyTheme(boolean isAmoledTheme) {
    int themeId = isAmoledTheme ? R.style.ThemeOverlay_aShellYou_AmoledTheme : R.style.aShellYou_AppTheme;
    context.setTheme(themeId);
      ((AppCompatActivity) context).recreate();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
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

    void bind(SettingsItem settingsItem, boolean isLastItem) {
      symbolImageView.setImageDrawable(settingsItem.getSymbol(context));
      titleTextView.setText(settingsItem.getTitle());
      descriptionTextView.setText(settingsItem.getDescription());
      descriptionTextView.setVisibility(TextUtils.isEmpty(settingsItem.getDescription()) ? View.GONE : View.VISIBLE);

      setupSwitch(settingsItem);
      setupClickListener(settingsItem);
      setupItemMargin(isLastItem);
    }

    private void setupSwitch(SettingsItem settingsItem) {
      switchView.setVisibility(settingsItem.hasSwitch() ? View.VISIBLE : View.GONE);
      switchView.setChecked(settingsItem.isChecked());
      switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
        settingsItem.setChecked(isChecked);
        settingsItem.saveSwitchState(context);
        if (settingsItem.getId().equals("id_amoled_theme")) {
          if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                  == Configuration.UI_MODE_NIGHT_YES) {
            applyTheme(isChecked);
          }
        }
      });
    }

    private void setupClickListener(SettingsItem settingsItem) {
      settingsItemLayout.setOnClickListener(v -> {
        HapticUtils.weakVibrate(v, context);
        handleItemClick(settingsItem.getId());
      });
    }

    private void setupItemMargin(boolean isLastItem) {
      int bottomMargin = isLastItem ? (int) Utils.convertDpToPixel(30, context) : 0;
      ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
      layoutParams.bottomMargin = bottomMargin;
      itemView.setLayoutParams(layoutParams);
    }

    private void handleItemClick(String id) {
      switch (id) {
        case "id_unhide_cards":
          Preferences.setSpecificCardVisibility(context, "warning_usb_debugging", true);
          Toast.makeText(context, context.getString(R.string.unhide_cards_message), Toast.LENGTH_SHORT).show();
          break;
        case "id_examples":
          navigateToFragment(new ExamplesFragment());
          break;
        case "id_about":
          navigateToFragment(new AboutFragment());
          break;
        case "id_default_language":
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent intent = new Intent(Settings.ACTION_APP_LOCALE_SETTINGS);
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
      }
    }

    private void navigateToFragment(androidx.fragment.app.Fragment fragment) {
      if (activity instanceof FragmentActivity) {
        ((FragmentActivity) activity).getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.fragment_enter,
                        R.anim.fragment_exit,
                        R.anim.fragment_pop_enter,
                        R.anim.fragment_pop_exit
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
      }
    }
  }
}