package in.hridayan.ashell.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.R;
import com.google.android.material.search.SearchView;

public class CustomSearchView extends SearchView {

  public CustomSearchView(@NonNull Context context) {
    super(context);
  }

  public CustomSearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomSearchView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public EditText getSearchEditText() {
    return findViewById(R.id.open_search_view_edit_text);
  }
}
