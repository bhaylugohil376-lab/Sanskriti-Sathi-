package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView culturePostRecyclerView;
    private CulturePostAdapter culturePostAdapter;
    private ArrayList<CulturePost> culturePostList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // =========================
        // CULTURE FEED
        // =========================

        culturePostRecyclerView =
                findViewById(R.id.culturePostRecyclerView);

        culturePostRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        culturePostList =
                CulturePostData.getAllPosts();

        culturePostAdapter =
                new CulturePostAdapter(
                        this,
                        culturePostList
                );

        culturePostRecyclerView.setAdapter(
                culturePostAdapter
        );


        // =========================
        // INDIAN KINGS
        // =========================

        Button rajaButton =
                findViewById(R.id.rajaButton);

        rajaButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    RajaActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // TEMPLES
        // =========================

        Button templeButton =
                findViewById(R.id.templeButton);

        templeButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    TempleActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // BHAGAVAD GITA
        // =========================

        Button gitaButton =
                findViewById(R.id.gitaButton);

        gitaButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    GitaActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // DEVI & DEVTA
        // =========================

        Button deviDevtaButton =
                findViewById(R.id.deviDevtaButton);

        deviDevtaButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    DeviDevtaActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // RSS NEWS
        // =========================

        Button rssButton =
                findViewById(R.id.rssButton);

        rssButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    RssActivity.class
            );

            startActivity(intent);
        });


        // =========================
        // ABOUT
        // =========================

        Button aboutButton =
                findViewById(R.id.aboutButton);

        aboutButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    AboutActivity.class
            );

            startActivity(intent);
        });
    }
}
