package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FiltersActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_filters);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        int[] ids = {R.id.filterNormal,R.id.filterBright,R.id.filterVivid,R.id.filterWarm};
        for (int id : ids) findViewById(id).setOnClickListener(v -> {
            for (int x : ids) findViewById(x).setSelected(false);
            v.setSelected(true);
        });
    }
}
