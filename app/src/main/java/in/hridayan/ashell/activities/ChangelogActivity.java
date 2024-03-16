package in.hridayan.ashell.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.ChangelogAdapter;
import in.hridayan.ashell.utils.ChangelogItem;
import java.util.ArrayList;
import java.util.List;

public class ChangelogActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_changelog);

    int statusBarColor = getColor(R.color.StatusBar);
    double brightness = Color.luminance(statusBarColor);
    boolean isLightStatusBar = brightness > 0.5;

    View decorView = getWindow().getDecorView();
    if (isLightStatusBar) {
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
    ImageView imageView = findViewById(R.id.arrow_back);

    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    RecyclerView recyclerViewChangelogs = findViewById(R.id.recycler_view_changelogs);

    List<ChangelogItem> changelogItems = new ArrayList<>();

    changelogItems.add(new ChangelogItem("Version 3.4.0", getString(R.string.changelog_v3_4_0)));

    changelogItems.add(new ChangelogItem("Version 3.3.0", getString(R.string.changelog_v3_3_0)));

    changelogItems.add(new ChangelogItem("Version 3.2.0", getString(R.string.changelog_v3_2_0)));

    changelogItems.add(new ChangelogItem("Version 3.1.0", getString(R.string.changelog_v3_1_0)));

    changelogItems.add(new ChangelogItem("Version 3.0.0", getString(R.string.changelog_v3_0_0)));

    changelogItems.add(new ChangelogItem("Version 2.0.2", getString(R.string.changelog_v2_0_2)));

    changelogItems.add(new ChangelogItem("Version 2.0.1", getString(R.string.changelog_v2_0_1)));

    changelogItems.add(new ChangelogItem("Version 2.0.0", getString(R.string.changelog_v2_0_0)));

    changelogItems.add(new ChangelogItem("Version 1.3.0", getString(R.string.changelog_v1_3_0)));

    changelogItems.add(new ChangelogItem("Version 1.2.0", getString(R.string.changelog_v1_2_0)));

    changelogItems.add(new ChangelogItem("Version 1.1.1", getString(R.string.changelog_v1_1_1)));

    changelogItems.add(new ChangelogItem("Version 1.1.0", getString(R.string.changelog_v1_1_0)));
    changelogItems.add(new ChangelogItem("Version 1.0.0", getString(R.string.changelog_v1_0_0)));

    changelogItems.add(new ChangelogItem("Version 0.9.1", getString(R.string.changelog_v0_9_1)));

    changelogItems.add(new ChangelogItem("Version 0.9.0", getString(R.string.changelog_v0_9_0)));

    ChangelogAdapter adapter = new ChangelogAdapter(changelogItems, this);
    recyclerViewChangelogs.setAdapter(adapter);
    recyclerViewChangelogs.setLayoutManager(new LinearLayoutManager(this));
  }
}
