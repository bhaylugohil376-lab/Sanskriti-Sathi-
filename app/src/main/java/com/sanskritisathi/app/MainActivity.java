package com.sanskritisathi.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

        setContentView(R.layout.activity_main);

        // =========================
        // HOME FEED
        // =========================

        homeFeedRecyclerView =
                findViewById(R.id.homeFeedRecyclerView);

        homeFeedRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        homeFeedRecyclerView.setHasFixedSize(false);

        CulturePostAdapter feedAdapter =
                new CulturePostAdapter(
                        this,
                        CulturePostData.getAllPosts()
                );

        homeFeedRecyclerView.setAdapter(feedAdapter);


        // =========================
        // TEMPLE
        // =========================

        findViewById(R.id.templeStoryButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        TempleActivity.class
                                )
                        )
                );


        // =========================
        // RAJA
        // =========================

        findViewById(R.id.rajaStoryButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        RajaActivity.class
                                )
                        )
                );


        // =========================
        // DEVI DEVTA
        // =========================

        findViewById(R.id.deviStoryButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        DeviDevtaActivity.class
                                )
                        )
                );


        // =========================
        // GITA
        // =========================

        findViewById(R.id.gitaStoryButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        GitaActivity.class
                                )
                        )
                );


        // =========================
        // FESTIVAL / RSS
        // =========================

        findViewById(R.id.festivalStoryButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        RssActivity.class
                                )
                        )
                );


        // =========================
        // YOUR STORY
        // =========================

        findViewById(R.id.yourStoryButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        StoryUploadActivity.class
                                )
                        )
                );


        // =========================
        // TOP CREATE
        // =========================

        findViewById(R.id.createTopButton)
                .setOnClickListener(v ->
                        showCreateMenu()
                );


        // =========================
        // NOTIFICATIONS
        // =========================

        findViewById(R.id.notificationButton)
                .setOnClickListener(v ->
                        Toast.makeText(
                                MainActivity.this,
                                "Notifications next step mein add honge.",
                                Toast.LENGTH_SHORT
                        ).show()
                );


        // =========================
        // DAY / NIGHT MODE
        // =========================

        TextView themeToggleButton =
                findViewById(R.id.themeToggleButton);

        updateThemeIcon(themeToggleButton);

        themeToggleButton.setOnClickListener(v -> {

            SharedPreferences preferences =
                    getSharedPreferences(
                            PREFS_NAME,
                            MODE_PRIVATE
                    );

            boolean currentDarkMode =
                    preferences.getBoolean(
                            THEME_KEY,
                            false
                    );

            boolean newDarkMode =
                    !currentDarkMode;

            preferences.edit()
                    .putBoolean(
                            THEME_KEY,
                            newDarkMode
                    )
                    .apply();

            updateThemeIcon(themeToggleButton);

            Toast.makeText(
                    MainActivity.this,
                    newDarkMode
                            ? "🌙 Night Mode"
                            : "☀️ Day Mode",
                    Toast.LENGTH_SHORT
            ).show();
        });


        // =========================
        // HOME
        // =========================

        findViewById(R.id.homeNavButton)
                .setOnClickListener(v ->
                        homeFeedRecyclerView
                                .smoothScrollToPosition(0)
                );


        // =========================
        // REELS
        // =========================

        findViewById(R.id.reelsNavButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        ReelActivity.class
                                )
                        )
                );


// =========================
// CHAT
// =========================

findViewById(R.id.chatNavButton)
        .setOnClickListener(v ->
                startActivity(
                        new Intent(
                                MainActivity.this,
                                ChatActivity.class
                        )
                )
        );

        // =========================
        // SEARCH
        // =========================

        findViewById(R.id.searchNavButton)
                .setOnClickListener(v ->
                        Toast.makeText(
                                MainActivity.this,
                                "Search next step mein add hoga.",
                                Toast.LENGTH_SHORT
                        ).show()
                );


        // =========================
        // PROFILE / LOGIN
        // =========================

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
                                        ProfileActivity.class
                                )
                        );
                    }
                });
    }


    // =========================
    // THEME ICON
    // =========================

    private void updateThemeIcon(TextView button) {

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
            button.setText("☀️");
        } else {
            button.setText("🌙");
        }
    }


    // =========================
    // CREATE MENU
    // =========================

    private void showCreateMenu() {

        String[] options = {
                "🎬 Create Reel",
                "📸 Create Post",
                "⭕ Create Story"
        };

        new AlertDialog.Builder(this)
                .setTitle("Create")
                .setItems(
                        options,
                        (dialog, which) -> {

                            if (which == 0) {

                                // CREATE REEL

                                startActivity(
                                        new Intent(
                                                MainActivity.this,
                                                ReelUploadActivity.class
                                        )
                                );

                            } else if (which == 1) {

                                // CREATE POST

                                Toast.makeText(
                                        MainActivity.this,
                                        "Post creation next step mein add hoga.",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } else {

                                // CREATE STORY

                                startActivity(
                                        new Intent(
                                                MainActivity.this,
                                                StoryUploadActivity.class
                                        )
                                );
                            }
                        }
                )
                .show();
    }
}
