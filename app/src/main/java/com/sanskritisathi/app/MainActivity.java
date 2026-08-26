package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setText("🙏 Sanskriti Sathi");
        textView.setTextSize(28);
        textView.setGravity(android.view.Gravity.CENTER);

        setContentView(textView);
    }
}
