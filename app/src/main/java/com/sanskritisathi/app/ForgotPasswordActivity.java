package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button resetButton;
    private TextView backToLoginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);

        bindViews();
        setupListeners();
    }

    private void bindViews() {
        emailInput = findViewById(R.id.emailInput);
        resetButton = findViewById(R.id.resetButton);
        backToLoginText = findViewById(R.id.backToLoginText);
    }

    private void setupListeners() {

        if (resetButton != null) {
            resetButton.setOnClickListener(v -> resetPassword());
        }

        if (backToLoginText != null) {
            backToLoginText.setOnClickListener(v -> finish());
        }
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

        setResetEnabled(false);

        SupabaseAuthManager.resetPassword(
                this,
                email,
                new SupabaseAuthManager.AuthCallback() {

                    @Override
                    public void onSuccess(
                            String accessToken,
                            String refreshToken,
                            String userId,
                            String userEmail) {

                        setResetEnabled(true);

                        Toast.makeText(
                                ForgotPasswordActivity.this,
                                "Password reset email sent ✅",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onError(String message) {

                        setResetEnabled(true);

                        Toast.makeText(
                                ForgotPasswordActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
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

    private void setResetEnabled(boolean enabled) {

        if (resetButton != null) {
            resetButton.setEnabled(enabled);
        }
    }
}
