package `in`.hridayan.ashell.fragments

import android.content.Context
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedDispatcher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.adapters.ChangelogAdapter
import `in`.hridayan.ashell.databinding.FragmentChangelogBinding
import `in`.hridayan.ashell.items.ChangelogItem
import `in`.hridayan.ashell.utils.HapticUtils
import `in`.hridayan.ashell.utils.Utils
import `in`.hridayan.ashell.viewmodels.ChangelogViewModel

class ChangelogFragment : Fragment() {
    private  val viewModel: ChangelogViewModel by viewModels()
    private lateinit var context: Context
    private lateinit var binding: FragmentChangelogBinding
    private lateinit var mRVPositionAndOffset: Pair<Int, Int>

    private val versionNames = arrayOf(
        "v6.0.2", "v6.0.1", "v6.0.0", "v5.2.1", "v5.2.0",
        "v5.1.0", "v5.0.0", "v4.4.0", "v4.3.1", "v4.3.0", "v4.2.1", "v4.2.0", "v4.1.0", "v4.0.3",
        "v4.0.2", "v4.0.1", "v4.0.0", "v3.9.1", "v3.9.0", "v3.8.2", "v3.8.1", "v3.8.0", "v3.7.0",
        "v3.6.0", "v3.5.1", "v3.5.0", "v3.4.0", "v3.3.0", "v3.2.0", "v3.1.0", "v3.0.0", "v2.0.2",
        "v2.0.1", "v2.0.0", "v1.3.0", "v1.2.0", "v1.1.1", "v1.1.0", "v1.0.0", "v0.9.1", "v0.9.0"
    )

    override fun onPause() {
        super.onPause()
        val layoutManager =
            binding.rvChangelogs.layoutManager as LinearLayoutManager?

        val currentPosition = layoutManager?.findLastVisibleItemPosition()
        val currentView = layoutManager?.findViewByPosition(currentPosition!!)

        if (currentView != null) {
            mRVPositionAndOffset = Pair(currentPosition, currentView.top)
            viewModel.rvPositionAndOffset = mRVPositionAndOffset
        }
        // Save toolbar state
        viewModel.isToolbarExpanded = Utils.isToolbarExpanded(binding.appBarLayout)
    }

    override fun onResume() {
        super.onResume()
        if (binding.rvChangelogs.layoutManager != null) {
            viewModel.isToolbarExpanded.let { binding.appBarLayout.setExpanded(it) }

            mRVPositionAndOffset = viewModel.rvPositionAndOffset
            val position = viewModel.rvPositionAndOffset?.first
            val offset = viewModel.rvPositionAndOffset?.second

            // Restore recyclerView scroll position
            (binding.rvChangelogs.layoutManager as LinearLayoutManager)
                .scrollToPositionWithOffset(position!!, offset!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangelogBinding.inflate(inflater, container, false)
        context = requireContext()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dispatcher: OnBackPressedDispatcher = requireActivity().onBackPressedDispatcher
        binding.arrowBack.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            dispatcher.onBackPressed()
        }

        val changelogItems: MutableList<ChangelogItem> = ArrayList<ChangelogItem>()

        for (versionName in versionNames) {
            changelogItems.add(
                ChangelogItem(
                    getString(R.string.version) + "\t\t" + versionName,
                    Utils.loadChangelogText(versionName, getContext())
                )
            )
        }

        val adapter: ChangelogAdapter = ChangelogAdapter(changelogItems, getContext())
        binding.rvChangelogs.adapter = adapter
        binding.rvChangelogs.layoutManager = LinearLayoutManager(context)
    }
}
