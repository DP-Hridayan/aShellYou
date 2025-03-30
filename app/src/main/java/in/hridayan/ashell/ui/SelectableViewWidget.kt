package `in`.hridayan.ashell.ui

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.R.styleable.SelectableViewWidget_descriptionText

class SelectableViewWidget : RelativeLayout {
    private lateinit var context: Context
    private lateinit var container: MaterialCardView
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var iconImageView: ImageView
    private lateinit var onClickListener: OnClickListener

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        this.context = context
        inflate(context, R.layout.view_widget_selectable, this)

        initializeId()

        context.withStyledAttributes(attrs, R.styleable.SelectableViewWidget) {
            setTitle(getString(R.styleable.SelectableViewWidget_titleText))
            setDescription(getString(SelectableViewWidget_descriptionText))
            setSelected(getBoolean(R.styleable.SelectableViewWidget_isSelected, false))
        }

        container.setOnClickListener(OnClickListener { v: View? ->
            if (!isSelected) {
                setSelected(true)
                onClickListener.onClick(v)
            }
        })

        updateViewOnOrientation()
    }

    fun setTitle(titleResId: Int) {
        titleTextView.setText(titleResId)
    }

    fun setTitle(title: String?) {
        titleTextView.text = title
    }

    fun setDescription(descriptionResId: Int) {
        descriptionTextView.setText(descriptionResId)
    }

    fun setDescription(description: String?) {
        descriptionTextView.text = description
    }

    override fun isSelected(): Boolean {
        return iconImageView.alpha == 1.0f
    }

    override fun setSelected(isSelected: Boolean) {
        iconImageView.setAlpha(if (isSelected) 1.0f else 0.2f)
        iconImageView.setColorFilter(this.iconColor, PorterDuff.Mode.SRC_IN)
        iconImageView.setImageResource(if (isSelected) R.drawable.ic_checked_filled else R.drawable.ic_checked_outline)
        container.setCardBackgroundColor(this.cardBackgroundColor)
        container.setStrokeWidth(if (isSelected) 0 else 2)
        titleTextView.setTextColor(getTextColor(isSelected))
        descriptionTextView.setTextColor(getTextColor(isSelected))
    }

    override fun setOnClickListener(l: OnClickListener?) {
        if (l != null) {
            onClickListener = l
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        if (enabled) {
            val typedValue = TypedValue()
            val a = context.obtainStyledAttributes(
                typedValue.data,
                intArrayOf(com.google.android.material.R.attr.colorPrimary)
            )
            val color = a.getColor(0, 0)
            a.recycle()

            iconImageView.setImageTintList(ColorStateList.valueOf(color))

            titleTextView.setAlpha(1.0f)
            descriptionTextView.setAlpha(0.8f)
        } else {
            val isDarkMode = (context.resources
                .configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES

            if (isDarkMode) {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.DKGRAY))
            } else {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.LTGRAY))
            }

            titleTextView.setAlpha(0.6f)
            descriptionTextView.setAlpha(0.4f)
        }

        container.setEnabled(enabled)
        iconImageView.setEnabled(enabled)
        titleTextView.setEnabled(enabled)
        descriptionTextView.setEnabled(enabled)
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById<MaterialCardView>(R.id.container)
        iconImageView = findViewById<ImageView>(R.id.icon)
        titleTextView = findViewById<TextView>(R.id.title)
        descriptionTextView = findViewById<TextView>(R.id.description)

        container.setId(generateViewId())
        iconImageView.setId(generateViewId())
        titleTextView.setId(generateViewId())
        descriptionTextView.setId(generateViewId())
    }

    @get:ColorInt
    private val cardBackgroundColor: Int
        get() = if (isSelected) MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorPrimaryContainer
        ) else MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorSurfaceContainer
        )

    @get:ColorInt
    private val iconColor: Int
        get() = if (isSelected) MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorPrimary
        ) else MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface)

    @ColorInt
    private fun getTextColor(isSelected: Boolean): Int {
        return if (isSelected) MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorOnPrimaryContainer
        ) else MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface)
    }

    private fun updateViewOnOrientation() {
        val config = context.resources.configuration
        val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            val screenWidth = context.resources.displayMetrics.widthPixels
            val screenHeight = context.resources.displayMetrics.heightPixels

            val isSmallHeightDevice = screenWidth >= screenHeight * 1.8

            if (isSmallHeightDevice) {
                container.setMinimumHeight(0)
                descriptionTextView.visibility = GONE
            }
        } else {
            val minHeightInDp = 100
            val minHeightInPixels =
                (minHeightInDp * context.resources.displayMetrics.density).toInt()
            container.setMinimumHeight(minHeightInPixels)
            descriptionTextView.visibility = VISIBLE
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val ss = SavedState(superState)
        ss.isSelected = isSelected

        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        setSelected(state.isSelected)
        updateViewOnOrientation()
    }

    private class SavedState : BaseSavedState {
        var isSelected: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                dest.writeBoolean(isSelected)
            }
        }
    }
}
