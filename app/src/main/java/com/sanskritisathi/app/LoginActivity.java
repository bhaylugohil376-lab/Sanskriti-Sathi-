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

    private EditText emailInput, passwordInput;
    private Button loginButton, registerButton, googleSignInButton;
    private TextView registerTabText, forgotPasswordText;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        // Bind Views Safely
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        registerTabText = findViewById(R.id.registerTabText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        // Click Listeners
        if (loginButton != null) loginButton.setOnClickListener(v -> loginUser());
        if (registerButton != null) registerButton.setOnClickListener(v -> registerUser());
        if (registerTabText != null) registerTabText.setOnClickListener(v -> registerUser());
        if (forgotPasswordText != null) forgotPasswordText.setOnClickListener(v -> resetPassword());

        // Google Sign-In Action
        if (googleSignInButton != null) {
            googleSignInButton.setOnClickListener(v -> 
                Toast.makeText(LoginActivity.this, "Google Sign-In integration ready", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInput(email, password)) return;

        loginButton.setEnabled(false);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login successful ✅", Toast.LENGTH_SHORT).show();
                        openMainActivity();
                    } else {
                        String message = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInput(email, password)) return;

        if (registerButton != null) registerButton.setEnabled(false);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (registerButton != null) registerButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Account created successfully ✅", Toast.LENGTH_SHORT).show();
                        openProfileActivity();
                    } else {
                        String message = task.getException() != null ? task.getException().getMessage() : "Account creation failed";
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resetPassword() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email डालें");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Valid email डालें");
            emailInput.requestFocus();
            return;
        }

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset email भेज दिया गया 📧", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Password reset failed", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email डालें");
            emailInput.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Valid email डालें");
            emailInput.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password डालें");
            passwordInput.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password कम से कम 6 characters का होना चाहिए");
            passwordInput.requestFocus();
            return false;
        }

        return true;
    }

    private void openMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void openProfileActivity() {
        startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
        finish();
    }
}
