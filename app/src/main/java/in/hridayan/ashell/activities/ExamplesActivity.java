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
import com.google.android.material.search.SearchView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.ExamplesViewModel;
import in.hridayan.ashell.adapters.CommandsSearchAdapter;
import in.hridayan.ashell.adapters.ExamplesAdapter;
import in.hridayan.ashell.utils.Commands;
import in.hridayan.ashell.utils.ThemeUtils;
import in.hridayan.ashell.utils.Utils;

public class ExamplesActivity extends AppCompatActivity {
  private ExamplesViewModel viewModel;
  private AppBarLayout appBarLayout;
  private SearchView mSearchView;

  private RecyclerView mRecyclerView, mSearchRecyclerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);

    ThemeUtils.updateTheme(this);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_examples);

    appBarLayout = findViewById(R.id.appBarLayout);
    mSearchView = findViewById(R.id.search_view);
    mSearchRecyclerView = findViewById(R.id.search_recycler_view);

    viewModel = new ViewModelProvider(this).get(ExamplesViewModel.class);

    ImageView imageView = findViewById(R.id.arrow_back);

    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    mRecyclerView = findViewById(R.id.recycler_view);
    mSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    GridLayoutManager mLayoutManager =
        new GridLayoutManager(
            this,
            getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                ? 2
                : 1);
    mRecyclerView.setLayoutManager(mLayoutManager);
    ExamplesAdapter mRecycleViewAdapter = new ExamplesAdapter(Commands.commandList(), this);
    CommandsSearchAdapter mCommandsSearchAdapter =
        new CommandsSearchAdapter(Commands.commandList(), this);
    mSearchRecyclerView.setAdapter(mCommandsSearchAdapter);
    mSearchRecyclerView.setVisibility(View.VISIBLE);
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
