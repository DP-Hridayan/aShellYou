package in.hridayan.ashell.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.R;
import rikka.shizuku.Shizuku;

public class StartFragment extends Fragment {

  public StartFragment() {}

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_start, container, false);
    initViews(view);
    return view;
  }

  private void initViews(View view) {
    LinearLayoutCompat mStartLayout = view.findViewById(R.id.fragment_start);
    MaterialButton mStartButton = view.findViewById(R.id.start_button);
    MaterialTextView mAboutText = view.findViewById(R.id.about_text);

    if (Shizuku.pingBinder()) {
      Shizuku.requestPermission(0);
    }
    mStartLayout.setVisibility(View.VISIBLE);
    mAboutText.setText(getString(R.string.app_summary));

    mStartButton.setOnClickListener(
        v -> {
          PreferenceManager.getDefaultSharedPreferences(requireContext())
              .edit()
              .putBoolean("firstLaunch", false)
              .apply();
          getParentFragmentManager()
              .beginTransaction()
              .replace(R.id.fragment_container, new aShellFragment())
              .commit();
        });
  }
}
