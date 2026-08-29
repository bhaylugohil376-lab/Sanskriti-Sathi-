package com.sanskritisathi.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TempleActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TempleAdapter adapter;
    private List<Temple> templeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_temple);

        recyclerView = findViewById(
                R.id.templeRecyclerView
        );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setHasFixedSize(false);

        templeList = TempleData.getAllTemples();

        adapter = new TempleAdapter(
                this,
                templeList
        );

        recyclerView.setAdapter(adapter);
    }
}
