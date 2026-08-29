package com.sanskritisathi.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RajaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RajaAdapter adapter;
    private ArrayList<Raja> rajaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_raja);

        recyclerView = findViewById(R.id.rajaRecyclerView);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        // RajaData से सभी राजाओं की जानकारी
        rajaList = new ArrayList<>(
                RajaData.getAllRajas()
        );

        // Adapter
        adapter = new RajaAdapter(
                this,
                rajaList
        );

        recyclerView.setAdapter(adapter);
    }
}
