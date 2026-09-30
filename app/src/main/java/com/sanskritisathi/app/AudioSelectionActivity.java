package com.sanskritisathi.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class AudioSelectionActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_audio_selection);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
}
