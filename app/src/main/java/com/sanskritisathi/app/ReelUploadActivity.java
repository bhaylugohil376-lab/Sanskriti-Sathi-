package com.sanskritisathi.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ReelUploadActivity extends AppCompatActivity {

    private EditText captionInput;
    private Spinner visibilitySpinner;
    private Button selectVideoButton;
    private Button uploadButton;
    private ProgressBar uploadProgress;

    private Uri selectedVideoUri;

    private ActivityResultLauncher<String> videoPickerLauncher;

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
        }
    }

    // ============================================================
    // BIND VIEWS
    // ============================================================

    private void bindViews() {

        captionInput =
                findViewById(R.id.captionInput);

        visibilitySpinner =
                findViewById(R.id.visibilitySpinner);

        selectVideoButton =
                findViewById(R.id.selectVideoButton);

        uploadButton =
                findViewById(R.id.uploadButton);

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

                            selectVideoButton.setText(
                                    "Video Selected ✓"
                            );

                            uploadButton.setEnabled(
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

        uploadButton.setOnClickListener(
                v -> uploadReel()
        );
    }

    // ============================================================
    // OPEN PICKER
    // ============================================================

    private void openVideoPicker() {

        videoPickerLauncher.launch(
                "video/*"
        );
    }

    // ============================================================
    // UPLOAD
    // ============================================================

    private void uploadReel() {

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

        if (caption.length() > 500) {

            Toast.makeText(
                    this,
                    "Caption maximum 500 characters ka ho sakta hai.",
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

                            uploadProgress.setProgress(
                                    progress
                            );
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

                            setUploadingState(
                                    false
                            );

                            Toast.makeText(
                                    ReelUploadActivity.this,
                                    message,
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

        if (visibilitySpinner == null) {
            return "Public";
        }

        Object selectedItem =
                visibilitySpinner
                        .getSelectedItem();

        if (selectedItem == null) {
            return "Public";
        }

        String value =
                selectedItem
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(value)) {
            return "Public";
        }

        /*
         * Existing UI values:
         *
         * Followers
         * Public
         */
        return value;
    }

    // ============================================================
    // UPLOAD STATE
    // ============================================================

    private void setUploadingState(
            boolean uploading) {

        selectVideoButton.setEnabled(
                !uploading
        );

        captionInput.setEnabled(
                !uploading
        );

        visibilitySpinner.setEnabled(
                !uploading
        );

        uploadButton.setEnabled(
                !uploading
        );

        if (uploading) {

            uploadProgress.setVisibility(
                    android.view.View.VISIBLE
            );

            uploadProgress.setProgress(
                    0
            );

            uploadButton.setText(
                    "Uploading..."
            );

        } else {

            uploadProgress.setVisibility(
                    android.view.View.GONE
            );

            uploadButton.setText(
                    "Upload Reel"
            );
        }
    }

    @Override
    public void onBackPressed() {

        if (uploadButton != null
                && !uploadButton.isEnabled()) {

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
