package in.hridayan.ashell.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import in.hridayan.ashell.R;
import in.hridayan.ashell.fragments.aShellFragment;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 28, 2022
 */
public class aShellActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ashell);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new aShellFragment()).commit();
    }

}