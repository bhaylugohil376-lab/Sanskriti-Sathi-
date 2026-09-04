package com.sanskritisathi.app;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReelAdapter
        extends RecyclerView.Adapter<ReelAdapter.ReelViewHolder> {

    public interface ReelActionListener {
        void onDelete(Reel reel, int position);
        void onError(String message);
    }

    private final Context context;
    private final List<Reel> reels;
    private final ReelActionListener listener;

    private final List<ExoPlayer> players =
            new ArrayList<>();

    public ReelAdapter(
            Context context,
            List<Reel> reels,
            ReelActionListener listener) {

        this.context = context;
        this.reels = reels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_reel,
                        parent,
                        false
                );

        return new ReelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ReelViewHolder holder,
            int position) {

        Reel reel = reels.get(position);

        // -------------------------
        // TEXT
        // -------------------------

        holder.usernameText.setText(
                reel.getUsername()
        );

        holder.captionText.setText(
                reel.getCaption()
        );

        holder.likesText.setText(
                reel.getLikes() + " likes"
        );

        holder.commentsText.setText(
                reel.getComments() + " comments"
        );

        holder.viewsText.setText(
                reel.getViews() + " views"
        );

        holder.errorText.setVisibility(
                View.GONE
        );

        // -------------------------
        // DELETE
        // -------------------------

        if (reel.isOwnReel()) {
            holder.deleteButton.setVisibility(
                    View.VISIBLE
            );
        } else {
            holder.deleteButton.setVisibility(
                    View.GONE
            );
        }

        holder.deleteButton.setOnClickListener(v -> {

            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition != RecyclerView.NO_POSITION) {

                listener.onDelete(
                        reel,
                        adapterPosition
                );
            }
        });

        // -------------------------
        // PROFILE
        // -------------------------

        holder.profileImage.setImageResource(
                R.drawable.icon_foreground
        );

        // -------------------------
        // PLAYER
        // -------------------------

        ExoPlayer player =
                new ExoPlayer.Builder(context)
                        .build();

        holder.playerView.setPlayer(player);

        String videoUrl =
                reel.getVideoUrl();

        if (videoUrl != null &&
                !videoUrl.trim().isEmpty()) {

            MediaItem mediaItem =
                    MediaItem.fromUri(
                            Uri.parse(videoUrl)
                    );

            player.setMediaItem(
                    mediaItem
            );

            player.setRepeatMode(
                    Player.REPEAT_MODE_ONE
            );

            player.prepare();

            // Start only when user taps.
            player.setPlayWhenReady(false);

            players.add(player);

        } else {

            holder.errorText.setText(
                    "Video unavailable"
            );

            holder.errorText.setVisibility(
                    View.VISIBLE
            );
        }

        // -------------------------
        // PLAY / PAUSE
        // -------------------------

        holder.playerView.setOnClickListener(v -> {

            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
        });

        // -------------------------
        // LIKE
        // -------------------------

        updateLikeButton(holder, reel);

        holder.likeButton.setOnClickListener(v -> {

            boolean newLiked =
                    !reel.isLiked();

            reel.setLiked(newLiked);

            int newLikes =
                    reel.getLikes();

            if (newLiked) {
                newLikes++;
            } else {
                newLikes =
                        Math.max(
                                0,
                                newLikes - 1
                        );
            }

            reel.setLikes(newLikes);

            holder.likesText.setText(
                    newLikes + " likes"
            );

            updateLikeButton(
                    holder,
                    reel
            );
        });

        // -------------------------
        // COMMENTS
        // -------------------------

        holder.commentButton.setOnClickListener(v ->
                listener.onError(
                        "Comments system next step mein add hoga."
                )
        );

        // -------------------------
        // SHARE
        // -------------------------

        holder.shareButton.setOnClickListener(v ->
                listener.onError(
                        "Share system next step mein add hoga."
                )
        );
    }

    private void updateLikeButton(
            ReelViewHolder holder,
            Reel reel) {

        if (reel.isLiked()) {

            holder.likeButton.setImageResource(
                    android.R.drawable
                            .btn_star_big_on
            );

        } else {

            holder.likeButton.setImageResource(
                    android.R.drawable
                            .btn_star_big_off
            );
        }
    }

    @Override
    public int getItemCount() {
        return reels.size();
    }

    // -------------------------
    // PAUSE ALL
    // -------------------------

    public void pauseAllVideos() {

        for (ExoPlayer player : players) {

            if (player != null) {
                player.pause();
            }
        }
    }

    // -------------------------
    // RELEASE ALL
    // -------------------------

    public void releaseAllVideos() {

        for (ExoPlayer player : players) {

            if (player != null) {
                player.release();
            }
        }

        players.clear();
    }

    @Override
    public void onViewRecycled(
            @NonNull ReelViewHolder holder) {

        Player player =
                holder.playerView.getPlayer();

        if (player != null) {
            player.pause();
            holder.playerView.setPlayer(null);
        }

        super.onViewRecycled(holder);
    }

    // =========================
    // VIEW HOLDER
    // =========================

    static class ReelViewHolder
            extends RecyclerView.ViewHolder {

        PlayerView playerView;

        ImageView profileImage;

        ImageButton likeButton;
        ImageButton commentButton;
        ImageButton shareButton;
        ImageButton deleteButton;

        TextView usernameText;
        TextView captionText;
        TextView likesText;
        TextView commentsText;
        TextView viewsText;
        TextView errorText;

        ReelViewHolder(
                @NonNull View itemView) {

            super(itemView);

            playerView =
                    itemView.findViewById(
                            R.id.reelPlayerView
                    );

            profileImage =
                    itemView.findViewById(
                            R.id.reelProfileImage
                    );

            likeButton =
                    itemView.findViewById(
                            R.id.reelLikeButton
                    );

            commentButton =
                    itemView.findViewById(
                            R.id.reelCommentButton
                    );

            shareButton =
                    itemView.findViewById(
                            R.id.reelShareButton
                    );

            deleteButton =
                    itemView.findViewById(
                            R.id.reelDeleteButton
                    );

            usernameText =
                    itemView.findViewById(
                            R.id.reelUsernameText
                    );

            captionText =
                    itemView.findViewById(
                            R.id.reelCaptionText
                    );

            likesText =
                    itemView.findViewById(
                            R.id.reelLikesText
                    );

            commentsText =
                    itemView.findViewById(
                            R.id.reelCommentsText
                    );

            viewsText =
                    itemView.findViewById(
                            R.id.reelViewsText
                    );

            errorText =
                    itemView.findViewById(
                            R.id.reelErrorText
                    );
        }
    }
}
