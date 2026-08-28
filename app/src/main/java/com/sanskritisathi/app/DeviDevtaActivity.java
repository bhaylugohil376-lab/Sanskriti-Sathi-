package com.sanskritisathi.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeviDevtaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DeviDevtaAdapter adapter;
    private ArrayList<DeviDevta> deviDevtaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devi_devta);

        recyclerView = findViewById(R.id.deviDevtaRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        deviDevtaList = new ArrayList<>();

        deviDevtaList.add(new DeviDevta(
                "Shiva",
                "A major Hindu deity traditionally associated with transformation, meditation and spiritual practice.",
                R.drawable.shiva
        ));

        deviDevtaList.add(new DeviDevta(
                "Vishnu",
                "A major Hindu deity traditionally associated with preservation and protection.",
                R.drawable.vishnu
        ));

        deviDevtaList.add(new DeviDevta(
                "Brahma",
                "A deity traditionally associated with creation in Hindu cosmology.",
                R.drawable.brahma
        ));

        deviDevtaList.add(new DeviDevta(
                "Ganesha",
                "A widely worshipped deity traditionally associated with wisdom, learning and auspicious beginnings.",
                R.drawable.ganesha
        ));

        deviDevtaList.add(new DeviDevta(
                "Hanuman",
                "A revered figure in the Ramayana tradition, associated with devotion, courage and service to Rama.",
                R.drawable.hanuman
        ));

        deviDevtaList.add(new DeviDevta(
                "Krishna",
                "A central figure in the Mahabharata and Bhagavad Gita and widely worshipped as a divine teacher.",
                R.drawable.krishna
        ));

        deviDevtaList.add(new DeviDevta(
                "Rama",
                "A major figure of the Ramayana, traditionally associated with dharma and righteous conduct.",
                R.drawable.rama
        ));

        deviDevtaList.add(new DeviDevta(
                "Durga",
                "A revered Goddess traditionally associated with strength, protection and the triumph of good over evil.",
                R.drawable.durga
        ));

        deviDevtaList.add(new DeviDevta(
                "Lakshmi",
                "A revered Goddess traditionally associated with prosperity, abundance and well-being.",
                R.drawable.lakshmi
        ));

        deviDevtaList.add(new DeviDevta(
                "Saraswati",
                "A revered Goddess traditionally associated with knowledge, learning, music and arts.",
                R.drawable.saraswati
        ));

        deviDevtaList.add(new DeviDevta(
                "Parvati",
                "A major Goddess traditionally associated with motherhood, strength and devotion.",
                R.drawable.parvati
        ));

        deviDevtaList.add(new DeviDevta(
                "Kali",
                "A powerful form of the Goddess traditionally associated with time, transformation and protection.",
                R.drawable.kali
        ));

        deviDevtaList.add(new DeviDevta(
                "Radha",
                "A central devotional figure in many Vaishnava traditions and especially associated with devotion to Krishna.",
                R.drawable.radha
        ));

        deviDevtaList.add(new DeviDevta(
                "Surya",
                "The Sun deity, traditionally associated with light, vitality and the life-giving power of the Sun.",
                R.drawable.surya
        ));

        deviDevtaList.add(new DeviDevta(
                "Shani",
                "A deity associated in Hindu tradition with Shani and the planet Saturn.",
                R.drawable.shani
        ));

        adapter = new DeviDevtaAdapter(this, deviDevtaList);
        recyclerView.setAdapter(adapter);
    }
}
