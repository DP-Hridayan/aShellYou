package in.hridayan.ashell.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import in.hridayan.ashell.databinding.FragmentOnboardingItem3Binding;

public class OnboardingItem3Fragment extends Fragment {

    private FragmentOnboardingItem3Binding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnboardingItem3Binding.inflate(inflater, container, false);

        binding.root.setOnClickListener(v -> {
            // save selection in prefs Prefs.putString("working_method", "root")
            binding.shizuku.setSelected(false);
        });

        binding.shizuku.setOnClickListener(v -> {
            // save selection in prefs Prefs.putString("working_method", "shizuku")
            binding.root.setSelected(false);
        });

        return binding.getRoot();
    }
}