package `in`.hridayan.ashell.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Pair
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.adapters.CommandsSearchAdapter
import `in`.hridayan.ashell.adapters.CommandsSearchAdapter.UseCommandInSearchListener
import `in`.hridayan.ashell.adapters.ExamplesAdapter
import `in`.hridayan.ashell.adapters.ExamplesAdapter.UseCommandListener
import `in`.hridayan.ashell.config.Const
import `in`.hridayan.ashell.config.Preferences
import `in`.hridayan.ashell.databinding.FragmentExamplesBinding
import `in`.hridayan.ashell.fragments.home.AshellFragment
import `in`.hridayan.ashell.fragments.home.OtgFragment
import `in`.hridayan.ashell.fragments.home.WifiAdbFragment
import `in`.hridayan.ashell.items.CommandItems
import `in`.hridayan.ashell.ui.KeyboardUtils
import `in`.hridayan.ashell.utils.Commands
import `in`.hridayan.ashell.utils.HapticUtils
import `in`.hridayan.ashell.utils.Utils
import `in`.hridayan.ashell.viewmodels.ExamplesViewModel
import `in`.hridayan.ashell.viewmodels.MainViewModel
import java.util.Locale

class ExamplesFragment : Fragment(), ExamplesAdapter.OnItemClickListener,
    UseCommandListener, UseCommandInSearchListener {
    private val viewModel: ExamplesViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var itemList: List<CommandItems>
    private lateinit var mExamplesAdapter: ExamplesAdapter
    private lateinit var sort: MenuItem
    private lateinit var pin: MenuItem
    private lateinit var selectAll: MenuItem
    private lateinit var addBookmark: MenuItem
    private lateinit var deselectAll: MenuItem
    private lateinit var searchBarMenu: Menu
    private lateinit var view: View
    private lateinit var editText: EditText
    private var isSortingOptionSame = 0
    private var isSummaryChipClicked = false
    private var isAllItemsSelected = false
    private var context: Context? = null
    private lateinit var binding: FragmentExamplesBinding
    private lateinit var mRVPositionAndOffset: Pair<Int, Int>

    override fun onPause() {
        super.onPause()
        val layoutManager =
            binding.rvSearchView.layoutManager as LinearLayoutManager?

        val currentPosition = layoutManager?.findLastVisibleItemPosition()
        val currentView = layoutManager?.findViewByPosition(currentPosition!!)

        if (currentView != null) {
            mRVPositionAndOffset = Pair(currentPosition, currentView.top)
            viewModel.rvPositionAndOffset = mRVPositionAndOffset
        }
        // Save toolbar state
        viewModel.isToolbarExpanded = Utils.isToolbarExpanded(binding.appBarLayout)

        viewModel.isEnteringFromSettings = false
    }

    override fun onResume() {
        super.onResume()
        if (binding.rvSearchView.layoutManager != null) {
            binding.appBarLayout.setExpanded(viewModel.isToolbarExpanded ?: return)

            mRVPositionAndOffset = viewModel.rvPositionAndOffset
            val position: Int? = viewModel.rvPositionAndOffset?.first
            val offset: Int? = viewModel.rvPositionAndOffset?.second

            // Restore recyclerView scroll position
            (binding.rvSearchView.layoutManager as LinearLayoutManager)
                .scrollToPositionWithOffset(position ?: return, offset ?: return)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!viewModel.isEnteringFromSettings) sharedElementEnterTransition =
            MaterialContainerTransform()

        binding = FragmentExamplesBinding.inflate(inflater, container, false)

        view = binding.root

        context = requireContext()

        editText = binding.searchView.searchEditText
        searchBarMenu = binding.searchBar.menu
        sort = searchBarMenu.findItem(R.id.sort)
        addBookmark = searchBarMenu.findItem(R.id.add_bookmark)
        pin = searchBarMenu.findItem(R.id.pin)
        selectAll = searchBarMenu.findItem(R.id.select_all)
        deselectAll = searchBarMenu.findItem(R.id.deselect_all)
        itemList = Commands.commandList(context)

        val dispatcher: OnBackPressedDispatcher = requireActivity().onBackPressedDispatcher

        binding.arrowBack.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            dispatcher.onBackPressed()
        }

        binding.searchBar.clearFocus()
        binding.searchBar.setNavigationIcon(R.drawable.ic_search)
        binding.searchBar.setOnMenuItemClickListener { item: MenuItem ->
            HapticUtils.weakVibrate(view)
            when (item.itemId) {
                R.id.sort -> {
                    sortingDialog(requireContext(), requireActivity())
                    return@setOnMenuItemClickListener true
                }

                R.id.select_all -> {
                    mExamplesAdapter.selectAll()
                    updateSearchBar()
                    return@setOnMenuItemClickListener true
                }

                R.id.deselect_all -> {
                    mExamplesAdapter.deselectAll()
                    updateSearchBar()
                    return@setOnMenuItemClickListener true
                }

                R.id.add_bookmark -> {
                    manageBookmarkAddOrRemove()
                    return@setOnMenuItemClickListener true
                }

                R.id.pin -> {
                    managePinUnpin()
                    return@setOnMenuItemClickListener true
                }

                else -> return@setOnMenuItemClickListener false
            }
        }

        binding.rvSearch.layoutManager = LinearLayoutManager(context)

        binding.rvSearchView.layoutManager = LinearLayoutManager(context)
        val mLayoutManager = GridLayoutManager(context, spanCount)
        binding.rvSearchView.layoutManager = mLayoutManager

        mExamplesAdapter = ExamplesAdapter(
            Commands.commandList(context), context,
            this
        )
        mExamplesAdapter.sortData()
        mExamplesAdapter.setOnItemClickListener(this)
        binding.rvSearchView.adapter = mExamplesAdapter
        binding.rvSearchView.visibility = View.VISIBLE

        editText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(text: CharSequence, i: Int, i1: Int, i2: Int) {
                }

                override fun onTextChanged(text: CharSequence, i: Int, i1: Int, i2: Int) {
                }

                override fun afterTextChanged(text: Editable) {
                    filterList(text)
                    binding.chipSearchSummary.setOnClickListener { v: View? ->
                        isSummaryChipClicked = true
                        filterList(text)
                    }
                }
            })

        return view
    }

    private val spanCount: Int
        get() {
            val spanCount = Preferences.getExamplesLayoutStyle()
            return if (spanCount >= 1) spanCount
            else 1
        }

    private fun filterList(text: CharSequence?) {
        val filteredList: MutableList<CommandItems> = ArrayList<CommandItems>()

        binding.chipSearchSummary.visibility = View.GONE
        binding.noCommandFound.visibility = View.GONE
        binding.searchImg.visibility = View.VISIBLE

        if (text != null && !text.toString().isEmpty()) {
            searchTitle(text, filteredList)

            if (filteredList.isEmpty()) {
                binding.noCommandFound.visibility = View.VISIBLE
                binding.chipSearchSummary.visibility = View.VISIBLE
                binding.searchImg.visibility = View.VISIBLE
                if (isSummaryChipClicked) chipSummaryOnClick(text, filteredList)
                /* if filtered list is empty again after running chipSummaryOnClick, then we show no command found text message */
                if (filteredList.isEmpty()) {
                    binding.noCommandFound.visibility = View.VISIBLE
                    binding.searchImg.visibility = View.VISIBLE
                }
            } else binding.searchImg.visibility = View.GONE
        }
        binding.rvSearch.visibility = View.VISIBLE
        val adapter: CommandsSearchAdapter = CommandsSearchAdapter(filteredList, context, this)
        binding.rvSearch.adapter = adapter
    }

    // Conduct search in only command titles
    private fun searchTitle(text: CharSequence, filteredList: MutableList<CommandItems>) {
        for (item in itemList) {
            if (item.title
                    .lowercase(Locale.getDefault())
                    .contains(text.toString().lowercase(Locale.getDefault()))
            ) {
                filteredList.add(item)
            }
        }
    }

    // Conduct search in both command titles and summary
    private fun searchTitleAndSummary(text: CharSequence, filteredList: MutableList<CommandItems>) {
        for (item in itemList) {
            if (item.title
                    .lowercase(Locale.getDefault())
                    .contains(text.toString().lowercase(Locale.getDefault()))
                || item.summary
                    .lowercase(Locale.getDefault())
                    .contains(text.toString().lowercase(Locale.getDefault()))
            ) {
                filteredList.add(item)
            }
        }
    }

    /* Onclick function for the chip which appears when no search result is found when only searched through titles */
    private fun chipSummaryOnClick(text: CharSequence, filteredList: MutableList<CommandItems>) {
        binding.noCommandFound.visibility = View.GONE
        binding.searchImg.visibility = View.GONE
        searchTitleAndSummary(text, filteredList)
        binding.chipSearchSummary.visibility = View.GONE
    }

    private fun sortingDialog(context: Context, activity: Activity) {
        val sortingOptions = arrayOf<CharSequence>(
            getString(R.string.sort_A_Z),
            getString(R.string.sort_Z_A),
            getString(R.string.most_used),
            getString(R.string.least_used)
        )

        val currentSortingOption = Preferences.getSortingExamples()
        isSortingOptionSame = currentSortingOption
        val sortingOption = intArrayOf(currentSortingOption)

        MaterialAlertDialogBuilder(activity)
            .setTitle(getString(R.string.sort))
            .setSingleChoiceItems(
                sortingOptions,
                currentSortingOption
            ) { dialog: DialogInterface?, which: Int ->
                sortingOption[0] = which
            }
            .setPositiveButton(
                getString(R.string.ok)
            ) { dialog: DialogInterface?, which: Int ->
                Preferences.setSortingExamples(
                    sortingOption[0]
                )
                if (isSortingOptionSame != sortingOption[0]) mExamplesAdapter.sortData()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    // Update search bar when selecting items
    private fun updateSearchBar() {
        val numSelectedItems = mExamplesAdapter.selectedItemsSize
        isAllItemsSelected = numSelectedItems == mExamplesAdapter.itemCount
        if (numSelectedItems > 0) startSelection(numSelectedItems)
        else endSelection()
    }

    private fun searchBarNavigationIconOnClickListener(numSelectedItems: Int) {
        binding.searchBar.setNavigationOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            if (numSelectedItems > 0) {
                endSelection()
                mExamplesAdapter.deselectAll()
            }
        }
    }

    // End the selection phase
    private fun endSelection() {
        binding.searchBar.setHint(R.string.search_command)
        binding.searchBar.setNavigationIcon(R.drawable.ic_search)
        binding.searchBar.isClickable = true
        searchBarNavigationIconOnClickListener(0)
        updateMenuItemVisibility(false, isAllItemsSelected)
    }

    // Enter the selection phase
    private fun startSelection(numSelectedItems: Int) {
        val hint =
            getString(R.string.selected) + "\t\t" + "( " + numSelectedItems.toString() + " )"
        binding.searchBar.hint = hint
        binding.searchBar.setNavigationIcon(R.drawable.ic_cross)
        binding.searchBar.isClickable = false
        searchBarNavigationIconOnClickListener(numSelectedItems)
        updateMenuItemVisibility(true, isAllItemsSelected)
    }

    private fun updateMenuItemVisibility(isItemSelecting: Boolean, isAllSelected: Boolean) {
        sort.setVisible(!isItemSelecting)
        pin.setVisible(isItemSelecting)
        selectAll.setVisible(isItemSelecting && !isAllSelected)
        deselectAll.setVisible(isItemSelecting && isAllSelected)

        if (isItemSelecting) {
            addBookmark.setVisible(true)
            addBookmark.setIcon(
                if (mExamplesAdapter.isAllItemsBookmarked)
                    R.drawable.ic_bookmark_added
                else
                    R.drawable.ic_add_bookmark
            )
            pin.setIcon(
                if (mExamplesAdapter.isAllItemsPinned)
                    R.drawable.ic_pinned
                else
                    R.drawable.ic_pin
            )
        } else addBookmark.setVisible(false)
    }

    @SuppressLint("StringFormatInvalid")
    private fun batchBookmarkDialog(
        selectedCount: Int, isAllItemBookmarked: Boolean, isLimitReached: Boolean, isBatch: Boolean
    ) {
        val message =
            if (isAllItemBookmarked) {
                getString(R.string.confirm_batch_remove_bookmark, selectedCount)
            } else {
                getString(R.string.confirm_batch_add_bookmark, selectedCount)
            }

        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm))
            .setMessage(message)
            .setPositiveButton(
                getString(R.string.ok)
            ) { dialog: DialogInterface?, i: Int ->
                if (isAllItemBookmarked) mExamplesAdapter.deleteSelectedFromBookmarks()
                else if (!isLimitReached) mExamplesAdapter.addSelectedToBookmarks()
                updateSearchBar()
                bookmarksAddedOrRemovedMessage(
                    !isAllItemBookmarked, isBatch, isLimitReached, selectedCount
                )
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    // Function to handle bookmarks add or remove
    private fun manageBookmarkAddOrRemove() {
        val selectedItems = mExamplesAdapter.selectedItemsSize
        val isAllItemBookmarked = mExamplesAdapter.isAllItemsBookmarked
        val isLimitReached =
            selectedItems + Utils.getBookmarks(context).size > Const.MAX_BOOKMARKS_LIMIT
                    && !Preferences.getOverrideBookmarks()

        val isBatch = selectedItems > 1
        if (isBatch) batchBookmarkDialog(
            selectedItems,
            isAllItemBookmarked,
            isLimitReached,
            isBatch
        )
        else {
            if (isAllItemBookmarked) mExamplesAdapter.deleteSelectedFromBookmarks()
            else if (!isLimitReached) mExamplesAdapter.addSelectedToBookmarks()

            updateSearchBar()
            bookmarksAddedOrRemovedMessage(
                !isAllItemBookmarked,
                isBatch,
                isLimitReached,
                selectedItems
            )
        }
    }

    // Snackbar message to show when commands are added or removed from bookmarks
    private fun bookmarksAddedOrRemovedMessage(
        isAdded: Boolean, isBatch: Boolean, isLimitReached: Boolean, selectedCount: Int
    ) {
        if (isLimitReached && isAdded) Utils.snackBar(
            view,
            getString(R.string.bookmark_limit_reached)
        ).show()
        else if (isBatch) {
            val message =
                if (isAdded) R.string.batch_bookmark_added_message else R.string.batch_bookmark_removed_message
            Utils.snackBar(view, getString(message, selectedCount)).show()
        } else {
            val command =
                mExamplesAdapter.sanitizeText(mExamplesAdapter.selectedItems[0].title)
            val message =
                if (isAdded) R.string.bookmark_added_message else R.string.bookmark_removed_message
            Utils.snackBar(view, getString(message, command)).show()
        }
    }

    // Function which manages the pin and unpin commands feature
    private fun managePinUnpin() {
        // Get the number of selected items
        val size = mExamplesAdapter.selectedItemsSize
        // This sets the title that shows number of selected items
        val title = mExamplesAdapter.selectedItems[0].title

        // If selected items is more than 1 we declare the selection as batch
        val isBatch = size > 1
        // check if every item is already pinned
        val isAllItemsPinned = mExamplesAdapter.isAllItemsPinned

        // If batch we show confirm dialog message for batch else we show it for single item
        val confirmPin =
            if (isBatch) getString(R.string.confirm_pin) else getString(
                R.string.confirm_pin_single,
                title
            )
        val confirmUnpin =
            if (isBatch)
                getString(R.string.confirm_unpin)
            else
                getString(R.string.confirm_unpin_single, title)
        val message = if (isAllItemsPinned) confirmUnpin else confirmPin

        // If all items are pinned we show unpin button else we show pin button
        val positiveButtonText =
            if (isAllItemsPinned) getString(R.string.unpin) else getString(R.string.pin)

        // Message to display after pinning or unpinning
        val snackBarMessage = if (isBatch) if (isAllItemsPinned)
            getString(R.string.batch_unpinned_message, size)
        else
            getString(R.string.batch_pinned_message, size)
        else if (isAllItemsPinned)
            getString(R.string.unpinned_message, title)
        else
            getString(R.string.pinned_message, title)

        // Dialog asking for confirmation of pin or unpin
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm))
            .setMessage(message)
            .setPositiveButton(
                positiveButtonText
            ) { dialog: DialogInterface?, i: Int ->
                mExamplesAdapter.pinUnpinSelectedItems(isAllItemsPinned)
                endSelection()
                updateSearchBar()
                Utils.snackBar(view, snackBarMessage).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onItemClick(position: Int) {
        updateSearchBar()
    }

    override fun onItemLongClick(position: Int) {
        updateSearchBar()
    }

    override fun useCommand(command: String) {
        navigateToFragmentAndSetCommand(command)
    }

    override fun useCommandInSearch(command: String) {
        navigateToFragmentAndSetCommand(command)
    }

    /* This function is called when we use the "Use" feature in commands examples to set the command in the fragment edit text */
    private fun navigateToFragmentAndSetCommand(command: String) {
        mainViewModel.setUseCommand(command)
        var fragment: Fragment = AshellFragment()
        if (mainViewModel.previousFragment() == Const.OTG_FRAGMENT) fragment = OtgFragment()
        else if (mainViewModel.previousFragment() == Const.WIFI_ADB_FRAGMENT) fragment =
            WifiAdbFragment()

        // clear previous backstacks
        clearBackStack()

        KeyboardUtils.closeKeyboard(requireActivity(), view)
        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.fragment_enter,
                R.anim.fragment_exit,
                R.anim.fragment_pop_enter,
                R.anim.fragment_pop_exit
            )
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun clearBackStack() {
        val fragmentManager = requireActivity().supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            // Pop all back stack entries
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }
}
