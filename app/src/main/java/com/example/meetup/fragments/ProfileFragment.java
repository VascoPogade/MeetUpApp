package com.example.meetup.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.meetup.R;
import com.example.meetup.activities.ProfileActivity;
import com.example.meetup.activities.SignInActivity;
import com.example.meetup.models.User;
import com.example.meetup.viewmodels.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserViewModel userViewModel;

    private TextView tvName, tvAge, tvInterests;
    private Button btnEditProfile, btnLogout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Initialize UI elements
        tvName = view.findViewById(R.id.tvProfileName);
        tvAge = view.findViewById(R.id.tvProfileAge);
        tvInterests = view.findViewById(R.id.tvProfileInterests);
        btnEditProfile = view.findViewById(R.id.buttonEditProfile);
        btnLogout = view.findViewById(R.id.buttonLogout);

        // Load user data
        loadUserProfile();

        // Set onClick listeners
        btnEditProfile.setOnClickListener(v -> {
            // Navigate to ProfileActivity to edit profile
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(getContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    /**
     * Loads the user's profile data from Firestore and updates the UI.
     */
    private void loadUserProfile() {
        // Get the current user's ID or null if not authenticated
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        // If user is not authenticated, show a toast and return
        if (uid == null) {
            Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch user profile from Firestore
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            tvName.setText("Name: " + user.getName());
                            tvAge.setText("Age: " + user.getAge());
                            tvInterests.setText("Interests: " + String.join(", ", user.getInterests()));
                            // Update ViewModel if needed
                            userViewModel.setName(user.getName());
                            userViewModel.setAge(user.getAge());
                            userViewModel.setInterests(user.getInterests());
                        } else {
                            Toast.makeText(getContext(), "User data is incomplete.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Error fetching user profile: ", e);
                    Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}