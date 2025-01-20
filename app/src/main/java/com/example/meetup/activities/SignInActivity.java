package com.example.meetup.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.meetup.R;
import com.example.meetup.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText editTextEmail, editTextPassword;
    private Button buttonSignIn, buttonGoToSignUp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonGoToSignUp = findViewById(R.id.buttonGoToSignUp);

        // click listener for sign in button
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignInActivity.this,
                            "Please enter email and password",
                            Toast.LENGTH_SHORT).show();
                } else {
                    signInUser(email, password);
                }
            }
        });

        // click listener for go to sign up button
        buttonGoToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go to SignUpActivity
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Attempts to sign in the user with the provided email and password.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     */
    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkUserProfile(user);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // Wrong password
                                Toast.makeText(SignInActivity.this,
                                        "Invalid password or credentials",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignInActivity.this,
                                        "Sign in failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    /**
     * Checks if the user's profile is complete by fetching data from Firestore.
     *
     * @param user The currently signed-in Firebase user.
     */
    private void checkUserProfile(FirebaseUser user) {
        db.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User userProfile = document.toObject(User.class);
                                if (userProfile != null && isProfileComplete(userProfile)) {
                                    goToHome();
                                } else {
                                    goToProfile();
                                }
                            } else {
                                // Profile does not exist
                                goToProfile();
                            }
                        } else {
                            Log.d("SignInActivity", "Failed to fetch user profile.", task.getException());
                            Toast.makeText(SignInActivity.this,
                                    "Failed to check profile: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            // Sign out the user and redirect to Sign In screen
                            mAuth.signOut();
                        }
                    }
                });
    }

    /**
     * Determines if the user's profile is complete based on predefined criteria.
     *
     * @param user The user object fetched from Firestore.
     * @return True if the profile is complete; false otherwise.
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
        Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
        // Clear the back stack to prevent the user from returning to the SignInActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Navigates the user to the ProfileActivity to complete their profile.
     */
    private void goToProfile() {
        Intent intent = new Intent(SignInActivity.this, ProfileActivity.class);
        // Clear the back stack to prevent the user from returning to the SignInActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}