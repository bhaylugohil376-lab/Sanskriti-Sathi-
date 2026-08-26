package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RajaActivity extends AppCompatActivity {

    private RajaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raja);

        EditText searchRaja = findViewById(R.id.searchRaja);
        RecyclerView recyclerView = findViewById(R.id.rajaRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Raja> rajaList = RajaData.getAllRajas();

        adapter = new RajaAdapter(rajaList);
        recyclerView.setAdapter(adapter);

        // 🔍 Search
        searchRaja.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}
