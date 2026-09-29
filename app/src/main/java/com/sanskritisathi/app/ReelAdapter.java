package com.sanskritisathi.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReelActivity extends AppCompatActivity {

    private RecyclerView reelRecyclerView;
    private ReelAdapter reelAdapter;

    private final List<Reel> reelList = new ArrayList<>();

    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reel);

        bindViews();
        setupRecyclerView();
        loadReels();
    }

    // =========================================================
    // BIND VIEWS
    // =========================================================

    private void bindViews() {

        reelRecyclerView =
                findViewById(R.id.reelRecyclerView);

        if (reelRecyclerView == null) {
            throw new IllegalStateException(
                    "reelRecyclerView not found in activity_reel.xml"
            );
        }
    }

    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        false
                );

        reelRecyclerView.setLayoutManager(
                layoutManager
        );

        reelRecyclerView.setHasFixedSize(false);

        reelRecyclerView.setItemViewCacheSize(2);

        reelRecyclerView.setOverScrollMode(
                View.OVER_SCROLL_NEVER
        );

        reelRecyclerView.setClipToPadding(false);

        reelRecyclerView.setVerticalScrollBarEnabled(
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

        /*
         * Instagram-style:
         * Play only the reel that becomes visible.
         */
        reelRecyclerView.addOnScrollListener(
                new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(
                            @NonNull RecyclerView recyclerView,
                            int newState) {

                        super.onScrollStateChanged(
                                recyclerView,
                                newState
                        );

                        if (newState ==
                                RecyclerView.SCROLL_STATE_IDLE) {

                            playCurrentReel();
                        }
                    }
                }
        );
    }

    // =========================================================
    // LOAD REELS
    // =========================================================

    private void loadReels() {

        if (loading) {
            return;
        }

        loading = true;

        ReelSupabaseHelper.getActiveReels(
                this,
                new ReelSupabaseHelper.ReelsCallback() {

                    @Override
                    public void onSuccess(
                            List<Reel> reels) {

                        runOnUiThread(() -> {

                            loading = false;

                            reelList.clear();

                            if (reels != null) {
                                reelList.addAll(reels);
                            }

                            reelAdapter.notifyDataSetChanged();

                            if (reelList.isEmpty()) {

                                Toast.makeText(
                                        ReelActivity.this,
                                        "Abhi koi Reel available nahi hai.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            /*
                             * Wait until RecyclerView has
                             * finished layout before playback.
                             */
                            reelRecyclerView.post(() ->
                                    playCurrentReel()
                            );
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() -> {

                            loading = false;

                            Toast.makeText(
                                    ReelActivity.this,
                                    message == null
                                            ? "Reels load nahi hui."
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    // =========================================================
    // CURRENT REEL
    // =========================================================

    private void playCurrentReel() {

        if (reelAdapter == null) {
            return;
        }

        reelAdapter.resumeCurrentVideo();
    }

    // =========================================================
    // PAUSE WHEN ACTIVITY NOT VISIBLE
    // =========================================================

    @Override
    protected void onPause() {

        if (reelAdapter != null) {
            reelAdapter.pauseAllVideos();
        }

        super.onPause();
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (reelAdapter != null) {

            reelRecyclerView.post(() ->
                    reelAdapter.resumeCurrentVideo()
            );
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        if (reelAdapter != null) {
            reelAdapter.releaseAllVideos();
        }

        if (reelRecyclerView != null) {
            reelRecyclerView.setAdapter(null);
        }

        super.onDestroy();
    }

    // =========================================================
    // PUBLIC ACCESS FOR ADAPTER
    // =========================================================

    public RecyclerView getReelRecyclerView() {
        return reelRecyclerView;
    }
}
