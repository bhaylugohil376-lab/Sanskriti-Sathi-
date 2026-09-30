package com.sanskritisathi.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Supabase-powered main shell. Reel feed is powered by Supabase.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView homeFeedRecyclerView;
    private static final String PREFS_NAME = "SanskritiSathiPrefs";
    private static final String THEME_KEY = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySavedTheme();
        setContentView(R.layout.activity_main);
        setupHomeShell();
        setupStoryButtons();
        setupTopCreateButton();
        setupBottomNavigation();
    }

    private void setupHomeShell() {
        homeFeedRecyclerView = findViewById(R.id.homeFeedRecyclerView);
        if (homeFeedRecyclerView != null) {
            homeFeedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            homeFeedRecyclerView.setHasFixedSize(false);
            homeFeedRecyclerView.setAdapter(new CulturePostAdapter(this, CulturePostData.getAllPosts()));
        }
    }

    private void setupStoryButtons() {
        setOnClick(R.id.templeStoryButton, TempleActivity.class);
        setOnClick(R.id.rajaStoryButton, RajaActivity.class);
        setOnClick(R.id.deviStoryButton, DeviDevtaActivity.class);
        setOnClick(R.id.gitaStoryButton, GitaActivity.class);
    }

    private void setupTopCreateButton() {
        if (findViewById(R.id.createTopButton) != null) {
            findViewById(R.id.createTopButton).setOnClickListener(v ->
                    startActivity(new Intent(this, ReelUploadActivity.class)));
        }
    }

    private void setupBottomNavigation() {
        if (findViewById(R.id.homeNavButton) != null) {
            findViewById(R.id.homeNavButton).setOnClickListener(v -> {
                if (homeFeedRecyclerView != null) homeFeedRecyclerView.smoothScrollToPosition(0);
            });
        }

        if (findViewById(R.id.exploreNavButton) != null) {
            findViewById(R.id.exploreNavButton).setOnClickListener(v ->
                    Toast.makeText(this, "Explore — coming soon", Toast.LENGTH_SHORT).show());
        }

        if (findViewById(R.id.reelsNavButton) != null) {
            findViewById(R.id.reelsNavButton).setOnClickListener(v ->
                    startActivity(new Intent(this, ReelActivity.class)));
        }

        setOnClick(R.id.chatNavButton, ChatActivity.class);

        setOnClick(R.id.notificationButton, NotificationsActivity.class);

        if (findViewById(R.id.profileNavButton) != null) {
            findViewById(R.id.profileNavButton).setOnClickListener(v -> {
                if (!SupabaseAuthManager.isLoggedIn(this)) {
                    startActivity(new Intent(this, LoginActivity.class));
                } else {
                    startActivity(new Intent(this, MyProfileActivity.class));
                }
            });
        }
    }

    private void setOnClick(int viewId, Class<?> targetActivity) {
        if (findViewById(viewId) != null) {
            findViewById(viewId).setOnClickListener(v ->
                    startActivity(new Intent(this, targetActivity)));
        }
    }

    private void applySavedTheme() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkMode = preferences.getBoolean(THEME_KEY, false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
