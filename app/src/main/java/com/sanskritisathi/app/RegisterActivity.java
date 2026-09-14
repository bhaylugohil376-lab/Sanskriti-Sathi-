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

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;

    private Button registerButton;
    private TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        bindViews();
        setupListeners();
    }

    private void bindViews() {

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput =
                findViewById(R.id.confirmPasswordInput);

        registerButton =
                findViewById(R.id.registerButton);

        loginText =
                findViewById(R.id.loginText);
    }

    private void setupListeners() {

        if (registerButton != null) {
            registerButton.setOnClickListener(
                    v -> registerUser()
            );
        }

        if (loginText != null) {
            loginText.setOnClickListener(
                    v -> finish()
            );
        }
    }

    private void registerUser() {

        String name = getName();
        String email = getEmail();
        String password = getPassword();
        String confirmPassword = getConfirmPassword();

        if (!validateInput(
                name,
                email,
                password,
                confirmPassword
        )) {
            return;
        }

        setRegisterEnabled(false);

        SupabaseAuthManager.register(
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

                        setRegisterEnabled(true);

                        Toast.makeText(
                                RegisterActivity.this,
                                "Account created successfully ✅",
                                Toast.LENGTH_LONG
                        ).show();

                        openProfileActivity();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        setRegisterEnabled(true);

                        Toast.makeText(
                                RegisterActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private boolean validateInput(
            String name,
            String email,
            String password,
            String confirmPassword
    ) {

        if (TextUtils.isEmpty(name)) {

            nameInput.setError(
                    "Name डालें"
            );

            nameInput.requestFocus();

            return false;
        }

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

        if (TextUtils.isEmpty(confirmPassword)) {

            confirmPasswordInput.setError(
                    "Password फिर से डालें"
            );

            confirmPasswordInput.requestFocus();

            return false;
        }

        if (!password.equals(confirmPassword)) {

            confirmPasswordInput.setError(
                    "Passwords match नहीं कर रहे"
            );

            confirmPasswordInput.requestFocus();

            return false;
        }

        return true;
    }

    private String getName() {

        if (nameInput == null) {
            return "";
        }

        return nameInput
                .getText()
                .toString()
                .trim();
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

    private String getConfirmPassword() {

        if (confirmPasswordInput == null) {
            return "";
        }

        return confirmPasswordInput
                .getText()
                .toString()
                .trim();
    }

    private void setRegisterEnabled(
            boolean enabled
    ) {

        if (registerButton != null) {
            registerButton.setEnabled(enabled);
        }
    }

    private void openProfileActivity() {

        Intent intent =
                new Intent(
                        RegisterActivity.this,
                        ProfileActivity.class
                );

        startActivity(intent);
        finish();
    }
}
