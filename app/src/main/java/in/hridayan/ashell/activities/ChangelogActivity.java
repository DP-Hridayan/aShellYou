package in.hridayan.ashell.activities;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.AppBarLayout;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.ChangelogViewModel;
import in.hridayan.ashell.adapters.ChangelogAdapter;
import in.hridayan.ashell.utils.ChangelogItem;
import in.hridayan.ashell.utils.ThemeUtils;
import in.hridayan.ashell.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class ChangelogActivity extends AppCompatActivity {
  private ChangelogViewModel viewModel;
  private AppBarLayout appBarLayout;
  private RecyclerView recyclerViewChangelogs;

  private final String[] versionNumbers = {
    "v4.2.0", "v4.1.0", "v4.0.3", "v4.0.2", "v4.0.1", "v4.0.0", "v3.9.1", "v3.9.0", "v3.8.2",
    "v3.8.1", "v3.8.0", "v3.7.0", "v3.6.0", "v3.5.1", "v3.5.0", "v3.4.0", "v3.3.0", "v3.2.0",
    "v3.1.0", "v3.0.0", "v2.0.2", "v2.0.1", "v2.0.0", "v1.3.0", "v1.2.0", "v1.1.1", "v1.1.0",
    "v1.0.0", "v0.9.1", "v0.9.0"
  };

  @Override
  protected void onPause() {
    super.onPause();
    viewModel.setToolbarExpanded(Utils.isToolbarExpanded(appBarLayout));
  }

  @Override
  protected void onResume() {
    super.onResume();
    int position = Utils.recyclerViewPosition(recyclerViewChangelogs);

    if (viewModel.isToolbarExpanded()) {
      if (position == 0) {
        Utils.expandToolbar(appBarLayout);
      }
    } else {
      Utils.collapseToolbar(appBarLayout);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    ThemeUtils.updateTheme(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_changelog);

    appBarLayout = findViewById(R.id.appBarLayout);

    viewModel = new ViewModelProvider(this).get(ChangelogViewModel.class);

    ImageView imageView = findViewById(R.id.arrow_back);
    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    recyclerViewChangelogs = findViewById(R.id.recycler_view_changelogs);

    List<ChangelogItem> changelogItems = new ArrayList<>();

    for (String versionNumber : versionNumbers) {
      changelogItems.add(
          new ChangelogItem(
              getString(R.string.version) + "\t\t" + versionNumber,
              Utils.loadChangelogText(versionNumber, this)));
    }

    ChangelogAdapter adapter = new ChangelogAdapter(changelogItems, this);
    recyclerViewChangelogs.setAdapter(adapter);
    recyclerViewChangelogs.setLayoutManager(new LinearLayoutManager(this));
  }
}
