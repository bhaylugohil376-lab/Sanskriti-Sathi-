package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TempleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);

        textView.setText(
                "🛕 Sanskriti Sathi\n\n" +
                "Mandir aur Dharmik Sthal\n\n" +
                "Yahan Bharat ke prasiddh mandiron, " +
                "devi-devtaon aur dharmik sthalon ki jankari milegi."
        );

        textView.setTextSize(20);
        textView.setPadding(40, 60, 40, 40);

        setContentView(textView);
    }
}
