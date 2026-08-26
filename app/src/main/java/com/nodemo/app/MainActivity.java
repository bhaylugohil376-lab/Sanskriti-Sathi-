package com.nodemo.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button templeButton = findViewById(R.id.templeButton);
        Button videoButton = findViewById(R.id.videoButton);
        Button aboutButton = findViewById(R.id.aboutButton);
        Button privacyButton = findViewById(R.id.privacyButton);

        TextView content = findViewById(R.id.contentText);

        templeButton.setOnClickListener(v ->
                content.setText("🛕 Hindu Temples\n\nExplore temple names, history, timings and useful information.")
        );

        videoButton.setOnClickListener(v ->
                content.setText("🎥 Video Upload\n\nVideo upload feature will be connected in the next step.")
        );

        aboutButton.setOnClickListener(v ->
                content.setText("ℹ️ About NoDemo\n\nNoDemo is an original learning and information app for exploring temples, educational content, videos and useful resources.")
        );

        privacyButton.setOnClickListener(v ->
                content.setText("🔒 Privacy Policy\n\nNoDemo respects your privacy. The app will only request permissions that are needed for its features.")
        );
    }
}
