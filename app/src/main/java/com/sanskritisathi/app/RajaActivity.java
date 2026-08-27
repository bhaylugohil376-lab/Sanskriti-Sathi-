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

        rajaList = new ArrayList<>();

        // Ancient Indian Kings
        rajaList.add(new Raja(
                "Rama",
                "King of Ayodhya and an important figure in the Ramayana."
        ));

        rajaList.add(new Raja(
                "Dasharatha",
                "King of Ayodhya and father of Rama."
        ));

        rajaList.add(new Raja(
                "Janaka",
                "Wise King of Mithila and father of Sita."
        ));

        rajaList.add(new Raja(
                "Harishchandra",
                "Legendary king remembered for his commitment to truth and righteousness."
        ));

        rajaList.add(new Raja(
                "Yudhishthira",
                "Eldest of the Pandavas and King of Hastinapura."
        ));

        rajaList.add(new Raja(
                "Krishna",
                "A central figure in the Mahabharata and revered as a divine teacher."
        ));

        rajaList.add(new Raja(
                "Chandragupta Maurya",
                "Founder of the Maurya Empire and one of ancient India's major rulers."
        ));

        rajaList.add(new Raja(
                "Ashoka",
                "Mauryan emperor known for his later support of Buddhism and dhamma."
        ));

        rajaList.add(new Raja(
                "Samudragupta",
                "Gupta emperor remembered for his military campaigns and patronage of culture."
        ));

        rajaList.add(new Raja(
                "Vikramaditya",
                "A celebrated traditional Indian king associated with learning, justice and cultural heritage."
        ));

        adapter = new RajaAdapter(this, rajaList);
        recyclerView.setAdapter(adapter);
    }
}
