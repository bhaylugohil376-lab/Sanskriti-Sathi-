package com.sanskritisathi.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GitaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GitaAdapter adapter;
    private ArrayList<Gita> gitaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gita);

        recyclerView = findViewById(R.id.gitaRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        gitaList = new ArrayList<>();

        gitaList.add(new Gita(
                "Chapter 1",
                "Arjuna Vishada Yoga",
                "The chapter describes Arjuna's confusion and sorrow before the great battle."
        ));

        gitaList.add(new Gita(
                "Chapter 2",
                "Sankhya Yoga",
                "Krishna explains the nature of the self, duty, wisdom and the importance of performing one's duty."
        ));

        gitaList.add(new Gita(
                "Chapter 3",
                "Karma Yoga",
                "The chapter explains the path of selfless action and the importance of performing one's duty."
        ));

        gitaList.add(new Gita(
                "Chapter 4",
                "Jnana Karma Sannyasa Yoga",
                "Krishna explains spiritual knowledge, righteous action and the purpose of divine incarnation."
        ));

        gitaList.add(new Gita(
                "Chapter 5",
                "Karma Sannyasa Yoga",
                "The chapter compares renunciation and selfless action and explains how both can lead toward spiritual freedom."
        ));

        gitaList.add(new Gita(
                "Chapter 6",
                "Dhyana Yoga",
                "The chapter teaches meditation, discipline of the mind and the balanced path of spiritual practice."
        ));

        gitaList.add(new Gita(
                "Chapter 7",
                "Jnana Vijnana Yoga",
                "Krishna describes divine knowledge and explains the relationship between the material world and the Supreme."
        ));

        gitaList.add(new Gita(
                "Chapter 8",
                "Akshara Brahma Yoga",
                "The chapter discusses Brahman, the self, remembrance of the Divine and the spiritual goal at the time of death."
        ));

        gitaList.add(new Gita(
                "Chapter 9",
                "Raja Vidya Raja Guhya Yoga",
                "Krishna presents spiritual knowledge as a profound and direct path of devotion."
        ));

        gitaList.add(new Gita(
                "Chapter 10",
                "Vibhuti Yoga",
                "Krishna describes divine manifestations and explains how the Divine can be recognized through the wonders of creation."
        ));

        gitaList.add(new Gita(
                "Chapter 11",
                "Vishvarupa Darshana Yoga",
                "Arjuna receives a vision of Krishna's universal form and experiences the vastness of the Divine."
        ));

        gitaList.add(new Gita(
                "Chapter 12",
                "Bhakti Yoga",
                "The chapter explains devotion and describes qualities valued in a devoted person."
        ));

        gitaList.add(new Gita(
                "Chapter 13",
                "Kshetra Kshetrajna Vibhaga Yoga",
                "Krishna explains the distinction between the field of experience and the knower of the field."
        ));

        gitaList.add(new Gita(
                "Chapter 14",
                "Gunatraya Vibhaga Yoga",
                "The chapter explains the three gunas—sattva, rajas and tamas—and their influence on human behavior."
        ));

        gitaList.add(new Gita(
                "Chapter 15",
                "Purushottama Yoga",
                "Krishna describes the Supreme Being and explains the spiritual nature of the self."
        ));

        gitaList.add(new Gita(
                "Chapter 16",
                "Daivasura Sampad Vibhaga Yoga",
                "The chapter describes divine and destructive qualities and their influence on a person's life."
        ));

        gitaList.add(new Gita(
                "Chapter 17",
                "Shraddhatraya Vibhaga Yoga",
                "Krishna explains three kinds of faith and how the three gunas influence faith, food, worship and discipline."
        ));

        gitaList.add(new Gita(
                "Chapter 18",
                "Moksha Sannyasa Yoga",
                "The final chapter summarizes important teachings about duty, knowledge, devotion, renunciation and liberation."
        ));

        adapter = new GitaAdapter(this, gitaList);
        recyclerView.setAdapter(adapter);
    }
}
