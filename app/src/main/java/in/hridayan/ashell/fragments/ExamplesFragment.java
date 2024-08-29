package in.hridayan.ashell.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.transition.MaterialContainerTransform;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.KeyboardUtils;
import in.hridayan.ashell.adapters.CommandsSearchAdapter;
import in.hridayan.ashell.adapters.ExamplesAdapter;
import in.hridayan.ashell.config.Const;
import in.hridayan.ashell.databinding.FragmentExamplesBinding;
import in.hridayan.ashell.items.CommandItems;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.HapticUtils;
import in.hridayan.ashell.config.Preferences;
import in.hridayan.ashell.utils.Utils;
import in.hridayan.ashell.viewmodels.ExamplesViewModel;
import in.hridayan.ashell.viewmodels.MainViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExamplesFragment extends Fragment
    implements ExamplesAdapter.OnItemClickListener,
        ExamplesAdapter.UseCommandListener,
        CommandsSearchAdapter.UseCommandInSearchListener {

  private ExamplesViewModel viewModel;
  private MainViewModel mainViewModel;
  private EditText editText;
  private List<CommandItems> itemList;
  private ExamplesAdapter mExamplesAdapter;
  private MenuItem sort, pin, selectAll, addBookmark, deselectAll;
  private Menu searchBarMenu;
  private View view;
  private int isSortingOptionSame;
  private boolean isSummaryChipClicked = false, isAllItemsSelected;
  private Context context;
  private BottomNavigationView mNav;
  private FragmentExamplesBinding binding;
  private Pair<Integer, Integer> mRVPositionAndOffset;

  @Override
  public void onPause() {
    super.onPause();
    if (binding.rvSearchView != null) {

      LinearLayoutManager layoutManager =
          (LinearLayoutManager) binding.rvSearchView.getLayoutManager();

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
    if (binding.rvSearchView != null && binding.rvSearchView.getLayoutManager() != null) {

      binding.appBarLayout.setExpanded(viewModel.isToolbarExpanded());

      mRVPositionAndOffset = viewModel.getRVPositionAndOffset();
      if (mRVPositionAndOffset != null) {

        int position = viewModel.getRVPositionAndOffset().first;
        int offset = viewModel.getRVPositionAndOffset().second;

        // Restore recyclerView scroll position
        ((LinearLayoutManager) binding.rvSearchView.getLayoutManager())
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
    binding = FragmentExamplesBinding.inflate(inflater, container, false);

    view = binding.getRoot();

    context = requireContext();

    mNav = requireActivity().findViewById(R.id.bottom_nav_bar);

    editText = binding.searchView.getSearchEditText();
    searchBarMenu = binding.searchBar.getMenu();
    sort = searchBarMenu.findItem(R.id.sort);
    addBookmark = searchBarMenu.findItem(R.id.add_bookmark);
    pin = searchBarMenu.findItem(R.id.pin);
    selectAll = searchBarMenu.findItem(R.id.select_all);
    deselectAll = searchBarMenu.findItem(R.id.deselect_all);
    itemList = Commands.commandList(context);

    viewModel = new ViewModelProvider(requireActivity()).get(ExamplesViewModel.class);
    mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

    OnBackPressedDispatcher dispatcher = requireActivity().getOnBackPressedDispatcher();

    binding.arrowBack.setOnClickListener(
        v -> {
          HapticUtils.weakVibrate(v);
          dispatcher.onBackPressed();
        });

    mNav.setVisibility(View.GONE);

    binding.searchBar.clearFocus();
    binding.searchBar.setNavigationIcon(R.drawable.ic_search);
    binding.searchBar.setOnMenuItemClickListener(
        item -> {
          HapticUtils.weakVibrate(view);
          switch (item.getItemId()) {
            case R.id.sort:
              sortingDialog(context, getActivity());
              return true;

            case R.id.select_all:
              mExamplesAdapter.selectAll();
              updateSearchBar();
              return true;

            case R.id.deselect_all:
              mExamplesAdapter.deselectAll();
              updateSearchBar();
              return true;

            case R.id.add_bookmark:
              manageBookmarkAddOrRemove();
              return true;

            case R.id.pin:
              managePinUnpin();
              return true;

            default:
              return false;
          }
        });

    binding.rvSearch.setLayoutManager(new LinearLayoutManager(context));

    binding.rvSearchView.setLayoutManager(new LinearLayoutManager(context));
    GridLayoutManager mLayoutManager =
        new GridLayoutManager(
            context,
            getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                ? Const.GRID_STYLE
                : Preferences.getExamplesLayoutStyle());
    binding.rvSearchView.setLayoutManager(mLayoutManager);

    mExamplesAdapter = new ExamplesAdapter(Commands.commandList(context), context, this);
    mExamplesAdapter.sortData();
    mExamplesAdapter.setOnItemClickListener(this);
    binding.rvSearchView.setAdapter(mExamplesAdapter);
    binding.rvSearchView.setVisibility(View.VISIBLE);

    editText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence text, int i, int i1, int i2) {}

          @Override
          public void onTextChanged(CharSequence text, int i, int i1, int i2) {}

          @Override
          public void afterTextChanged(Editable text) {
            filterList(text);
            binding.chipSearchSummary.setOnClickListener(
                v -> {
                  isSummaryChipClicked = true;
                  filterList(text);
                });
          }
        });

    return view;
  }

  private void filterList(CharSequence text) {

    List<CommandItems> filteredList = new ArrayList<>();

    binding.chipSearchSummary.setVisibility(View.GONE);
    binding.noCommandFound.setVisibility(View.GONE);
    binding.searchImg.setVisibility(View.VISIBLE);

    if (text != null && !text.toString().isEmpty()) {

      searchTitle(text, filteredList);

      if (filteredList.isEmpty()) {
        binding.noCommandFound.setVisibility(View.VISIBLE);
        binding.chipSearchSummary.setVisibility(View.VISIBLE);
        binding.searchImg.setVisibility(View.VISIBLE);
        if (isSummaryChipClicked) chipSummaryOnClick(text, filteredList);
        /* if filtered list is empty again after running chipSummaryOnClick, then we show no command found text message */
        if (filteredList.isEmpty()) {
          binding.noCommandFound.setVisibility(View.VISIBLE);
          binding.searchImg.setVisibility(View.VISIBLE);
        }
      } else binding.searchImg.setVisibility(View.GONE);
    }
    binding.rvSearch.setVisibility(View.VISIBLE);
    CommandsSearchAdapter adapter = new CommandsSearchAdapter(filteredList, context, this);
    binding.rvSearch.setAdapter(adapter);
  }

  // Conduct search in only command titles
  private void searchTitle(CharSequence text, List<CommandItems> filteredList) {
    for (CommandItems item : itemList) {
      if (item.getTitle()
          .toLowerCase(Locale.getDefault())
          .contains(text.toString().toLowerCase(Locale.getDefault()))) {
        filteredList.add(item);
      }
    }
  }

  // Conduct search in both command titles and summary
  private void searchTitleAndSummary(CharSequence text, List<CommandItems> filteredList) {
    for (CommandItems item : itemList) {
      if (item.getTitle()
              .toLowerCase(Locale.getDefault())
              .contains(text.toString().toLowerCase(Locale.getDefault()))
          || item.getSummary()
              .toLowerCase(Locale.getDefault())
              .contains(text.toString().toLowerCase(Locale.getDefault()))) {
        filteredList.add(item);
      }
    }
  }

  /* Onclick function for the chip which appears when no search result is found when only searched through titles */
  private void chipSummaryOnClick(CharSequence text, List<CommandItems> filteredList) {
    binding.noCommandFound.setVisibility(View.GONE);
    binding.searchImg.setVisibility(View.GONE);
    searchTitleAndSummary(text, filteredList);
    binding.chipSearchSummary.setVisibility(View.GONE);
  }

  private void sortingDialog(Context context, Activity activity) {
    CharSequence[] sortingOptions = {
      getString(R.string.sort_A_Z),
      getString(R.string.sort_Z_A),
      getString(R.string.most_used),
      getString(R.string.least_used)
    };

    int currentSortingOption = Preferences.getSortingExamples();
    isSortingOptionSame = currentSortingOption;
    final int[] sortingOption = {currentSortingOption};

    new MaterialAlertDialogBuilder(activity)
        .setTitle(getString(R.string.sort))
        .setSingleChoiceItems(
            sortingOptions,
            currentSortingOption,
            (dialog, which) -> {
              sortingOption[0] = which;
            })
        .setPositiveButton(
            getString(R.string.ok),
            (dialog, which) -> {
              Preferences.setSortingExamples(sortingOption[0]);
              if (isSortingOptionSame != sortingOption[0]) mExamplesAdapter.sortData();
            })
        .setNegativeButton(getString(R.string.cancel), null)
        .show();
  }

  // Update search bar when selecting items
  private void updateSearchBar() {
    int numSelectedItems = mExamplesAdapter.getSelectedItemsSize();
    isAllItemsSelected = numSelectedItems == mExamplesAdapter.getItemCount();
    if (numSelectedItems > 0) startSelection(numSelectedItems);
    else endSelection();
  }

  private void searchBarNavigationIconOnClickListener(int numSelectedItems) {
    binding.searchBar.setNavigationOnClickListener(
        v -> {
          if (numSelectedItems > 0) {
            endSelection();
            mExamplesAdapter.deselectAll();
          }
        });
  }

  // End the selection phase
  private void endSelection() {
    binding.searchBar.setHint(R.string.search_command);
    binding.searchBar.setNavigationIcon(R.drawable.ic_search);
    binding.searchBar.setClickable(true);
    searchBarNavigationIconOnClickListener(0);
    updateMenuItemVisibility(false, isAllItemsSelected);
  }

  // Enter the selection phase
  private void startSelection(int numSelectedItems) {
    String hint =
        getString(R.string.selected) + "\t\t" + "( " + Integer.toString(numSelectedItems) + " )";
    binding.searchBar.setHint(hint);
    binding.searchBar.setNavigationIcon(R.drawable.ic_cross);
    binding.searchBar.setClickable(false);
    searchBarNavigationIconOnClickListener(numSelectedItems);
    updateMenuItemVisibility(true, isAllItemsSelected);
  }

  private void updateMenuItemVisibility(boolean isItemSelecting, boolean isAllSelected) {
    sort.setVisible(!isItemSelecting);
    pin.setVisible(isItemSelecting);
    selectAll.setVisible(isItemSelecting && !isAllSelected);
    deselectAll.setVisible(isItemSelecting && isAllSelected);

    if (isItemSelecting) {
      addBookmark.setVisible(true);
      addBookmark.setIcon(
          mExamplesAdapter.isAllItemsBookmarked()
              ? R.drawable.ic_bookmark_added
              : R.drawable.ic_add_bookmark);
      pin.setIcon(mExamplesAdapter.isAllItemsPinned() ? R.drawable.ic_pinned : R.drawable.ic_pin);
    } else addBookmark.setVisible(false);
  }

  private void batchBookmarkDialog(
      int selectedCount, boolean isAllItemBookmarked, boolean isLimitReached, boolean isBatch) {

    String message =
        isAllItemBookmarked
            ? getString(R.string.confirm_batch_remove_bookmark, selectedCount)
            : getString(R.string.confirm_batch_add_bookmark, selectedCount);

    new MaterialAlertDialogBuilder(context)
        .setTitle(getString(R.string.confirm))
        .setMessage(message)
        .setPositiveButton(
            getString(R.string.ok),
            (dialog, i) -> {
              if (isAllItemBookmarked) mExamplesAdapter.deleteSelectedFromBookmarks();
              else if (!isLimitReached) mExamplesAdapter.addSelectedToBookmarks();

              updateSearchBar();
              bookmarksAddedOrRemovedMessage(
                  !isAllItemBookmarked, isBatch, isLimitReached, selectedCount);
            })
        .setNegativeButton(getString(R.string.cancel), null)
        .show();
  }

  // Function to handle bookmarks add or remove
  private void manageBookmarkAddOrRemove() {

    int selectedItems = mExamplesAdapter.getSelectedItemsSize();
    boolean isAllItemBookmarked = mExamplesAdapter.isAllItemsBookmarked(),
        isLimitReached =
            selectedItems + Utils.getBookmarks(context).size() > Const.MAX_BOOKMARKS_LIMIT
                && !Preferences.getOverrideBookmarks();

    boolean isBatch = selectedItems > 1;
    if (isBatch) batchBookmarkDialog(selectedItems, isAllItemBookmarked, isLimitReached, isBatch);
    else {
      if (isAllItemBookmarked) mExamplesAdapter.deleteSelectedFromBookmarks();
      else if (!isLimitReached) mExamplesAdapter.addSelectedToBookmarks();

      updateSearchBar();
      bookmarksAddedOrRemovedMessage(!isAllItemBookmarked, isBatch, isLimitReached, selectedItems);
    }
  }

  // Snackbar message to show when commands are added or removed from bookmarks
  private void bookmarksAddedOrRemovedMessage(
      boolean isAdded, boolean isBatch, boolean isLimitReached, int selectedCount) {

    if (isLimitReached && isAdded)
      Utils.snackBar(view, getString(R.string.bookmark_limit_reached)).show();
    else if (isBatch) {
      int message =
          isAdded ? R.string.batch_bookmark_added_message : R.string.batch_bookmark_removed_message;
      Utils.snackBar(view, getString(message, selectedCount)).show();
    } else {
      String command =
          mExamplesAdapter.sanitizeText(mExamplesAdapter.selectedItems.get(0).getTitle());
      int message = isAdded ? R.string.bookmark_added_message : R.string.bookmark_removed_message;
      Utils.snackBar(view, getString(message, command)).show();
    }
  }

  // Function which manages the pin and unpin commands feature
  private void managePinUnpin() {
    // Get the number of selected items
    int size = mExamplesAdapter.getSelectedItemsSize();
    // This sets the title that shows number of selected items
    String title = mExamplesAdapter.selectedItems.get(0).getTitle();

    // If selected items is more than 1 we declare the selection as batch
    boolean isBatch = size > 1;
    // check if every item is already pinned
    boolean isAllItemsPinned = mExamplesAdapter.isAllItemsPinned();

    // If batch we show confirm dialog message for batch else we show it for single item
    String confirmPin =
        isBatch ? getString(R.string.confirm_pin) : getString(R.string.confirm_pin_single, title);
    String confirmUnpin =
        isBatch
            ? getString(R.string.confirm_unpin)
            : getString(R.string.confirm_unpin_single, title);
    String message = isAllItemsPinned ? confirmUnpin : confirmPin;

    // If all items are pinned we show unpin button else we show pin button
    String positiveButtonText =
        isAllItemsPinned ? getString(R.string.unpin) : getString(R.string.pin);

    // Message to display after pinning or unpinning
    String snackBarMessage;
    if (isBatch)
      snackBarMessage =
          isAllItemsPinned
              ? getString(R.string.batch_unpinned_message, size)
              : getString(R.string.batch_pinned_message, size);
    else
      snackBarMessage =
          isAllItemsPinned
              ? getString(R.string.unpinned_message, title)
              : getString(R.string.pinned_message, title);

    // Dialog asking for confirmation of pin or unpin
    new MaterialAlertDialogBuilder(context)
        .setTitle(getString(R.string.confirm))
        .setMessage(message)
        .setPositiveButton(
            positiveButtonText,
            (dialog, i) -> {
              mExamplesAdapter.pinUnpinSelectedItems(isAllItemsPinned);
              endSelection();
              updateSearchBar();
              Utils.snackBar(view, snackBarMessage).show();
            })
        .setNegativeButton(getString(R.string.cancel), null)
        .show();
  }

  @Override
  public void onItemClick(int position) {
    updateSearchBar();
  }

  @Override
  public void onItemLongClick(int position) {
    updateSearchBar();
  }

  @Override
  public void useCommand(String command) {
    navigateToFragmentAndSetCommand(command);
  }

  @Override
  public void useCommandInSearch(String command) {
    navigateToFragmentAndSetCommand(command);
  }

  /* This function is called when we use the "Use" feature in commands examples to set the command in the fragment edit text */
  private void navigateToFragmentAndSetCommand(String command) {
    mainViewModel.setUseCommand(command);
    Fragment fragment = new AshellFragment();
    if (mainViewModel.previousFragment() == Const.OTG_FRAGMENT) fragment = new OtgFragment();

    // clear previous backstacks
    clearBackStack();

    KeyboardUtils.closeKeyboard(requireActivity(), view);
    requireActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .setCustomAnimations(
            R.anim.fragment_enter,
            R.anim.fragment_exit,
            R.anim.fragment_pop_enter,
            R.anim.fragment_pop_exit)
        .replace(R.id.fragment_container, fragment)
        .commit();
  }

  private void clearBackStack() {
    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
    if (fragmentManager.getBackStackEntryCount() > 0) {
      // Pop all back stack entries
      fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
  }
}
