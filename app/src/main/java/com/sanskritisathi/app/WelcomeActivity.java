package com.sanskritisathi.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SanskritiSathiPrefs";
    private static final String LANGUAGE_KEY = "selected_language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Agar user already login hai
        if (user != null) {
            openProfileOrHome(user);
            return;
        }

        SharedPreferences prefs =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        String savedLanguage =
                prefs.getString(LANGUAGE_KEY, "");

        // Language pehle select ho chuki hai
        if (!savedLanguage.isEmpty()) {
            openLogin();
            return;
        }

        setContentView(R.layout.activity_welcome);

        Button hindiButton = findViewById(R.id.hindiButton);
        Button englishButton = findViewById(R.id.englishButton);
        Button gujaratiButton = findViewById(R.id.gujaratiButton);

        hindiButton.setOnClickListener(v -> selectLanguage("hi"));
        englishButton.setOnClickListener(v -> selectLanguage("en"));
        gujaratiButton.setOnClickListener(v -> selectLanguage("gu"));
    }

    private void selectLanguage(String language) {

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(LANGUAGE_KEY, language)
                .apply();

        openLogin();
    }

    private void openLogin() {
        startActivity(
                new Intent(
                        WelcomeActivity.this,
                        LoginActivity.class
                )
        );
        finish();
    }

    private void openProfileOrHome(FirebaseUser user) {

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {

                    String name = snapshot.getString("name");
                    String username = snapshot.getString("username");

                    if (snapshot.exists()
                            && name != null
                            && !name.trim().isEmpty()
                            && username != null
                            && !username.trim().isEmpty()) {

                        startActivity(
                                new Intent(
                                        WelcomeActivity.this,
                                        MainActivity.class
                                )
                        );

                    } else {

                        startActivity(
                                new Intent(
                                        WelcomeActivity.this,
                                        ProfileActivity.class
                                )
                        );
                    }

                    finish();
                })
                .addOnFailureListener(e -> {

                    startActivity(
                            new Intent(
                                    WelcomeActivity.this,
                                    ProfileActivity.class
                            )
                    );

                    finish();
                });
    }
}
