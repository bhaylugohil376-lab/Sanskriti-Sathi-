package com.sanskritisathi.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RajaActivity extends AppCompatActivity {

    private RecyclerView rajaRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_raja);

        rajaRecyclerView = findViewById(R.id.rajaRecyclerView);

        rajaRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        List<Raja> rajaList = RajaData.getAllRajas();

        RajaAdapter adapter = new RajaAdapter(rajaList);

        rajaRecyclerView.setAdapter(adapter);
    }
}
