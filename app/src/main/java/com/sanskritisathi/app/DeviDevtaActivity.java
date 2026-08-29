package com.sanskritisathi.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviDevtaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DeviDevtaAdapter adapter;
    private List<DeviDevta> deviDevtaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_devi_devta);

        recyclerView = findViewById(
                R.id.deviDevtaRecyclerView
        );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setHasFixedSize(false);

        deviDevtaList =
                DeviDevtaData.getAllDeviDevta();

        adapter = new DeviDevtaAdapter(
                this,
                deviDevtaList
        );

        recyclerView.setAdapter(adapter);
    }
}
