package com.sanskritisathi.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private RecyclerView homeFeedRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // =========================
        // HOME FEED
        // =========================

        homeFeedRecyclerView =
                findViewById(R.id.homeFeedRecyclerView);

        homeFeedRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        homeFeedRecyclerView.setHasFixedSize(false);

        CulturePostAdapter feedAdapter =
                new CulturePostAdapter(
                        this,
                        CulturePostData.getAllPosts()
                );

        homeFeedRecyclerView.setAdapter(feedAdapter);


        // =========================
        // STORY / CULTURE BUTTONS
        // =========================

        findViewById(R.id.templeStoryButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        TempleActivity.class
                                )
                        )
                );

        findViewById(R.id.rajaStoryButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        RajaActivity.class
                                )
                        )
                );

        findViewById(R.id.deviStoryButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        DeviDevtaActivity.class
                                )
                        )
                );

        findViewById(R.id.gitaStoryButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        GitaActivity.class
                                )
                        )
                );

        findViewById(R.id.festivalStoryButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        RssActivity.class
                                )
                        )
                );


        // =========================
        // YOUR STORY
        // =========================

        findViewById(R.id.yourStoryButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        StoryUploadActivity.class
                                )
                        )
                );


        // =========================
        // TOP CREATE
        // =========================

        findViewById(R.id.createTopButton)
                .setOnClickListener(v ->
                        showCreateMenu()
                );


        // =========================
        // NOTIFICATIONS
        // =========================

        findViewById(R.id.notificationButton)
                .setOnClickListener(v ->
                        Toast.makeText(
                                MainActivity.this,
                                "Notifications next step mein add honge.",
                                Toast.LENGTH_SHORT
                        ).show()
                );


        // =========================
        // BOTTOM HOME
        // =========================

        findViewById(R.id.homeNavButton)
                .setOnClickListener(v ->
                        homeFeedRecyclerView
                                .smoothScrollToPosition(0)
                );


        // =========================
        // REELS
        // =========================

        findViewById(R.id.reelsNavButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        ReelActivity.class
                                )
                        )
                );


        // =========================
        // BOTTOM CREATE
        // =========================

        findViewById(R.id.createNavButton)
                .setOnClickListener(v ->
                        showCreateMenu()
                );


        // =========================
        // SEARCH
        // =========================

        findViewById(R.id.searchNavButton)
                .setOnClickListener(v ->
                        Toast.makeText(
                                MainActivity.this,
                                "Search next step mein add hoga.",
                                Toast.LENGTH_SHORT
                        ).show()
                );


        // =========================
        // PROFILE / LOGIN
        // =========================

        findViewById(R.id.profileNavButton)
                .setOnClickListener(v -> {

                    FirebaseAuth auth =
                            FirebaseAuth.getInstance();

                    // User Login nahi hai
                    if (auth.getCurrentUser() == null) {

                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        LoginActivity.class
                                )
                        );

                    } else {

                        // User already Login hai
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        ProfileActivity.class
                                )
                        );
                    }
                });
    }


    // =========================
    // CREATE MENU
    // =========================

    private void showCreateMenu() {

        String[] options = {
                "🎬 Create Reel",
                "📸 Create Post",
                "⭕ Create Story"
        };

        new AlertDialog.Builder(this)
                .setTitle("Create")
                .setItems(
                        options,
                        (dialog, which) -> {

                            if (which == 0) {

                                // CREATE REEL

                                startActivity(
                                        new Intent(
                                                MainActivity.this,
                                                ReelUploadActivity.class
                                        )
                                );

                            } else if (which == 1) {

                                // CREATE POST

                                Toast.makeText(
                                        MainActivity.this,
                                        "Post creation next step mein add hoga.",
                                        Toast.LENGTH_SHORT
                                ).show();

                            } else {

                                // CREATE STORY

                                startActivity(
                                        new Intent(
                                                MainActivity.this,
                                                StoryUploadActivity.class
                                        )
                                );
                            }
                        }
                )
                .show();
    }
}
