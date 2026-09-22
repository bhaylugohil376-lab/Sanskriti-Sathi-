package com.sanskritisathi.app;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ReelUploadActivity extends AppCompatActivity {

    private EditText captionInput;

    private RadioButton publicRadio;
    private RadioButton followersRadio;

    private Button selectVideoButton;
    private Button publishButton;

    private TextView videoNameText;
    private ProgressBar uploadProgress;

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

    // ============================================================
    // BIND VIEWS
    // ============================================================

    private void bindViews() {

        captionInput =
                findViewById(R.id.captionInput);

        publicRadio =
                findViewById(R.id.publicRadio);

        followersRadio =
                findViewById(R.id.followersRadio);

        selectVideoButton =
                findViewById(R.id.selectVideoButton);

        publishButton =
                findViewById(R.id.publishButton);

        videoNameText =
                findViewById(R.id.videoNameText);

        uploadProgress =
                findViewById(R.id.uploadProgress);
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

                            videoNameText.setText(
                                    "Video selected ✓"
                            );

                            publishButton.setEnabled(
                                    true
                            );
                        }
                );
    }

    // ============================================================
    // LISTENERS
    // ============================================================

    private void setupListeners() {

        selectVideoButton.setOnClickListener(
                v -> openVideoPicker()
        );

        publishButton.setOnClickListener(
                v -> uploadReel()
        );
    }

    // ============================================================
    // PICK VIDEO
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
                                        Math.max(
                                                0,
                                                Math.min(
                                                        100,
                                                        progress
                                                )
                                        )
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
                                    "Reel upload ho gayi.",
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

        if (isUploading) {

            publishButton.setEnabled(false);

            uploadProgress.setVisibility(
                    View.VISIBLE
            );

            uploadProgress.setProgress(0);

            publishButton.setText(
                    "Uploading..."
            );

        } else {

            uploadProgress.setVisibility(
                    View.GONE
            );

            publishButton.setText(
                    "Publish Reel"
            );

            /*
             * Video selected hai to button dobara enable.
             */
            publishButton.setEnabled(
                    selectedVideoUri != null
            );
        }
    }

    // ============================================================
    // BACK PRESS
    // ============================================================

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
