package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

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

        // 1. RecyclerView initialize karein
        recyclerView = findViewById(R.id.rajaRecyclerView);

        // 2. LayoutManager set karein
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 3. Data load karein
        rajaList = new ArrayList<>(RajaData.getAllRajas());

        // 4. Adapter set karein
        adapter = new RajaAdapter(this, rajaList);
        recyclerView.setAdapter(adapter);
    }
}
