package com.sanskritisathi.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

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

        currentPosition =
                getIntent().getIntExtra(
                        "story_position",
                        0
                );

        loadStories();
    }

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

        closeButton.setOnClickListener(
                v -> finish()
        );

        likeButton.setOnClickListener(
                v -> toggleLike()
        );

        replyButton.setOnClickListener(
                v -> showReplyDialog()
        );

        deleteButton.setOnClickListener(
                v -> confirmDelete()
        );
    }

    // =========================================================
    // LOAD STORIES
    // =========================================================

    private void loadStories() {

        StorySupabaseHelper.getActiveStories(
                this,
                new StorySupabaseHelper.StoriesCallback() {

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
    // SHOW STORY
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
                safeText(
                        currentStory.getUsername(),
                        "Sanskriti User"
                )
        );

        captionText.setText(
                safeText(
                        currentStory.getCaption(),
                        ""
                )
        );

        viewsText.setText(
                String.valueOf(
                        Math.max(
                                0,
                                currentStory.getViews()
                        )
                )
        );

        timeText.setText(
                getTimeText(
                        currentStory.getCreatedAt()
                )
        );

        deleteButton.setVisibility(
                currentStory.isOwnStory()
                        ? ImageButton.VISIBLE
                        : ImageButton.GONE
        );

        likeButton.setImageResource(
                android.R.drawable.btn_star_big_off
        );

        loadStoryImage();

        addView();

        checkLike();

        startProgress();
    }

    // =========================================================
    // IMAGE
    // =========================================================

    private void loadStoryImage() {

        String imageUrl =
                currentStory.getStoryImage();

        if (imageUrl == null ||
                imageUrl.trim().isEmpty()) {

            storyImage.setImageResource(
                    android.R.drawable.ic_menu_gallery
            );

            return;
        }

        if (imageUrl.startsWith("http://") ||
                imageUrl.startsWith("https://")) {

            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(
                            android.R.drawable.ic_menu_gallery
                    )
                    .error(
                            android.R.drawable.ic_menu_gallery
                    )
                    .into(storyImage);

        } else {

            int resourceId =
                    getResources()
                            .getIdentifier(
                                    imageUrl,
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
    // VIEW
    // =========================================================

    private void addView() {

        StorySupabaseHelper.addStoryView(
                this,
                currentStory.getId(),
                new StorySupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        int views =
                                currentStory.getViews();

                        currentStory.setViews(
                                views + 1
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
                        // View failure intentionally ignored.
                    }
                }
        );
    }

    // =========================================================
    // CHECK LIKE
    // =========================================================

    private void checkLike() {

        StorySupabaseHelper.checkStoryLike(
                this,
                currentStory.getId(),
                new StorySupabaseHelper.LikeCheckCallback() {

                    @Override
                    public void onResult(
                            boolean isLiked) {

                        liked = isLiked;

                        likeButton.setImageResource(
                                liked
                                        ? android.R.drawable.btn_star_big_on
                                        : android.R.drawable.btn_star_big_off
                        );
                    }

                    @Override
                    public void onError(
                            String message) {
                        liked = false;
                    }
                }
        );
    }

    // =========================================================
    // LIKE / UNLIKE
    // =========================================================

    private void toggleLike() {

        if (currentStory == null) {
            return;
        }

        boolean newState =
                !liked;

        likeButton.setEnabled(false);

        StorySupabaseHelper.toggleStoryLike(
                this,
                currentStory.getId(),
                newState,
                new StorySupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        liked = newState;

                        likeButton.setImageResource(
                                liked
                                        ? android.R.drawable.btn_star_big_on
                                        : android.R.drawable.btn_star_big_off
                        );

                        likeButton.setEnabled(true);
                    }

                    @Override
                    public void onError(
                            String message) {

                        likeButton.setEnabled(true);

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

        input.setHint("Reply likhein...");

        input.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES |
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        input.setMaxLines(4);

        int padding =
                (int) (
                        16 *
                                getResources()
                                        .getDisplayMetrics()
                                        .density
                );

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
                ignored -> {

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

                        if (text.length() > 1000) {

                            input.setError(
                                    "Reply bahut lamba hai"
                            );

                            return;
                        }

                        StorySupabaseHelper.addStoryReply(
                                this,
                                currentStory.getId(),
                                text,
                                new StorySupabaseHelper.ActionCallback() {

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
    // DELETE CONFIRM
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
    // DELETE
    // =========================================================

    private void deleteCurrentStory() {

        if (currentStory == null) {
            return;
        }

        StorySupabaseHelper.deleteStory(
                this,
                currentStory.getId(),
                new StorySupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

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
    // PROGRESS
    // =========================================================

    private void startProgress() {

        handler.removeCallbacksAndMessages(null);

        if (progressBar == null) {
            return;
        }

        progressBar.setMax(150);
        progressBar.setProgress(0);

        final int[] progress = {0};

        Runnable progressRunnable =
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

        handler.post(progressRunnable);

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

        if (createdAt <= 0) {
            return "Just now";
        }

        long difference =
                System.currentTimeMillis()
                        - createdAt;

        if (difference < 0) {
            return "Just now";
        }

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
    // SAFE TEXT
    // =========================================================

    private String safeText(
            String value,
            String fallback) {

        if (value == null ||
                value.trim().isEmpty()) {

            return fallback;
        }

        return value;
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
