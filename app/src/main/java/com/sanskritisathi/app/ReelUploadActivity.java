package com.sanskritisathi.app;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class ReelUploadActivity extends AppCompatActivity {

    private View rootLayout;
    private View topBar;
    private View bottomBar;

    private VideoView videoPreview;
    private TextView videoNameText;

    private EditText captionInput;

    private RadioButton publicRadio;
    private RadioButton followersRadio;

    private Button selectVideoButton;
    private Button publishButton;

    private ProgressBar uploadProgress;

    private Uri selectedVideoUri;

    private ActivityResultLauncher<String> videoPickerLauncher;

    private boolean uploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * IMPORTANT:
         * We allow edge-to-edge but manually apply
         * status/navigation bar insets to the correct areas.
         * This prevents Android navigation buttons from
         * covering the Reel controls.
         */
        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );

        Window window = getWindow();

        window.setStatusBarColor(
                android.graphics.Color.TRANSPARENT
        );

        window.setNavigationBarColor(
                android.graphics.Color.BLACK
        );

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            window.setNavigationBarContrastEnforced(false);
            window.setStatusBarContrastEnforced(false);
        }

        setContentView(
                R.layout.activity_reel_upload
        );

        bindViews();

        setupSystemInsets();

        setupVideoPicker();

        setupListeners();

        if (!SupabaseAuthManager.isLoggedIn(this)) {

            Toast.makeText(
                    this,
                    "Login required.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        publishButton.setEnabled(false);

        uploadProgress.setVisibility(
                View.GONE
        );
    }

    // =========================================================
    // BIND VIEWS
    // =========================================================

    private void bindViews() {

        rootLayout =
                findViewById(
                        R.id.reelUploadRoot
                );

        topBar =
                findViewById(
                        R.id.reelTopBar
                );

        bottomBar =
                findViewById(
                        R.id.reelBottomBar
                );

        videoPreview =
                findViewById(
                        R.id.videoPreview
                );

        videoNameText =
                findViewById(
                        R.id.videoNameText
                );

        captionInput =
                findViewById(
                        R.id.captionInput
                );

        publicRadio =
                findViewById(
                        R.id.publicRadio
                );

        followersRadio =
                findViewById(
                        R.id.followersRadio
                );

        selectVideoButton =
                findViewById(
                        R.id.selectVideoButton
                );

        publishButton =
                findViewById(
                        R.id.publishButton
                );

        uploadProgress =
                findViewById(
                        R.id.uploadProgress
                );
    }

    // =========================================================
    // SYSTEM INSETS
    // =========================================================

    private void setupSystemInsets() {

        if (rootLayout == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                rootLayout,
                (view, windowInsets) -> {

                    Insets insets =
                            windowInsets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    if (topBar != null) {

                        topBar.setPadding(
                                topBar.getPaddingLeft(),
                                insets.top + dp(8),
                                topBar.getPaddingRight(),
                                topBar.getPaddingBottom()
                        );
                    }

                    if (bottomBar != null) {

                        bottomBar.setPadding(
                                bottomBar.getPaddingLeft(),
                                bottomBar.getPaddingTop(),
                                bottomBar.getPaddingRight(),
                                insets.bottom + dp(12)
                        );
                    }

                    return windowInsets;
                }
        );

        ViewCompat.requestApplyInsets(
                rootLayout
        );
    }

    private int dp(int value) {

        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // =========================================================
    // VIDEO PICKER
    // =========================================================

    private void setupVideoPicker() {

        videoPickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {

                            if (uri == null) {
                                return;
                            }

                            selectedVideoUri = uri;

                            showVideoPreview(
                                    uri
                            );

                            if (videoNameText != null) {

                                videoNameText.setText(
                                        "Video selected ✓"
                                );
                            }

                            if (publishButton != null) {

                                publishButton.setEnabled(
                                        true
                                );
                            }
                        }
                );
    }

    // =========================================================
    // VIDEO PREVIEW
    // =========================================================

    private void showVideoPreview(
            @NonNull Uri uri) {

        if (videoPreview == null) {
            return;
        }

        try {

            videoPreview.setVisibility(
                    View.VISIBLE
            );

            videoPreview.setVideoURI(
                    uri
            );

            videoPreview.setOnPreparedListener(
                    mediaPlayer -> {

                        mediaPlayer.setLooping(
                                true
                        );

                        /*
                         * Start automatically like Instagram.
                         */
                        videoPreview.start();
                    }
            );

            videoPreview.setOnErrorListener(
                    (mp, what, extra) -> {

                        Toast.makeText(
                                ReelUploadActivity.this,
                                "Video preview open nahi ho saka.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return true;
                    }
            );

            videoPreview.setOnClickListener(
                    v -> {

                        if (videoPreview.isPlaying()) {

                            videoPreview.pause();

                        } else {

                            videoPreview.start();
                        }
                    }
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Video preview failed.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        if (selectVideoButton != null) {

            selectVideoButton.setOnClickListener(
                    v -> openVideoPicker()
            );
        }

        if (publishButton != null) {

            publishButton.setOnClickListener(
                    v -> uploadReel()
            );
        }
    }

    // =========================================================
    // OPEN VIDEO PICKER
    // =========================================================

    private void openVideoPicker() {

        if (uploading) {
            return;
        }

        videoPickerLauncher.launch(
                "video/*"
        );
    }

    // =========================================================
    // UPLOAD REEL
    // =========================================================

    private void uploadReel() {

        if (uploading) {
            return;
        }

        if (selectedVideoUri == null) {

            Toast.makeText(
                    this,
                    "Pehle video select karo.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String caption =
                captionInput
                        .getText()
                        .toString()
                        .trim();

        if (caption.length() > 1000) {

            Toast.makeText(
                    this,
                    "Caption maximum 1000 characters ka ho sakta hai.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String visibility =
                getSelectedVisibility();

        setUploadingState(
                true
        );

        ReelSupabaseHelper.uploadReel(
                this,
                selectedVideoUri,
                caption,
                visibility,
                new ReelSupabaseHelper.UploadCallback() {

                    @Override
                    public void onProgress(
                            int progress) {

                        runOnUiThread(() -> {

                            if (uploadProgress != null) {

                                uploadProgress.setProgress(
                                        progress
                                );
                            }

                            if (publishButton != null) {

                                publishButton.setText(
                                        "Uploading "
                                                + progress
                                                + "%"
                                );
                            }
                        });
                    }

                    @Override
                    public void onSuccess(
                            String videoUrl) {

                        runOnUiThread(() -> {

                            setUploadingState(
                                    false
                            );

                            Toast.makeText(
                                    ReelUploadActivity.this,
                                    "Reel publish ho gayi ✓",
                                    Toast.LENGTH_SHORT
                            ).show();

                            /*
                             * Upload helper already:
                             * 1. uploads video
                             * 2. creates Reel row
                             */
                            finish();
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() -> {

                            setUploadingState(
                                    false
                            );

                            Toast.makeText(
                                    ReelUploadActivity.this,
                                    message == null
                                            ? "Reel upload failed."
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    // =========================================================
    // VISIBILITY
    // =========================================================

    private String getSelectedVisibility() {

        if (followersRadio != null
                && followersRadio.isChecked()) {

            return "Followers";
        }

        return "Public";
    }

    // =========================================================
    // UPLOADING STATE
    // =========================================================

    private void setUploadingState(
            boolean isUploading) {

        uploading = isUploading;

        if (selectVideoButton != null) {

            selectVideoButton.setEnabled(
                    !isUploading
            );
        }

        if (captionInput != null) {

            captionInput.setEnabled(
                    !isUploading
            );
        }

        if (publicRadio != null) {

            publicRadio.setEnabled(
                    !isUploading
            );
        }

        if (followersRadio != null) {

            followersRadio.setEnabled(
                    !isUploading
            );
        }

        if (isUploading) {

            if (publishButton != null) {

                publishButton.setEnabled(
                        false
                );

                publishButton.setText(
                        "Uploading..."
                );
            }

            if (uploadProgress != null) {

                uploadProgress.setVisibility(
                        View.VISIBLE
                );

                uploadProgress.setProgress(
                        0
                );
            }

        } else {

            if (uploadProgress != null) {

                uploadProgress.setVisibility(
                        View.GONE
                );
            }

            if (publishButton != null) {

                publishButton.setText(
                        "Publish Reel"
                );

                publishButton.setEnabled(
                        selectedVideoUri != null
                );
            }
        }
    }

    // =========================================================
    // PAUSE VIDEO WHEN LEAVING
    // =========================================================

    @Override
    protected void onPause() {

        super.onPause();

        if (videoPreview != null
                && videoPreview.isPlaying()) {

            videoPreview.pause();
        }
    }

    // =========================================================
    // RELEASE VIDEO
    // =========================================================

    @Override
    protected void onDestroy() {

        if (videoPreview != null) {

            videoPreview.stopPlayback();
        }

        super.onDestroy();
    }

    // =========================================================
    // BACK PRESS
    // =========================================================

    @Override
    public void onBackPressed() {

        if (uploading) {

            Toast.makeText(
                    this,
                    "Upload complete hone do.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        super.onBackPressed();
    }
}
