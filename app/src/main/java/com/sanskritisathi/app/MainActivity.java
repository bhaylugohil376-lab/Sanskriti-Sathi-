package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 👑 Raja
        findViewById(R.id.rajaButton).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RajaActivity.class)));

        // 🛕 Temple
        findViewById(R.id.templeButton).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TempleActivity.class)));

        // 🕉️ Devi-Devta
        findViewById(R.id.deviDevtaButton).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, DeviDevtaActivity.class)));

        // 📖 Bhagavad Gita
        findViewById(R.id.gitaButton).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, GitaActivity.class)));

        // 📰 RSS News
        findViewById(R.id.rssButton).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RssActivity.class)));

        // ℹ️ About
        findViewById(R.id.aboutButton).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AboutActivity.class)));

        // 📱 Culture Posts
        findViewById(R.id.cultureButton).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CultureActivity.class)));
    }
}
