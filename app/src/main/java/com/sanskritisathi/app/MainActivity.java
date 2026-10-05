package com.sanskritisathi.app;

import android.Manifest;
import android.app.AlertDialog;
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

/**
 * Sanskriti Sathi main shell.
 *
 * Home
 * Explore
 * Reels
 * Messages
 * Notifications
 * Profile
 * Create / Camera
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView homeFeedRecyclerView;

    private static final String PREFS_NAME =
            "SanskritiSathiPrefs";

    private static final String THEME_KEY =
            "dark_mode";

    /*
     * Camera permission launcher.
     */
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {

                        if (granted) {
                            openCamera();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Camera permission is required to use camera.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    /*
     * Camera result.
     */
    private final ActivityResultLauncher<Intent> cameraLauncher =
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
                             * Camera result can later be connected
                             * to the final Post/Story/Reel editor.
                             */
                        }
                    }
            );

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

        homeFeedRecyclerView =
                findViewById(
                        R.id.homeFeedRecyclerView
                );

        if (homeFeedRecyclerView != null) {

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
    }

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
    }

    /*
     * Final Create menu.
     */
    private void setupTopCreateButton() {

        if (findViewById(R.id.createTopButton) != null) {

            findViewById(R.id.createTopButton)
                    .setOnClickListener(
                            v -> showCreateOptions()
                    );
        }
    }

    private void showCreateOptions() {

        String[] options = {
                "📷 Camera",
                "📝 Create Post",
                "📸 Create Story",
                "🎬 Create Reel"
        };

        new AlertDialog.Builder(this)
                .setTitle("Create")
                .setItems(
                        options,
                        (dialog, which) -> {

                            switch (which) {

                                case 0:
                                    requestCameraPermission();
                                    break;

                                case 1:
                                    startActivity(
                                            new Intent(
                                                    this,
                                                    PostUploadActivity.class
                                            )
                                    );
                                    break;

                                case 2:
                                    openStoryCreator();
                                    break;

                                case 3:
                                    startActivity(
                                            new Intent(
                                                    this,
                                                    ReelUploadActivity.class
                                            )
                                    );
                                    break;
                            }
                        }
                )
                .show();
    }

    /*
     * Story creator placeholder.
     *
     * When the final StoryActivity is present,
     * this method can directly open it.
     */
    private void openStoryCreator() {

        Toast.makeText(
                this,
                "Story creator",
                Toast.LENGTH_SHORT
        ).show();
    }

    /*
     * Camera permission.
     */
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

    /*
     * Open Android camera.
     */
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

        /*
         * Home
         */
        if (findViewById(R.id.homeNavButton) != null) {

            findViewById(R.id.homeNavButton)
                    .setOnClickListener(v -> {

                        if (homeFeedRecyclerView != null) {

                            homeFeedRecyclerView
                                    .smoothScrollToPosition(0);
                        }
                    });
        }

        /*
         * Explore
         */
        if (findViewById(R.id.exploreNavButton) != null) {

            findViewById(R.id.exploreNavButton)
                    .setOnClickListener(v ->
                            startActivity(
                                    new Intent(
                                            this,
                                            ExploreActivity.class
                                    )
                            )
                    );
        }

        /*
         * Reels
         */
        if (findViewById(R.id.reelsNavButton) != null) {

            findViewById(R.id.reelsNavButton)
                    .setOnClickListener(v ->
                            startActivity(
                                    new Intent(
                                            this,
                                            ReelActivity.class
                                    )
                            )
                    );
        }

        /*
         * Messages
         */
        setOnClick(
                R.id.chatNavButton,
                ChatActivity.class
        );

        /*
         * Notifications
         */
        setOnClick(
                R.id.notificationButton,
                NotificationsActivity.class
        );

        /*
         * Profile
         */
        if (findViewById(R.id.profileNavButton) != null) {

            findViewById(R.id.profileNavButton)
                    .setOnClickListener(v -> {

                        if (!SupabaseAuthManager
                                .isLoggedIn(this)) {

                            startActivity(
                                    new Intent(
                                            this,
                                            LoginActivity.class
                                    )
                            );

                        } else {

                            startActivity(
                                    new Intent(
                                            this,
                                            MyProfileActivity.class
                                    )
                            );
                        }
                    });
        }
    }

    private void setOnClick(
            int viewId,
            Class<?> targetActivity
    ) {

        if (findViewById(viewId) != null) {

            findViewById(viewId)
                    .setOnClickListener(v ->
                            startActivity(
                                    new Intent(
                                            this,
                                            targetActivity
                                    )
                            )
                    );
        }
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
