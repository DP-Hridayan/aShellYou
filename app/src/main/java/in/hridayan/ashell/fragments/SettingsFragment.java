package in.hridayan.ashell.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialContainerTransform;
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.databinding.FragmentSettingsBinding;
import in.hridayan.ashell.items.SettingsItem;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.MiuiCheck;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.AboutViewModel;
import in.hridayan.ashell.viewmodels.ExamplesViewModel;
import in.hridayan.ashell.viewmodels.SettingsViewModel;
import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

  private List<SettingsItem> settingsData;
  private SettingsAdapter adapter;
  private int currentTheme;
  private SettingsViewModel viewModel;
  private AboutViewModel aboutViewModel;
  private ExamplesViewModel examplesViewModel;
  private Context context;
  private BottomNavigationView mNav;
  private Pair<Integer, Integer> mRVPositionAndOffset;
  private FragmentSettingsBinding binding;
  private View view;

  @Override
  public void onPause() {
    super.onPause();
    if (binding.rvSettings != null) {

      LinearLayoutManager layoutManager =
          (LinearLayoutManager) binding.rvSettings.getLayoutManager();

      int currentPosition = layoutManager.findLastVisibleItemPosition();
      View currentView = layoutManager.findViewByPosition(currentPosition);

      if (currentView != null) {
        mRVPositionAndOffset = new Pair<>(currentPosition, currentView.getTop());
        viewModel.setRVPositionAndOffset(mRVPositionAndOffset);
      }
      // Save toolbar state
      viewModel.setToolbarExpanded(Utils.isToolbarExpanded(binding.appBarLayout));
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    if (binding.rvSettings != null && binding.rvSettings.getLayoutManager() != null) {

      binding.appBarLayout.setExpanded(viewModel.isToolbarExpanded());

      mRVPositionAndOffset = viewModel.getRVPositionAndOffset();
      if (mRVPositionAndOffset != null) {

        int position = viewModel.getRVPositionAndOffset().first;
        int offset = viewModel.getRVPositionAndOffset().second;

        // Restore recyclerView scroll position
        ((LinearLayoutManager) binding.rvSettings.getLayoutManager())
            .scrollToPositionWithOffset(position, offset);
      }
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    setSharedElementEnterTransition(new MaterialContainerTransform());

    postponeEnterTransition();
    setExitTransition(new Hold());

    binding = FragmentSettingsBinding.inflate(inflater, container, false);
    view = binding.getRoot();

    context = requireContext();

    mNav = requireActivity().findViewById(R.id.bottom_nav_bar);

    viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

    aboutViewModel = new ViewModelProvider(requireActivity()).get(AboutViewModel.class);

    examplesViewModel = new ViewModelProvider(requireActivity()).get(ExamplesViewModel.class);

    mNav.setVisibility(View.GONE);

    OnBackPressedDispatcher dispatcher = requireActivity().getOnBackPressedDispatcher();
    binding.arrowBack.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          dispatcher.onBackPressed();
        });

    settingsData = new ArrayList<>();

    settingsData.add(
        new SettingsItem(
            Const.PREF_AMOLED_THEME,
            R.drawable.ic_amoled_theme,
            getString(R.string.amoled_theme),
            getString(R.string.des_amoled_theme),
            true,
            Preferences.getAmoledTheme()));

    settingsData.add(
        new SettingsItem(
            Const.PREF_CLEAR,
            R.drawable.ic_clear,
            getString(R.string.ask_to_clean),
            getString(R.string.des_ask_to_clean),
            true,
            Preferences.getClear()));

    settingsData.add(
        new SettingsItem(
            Const.PREF_SHARE_AND_RUN,
            R.drawable.ic_share,
            getString(R.string.share_and_run),
            getString(R.string.des_share_and_run),
            true,
            Preferences.getShareAndRun()));

    settingsData.add(
        new SettingsItem(
            Const.PREF_AUTO_UPDATE_CHECK,
            R.drawable.ic_auto_update,
            getString(R.string.auto_update_check),
            getString(R.string.des_auto_update_check),
            true,
            Preferences.getAutoUpdateCheck()));

    settingsData.add(
        new SettingsItem(
            Const.PREF_OUTPUT_SAVE_DIRECTORY,
            R.drawable.ic_directory,
            getString(R.string.configure_save_directory),
            getString(R.string.des_configure_save_directory),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            Const.PREF_LOCAL_ADB_MODE,
            R.drawable.ic_terminal,
            getString(R.string.local_adb_mode),
            getString(R.string.des_local_adb_mode),
            false,
            false));

    // App locale setting is only available on Android 13+
    // Also, it's not functional on MIUI devices even on Android 13,
    // Thanks to Xiaomi's broken implementation of standard Android APIs.
    // See: https://github.com/Pool-Of-Tears/GreenStash/issues/130 for more information.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !MiuiCheck.isMiui()) {
      settingsData.add(
          new SettingsItem(
              Const.ID_DEF_LANGUAGE,
              R.drawable.ic_language,
              getString(R.string.default_language),
              getString(R.string.des_default_language),
              false,
              false));
    }

    settingsData.add(
        new SettingsItem(
            Const.PREF_DEFAULT_LAUNCH_MODE,
            R.drawable.ic_mode,
            getString(R.string.default_launch_mode),
            getString(R.string.des_default_launch_mode),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            Const.PREF_DISABLE_SOFTKEY,
            R.drawable.ic_disable_keyboard,
            getString(R.string.disable_softkey),
            getString(R.string.des_disable_softkey),
            true,
            Preferences.getDisableSoftkey()));

    settingsData.add(
        new SettingsItem(
            Const.PREF_EXAMPLES_LAYOUT_STYLE,
            R.drawable.ic_styles,
            getString(R.string.examples_layout_style),
            getString(R.string.des_examples_layout_style),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            Const.PREF_HAPTICS_AND_VIBRATION,
            R.drawable.ic_vibration,
            getString(R.string.vibration),
            getString(R.string.des_vibration),
            true,
            Preferences.getHapticsAndVibration()));

    settingsData.add(
        new SettingsItem(
            Const.PREF_OVERRIDE_BOOKMARKS,
            R.drawable.ic_warning,
            getString(R.string.override_bookmarks_limit),
            getString(R.string.des_override_bookmarks),
            true,
            Preferences.getOverrideBookmarks()));

    settingsData.add(
        new SettingsItem(
            Const.PREF_SAVE_PREFERENCE,
            R.drawable.ic_save_24px,
            getString(R.string.save_preference),
            getString(R.string.des_save_preference),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            Const.PREF_SMOOTH_SCROLL,
            R.drawable.ic_scroll,
            getString(R.string.smooth_scrolling),
            getString(R.string.des_smooth_scroll),
            true,
            Preferences.getSmoothScroll()));

    settingsData.add(
        new SettingsItem(
            Const.ID_UNHIDE_CARDS,
            R.drawable.ic_cards,
            getString(R.string.unhide_cards),
            getString(R.string.des_unhide_cards),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            Const.ID_EXAMPLES,
            R.drawable.ic_numbers,
            getString(R.string.commands),
            getString(R.string.des_examples),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            Const.ID_ABOUT,
            R.drawable.ic_info,
            getString(R.string.about),
            getString(R.string.des_about),
            false,
            false));

    adapter =
        new SettingsAdapter(
            settingsData, context, requireActivity(), aboutViewModel, examplesViewModel);
    binding.rvSettings.setAdapter(adapter);
    binding.rvSettings.setLayoutManager(new LinearLayoutManager(context));

    // After recyclerview is drawn, start the transition
    binding.rvSettings.getViewTreeObserver().addOnDrawListener(this::startPostponedEnterTransition);

    // intentional crash with a long message
    // throwLongException();

    return view;
  }

  public String generateLongMessage() {
    StringBuilder message = new StringBuilder("Long Exception Message: ");
    for (int i = 0; i < 1000; i++) { // Adjust the loop count for longer messages
      message.append("This is line ").append(i).append(". ");
    }
    return message.toString();
  }

  public void throwLongException() {
    String longMessage = generateLongMessage();
    throw new RuntimeException(longMessage);
  }
}
