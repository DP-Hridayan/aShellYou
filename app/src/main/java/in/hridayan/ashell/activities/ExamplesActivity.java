package in.hridayan.ashell.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.ExamplesViewModel;
import in.hridayan.ashell.adapters.ExamplesAdapter;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.ThemeUtils;
import in.hridayan.ashell.utils.Utils;


public class ExamplesActivity extends AppCompatActivity {
  private ExamplesViewModel viewModel;
  private AppBarLayout appBarLayout;
  private RecyclerView mRecyclerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);

    ThemeUtils.updateTheme(this);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_examples);

    appBarLayout = findViewById(R.id.appBarLayout);

    viewModel = new ViewModelProvider(this).get(ExamplesViewModel.class);

    ImageView imageView = findViewById(R.id.arrow_back);

    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    mRecyclerView = findViewById(R.id.recycler_view);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    GridLayoutManager mLayoutManager =
        new GridLayoutManager(
            this,
            getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                ? 2
                : 1);
    mRecyclerView.setLayoutManager(mLayoutManager);
    ExamplesAdapter mRecycleViewAdapter = new ExamplesAdapter(Commands.commandList());
    mRecyclerView.setAdapter(mRecycleViewAdapter);
    mRecyclerView.setVisibility(View.VISIBLE);
  }

  @Override
  protected void onPause() {
    super.onPause();
    viewModel.setToolbarExpanded(Utils.isToolbarExpanded(appBarLayout));
  }

  @Override
  protected void onResume() {
    super.onResume();
    int position = Utils.recyclerViewPosition(mRecyclerView);

    if (viewModel.isToolbarExpanded()) {
      if (position == 0) {
        Utils.expandToolbar(appBarLayout);
      }
    } else {
      Utils.collapseToolbar(appBarLayout);
    }
  }
}
