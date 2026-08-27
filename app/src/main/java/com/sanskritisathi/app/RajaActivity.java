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

        // Ramayana
        rajaList.add(new Raja(
                "Rama",
                "King of Ayodhya and the central figure of the Ramayana.",
                R.drawable.rama
        ));

        rajaList.add(new Raja(
                "Dasharatha",
                "King of Ayodhya and father of Rama.",
                R.drawable.dasharatha
        ));

        rajaList.add(new Raja(
                "Janaka",
                "King of Mithila and father of Sita, remembered for wisdom and knowledge.",
                R.drawable.janaka
        ));

        rajaList.add(new Raja(
                "Harishchandra",
                "Legendary king remembered in Indian tradition for his commitment to truth and righteousness.",
                R.drawable.harishchandra
        ));

        // Mahabharata
        rajaList.add(new Raja(
                "Yudhishthira",
                "Eldest of the Pandavas and a major figure in the Mahabharata.",
                R.drawable.yudhishthira
        ));

        rajaList.add(new Raja(
                "Krishna",
                "A central figure in the Mahabharata and the teacher of Arjuna in the Bhagavad Gita.",
                R.drawable.krishna
        ));

        // Ancient India
        rajaList.add(new Raja(
                "Chandragupta Maurya",
                "Founder of the Maurya Empire and one of the major rulers of ancient India.",
                R.drawable.chandragupta
        ));

        rajaList.add(new Raja(
                "Ashoka",
                "Mauryan emperor who promoted dhamma and supported the spread of Buddhism after the Kalinga War.",
                R.drawable.ashoka
        ));

        rajaList.add(new Raja(
                "Samudragupta",
                "Gupta emperor remembered for his military campaigns and patronage of art and culture.",
                R.drawable.samudragupta
        ));

        rajaList.add(new Raja(
                "Vikramaditya",
                "A celebrated traditional Indian king associated with learning, justice and the legendary Vikramaditya tradition.",
                R.drawable.vikramaditya
        ));

        // Medieval and Early Modern India
        rajaList.add(new Raja(
                "Harshavardhana",
                "Ruler of much of northern India during the 7th century, known for supporting learning and culture.",
                R.drawable.harshavardhana
        ));

        rajaList.add(new Raja(
                "Prithviraj Chauhan",
                "Chahamana ruler associated with Ajmer and Delhi and an important figure in medieval Indian history.",
                R.drawable.prithviraj_chauhan
        ));

        rajaList.add(new Raja(
                "Rajaraja Chola I",
                "Chola emperor whose reign saw major expansion of the Chola kingdom and development of temple architecture.",
                R.drawable.rajaraja_chola
        ));

        rajaList.add(new Raja(
                "Krishnadevaraya",
                "Vijayanagara emperor whose reign is remembered for political strength, literature, art and architecture.",
                R.drawable.krishnadevaraya
        ));

        // Maratha and Sikh history
        rajaList.add(new Raja(
                "Chhatrapati Shivaji Maharaj",
                "Founder of the Maratha state and an important historical figure known for administration and military organization.",
                R.drawable.shivaji
        ));

        rajaList.add(new Raja(
                "Maharana Pratap",
                "Ruler of Mewar remembered for his resistance and commitment to the independence of his kingdom.",
                R.drawable.maharana_pratap
        ));

        rajaList.add(new Raja(
                "Maharaja Ranjit Singh",
                "Founder and principal ruler of the Sikh Empire, who united large parts of Punjab under his rule.",
                R.drawable.ranjit_singh
        ));

        // Adapter
        adapter = new RajaAdapter(this, rajaList);
        recyclerView.setAdapter(adapter);
    }
}
