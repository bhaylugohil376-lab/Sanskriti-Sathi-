package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.TextUtils;
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

    private LinearLayoutManager layoutManager;

    private PagerSnapHelper pagerSnapHelper;

    private int currentPosition =
            RecyclerView.NO_POSITION;

    private boolean reelsLoaded = false;

    // =========================================================
    // ON CREATE
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
    // BIND VIEWS
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

        reelRecyclerView.setOverScrollMode(
                View.OVER_SCROLL_NEVER
        );

        reelRecyclerView.setVerticalScrollBarEnabled(
                false
        );

        /*
         * One full-screen reel per swipe.
         */
        pagerSnapHelper =
                new PagerSnapHelper();

        pagerSnapHelper.attachToRecyclerView(
                reelRecyclerView
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
                         * User finished swiping.
                         * Find the snapped reel and play only it.
                         */
                        if (newState
                                == RecyclerView.SCROLL_STATE_IDLE) {

                            playSnappedReel();
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
                         * While swiping, pause visible videos.
                         * This prevents two reels playing together.
                         */
                        if (Math.abs(dy) > 0) {

                            pauseCurrentVideo();
                        }
                    }
                }
        );
    }

    // =========================================================
    // LOAD REELS
    // =========================================================

    private void loadReels() {

        if (!SupabaseAuthManager.isLoggedIn(
                this
        )) {

            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        ReelSupabaseHelper.getActiveReels(
                this,
                new ReelSupabaseHelper.ReelsCallback() {

                    @Override
                    public void onSuccess(
                            List<Reel> reels) {

                        if (isFinishing()
                                || isDestroyed()) {

                            return;
                        }

                        reelList.clear();

                        if (reels != null) {

                            for (Reel reel : reels) {

                                if (reel == null) {
                                    continue;
                                }

                                if (TextUtils.isEmpty(
                                        reel.getVideoUrl()
                                )) {

                                    continue;
                                }

                                reelList.add(
                                        reel
                                );
                            }
                        }

                        reelAdapter.notifyDataSetChanged();

                        reelsLoaded = true;

                        if (reelList.isEmpty()) {

                            Toast.makeText(
                                    ReelActivity.this,
                                    "No reels available.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        /*
                         * Wait until RecyclerView has created
                         * the first ViewHolder.
                         */
                        reelRecyclerView.post(
                                () -> {

                                    if (!isFinishing()
                                            && !isDestroyed()) {

                                        playSnappedReel();
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(
                            String message) {

                        if (isFinishing()
                                || isDestroyed()) {

                            return;
                        }

                        Toast.makeText(
                                ReelActivity.this,
                                TextUtils.isEmpty(message)
                                        ? "Reels load failed."
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

        if (!reelsLoaded
                || reelList.isEmpty()) {

            return;
        }

        if (pagerSnapHelper == null
                || layoutManager == null) {

            return;
        }

        /*
         * Get the View currently snapped to the center.
         */
        View snappedView =
                pagerSnapHelper.findSnapView(
                        layoutManager
                );

        if (snappedView == null) {
            return;
        }

        int position =
                layoutManager.getPosition(
                        snappedView
                );

        if (position
                == RecyclerView.NO_POSITION) {

            return;
        }

        /*
         * Pause everything first.
         */
        reelAdapter.pauseAllVideos();

        RecyclerView.ViewHolder holder =
                reelRecyclerView.findViewHolderForAdapterPosition(
                        position
                );

        if (holder instanceof ReelAdapter.ReelViewHolder) {

            ReelAdapter.ReelViewHolder reelHolder =
                    (ReelAdapter.ReelViewHolder)
                            holder;

            reelHolder.playVideo();

            /*
             * Add view only when the reel becomes
             * the active snapped reel.
             */
            if (position != currentPosition) {

                currentPosition =
                        position;

                addViewForReel(
                        position
                );
            }
        }
    }

    // =========================================================
    // PAUSE CURRENT
    // =========================================================

    private void pauseCurrentVideo() {

        if (reelAdapter != null) {

            reelAdapter.pauseAllVideos();
        }
    }

    // =========================================================
    // ADD VIEW
    // =========================================================

    private void addViewForReel(
            int position) {

        if (position < 0
                || position >= reelList.size()) {

            return;
        }

        Reel reel =
                reelList.get(
                        position
                );

        if (reel == null
                || TextUtils.isEmpty(
                        reel.getId()
                )) {

            return;
        }

        /*
         * Optimistically update the local count.
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
                position,
                "view_count"
        );

        /*
         * Persist to Supabase.
         */
        ReelSupabaseHelper.addReelView(
                this,
                reel.getId(),
                new ReelSupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {
                        // View successfully saved.
                    }

                    @Override
                    public void onError(
                            String message) {

                        /*
                         * Keep local count.
                         * Backend failure should not interrupt
                         * video playback.
                         */
                    }
                }
        );
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    protected void onResume() {
        super.onResume();

        if (!reelsLoaded) {
            return;
        }

        reelRecyclerView.post(
                () -> {

                    if (!isFinishing()
                            && !isDestroyed()) {

                        playSnappedReel();
                    }
                }
        );
    }

    // =========================================================
    // PAUSE
    // =========================================================

    @Override
    protected void onPause() {
        super.onPause();

        pauseCurrentVideo();
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
