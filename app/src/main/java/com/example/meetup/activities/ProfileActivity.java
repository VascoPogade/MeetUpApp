package com.example.meetup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.meetup.R;
import com.example.meetup.fragments.InterestsFragment;
import com.example.meetup.fragments.NameAgePictureFragment;
import com.example.meetup.models.User;
import com.example.meetup.viewmodels.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private UserViewModel userViewModel;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Only show the NameAgePictureFragment if the activity is created for the first time (e.g. not on rotation)
        if (savedInstanceState == null) {
            showNameAgePictureFragment();
        }
    }

    /**
     * Displays the NameAgePictureFragment for the user to input their name, age, and profile picture.
     */
    public void showNameAgePictureFragment() {
        NameAgePictureFragment fragment = NameAgePictureFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * Displays the InterestsFragment for the user to select their interests.
     */
    public void showInterestsFragment() {
        InterestsFragment fragment = InterestsFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Checks if the profile is complete and saves it if so.
     */
    public void checkAndSaveProfile() {
        if (userViewModel.isProfileComplete()) {
            saveUserProfile();
        }
    }

    /**
     * Saves the user data to Firestore under the "users" collection.
     */
    private void saveUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            String name = userViewModel.getName().getValue();
            Integer age = userViewModel.getAge().getValue();
            List<String> interests = userViewModel.getInterests().getValue();

            if (isProfileComplete(name, age, interests)) {
                User user = new User(uid, name, age, interests);
                db.collection("users").document(uid)
                        .set(user)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("ProfileActivity", "User data saved successfully");
                            Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                            goToHome();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ProfileActivity", "Error saving user data", e);
                            Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Incomplete profile data", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            Log.e("ProfileActivity", "saveUserProfile: Couldn't save profile because User is not authenticated");
            // Redirect to login screen
            Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Determines if the profile data is complete.
     *
     * @param name       The user's name.
     * @param age        The user's age.
     * @param interests  The list of user's interests.
     * @return True if the profile is complete; false otherwise.
     */
    private boolean isProfileComplete(String name, Integer age, List<String> interests) {
        return name != null && !name.isEmpty()
                && age != null && age > 0
                && interests != null && !interests.isEmpty()
                && interests.size() >= 8;
    }

    /**
     * Navigates the user to the HomeActivity after successfully saving the profile.
     */
    private void goToHome() {
        Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
        // Clear the back stack to prevent the user from returning to the ProfileActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}