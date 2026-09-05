package com.sanskritisathi.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private Button hindiButton;
    private Button englishButton;
    private Button gujaratiButton;

    private static final String PREFS_NAME = "SanskritiSathiPrefs";
    private static final String LANGUAGE_KEY = "selected_language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Agar language pehle se select hai,
        // to Welcome screen dobara nahi dikhayenge.
        String savedLanguage =
                prefs.getString(LANGUAGE_KEY, "");

        if (!savedLanguage.isEmpty()) {
            openLogin();
            return;
        }

        setContentView(R.layout.activity_welcome);

        hindiButton = findViewById(R.id.hindiButton);
        englishButton = findViewById(R.id.englishButton);
        gujaratiButton = findViewById(R.id.gujaratiButton);

        hindiButton.setOnClickListener(v ->
                selectLanguage("hi")
        );

        englishButton.setOnClickListener(v ->
                selectLanguage("en")
        );

        gujaratiButton.setOnClickListener(v ->
                selectLanguage("gu")
        );
    }

    private void selectLanguage(String language) {

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(LANGUAGE_KEY, language)
                .apply();

        openLogin();
    }

    private void openLogin() {

        Intent intent =
                new Intent(
                        WelcomeActivity.this,
                        LoginActivity.class
                );

        startActivity(intent);
        finish();
    }
}
