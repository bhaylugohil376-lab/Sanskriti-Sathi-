package com.sanskritisathi.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReelAdapter
        extends RecyclerView.Adapter<ReelAdapter.ReelViewHolder> {

    private final Context context;
    private final List<Reel> reelList;

    public ReelAdapter(
            Context context,
            List<Reel> reelList) {

        this.context = context;
        this.reelList = reelList;

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {

        if (position >= 0
                && position < reelList.size()
                && reelList.get(position) != null
                && reelList.get(position).getId() != null) {

            return reelList
                    .get(position)
                    .getId()
                    .hashCode();
        }

        return position;
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(context)
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

        Reel reel = reelList.get(position);

        holder.bind(reel);
    }

    @Override
    public int getItemCount() {
        return reelList.size();
    }

    // =========================================================
    // CURRENT VIDEO
    // =========================================================

    public void resumeCurrentVideo() {

        RecyclerView recyclerView =
                findRecyclerView();

        if (recyclerView == null) {
            return;
        }

        LinearLayoutManager lm =
                (LinearLayoutManager)
                        recyclerView.getLayoutManager();

        if (lm == null) {
            return;
        }

        int position =
                lm.findFirstCompletelyVisibleItemPosition();

        if (position == RecyclerView.NO_POSITION) {

            position =
                    lm.findFirstVisibleItemPosition();
        }

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        View view =
                lm.findViewByPosition(position);

        if (view == null) {
            return;
        }

        RecyclerView.ViewHolder vh =
                recyclerView.getChildViewHolder(view);

        if (vh instanceof ReelViewHolder) {

            ReelViewHolder holder =
                    (ReelViewHolder) vh;

            holder.play();
        }
    }

    public void pauseAllVideos() {

        RecyclerView recyclerView =
                findRecyclerView();

        if (recyclerView == null) {
            return;
        }

        for (int i = 0;
             i < recyclerView.getChildCount();
             i++) {

            View child =
                    recyclerView.getChildAt(i);

            RecyclerView.ViewHolder vh =
                    recyclerView.getChildViewHolder(child);

            if (vh instanceof ReelViewHolder) {

                ((ReelViewHolder) vh).pause();
            }
        }
    }

    public void releaseAllVideos() {

        RecyclerView recyclerView =
                findRecyclerView();

        if (recyclerView == null) {
            return;
        }

        for (int i = 0;
             i < recyclerView.getChildCount();
             i++) {

            View child =
                    recyclerView.getChildAt(i);

            RecyclerView.ViewHolder vh =
                    recyclerView.getChildViewHolder(child);

            if (vh instanceof ReelViewHolder) {

                ((ReelViewHolder) vh).releasePlayer();
            }
        }
    }

    private RecyclerView findRecyclerView() {

        if (context instanceof ReelActivity) {

            return ((ReelActivity) context)
                    .getReelRecyclerView();
        }

        return null;
    }

    // =========================================================
    // RECYCLE
    // =========================================================

    @Override
    public void onViewRecycled(
            @NonNull ReelViewHolder holder) {

        holder.releasePlayer();

        super.onViewRecycled(holder);
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    class ReelViewHolder
            extends RecyclerView.ViewHolder {

        private final PlayerView playerView;

        private final TextView errorText;
        private final ImageView profileImage;
        private final TextView usernameText;
        private final TextView captionText;

        private final ImageButton likeButton;
        private final ImageButton commentButton;
        private final ImageButton shareButton;
        private final ImageButton deleteButton;

        private final TextView likesText;
        private final TextView commentsText;
        private final TextView viewsText;

        private ExoPlayer player;

        ReelViewHolder(
                @NonNull View itemView) {

            super(itemView);

            playerView =
                    itemView.findViewById(
                            R.id.reelPlayerView
                    );

            errorText =
                    itemView.findViewById(
                            R.id.reelErrorText
                    );

            profileImage =
                    itemView.findViewById(
                            R.id.reelProfileImage
                    );

            usernameText =
                    itemView.findViewById(
                            R.id.reelUsernameText
                    );

            captionText =
                    itemView.findViewById(
                            R.id.reelCaptionText
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

            setupPlayer();

            playerView.setOnClickListener(
                    v -> togglePlayback()
            );
        }

        // =====================================================
        // PLAYER
        // =====================================================

        private void setupPlayer() {

            player =
                    new ExoPlayer.Builder(context)
                            .build();

            playerView.setPlayer(player);

            player.setRepeatMode(
                    Player.REPEAT_MODE_ONE
            );

            player.addListener(
                    new Player.Listener() {

                        @Override
                        public void onPlaybackStateChanged(
                                int state) {

                            if (state ==
                                    Player.STATE_READY) {

                                if (errorText != null) {
                                    errorText.setVisibility(
                                            View.GONE
                                    );
                                }
                            }
                        }

                        @Override
                        public void onPlayerError(
                                @NonNull
                                        androidx.media3.common.PlaybackException error) {

                            if (errorText != null) {

                                errorText.setText(
                                        "Video unavailable"
                                );

                                errorText.setVisibility(
                                        View.VISIBLE
                                );
                            }
                        }
                    }
            );
        }

        // =====================================================
        // BIND
        // =====================================================

        void bind(Reel reel) {

            if (reel == null) {
                return;
            }

            String username =
                    reel.getUsername();

            if (username == null
                    || username.trim().isEmpty()) {

                username = "Sanskriti User";
            }

            usernameText.setText(username);

            String caption =
                    reel.getCaption();

            captionText.setText(
                    caption == null
                            ? ""
                            : caption
            );

            likesText.setText(
                    String.valueOf(
                            Math.max(
                                    0,
                                    reel.getLikes()
                            )
                    )
            );

            commentsText.setText(
                    String.valueOf(
                            Math.max(
                                    0,
                                    reel.getComments()
                            )
                    )
            );

            viewsText.setText(
                    String.valueOf(
                            Math.max(
                                    0,
                                    reel.getViews()
                            )
                    )
            );

            if (deleteButton != null) {

                deleteButton.setVisibility(
                        reel.isOwnReel()
                                ? View.VISIBLE
                                : View.GONE
                );
            }

            updateLikeIcon(reel);

            String videoUrl =
                    reel.getVideoUrl();

            if (videoUrl == null
                    || videoUrl.trim().isEmpty()) {

                if (errorText != null) {

                    errorText.setText(
                            "Video unavailable"
                    );

                    errorText.setVisibility(
                            View.VISIBLE
                    );
                }

                return;
            }

            if (errorText != null) {
                errorText.setVisibility(
                        View.GONE
                );
            }

            try {

                player.stop();

                player.clearMediaItems();

                MediaItem mediaItem =
                        MediaItem.fromUri(
                                Uri.parse(videoUrl)
                        );

                player.setMediaItem(mediaItem);

                player.prepare();

                /*
                 * ReelActivity decides which
                 * visible Reel should play.
                 */
                player.setPlayWhenReady(false);

            } catch (Exception e) {

                if (errorText != null) {

                    errorText.setText(
                            "Video unavailable"
                    );

                    errorText.setVisibility(
                            View.VISIBLE
                    );
                }
            }

            setupActions(reel);
        }

        // =====================================================
        // ACTIONS
        // =====================================================

        private void setupActions(Reel reel) {

            likeButton.setOnClickListener(
                    v -> toggleLike(reel)
            );

            commentButton.setOnClickListener(
                    v -> openComments(reel)
            );

            shareButton.setOnClickListener(
                    v -> shareReel(reel)
            );

            if (deleteButton != null) {

                deleteButton.setOnClickListener(
                        v -> deleteReel(reel)
                );
            }
        }

        // =====================================================
        // LIKE
        // =====================================================

        private void toggleLike(Reel reel) {

            if (!SupabaseAuthManager
                    .isLoggedIn(context)) {

                Toast.makeText(
                        context,
                        "Please login first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            likeButton.setEnabled(false);

            ReelSupabaseHelper.toggleReelLike(
                    context,
                    reel.getId(),
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            boolean liked =
                                    !reel.isLiked();

                            reel.setLiked(liked);

                            int count =
                                    Math.max(
                                            0,
                                            reel.getLikes()
                                    );

                            reel.setLikes(
                                    liked
                                            ? count + 1
                                            : Math.max(
                                                    0,
                                                    count - 1
                                            )
                            );

                            likesText.setText(
                                    String.valueOf(
                                            reel.getLikes()
                                    )
                            );

                            updateLikeIcon(reel);

                            likeButton.setEnabled(true);
                        }

                        @Override
                        public void onError(
                                String message) {

                            likeButton.setEnabled(true);

                            Toast.makeText(
                                    context,
                                    message == null
                                            ? "Like update failed"
                                            : message,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        }

        private void updateLikeIcon(Reel reel) {

            if (reel.isLiked()) {

                likeButton.setImageResource(
                        android.R.drawable
                                .btn_star_big_on
                );

                likeButton.setContentDescription(
                        "Unlike"
                );

            } else {

                likeButton.setImageResource(
                        android.R.drawable
                                .btn_star_big_off
                );

                likeButton.setContentDescription(
                        "Like"
                );
            }
        }

        // =====================================================
        // COMMENTS
        // =====================================================

        private void openComments(Reel reel) {

            Intent intent =
                    new Intent(
                            context,
                            ReelCommentsActivity.class
                    );

            intent.putExtra(
                    "reel_id",
                    reel.getId()
            );

            context.startActivity(intent);
        }

        // =====================================================
        // SHARE
        // =====================================================

        private void shareReel(Reel reel) {

            String url =
                    reel.getVideoUrl();

            if (url == null
                    || url.trim().isEmpty()) {

                Toast.makeText(
                        context,
                        "Video link available nahi hai",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String caption =
                    reel.getCaption();

            String shareText =
                    (caption == null
                            ? ""
                            : caption)
                            + "\n\n"
                            + url;

            Intent intent =
                    new Intent(
                            Intent.ACTION_SEND
                    );

            intent.setType(
                    "text/plain"
            );

            intent.putExtra(
                    Intent.EXTRA_TEXT,
                    shareText
            );

            context.startActivity(
                    Intent.createChooser(
                            intent,
                            "Share Reel"
                    )
            );
        }

        // =====================================================
        // DELETE
        // =====================================================

        private void deleteReel(Reel reel) {

            new androidx.appcompat.app.AlertDialog.Builder(
                    context
            )
                    .setTitle("Delete Reel?")
                    .setMessage(
                            "Kya aap is Reel ko delete karna chahte ho?"
                    )
                    .setNegativeButton(
                            "Cancel",
                            null
                    )
                    .setPositiveButton(
                            "Delete",
                            (dialog, which) -> {

                                ReelSupabaseHelper.deleteReel(
                                        context,
                                        reel.getId(),
                                        new ReelSupabaseHelper.ActionCallback() {

                                            @Override
                                            public void onSuccess() {

                                                int position =
                                                        getBindingAdapterPosition();

                                                if (position !=
                                                        RecyclerView.NO_POSITION) {

                                                    reelList.remove(
                                                            position
                                                    );

                                                    notifyItemRemoved(
                                                            position
                                                    );
                                                }

                                                Toast.makeText(
                                                        context,
                                                        "Reel deleted",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }

                                            @Override
                                            public void onError(
                                                    String message) {

                                                Toast.makeText(
                                                        context,
                                                        message == null
                                                                ? "Delete failed"
                                                                : message,
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        }
                                );
                            }
                    )
                    .show();
        }

        // =====================================================
        // PLAY / PAUSE
        // =====================================================

        void play() {

            if (player != null) {

                player.setPlayWhenReady(true);
            }
        }

        void pause() {

            if (player != null) {

                player.setPlayWhenReady(false);
            }
        }

        void togglePlayback() {

            if (player == null) {
                return;
            }

            if (player.isPlaying()) {

                player.pause();

            } else {

                player.play();
            }
        }

        void releasePlayer() {

            if (player != null) {

                player.release();

                player = null;
            }
        }
    }
}
