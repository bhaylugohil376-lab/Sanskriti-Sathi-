package com.sanskritisathi.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class ProfessionalDashboardActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_professional_dashboard);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
}
