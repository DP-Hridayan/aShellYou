package in.hridayan.ashell.activities;

import android.content.res.Resources;
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

  private final String[] versionNumbers = {"4.0.3",
    "4.0.2", "4.0.1", "4.0.0", "3.9.1", "3.9.0", "3.8.2", "3.8.1", "3.8.0", "3.7.0", "3.6.0",
    "3.5.1", "3.5.0", "3.4.0", "3.3.0", "3.2.0", "3.1.0", "3.0.0", "2.0.2", "2.0.1", "2.0.0",
    "1.3.0", "1.2.0", "1.1.1", "1.1.0", "1.0.0", "0.9.1", "0.9.0"
  };

  private Resources resources;

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

    resources = getResources();

    ImageView imageView = findViewById(R.id.arrow_back);
    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    recyclerViewChangelogs = findViewById(R.id.recycler_view_changelogs);

    List<ChangelogItem> changelogItems = new ArrayList<>();

    for (String versionNumber : versionNumbers) {
      changelogItems.add(
          new ChangelogItem(
              getString(R.string.version) + "\t\t" + versionNumber,
              loadChangelogText(versionNumber)));
    }

    ChangelogAdapter adapter = new ChangelogAdapter(changelogItems, this);
    recyclerViewChangelogs.setAdapter(adapter);
    recyclerViewChangelogs.setLayoutManager(new LinearLayoutManager(this));
  }

  private String loadChangelogText(String versionNumber) {
    int resourceId =
        resources.getIdentifier(
            "changelog_v" + versionNumber.replace(".", "_"), "string", getPackageName());
    return resources.getString(resourceId);
  }
}
