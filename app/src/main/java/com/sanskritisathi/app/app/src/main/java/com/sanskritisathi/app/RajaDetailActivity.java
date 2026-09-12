package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RajaDetailActivity extends AppCompatActivity {

    private TextView rajaNameTextView;
    private TextView rajaDynastyTextView;
    private TextView rajaHistoryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_raja_detail);

        rajaNameTextView = findViewById(R.id.rajaName);
        rajaDynastyTextView = findViewById(R.id.rajaDynasty);
        rajaHistoryTextView = findViewById(R.id.rajaHistory);

        String rajaName =
                getIntent().getStringExtra("raja_name");

        String rajaDynasty =
                getIntent().getStringExtra("raja_dynasty");

        String rajaHistory =
                getIntent().getStringExtra("raja_history");

        if (rajaName != null) {
            rajaNameTextView.setText(rajaName);
        }

        if (rajaDynasty != null) {
            rajaDynastyTextView.setText(
                    "वंश: " + rajaDynasty
            );
        }

        if (rajaHistory != null) {
            rajaHistoryTextView.setText(
                    rajaHistory
            );
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("राजा विवरण");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
