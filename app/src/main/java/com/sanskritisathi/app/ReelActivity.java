package com.sanskritisathi.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

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

    private final Handler handler =
            new Handler(Looper.getMainLooper());

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

        reelRecyclerView.setItemAnimator(null);

        reelRecyclerView.setOverScrollMode(
                View.OVER_SCROLL_NEVER
        );

        reelRecyclerView.setClipToPadding(false);

        reelAdapter =
                new ReelAdapter(
                        this,
                        reelList
                );

        reelRecyclerView.setAdapter(
                reelAdapter
        );

        // -----------------------------------------------------
        // Instagram-style:
        // Only the most visible reel plays.
        // -----------------------------------------------------

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

                            playMostVisibleReel();
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

                            handler.postDelayed(
                                    () -> playMostVisibleReel(),
                                    250
                            );
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() -> {

                            loading = false;

                            // Keep screen usable.
                            // No crash if Supabase returns an error.
                        });
                    }
                }
        );
    }

    // =========================================================
    // PLAY MOST VISIBLE REEL
    // =========================================================

    private void playMostVisibleReel() {

        if (reelAdapter == null
                || reelRecyclerView == null) {
            return;
        }

        LinearLayoutManager manager =
                (LinearLayoutManager)
                        reelRecyclerView.getLayoutManager();

        if (manager == null) {
            return;
        }

        int firstVisible =
                manager.findFirstVisibleItemPosition();

        int lastVisible =
                manager.findLastVisibleItemPosition();

        if (firstVisible == RecyclerView.NO_POSITION
                || lastVisible == RecyclerView.NO_POSITION) {
            return;
        }

        int bestPosition = firstVisible;
        int bestVisibleHeight = -1;

        for (int position = firstVisible;
             position <= lastVisible;
             position++) {

            View child =
                    manager.findViewByPosition(
                            position
                    );

            if (child == null) {
                continue;
            }

            int visibleHeight =
                    calculateVisibleHeight(
                            child
                    );

            if (visibleHeight > bestVisibleHeight) {

                bestVisibleHeight =
                        visibleHeight;

                bestPosition =
                        position;
            }
        }

        reelAdapter.pauseAllVideos();

        reelAdapter.playVideoAt(
                bestPosition
        );

        // -----------------------------------------------------
        // Add view only when reel becomes the active reel.
        // -----------------------------------------------------

        if (bestPosition >= 0
                && bestPosition < reelList.size()) {

            Reel reel =
                    reelList.get(bestPosition);

            if (reel != null
                    && reel.getId() != null) {

                ReelSupabaseHelper.addReelView(
                        this,
                        reel.getId(),
                        new ReelSupabaseHelper.ActionCallback() {

                            @Override
                            public void onSuccess() {

                                runOnUiThread(() -> {

                                    int currentViews =
                                            reel.getViews();

                                    reel.setViews(
                                            currentViews + 1
                                    );

                                    reelAdapter
                                            .notifyItemChanged(
                                                    bestPosition,
                                                    "views"
                                            );
                                });
                            }

                            @Override
                            public void onError(
                                    String message) {
                                // View count failure should
                                // never stop video playback.
                            }
                        }
                );
            }
        }
    }

    // =========================================================
    // VISIBLE HEIGHT
    // =========================================================

    private int calculateVisibleHeight(
            View view) {

        int[] location =
                new int[2];

        view.getLocationOnScreen(
                location
        );

        int top =
                location[1];

        int bottom =
                top + view.getHeight();

        int screenHeight =
                reelRecyclerView.getHeight();

        int visibleTop =
                Math.max(
                        top,
                        0
                );

        int visibleBottom =
                Math.min(
                        bottom,
                        screenHeight
                );

        return Math.max(
                0,
                visibleBottom - visibleTop
        );
    }

    // =========================================================
    // RECYCLER VIEW ACCESS
    // =========================================================

    public RecyclerView getReelRecyclerView() {

        return reelRecyclerView;
    }

    // =========================================================
    // PAUSE
    // =========================================================

    @Override
    protected void onPause() {

        super.onPause();

        if (reelAdapter != null) {

            reelAdapter.pauseAllVideos();
        }
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        handler.postDelayed(
                () -> {

                    if (!isFinishing()
                            && !isDestroyed()) {

                        playMostVisibleReel();
                    }

                },
                250
        );
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        handler.removeCallbacksAndMessages(
                null
        );

        if (reelAdapter != null) {

            reelAdapter.releaseAllVideos();
        }

        super.onDestroy();
    }
}
