package com.sanskritisathi.app;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ReelUploadActivity extends AppCompatActivity {

    private VideoView videoPreview;

    private ImageButton closeButton;

    private EditText captionInput;

    private RadioButton publicRadio;
    private RadioButton followersRadio;

    private Button selectVideoButton;
    private Button publishButton;

    private TextView videoNameText;
    private TextView selectedVideoHint;
    private TextView uploadStatusText;

    private ProgressBar uploadProgress;

    private View emptyPreview;

    private Uri selectedVideoUri;

    private ActivityResultLauncher<String> videoPickerLauncher;

    private boolean uploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reel_upload);

        bindViews();
        setupVideoPicker();
        setupListeners();
        setupBackHandler();
        setupInitialUI();

        if (!SupabaseAuthManager.isLoggedIn(this)) {

            Toast.makeText(
                    this,
                    "Login required.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }

    // ============================================================
    // BIND VIEWS
    // ============================================================

    private void bindViews() {

        videoPreview = findViewById(R.id.videoPreview);

        closeButton = findViewById(R.id.closeButton);

        captionInput = findViewById(R.id.captionInput);

        publicRadio = findViewById(R.id.publicRadio);
        followersRadio = findViewById(R.id.followersRadio);

        selectVideoButton = findViewById(R.id.selectVideoButton);
        publishButton = findViewById(R.id.publishButton);

        videoNameText = findViewById(R.id.videoNameText);
        selectedVideoHint = findViewById(R.id.selectedVideoHint);
        uploadStatusText = findViewById(R.id.uploadStatusText);

        uploadProgress = findViewById(R.id.uploadProgress);

        emptyPreview = findViewById(R.id.emptyPreview);
    }

    // ============================================================
    // INITIAL UI
    // ============================================================

    private void setupInitialUI() {

        publicRadio.setChecked(true);

        publishButton.setEnabled(false);

        uploadProgress.setVisibility(View.GONE);

        uploadStatusText.setVisibility(View.GONE);

        videoNameText.setVisibility(View.GONE);

        selectedVideoHint.setVisibility(View.VISIBLE);

        emptyPreview.setVisibility(View.VISIBLE);

        videoPreview.setVisibility(View.GONE);

        setupButtonBackgrounds();
    }

    // ============================================================
    // BUTTON STYLING
    // ============================================================

    private void setupButtonBackgrounds() {

        GradientDrawable publishBackground =
                new GradientDrawable();

        publishBackground.setColor(
                Color.rgb(210, 165, 70)
        );

        publishBackground.setCornerRadius(
                dp(28)
        );

        publishButton.setBackground(
                publishBackground
        );

        publishButton.setTextColor(
                Color.BLACK
        );

        GradientDrawable selectBackground =
                new GradientDrawable();

        selectBackground.setColor(
                Color.rgb(35, 35, 35)
        );

        selectBackground.setCornerRadius(
                dp(24)
        );

        selectBackground.setStroke(
                (int) dp(1),
                Color.rgb(80, 80, 80)
        );

        selectVideoButton.setBackground(
                selectBackground
        );

        selectVideoButton.setTextColor(
                Color.WHITE
        );
    }

    // ============================================================
    // VIDEO PICKER
    // ============================================================

    private void setupVideoPicker() {

        videoPickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {

                            if (uri == null) {
                                return;
                            }

                            selectedVideoUri = uri;

                            showVideoPreview(uri);
                        }
                );
    }

    // ============================================================
    // LISTENERS
    // ============================================================

    private void setupListeners() {

        closeButton.setOnClickListener(v -> {

            if (uploading) {

                Toast.makeText(
                        ReelUploadActivity.this,
                        "Upload complete hone do.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            finish();
        });

        selectVideoButton.setOnClickListener(
                v -> openVideoPicker()
        );

        publishButton.setOnClickListener(
                v -> uploadReel()
        );

        /*
         * VideoView automatically keeps the video's aspect ratio.
         *
         * The XML places it inside a fixed 9:16 FrameLayout.
         * Therefore the video is scaled proportionally and
         * black space is left wherever required.
         */

        videoPreview.setOnPreparedListener(
                mediaPlayer -> {

                    mediaPlayer.setLooping(true);

                    mediaPlayer.setVolume(
                            1.0f,
                            1.0f
                    );

                    videoPreview.start();
                }
        );

        videoPreview.setOnErrorListener(
                (mp, what, extra) -> {

                    Toast.makeText(
                            ReelUploadActivity.this,
                            "Video preview load nahi ho paya.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return true;
                }
        );
    }

    // ============================================================
    // BACK HANDLER
    // ============================================================

    private void setupBackHandler() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (uploading) {

                            Toast.makeText(
                                    ReelUploadActivity.this,
                                    "Upload complete hone do.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        finish();
                    }
                }
        );
    }

    // ============================================================
    // OPEN VIDEO PICKER
    // ============================================================

    private void openVideoPicker() {

        if (uploading) {
            return;
        }

        videoPickerLauncher.launch(
                "video/*"
        );
    }

    // ============================================================
    // SHOW VIDEO PREVIEW
    // ============================================================

    private void showVideoPreview(Uri uri) {

        selectedVideoUri = uri;

        emptyPreview.setVisibility(
                View.GONE
        );

        videoPreview.setVisibility(
                View.VISIBLE
        );

        videoNameText.setVisibility(
                View.VISIBLE
        );

        videoNameText.setText(
                "Video selected ✓"
        );

        selectedVideoHint.setVisibility(
                View.GONE
        );

        publishButton.setEnabled(true);

        uploadStatusText.setVisibility(
                View.GONE
        );

        /*
         * Important:
         *
         * We DO NOT crop, resize, compress or modify the URI.
         *
         * VideoView renders the original video while preserving
         * its aspect ratio.
         *
         * The parent preview container is fixed at 9:16.
         */

        videoPreview.stopPlayback();

        videoPreview.setVideoURI(uri);

        videoPreview.requestFocus();
    }

    // ============================================================
    // UPLOAD REEL
    // ============================================================

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

        setUploadingState(true);

        /*
         * IMPORTANT:
         *
         * selectedVideoUri is passed directly.
         *
         * No crop.
         * No resize.
         * No preview bitmap.
         * No conversion.
         *
         * Therefore B2 receives the original selected video.
         */

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

                            if (uploadStatusText != null) {

                                uploadStatusText.setVisibility(
                                        View.VISIBLE
                                );

                                uploadStatusText.setText(
                                        "Uploading "
                                                + progress
                                                + "%"
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
                        });
                    }
                }
        );
    }

    // ============================================================
    // VISIBILITY
    // ============================================================

    private String getSelectedVisibility() {

        if (followersRadio != null
                && followersRadio.isChecked()) {

            return "Followers";
        }

        return "Public";
    }

    // ============================================================
    // UPLOADING STATE
    // ============================================================

    private void setUploadingState(
            boolean isUploading) {

        uploading = isUploading;

        selectVideoButton.setEnabled(
                !isUploading
        );

        captionInput.setEnabled(
                !isUploading
        );

        publicRadio.setEnabled(
                !isUploading
        );

        followersRadio.setEnabled(
                !isUploading
        );

        closeButton.setEnabled(
                !isUploading
        );

        if (isUploading) {

            publishButton.setEnabled(false);

            uploadProgress.setVisibility(
                    View.VISIBLE
            );

            uploadProgress.setProgress(0);

            uploadStatusText.setVisibility(
                    View.VISIBLE
            );

            uploadStatusText.setText(
                    "Preparing upload..."
            );

            publishButton.setText(
                    "Uploading..."
            );

        } else {

            uploadProgress.setVisibility(
                    View.GONE
            );

            uploadStatusText.setVisibility(
                    View.GONE
            );

            publishButton.setText(
                    "Publish Reel"
            );

            publishButton.setEnabled(
                    selectedVideoUri != null
            );
        }
    }

    // ============================================================
    // LIFECYCLE
    // ============================================================

    @Override
    protected void onPause() {
        super.onPause();

        if (videoPreview != null
                && videoPreview.isPlaying()) {

            videoPreview.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (videoPreview != null
                && selectedVideoUri != null
                && !uploading) {

            videoPreview.start();
        }
    }

    // ============================================================
    // DP
    // ============================================================

    private float dp(float value) {

        return value *
                getResources()
                        .getDisplayMetrics()
                        .density;
    }
}
