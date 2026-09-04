package com.sanskritisathi.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.List;

public class StoryActivity extends AppCompatActivity {

    private ImageView storyImage;
    private ImageButton closeButton;
    private ImageButton deleteButton;
    private ImageButton likeButton;
    private ImageButton replyButton;

    private TextView usernameText;
    private TextView timeText;
    private TextView viewsText;
    private TextView captionText;
    private ProgressBar progressBar;

    private StoryFirebaseHelper firebaseHelper;

    private List<Story> stories;
    private int currentPosition = 0;
    private Story currentStory;

    private boolean liked = false;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final Runnable autoCloseRunnable =
            this::finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_story);

        initializeViews();

        firebaseHelper =
                new StoryFirebaseHelper();

        currentPosition =
                getIntent().getIntExtra(
                        "story_position",
                        0
                );

        loadStories();
    }

    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initializeViews() {

        storyImage =
                findViewById(R.id.storyImage);

        closeButton =
                findViewById(R.id.closeButton);

        deleteButton =
                findViewById(R.id.deleteButton);

        likeButton =
                findViewById(R.id.likeButton);

        replyButton =
                findViewById(R.id.replyButton);

        usernameText =
                findViewById(R.id.usernameText);

        timeText =
                findViewById(R.id.timeText);

        viewsText =
                findViewById(R.id.viewsText);

        captionText =
                findViewById(R.id.captionText);

        progressBar =
                findViewById(R.id.storyProgress);

        closeButton.setOnClickListener(v ->
                finish()
        );

        likeButton.setOnClickListener(v ->
                toggleLike()
        );

        replyButton.setOnClickListener(v ->
                showReplyDialog()
        );

        deleteButton.setOnClickListener(v ->
                confirmDelete()
        );
    }

    // =========================================================
    // LOAD FIREBASE STORIES
    // =========================================================

    private void loadStories() {

        firebaseHelper.getActiveStories(
                new StoryFirebaseHelper.StoriesCallback() {

                    @Override
                    public void onSuccess(
                            List<Story> loadedStories) {

                        stories = loadedStories;

                        if (stories == null ||
                                stories.isEmpty()) {

                            Toast.makeText(
                                    StoryActivity.this,
                                    "Koi active Story nahi hai",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                            return;
                        }

                        if (currentPosition < 0 ||
                                currentPosition >= stories.size()) {

                            currentPosition = 0;
                        }

                        showCurrentStory();
                    }

                    @Override
                    public void onError(
                            String message) {

                        Toast.makeText(
                                StoryActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    }
                }
        );
    }

    // =========================================================
    // SHOW CURRENT STORY
    // =========================================================

    private void showCurrentStory() {

        if (stories == null ||
                stories.isEmpty()) {
            finish();
            return;
        }

        if (currentPosition < 0 ||
                currentPosition >= stories.size()) {
            finish();
            return;
        }

        currentStory =
                stories.get(currentPosition);

        liked = false;

        usernameText.setText(
                currentStory.getUsername()
        );

        captionText.setText(
                currentStory.getCaption()
        );

        viewsText.setText(
                String.valueOf(
                        currentStory.getViews()
                )
        );

        timeText.setText(
                getTimeText(
                        currentStory.getCreatedAt()
                )
        );

        deleteButton.setVisibility(
                currentStory.isOwnStory()
                        ? View.VISIBLE
                        : View.GONE
        );

        likeButton.setImageResource(
                android.R.drawable.btn_star_big_off
        );

        loadStoryImage();

        addView();

        startProgress();
    }

    // =========================================================
    // LOAD STORY IMAGE
    // =========================================================

    private void loadStoryImage() {

        String image =
                currentStory.getStoryImage();

        if (image == null ||
                image.trim().isEmpty()) {

            storyImage.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );

            return;
        }

        if (image.startsWith("http://") ||
                image.startsWith("https://")) {

            FirebaseStorage
                    .getInstance()
                    .getReferenceFromUrl(image)
                    .getBytes(5 * 1024 * 1024)
                    .addOnSuccessListener(bytes -> {

                        android.graphics.Bitmap bitmap =
                                android.graphics.BitmapFactory
                                        .decodeByteArray(
                                                bytes,
                                                0,
                                                bytes.length
                                        );

                        if (bitmap != null) {
                            storyImage.setImageBitmap(
                                    bitmap
                            );
                        }

                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    StoryActivity.this,
                                    "Story image load nahi hui",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );

        } else {

            int resourceId =
                    getResources()
                            .getIdentifier(
                                    image,
                                    "drawable",
                                    getPackageName()
                            );

            if (resourceId != 0) {

                storyImage.setImageResource(
                        resourceId
                );

            } else {

                storyImage.setImageResource(
                        android.R.drawable.ic_menu_gallery
                );
            }
        }
    }

    // =========================================================
    // UNIQUE VIEW
    // =========================================================

    private void addView() {

        firebaseHelper.addStoryView(
                currentStory.getId(),
                new StoryFirebaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        int oldViews =
                                currentStory.getViews();

                        currentStory.setViews(
                                oldViews + 1
                        );

                        viewsText.setText(
                                String.valueOf(
                                        currentStory.getViews()
                                )
                        );
                    }

                    @Override
                    public void onError(
                            String message) {
                        // View failure ko user ko disturb
                        // nahi karna.
                    }
                }
        );
    }

    // =========================================================
    // LIKE / UNLIKE
    // =========================================================

    private void toggleLike() {

        liked = !liked;

        final boolean newLikeState =
                liked;

        likeButton.setEnabled(false);

        firebaseHelper.toggleStoryLike(
                currentStory.getId(),
                newLikeState,
                new StoryFirebaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        likeButton.setEnabled(true);

                        if (newLikeState) {

                            likeButton.setImageResource(
                                    android.R.drawable.btn_star_big_on
                            );

                        } else {

                            likeButton.setImageResource(
                                    android.R.drawable.btn_star_big_off
                            );
                        }
                    }

                    @Override
                    public void onError(
                            String message) {

                        likeButton.setEnabled(true);

                        liked = !newLikeState;

                        Toast.makeText(
                                StoryActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // =========================================================
    // REPLY
    // =========================================================

    private void showReplyDialog() {

        EditText input =
                new EditText(this);

        input.setHint(
                "Reply likhein..."
        );

        input.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES |
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        input.setMaxLines(4);

        int padding =
                (int) (16 *
                        getResources()
                                .getDisplayMetrics()
                                .density);

        input.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Story Reply")
                        .setView(input)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Send",
                                null
                        )
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener(v -> {

                        String text =
                                input.getText()
                                        .toString()
                                        .trim();

                        if (text.isEmpty()) {

                            input.setError(
                                    "Reply likhein"
                            );

                            return;
                        }

                        firebaseHelper.addStoryReply(
                                currentStory.getId(),
                                text,
                                new StoryFirebaseHelper.ActionCallback() {

                                    @Override
                                    public void onSuccess() {

                                        Toast.makeText(
                                                StoryActivity.this,
                                                "Reply sent",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onError(
                                            String message) {

                                        Toast.makeText(
                                                StoryActivity.this,
                                                message,
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                        );
                    });
                }
        );

        dialog.show();
    }

    // =========================================================
    // DELETE CONFIRMATION
    // =========================================================

    private void confirmDelete() {

        if (currentStory == null ||
                !currentStory.isOwnStory()) {

            Toast.makeText(
                    this,
                    "Aap sirf apni Story delete kar sakte hain",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Story?")
                .setMessage(
                        "Kya aap ye Story delete karna chahte hain?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteCurrentStory()
                )
                .show();
    }

    // =========================================================
    // DELETE STORY
    // =========================================================

    private void deleteCurrentStory() {

        String imagePath =
                getStoragePath(
                        currentStory.getStoryImage()
                );

        firebaseHelper.deleteStory(
                currentStory.getId(),
                imagePath,
                new StoryFirebaseHelper.UploadCallback() {

                    @Override
                    public void onSuccess(
                            String storyId) {

                        Toast.makeText(
                                StoryActivity.this,
                                "Story deleted",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    }

                    @Override
                    public void onError(
                            String message) {

                        Toast.makeText(
                                StoryActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // =========================================================
    // FIREBASE STORAGE PATH
    // =========================================================

    private String getStoragePath(
            String imageUrl) {

        if (imageUrl == null ||
                imageUrl.trim().isEmpty()) {
            return "";
        }

        try {

            android.net.Uri uri =
                    android.net.Uri.parse(
                            imageUrl
                    );

            String path =
                    uri.getPath();

            if (path == null) {
                return "";
            }

            if (path.startsWith("/v0/b/")) {

                int oIndex =
                        path.indexOf(
                                "/o/"
                        );

                if (oIndex != -1) {

                    path =
                            path.substring(
                                    oIndex + 3
                            );
                }
            }

            return path
                    .replace(
                            "%2F",
                            "/"
                    )
                    .replace(
                            "%20",
                            " "
                    );

        } catch (Exception e) {

            return "";
        }
    }

    // =========================================================
    // PROGRESS / AUTO CLOSE
    // =========================================================

    private void startProgress() {

        if (progressBar == null) {
            return;
        }

        progressBar.setProgress(0);

        progressBar.post(() -> {

            progressBar.setMax(150);

            final int[] progress =
                    {0};

            final Runnable runnable =
                    new Runnable() {

                        @Override
                        public void run() {

                            progress[0]++;

                            progressBar.setProgress(
                                    progress[0]
                            );

                            if (progress[0] < 150) {

                                handler.postDelayed(
                                        this,
                                        100
                                );

                            }
                        }
                    };

            handler.removeCallbacksAndMessages(
                    null
            );

            handler.post(runnable);
        });

        handler.postDelayed(
                autoCloseRunnable,
                15000
        );
    }

    // =========================================================
    // TIME
    // =========================================================

    private String getTimeText(
            long createdAt) {

        long difference =
                System.currentTimeMillis()
                        - createdAt;

        long minutes =
                difference /
                        (60L * 1000L);

        if (minutes < 1) {
            return "Just now";
        }

        if (minutes < 60) {
            return minutes + " min ago";
        }

        long hours =
                minutes / 60L;

        if (hours < 24) {
            return hours + " hr ago";
        }

        return "24h+";
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    protected void onDestroy() {

        handler.removeCallbacksAndMessages(
                null
        );

        super.onDestroy();
    }
}
