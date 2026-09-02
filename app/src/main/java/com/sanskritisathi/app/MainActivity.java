package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.rajaButton).setOnClickListener(v ->
                startActivity(new Intent(this, RajaActivity.class)));

        findViewById(R.id.templeButton).setOnClickListener(v ->
                startActivity(new Intent(this, TempleActivity.class)));

        findViewById(R.id.deviDevtaButton).setOnClickListener(v ->
                startActivity(new Intent(this, DeviDevtaActivity.class)));

        findViewById(R.id.gitaButton).setOnClickListener(v ->
                startActivity(new Intent(this, GitaActivity.class)));

        findViewById(R.id.rssButton).setOnClickListener(v ->
                startActivity(new Intent(this, RssActivity.class)));

        findViewById(R.id.aboutButton).setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));

        findViewById(R.id.cultureButton).setOnClickListener(v ->
                startActivity(new Intent(this, CultureActivity.class)));
    }
}
