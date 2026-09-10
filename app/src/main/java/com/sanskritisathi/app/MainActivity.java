package com.sanskritisathi.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        /*
         * =====================================================
         * SAFE AREA
         * Motorola Edge 50 + other screen sizes
         * =====================================================
         */

        View root = findViewById(R.id.mainRoot);

        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    root,
                    (view, windowInsets) -> {

                        Insets insets =
                                windowInsets.getInsets(
                                        WindowInsetsCompat.Type.systemBars()
                                                | WindowInsetsCompat.Type.displayCutout()
                                );

                        view.setPadding(
                                insets.left,
                                insets.top,
                                insets.right,
                                insets.bottom
                        );

                        return windowInsets;
                    }
            );
        }

        /*
         * =====================================================
         * HOME FEED
         * =====================================================
         */

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

        /*
         * =====================================================
         * TEMPLE
         * =====================================================
         */

        findViewById(R.id.templeStoryButton)
                .setOnClickListener(v ->
                        openActivity(TempleActivity.class)
                );

        /*
         * =====================================================
         * RAJA
         * =====================================================
         */

        findViewById(R.id.rajaStoryButton)
                .setOnClickListener(v ->
                        openActivity(RajaActivity.class)
                );

        /*
         * =====================================================
         * DEVI DEVTA
         * =====================================================
         */

        findViewById(R.id.deviStoryButton)
                .setOnClickListener(v ->
                        openActivity(DeviDevtaActivity.class)
                );

        /*
         * =====================================================
         * GITA
         * =====================================================
         */

        findViewById(R.id.gitaStoryButton)
                .setOnClickListener(v ->
                        openActivity(GitaActivity.class)
                );

        /*
         * =====================================================
         * NEWS / RSS
         * =====================================================
         */

        findViewById(R.id.festivalStoryButton)
                .setOnClickListener(v ->
                        openActivity(RssActivity.class)
                );

        /*
         * =====================================================
         * YOUR STORY
         * =====================================================
         */

        findViewById(R.id.yourStoryButton)
                .setOnClickListener(v ->
                        openActivity(StoryUploadActivity.class)
                );

        /*
         * =====================================================
         * CREATE
         * =====================================================
         */

        findViewById(R.id.createTopButton)
                .setOnClickListener(v ->
                        showCreateMenu()
                );

        /*
         * =====================================================
         * NOTIFICATIONS
         * =====================================================
         */

        findViewById(R.id.notificationButton)
                .setOnClickListener(v ->
                        openActivity(NotificationsActivity.class)
                );

        /*
         * =====================================================
         * THEME
         * =====================================================
         */

        TextView themeToggleButton =
                findViewById(R.id.themeToggleButton);

        if (themeToggleButton != null) {

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
        }

        /*
         * =====================================================
         * HOME
         * =====================================================
         */

        findViewById(R.id.homeNavButton)
                .setOnClickListener(v -> {

                    if (homeFeedRecyclerView != null) {
                        homeFeedRecyclerView.smoothScrollToPosition(0);
                    }
                });

        /*
         * =====================================================
         * REELS
         * =====================================================
         */

        findViewById(R.id.reelsNavButton)
                .setOnClickListener(v ->
                        openActivity(ReelActivity.class)
                );

        /*
         * =====================================================
         * CHAT
         * =====================================================
         */

        findViewById(R.id.chatNavButton)
                .setOnClickListener(v ->
                        openActivity(ChatActivity.class)
                );

        /*
         * =====================================================
         * SEARCH
         * =====================================================
         */

        findViewById(R.id.searchNavButton)
                .setOnClickListener(v ->
                        openActivity(SearchActivity.class)
                );

        /*
         * =====================================================
         * PROFILE
         * =====================================================
         */

        findViewById(R.id.profileNavButton)
                .setOnClickListener(v -> {

                    FirebaseAuth auth =
                            FirebaseAuth.getInstance();

                    if (auth.getCurrentUser() == null) {

                        openActivity(LoginActivity.class);

                    } else {

                        openActivity(MyProfileActivity.class);
                    }
                });
    }

    /*
     * =========================================================
     * OPEN ACTIVITY
     * =========================================================
     */

    private void openActivity(
            @NonNull Class<?> activityClass) {

        Intent intent =
                new Intent(
                        MainActivity.this,
                        activityClass
                );

        startActivity(intent);
    }

    /*
     * =========================================================
     * THEME ICON
     * =========================================================
     */

    private void updateThemeIcon(
            TextView button) {

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

        button.setText(
                darkMode
                        ? "☀️"
                        : "🌙"
        );
    }

    /*
     * =========================================================
     * CREATE MENU
     * =========================================================
     */

    private void showCreateMenu() {

        String[] options = {
                "🎬  Create Reel",
                "📸  Create Post",
                "⭕  Create Story"
        };

        new AlertDialog.Builder(this)
                .setTitle("Create on Sanskriti Sathi")
                .setItems(
                        options,
                        (dialog, which) -> {

                            switch (which) {

                                case 0:

                                    // Reel upload
                                    openActivity(
                                            ReelUploadActivity.class
                                    );

                                    break;

                                case 1:

                                    // Real Post creation
                                    openActivity(
                                            PostUploadActivity.class
                                    );

                                    break;

                                case 2:

                                    // Story upload
                                    openActivity(
                                            StoryUploadActivity.class
                                    );

                                    break;
                            }
                        }
                )
                .show();
    }
}
