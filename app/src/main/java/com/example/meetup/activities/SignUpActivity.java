package com.example.meetup.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.meetup.R;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText editTextEmail, editTextPassword;
    private Button buttonSignUp, buttonGoToSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        buttonGoToSignIn = findViewById(R.id.buttonGoToSignIn);

        // Set click listeners
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignUpActivity.this,
                            "Please enter email and password",
                            Toast.LENGTH_SHORT).show();
                } else {
                    createAccount(email, password);
                }
            }
        });

        buttonGoToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go back to SignInActivity
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Creates a new account with the given email and password.
     *
     * @param email The email address to use for the account.
     * @param password The password to use for the account.
     */
    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success
                        Toast.makeText(SignUpActivity.this,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT).show();

                        // Go to ProfileActivity so user can create their profile
                        Intent intent = new Intent(SignUpActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // If sign up fails, displays a message to the user
                        Toast.makeText(SignUpActivity.this,
                                "Failed to create account: "
                                        + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}