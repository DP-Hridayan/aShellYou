package in.hridayan.ashell.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.materialswitch.MaterialSwitch;
import in.hridayan.ashell.R;
import in.hridayan.ashell.utils.SettingsItem;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private List<SettingsItem> settingsList;
    private Context context;

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
        holder.titleTextView.setText(settingsItem.getTitle());
        holder.descriptionTextView.setText(settingsItem.getDescription());

        boolean isChecked = getSavedSwitchState(settingsItem.getTitle());
        holder.switchView.setChecked(isChecked);
        settingsItem.setEnabled(isChecked);

        holder.switchView.setOnCheckedChangeListener(
                (buttonView, isCheckedNew) -> {
                    settingsItem.setEnabled(isCheckedNew);
                    saveSwitchState(settingsItem.getTitle(), isCheckedNew);
                });

        // Make non-switch views non-clickable
        holder.titleTextView.setClickable(false);
        holder.descriptionTextView.setClickable(false);
    }

    @Override
    public int getItemCount() {
        return settingsList.size();
    }

    public void saveSwitchState(String title, boolean isChecked) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(title, isChecked);
        editor.apply();
    }

    public boolean getSavedSwitchState(String title) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(title, false);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView descriptionTextView;
        MaterialSwitch switchView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.setting_title);
            descriptionTextView = itemView.findViewById(R.id.setting_description);
            switchView = itemView.findViewById(R.id.setting_switch);
        }
    }
}