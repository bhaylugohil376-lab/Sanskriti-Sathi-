package com.sanskritisathi.app;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

    // =========================================================
    // EDITOR SCREEN
    // =========================================================

    private View editorScreen;
    private VideoView editorVideoPreview;

    private ImageButton editorBackButton;

    private Button audioButton;
    private Button textButton;
    private Button voiceButton;
    private Button captionsButton;
    private Button stickersButton;

    private Button openEditsButton;
    private Button nextButton;

    // =========================================================
    // DETAILS SCREEN
    // =========================================================

    private View detailsScreen;

    private ImageButton detailsBackButton;

    private VideoView coverPreview;
    private Button editCoverButton;

    private EditText captionInput;

    private Button hashtagsButton;
    private Button pollButton;
    private Button promptButton;

    private Button tagPeopleButton;
    private Button locationButton;
    private Button renameAudioButton;
    private Button aiLabelButton;

    private RadioButton publicRadio;
    private RadioButton followersRadio;

    private Button saveDraftButton;
    private Button publishButton;

    private ProgressBar uploadProgress;
    private TextView uploadStatusText;

    // =========================================================
    // DATA
    // =========================================================

    private Uri selectedVideoUri;

    private boolean uploading = false;

    private ActivityResultLauncher<String> videoPickerLauncher;

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_reel_upload
        );

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

    // =========================================================
    // BIND VIEWS
    // =========================================================

    private void bindViews() {

        // Editor

        editorScreen =
                findViewById(
                        R.id.editorScreen
                );

        editorVideoPreview =
                findViewById(
                        R.id.editorVideoPreview
                );

        editorBackButton =
                findViewById(
                        R.id.editorBackButton
                );

        audioButton =
                findViewById(
                        R.id.audioButton
                );

        textButton =
                findViewById(
                        R.id.textButton
                );

        voiceButton =
                findViewById(
                        R.id.voiceButton
                );

        captionsButton =
                findViewById(
                        R.id.captionsButton
                );

        stickersButton =
                findViewById(
                        R.id.stickersButton
                );

        openEditsButton =
                findViewById(
                        R.id.openEditsButton
                );

        nextButton =
                findViewById(
                        R.id.nextButton
                );


        // Details

        detailsScreen =
                findViewById(
                        R.id.detailsScreen
                );

        detailsBackButton =
                findViewById(
                        R.id.detailsBackButton
                );

        coverPreview =
                findViewById(
                        R.id.coverPreview
                );

        editCoverButton =
                findViewById(
                        R.id.editCoverButton
                );

        captionInput =
                findViewById(
                        R.id.captionInput
                );

        hashtagsButton =
                findViewById(
                        R.id.hashtagsButton
                );

        pollButton =
                findViewById(
                        R.id.pollButton
                );

        promptButton =
                findViewById(
                        R.id.promptButton
                );

        tagPeopleButton =
                findViewById(
                        R.id.tagPeopleButton
                );

        locationButton =
                findViewById(
                        R.id.locationButton
                );

        renameAudioButton =
                findViewById(
                        R.id.renameAudioButton
                );

        aiLabelButton =
                findViewById(
                        R.id.aiLabelButton
                );

        publicRadio =
                findViewById(
                        R.id.publicRadio
                );

        followersRadio =
                findViewById(
                        R.id.followersRadio
                );

        saveDraftButton =
                findViewById(
                        R.id.saveDraftButton
                );

        publishButton =
                findViewById(
                        R.id.publishButton
                );

        uploadProgress =
                findViewById(
                        R.id.uploadProgress
                );

        uploadStatusText =
                findViewById(
                        R.id.uploadStatusText
                );
    }

    // =========================================================
    // INITIAL UI
    // =========================================================

    private void setupInitialUI() {

        editorScreen.setVisibility(
                View.VISIBLE
        );

        detailsScreen.setVisibility(
                View.GONE
        );

        nextButton.setEnabled(
                false
        );

        publicRadio.setChecked(
                true
        );

        uploadProgress.setVisibility(
                View.GONE
        );

        uploadStatusText.setVisibility(
                View.GONE
        );

        setupButtonStyles();
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

                            showSelectedVideo(uri);
                        }
                );
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        // -----------------------------------------------------
        // Editor back
        // -----------------------------------------------------

        editorBackButton.setOnClickListener(
                v -> finish()
        );


        // -----------------------------------------------------
        // Open video picker
        // -----------------------------------------------------

        editorVideoPreview.setOnClickListener(
                v -> openVideoPicker()
        );


        // -----------------------------------------------------
        // Editing buttons
        // -----------------------------------------------------

        audioButton.setOnClickListener(
                v -> showComingSoon("Audio")
        );

        textButton.setOnClickListener(
                v -> showComingSoon("Text")
        );

        voiceButton.setOnClickListener(
                v -> showComingSoon("Voice")
        );

        captionsButton.setOnClickListener(
                v -> showComingSoon("Captions")
        );

        stickersButton.setOnClickListener(
                v -> showComingSoon("Stickers")
        );

        openEditsButton.setOnClickListener(
                v -> showComingSoon("Edits")
        );


        // -----------------------------------------------------
        // Next
        // -----------------------------------------------------

        nextButton.setOnClickListener(
                v -> openDetailsScreen()
        );


        // -----------------------------------------------------
        // Details back
        // -----------------------------------------------------

        detailsBackButton.setOnClickListener(
                v -> showEditorScreen()
        );


        // -----------------------------------------------------
        // Cover
        // -----------------------------------------------------

        editCoverButton.setOnClickListener(
                v -> showComingSoon("Edit cover")
        );


        // -----------------------------------------------------
        // Details options
        // -----------------------------------------------------

        hashtagsButton.setOnClickListener(
                v -> insertHashtag()
        );

        pollButton.setOnClickListener(
                v -> showComingSoon("Poll")
        );

        promptButton.setOnClickListener(
                v -> showComingSoon("Prompt")
        );

        tagPeopleButton.setOnClickListener(
                v -> showComingSoon("Tag people")
        );

        locationButton.setOnClickListener(
                v -> showComingSoon("Add location")
        );

        renameAudioButton.setOnClickListener(
                v -> showComingSoon("Rename audio")
        );

        aiLabelButton.setOnClickListener(
                v -> showComingSoon("AI label")
        );


        // -----------------------------------------------------
        // Save draft
        // -----------------------------------------------------

        saveDraftButton.setOnClickListener(
                v -> Toast.makeText(
                        this,
                        "Draft feature ready for next update.",
                        Toast.LENGTH_SHORT
                ).show()
        );


        // -----------------------------------------------------
        // Publish
        // -----------------------------------------------------

        publishButton.setOnClickListener(
                v -> uploadReel()
        );


        // -----------------------------------------------------
        // Video prepared
        // -----------------------------------------------------

        editorVideoPreview.setOnPreparedListener(
                mediaPlayer -> {

                    mediaPlayer.setLooping(
                            true
                    );

                    mediaPlayer.setVolume(
                            1.0f,
                            1.0f
                    );

                    editorVideoPreview.start();
                }
        );


        editorVideoPreview.setOnErrorListener(
                (mp, what, extra) -> {

                    Toast.makeText(
                            ReelUploadActivity.this,
                            "Video preview load nahi ho paya.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return true;
                }
        );


        coverPreview.setOnPreparedListener(
                mediaPlayer -> {

                    mediaPlayer.setLooping(
                            true
                    );

                    mediaPlayer.setVolume(
                            0.0f,
                            0.0f
                    );
                }
        );
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
    // SHOW SELECTED VIDEO
    // =========================================================

    private void showSelectedVideo(
            Uri uri) {

        selectedVideoUri = uri;

        editorVideoPreview.stopPlayback();

        editorVideoPreview.setVideoURI(
                uri
        );

        editorVideoPreview.setVisibility(
                View.VISIBLE
        );

        nextButton.setEnabled(
                true
        );

        nextButton.setAlpha(
                1.0f
        );

        editorVideoPreview.requestFocus();

        // Details cover

        coverPreview.stopPlayback();

        coverPreview.setVideoURI(
                uri
        );

        coverPreview.setVisibility(
                View.VISIBLE
        );

        Toast.makeText(
                this,
                "Video selected ✓",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================================================
    // DETAILS SCREEN
    // =========================================================

    private void openDetailsScreen() {

        if (selectedVideoUri == null) {

            Toast.makeText(
                    this,
                    "Pehle video select karo.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (editorVideoPreview != null) {

            editorVideoPreview.pause();
        }

        editorScreen.setVisibility(
                View.GONE
        );

        detailsScreen.setVisibility(
                View.VISIBLE
        );

        coverPreview.setVideoURI(
                selectedVideoUri
        );

        coverPreview.start();
    }

    // =========================================================
    // EDITOR SCREEN
    // =========================================================

    private void showEditorScreen() {

        if (uploading) {
            return;
        }

        detailsScreen.setVisibility(
                View.GONE
        );

        editorScreen.setVisibility(
                View.VISIBLE
        );

        if (selectedVideoUri != null) {

            editorVideoPreview.start();
        }
    }

    // =========================================================
    // HASHTAG
    // =========================================================

    private void insertHashtag() {

        if (captionInput == null) {
            return;
        }

        int position =
                captionInput
                        .getSelectionStart();

        if (position < 0) {
            position = captionInput.length();
        }

        String current =
                captionInput
                        .getText()
                        .toString();

        String add =
                current.length() == 0
                        ? "#"
                        : " #";

        captionInput
                .getText()
                .insert(
                        position,
                        add
                );

        captionInput.requestFocus();
    }

    // =========================================================
    // PUBLISH REEL
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

                        runOnUiThread(
                                () -> {

                                    uploadProgress
                                            .setProgress(
                                                    progress
                                            );

                                    uploadStatusText
                                            .setText(
                                                    "Publishing "
                                                            + progress
                                                            + "%"
                                            );

                                    publishButton
                                            .setText(
                                                    "Publishing "
                                                            + progress
                                                            + "%"
                                            );
                                }
                        );
                    }

                    @Override
                    public void onSuccess(
                            String videoUrl) {

                        runOnUiThread(
                                () -> {

                                    setUploadingState(
                                            false
                                    );

                                    Toast.makeText(
                                            ReelUploadActivity.this,
                                            "Reel publish ho gayi ✓",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    finish();
                                }
                        );
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(
                                () -> {

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
                                }
                        );
                    }
                }
        );
    }

    // =========================================================
    // VISIBILITY
    // =========================================================

    private String getSelectedVisibility() {

        if (followersRadio.isChecked()) {

            return "Followers";
        }

        return "Public";
    }

    // =========================================================
    // UPLOADING STATE
    // =========================================================

    private void setUploadingState(
            boolean state) {

        uploading = state;

        editorBackButton.setEnabled(
                !state
        );

        detailsBackButton.setEnabled(
                !state
        );

        captionInput.setEnabled(
                !state
        );

        publicRadio.setEnabled(
                !state
        );

        followersRadio.setEnabled(
                !state
        );

        saveDraftButton.setEnabled(
                !state
        );

        publishButton.setEnabled(
                !state
        );

        uploadProgress.setVisibility(
                state
                        ? View.VISIBLE
                        : View.GONE
        );

        uploadStatusText.setVisibility(
                state
                        ? View.VISIBLE
                        : View.GONE
        );

        if (state) {

            uploadProgress.setProgress(
                    0
            );

            uploadStatusText.setText(
                    "Preparing upload..."
            );

            publishButton.setText(
                    "Publishing..."
            );

        } else {

            publishButton.setText(
                    "Publish Reel"
            );

            publishButton.setEnabled(
                    selectedVideoUri != null
            );
        }
    }

    // =========================================================
    // BUTTON STYLES
    // =========================================================

    private void setupButtonStyles() {

        stylePrimaryButton(
                nextButton
        );

        stylePrimaryButton(
                publishButton
        );

        styleSecondaryButton(
                openEditsButton
        );

        styleSecondaryButton(
                saveDraftButton
        );

        styleToolButton(
                audioButton
        );

        styleToolButton(
                textButton
        );

        styleToolButton(
                voiceButton
        );

        styleToolButton(
                captionsButton
        );

        styleToolButton(
                stickersButton
        );
    }

    private void stylePrimaryButton(
            Button button) {

        GradientDrawable bg =
                new GradientDrawable();

        bg.setColor(
                Color.rgb(
                        210,
                        165,
                        70
                )
        );

        bg.setCornerRadius(
                dp(28)
        );

        button.setBackground(
                bg
        );

        button.setBackgroundTintList(
                null
        );

        button.setTextColor(
                Color.BLACK
        );

        button.setAllCaps(
                false
        );
    }

    private void styleSecondaryButton(
            Button button) {

        GradientDrawable bg =
                new GradientDrawable();

        bg.setColor(
                Color.rgb(
                        35,
                        38,
                        43
                )
        );

        bg.setCornerRadius(
                dp(28)
        );

        button.setBackground(
                bg
        );

        button.setBackgroundTintList(
                null
        );

        button.setTextColor(
                Color.WHITE
        );

        button.setAllCaps(
                false
        );
    }

    private void styleToolButton(
            Button button) {

        GradientDrawable bg =
                new GradientDrawable();

        bg.setColor(
                Color.rgb(
                        40,
                        43,
                        48
                )
        );

        bg.setCornerRadius(
                dp(12)
        );

        button.setBackground(
                bg
        );

        button.setBackgroundTintList(
                null
        );

        button.setTextColor(
                Color.WHITE
        );

        button.setAllCaps(
                false
        );
    }

    // =========================================================
    // BACK HANDLER
    // =========================================================

    private void setupBackHandler() {

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                if (uploading) {

                                    Toast.makeText(
                                            ReelUploadActivity.this,
                                            "Publishing complete hone do.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                if (detailsScreen.getVisibility()
                                        == View.VISIBLE) {

                                    showEditorScreen();

                                } else {

                                    finish();
                                }
                            }
                        }
                );
    }

    // =========================================================
    // LIFECYCLE
    // =========================================================

    @Override
    protected void onPause() {

        super.onPause();

        if (editorVideoPreview != null) {
            editorVideoPreview.pause();
        }

        if (coverPreview != null) {
            coverPreview.pause();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (!uploading
                && selectedVideoUri != null) {

            if (editorScreen.getVisibility()
                    == View.VISIBLE) {

                editorVideoPreview.start();

            } else if (detailsScreen.getVisibility()
                    == View.VISIBLE) {

                coverPreview.start();
            }
        }
    }

    // =========================================================
    // SMALL ACTION
    // =========================================================

    private void showComingSoon(
            String feature) {

        Toast.makeText(
                this,
                feature
                        + " editing next update mein add hoga.",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================================================
    // DP
    // =========================================================

    private float dp(
            float value) {

        return value *
                getResources()
                        .getDisplayMetrics()
                        .density;
    }
}
