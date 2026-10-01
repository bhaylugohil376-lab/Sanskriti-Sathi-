package com.sanskritisathi.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationsActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_notifications);
        findViewById(R.id.notificationsBackButton).setOnClickListener(v -> finish());
    }
}
