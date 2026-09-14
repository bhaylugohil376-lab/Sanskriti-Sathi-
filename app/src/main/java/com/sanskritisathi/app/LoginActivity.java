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

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;

    private Button loginButton;
    private Button googleSignInButton;

    private TextView registerTabText;
    private TextView forgotPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bindViews();
        setupListeners();
    }

    private void bindViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        registerTabText = findViewById(R.id.registerTabText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
    }

    private void setupListeners() {

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> loginUser());
        }

        if (forgotPasswordText != null) {
            forgotPasswordText.setOnClickListener(v -> {
                startActivity(new Intent(
                        LoginActivity.this,
                        ForgotPasswordActivity.class
                ));
            });
        }

        if (registerTabText != null) {
            registerTabText.setOnClickListener(v -> {
                startActivity(new Intent(
                        LoginActivity.this,
                        RegisterActivity.class
                ));
            });
        }

        if (googleSignInButton != null) {
            googleSignInButton.setOnClickListener(v -> startGoogleLogin());
        }
    }

    private void loginUser() {

        String email = getEmail();
        String password = getPassword();

        if (!validateInput(email, password)) {
            return;
        }

        setLoginEnabled(false);

        SupabaseAuthManager.login(
                this,
                email,
                password,
                new SupabaseAuthManager.AuthCallback() {

                    @Override
                    public void onSuccess(
                            String accessToken,
                            String refreshToken,
                            String userId,
                            String userEmail
                    ) {
                        setLoginEnabled(true);

                        Toast.makeText(
                                LoginActivity.this,
                                "Login successful ✅",
                                Toast.LENGTH_SHORT
                        ).show();

                        openMainActivity();
                    }

                    @Override
                    public void onError(String message) {
                        setLoginEnabled(true);

                        Toast.makeText(
                                LoginActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void startGoogleLogin() {

        try {
            Intent intent = new Intent(
                    LoginActivity.this,
                    GoogleAuthActivity.class
            );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Google Sign-In start nahi ho saka.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private boolean validateInput(
            String email,
            String password
    ) {

        if (TextUtils.isEmpty(email)) {

            emailInput.setError("Email डालें");
            emailInput.requestFocus();

            return false;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

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

        if (googleSignInButton != null) {
            googleSignInButton.setEnabled(enabled);
        }
    }

    private void openMainActivity() {

        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}
