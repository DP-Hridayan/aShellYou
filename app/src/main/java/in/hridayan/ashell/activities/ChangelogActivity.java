package in.hridayan.ashell.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.ChangelogAdapter;
import in.hridayan.ashell.models.ChangelogItem;

public class ChangelogActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_changelog);

    RecyclerView recyclerViewChangelogs = findViewById(R.id.recycler_view_changelogs);

  
    List<ChangelogItem> changelogItems = new ArrayList<>();
    changelogItems.add(
        new ChangelogItem(
            "Version 1.2.0 : \n\n    • Added Settings panel.\n\n    • Added option to disable smooth scroll in shell output.\n\n    • Fixed major bugs :\n    ~ Crash while changing device theme.\n    ~ Crash while trying to save large shell output . eg : output of 'pm' command."));
       changelogItems.add(
        new ChangelogItem(
            "Version 1.1.1 : \n\n    • Replaced some deprecated api.\n\n    • Minor UI changes."));
        
        changelogItems.add(
        new ChangelogItem(
            "Version 1.1.0 : \n\n    • Added Scroll To Top and Scroll To Bottom buttons.\n\n    • Added Click keyboard Enter key to Send command.\n\n    • Added some commands.\n\n    • Updated dependencies to include latest material you library.\n\n    • Minor UI changes."));
        changelogItems.add(
        new ChangelogItem(
            "Version 1.0.0 : \n\n    • Added some more command examples.\n\n    • Minor UI changes.\n\n    • Changed package name to avoid version confusion with original app."));
            changelogItems.add(
        new ChangelogItem(
            "Version 0.9.1 : \n\n    • Added changelogs into the app.\n\n    • Added highlighted shell output.\n\n    • Minor UI changes."));
    changelogItems.add(
        new ChangelogItem(
            "Version 0.9 : \n\n    • Added dynamic material themeing.\n\n    • Revamped whole UI to give a fresh look."));

   

    ChangelogAdapter adapter = new ChangelogAdapter(changelogItems);
    recyclerViewChangelogs.setAdapter(adapter);

    recyclerViewChangelogs.setLayoutManager(new LinearLayoutManager(this));
  }
}
