package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_search);
        EditText search = findViewById(R.id.searchInput);
        if (search != null) search.requestFocus();
    }
}
