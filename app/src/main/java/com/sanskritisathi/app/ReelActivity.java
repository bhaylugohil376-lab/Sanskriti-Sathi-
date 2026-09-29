package com.sanskritisathi.app;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReelActivity extends AppCompatActivity {

    private RecyclerView reelRecyclerView;
    private ReelAdapter reelAdapter;
    private LinearLayoutManager layoutManager;

    private final List<Reel> reelList = new ArrayList<>();

    private int currentPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reel);

        reelRecyclerView = findViewById(R.id.reelRecyclerView);

        setupRecyclerView();
        setupSnapHelper();
        loadReels();
    }

    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        layoutManager =
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        false
                );

        reelRecyclerView.setLayoutManager(layoutManager);

        reelRecyclerView.setHasFixedSize(false);

        reelRecyclerView.setItemViewCacheSize(2);

        reelRecyclerView.setOverScrollMode(
                View.OVER_SCROLL_NEVER
        );

        reelRecyclerView.setClipToPadding(false);

        reelRecyclerView.setClipChildren(false);

        reelAdapter =
                new ReelAdapter(
                        this,
                        reelList
                );

        reelRecyclerView.setAdapter(reelAdapter);

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

                        /*
                         * Instagram-style:
                         * play only after scrolling settles.
                         */
                        if (newState ==
                                RecyclerView.SCROLL_STATE_IDLE) {

                            playCurrentReel();
                        }
                    }
                }
        );
    }

    // =========================================================
    // SNAP - ONE REEL AT A TIME
    // =========================================================

    private void setupSnapHelper() {

        PagerSnapHelper snapHelper =
                new PagerSnapHelper();

        snapHelper.attachToRecyclerView(
                reelRecyclerView
        );
    }

    // =========================================================
    // LOAD REELS
    // =========================================================

    private void loadReels() {

        /*
         * IMPORTANT:
         *
         * Replace this with your existing
         * ReelSupabaseHelper method if the method
         * name is different.
         */

        ReelSupabaseHelper.loadReels(
                this,
                new ReelSupabaseHelper.ReelsCallback() {

                    @Override
                    public void onSuccess(
                            List<Reel> reels) {

                        runOnUiThread(() -> {

                            reelList.clear();

                            if (reels != null) {
                                reelList.addAll(reels);
                            }

                            reelAdapter.notifyDataSetChanged();

                            currentPosition =
                                    RecyclerView.NO_POSITION;

                            if (!reelList.isEmpty()) {

                                reelRecyclerView.post(
                                        () -> playCurrentReel()
                                );
                            }
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() -> {

                            /*
                             * Keep screen stable if loading fails.
                             * You can add your own error UI here.
                             */
                        });
                    }
                }
        );
    }

    // =========================================================
    // PLAY CURRENT REEL
    // =========================================================

    private void playCurrentReel() {

        if (reelAdapter == null ||
                reelRecyclerView == null) {
            return;
        }

        if (layoutManager == null) {
            return;
        }

        /*
         * Find the reel completely visible on screen.
         */
        int position =
                layoutManager
                        .findFirstCompletelyVisibleItemPosition();

        /*
         * If not completely visible, use first visible.
         */
        if (position ==
                RecyclerView.NO_POSITION) {

            position =
                    layoutManager
                            .findFirstVisibleItemPosition();
        }

        if (position ==
                RecyclerView.NO_POSITION) {
            return;
        }

        if (position < 0 ||
                position >= reelList.size()) {
            return;
        }

        currentPosition = position;

        /*
         * Adapter controls the actual ExoPlayer.
         */
        reelAdapter.pauseAllVideos();

        reelAdapter.playVideoAtPosition(
                position
        );
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (reelAdapter != null) {

            reelRecyclerView.post(
                    () -> playCurrentReel()
            );
        }
    }

    // =========================================================
    // PAUSE
    // =========================================================

    @Override
    protected void onPause() {

        if (reelAdapter != null) {

            reelAdapter.pauseAllVideos();
        }

        super.onPause();
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
    // GETTER
    // =========================================================

    public RecyclerView getReelRecyclerView() {

        return reelRecyclerView;
    }
}
