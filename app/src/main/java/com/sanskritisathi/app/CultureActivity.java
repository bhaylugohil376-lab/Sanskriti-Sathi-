package com.sanskritisathi.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CultureActivity extends AppCompatActivity {

    private RecyclerView cultureRecyclerView;
    private CulturePostAdapter culturePostAdapter;
    private ArrayList<CulturePost> culturePostList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_culture);

        cultureRecyclerView = findViewById(R.id.cultureRecyclerView);

        cultureRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        culturePostList = CulturePostData.getAllPosts();

        culturePostAdapter = new CulturePostAdapter(
                this,
                culturePostList
        );

        cultureRecyclerView.setAdapter(
                culturePostAdapter
        );
    }
}
