package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CultureActivity extends AppCompatActivity {

    private RecyclerView cultureRecyclerView;
    private CulturePostAdapter culturePostAdapter;

    private final ArrayList<CulturePost> culturePostList =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_culture);

        cultureRecyclerView =
                findViewById(R.id.cultureRecyclerView);

        cultureRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        cultureRecyclerView.setHasFixedSize(false);

        culturePostAdapter =
                new CulturePostAdapter(
                        this,
                        culturePostList
                );

        cultureRecyclerView.setAdapter(
                culturePostAdapter
        );

        loadPosts();
    }

    private void loadPosts() {

        CulturePostFirebaseHelper.getPublicPosts(
                new CulturePostFirebaseHelper.PostsCallback() {

                    @Override
                    public void onSuccess(
                            List<CulturePost> posts) {

                        culturePostList.clear();

                        if (posts != null) {
                            culturePostList.addAll(posts);
                        }

                        culturePostAdapter.notifyDataSetChanged();

                        if (culturePostList.isEmpty()) {
                            Toast.makeText(
                                    CultureActivity.this,
                                    "Abhi koi public post available nahi hai.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onError(
                            String message) {

                        Toast.makeText(
                                CultureActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (culturePostAdapter != null) {
            loadPosts();
        }
    }
}
