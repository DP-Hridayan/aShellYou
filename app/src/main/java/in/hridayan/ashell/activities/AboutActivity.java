package in.hridayan.ashell.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

    ImageView imageView = findViewById(R.id.arrow_back);

    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    items.add(new Category("Lead developer"));
    items.add(
        new Category.CategoryAItem(
            "Hridayan", "Just a guy who loves apps and design", R.mipmap.dp_hridayan));

    items.add(new Category("Contributors"));
    items.add(
        new Category.CategoryBItem(
            "RikkaApps",
            "Developer of Shizuku\nMain functionality provider of aShell You",
            R.mipmap.dp_shizuku));

    items.add(
        new Category.CategoryBItem(
            "Sunilpaulmathew",
            "Creator of aShell\nWithout him aShell You wouldn't exist!!'",
            R.drawable.ic_github));

    items.add(
        new Category.CategoryBItem(
            "Khun Htetz Naing",
            "Developer of ADB OTG\nThanks to him OTG feature could be added in aShell You",
            R.mipmap.dp_adb_otg));

    items.add(
        new Category.CategoryBItem(
            "Krishna",
            "He is the website developer , tester , channel manager and a lot more\n\nThanks for your support buddy 🤗",
            R.mipmap.dp_krishna));

    items.add(new Category("App"));
    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      String version = pInfo.versionName;

      items.add(new Category.CategoryCItem("Version", version, R.drawable.ic_version_tag));
    } catch (PackageManager.NameNotFoundException ignored) {}
    items.add(
        new Category.CategoryCItem(
            "Report an issue",
            "Report any issues you have faced during using the app",
            R.drawable.ic_report));

    items.add(
        new Category.CategoryCItem(
            "Feature request",
            "If you have any ideas on your mind , let me know",
            R.drawable.ic_feature));

    items.add(
        new Category.CategoryCItem(
            "Github", "Visit the github repository for aShell You", R.drawable.ic_github));

    items.add(
        new Category.CategoryCItem(
            "Telegram channel",
            "Join the telegram channel for discussion",
            R.drawable.ic_telegram));

    adapter = new AboutAdapter(items, this);
    recyclerView.setAdapter(adapter);
  }
}
