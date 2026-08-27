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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        rajaList = new ArrayList<>();

        rajaList.add(new Raja("Rama", "King of Ayodhya"));
        rajaList.add(new Raja("Krishna", "King and divine guide"));
        rajaList.add(new Raja("Yudhishthira", "King of Hastinapura"));
        rajaList.add(new Raja("Arjuna", "Great warrior of the Pandavas"));
        rajaList.add(new Raja("Bhima", "Powerful Pandava warrior"));
        rajaList.add(new Raja("Dasharatha", "King of Ayodhya and father of Rama"));
        rajaList.add(new Raja("Janaka", "Wise King of Mithila"));
        rajaList.add(new Raja("Harishchandra", "Famous for truth and righteousness"));

        adapter = new RajaAdapter(this, rajaList);
        recyclerView.setAdapter(adapter);
    }
}
