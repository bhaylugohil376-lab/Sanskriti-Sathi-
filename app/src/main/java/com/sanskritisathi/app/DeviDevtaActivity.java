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
                "Bhagwan Shiva",
                "Bhagwan Shiva are an important deity in Hindu traditions and are associated with meditation, transformation and spiritual knowledge.",
                R.drawable.shiva
        ));

        deviDevtaList.add(new DeviDevta(
                "Bhagwan Vishnu",
                "Bhagwan Vishnu is traditionally regarded as the preserver in the Hindu Trimurti.",
                R.drawable.vishnu
        ));

        deviDevtaList.add(new DeviDevta(
                "Bhagwan Rama",
                "Bhagwan Rama is a central figure of the Ramayana and is traditionally associated with dharma and righteous conduct.",
                R.drawable.rama
        ));

        deviDevtaList.add(new DeviDevta(
                "Bhagwan Krishna",
                "Bhagwan Krishna is a central figure of the Mahabharata and the speaker of the Bhagavad Gita.",
                R.drawable.krishna
        ));

        deviDevtaList.add(new DeviDevta(
                "Bhagwan Ganesha",
                "Bhagwan Ganesha is widely worshipped and is traditionally associated with wisdom, learning and auspicious beginnings.",
                R.drawable.ganesha
        ));

        deviDevtaList.add(new DeviDevta(
                "Bhagwan Hanuman",
                "Bhagwan Hanuman is an important figure in the Ramayana and is traditionally associated with devotion, courage and strength.",
                R.drawable.hanuman
        ));

        deviDevtaList.add(new DeviDevta(
                "Bhagwan Kartikeya",
                "Bhagwan Kartikeya is traditionally associated with courage and is especially revered in many parts of India.",
                R.drawable.kartikeya
        ));

        deviDevtaList.add(new DeviDevta(
                "Goddess Durga",
                "Goddess Durga is widely revered as a powerful divine mother and is celebrated during festivals such as Navratri.",
                R.drawable.durga
        ));

        deviDevtaList.add(new DeviDevta(
                "Goddess Lakshmi",
                "Goddess Lakshmi is traditionally associated with prosperity, good fortune and well-being.",
                R.drawable.lakshmi
        ));

        deviDevtaList.add(new DeviDevta(
                "Goddess Saraswati",
                "Goddess Saraswati is traditionally associated with knowledge, learning, music and the arts.",
                R.drawable.saraswati
        ));

        deviDevtaList.add(new DeviDevta(
                "Goddess Parvati",
                "Goddess Parvati is a major Hindu goddess and is traditionally regarded as the consort of Bhagwan Shiva.",
                R.drawable.parvati
        ));

        deviDevtaList.add(new DeviDevta(
                "Surya Dev",
                "Surya Dev is traditionally revered as the Sun deity and is associated with light and vitality.",
                R.drawable.surya
        ));

        adapter = new DeviDevtaAdapter(this, deviDevtaList);
        recyclerView.setAdapter(adapter);
    }
}
