package `in`.hridayan.ashell.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.adapters.AboutAdapter
import `in`.hridayan.ashell.adapters.AboutAdapter.AdapterListener
import `in`.hridayan.ashell.config.Const
import `in`.hridayan.ashell.config.Const.Contributors
import `in`.hridayan.ashell.databinding.FragmentAboutBinding
import `in`.hridayan.ashell.ui.CategoryAbout
import `in`.hridayan.ashell.ui.CategoryAbout.AppItem
import `in`.hridayan.ashell.ui.CategoryAbout.ContributorsItem
import `in`.hridayan.ashell.ui.CategoryAbout.LeadDeveloperItem
import `in`.hridayan.ashell.ui.ToastUtils
import `in`.hridayan.ashell.ui.bottomsheets.UpdateCheckerBottomSheet
import `in`.hridayan.ashell.utils.DeviceUtils.FetchLatestVersionCodeCallback
import `in`.hridayan.ashell.utils.HapticUtils
import `in`.hridayan.ashell.utils.Utils
import `in`.hridayan.ashell.utils.app.updater.FetchLatestVersionCode
import `in`.hridayan.ashell.viewmodels.AboutViewModel

class AboutFragment : Fragment(), AdapterListener,
    FetchLatestVersionCodeCallback {
    private val viewModel: AboutViewModel by viewModels()
    private lateinit var binding: FragmentAboutBinding
    private var mRVPositionAndOffset: android.util.Pair<Int?, Int>? = null
    private lateinit var loadingDots: LottieAnimationView
    private lateinit var updateButtonIcon: Drawable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupListeners()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvAbout.layoutManager = LinearLayoutManager(context)
        val adapter: AboutAdapter = AboutAdapter(initializeItems(), requireActivity())
        adapter.setAdapterListener(this)
        binding.rvAbout.adapter = adapter
        binding.rvAbout.viewTreeObserver.addOnDrawListener { this.startPostponedEnterTransition() }
    }

    private fun setupListeners() {
        binding.arrowBack.setOnClickListener { v: View? ->
            HapticUtils.weakVibrate(v)
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun initializeItems(): List<Any> {
        val items: MutableList<Any> = ArrayList()
        items.add(CategoryAbout(getString(R.string.lead_developer)))
        items.add(
            LeadDeveloperItem(
                Contributors.HRIDAYAN.getName(),
                getString(R.string.hridayan_about),
                R.mipmap.dp_hridayan
            )
        )

        items.add(CategoryAbout(getString(R.string.contributors)))
        items.add(
            ContributorsItem(
                Contributors.KRISHNA,
                Contributors.KRISHNA.getName(),
                getString(R.string.krishna_about),
                R.mipmap.dp_krishna
            )
        )
        items.add(
            ContributorsItem(
                Contributors.STARRY,
                Contributors.STARRY.getName(),
                getString(R.string.shivam_about),
                R.mipmap.dp_shivam
            )
        )
        items.add(
            ContributorsItem(
                Contributors.DISAGREE,
                Contributors.DISAGREE.getName(),
                getString(R.string.drDisagree_about),
                R.mipmap.dp_drdisagree
            )
        )
        items.add(
            ContributorsItem(
                Contributors.RIKKA,
                Contributors.RIKKA.getName(),
                getString(R.string.rikka_about),
                R.mipmap.dp_shizuku
            )
        )
        items.add(
            ContributorsItem(
                Contributors.SUNILPAULMATHEW,
                Contributors.SUNILPAULMATHEW.getName(),
                getString(R.string.sunilpaulmathew_about),
                R.mipmap.dp_sunilpaulmathew
            )
        )
        items.add(
            ContributorsItem(
                Contributors.KHUN_HTETZ,
                Contributors.KHUN_HTETZ.getName(),
                getString(R.string.khun_htetz_about),
                R.mipmap.dp_adb_otg
            )
        )
        items.add(
            ContributorsItem(
                Contributors.MARCIOZOMB,
                Contributors.MARCIOZOMB.getName(),
                getString(R.string.marciozomb13_about),
                R.mipmap.dp_marciozomb13
            )
        )
        items.add(
            ContributorsItem(
                Contributors.WEIGUANGTWK,
                Contributors.WEIGUANGTWK.getName(),
                getString(R.string.weiguangtwk_about),
                R.mipmap.dp_weiguangtwk
            )
        )
        items.add(
            ContributorsItem(
                Contributors.WINZORT,
                Contributors.WINZORT.getName(),
                getString(R.string.winzort_about),
                R.mipmap.dp_winzort
            )
        )

        items.add(CategoryAbout(getString(R.string.app)))
        try {
            val pInfo =
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            items.add(
                AppItem(
                    Const.ID_VERSION,
                    getString(R.string.version),
                    pInfo.versionName,
                    R.drawable.ic_version_tag
                )
            )
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        items.add(
            AppItem(
                Const.ID_CHANGELOGS,
                getString(R.string.changelogs),
                getString(R.string.des_changelogs),
                R.drawable.ic_changelog
            )
        )
        items.add(
            AppItem(
                Const.ID_REPORT,
                getString(R.string.report_issue),
                getString(R.string.des_report_issue),
                R.drawable.ic_report
            )
        )
        items.add(
            AppItem(
                Const.ID_FEATURE,
                getString(R.string.feature_request),
                getString(R.string.des_feature_request),
                R.drawable.ic_feature
            )
        )
        items.add(
            AppItem(
                Const.ID_GITHUB,
                getString(R.string.github),
                getString(R.string.des_github),
                R.drawable.ic_github
            )
        )
        items.add(
            AppItem(
                Const.ID_TELEGRAM,
                getString(R.string.telegram_channel),
                getString(R.string.des_telegram_channel),
                R.drawable.ic_telegram
            )
        )
        items.add(
            AppItem(
                Const.ID_LICENSE,
                getString(R.string.license),
                getString(R.string.des_license),
                R.drawable.ic_license
            )
        )

        return items
    }

    override fun onPause() {
        super.onPause()
        saveRecyclerViewState()
        viewModel.isToolbarExpanded = Utils.isToolbarExpanded(binding.appBarLayout)
    }

    override fun onResume() {
        super.onResume()
        restoreRecyclerViewState()
        binding.appBarLayout.setExpanded(viewModel.isToolbarExpanded)
    }

    private fun saveRecyclerViewState() {
        val layoutManager = binding.rvAbout.layoutManager as LinearLayoutManager?
        if (layoutManager != null) {
            val currentPosition = layoutManager.findLastVisibleItemPosition()
            val currentView = layoutManager.findViewByPosition(currentPosition)
            if (currentView != null) {
                mRVPositionAndOffset = Pair(currentPosition, currentView.top)
                viewModel.rvPositionAndOffset = mRVPositionAndOffset
            }
        }
    }

    private fun restoreRecyclerViewState() {
        mRVPositionAndOffset = viewModel.rvPositionAndOffset
        val layoutManager = binding.rvAbout.layoutManager as LinearLayoutManager?
        layoutManager?.scrollToPositionWithOffset(
            mRVPositionAndOffset?.first?:return, mRVPositionAndOffset?.second?:return
        )
    }

    override fun onCheckUpdate(button: Button?, animation: LottieAnimationView) {
        this.loadingDots = animation
        if (button != null) {
            updateButtonIcon = button.compoundDrawables[0] // Save the original icon
            button.text = null
            // casting button to MaterialButton to use setIcon method.
            (button as MaterialButton).icon = null
            button.setMinWidth(button.getWidth())
            button.setMinHeight(button.getHeight())
        }

        loadingDots.visibility = View.VISIBLE
        FetchLatestVersionCode(context, this).execute(Const.URL_BUILD_GRADLE)
    }

    override fun onResult(result: Int) {
        // Restore the original icon and text
        if (this.view != null) {
            loadingDots.visibility = View.GONE
            val button = requireView().findViewById<Button>(R.id.check_update_button)
            button.setText(R.string.check_updates)
            // casting button to MaterialButton to use setIcon method.
            (button as MaterialButton).icon = updateButtonIcon
        }

        when (result) {
            Const.UPDATE_AVAILABLE -> {
                val updateChecker: UpdateCheckerBottomSheet =
                    UpdateCheckerBottomSheet(requireActivity(), requireContext())
                updateChecker.show()
                return
            }

            Const.UPDATE_NOT_AVAILABLE -> latestVersionDialog(requireContext())

            Const.CONNECTION_ERROR -> ToastUtils.showToast(
                requireContext(),
                R.string.check_internet,
                ToastUtils.LENGTH_SHORT
            )

            else -> return
        }
    }

    private fun latestVersionDialog(context: Context) {
        if (isAdded) {
            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.dialog_latest_version, null)

            MaterialAlertDialogBuilder(context).setView(dialogView).show()
        }
    }

    private fun generateLongMessage(): String {
        val message = StringBuilder("Long Exception Message: ")
        for (i in 0..999) { // Adjust the loop count for longer messages
            message.append("This is an intentional crash ").append(i).append(". ")
        }
        return message.toString()
    }

    fun throwLongException() {
        val longMessage = generateLongMessage()
        throw RuntimeException(longMessage)
    }
}
