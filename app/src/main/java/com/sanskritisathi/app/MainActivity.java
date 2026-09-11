package com.sanskritisathi.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private RecyclerView homeFeedRecyclerView;

    private static final String PREFS_NAME = "SanskritiSathiPrefs";
    private static final String THEME_KEY = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pre-apply theme mode before layout inflate
        applySavedTheme();

        setContentView(R.layout.activity_main);

        // =========================
        // FIREBASE CONNECTION TEST
        // =========================
        try {
            FirebaseConnectionTest.run(this);
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                FirebaseStorageConnectionTest.run(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // =========================
        // HOME FEED
        // =========================
        homeFeedRecyclerView = findViewById(R.id.homeFeedRecyclerView);
        if (homeFeedRecyclerView != null) {
            homeFeedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            homeFeedRecyclerView.setHasFixedSize(false);

            CulturePostAdapter feedAdapter = new CulturePostAdapter(
                    this,
                    CulturePostData.getAllPosts()
            );
            homeFeedRecyclerView.setAdapter(feedAdapter);
        }

        // =========================
        // STORIES BUTTON LISTENERS
        // =========================
        setOnClick(R.id.templeStoryButton, TempleActivity.class);
        setOnClick(R.id.rajaStoryButton, RajaActivity.class);
        setOnClick(R.id.deviStoryButton, DeviDevtaActivity.class);
        setOnClick(R.id.gitaStoryButton, GitaActivity.class);
        setOnClick(R.id.festivalStoryButton, RssActivity.class);
        setOnClick(R.id.yourStoryButton, StoryUploadActivity.class);

        // =========================
        // TOP CREATE BUTTON
        // =========================
        if (findViewById(R.id.createTopButton) != null) {
            findViewById(R.id.createTopButton).setOnClickListener(v -> showCreateMenu());
        }

        // =========================
        // NOTIFICATIONS
        // =========================
        if (findViewById(R.id.notificationButton) != null) {
            findViewById(R.id.notificationButton).setOnClickListener(v ->
                    Toast.makeText(MainActivity.this, "Notifications feature coming soon!", Toast.LENGTH_SHORT).show()
            );
        }

        // =========================
        // DAY / NIGHT MODE TOGGLE
        // =========================
        TextView themeToggleButton = findViewById(R.id.themeToggleButton);
        if (themeToggleButton != null) {
            updateThemeIcon(themeToggleButton);

            themeToggleButton.setOnClickListener(v -> {
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                boolean currentDarkMode = preferences.getBoolean(THEME_KEY, false);
                boolean newDarkMode = !currentDarkMode;

                preferences.edit().putBoolean(THEME_KEY, newDarkMode).apply();

                // Apply Actual App Theme Switch
                if (newDarkMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }

                updateThemeIcon(themeToggleButton);
                Toast.makeText(MainActivity.this, newDarkMode ? "🌙 Night Mode" : "☀️ Day Mode", Toast.LENGTH_SHORT).show();
            });
        }

        // =========================
        // BOTTOM NAVIGATION LISTENERS
        // =========================
        if (findViewById(R.id.homeNavButton) != null) {
            findViewById(R.id.homeNavButton).setOnClickListener(v -> {
                if (homeFeedRecyclerView != null) {
                    homeFeedRecyclerView.smoothScrollToPosition(0);
                }
            });
        }

        setOnClick(R.id.reelsNavButton, ReelActivity.class);
        setOnClick(R.id.chatNavButton, ChatActivity.class);

        if (findViewById(R.id.searchNavButton) != null) {
            findViewById(R.id.searchNavButton).setOnClickListener(v ->
                    Toast.makeText(MainActivity.this, "Search feature coming soon!", Toast.LENGTH_SHORT).show()
            );
        }

        if (findViewById(R.id.profileNavButton) != null) {
            findViewById(R.id.profileNavButton).setOnClickListener(v -> {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
                }
            });
        }
    }

    // Helper Method to Safely Bind Click Intent
    private void setOnClick(int viewId, Class<?> targetActivity) {
        if (findViewById(viewId) != null) {
            findViewById(viewId).setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, targetActivity))
            );
        }
    }

    // Apply Saved Theme
    private void applySavedTheme() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkMode = preferences.getBoolean(THEME_KEY, false);
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // Update Theme Icon
    private void updateThemeIcon(TextView button) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkMode = preferences.getBoolean(THEME_KEY, false);
        button.setText(darkMode ? "☀️" : "🌙");
    }

    // Create Options Dialog
    private void showCreateMenu() {
        String[] options = {
                "🎬 Create Reel",
                "📸 Create Post",
                "⭕ Create Story"
        };

        new AlertDialog.Builder(this)
                .setTitle("Create")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        startActivity(new Intent(MainActivity.this, ReelUploadActivity.class));
                    } else if (which == 1) {
                        startActivity(new Intent(MainActivity.this, PostUploadActivity.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, StoryUploadActivity.class));
                    }
                })
                .show();
    }
}
