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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        templeList = new ArrayList<>();

        templeList.add(new Temple(
                "Somnath Temple",
                "Prabhas Patan, Gujarat",
                "One of the important Hindu pilgrimage sites, traditionally associated with Bhagwan Shiva and the twelve Jyotirlingas.",
                R.drawable.somnath
        ));

        templeList.add(new Temple(
                "Dwarkadhish Temple",
                "Dwarka, Gujarat",
                "A major pilgrimage temple traditionally associated with Bhagwan Krishna and the sacred city of Dwarka.",
                R.drawable.dwarkadhish
        ));

        templeList.add(new Temple(
                "Kashi Vishwanath Temple",
                "Varanasi, Uttar Pradesh",
                "A major Shiva temple in Varanasi and one of the traditionally recognized twelve Jyotirlingas.",
                R.drawable.kashi_vishwanath
        ));

        templeList.add(new Temple(
                "Kedarnath Temple",
                "Kedarnath, Uttarakhand",
                "A famous Himalayan pilgrimage temple dedicated to Bhagwan Shiva and one of the twelve Jyotirlingas.",
                R.drawable.kedarnath
        ));

        templeList.add(new Temple(
                "Badrinath Temple",
                "Badrinath, Uttarakhand",
                "A major pilgrimage temple dedicated to Bhagwan Vishnu and an important site in the Char Dham tradition.",
                R.drawable.badrinath
        ));

        templeList.add(new Temple(
                "Jagannath Temple",
                "Puri, Odisha",
                "A major Vaishnava pilgrimage temple associated with Bhagwan Jagannath, a form of Bhagwan Krishna.",
                R.drawable.jagannath
        ));

        templeList.add(new Temple(
                "Tirumala Venkateswara Temple",
                "Tirupati, Andhra Pradesh",
                "A renowned temple dedicated to Bhagwan Venkateswara and one of India's major pilgrimage destinations.",
                R.drawable.tirupati
        ));

        templeList.add(new Temple(
                "Meenakshi Amman Temple",
                "Madurai, Tamil Nadu",
                "A historic temple complex dedicated to Goddess Meenakshi and Bhagwan Sundareswarar.",
                R.drawable.meenakshi
        ));

        templeList.add(new Temple(
                "Akshardham Temple",
                "Gandhinagar, Gujarat",
                "A prominent Hindu temple complex known for its architecture, exhibitions and cultural presentation.",
                R.drawable.akshardham
        ));

        templeList.add(new Temple(
                "Mahakaleshwar Temple",
                "Ujjain, Madhya Pradesh",
                "A major Shiva temple and one of the twelve Jyotirlingas, located in the ancient city of Ujjain.",
                R.drawable.mahakaleshwar
        ));

        adapter = new TempleAdapter(this, templeList);
        recyclerView.setAdapter(adapter);
    }
}
