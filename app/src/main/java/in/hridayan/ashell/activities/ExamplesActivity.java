package in.hridayan.ashell.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.adapters.ExamplesAdapter;
import in.hridayan.ashell.utils.Commands;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 05, 2022
 */
public class ExamplesActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EdgeToEdge.enable(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_examples);

    ImageView imageView = findViewById(R.id.arrow_back);

    OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
    imageView.setOnClickListener(v -> dispatcher.onBackPressed());

    RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    GridLayoutManager mLayoutManager =
        new GridLayoutManager(
            this,
            getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                ? 2
                : 1);
    mRecyclerView.setLayoutManager(mLayoutManager);
    ExamplesAdapter mRecycleViewAdapter = new ExamplesAdapter(Commands.commandList());
    mRecyclerView.setAdapter(mRecycleViewAdapter);
    mRecyclerView.setVisibility(View.VISIBLE);
  }
}
