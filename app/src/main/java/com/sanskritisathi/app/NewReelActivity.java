package com.sanskritisathi.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NewReelActivity extends AppCompatActivity {

    private VideoView videoPreview;
    private TextView videoNameText;
    private EditText captionInput;

    private RadioButton publicRadio;
    private RadioButton followersRadio;

    private Button changeVideoButton;
    private Button publishButton;

    private Uri selectedVideoUri;
    private boolean publishing = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_reel);

        bindViews();
        getVideoUri();
        setupVideoPreview();
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

    private void bindViews() {
        videoPreview = findViewById(R.id.newReelVideoPreview);
        videoNameText = findViewById(R.id.newReelVideoName);
        captionInput = findViewById(R.id.newReelCaption);

        publicRadio = findViewById(R.id.newReelPublic);
        followersRadio = findViewById(R.id.newReelFollowers);

        changeVideoButton = findViewById(R.id.newReelChangeButton);
        publishButton = findViewById(R.id.newReelPublishButton);
    }

    private void getVideoUri() {

        String uriString = getIntent().getStringExtra("video_uri");

        if (uriString == null || uriString.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Video nahi mili.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        try {
            selectedVideoUri = Uri.parse(uriString);
        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Invalid video.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }

    private void setupVideoPreview() {

        if (videoPreview == null || selectedVideoUri == null) {
            return;
        }

        try {
            videoPreview.setVideoURI(selectedVideoUri);

            videoPreview.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                videoPreview.start();
            });

            videoPreview.setOnErrorListener((mp, what, extra) -> {

                Toast.makeText(
                        NewReelActivity.this,
                        "Video preview open nahi ho saka.",
                        Toast.LENGTH_SHORT
                ).show();

                return true;
            });

            videoPreview.setOnClickListener(v -> {

                if (videoPreview.isPlaying()) {
                    videoPreview.pause();
                } else {
                    videoPreview.start();
                }
            });

            if (videoNameText != null) {
                videoNameText.setText("Video selected ✓");
            }

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Video preview failed.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void setupListeners() {

        if (changeVideoButton != null) {
            changeVideoButton.setOnClickListener(v -> {

                /*
                 * Existing selected video ko dobara edit/upload
                 * screen par bhej sakte hain.
                 */
                Intent intent = new Intent(
                        NewReelActivity.this,
                        ReelUploadActivity.class
                );

                startActivity(intent);
                finish();
            });
        }

        if (publishButton != null) {
            publishButton.setOnClickListener(
                    v -> publishReel()
            );
        }
    }

    private void publishReel() {

        if (publishing) {
            return;
        }

        if (selectedVideoUri == null) {
            Toast.makeText(
                    this,
                    "Video select nahi hui.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String caption = "";

        if (captionInput != null) {
            caption = captionInput
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

        String visibility = "Public";

        if (followersRadio != null
                && followersRadio.isChecked()) {

            visibility = "Followers";
        }

        publishing = true;

        if (publishButton != null) {
            publishButton.setEnabled(false);
            publishButton.setText("Publishing...");
        }

        ReelSupabaseHelper.uploadReel(
                this,
                selectedVideoUri,
                caption,
                visibility,
                new ReelSupabaseHelper.UploadCallback() {

                    @Override
                    public void onProgress(int progress) {

                        runOnUiThread(() -> {

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
                    public void onSuccess(String videoUrl) {

                        runOnUiThread(() -> {

                            publishing = false;

                            Toast.makeText(
                                    NewReelActivity.this,
                                    "Reel publish ho gayi ✓",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        });
                    }

                    @Override
                    public void onError(String message) {

                        runOnUiThread(() -> {

                            publishing = false;

                            if (publishButton != null) {
                                publishButton.setEnabled(true);
                                publishButton.setText(
                                        "Publish Reel"
                                );
                            }

                            Toast.makeText(
                                    NewReelActivity.this,
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

    @Override
    protected void onPause() {

        if (videoPreview != null
                && videoPreview.isPlaying()) {

            videoPreview.pause();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (!publishing
                && videoPreview != null
                && selectedVideoUri != null) {

            try {
                videoPreview.start();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (videoPreview != null) {
            try {
                videoPreview.stopPlayback();
            } catch (Exception ignored) {
            }
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (publishing) {

            Toast.makeText(
                    this,
                    "Publishing complete hone do.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        super.onBackPressed();
    }
}
