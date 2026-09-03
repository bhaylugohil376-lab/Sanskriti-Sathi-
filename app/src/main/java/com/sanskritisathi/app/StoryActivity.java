package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class StoryActivity extends AppCompatActivity {

    private ImageView storyImage;
    private TextView storyUsername;
    private TextView storyTime;
    private TextView storyViews;
    private TextView storyCaption;

    private ImageButton likeButton;
    private ImageButton replyButton;
    private ImageButton deleteButton;
    private ImageButton closeButton;

    private Story currentStory;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable autoClose = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_story);

        storyImage = findViewById(R.id.storyImage);
        storyUsername = findViewById(R.id.storyUsername);
        storyTime = findViewById(R.id.storyTime);
        storyViews = findViewById(R.id.storyViews);
        storyCaption = findViewById(R.id.storyCaption);

        likeButton = findViewById(R.id.storyLikeButton);
        replyButton = findViewById(R.id.storyReplyButton);
        deleteButton = findViewById(R.id.storyDeleteButton);
        closeButton = findViewById(R.id.storyCloseButton);

        int position = getIntent().getIntExtra("story_position", 0);

        List<Story> stories = StoryData.getActiveStories();

        if (stories.isEmpty() || position < 0 || position >= stories.size()) {
            Toast.makeText(this, "Story available nahi hai", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentStory = stories.get(position);

        showStory();

        likeButton.setOnClickListener(v -> {

            currentStory.setLiked(!currentStory.isLiked());

            if (currentStory.isLiked()) {
                likeButton.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                likeButton.setImageResource(android.R.drawable.btn_star_big_off);
            }
        });

        replyButton.setOnClickListener(v -> showReplyDialog());

        deleteButton.setOnClickListener(v -> confirmDelete());

        closeButton.setOnClickListener(v -> finish());

        // Story automatically closes after 15 seconds.
        handler.postDelayed(autoClose, 15000);
    }

    private void showStory() {

        storyUsername.setText(currentStory.getUsername());

        storyCaption.setText(currentStory.getCaption());

        storyViews.setText("👁 " + currentStory.getViews() + " views");

        storyTime.setText(getTimeText(currentStory.getCreatedAt()));

        int imageRes = getDrawableResource(
                currentStory.getStoryImage()
        );

        if (imageRes != 0) {
            storyImage.setImageResource(imageRes);
        }

        // Delete button only appears for owner's Story.
        if (currentStory.isOwnStory()) {
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.GONE);
        }
    }

    private String getTimeText(long createdAt) {

        long difference = System.currentTimeMillis() - createdAt;

        long minutes = difference / (60 * 1000);

        if (minutes < 1) {
            return "Just now";
        }

        if (minutes < 60) {
            return minutes + " min ago";
        }

        long hours = minutes / 60;

        if (hours < 24) {
            return hours + " hr ago";
        }

        return "24 hr ago";
    }

    private int getDrawableResource(String name) {

        if (name == null) {
            return 0;
        }

        return getResources().getIdentifier(
                name,
                "drawable",
                getPackageName()
        );
    }

    private void showReplyDialog() {

        final EditText input = new EditText(this);

        input.setHint("Reply likhiye...");

        input.setSingleLine(false);

        int padding = 40;

        input.setPadding(
                padding,
                padding / 2,
                padding,
                padding / 2
        );

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Reply")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Send", null)
                .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String reply = input.getText()
                                .toString()
                                .trim();

                        if (reply.isEmpty()) {
                            input.setError("Reply likhiye");
                            return;
                        }

                        Toast.makeText(
                                this,
                                "Reply sent",
                                Toast.LENGTH_SHORT
                        ).show();

                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    private void confirmDelete() {

        if (!currentStory.isOwnStory()) {
            Toast.makeText(
                    this,
                    "Aap sirf apni Story delete kar sakte hain",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Story?")
                .setMessage("Kya aap apni Story delete karna chahte hain?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {

                    StoryData.deleteStory(currentStory.getId());

                    Toast.makeText(
                            this,
                            "Story deleted",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .show();
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacks(autoClose);

        super.onDestroy();
    }
}
