package com.example.meetup.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.example.meetup.R;
import com.example.meetup.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //loading symbol

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is signed in, check profile completeness
            checkUserProfile(currentUser);
        } else {
            // User is not signed in, navigate to Sign In screen
            goToSignIn();
        }
    }

    /**
     * Checks if the user's profile is complete.
     *
     * @param user The user object to check.
     */
    private void checkUserProfile(FirebaseUser user) {
        db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User userProfile = document.toObject(User.class);
                            if (userProfile != null && isProfileComplete(userProfile)) {
                                goToHome();
                            } else {
                                // Profile exists but is incomplete
                                goToProfile();
                            }
                        } else {
                            // Profile does not exist
                            goToProfile();
                        }
                    } else {
                        Log.d("MainActivity", "Failed to fetch user profile.", task.getException());
                        // Redirect to Sign In screen if profile check fails
                        goToSignIn();
                    }
                });
    }

    /**
     * Checks if the user's profile is complete.
     *
     * @param user The user object to check.
     * @return True if the user's profile is complete, false otherwise.
     */
    private boolean isProfileComplete(User user) {
        return user.getName() != null && !user.getName().isEmpty()
                && user.getAge() > 0
                && user.getInterests() != null && !user.getInterests().isEmpty()
                && user.getInterests().size() >= 8;
    }

    /**
     * Navigates the user to the HomeActivity.
     */
    private void goToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
    * Navigates the user to the SignInActivity.
    */
    private void goToSignIn() {
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Navigates the user to the ProfileActivity.
     */
    private void goToProfile() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }
}