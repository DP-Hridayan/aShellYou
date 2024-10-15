package in.hridayan.ashell.UI;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import in.hridayan.ashell.R;

public class CoordinatedNestedScrollView extends NestedScrollView {

  public CoordinatedNestedScrollView(Context context) {
    super(context);
  }

  public CoordinatedNestedScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CoordinatedNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public boolean onStartNestedScroll(View child, View target, int axes, int type) {
    return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
  }

  @Override
  public void onNestedScroll(
      View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {

    BottomNavigationView bottomNavigationView =
        ((Activity) getContext()).findViewById(R.id.bottom_nav_bar);

    if (bottomNavigationView != null) {
      if (dyConsumed > 0) bottomNavigationView.setVisibility(View.GONE);
      else if (dyConsumed < 0) bottomNavigationView.setVisibility(View.VISIBLE);
    }
  }
}
