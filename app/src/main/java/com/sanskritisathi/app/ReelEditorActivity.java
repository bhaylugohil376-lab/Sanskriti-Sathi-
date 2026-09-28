package com.sanskritisathi.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ReelEditorActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    private VideoView editorVideoPreview;

    private TextView editorVideoError;

    private TextView editorNextButton;

    private TextView toolAudio;
    private TextView toolText;
    private TextView toolVoice;
    private TextView toolCaptions;
    private TextView toolStickers;
    private TextView toolEdits;

    // =========================================================
    // DATA
    // =========================================================

    private Uri selectedVideoUri;

    private boolean videoReady = false;

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_reel_editor
        );

        bindViews();

        getVideoUri();

        setupVideoPreview();

        setupTools();

        setupNextButton();
    }

    // =========================================================
    // BIND VIEWS
    // =========================================================

    private void bindViews() {

        editorVideoPreview =
                findViewById(
                        R.id.editorVideoPreview
                );

        editorVideoError =
                findViewById(
                        R.id.editorVideoError
                );

        editorNextButton =
                findViewById(
                        R.id.editorNextButton
                );

        toolAudio =
                findViewById(
                        R.id.toolAudio
                );

        toolText =
                findViewById(
                        R.id.toolText
                );

        toolVoice =
                findViewById(
                        R.id.toolVoice
                );

        toolCaptions =
                findViewById(
                        R.id.toolCaptions
                );

        toolStickers =
                findViewById(
                        R.id.toolStickers
                );

        toolEdits =
                findViewById(
                        R.id.toolEdits
                );
    }

    // =========================================================
    // GET VIDEO URI
    // =========================================================

    private void getVideoUri() {

        String uriString =
                getIntent().getStringExtra(
                        "video_uri"
                );

        if (uriString == null
                || uriString.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Video nahi mili.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        try {

            selectedVideoUri =
                    Uri.parse(uriString);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Invalid video.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }

    // =========================================================
    // VIDEO PREVIEW
    // =========================================================

    private void setupVideoPreview() {

        if (editorVideoPreview == null
                || selectedVideoUri == null) {

            return;
        }

        editorVideoError.setVisibility(
                View.GONE
        );

        try {

            editorVideoPreview.setVideoURI(
                    selectedVideoUri
            );

            editorVideoPreview.setOnPreparedListener(
                    mediaPlayer -> {

                        videoReady = true;

                        mediaPlayer.setLooping(
                                true
                        );

                        editorVideoPreview.start();
                    }
            );

            editorVideoPreview.setOnErrorListener(
                    (mp, what, extra) -> {

                        videoReady = false;

                        showVideoError();

                        return true;
                    }
            );

            editorVideoPreview.setOnCompletionListener(
                    mp -> {

                        if (videoReady) {

                            editorVideoPreview.start();
                        }
                    }
            );

            editorVideoPreview.setOnClickListener(
                    v -> toggleVideo()
            );

        } catch (Exception e) {

            showVideoError();
        }
    }

    // =========================================================
    // TOGGLE VIDEO
    // =========================================================

    private void toggleVideo() {

        if (!videoReady
                || editorVideoPreview == null) {

            return;
        }

        if (editorVideoPreview.isPlaying()) {

            editorVideoPreview.pause();

        } else {

            editorVideoPreview.start();
        }
    }

    // =========================================================
    // TOOLS
    // =========================================================

    private void setupTools() {

        if (toolAudio != null) {

            toolAudio.setOnClickListener(
                    v -> showComingSoon(
                            "Audio"
                    )
            );
        }

        if (toolText != null) {

            toolText.setOnClickListener(
                    v -> showComingSoon(
                            "Text"
                    )
            );
        }

        if (toolVoice != null) {

            toolVoice.setOnClickListener(
                    v -> showComingSoon(
                            "Voice"
                    )
            );
        }

        if (toolCaptions != null) {

            toolCaptions.setOnClickListener(
                    v -> showComingSoon(
                            "Captions"
                    )
            );
        }

        if (toolStickers != null) {

            toolStickers.setOnClickListener(
                    v -> showComingSoon(
                            "Stickers"
                    )
            );
        }

        if (toolEdits != null) {

            toolEdits.setOnClickListener(
                    v -> {

                        Toast.makeText(
                                this,
                                "Open in Edits option ready hai.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
            );
        }
    }

    // =========================================================
    // COMING SOON
    // =========================================================

    private void showComingSoon(
            String feature) {

        Toast.makeText(
                this,
                feature + " editor next step mein add hoga.",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================================================
    // NEXT BUTTON
    // =========================================================

    private void setupNextButton() {

        if (editorNextButton == null) {
            return;
        }

        editorNextButton.setOnClickListener(
                v -> openNewReel()
        );
    }

    // =========================================================
    // OPEN NEW REEL
    // =========================================================

    private void openNewReel() {

        if (selectedVideoUri == null) {

            Toast.makeText(
                    this,
                    "Video select nahi hui.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent =
                new Intent(
                        this,
                        NewReelActivity.class
                );

        /*
         * Selected video New Reel screen
         * ko pass kar rahe hain.
         */
        intent.putExtra(
                "video_uri",
                selectedVideoUri.toString()
        );

        startActivity(
                intent
        );
    }

    // =========================================================
    // VIDEO ERROR
    // =========================================================

    private void showVideoError() {

        if (editorVideoError != null) {

            editorVideoError.setText(
                    "Video preview unavailable"
            );

            editorVideoError.setVisibility(
                    View.VISIBLE
            );
        }

        Toast.makeText(
                this,
                "Video preview open nahi ho saka.",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================================================
    // PAUSE
    // =========================================================

    @Override
    protected void onPause() {

        if (editorVideoPreview != null
                && editorVideoPreview.isPlaying()) {

            editorVideoPreview.pause();
        }

        super.onPause();
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (editorVideoPreview != null
                && videoReady) {

            editorVideoPreview.start();
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        if (editorVideoPreview != null) {

            editorVideoPreview.stopPlayback();
        }

        super.onDestroy();
    }

    // =========================================================
    // BACK
    // =========================================================

    @Override
    public void onBackPressed() {

        if (editorVideoPreview != null) {

            editorVideoPreview.pause();
        }

        super.onBackPressed();
    }
}
