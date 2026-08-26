package com.sanskritisathi.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class RajaActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView listView = new ListView(this);
        listView.setPadding(16, 16, 16, 16);

        setContentView(listView);

        List<Raja> rajaList = RajaData.getAllRajas();

        ArrayList<String> names = new ArrayList<>();

        for (Raja raja : rajaList) {
            names.add(raja.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                names
        );

        listView.setAdapter(adapter);
    }
}
