package in.hridayan.ashell.fragments.settings;

import android.content.Context;
import android.net.Uri;
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
import com.google.android.material.transition.MaterialContainerTransform;
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.SettingsAdapter;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.databinding.FragmentSettingsBinding;
import in.hridayan.ashell.items.SettingsItem;
import in.hridayan.ashell.utils.DocumentTreeUtil;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.MiuiCheck;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.AboutViewModel;
import in.hridayan.ashell.viewmodels.ExamplesViewModel;
import in.hridayan.ashell.viewmodels.SettingsItemViewModel;
import in.hridayan.ashell.viewmodels.SettingsViewModel;
import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

  private List<SettingsItem> settingsData;
  private static SettingsAdapter adapter;
  private int currentTheme;
  private SettingsViewModel viewModel;
  private AboutViewModel aboutViewModel;
  private ExamplesViewModel examplesViewModel;
  private SettingsItemViewModel itemViewModel;
  private Context context;
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

    // we refresh the text in the editText onResume to update the newly set file path
    if (adapter.textViewSaveDir != null
        && adapter.textViewSaveDir.getVisibility() == View.VISIBLE) {
      String outputSaveDirectory = Preferences.getSavedOutputDir();
      if (!outputSaveDirectory.equals("")) {
        String outputSaveDirPath =
            DocumentTreeUtil.getFullPathFromTreeUri(Uri.parse(outputSaveDirectory), context);
        adapter.textViewSaveDir.setText(outputSaveDirPath);
      }
    }

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

    binding = FragmentSettingsBinding.inflate(inflater, container, false);
    view = binding.getRoot();

    context = requireContext();

    viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

    aboutViewModel = new ViewModelProvider(requireActivity()).get(AboutViewModel.class);

    examplesViewModel = new ViewModelProvider(requireActivity()).get(ExamplesViewModel.class);

    itemViewModel = new ViewModelProvider(requireActivity()).get(SettingsItemViewModel.class);

    OnBackPressedDispatcher dispatcher = requireActivity().getOnBackPressedDispatcher();

    binding.arrowBack.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          dispatcher.onBackPressed();
        });

    settingsData = new ArrayList<>();

    settingsData.add(
        new SettingsItem(
            Const.ID_LOOK_AND_FEEL,
            R.drawable.ic_pallete,
            getString(R.string.look_and_feel),
            getString(R.string.des_look_and_feel),
            false,
            false));

    settingsData.add(
        new SettingsItem(
            Const.PREF_CLEAR,
            R.drawable.ic_clear,
            getString(R.string.ask_to_clean),
            getString(R.string.des_ask_to_clean),
            true,
            Preferences.getClear()));

  /*  settingsData.add(
        new SettingsItem(
            Const.PREF_SHARE_AND_RUN,
            R.drawable.ic_share,
            getString(R.string.share_and_run),
            getString(R.string.des_share_and_run),
            true,
            Preferences.getShareAndRun()));*/

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
            Const.ID_CONFIG_SAVE_DIR,
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
            settingsData,
            context,
            requireActivity(),
            itemViewModel,
            aboutViewModel,
            examplesViewModel);
    binding.rvSettings.setAdapter(adapter);
    binding.rvSettings.setLayoutManager(new LinearLayoutManager(context));

    // After recyclerview is drawn, start the transition
    binding.rvSettings.getViewTreeObserver().addOnDrawListener(this::startPostponedEnterTransition);

    // intentional crash with a long message
    //  throwLongException();

    return view;
  }

  public String generateLongMessage() {
    StringBuilder message = new StringBuilder("Long Exception Message: ");
    for (int i = 0; i < 1000; i++) { // Adjust the loop count for longer messages
      message.append("This is an intentional crash ").append(i).append(". ");
    }
    return message.toString();
  }

  public void throwLongException() {
    String longMessage = generateLongMessage();
    throw new RuntimeException(longMessage);
  }
}
