package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    private ImageView rajaImage;
    private TextView rajaName, rajaDynasty, rajaPeriod, rajaKingdom, rajaCapital, rajaHistory, rajaWars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // 1. All Views ko XML IDs se initialize karein
        rajaImage = findViewById(R.id.rajaImage);
        rajaName = findViewById(R.id.rajaName);
        rajaDynasty = findViewById(R.id.rajaDynasty);
        rajaPeriod = findViewById(R.id.rajaPeriod);
        rajaKingdom = findViewById(R.id.rajaKingdom);
        rajaCapital = findViewById(R.id.rajaCapital);
        rajaHistory = findViewById(R.id.rajaHistory);
        rajaWars = findViewById(R.id.rajaWars);

        // 2. Intent se Data get karke Views me set karein
        loadDataFromIntent();
    }

    private void loadDataFromIntent() {
        if (getIntent() != null) {
            // Text data receive karein
            String name = getIntent().getStringExtra("name");
            String dynasty = getIntent().getStringExtra("dynasty");
            String period = getIntent().getStringExtra("period");
            String kingdom = getIntent().getStringExtra("kingdom");
            String capital = getIntent().getStringExtra("capital");
            String history = getIntent().getStringExtra("history");
            String wars = getIntent().getStringExtra("wars");
            
            // Image drawable ID receive karein (Default value 0)
            int imageResId = getIntent().getIntExtra("imageRes", 0);

            // Set Data to Views (Null Safety check ke saath)
            if (name != null) rajaName.setText(name);
            if (dynasty != null) rajaDynasty.setText("वंश: " + dynasty);
            if (period != null) rajaPeriod.setText("काल: " + period);
            if (kingdom != null) rajaKingdom.setText("राज्य: " + kingdom);
            if (capital != null) rajaCapital.setText("राजधानी: " + capital);
            if (history != null) rajaHistory.setText("इतिहास:\n" + history);
            if (wars != null) rajaWars.setText("⚔️ युद्ध एवं संघर्ष:\n" + wars);

            if (imageResId != 0) {
                rajaImage.setImageResource(imageResId);
            }
        }
    }
}
