package com.sanskritisathi.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SanskritiSathiPrefs";
    private static final String LANGUAGE_KEY = "selected_language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =====================================================
        // SUPABASE SESSION CHECK
        // =====================================================

        if (SupabaseAuthManager.isLoggedIn(this)) {
            openMain();
            return;
        }

        // =====================================================
        // LANGUAGE CHECK
        // =====================================================

        SharedPreferences prefs =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String savedLanguage =
                prefs.getString(LANGUAGE_KEY, "");

        // Language already selected
        if (!savedLanguage.isEmpty()) {
            openLogin();
            return;
        }

        // =====================================================
        // FIRST TIME USER
        // =====================================================

        setContentView(R.layout.activity_welcome);

        Button hindiButton =
                findViewById(R.id.hindiButton);

        Button englishButton =
                findViewById(R.id.englishButton);

        Button gujaratiButton =
                findViewById(R.id.gujaratiButton);

        if (hindiButton != null) {
            hindiButton.setOnClickListener(
                    v -> selectLanguage("hi")
            );
        }

        if (englishButton != null) {
            englishButton.setOnClickListener(
                    v -> selectLanguage("en")
            );
        }

        if (gujaratiButton != null) {
            gujaratiButton.setOnClickListener(
                    v -> selectLanguage("gu")
            );
        }
    }

    // =====================================================
    // LANGUAGE
    // =====================================================

    private void selectLanguage(String language) {

        getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        )
                .edit()
                .putString(LANGUAGE_KEY, language)
                .apply();

        openLogin();
    }

    // =====================================================
    // LOGIN
    // =====================================================

    private void openLogin() {

        Intent intent = new Intent(
                WelcomeActivity.this,
                LoginActivity.class
        );

        startActivity(intent);
        finish();
    }

    // =====================================================
    // MAIN SCREEN
    // =====================================================

    private void openMain() {

        Intent intent = new Intent(
                WelcomeActivity.this,
                MainActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}
