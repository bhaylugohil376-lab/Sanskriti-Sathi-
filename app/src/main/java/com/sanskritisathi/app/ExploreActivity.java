package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ExploreActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_explore);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.categoryCulture).setOnClickListener(v -> startActivity(new android.content.Intent(this, TempleActivity.class)));
        findViewById(R.id.categoryHistory).setOnClickListener(v -> startActivity(new android.content.Intent(this, RajaActivity.class)));
        findViewById(R.id.categoryKnowledge).setOnClickListener(v -> startActivity(new android.content.Intent(this, GitaActivity.class)));
        findViewById(R.id.categoryFestivals).setOnClickListener(v -> startActivity(new android.content.Intent(this, DeviDevtaActivity.class)));
    }
}
