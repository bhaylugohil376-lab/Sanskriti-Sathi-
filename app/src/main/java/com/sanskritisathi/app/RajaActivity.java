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

        rajaList.add(new Raja(
                "Rama",
                "King of Ayodhya and an important figure in the Ramayana.",
                R.drawable.rama
        ));

        rajaList.add(new Raja(
                "Dasharatha",
                "King of Ayodhya and father of Rama.",
                R.drawable.dasharatha
        ));

        rajaList.add(new Raja(
                "Janaka",
                "Wise King of Mithila and father of Sita.",
                R.drawable.janaka
        ));

        rajaList.add(new Raja(
                "Harishchandra",
                "Legendary king remembered for truth and righteousness.",
                R.drawable.harishchandra
        ));

        rajaList.add(new Raja(
                "Yudhishthira",
                "Eldest of the Pandavas and King of Hastinapura.",
                R.drawable.yudhishthira
        ));

        rajaList.add(new Raja(
                "Krishna",
                "A central figure in the Mahabharata and revered as a divine teacher.",
                R.drawable.krishna
        ));

        rajaList.add(new Raja(
                "Chandragupta Maurya",
                "Founder of the Maurya Empire.",
                R.drawable.chandragupta
        ));

        rajaList.add(new Raja(
                "Ashoka",
                "Mauryan emperor known for his support of dhamma.",
                R.drawable.ashoka
        ));

        rajaList.add(new Raja(
                "Samudragupta",
                "Gupta emperor remembered for his military campaigns and patronage of culture.",
                R.drawable.samudragupta
        ));

        rajaList.add(new Raja(
                "Vikramaditya",
                "A celebrated traditional Indian king associated with learning and justice.",
                R.drawable.vikramaditya
        ));

        adapter = new RajaAdapter(this, rajaList);
        recyclerView.setAdapter(adapter);
    }
}
