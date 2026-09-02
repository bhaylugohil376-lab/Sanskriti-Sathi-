package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // =========================
        // CULTURE FEED - TEMPORARILY DISABLED
        // =========================
        // Feed ko test ke liye abhi load nahi kar rahe.


        // =========================
        // INDIAN KINGS
        // =========================

        Button rajaButton = findViewById(R.id.rajaButton);

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

        Button templeButton = findViewById(R.id.templeButton);

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

        Button gitaButton = findViewById(R.id.gitaButton);

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

        Button rssButton = findViewById(R.id.rssButton);

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

        Button aboutButton = findViewById(R.id.aboutButton);

        aboutButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AboutActivity.class
            );
            startActivity(intent);
        });
    }
}
