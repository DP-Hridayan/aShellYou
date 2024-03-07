package in.hridayan.ashell.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.AboutAdapter;
import in.hridayan.ashell.utils.AboutItem;
import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);

    RecyclerView recyclerViewAbout = findViewById(R.id.about_list);

    List<AboutItem> aboutItemList = new ArrayList<>();

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

    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      String version = pInfo.versionName;

      aboutItemList.add(new AboutItem(R.drawable.ic_version_tag, "Version", version));
    } catch (PackageManager.NameNotFoundException ignored) {
      // Handle the absence of package name if needed
    }

    aboutItemList.add(
        new AboutItem(
            R.drawable.ic_credits,
            "Credits",
            "\nRikka apps : Shizuku\n\nsunilpaulmathew : aShell app's original creator!\n\nKhun Htetz Naing : ADB OTG creator\n\nwuxudong : creator of Flashbot"));

    aboutItemList.add(new AboutItem(R.drawable.ic_developer, "Current Developer", "Hridayan"));
    aboutItemList.add(
        new AboutItem(
            R.drawable.ic_report,
            "Report an issue",
            "Report any problem you have faced using the app"));
    aboutItemList.add(
        new AboutItem(
            R.drawable.ic_feature,
            "Feature request",
            "If you have any ideas in your mind, let me know !"));
    aboutItemList.add(
        new AboutItem(R.drawable.ic_github, "Github", "Open github repository for aShell app"));

    AboutAdapter adapter = new AboutAdapter(aboutItemList, this);
    recyclerViewAbout.setAdapter(adapter);

    recyclerViewAbout.setLayoutManager(new LinearLayoutManager(this));
  }
}
