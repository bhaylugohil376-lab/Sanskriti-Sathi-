package com.sanskritisathi.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CultureActivity extends AppCompatActivity {

    private RecyclerView cultureRecyclerView;
    private CulturePostAdapter culturePostAdapter;
    private TextView emptyCultureText;

    private final ArrayList<CulturePost> culturePostList =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_culture);

        cultureRecyclerView =
                findViewById(R.id.cultureRecyclerView);

        /*
         * Optional empty-state TextView.
         * XML mein na ho to bhi app crash nahi karega.
         */
        emptyCultureText =
                findViewById(R.id.emptyCultureText);

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

                        updateEmptyState();

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

                        updateEmptyState();

                        Toast.makeText(
                                CultureActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void updateEmptyState() {

        if (emptyCultureText == null) {
            return;
        }

        if (culturePostList.isEmpty()) {

            emptyCultureText.setVisibility(
                    View.VISIBLE
            );

        } else {

            emptyCultureText.setVisibility(
                    View.GONE
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Post create/delete ke baad Culture feed
         * automatically refresh ho jayegi.
         */
        if (culturePostAdapter != null) {
            loadPosts();
        }
    }
}
