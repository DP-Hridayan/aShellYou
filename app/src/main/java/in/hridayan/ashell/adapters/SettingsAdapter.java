package in.hridayan.ashell.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.DialogUtils;
import in.hridayan.ashell.activities.MainActivity;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.fragments.AboutFragment;
import in.hridayan.ashell.fragments.ExamplesFragment;
import in.hridayan.ashell.fragments.settings.LookAndFeel;
import in.hridayan.ashell.items.SettingsItem;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.AboutViewModel;
import in.hridayan.ashell.viewmodels.ExamplesViewModel;
import in.hridayan.ashell.viewmodels.SettingsItemViewModel;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

  private final List<SettingsItem> settingsList;
  private final Context context;
  private final Activity activity;
  private final AboutViewModel aboutViewModel;
  private final ExamplesViewModel examplesViewModel;
  private final SettingsItemViewModel viewModel;
  public MaterialTextView textViewSaveDir;
  private Map<String, WeakReference<View>> viewMap;
  private WeakReference itemRef;

  public SettingsAdapter(
      List<SettingsItem> settingsList,
      Context context,
      Activity activity,
      SettingsItemViewModel itemViewModel,
      AboutViewModel aboutVM,
      ExamplesViewModel examplesVM) {
    this.settingsList = settingsList;
    this.context = context;
    this.activity = activity;
    this.viewModel = itemViewModel;
    this.aboutViewModel = aboutVM;
    this.examplesViewModel = examplesVM;
    this.viewMap = new HashMap<>();
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

    viewMap.put(settingsItem.getId(), new WeakReference<>(holder.itemView));

    holder.bind(settingsItem, position == getItemCount() - 1);
  }

  @Override
  public int getItemCount() {
    return settingsList.size();
  }

  public View getItemViewById(String id) {
    return viewMap.get(id) != null ? viewMap.get(id).get() : null;
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    ImageView symbolImageView;
    MaterialTextView titleTextView, descriptionTextView;
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

      descriptionTextView.setVisibility(
          TextUtils.isEmpty(settingsItem.getDescription()) ? View.GONE : View.VISIBLE);

      setupSwitch(settingsItem);
      setupClickListener(settingsItem);
      setupItemMargin(isLastItem);
    }

    private void setupSwitch(SettingsItem settingsItem) {
      // set the switch state without triggering the listener
      switchView.setOnCheckedChangeListener(null);
      switchView.setVisibility(settingsItem.hasSwitch() ? View.VISIBLE : View.GONE);
      switchView.setChecked(settingsItem.isChecked());
      switchView.setOnCheckedChangeListener(
          (view, isChecked) -> {
            settingsItem.setChecked(isChecked);
            settingsItem.saveSwitchState();
          });
    }

    private void setupClickListener(SettingsItem settingsItem) {
      settingsItemLayout.setOnClickListener(
          v -> {
            HapticUtils.weakVibrate(v);
            handleItemClick(settingsItem.getId());
          });
    }

    private void setupItemMargin(boolean isLastItem) {
      int bottomMargin = isLastItem ? (int) Utils.convertDpToPixel(30, context) : 0;
      ViewGroup.MarginLayoutParams layoutParams =
          (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();
      layoutParams.bottomMargin = bottomMargin;
      itemView.setLayoutParams(layoutParams);
    }

    private void handleItemClick(String id) {
      switch (id) {
        case Const.ID_LOOK_AND_FEEL:
          viewModel.setScrollPosition(null);
          viewModel.setToolbarExpanded(true);
          loadFragmentWithTransition(new LookAndFeel(), itemView);
          break;

        case Const.ID_UNHIDE_CARDS:
          Preferences.setSpecificCardVisibility(Const.InfoCards.WARNING_USB_DEBUGGING, true);
          Toast.makeText(
                  context, context.getString(R.string.unhide_cards_message), Toast.LENGTH_SHORT)
              .show();
          break;

        case Const.ID_EXAMPLES:
          examplesViewModel.setRVPositionAndOffset(null);
          examplesViewModel.setToolbarExpanded(true);
          examplesViewModel.setEnteringFromSettings(true);
          loadFragmentWithTransition(new ExamplesFragment(), itemView);
          break;

        case Const.ID_ABOUT:
          aboutViewModel.setRVPositionAndOffset(null);
          aboutViewModel.setToolbarExpanded(true);
          loadFragmentWithTransition(new AboutFragment(), itemView);
          break;

        case Const.ID_CONFIG_SAVE_DIR:
          textViewSaveDir = DialogUtils.configureSaveDirDialog(context, activity);
          break;

        case Const.PREF_DEFAULT_LAUNCH_MODE:
          DialogUtils.defaultLaunchModeDialog(context);
          break;

        case Const.PREF_EXAMPLES_LAYOUT_STYLE:
          DialogUtils.examplesLayoutStyleDialog(context);
          break;

        case Const.PREF_SAVE_PREFERENCE:
          DialogUtils.savePreferenceDialog(context);
          break;

        case Const.PREF_LOCAL_ADB_MODE:
          DialogUtils.localAdbModeDialog(context);
          break;
      }
    }

    private void loadFragmentWithTransition(Fragment fragment, View itemView) {
      ((MainActivity) activity)
          .getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
          .addToBackStack(fragment.getClass().getSimpleName())
          .commit();
    }
  }
}
