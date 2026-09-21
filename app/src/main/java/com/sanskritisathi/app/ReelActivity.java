package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReelActivity extends AppCompatActivity {

    private RecyclerView reelRecyclerView;
    private ReelAdapter reelAdapter;

    private final List<Reel> reelList =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_reel
        );

        bindViews();
        setupRecyclerView();

        loadReels();
    }

    // ============================================================
    // BIND VIEWS
    // ============================================================

    private void bindViews() {

        reelRecyclerView =
                findViewById(
                        R.id.reelRecyclerView
                );
    }

    // ============================================================
    // RECYCLER VIEW
    // ============================================================

    private void setupRecyclerView() {

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        layoutManager.setOrientation(
                LinearLayoutManager.VERTICAL
        );

        reelRecyclerView.setLayoutManager(
                layoutManager
        );

        reelRecyclerView.setHasFixedSize(
                false
        );

        reelAdapter =
                new ReelAdapter(
                        this,
                        reelList
                );

        reelRecyclerView.setAdapter(
                reelAdapter
        );
    }

    // ============================================================
    // LOAD REELS
    // ============================================================

    private void loadReels() {

        ReelSupabaseHelper.getActiveReels(
                this,
                new ReelSupabaseHelper.ReelsCallback() {

                    @Override
                    public void onSuccess(
                            List<Reel> reels) {

                        runOnUiThread(() -> {

                            reelList.clear();

                            if (reels != null) {
                                reelList.addAll(
                                        reels
                                );
                            }

                            reelAdapter.setReels(
                                    reelList
                            );

                            if (reelList.isEmpty()) {

                                Toast.makeText(
                                        ReelActivity.this,
                                        "Abhi koi Reel available nahi hai.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() ->
                                Toast.makeText(
                                        ReelActivity.this,
                                        message,
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }
                }
        );
    }

    // ============================================================
    // REFRESH WHEN ACTIVITY RETURNS
    // ============================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (reelAdapter != null) {
            loadReels();
        }
    }

    // ============================================================
    // PAUSE / RELEASE
    // ============================================================

    /*
     * Current ReelAdapter individual ViewHolder ke
     * ExoPlayer ko recycle hone par release karta hai.
     *
     * Isliye purane Firebase adapter ke:
     *
     * pauseAllVideos()
     * releaseAllVideos()
     *
     * yahan intentionally use nahi kiye gaye.
     */

    @Override
    protected void onPause() {

        super.onPause();

        reelRecyclerView.stopScroll();
    }
}
