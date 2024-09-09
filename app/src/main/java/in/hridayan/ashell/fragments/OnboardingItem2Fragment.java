package in.hridayan.ashell.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import in.hridayan.ashell.databinding.FragmentOnboardingItem2Binding;

public class OnboardingItem2Fragment extends Fragment {

    private FragmentOnboardingItem2Binding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboardingItem2Binding.inflate(inflater, container, false);

        return binding.getRoot();
    }
}