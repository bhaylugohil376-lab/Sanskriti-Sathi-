package com.sanskritisathi.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
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

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_culture);

        cultureRecyclerView =
                findViewById(R.id.cultureRecyclerView);

        progressBar =
                findViewById(R.id.progressBar);

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

        showLoading(true);

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

                        showLoading(false);

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

                        showLoading(false);

                        Toast.makeText(
                                CultureActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void showLoading(
            boolean loading) {

        if (progressBar != null) {

            progressBar.setVisibility(
                    loading
                            ? View.VISIBLE
                            : View.GONE
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Feed refresh
        if (culturePostAdapter != null) {
            loadPosts();
        }
    }
}
