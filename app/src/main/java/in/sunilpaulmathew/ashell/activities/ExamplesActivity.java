package in.sunilpaulmathew.ashell.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import in.sunilpaulmathew.ashell.R;
import in.sunilpaulmathew.ashell.adapters.ExamplesAdapter;
import in.sunilpaulmathew.ashell.utils.Commands;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 05, 2022
 */
public class ExamplesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examples);

        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, getResources().getConfiguration()
                .orientation == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        ExamplesAdapter mRecycleViewAdapter = new ExamplesAdapter(Commands.commandList());
        mRecyclerView.setAdapter(mRecycleViewAdapter);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

}