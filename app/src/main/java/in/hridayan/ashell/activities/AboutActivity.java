package in.hridayan.ashell.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import in.hridayan.ashell.UI.AboutViewModel;
import in.hridayan.ashell.UI.Category;
import in.hridayan.ashell.adapters.AboutAdapter;
import in.hridayan.ashell.utils.ThemeUtils;
import in.hridayan.ashell.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends AppCompatActivity {

  private RecyclerView recyclerView;
  private AboutAdapter adapter;
  private List<Object> items;
  private AppBarLayout appBarLayout;
  private AboutViewModel viewModel;
  

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    ThemeUtils.updateTheme(this);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);

    appBarLayout = findViewById(R.id.appBarLayout);

    viewModel = new ViewModelProvider(this).get(AboutViewModel.class);

    recyclerView = findViewById(R.id.about_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    items = new ArrayList<>();

    ImageView imageView = findViewById(R.id.arrow_back);

    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    items.add(new Category(getString(R.string.lead_developer)));
    items.add(
        new Category.LeadDeveloperItem(
            "Hridayan", getString(R.string.hridayan_about), R.mipmap.dp_hridayan));

    items.add(new Category(getString(R.string.contributors)));
    items.add(
        new Category.ContributorsItem(
            "id_rikka", "RikkaApps", getString(R.string.rikka_about), R.mipmap.dp_shizuku));

    items.add(
        new Category.ContributorsItem(
            "id_sunilpaulmathew",
            "Sunilpaulmathew",
            getString(R.string.sunilpaulmathew_about),
            R.mipmap.dp_sunilpaulmathew));

    items.add(
        new Category.ContributorsItem(
            "id_khun_htetz",
            "Khun Htetz Naing",
            getString(R.string.khun_htetz_about),
            R.mipmap.dp_adb_otg));

    items.add(
        new Category.ContributorsItem(
            "id_krishna", "Krishna", getString(R.string.krishna_about), R.mipmap.dp_krishna));

    items.add(
        new Category.ContributorsItem(
            "id_drDisagree",
            "DrDisagree",
            getString(R.string.drDisagree_about),
            R.mipmap.dp_drdisagree));

    items.add(
        new Category.ContributorsItem(
            "id_marciozomb13",
            "marciozomb13",
            getString(R.string.marciozomb13_about),
            R.mipmap.dp_marciozomb13));

    items.add(
        new Category.ContributorsItem(
            "id_weiguangtwk",
            "weiguangtwk",
            getString(R.string.weiguangtwk_about),
            R.mipmap.dp_weiguangtwk));

    items.add(new Category(getString(R.string.app)));
    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      String version = pInfo.versionName;

      items.add(
          new Category.AppItem(
              "id_version", getString(R.string.version), version, R.drawable.ic_version_tag));
    } catch (PackageManager.NameNotFoundException ignored) {
    }

    items.add(
        new Category.AppItem(
            "id_changelogs",
            getString(R.string.changelogs),
            getString(R.string.des_changelogs),
            R.drawable.ic_changelog));

    items.add(
        new Category.AppItem(
            "id_report",
            getString(R.string.report),
            getString(R.string.des_report),
            R.drawable.ic_report));

    items.add(
        new Category.AppItem(
            "id_feature",
            getString(R.string.feature_request),
            getString(R.string.des_feature_request),
            R.drawable.ic_feature));

    items.add(
        new Category.AppItem(
            "id_github",
            getString(R.string.github),
            getString(R.string.des_github),
            R.drawable.ic_github));

    items.add(
        new Category.AppItem(
            "id_telegram",
            getString(R.string.telegram_channel),
            getString(R.string.des_telegram_channel),
            R.drawable.ic_telegram));

    items.add(
        new Category.AppItem(
            "id_discord",
            getString(R.string.discord),
            getString(R.string.des_discord),
            R.drawable.ic_discord));

    adapter = new AboutAdapter(items, this);
    recyclerView.setAdapter(adapter);
  }

  @Override
  protected void onPause() {
    super.onPause();
    viewModel.setToolbarExpanded(Utils.isToolbarExpanded(appBarLayout));
  }

  @Override
  protected void onResume() {
    super.onResume();
    int position = Utils.recyclerViewPosition(recyclerView);

    if (viewModel.isToolbarExpanded()) {
      if (position == 0) {
        Utils.expandToolbar(appBarLayout);
      }
    } else {
      Utils.collapseToolbar(appBarLayout);
    }
  }
}
