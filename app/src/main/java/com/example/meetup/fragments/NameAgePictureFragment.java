package com.example.meetup.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.meetup.R;
import com.example.meetup.viewmodels.UserViewModel;

public class NameAgePictureFragment extends Fragment {

    private UserViewModel userViewModel;

    public static NameAgePictureFragment newInstance() {
        return new NameAgePictureFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_name_age_picture, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        EditText editName = view.findViewById(R.id.editName);
        EditText editAge  = view.findViewById(R.id.editAge);
        Button nextButton = view.findViewById(R.id.buttonNext);

        nextButton.setOnClickListener(v -> {
            String userName = editName.getText().toString().trim();
            String userAgeStr = editAge.getText().toString().trim();
            int userAge = 0;

            if (userName.isEmpty() || userAgeStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter name and age", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                userAge = Integer.parseInt(userAgeStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid age", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update ViewModel
            userViewModel.setName(userName);
            userViewModel.setAge(userAge);

            // Navigate to InterestsFragment
            if (getActivity() instanceof com.example.meetup.activities.ProfileActivity) {
                ((com.example.meetup.activities.ProfileActivity) getActivity()).showInterestsFragment();
            }
        });
    }
}
