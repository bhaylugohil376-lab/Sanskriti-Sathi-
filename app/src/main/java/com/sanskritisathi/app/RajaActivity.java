package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class RajaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RajaAdapter adapter;
    private ArrayList<Raja> rajaList;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raja);

        recyclerView = findViewById(R.id.rajaRecyclerView);
        searchView = findViewById(R.id.searchView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        rajaList = new ArrayList<>(RajaData.getAllRajas());

        // Click Listener logic
        adapter = new RajaAdapter(this, rajaList, raja -> {
            Intent intent = new Intent(RajaActivity.this, RajaDetailActivity.class);
            intent.putExtra("raja_name", raja.getName());
            intent.putExtra("raja_dynasty", raja.getDynasty());
            intent.putExtra("raja_history", raja.getHistory());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // Search Filter logic
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterList(newText);
                return true;
            }
        });
    }
}
