package com.example.meetup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.meetup.R;
import com.example.meetup.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class InterestsFragment extends Fragment {

    private UserViewModel userViewModel;

    public static InterestsFragment newInstance() {
        return new InterestsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_interests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // CheckBoxes for each interest
        CheckBox[] checkBoxes = new CheckBox[]{
                view.findViewById(R.id.checkbox_fiction),
                view.findViewById(R.id.checkbox_mystery),
                view.findViewById(R.id.checkbox_fantasy),
                view.findViewById(R.id.checkbox_biographies),
                view.findViewById(R.id.checkbox_scifi),

                view.findViewById(R.id.checkbox_rock),
                view.findViewById(R.id.checkbox_jazz),
                view.findViewById(R.id.checkbox_classical),
                view.findViewById(R.id.checkbox_pop),
                view.findViewById(R.id.checkbox_electronic),

                view.findViewById(R.id.checkbox_action),
                view.findViewById(R.id.checkbox_comedy),
                view.findViewById(R.id.checkbox_drama),
                view.findViewById(R.id.checkbox_documentaries),
                view.findViewById(R.id.checkbox_anime),

                view.findViewById(R.id.checkbox_yoga),
                view.findViewById(R.id.checkbox_running),
                view.findViewById(R.id.checkbox_soccer),
                view.findViewById(R.id.checkbox_swimming),
                view.findViewById(R.id.checkbox_tennis),

                view.findViewById(R.id.checkbox_vegan),
                view.findViewById(R.id.checkbox_streetfood),
                view.findViewById(R.id.checkbox_baking),
                view.findViewById(R.id.checkbox_wine),
                view.findViewById(R.id.checkbox_coffee),

                view.findViewById(R.id.checkbox_backpacking),
                view.findViewById(R.id.checkbox_roadtrips),
                view.findViewById(R.id.checkbox_camping),
                view.findViewById(R.id.checkbox_culture),
                view.findViewById(R.id.checkbox_hiking),

                view.findViewById(R.id.checkbox_painting),
                view.findViewById(R.id.checkbox_photography),
                view.findViewById(R.id.checkbox_pottery),
                view.findViewById(R.id.checkbox_diy),
                view.findViewById(R.id.checkbox_crochet),

                view.findViewById(R.id.checkbox_pc),
                view.findViewById(R.id.checkbox_console),
                view.findViewById(R.id.checkbox_mobile),
                view.findViewById(R.id.checkbox_boardgames),
                view.findViewById(R.id.checkbox_esports),

                view.findViewById(R.id.checkbox_meditation),
                view.findViewById(R.id.checkbox_mentalhealth),
                view.findViewById(R.id.checkbox_spirituality),
                view.findViewById(R.id.checkbox_minimalism),
                view.findViewById(R.id.checkbox_healthyeating),

                view.findViewById(R.id.checkbox_startups),
                view.findViewById(R.id.checkbox_networking),
                view.findViewById(R.id.checkbox_publicspeaking),
                view.findViewById(R.id.checkbox_coding),
                view.findViewById(R.id.checkbox_marketing)
        };

        Button finishButton = view.findViewById(R.id.buttonFinish);

        finishButton.setOnClickListener(v -> {
            List<String> selectedInterests = new ArrayList<>();

            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isChecked()) {
                    selectedInterests.add(checkBox.getText().toString());
                }
            }

            if (selectedInterests.size() < 8) {
                Toast.makeText(getContext(), "Please select at least 8 interests", Toast.LENGTH_SHORT).show();
                return;
            }

            userViewModel.setInterests(selectedInterests);
            Toast.makeText(getContext(), "Interests saved successfully!", Toast.LENGTH_SHORT).show();

            if (getActivity() instanceof com.example.meetup.activities.ProfileActivity) {
                ((com.example.meetup.activities.ProfileActivity) getActivity()).checkAndSaveProfile();
            }

        });
    }
}