package com.sanskritisathi.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    private Uri selectedVideoUri;

    private TextView videoNameText;
    private EditText captionInput;
    private RadioButton publicRadio;
    private RadioButton followersRadio;
    private ProgressBar uploadProgress;
    private Button selectVideoButton;
    private Button publishButton;

    private final ActivityResultLauncher<String> videoPicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedVideoUri = uri;

                            String name = uri.getLastPathSegment();

                            if (name == null || name.isEmpty()) {
                                name = "Video selected";
                            }

                            videoNameText.setText("🎬 " + name);
                            publishButton.setEnabled(true);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reel_upload);

        videoNameText = findViewById(R.id.videoNameText);
        captionInput = findViewById(R.id.captionInput);
        publicRadio = findViewById(R.id.publicRadio);
        followersRadio = findViewById(R.id.followersRadio);
        uploadProgress = findViewById(R.id.uploadProgress);
        selectVideoButton = findViewById(R.id.selectVideoButton);
        publishButton = findViewById(R.id.publishButton);

        uploadProgress.setVisibility(ProgressBar.GONE);
        publishButton.setEnabled(false);

        selectVideoButton.setOnClickListener(v ->
                videoPicker.launch("video/*")
        );

        publishButton.setOnClickListener(v -> publishReel());
    }

    private void publishReel() {

        if (selectedVideoUri == null) {
            Toast.makeText(
                    this,
                    "Pehle video select karo.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String caption = captionInput.getText()
                .toString()
                .trim();

        String visibility;

        if (followersRadio.isChecked()) {
            visibility = "Followers";
        } else {
            visibility = "Public";
        }

        publishButton.setEnabled(false);
        selectVideoButton.setEnabled(false);
        uploadProgress.setVisibility(ProgressBar.VISIBLE);
        uploadProgress.setProgress(0);

        ReelFirebaseHelper.uploadReel(
                selectedVideoUri,
                caption,
                visibility,
                new ReelFirebaseHelper.UploadCallback() {

                    @Override
                    public void onProgress(int progress) {
                        runOnUiThread(() -> {
                            uploadProgress.setProgress(progress);
                            publishButton.setText(
                                    "Uploading " + progress + "%"
                            );
                        });
                    }

                    @Override
                    public void onSuccess(String reelId) {
                        runOnUiThread(() -> {

                            Toast.makeText(
                                    ReelUploadActivity.this,
                                    "Reel published successfully 🎉",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {

                            uploadProgress.setVisibility(
                                    ProgressBar.GONE
                            );

                            publishButton.setEnabled(true);
                            selectVideoButton.setEnabled(true);
                            publishButton.setText("Publish Reel");

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
}
