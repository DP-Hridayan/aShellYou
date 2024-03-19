package in.hridayan.ashell.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.UI.Category;
import in.hridayan.ashell.adapters.AboutAdapter;
import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends AppCompatActivity {

  private RecyclerView recyclerView;
  private AboutAdapter adapter;
  private List<Object> items;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);

    recyclerView = findViewById(R.id.about_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    items = new ArrayList<>();

    ImageView imageView = findViewById(R.id.arrow_back);

    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    items.add(new Category(getString(R.string.lead_developer)));
    items.add(
        new Category.CategoryAItem(
            "Hridayan", getString(R.string.hridayan_about), R.mipmap.dp_hridayan));

    items.add(new Category(getString(R.string.contributors)));
    items.add(
        new Category.CategoryBItem(
            "id_rikka", "RikkaApps", getString(R.string.rikka_about), R.mipmap.dp_shizuku));

    items.add(
        new Category.CategoryBItem(
            "id_sunilpaulmathew",
            "Sunilpaulmathew",
            getString(R.string.sunilpaulmathew_about),
            R.mipmap.dp_sunilpaulmathew));

    items.add(
        new Category.CategoryBItem(
            "id_khun_htetz",
            "Khun Htetz Naing",
            getString(R.string.khun_htetz_about),
            R.mipmap.dp_adb_otg));

    items.add(
        new Category.CategoryBItem(
            "id_krishna", "Krishna", getString(R.string.krishna_about), R.mipmap.dp_krishna));

    items.add(
        new Category.CategoryBItem(
            "id_drDisagree",
            "DrDisagree",
            getString(R.string.drDisagree_about),
            R.mipmap.dp_drdisagree));

    items.add(
        new Category.CategoryBItem(
            "id_marciozomb13",
            "marciozomb13",
            getString(R.string.marciozomb13_about),
            R.mipmap.dp_marciozomb13));

    items.add(new Category(getString(R.string.app)));
    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      String version = pInfo.versionName;

      items.add(
          new Category.CategoryCItem(
              "id_version", getString(R.string.version), version, R.drawable.ic_version_tag));
    } catch (PackageManager.NameNotFoundException ignored) {
    }
    items.add(
        new Category.CategoryCItem(
            "id_report",
            getString(R.string.report),
            getString(R.string.des_report),
            R.drawable.ic_report));

    items.add(
        new Category.CategoryCItem(
            "id_feature",
            getString(R.string.feature_request),
            getString(R.string.des_feature_request),
            R.drawable.ic_feature));

    items.add(
        new Category.CategoryCItem(
            "id_github",
            getString(R.string.github),
            getString(R.string.des_github),
            R.drawable.ic_github));

    items.add(
        new Category.CategoryCItem(
            "id_telegram",
            getString(R.string.telegram_channel),
            getString(R.string.des_telegram_channel),
            R.drawable.ic_telegram));

    adapter = new AboutAdapter(items, this);
    recyclerView.setAdapter(adapter);
  }
}
