package com.sanskritisathi.app;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
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
    private View emptyVideoState;

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
    private boolean previewPrepared = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );

        Window window = getWindow();

        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.BLACK);

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            window.setNavigationBarContrastEnforced(false);
            window.setStatusBarContrastEnforced(false);
        }

        setContentView(R.layout.activity_reel_upload);

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

        if (publishButton != null) {
            publishButton.setEnabled(false);
        }

        if (uploadProgress != null) {
            uploadProgress.setVisibility(View.GONE);
        }

        setupInitialPreviewState();
    }

    // =========================================================
    // BIND VIEWS
    // =========================================================

    private void bindViews() {

        rootLayout = findViewById(
                R.id.reelUploadRoot
        );

        topBar = findViewById(
                R.id.reelTopBar
        );

        bottomBar = findViewById(
                R.id.reelBottomBar
        );

        emptyVideoState = findViewById(
                R.id.emptyVideoState
        );

        videoPreview = findViewById(
                R.id.videoPreview
        );

        videoNameText = findViewById(
                R.id.videoNameText
        );

        captionInput = findViewById(
                R.id.captionInput
        );

        publicRadio = findViewById(
                R.id.publicRadio
        );

        followersRadio = findViewById(
                R.id.followersRadio
        );

        selectVideoButton = findViewById(
                R.id.selectVideoButton
        );

        publishButton = findViewById(
                R.id.publishButton
        );

        uploadProgress = findViewById(
                R.id.uploadProgress
        );
    }

    // =========================================================
    // INITIAL VIDEO STATE
    // =========================================================

    private void setupInitialPreviewState() {

        previewPrepared = false;

        if (videoPreview != null) {

            videoPreview.stopPlayback();

            videoPreview.setVisibility(
                    View.GONE
            );

            videoPreview.setBackgroundColor(
                    Color.BLACK
            );
        }

        if (emptyVideoState != null) {

            emptyVideoState.setVisibility(
                    View.VISIBLE
            );
        }

        if (videoNameText != null) {

            videoNameText.setText(
                    "Select a video"
            );
        }
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
                value *
                        getResources()
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
                            previewPrepared = false;

                            if (emptyVideoState != null) {

                                emptyVideoState.setVisibility(
                                        View.GONE
                                );
                            }

                            if (videoPreview != null) {

                                videoPreview.setVisibility(
                                        View.VISIBLE
                                );
                            }

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

                            showVideoPreview(uri);
                        }
                );
    }

    // =========================================================
    // VIDEO PREVIEW - BLACK SCREEN FIX
    // =========================================================

    private void showVideoPreview(
            @NonNull Uri uri) {

        if (videoPreview == null) {
            return;
        }

        try {

            /*
             * Stop previous MediaPlayer completely.
             */
            videoPreview.stopPlayback();

            previewPrepared = false;

            videoPreview.setVisibility(
                    View.VISIBLE
            );

            videoPreview.setBackgroundColor(
                    Color.BLACK
            );

            /*
             * IMPORTANT:
             * Set URI only ONCE.
             * Do not call setVideoURI again after delay.
             */
            videoPreview.setVideoURI(uri);

            videoPreview.setOnPreparedListener(
                    new MediaPlayer.OnPreparedListener() {

                        @Override
                        public void onPrepared(
                                MediaPlayer mp) {

                            previewPrepared = true;

                            mp.setLooping(true);

                            /*
                             * Keep first frame visible.
                             */
                            try {
                                mp.seekTo(1);
                            } catch (Exception ignored) {
                            }

                            /*
                             * Start only after prepared.
                             */
                            try {
                                videoPreview.start();
                            } catch (Exception ignored) {
                            }
                        }
                    }
            );

            videoPreview.setOnCompletionListener(
                    mp -> {

                        if (!isFinishing()
                                && !isDestroyed()) {

                            try {
                                videoPreview.start();
                            } catch (Exception ignored) {
                            }
                        }
                    }
            );

            videoPreview.setOnErrorListener(
                    (mp, what, extra) -> {

                        previewPrepared = false;

                        if (emptyVideoState != null) {

                            emptyVideoState.setVisibility(
                                    View.VISIBLE
                            );
                        }

                        Toast.makeText(
                                ReelUploadActivity.this,
                                "Video preview open nahi ho saka.",
                                Toast.LENGTH_LONG
                        ).show();

                        return true;
                    }
            );

            videoPreview.setOnClickListener(
                    v -> {

                        if (!previewPrepared
                                || uploading) {
                            return;
                        }

                        try {

                            if (videoPreview.isPlaying()) {

                                videoPreview.pause();

                            } else {

                                videoPreview.start();
                            }

                        } catch (Exception ignored) {
                        }
                    }
            );

        } catch (Exception e) {

            previewPrepared = false;

            Toast.makeText(
                    this,
                    "Video preview failed.",
                    Toast.LENGTH_LONG
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

        /*
         * XML back button.
         */
        View backButton =
                findViewById(
                        R.id.reelBackButton
                );

        if (backButton != null) {

            backButton.setOnClickListener(
                    v -> {

                        if (uploading) {

                            Toast.makeText(
                                    this,
                                    "Upload complete hone do.",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            finish();
                        }
                    }
            );
        }
    }

    // =========================================================
    // OPEN PICKER
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

        String caption = "";

        if (captionInput != null) {

            caption =
                    captionInput
                            .getText()
                            .toString()
                            .trim();
        }

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

        setUploadingState(true);

        /*
         * Pause preview while uploading.
         */
        if (videoPreview != null
                && videoPreview.isPlaying()) {

            videoPreview.pause();
        }

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

                            setUploadingState(false);

                            Toast.makeText(
                                    ReelUploadActivity.this,
                                    "Reel publish ho gayi ✓",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() -> {

                            setUploadingState(false);

                            Toast.makeText(
                                    ReelUploadActivity.this,
                                    message == null
                                            ? "Reel upload failed."
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();

                            if (videoPreview != null
                                    && selectedVideoUri != null
                                    && previewPrepared) {

                                try {
                                    videoPreview.start();
                                } catch (Exception ignored) {
                                }
                            }
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
    // UPLOAD STATE
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

                publishButton.setEnabled(false);

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
    // RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (!uploading
                && videoPreview != null
                && selectedVideoUri != null
                && previewPrepared) {

            try {
                videoPreview.start();
            } catch (Exception ignored) {
            }
        }
    }

    // =========================================================
    // PAUSE
    // =========================================================

    @Override
    protected void onPause() {

        if (videoPreview != null
                && videoPreview.isPlaying()) {

            videoPreview.pause();
        }

        super.onPause();
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        if (videoPreview != null) {

            try {
                videoPreview.stopPlayback();
            } catch (Exception ignored) {
            }
        }

        selectedVideoUri = null;
        previewPrepared = false;

        super.onDestroy();
    }

    // =========================================================
    // BACK
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
