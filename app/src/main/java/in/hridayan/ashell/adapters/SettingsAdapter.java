package in.hridayan.ashell.adapters;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.materialswitch.MaterialSwitch;
import in.hridayan.ashell.R;
import in.hridayan.ashell.activities.AboutActivity;
import in.hridayan.ashell.activities.ChangelogActivity;
import in.hridayan.ashell.activities.ExamplesActivity;
import in.hridayan.ashell.activities.SettingsActivity;
import in.hridayan.ashell.utils.SettingsItem;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

  private List<SettingsItem> settingsList;
  private Context context;
  private int currentTheme;

  public SettingsAdapter(List<SettingsItem> settingsList, Context context, int currentTheme) {
    this.settingsList = settingsList;
    this.context = context;
    this.currentTheme = currentTheme;
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

    if (settingsItem.hasSwitch()) {
      holder.switchView.setVisibility(View.VISIBLE);
      holder.switchView.setChecked(settingsItem.isEnabled());

      holder.switchView.setOnCheckedChangeListener(
          (buttonView, isChecked) -> {
            settingsItem.setEnabled(isChecked);
            settingsItem.saveSwitchState(context);
            if (settingsItem.getId().equals("id_amoled_theme")) {
              applyTheme(isChecked);
            }
          });

    } else {
      holder.switchView.setVisibility(View.GONE);

      if (TextUtils.isEmpty(settingsItem.getDescription())) {
        holder.descriptionTextView.setVisibility(View.GONE);
      } else {
        holder.descriptionTextView.setVisibility(View.VISIBLE);
      }
    }
    View.OnClickListener clickListener =
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            String id = settingsItem.getId();
            Intent intent;

            switch (id) {
              case "id_examples":
                intent = new Intent(context, ExamplesActivity.class);
                break;

              case "id_about":
                intent = new Intent(context, AboutActivity.class);
                break;

              default:
                return;
            }
            context.startActivity(intent);
          }
        };

    holder.settingsItemLayout.setOnClickListener(clickListener);
  }

  private void applyTheme(boolean isAmoledTheme) {
    if (isAmoledTheme) {
      context.setTheme(R.style.ThemeOverlay_aShellYou_AmoledTheme);
    } else {
      context.setTheme(R.style.aShellYou_AppTheme);
    }
    currentTheme =
        isAmoledTheme ? R.style.ThemeOverlay_aShellYou_AmoledTheme : R.style.aShellYou_AppTheme;

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
