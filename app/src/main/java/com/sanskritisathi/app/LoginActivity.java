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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;

    private Button loginButton;
    private Button registerButton;
    private Button googleSignInButton;

    private TextView registerTabText;
    private TextView forgotPasswordText;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        bindViews();
        setupListeners();
    }

    private void bindViews() {

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        registerTabText = findViewById(R.id.registerTabText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
    }

    private void setupListeners() {

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> loginUser());
        }

        if (registerButton != null) {
            registerButton.setOnClickListener(v -> registerUser());
        }

        if (registerTabText != null) {
            registerTabText.setOnClickListener(v -> registerUser());
        }

        if (forgotPasswordText != null) {
            forgotPasswordText.setOnClickListener(v -> resetPassword());
        }

        /*
         * Google Sign-In abhi actual OAuth integration nahi hai.
         * Isliye fake "integration ready" message nahi dikhayenge.
         */
        if (googleSignInButton != null) {
            googleSignInButton.setOnClickListener(v ->
                    Toast.makeText(
                            LoginActivity.this,
                            "Google Sign-In abhi configure nahi hai.",
                            Toast.LENGTH_SHORT
                    ).show()
            );
        }
    }

    private void loginUser() {

        String email = getEmail();
        String password = getPassword();

        if (!validateInput(email, password)) {
            return;
        }

        setLoginEnabled(false);

        firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    setLoginEnabled(true);

                    if (task.isSuccessful()) {

                        FirebaseUser user =
                                firebaseAuth.getCurrentUser();

                        if (user != null) {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login successful ✅",
                                    Toast.LENGTH_SHORT
                            ).show();

                            openMainActivity();

                        } else {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login successful, user data unavailable.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    } else {

                        String message =
                                getFirebaseErrorMessage(task.getException());

                        Toast.makeText(
                                LoginActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void registerUser() {

        String email = getEmail();
        String password = getPassword();

        if (!validateInput(email, password)) {
            return;
        }

        setRegisterEnabled(false);

        firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    setRegisterEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Account created successfully ✅",
                                Toast.LENGTH_SHORT
                        ).show();

                        openProfileActivity();

                    } else {

                        String message =
                                getFirebaseErrorMessage(task.getException());

                        Toast.makeText(
                                LoginActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void resetPassword() {

        String email = getEmail();

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

        if (forgotPasswordText != null) {
            forgotPasswordText.setEnabled(false);
        }

        firebaseAuth
                .sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {

                    if (forgotPasswordText != null) {
                        forgotPasswordText.setEnabled(true);
                    }

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Password reset email भेज दिया गया 📧",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        String message =
                                getFirebaseErrorMessage(task.getException());

                        Toast.makeText(
                                LoginActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private boolean validateInput(
            String email,
            String password) {

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

            passwordInput.setError(
                    "Password कम से कम 6 characters का होना चाहिए"
            );

            passwordInput.requestFocus();
            return false;
        }

        return true;
    }

    private String getEmail() {

        if (emailInput == null) {
            return "";
        }

        return emailInput
                .getText()
                .toString()
                .trim();
    }

    private String getPassword() {

        if (passwordInput == null) {
            return "";
        }

        return passwordInput
                .getText()
                .toString()
                .trim();
    }

    private void setLoginEnabled(boolean enabled) {

        if (loginButton != null) {
            loginButton.setEnabled(enabled);
        }
    }

    private void setRegisterEnabled(boolean enabled) {

        if (registerButton != null) {
            registerButton.setEnabled(enabled);
        }
    }

    private String getFirebaseErrorMessage(Exception exception) {

        if (exception == null) {
            return "Operation failed. Please try again.";
        }

        String error =
                exception.getMessage();

        if (error == null) {
            return "Operation failed. Please try again.";
        }

        String lowerError =
                error.toLowerCase();

        if (lowerError.contains("invalid credential")
                || lowerError.contains("invalid-credential")) {

            return "Email ya password galat hai.";
        }

        if (lowerError.contains("password is invalid")
                || lowerError.contains("wrong-password")) {

            return "Password galat hai.";
        }

        if (lowerError.contains("user-not-found")
                || lowerError.contains("no user record")) {

            return "Is email se account nahi mila.";
        }

        if (lowerError.contains("email-already-in-use")) {

            return "Is email se account pehle se bana hua hai.";
        }

        if (lowerError.contains("weak-password")) {

            return "Password kam se kam 6 characters ka hona chahiye.";
        }

        if (lowerError.contains("invalid-email")) {

            return "Email address valid nahi hai.";
        }

        if (lowerError.contains("network")) {

            return "Internet connection check karein.";
        }

        if (lowerError.contains("too-many-requests")) {

            return "Bahut zyada attempts ho gaye. Thodi der baad try karein.";
        }

        return "Login/Register failed. Please try again.";
    }

    private void openMainActivity() {

        Intent intent =
                new Intent(
                        LoginActivity.this,
                        MainActivity.class
                );

        startActivity(intent);
        finish();
    }

    private void openProfileActivity() {

        Intent intent =
                new Intent(
                        LoginActivity.this,
                        ProfileActivity.class
                );

        startActivity(intent);
        finish();
    }
}
