package `in`.hridayan.ashell.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import com.google.android.material.R
import com.google.android.material.search.SearchView

class CustomSearchView : SearchView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    val searchEditText: EditText
        get() = findViewById<EditText>(R.id.open_search_view_edit_text)
}
