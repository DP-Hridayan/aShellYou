package in.hridayan.ashell.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.View;
import android.os.Bundle;
import android.widget.ImageView;
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
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);

    recyclerView = findViewById(R.id.about_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    items = new ArrayList<>();

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
            R.drawable.ic_github));

    items.add(
        new Category.CategoryBItem(
            "id_khun_htetz",
            "Khun Htetz Naing",
            getString(R.string.khun_htetz_about),
            R.mipmap.dp_adb_otg));

    items.add(
        new Category.CategoryBItem(
            "id_krishna", "Krishna", getString(R.string.krishna_about), R.mipmap.dp_krishna));

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
