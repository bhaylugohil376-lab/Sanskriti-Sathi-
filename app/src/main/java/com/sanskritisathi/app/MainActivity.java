package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView homeFeedRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // =========================
        // HOME FEED
        // =========================

        homeFeedRecyclerView = findViewById(R.id.homeFeedRecyclerView);

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
        // STORY BUTTONS
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
                        Toast.makeText(
                                MainActivity.this,
                                "Story feature — next step",
                                Toast.LENGTH_SHORT
                        ).show()
                );


        // =========================
        // TOP CREATE
        // =========================

        findViewById(R.id.createTopButton)
                .setOnClickListener(v ->
                        Toast.makeText(
                                MainActivity.this,
                                "Create Post / Reel / Story",
                                Toast.LENGTH_SHORT
                        ).show()
                );


        // =========================
        // NOTIFICATIONS
        // =========================

        findViewById(R.id.notificationButton)
                .setOnClickListener(v ->
                        Toast.makeText(
                                MainActivity.this,
                                "Notifications — next step",
                                Toast.LENGTH_SHORT
                        ).show()
                );


        // =========================
        // BOTTOM HOME
        // =========================

        findViewById(R.id.homeNavButton)
                .setOnClickListener(v ->
                        homeFeedRecyclerView.smoothScrollToPosition(0)
                );


        // =========================
        // REELS
        // =========================

        findViewById(R.id.reelsNavButton)
                .setOnClickListener(v ->
                        Toast.makeText(
                                MainActivity.this,
                                "Reels — next step",
                                Toast.LENGTH_SHORT
                        ).show()
                );


        // =========================
        // CREATE
        // =========================

        findViewById(R.id.createNavButton)
                .setOnClickListener(v ->
                        Toast.makeText(
                                MainActivity.this,
                                "Create Post / Reel / Story",
                                Toast.LENGTH_SHORT
                        ).show()
                );


        // =========================
        // SEARCH
        // =========================

        findViewById(R.id.searchNavButton)
                .setOnClickListener(v ->
                        Toast.makeText(
                                MainActivity.this,
                                "Search — next step",
                                Toast.LENGTH_SHORT
                        ).show()
                );


        // =========================
        // PROFILE
        // =========================

        findViewById(R.id.profileNavButton)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(
                                        MainActivity.this,
                                        ProfileActivity.class
                                )
                        )
                );
    }
}
