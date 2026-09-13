package com.sanskritisathi.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AuthCallbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleCallback(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        handleCallback(intent);
    }

    private void handleCallback(Intent intent) {

        Uri data = intent.getData();

        if (data == null) {
            showError("Google callback nahi mila.");
            return;
        }

        String error =
                data.getQueryParameter("error");

        String errorDescription =
                data.getQueryParameter(
                        "error_description"
                );

        if (error != null) {

            if (errorDescription == null) {
                errorDescription = error;
            }

            showError(errorDescription);
            return;
        }

        String accessToken =
                data.getQueryParameter("access_token");

        String refreshToken =
                data.getQueryParameter("refresh_token");

        /*
         * Supabase implicit OAuth response
         * kabhi URL fragment (#...) mein bhi
         * tokens return karta hai.
         */
        if (accessToken == null) {

            String fragment =
                    data.getEncodedFragment();

            if (fragment != null
                    && !fragment.isEmpty()) {

                Uri fragmentUri =
                        Uri.parse(
                                "https://callback?"
                                        + fragment
                        );

                accessToken =
                        fragmentUri.getQueryParameter(
                                "access_token"
                        );

                refreshToken =
                        fragmentUri.getQueryParameter(
                                "refresh_token"
                        );
            }
        }

        if (accessToken == null
                || accessToken.isEmpty()) {

            showError(
                    "Google login token nahi mila."
            );

            return;
        }

        if (refreshToken == null) {
            refreshToken = "";
        }

        /*
         * OAuth session save.
         */
        SupabaseAuthManager.saveOAuthSession(
                this,
                accessToken,
                refreshToken
        );

        Toast.makeText(
                this,
                "Google Login successful ✅",
                Toast.LENGTH_SHORT
        ).show();

        openMainActivity();
    }

    private void openMainActivity() {

        Intent intent =
                new Intent(
                        AuthCallbackActivity.this,
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

    private void showError(String message) {

        Toast.makeText(
                this,
                "Google Login failed: " + message,
                Toast.LENGTH_LONG
        ).show();

        Intent intent =
                new Intent(
                        AuthCallbackActivity.this,
                        LoginActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        startActivity(intent);

        finish();
    }
}
