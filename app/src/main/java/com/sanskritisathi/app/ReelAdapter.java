package com.sanskritisathi.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReelAdapter extends RecyclerView.Adapter<ReelAdapter.ReelViewHolder> {

    private static final String TAG = "ReelAdapter";
    private final Context context;
    private final List<Reel> reelList = new ArrayList<>();
    private final ReelInteractionListener interactionListener;

    public interface ReelInteractionListener {
        void onLikeClicked(Reel reel, int position);
        void onCommentClicked(Reel reel, int position);
        void onShareClicked(Reel reel, int position);
        void onDeleteClicked(Reel reel, int position);
    }

    public ReelAdapter(Context context, ReelInteractionListener listener) {
        this.context = context;
        this.interactionListener = listener;
    }

    public void setReels(List<Reel> reels) {
        this.reelList.clear();
        if (reels != null) {
            this.reelList.addAll(reels);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reel, parent, false);
        return new ReelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReelViewHolder holder, int position) {
        Reel reel = reelList.get(position);
        holder.bind(reel, interactionListener);
    }

    @Override
    public void onViewRecycled(@NonNull ReelViewHolder holder) {
        super.onViewRecycled(holder);
        holder.releasePlayer();
    }

    @Override
    public int getItemCount() {
        return reelList.size();
    }

    public static class ReelViewHolder extends RecyclerView.ViewHolder {

        private final PlayerView playerView;
        private final TextView tvCaption, tvLikesCount, tvCommentsCount, tvUsername, tvErrorText;
        private final ImageButton btnLike, btnComment, btnShare, btnDelete;
        private ExoPlayer player;
        private Reel currentReel;

        public ReelViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvLikesCount = itemView.findViewById(R.id.tvLikesCount);
            tvCommentsCount = itemView.findViewById(R.id.tvCommentsCount);
            tvErrorText = itemView.findViewById(R.id.tvErrorText);

            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        @OptIn(markerClass = UnstableApi.class)
        public void bind(Reel reel, ReelInteractionListener listener) {
            this.currentReel = reel;
            tvCaption.setText(reel.getCaption());
            if (tvUsername != null) tvUsername.setText(reel.getUsername());
            tvLikesCount.setText(String.valueOf(reel.getLikes()));
            tvCommentsCount.setText(String.valueOf(reel.getComments()));
            if (tvErrorText != null) tvErrorText.setVisibility(View.GONE);

            btnLike.setOnClickListener(v -> {
                if (listener != null) listener.onLikeClicked(reel, getAdapterPosition());
            });
            btnComment.setOnClickListener(v -> {
                if (listener != null) listener.onCommentClicked(reel, getAdapterPosition());
            });
            btnShare.setOnClickListener(v -> {
                if (listener != null) listener.onShareClicked(reel, getAdapterPosition());
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClicked(reel, getAdapterPosition());
            });

            // Tap screen to Play/Pause toggle
            playerView.setOnClickListener(v -> {
                if (player != null) {
                    if (player.isPlaying()) {
                        player.pause();
                    } else {
                        player.play();
                    }
                }
            });
        }

        public void preparePlayer() {
            if (currentReel == null || currentReel.getVideoUrl() == null || currentReel.getVideoUrl().trim().isEmpty()) {
                showError("Invalid Video URL");
                return;
            }

            if (player == null) {
                player = new ExoPlayer.Builder(itemView.getContext()).build();
                playerView.setPlayer(player);
                player.setRepeatMode(Player.REPEAT_MODE_ONE);

                player.addListener(new Player.Listener() {
                    @Override
                    public void onPlayerError(@NonNull PlaybackException error) {
                        Log.e(TAG, "ExoPlayer Error [" + currentReel.getVideoUrl() + "]: " + error.getMessage(), error);
                        showError("Playback Error: " + error.getErrorCodeName());
                    }

                    @Override
                    public void onPlaybackStateChanged(int state) {
                        if (state == Player.STATE_READY) {
                            if (tvErrorText != null) tvErrorText.setVisibility(View.GONE);
                        }
                    }
                });
            }

            MediaItem mediaItem = MediaItem.fromUri(currentReel.getVideoUrl());
            player.setMediaItem(mediaItem);
            player.prepare();
        }

        public void playPlayer() {
            if (player == null) {
                preparePlayer();
            }
            if (player != null) {
                player.setPlayWhenReady(true);
                player.play();
            }
        }

        public void pausePlayer() {
            if (player != null) {
                player.setPlayWhenReady(false);
                player.pause();
            }
        }

        public void releasePlayer() {
            if (player != null) {
                player.stop();
                player.release();
                player = null;
                playerView.setPlayer(null);
            }
        }

        private void showError(String msg) {
            if (tvErrorText != null) {
                tvErrorText.setText(msg);
                tvErrorText.setVisibility(View.VISIBLE);
            }
        }
    }
}
