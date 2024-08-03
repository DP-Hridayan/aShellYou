package in.hridayan.ashell.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import in.hridayan.ashell.R;
import in.hridayan.ashell.utils.Preferences;
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
          Preferences.setFirstLaunch(requireContext(), false);
          getParentFragmentManager()
              .beginTransaction()
              .setCustomAnimations(
                      R.anim.fragment_enter,
                      R.anim.fragment_exit,
                      R.anim.fragment_pop_enter,
                      R.anim.fragment_pop_exit
               )
              .replace(R.id.fragment_container, new AshellFragment())
              .commit();
        });
  }
}
