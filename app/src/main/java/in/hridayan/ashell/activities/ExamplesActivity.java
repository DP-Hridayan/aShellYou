package in.hridayan.ashell.activities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.search.SearchBar;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.CustomSearchView;
import in.hridayan.ashell.UI.ExamplesViewModel;
import in.hridayan.ashell.adapters.CommandsSearchAdapter;
import in.hridayan.ashell.adapters.ExamplesAdapter;
import in.hridayan.ashell.utils.CommandItems;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.Preferences;
import in.hridayan.ashell.utils.ThemeUtils;
import in.hridayan.ashell.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class ExamplesActivity extends AppCompatActivity
    implements ExamplesAdapter.OnItemClickListener {
  private ExamplesViewModel viewModel;
  private AppBarLayout appBarLayout;
  private CustomSearchView searchView;
  private RecyclerView mRecyclerView, mSearchRecyclerView;
  private EditText editText;
  private MaterialTextView noCommandFoundText;
  private List<CommandItems> itemList;
  private SearchBar mSearchBar;
  private ExamplesAdapter mExamplesAdapter;
  private Chip mSummaryChip;
  private MenuItem sort, pin, selectAll, addBookmark, deselectAll;
  private Menu searchBarMenu;
  private View parent;
  private int isSortingOptionSame;
  private boolean isSummaryChipClicked = false, isAllItemsSelected;

  @Override
  protected void onPause() {
    super.onPause();
    viewModel.setToolbarExpanded(Utils.isToolbarExpanded(appBarLayout));
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (viewModel.isToolbarExpanded()) {
      if (Utils.recyclerViewPosition(mRecyclerView) == 0) {
        Utils.expandToolbar(appBarLayout);
      }
    } else {
      Utils.collapseToolbar(appBarLayout);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);

    ThemeUtils.updateTheme(this);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_examples);
    parent = findViewById(android.R.id.content);
    appBarLayout = findViewById(R.id.appBarLayout);
    searchView = findViewById(R.id.search_view);
    mSearchBar = findViewById(R.id.search_bar);
    mSearchRecyclerView = findViewById(R.id.search_recycler_view);
    mSummaryChip = findViewById(R.id.search_summary);
    editText = searchView.getSearchEditText();
    searchBarMenu = mSearchBar.getMenu();
    sort = searchBarMenu.findItem(R.id.sort);
    addBookmark = searchBarMenu.findItem(R.id.add_bookmark);
    pin = searchBarMenu.findItem(R.id.pin);
    selectAll = searchBarMenu.findItem(R.id.select_all);
    deselectAll = searchBarMenu.findItem(R.id.deselect_all);
    noCommandFoundText = findViewById(R.id.no_command_found);
    itemList = Commands.commandList(this);
    viewModel = new ViewModelProvider(this).get(ExamplesViewModel.class);
    ImageView imageView = findViewById(R.id.arrow_back);

    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    mSearchBar.clearFocus();
    mSearchBar.setNavigationIcon(R.drawable.ic_search);
    mSearchBar.setOnMenuItemClickListener(
        item -> {
          switch (item.getItemId()) {
            case R.id.sort:
              sortingDialog(this, this);
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

    mRecyclerView = findViewById(R.id.recycler_view);
    mSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    GridLayoutManager mLayoutManager =
        new GridLayoutManager(
            this,
            getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                ? 2
                : 1);
    mRecyclerView.setLayoutManager(mLayoutManager);
    mExamplesAdapter = new ExamplesAdapter(Commands.commandList(this), this);
    mExamplesAdapter.sortData();
    mExamplesAdapter.setOnItemClickListener(this);
    mRecyclerView.setAdapter(mExamplesAdapter);
    mRecyclerView.setVisibility(View.VISIBLE);
    editText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence text, int i, int i1, int i2) {}

          @Override
          public void onTextChanged(CharSequence text, int i, int i1, int i2) {}

          @Override
          public void afterTextChanged(Editable text) {
            filterList(text);
            mSummaryChip.setOnClickListener(
                v -> {
                  isSummaryChipClicked = true;
                  filterList(text);
                });
          }
        });
  }

  private void filterList(CharSequence text) {
    List<CommandItems> filteredList = new ArrayList<>();
    mSummaryChip.setVisibility(View.GONE);
    noCommandFoundText.setVisibility(View.GONE);
    if (text != null && !text.toString().isEmpty()) {
      searchTitle(text, filteredList);

      if (filteredList.isEmpty()) {
        noCommandFoundText.setVisibility(View.VISIBLE);
        mSummaryChip.setVisibility(View.VISIBLE);
        if (isSummaryChipClicked) {
          chipSummaryOnClick(text, filteredList);
        }
      }

      if (filteredList.isEmpty()) {
        noCommandFoundText.setVisibility(View.VISIBLE);
      }
    }

    mSearchRecyclerView.setVisibility(View.VISIBLE);
    CommandsSearchAdapter adapter = new CommandsSearchAdapter(filteredList, this);
    mSearchRecyclerView.setAdapter(adapter);
  }

  private void searchTitle(CharSequence text, List<CommandItems> filteredList) {
    for (CommandItems item : itemList) {
      if (item.getTitle().toLowerCase().contains(text.toString().toLowerCase())) {
        filteredList.add(item);
      }
    }
  }

  private void searchTitleAndSummary(CharSequence text, List<CommandItems> filteredList) {
    for (CommandItems item : itemList) {
      if (item.getTitle().toLowerCase().contains(text.toString().toLowerCase())
          || item.getSummary().toLowerCase().contains(text.toString().toLowerCase())) {
        filteredList.add(item);
      }
    }
  }

  private void chipSummaryOnClick(CharSequence text, List<CommandItems> filteredList) {
    noCommandFoundText.setVisibility(View.GONE);
    searchTitleAndSummary(text, filteredList);
    mSummaryChip.setVisibility(View.GONE);
  }

  private void sortingDialog(Context context, Activity activity) {
    CharSequence[] sortingOptions = {
      getString(R.string.sort_A_Z),
      getString(R.string.sort_Z_A),
      getString(R.string.most_used),
      getString(R.string.least_used)
    };

    int currentSortingOption = Preferences.getSortingExamples(context);
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
              Preferences.setSortingExamples(context, sortingOption[0]);
              if (isSortingOptionSame != sortingOption[0]) {
                mExamplesAdapter.sortData();
              }
            })
        .setNegativeButton(getString(R.string.cancel), (dialog, i) -> {})
        .show();
  }

  private void updateSearchBar() {
    int numSelectedItems = mExamplesAdapter.getSelectedItemsSize();
    isAllItemsSelected = numSelectedItems == mExamplesAdapter.getItemCount();
    if (numSelectedItems > 0) {
      startSelection(numSelectedItems);
    } else {
      endSelection();
    }
  }

  private void mSearchBarNavigationIconOnClickListener(int numSelectedItems) {
    mSearchBar.setNavigationOnClickListener(
        v -> {
          if (numSelectedItems > 0) {
            endSelection();
            mExamplesAdapter.deselectAll();
          }
        });
  }

  private void endSelection() {
    mSearchBar.setHint(R.string.search_command);
    mSearchBar.setNavigationIcon(R.drawable.ic_search);
    mSearchBar.setClickable(true);
    mSearchBarNavigationIconOnClickListener(0);
    updateMenuItemVisibility(false, isAllItemsSelected);
  }

  private void startSelection(int numSelectedItems) {
    String hint =
        getString(R.string.selected) + "\t\t" + "( " + Integer.toString(numSelectedItems) + " )";
    mSearchBar.setHint(hint);
    mSearchBar.setNavigationIcon(R.drawable.ic_cross);
    mSearchBar.setClickable(false);
    mSearchBarNavigationIconOnClickListener(numSelectedItems);
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
    } else {
      addBookmark.setVisible(false);
    }
  }

  private void batchBookmarkDialog(
      int selectedCount, boolean isAllItemBookmarked, boolean isLimitReached, boolean isBatch) {

    String message =
        isAllItemBookmarked
            ? getString(R.string.confirm_batch_remove_bookmark, selectedCount)
            : getString(R.string.confirm_batch_add_bookmark, selectedCount);

    new MaterialAlertDialogBuilder(this)
        .setTitle(getString(R.string.confirm))
        .setMessage(message)
        .setPositiveButton(
            getString(R.string.ok),
            (dialog, i) -> {
              if (isAllItemBookmarked) {
                mExamplesAdapter.deleteSelectedFromBookmarks();
              } else if (!isLimitReached) {
                mExamplesAdapter.addSelectedToBookmarks();
              }
              updateSearchBar();
              bookmarksAddedOrRemovedMessage(
                  !isAllItemBookmarked, isBatch, isLimitReached, selectedCount);
            })
        .setNegativeButton(getString(R.string.cancel), (dialog, i) -> {})
        .show();
  }

  private void manageBookmarkAddOrRemove() {

    int selectedItems = mExamplesAdapter.getSelectedItemsSize();
    boolean isAllItemBookmarked = mExamplesAdapter.isAllItemsBookmarked(),
        isLimitReached =
            selectedItems + Utils.getBookmarks(this).size() > Preferences.MAX_BOOKMARKS_LIMIT
                && !Preferences.getOverrideBookmarks(this);

    boolean isBatch = selectedItems > 1;
    if (isBatch) {
      batchBookmarkDialog(selectedItems, isAllItemBookmarked, isLimitReached, isBatch);
    } else {
      if (isAllItemBookmarked) {
        mExamplesAdapter.deleteSelectedFromBookmarks();
      } else if (!isLimitReached) {
        mExamplesAdapter.addSelectedToBookmarks();
      }
      updateSearchBar();
      bookmarksAddedOrRemovedMessage(!isAllItemBookmarked, isBatch, isLimitReached, selectedItems);
    }
  }

  private void bookmarksAddedOrRemovedMessage(
      boolean isAdded, boolean isBatch, boolean isLimitReached, int selectedCount) {

    if (isLimitReached && isAdded) {
      Utils.snackBar(parent, getString(R.string.bookmark_limit_reached)).show();
    } else if (isBatch) {
      int message =
          isAdded ? R.string.batch_bookmark_added_message : R.string.batch_bookmark_removed_message;
      Utils.snackBar(parent, getString(message, selectedCount)).show();
    } else {
      String command =
          mExamplesAdapter.sanitizeText(mExamplesAdapter.selectedItems.get(0).getTitle());
      int message = isAdded ? R.string.bookmark_added_message : R.string.bookmark_removed_message;
      Utils.snackBar(parent, getString(message, command)).show();
    }
  }

  private void managePinUnpin() {
    int size = mExamplesAdapter.getSelectedItemsSize();
    boolean isBatch = size > 1;

    boolean isAllItemsPinned = mExamplesAdapter.isAllItemsPinned();
    String message =
        isAllItemsPinned ? getString(R.string.confirm_unpin) : getString(R.string.confirm_pin);
    String positiveButtonText =
        isAllItemsPinned ? getString(R.string.unpin) : getString(R.string.pin);
    String snackBarMessage;
    if (isBatch) {
      snackBarMessage =
          isAllItemsPinned
              ? getString(R.string.batch_unpinned_message, size)
              : getString(R.string.batch_pinned_message, size);
    } else {
      String title = mExamplesAdapter.selectedItems.get(0).getTitle();
      snackBarMessage =
          isAllItemsPinned
              ? getString(R.string.unpinned_message, title)
              : getString(R.string.pinned_message, title);
    }
    new MaterialAlertDialogBuilder(this)
        .setTitle(getString(R.string.confirm))
        .setMessage(message)
        .setPositiveButton(
            positiveButtonText,
            (dialog, i) -> {
              mExamplesAdapter.pinUnpinSelectedItems(isAllItemsPinned);
              endSelection();
              updateSearchBar();

              Utils.snackBar(parent, snackBarMessage).show();
            })
        .setNegativeButton(getString(R.string.cancel), (dialog, i) -> {})
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
}
