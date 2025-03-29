package `in`.hridayan.ashell.fragments.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.button.MaterialButton
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.adapters.OnboardingAdapter
import `in`.hridayan.ashell.config.Const
import `in`.hridayan.ashell.config.Preferences
import `in`.hridayan.ashell.fragments.home.HomeFragment
import `in`.hridayan.ashell.utils.HapticUtils

class StartFragment : Fragment() {
    private lateinit var adapter: OnboardingAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: MaterialButton
    private lateinit var btnPrev: MaterialButton
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        viewPager = view.findViewById(R.id.viewPager)
        btnNext = view.findViewById(R.id.btn_next)
        btnPrev = view.findViewById(R.id.btn_prev)

        adapter = OnboardingAdapter(childFragmentManager, requireActivity().lifecycle)

        adapter.addFragment(OnboardingItem1Fragment())
        adapter.addFragment(OnboardingItem2Fragment())
        adapter.addFragment(OnboardingItem3Fragment())

        viewPager.setAdapter(adapter)

        viewPager.registerOnPageChangeCallback(
            object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    animateBackButton(position)
                    changeContinueButtonText(position)
                }
            })

        btnNext.setOnClickListener(
            View.OnClickListener { v: View? ->
                HapticUtils.weakVibrate(v)
                if (viewPager.currentItem == adapter.itemCount - 1) {
                    enterHomeFragment()
                } else { // this is not the last page, so just go to next page
                    viewPager.currentItem += 1
                }
            })

        btnPrev.setOnClickListener(
            View.OnClickListener { v: View? ->
                HapticUtils.weakVibrate(v)
                viewPager.setCurrentItem(viewPager.currentItem - 1, true)
            })
        registerOnBackInvokedCallback()
    }

    private val isBasicMode: Boolean
        get() = Preferences.getLocalAdbMode() == Const.BASIC_MODE

    /* private void confirmationDialog() {
   new MaterialAlertDialogBuilder(requireActivity())
       .setTitle(getString(R.string.warning))
       .setMessage(getString(R.string.confirm_basic_mode))
       .setNegativeButton(getString(R.string.cancel), null)
       .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> enterHomeFragment())
       .show();
 }
   */
    private fun enterHomeFragment() {
        Preferences.setFirstLaunch(false)
        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.fragment_enter,
                R.anim.fragment_exit,
                R.anim.fragment_pop_enter,
                R.anim.fragment_pop_exit
            )
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    private fun animateBackButton(position: Int) {
        val duration = 300

        if (position == 0 && btnPrev.isVisible) {
            val fadeOut = getFadeOutAnimation(duration)
            btnPrev.startAnimation(fadeOut)
        } else if (position != 0 && btnPrev.visibility != View.VISIBLE) {
            val fadeIn = getFadeInAnimation(duration)
            btnPrev.startAnimation(fadeIn)
        }
    }

    private fun getFadeOutAnimation(duration: Int): AlphaAnimation {
        val fadeOut = AlphaAnimation(1.0f, 0.0f)

        fadeOut.duration = duration.toLong()

        fadeOut.setAnimationListener(
            object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    btnPrev.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })

        return fadeOut
    }

    private fun getFadeInAnimation(duration: Int): AlphaAnimation {
        val fadeIn = AlphaAnimation(0.0f, 1.0f)

        fadeIn.duration = duration.toLong()

        fadeIn.setAnimationListener(
            object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    btnPrev.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation) {}

                override fun onAnimationRepeat(animation: Animation) {}
            })

        return fadeIn
    }

    private fun changeContinueButtonText(position: Int) {
        if (position == adapter.itemCount - 1) btnNext.setText(R.string.start)
        else btnNext.setText(R.string.btn_continue)
    }

    private fun registerOnBackInvokedCallback() {
        onBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(requireActivity(), onBackPressedCallback)
    }

    private fun onBackPressed() {
        if (viewPager.currentItem == 0) requireActivity().finish()
        else viewPager.setCurrentItem(viewPager.currentItem - 1, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onBackPressedCallback.remove()
    }
}
