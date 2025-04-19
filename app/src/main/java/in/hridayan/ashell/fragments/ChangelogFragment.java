package in.hridayan.ashell.fragments;

import android.content.Context;
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
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.ChangelogAdapter;
import in.hridayan.ashell.databinding.FragmentChangelogBinding;
import in.hridayan.ashell.items.ChangelogItem;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.ChangelogViewModel;
import java.util.ArrayList;
import java.util.List;

public class ChangelogFragment extends Fragment {
  private ChangelogViewModel viewModel;
  private Context context;
  private FragmentChangelogBinding binding;
  private Pair<Integer, Integer> mRVPositionAndOffset;

  private final String[] versionNames = {"v6.0.3","v6.0.2","v6.0.1","v6.0.0","v5.2.1","v5.2.0",
    "v5.1.0", "v5.0.0", "v4.4.0", "v4.3.1", "v4.3.0", "v4.2.1", "v4.2.0", "v4.1.0", "v4.0.3",
    "v4.0.2", "v4.0.1", "v4.0.0", "v3.9.1", "v3.9.0", "v3.8.2", "v3.8.1", "v3.8.0", "v3.7.0",
    "v3.6.0", "v3.5.1", "v3.5.0", "v3.4.0", "v3.3.0", "v3.2.0", "v3.1.0", "v3.0.0", "v2.0.2",
    "v2.0.1", "v2.0.0", "v1.3.0", "v1.2.0", "v1.1.1", "v1.1.0", "v1.0.0", "v0.9.1", "v0.9.0"
  };

  @Override
  public void onPause() {
    super.onPause();
    if (binding.rvChangelogs != null) {

      LinearLayoutManager layoutManager =
          (LinearLayoutManager) binding.rvChangelogs.getLayoutManager();

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
    if (binding.rvChangelogs != null && binding.rvChangelogs.getLayoutManager() != null) {

      binding.appBarLayout.setExpanded(viewModel.isToolbarExpanded());

      mRVPositionAndOffset = viewModel.getRVPositionAndOffset();
      if (mRVPositionAndOffset != null) {

        int position = viewModel.getRVPositionAndOffset().first;
        int offset = viewModel.getRVPositionAndOffset().second;

        // Restore recyclerView scroll position
        ((LinearLayoutManager) binding.rvChangelogs.getLayoutManager())
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

    binding = FragmentChangelogBinding.inflate(inflater, container, false);
    context = requireContext();

    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    viewModel = new ViewModelProvider(requireActivity()).get(ChangelogViewModel.class);

    OnBackPressedDispatcher dispatcher = requireActivity().getOnBackPressedDispatcher();
    binding.arrowBack.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          dispatcher.onBackPressed();
        });

    List<ChangelogItem> changelogItems = new ArrayList<>();

    for (String versionName : versionNames) {
      changelogItems.add(
          new ChangelogItem(
              getString(R.string.version) + "\t\t" + versionName,
              Utils.loadChangelogText(versionName, getContext())));
    }

    ChangelogAdapter adapter = new ChangelogAdapter(changelogItems, getContext());
    binding.rvChangelogs.setAdapter(adapter);
    binding.rvChangelogs.setLayoutManager(new LinearLayoutManager(context));
  }
}
