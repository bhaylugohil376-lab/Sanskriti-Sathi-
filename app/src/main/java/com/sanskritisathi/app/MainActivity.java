package com.sanskritisathi.app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView homeFeedRecyclerView;

    private static final String PREFS_NAME =
            "SanskritiSathiPrefs";

    private static final String THEME_KEY =
            "dark_mode";

    private final ActivityResultLauncher<String>
            cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            openCamera();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Camera permission is required.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    private final ActivityResultLauncher<Intent>
            cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Toast.makeText(
                                    this,
                                    "Photo captured",
                                    Toast.LENGTH_SHORT
                            ).show();

                            /*
                             * Camera result ko future Post/Story
                             * editor me connect kiya ja sakta hai.
                             */
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        applySavedTheme();

        setContentView(R.layout.activity_main);

        setupHomeFeed();
        setupStories();
        setupCreateButton();
        setupBottomNavigation();
    }

    private void setupHomeFeed() {

        homeFeedRecyclerView =
                findViewById(R.id.homeFeedRecyclerView);

        if (homeFeedRecyclerView == null) {
            return;
        }

        homeFeedRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        homeFeedRecyclerView.setHasFixedSize(false);

        homeFeedRecyclerView.setAdapter(
                new CulturePostAdapter(
                        this,
                        CulturePostData.getAllPosts()
                )
        );
    }

    private void setupStories() {

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
    }

    private void setupCreateButton() {

        if (findViewById(R.id.createTopButton) == null) {
            return;
        }

        findViewById(R.id.createTopButton)
                .setOnClickListener(
                        v -> showCreateOptions()
                );
    }

    private void showCreateOptions() {

        String[] options = {
                "Camera",
                "Create Post",
                "Create Story",
                "Create Reel"
        };

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Create")
                .setItems(
                        options,
                        (dialog, which) -> {

                            switch (which) {

                                case 0:
                                    requestCameraPermission();
                                    break;

                                case 1:
                                    openActivity(
                                            PostUploadActivity.class
                                    );
                                    break;

                                case 2:
                                    openStoryCreator();
                                    break;

                                case 3:
                                    openActivity(
                                            ReelUploadActivity.class
                                    );
                                    break;
                            }
                        }
                )
                .show();
    }

    private void openStoryCreator() {

        openActivity(
                StoryUploadActivity.class
        );
    }

    private void requestCameraPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            openCamera();

        } else {

            cameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
            );
        }
    }

    private void openCamera() {

        Intent cameraIntent =
                new Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE
                );

        if (cameraIntent.resolveActivity(
                getPackageManager()
        ) != null) {

            cameraLauncher.launch(cameraIntent);

        } else {

            Toast.makeText(
                    this,
                    "Camera is not available.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void setupBottomNavigation() {

        // Home
        setOnClick(
                R.id.homeNavButton,
                null
        );

        if (findViewById(R.id.homeNavButton) != null) {

            findViewById(R.id.homeNavButton)
                    .setOnClickListener(v -> {

                        if (homeFeedRecyclerView != null) {

                            homeFeedRecyclerView
                                    .smoothScrollToPosition(0);
                        }
                    });
        }

        // Explore
        setOnClick(
                R.id.exploreNavButton,
                ExploreActivity.class
        );

        // Reels
        setOnClick(
                R.id.reelsNavButton,
                ReelActivity.class
        );

        // Messages
        setOnClick(
                R.id.chatNavButton,
                ChatActivity.class
        );

        // Notifications
        setOnClick(
                R.id.notificationButton,
                NotificationsActivity.class
        );

        // Profile
        if (findViewById(R.id.profileNavButton) != null) {

            findViewById(R.id.profileNavButton)
                    .setOnClickListener(v -> {

                        if (!SupabaseAuthManager
                                .isLoggedIn(this)) {

                            openActivity(
                                    LoginActivity.class
                            );

                        } else {

                            openActivity(
                                    MyProfileActivity.class
                            );
                        }
                    });
        }
    }

    private void setOnClick(
            int viewId,
            Class<?> targetActivity
    ) {

        if (findViewById(viewId) == null) {
            return;
        }

        if (targetActivity == null) {
            return;
        }

        findViewById(viewId)
                .setOnClickListener(
                        v -> openActivity(
                                targetActivity
                        )
                );
    }

    private void openActivity(
            Class<?> targetActivity
    ) {

        if (targetActivity == null) {
            return;
        }

        startActivity(
                new Intent(
                        this,
                        targetActivity
                )
        );
    }

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

        AppCompatDelegate.setDefaultNightMode(
                darkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
