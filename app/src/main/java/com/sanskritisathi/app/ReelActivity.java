package com.sanskritisathi.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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

    private final List<Reel> reelList =
            new ArrayList<>();

    private PagerSnapHelper pagerSnapHelper;

    private boolean loading = false;

    // =========================================================
    // CREATE
    // =========================================================

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

    // =========================================================
    // BIND
    // =========================================================

    private void bindViews() {

        reelRecyclerView =
                findViewById(
                        R.id.reelRecyclerView
                );
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

        reelRecyclerView.setHasFixedSize(
                false
        );

        reelRecyclerView.setItemViewCacheSize(
                2
        );

        reelRecyclerView.setOverScrollMode(
                View.OVER_SCROLL_NEVER
        );

        reelRecyclerView.setClipToPadding(
                false
        );

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

        // -----------------------------------------------------
        // Instagram-style one-reel-at-a-time snapping
        // -----------------------------------------------------

        pagerSnapHelper =
                new PagerSnapHelper();

        pagerSnapHelper.attachToRecyclerView(
                reelRecyclerView
        );

        // -----------------------------------------------------
        // Detect settled reel and play it
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

                            playSnappedReel();
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

                        loading = false;

                        reelList.clear();

                        if (reels != null) {

                            reelList.addAll(
                                    reels
                            );
                        }

                        reelAdapter.notifyDataSetChanged();

                        if (reelList.isEmpty()) {

                            Toast.makeText(
                                    ReelActivity.this,
                                    "No reels available.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        reelRecyclerView.post(
                                () -> playSnappedReel()
                        );
                    }

                    @Override
                    public void onError(
                            String message) {

                        loading = false;

                        Toast.makeText(
                                ReelActivity.this,
                                message == null
                                        ? "Reels load nahi hui."
                                        : message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =========================================================
    // PLAY SNAPPED REEL
    // =========================================================

    private void playSnappedReel() {

        if (reelAdapter == null) {
            return;
        }

        reelAdapter.pauseAllVideos();

        View snappedView =
                pagerSnapHelper.findSnapView(
                        reelRecyclerView.getLayoutManager()
                );

        if (snappedView == null) {

            reelAdapter.resumeCurrentVideo();

            return;
        }

        RecyclerView.ViewHolder holder =
                reelRecyclerView.getChildViewHolder(
                        snappedView
                );

        if (holder instanceof ReelAdapter.ReelViewHolder) {

            ReelAdapter.ReelViewHolder reelHolder =
                    (ReelAdapter.ReelViewHolder) holder;

            reelHolder.playVideo();

            addViewForSnappedReel(
                    holder.getBindingAdapterPosition()
            );
        }
    }

    // =========================================================
    // ADD VIEW
    // =========================================================

    private void addViewForSnappedReel(
            int position) {

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        if (position < 0
                || position >= reelList.size()) {
            return;
        }

        Reel reel =
                reelList.get(position);

        if (reel == null
                || reel.getId() == null) {
            return;
        }

        // Local count update immediately.
        // Backend update happens asynchronously.
        int oldViews =
                Math.max(
                        0,
                        reel.getViews()
                );

        reel.setViews(
                oldViews + 1
        );

        reelAdapter.notifyItemChanged(
                position,
                "view_count"
        );

        ReelSupabaseHelper.addReelView(
                this,
                reel.getId(),
                new ReelSupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {
                        // Nothing else required.
                    }

                    @Override
                    public void onError(
                            String message) {
                        // Do not interrupt reel playback.
                    }
                }
        );
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
    // RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (reelAdapter != null) {

            reelRecyclerView.post(
                    () -> playSnappedReel()
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

        super.onDestroy();
    }

    // =========================================================
    // GET RECYCLER VIEW
    // =========================================================

    public RecyclerView getReelRecyclerView() {

        return reelRecyclerView;
    }
}
