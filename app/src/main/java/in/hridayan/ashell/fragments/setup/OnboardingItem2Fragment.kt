package `in`.hridayan.ashell.fragments.setup

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import `in`.hridayan.ashell.databinding.FragmentOnboardingItem2Binding

class OnboardingItem2Fragment : Fragment() {
    private lateinit var binding: FragmentOnboardingItem2Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingItem2Binding.inflate(inflater, container, false)

        // We take 10% of the screen height as top margin an then assign a top margin to the disclaimer
        // title
        val screenHeight = resources.displayMetrics.heightPixels
        val topMargin = (screenHeight * 0.1).toInt()

        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        params.topMargin = topMargin
        params.gravity = Gravity.CENTER_HORIZONTAL

        binding.disclaimer.layoutParams = params

        return binding.root
    }
}
