package com.sanskritisathi.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

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

        // Saved theme
        applySavedTheme();

        // Main Screen
        setContentView(R.layout.activity_main);

        initializeFirebase();
        setupHomeFeed();
        setupStoryButtons();
        setupNotificationButton();
        setupBottomNavigation();
    }

    // =========================================================
    // FIREBASE
    // =========================================================

    private void initializeFirebase() {
        try {
            FirebaseConnectionTest.run(this);

            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() != null) {
                FirebaseStorageConnectionTest.run(this);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // HOME FEED
    // =========================================================

    private void setupHomeFeed() {

        homeFeedRecyclerView =
                findViewById(R.id.homeFeedRecyclerView);

        if (homeFeedRecyclerView == null) {
            return;
        }

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        homeFeedRecyclerView.setLayoutManager(layoutManager);
        homeFeedRecyclerView.setHasFixedSize(false);

        CulturePostAdapter feedAdapter =
                new CulturePostAdapter(
                        this,
                        CulturePostData.getAllPosts()
                );

        homeFeedRecyclerView.setAdapter(feedAdapter);
    }

    // =========================================================
    // STORIES
    // =========================================================

    private void setupStoryButtons() {

        setOnClick(
                R.id.templeStoryButton,
                TempleActivity.class
        );

        setOnClick(
                R.id.rajaStoryButton,
                RajaActivity.class
        );

        setOnClick(
                R.id.deviStoryButton,
                DeviDevtaActivity.class
        );

        setOnClick(
                R.id.gitaStoryButton,
                GitaActivity.class
        );

        setOnClick(
                R.id.yourStoryButton,
                StoryUploadActivity.class
        );
    }

    // =========================================================
    // NOTIFICATIONS
    // =========================================================

    private void setupNotificationButton() {

        if (findViewById(R.id.notificationButton) == null) {
            return;
        }

        findViewById(R.id.notificationButton)
                .setOnClickListener(v -> {

                    Intent intent =
                            new Intent(
                                    MainActivity.this,
                                    NotificationsActivity.class
                            );

                    startActivity(intent);
                });
    }

    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private void setupBottomNavigation() {

        // HOME
        if (findViewById(R.id.homeNavButton) != null) {

            findViewById(R.id.homeNavButton)
                    .setOnClickListener(v -> {

                        if (homeFeedRecyclerView != null) {

                            homeFeedRecyclerView
                                    .smoothScrollToPosition(0);
                        }
                    });
        }

        // EXPLORE / SEARCH
        if (findViewById(R.id.exploreNavButton) != null) {

            findViewById(R.id.exploreNavButton)
                    .setOnClickListener(v -> {

                        Intent intent =
                                new Intent(
                                        MainActivity.this,
                                        SearchActivity.class
                                );

                        startActivity(intent);
                    });
        }

        // CREATE
        if (findViewById(R.id.createNavButton) != null) {

            findViewById(R.id.createNavButton)
                    .setOnClickListener(
                            v -> showCreateMenu()
                    );
        }

        // CHAT
        setOnClick(
                R.id.chatNavButton,
                ChatActivity.class
        );

        // PROFILE
        if (findViewById(R.id.profileNavButton) != null) {

            findViewById(R.id.profileNavButton)
                    .setOnClickListener(v -> {

                        FirebaseAuth auth =
                                FirebaseAuth.getInstance();

                        if (auth.getCurrentUser() == null) {

                            startActivity(
                                    new Intent(
                                            MainActivity.this,
                                            LoginActivity.class
                                    )
                            );

                        } else {

                            startActivity(
                                    new Intent(
                                            MainActivity.this,
                                            MyProfileActivity.class
                                    )
                            );
                        }
                    });
        }
    }

    // =========================================================
    // SAFE ACTIVITY CLICK
    // =========================================================

    private void setOnClick(
            int viewId,
            Class<?> targetActivity
    ) {

        if (findViewById(viewId) == null) {
            return;
        }

        findViewById(viewId)
                .setOnClickListener(v -> {

                    Intent intent =
                            new Intent(
                                    MainActivity.this,
                                    targetActivity
                            );

                    startActivity(intent);
                });
    }

    // =========================================================
    // THEME
    // =========================================================

    private void applySavedTheme() {

        SharedPreferences preferences =
                getSharedPreferences(
                        PREFS_NAME,
                        MODE_PRIVATE
                );

        boolean darkMode =
                preferences.getBoolean(
                        THEME_KEY,
                        false
                );

        if (darkMode) {

            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
            );

        } else {

            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
            );
        }
    }

    // =========================================================
    // CREATE MENU
    // =========================================================

    private void showCreateMenu() {

        String[] options = {
                "Create Reel",
                "Create Post",
                "Create Story"
        };

        new AlertDialog.Builder(this)
                .setTitle("Create")
                .setItems(options, (dialog, which) -> {

                    switch (which) {

                        case 0:
                            startActivity(
                                    new Intent(
                                            MainActivity.this,
                                            ReelUploadActivity.class
                                    )
                            );
                            break;

                        case 1:
                            startActivity(
                                    new Intent(
                                            MainActivity.this,
                                            PostUploadActivity.class
                                    )
                            );
                            break;

                        case 2:
                            startActivity(
                                    new Intent(
                                            MainActivity.this,
                                            StoryUploadActivity.class
                                    )
                            );
                            break;
                    }
                })
                .show();
    }
}
