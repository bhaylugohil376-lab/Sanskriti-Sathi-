package com.sanskritisathi.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReelActivity extends AppCompatActivity implements ReelAdapter.ReelInteractionListener {

    private static final String TAG = "ReelActivity";
    private RecyclerView recyclerViewReels;
    private ProgressBar progressBar;
    private ReelAdapter reelAdapter;
    private ReelSupabaseHelper supabaseHelper;
    private LinearLayoutManager layoutManager;
    private int currentPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reel);

        recyclerViewReels = findViewById(R.id.recyclerViewReels);
        progressBar = findViewById(R.id.progressBar);

        supabaseHelper = new ReelSupabaseHelper();
        reelAdapter = new ReelAdapter(this, this);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewReels.setLayoutManager(layoutManager);
        recyclerViewReels.setAdapter(reelAdapter);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerViewReels);

        recyclerViewReels.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    playCurrentVisibleReel();
                }
            }
        });

        fetchActiveReels();
    }

    private void fetchActiveReels() {
        progressBar.setVisibility(View.VISIBLE);
        supabaseHelper.getActiveReels(new ReelSupabaseHelper.GetReelsCallback() {
            @Override
            public void onSuccess(List<Reel> reels) {
                progressBar.setVisibility(View.GONE);
                if (reels == null || reels.isEmpty()) {
                    Toast.makeText(ReelActivity.this, "No reels found", Toast.LENGTH_SHORT).show();
                    return;
                }
                reelAdapter.setReels(reels);
                recyclerViewReels.post(() -> playCurrentVisibleReel());
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error fetching reels: " + e.getMessage());
                Toast.makeText(ReelActivity.this, "Failed to load reels: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playCurrentVisibleReel() {
        int position = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (position == RecyclerView.NO_POSITION) {
            position = layoutManager.findFirstVisibleItemPosition();
        }

        if (position != RecyclerView.NO_POSITION && position != currentPosition) {
            if (currentPosition != -1) {
                RecyclerView.ViewHolder oldHolder = recyclerViewReels.findViewHolderForAdapterPosition(currentPosition);
                if (oldHolder instanceof ReelAdapter.ReelViewHolder) {
                    ((ReelAdapter.ReelViewHolder) oldHolder).pausePlayer();
                }
            }

            currentPosition = position;
            RecyclerView.ViewHolder newHolder = recyclerViewReels.findViewHolderForAdapterPosition(currentPosition);
            if (newHolder instanceof ReelAdapter.ReelViewHolder) {
                ((ReelAdapter.ReelViewHolder) newHolder).playPlayer();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseActivePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playCurrentVisibleReel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAllPlayers();
    }

    private void pauseActivePlayer() {
        if (currentPosition != -1) {
            RecyclerView.ViewHolder holder = recyclerViewReels.findViewHolderForAdapterPosition(currentPosition);
            if (holder instanceof ReelAdapter.ReelViewHolder) {
                ((ReelAdapter.ReelViewHolder) holder).pausePlayer();
            }
        }
    }

    private void releaseAllPlayers() {
        for (int i = 0; i < recyclerViewReels.getChildCount(); i++) {
            View child = recyclerViewReels.getChildAt(i);
            RecyclerView.ViewHolder holder = recyclerViewReels.getChildViewHolder(child);
            if (holder instanceof ReelAdapter.ReelViewHolder) {
                ((ReelAdapter.ReelViewHolder) holder).releasePlayer();
            }
        }
    }

    @Override
    public void onLikeClicked(Reel reel, int position) {
        reel.setLiked(!reel.isLiked());
        reel.setLikes(reel.isLiked() ? reel.getLikes() + 1 : Math.max(0, reel.getLikes() - 1));
        reelAdapter.notifyItemChanged(position);
    }

    @Override
    public void onCommentClicked(Reel reel, int position) {
        Toast.makeText(this, "Comments feature clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShareClicked(Reel reel, int position) {
        Toast.makeText(this, "Share: " + reel.getVideoUrl(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClicked(Reel reel, int position) {
        Toast.makeText(this, "Delete option clicked", Toast.LENGTH_SHORT).show();
    }
}
