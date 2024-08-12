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
import in.hridayan.ashell.databinding.FragmentSettingsBinding;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.MiuiCheck;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.SettingsItem;
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

    postponeEnterTransition();
    setExitTransition(new Hold());

    setSharedElementEnterTransition(new MaterialContainerTransform());

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
          HapticUtils.weakVibrate(v, context);
          dispatcher.onBackPressed();
        });

    settingsData = new ArrayList<>();

    settingsData.add(
        new SettingsItem(
            "id_amoled_theme",
            R.drawable.ic_amoled_theme,
            getString(R.string.amoled_theme),
            getString(R.string.des_amoled_theme),
            true,
            Preferences.getAmoledTheme(context)));

    settingsData.add(
        new SettingsItem(
            "id_clear",
            R.drawable.ic_clear,
            getString(R.string.ask_to_clean),
            getString(R.string.des_ask_to_clean),
            true,
            Preferences.getClear(context)));

    settingsData.add(
        new SettingsItem(
            "id_share_and_run",
            R.drawable.ic_share,
            getString(R.string.share_and_run),
            getString(R.string.des_share_and_run),
            true,
            Preferences.getShareAndRun(context)));

    settingsData.add(
        new SettingsItem(
            "id_auto_update_check",
            R.drawable.ic_auto_update,
            getString(R.string.auto_update_check),
            getString(R.string.des_auto_update_check),
            true,
            Preferences.getAutoUpdateCheck(context)));

    settingsData.add(
        new SettingsItem(
            "id_local_adb_mode",
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
              "id_default_language",
              R.drawable.ic_language,
              getString(R.string.default_language),
              getString(R.string.des_default_language),
              false,
              false));
    }

    settingsData.add(
        new SettingsItem(
            "id_default_launch_mode",
            R.drawable.ic_mode,
            getString(R.string.default_launch_mode),
            getString(R.string.des_default_launch_mode),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            "id_disable_softkey",
            R.drawable.ic_disable_keyboard,
            getString(R.string.disable_softkey),
            getString(R.string.des_disable_softkey),
            true,
            Preferences.getDisableSoftkey(context)));

    settingsData.add(
        new SettingsItem(
            "id_vibration",
            R.drawable.ic_vibration,
            getString(R.string.vibration),
            getString(R.string.des_vibration),
            true,
            Preferences.getHapticsAndVibration(context)));

    settingsData.add(
        new SettingsItem(
            "id_override_bookmarks",
            R.drawable.ic_warning,
            getString(R.string.override_bookmarks_limit),
            getString(R.string.des_override_bookmarks),
            true,
            Preferences.getOverrideBookmarks(context)));

    settingsData.add(
        new SettingsItem(
            "id_save_preference",
            R.drawable.ic_save_24px,
            getString(R.string.save_preference),
            getString(R.string.des_save_preference),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            "id_smooth_scroll",
            R.drawable.ic_scroll,
            getString(R.string.smooth_scrolling),
            getString(R.string.des_smooth_scroll),
            true,
            Preferences.getSmoothScroll(context)));

    settingsData.add(
        new SettingsItem(
            "id_unhide_cards",
            R.drawable.ic_cards,
            getString(R.string.unhide_cards),
            getString(R.string.des_unhide_cards),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            "id_examples",
            R.drawable.ic_numbers,
            getString(R.string.commands),
            getString(R.string.des_examples),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            "id_about",
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
    return view;
  }
}
