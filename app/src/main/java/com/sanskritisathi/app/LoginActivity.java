package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;

    private Button loginButton;
    private Button registerButton;

    private TextView registerTabText;
    private TextView forgotPasswordText;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        registerTabText = findViewById(R.id.registerTabText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        // LOGIN
        loginButton.setOnClickListener(v -> loginUser());

        // CREATE ACCOUNT BUTTON
        registerButton.setOnClickListener(v -> registerUser());

        // CREATE ACCOUNT TAB
        registerTabText.setOnClickListener(v -> registerUser());

        // FORGOT PASSWORD
        forgotPasswordText.setOnClickListener(v -> resetPassword());
    }

    // ==================================================
    // LOGIN
    // ==================================================

    private void loginUser() {

        String email = emailInput.getText()
                .toString()
                .trim();

        String password = passwordInput.getText()
                .toString()
                .trim();

        if (!validateInput(email, password)) {
            return;
        }

        loginButton.setEnabled(false);

        firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Login successful ✅",
                                Toast.LENGTH_SHORT
                        ).show();

                        openMainActivity();

                    } else {

                        String message = "Login failed";

                        if (task.getException() != null &&
                                task.getException().getMessage() != null) {

                            message = task.getException()
                                    .getMessage();
                        }

                        Toast.makeText(
                                LoginActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // ==================================================
    // CREATE ACCOUNT
    // ==================================================

    private void registerUser() {

        String email = emailInput.getText()
                .toString()
                .trim();

        String password = passwordInput.getText()
                .toString()
                .trim();

        if (!validateInput(email, password)) {
            return;
        }

        registerButton.setEnabled(false);

        firebaseAuth
                .createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(this, task -> {

                    registerButton.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Account created successfully ✅",
                                Toast.LENGTH_SHORT
                        ).show();

                        /*
                         * New account ke baad
                         * profile setup screen open hogi.
                         */
                        openProfileActivity();

                    } else {

                        String message =
                                "Account creation failed";

                        if (task.getException() != null &&
                                task.getException().getMessage() != null) {

                            message = task.getException()
                                    .getMessage();
                        }

                        Toast.makeText(
                                LoginActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // ==================================================
    // FORGOT PASSWORD
    // ==================================================

    private void resetPassword() {

        String email = emailInput.getText()
                .toString()
                .trim();

        if (TextUtils.isEmpty(email)) {

            emailInput.setError(
                    "Email डालें"
            );

            emailInput.requestFocus();

            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            emailInput.setError(
                    "Valid email डालें"
            );

            emailInput.requestFocus();

            return;
        }

        firebaseAuth
                .sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Password reset email भेज दिया गया 📧",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        Toast.makeText(
                                LoginActivity.this,
                                "Password reset failed",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // ==================================================
    // VALIDATION
    // ==================================================

    private boolean validateInput(
            String email,
            String password) {

        if (TextUtils.isEmpty(email)) {

            emailInput.setError(
                    "Email डालें"
            );

            emailInput.requestFocus();

            return false;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            emailInput.setError(
                    "Valid email डालें"
            );

            emailInput.requestFocus();

            return false;
        }

        if (TextUtils.isEmpty(password)) {

            passwordInput.setError(
                    "Password डालें"
            );

            passwordInput.requestFocus();

            return false;
        }

        if (password.length() < 6) {

            passwordInput.setError(
                    "Password कम से कम 6 characters का होना चाहिए"
            );

            passwordInput.requestFocus();

            return false;
        }

        return true;
    }

    // ==================================================
    // OPEN MAIN
    // ==================================================

    private void openMainActivity() {

        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );

        startActivity(intent);
        finish();
    }

    // ==================================================
    // OPEN PROFILE
    // ==================================================

    private void openProfileActivity() {

        Intent intent = new Intent(
                LoginActivity.this,
                ProfileActivity.class
        );

        startActivity(intent);
        finish();
    }
}
