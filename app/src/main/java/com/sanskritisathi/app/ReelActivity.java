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

    private final List<Reel> reelList =
            new ArrayList<>();

    private LinearLayoutManager layoutManager;

    private boolean loading = false;
    private boolean firstLoad = true;

    private int lastPlayedPosition =
            RecyclerView.NO_POSITION;

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
    // BIND VIEWS
    // =========================================================

    private void bindViews() {

        reelRecyclerView =
                findViewById(
                        R.id.reelRecyclerView
                );
    }

    // =========================================================
    // SETUP RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        layoutManager =
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

        reelRecyclerView.setClipToPadding(
                false
        );

        reelRecyclerView.setOverScrollMode(
                View.OVER_SCROLL_NEVER
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

        setupScrollListener();
    }

    // =========================================================
    // SCROLL LISTENER
    // =========================================================

    private void setupScrollListener() {

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
                         * Instagram-style behavior:
                         *
                         * User swipe karte waqt
                         * current video pause rahega.
                         *
                         * Scroll complete hone ke baad
                         * center/visible reel play hogi.
                         */

                        if (newState ==
                                RecyclerView.SCROLL_STATE_IDLE) {

                            playBestVisibleReel();
                        }
                    }

                    @Override
                    public void onScrolled(
                            @NonNull RecyclerView recyclerView,
                            int dx,
                            int dy) {

                        super.onScrolled(
                                recyclerView,
                                dx,
                                dy
                        );

                        /*
                         * Fast scrolling ke time
                         * videos continuously switch nahi honge.
                         */
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

                        /*
                         * First reel ko layout hone ke
                         * baad play karenge.
                         */

                        reelRecyclerView.post(
                                () -> {

                                    playBestVisibleReel();

                                    firstLoad = false;
                                }
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
    // FIND BEST VISIBLE REEL
    // =========================================================

    private void playBestVisibleReel() {

        if (reelAdapter == null
                || reelList.isEmpty()) {

            return;
        }

        int position =
                findBestVisiblePosition();

        if (position ==
                RecyclerView.NO_POSITION) {

            return;
        }

        /*
         * Pehle sab visible videos pause.
         */

        reelAdapter.pauseAllVideos();

        /*
         * Selected visible video play.
         */

        RecyclerView.ViewHolder holder =
                reelRecyclerView
                        .findViewHolderForAdapterPosition(
                                position
                        );

        if (holder instanceof ReelAdapter.ReelViewHolder) {

            ReelAdapter.ReelViewHolder reelHolder =
                    (ReelAdapter.ReelViewHolder)
                            holder;

            reelHolder.playPlayer();
        }

        /*
         * New reel par view count.
         */

        if (position != lastPlayedPosition) {

            lastPlayedPosition =
                    position;

            Reel reel =
                    reelList.get(position);

            if (reel != null) {

                addViewOnce(
                        reel
                );
            }
        }
    }

    // =========================================================
    // FIND MOST CENTERED VISIBLE REEL
    // =========================================================

    private int findBestVisiblePosition() {

        int childCount =
                reelRecyclerView.getChildCount();

        if (childCount == 0) {

            return RecyclerView.NO_POSITION;
        }

        int recyclerCenter =
                reelRecyclerView.getHeight() / 2;

        int bestPosition =
                RecyclerView.NO_POSITION;

        int smallestDistance =
                Integer.MAX_VALUE;

        for (int i = 0;
             i < childCount;
             i++) {

            View child =
                    reelRecyclerView.getChildAt(i);

            int childCenter =
                    (child.getTop()
                            + child.getBottom()) / 2;

            int distance =
                    Math.abs(
                            childCenter
                                    - recyclerCenter
                    );

            if (distance <
                    smallestDistance) {

                smallestDistance =
                        distance;

                RecyclerView.ViewHolder holder =
                        reelRecyclerView
                                .getChildViewHolder(
                                        child
                                );

                bestPosition =
                        holder.getBindingAdapterPosition();
            }
        }

        return bestPosition;
    }

    // =========================================================
    // ADD VIEW
    // =========================================================

    private void addViewOnce(
            Reel reel) {

        if (reel == null
                || reel.getId() == null) {

            return;
        }

        /*
         * Local count immediately update.
         */

        int oldViews =
                Math.max(
                        0,
                        reel.getViews()
                );

        reel.setViews(
                oldViews + 1
        );

        reelAdapter.notifyItemChanged(
                lastPlayedPosition,
                "views"
        );

        /*
         * Supabase count update.
         */

        ReelSupabaseHelper.addReelView(
                this,
                reel.getId(),
                new ReelSupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {
                        // Count already updated locally.
                    }

                    @Override
                    public void onError(
                            String message) {
                        // Silent failure.
                    }
                }
        );
    }

    // =========================================================
    // LIFECYCLE - PAUSE
    // =========================================================

    @Override
    protected void onPause() {

        if (reelAdapter != null) {

            reelAdapter.pauseAllVideos();
        }

        super.onPause();
    }

    // =========================================================
    // LIFECYCLE - RESUME
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (reelAdapter != null
                && !reelList.isEmpty()) {

            reelRecyclerView.post(
                    () -> playBestVisibleReel()
            );
        }
    }

    // =========================================================
    // LIFECYCLE - STOP
    // =========================================================

    @Override
    protected void onStop() {

        if (reelAdapter != null) {

            reelAdapter.pauseAllVideos();
        }

        super.onStop();
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
    // PUBLIC ACCESS FOR ADAPTER
    // =========================================================

    public RecyclerView getReelRecyclerView() {

        return reelRecyclerView;
    }
}
