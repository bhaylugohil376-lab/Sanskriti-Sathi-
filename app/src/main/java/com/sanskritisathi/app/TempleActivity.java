package com.sanskritisathi.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TempleActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TempleAdapter adapter;
    private ArrayList<Temple> templeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temple);

        recyclerView = findViewById(R.id.templeRecyclerView);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        templeList = new ArrayList<>();

        templeList.add(new Temple(
                "Somnath Temple",
                "Somnath Temple is one of the twelve Jyotirlinga shrines of Lord Shiva, located at Prabhas Patan in Gujarat.",
                R.drawable.somnath
        ));

        adapter = new TempleAdapter(this, templeList);
        recyclerView.setAdapter(adapter);
    }
}
