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
import androidx.media3.common.PlaybackException;
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

        if (position < 0 ||
                position >= reelList.size()) {
            return RecyclerView.NO_ID;
        }

        Reel reel = reelList.get(position);

        if (reel == null ||
                reel.getId() == null) {
            return position;
        }

        return reel.getId().hashCode();
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
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
    // PAUSE ALL
    // =========================================================

    public void pauseAllVideos() {

        for (ReelViewHolder holder :
                getVisibleHolders()) {

            holder.pauseVideo();
        }
    }

    // =========================================================
    // RESUME CURRENT
    // =========================================================

    public void resumeCurrentVideo() {

        RecyclerView recyclerView =
                findRecyclerView();

        if (recyclerView == null) {
            return;
        }

        LinearLayoutManager manager =
                (LinearLayoutManager)
                        recyclerView.getLayoutManager();

        if (manager == null) {
            return;
        }

        int completelyVisible =
                manager.findFirstCompletelyVisibleItemPosition();

        int firstVisible =
                manager.findFirstVisibleItemPosition();

        int position =
                completelyVisible != RecyclerView.NO_POSITION
                        ? completelyVisible
                        : firstVisible;

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        RecyclerView.ViewHolder viewHolder =
                recyclerView.findViewHolderForAdapterPosition(
                        position
                );

        if (viewHolder instanceof ReelViewHolder) {

            ((ReelViewHolder) viewHolder)
                    .playVideo();
        }
    }

    // =========================================================
    // RELEASE ALL
    // =========================================================

    public void releaseAllVideos() {

        for (ReelViewHolder holder :
                getVisibleHolders()) {

            holder.releasePlayer();
        }
    }

    // =========================================================
    // FIND RECYCLER VIEW
    // =========================================================

    private RecyclerView findRecyclerView() {

        if (context instanceof ReelActivity) {

            return ((ReelActivity) context)
                    .getReelRecyclerView();
        }

        return null;
    }

    // =========================================================
    // VISIBLE HOLDERS
    // =========================================================

    private List<ReelViewHolder> getVisibleHolders() {

        java.util.ArrayList<ReelViewHolder>
                holders =
                new java.util.ArrayList<>();

        RecyclerView recyclerView =
                findRecyclerView();

        if (recyclerView == null) {
            return holders;
        }

        for (int i = 0;
             i < recyclerView.getChildCount();
             i++) {

            View child =
                    recyclerView.getChildAt(i);

            RecyclerView.ViewHolder holder =
                    recyclerView.getChildViewHolder(
                            child
                    );

            if (holder instanceof ReelViewHolder) {

                holders.add(
                        (ReelViewHolder) holder
                );
            }
        }

        return holders;
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public class ReelViewHolder
            extends RecyclerView.ViewHolder {

        private final PlayerView reelPlayerView;

        private final TextView reelErrorText;
        private final ImageView reelProfileImage;
        private final TextView reelUsernameText;
        private final TextView reelCaptionText;

        private final ImageButton reelLikeButton;
        private final ImageButton reelCommentButton;
        private final ImageButton reelShareButton;
        private final ImageButton reelDeleteButton;

        private final TextView reelLikesText;
        private final TextView reelCommentsText;
        private final TextView reelViewsText;

        private ExoPlayer player;

        private Reel currentReel;

        public ReelViewHolder(
                @NonNull View itemView) {

            super(itemView);

            reelPlayerView =
                    itemView.findViewById(
                            R.id.reelPlayerView
                    );

            reelErrorText =
                    itemView.findViewById(
                            R.id.reelErrorText
                    );

            reelProfileImage =
                    itemView.findViewById(
                            R.id.reelProfileImage
                    );

            reelUsernameText =
                    itemView.findViewById(
                            R.id.reelUsernameText
                    );

            reelCaptionText =
                    itemView.findViewById(
                            R.id.reelCaptionText
                    );

            reelLikeButton =
                    itemView.findViewById(
                            R.id.reelLikeButton
                    );

            reelCommentButton =
                    itemView.findViewById(
                            R.id.reelCommentButton
                    );

            reelShareButton =
                    itemView.findViewById(
                            R.id.reelShareButton
                    );

            reelDeleteButton =
                    itemView.findViewById(
                            R.id.reelDeleteButton
                    );

            reelLikesText =
                    itemView.findViewById(
                            R.id.reelLikesText
                    );

            reelCommentsText =
                    itemView.findViewById(
                            R.id.reelCommentsText
                    );

            reelViewsText =
                    itemView.findViewById(
                            R.id.reelViewsText
                    );

            setupPlayer();
            setupClickListeners();
        }

        // =====================================================
        // PLAYER
        // =====================================================

        private void setupPlayer() {

            player =
                    new ExoPlayer.Builder(context)
                            .build();

            reelPlayerView.setPlayer(player);

            player.setRepeatMode(
                    Player.REPEAT_MODE_ONE
            );

            player.setPlayWhenReady(false);

            player.addListener(
                    new Player.Listener() {

                        @Override
                        public void onPlaybackStateChanged(
                                int state) {

                            if (state ==
                                    Player.STATE_READY) {

                                if (reelErrorText != null) {
                                    reelErrorText.setVisibility(
                                            View.GONE
                                    );
                                }
                            }
                        }

                        @Override
                        public void onPlayerError(
                                @NonNull PlaybackException error) {

                            if (reelErrorText != null) {

                                reelErrorText.setText(
                                        "Video unavailable"
                                );

                                reelErrorText.setVisibility(
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

        public void bind(Reel reel) {

            currentReel = reel;

            if (reel == null) {
                return;
            }

            String username =
                    reel.getUsername();

            if (username == null ||
                    username.trim().isEmpty()) {

                username =
                        "Sanskriti User";
            }

            reelUsernameText.setText(
                    username
            );

            String caption =
                    reel.getCaption();

            reelCaptionText.setText(
                    caption == null
                            ? ""
                            : caption
            );

            reelLikesText.setText(
                    String.valueOf(
                            Math.max(
                                    0,
                                    reel.getLikes()
                            )
                    )
            );

            reelCommentsText.setText(
                    String.valueOf(
                            Math.max(
                                    0,
                                    reel.getComments()
                            )
                    )
            );

            reelViewsText.setText(
                    String.valueOf(
                            Math.max(
                                    0,
                                    reel.getViews()
                            )
                    )
            );

            if (reelDeleteButton != null) {

                reelDeleteButton.setVisibility(
                        reel.isOwnReel()
                                ? View.VISIBLE
                                : View.GONE
                );
            }

            updateLikeIcon();

            setupVideo(reel.getVideoUrl());

            checkLikeState();
        }

        // =====================================================
        // VIDEO
        // =====================================================

        private void setupVideo(String videoUrl) {

            player.stop();

            player.clearMediaItems();

            if (reelErrorText != null) {
                reelErrorText.setVisibility(
                        View.GONE
                );
            }

            if (videoUrl == null ||
                    videoUrl.trim().isEmpty()) {

                if (reelErrorText != null) {

                    reelErrorText.setText(
                            "Video unavailable"
                    );

                    reelErrorText.setVisibility(
                            View.VISIBLE
                    );
                }

                return;
            }

            try {

                MediaItem mediaItem =
                        MediaItem.fromUri(
                                Uri.parse(videoUrl)
                        );

                player.setMediaItem(
                        mediaItem
                );

                player.prepare();

                /*
                 * Important:
                 * ReelActivity decides which
                 * reel should actually play.
                 */
                player.setPlayWhenReady(false);

            } catch (Exception e) {

                if (reelErrorText != null) {

                    reelErrorText.setText(
                            "Video unavailable"
                    );

                    reelErrorText.setVisibility(
                            View.VISIBLE
                    );
                }
            }
        }

        // =====================================================
        // PLAY
        // =====================================================

        public void playVideo() {

            if (player == null ||
                    currentReel == null) {
                return;
            }

            try {

                player.setPlayWhenReady(true);
                player.play();

                addViewOnce();

            } catch (Exception ignored) {
            }
        }

        // =====================================================
        // PAUSE
        // =====================================================

        public void pauseVideo() {

            if (player == null) {
                return;
            }

            try {
                player.pause();
            } catch (Exception ignored) {
            }
        }

        // =====================================================
        // VIEW COUNT
        // =====================================================

        private boolean viewAdded = false;

        private void addViewOnce() {

            if (viewAdded ||
                    currentReel == null ||
                    currentReel.getId() == null) {
                return;
            }

            viewAdded = true;

            ReelSupabaseHelper.addReelView(
                    context,
                    currentReel.getId(),
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            currentReel.setViews(
                                    currentReel.getViews() + 1
                            );

                            reelViewsText.setText(
                                    String.valueOf(
                                            currentReel.getViews()
                                    )
                            );
                        }

                        @Override
                        public void onError(
                                String message) {
                            // Silent failure for view count.
                        }
                    }
            );
        }

        // =====================================================
        // LIKE STATE
        // =====================================================

        private void checkLikeState() {

            if (currentReel == null ||
                    currentReel.getId() == null) {
                return;
            }

            ReelSupabaseHelper.checkReelLike(
                    context,
                    currentReel.getId(),
                    new ReelSupabaseHelper.LikeCheckCallback() {

                        @Override
                        public void onResult(
                                boolean liked) {

                            currentReel.setLiked(
                                    liked
                            );

                            updateLikeIcon();
                        }

                        @Override
                        public void onError(
                                String message) {
                            // Keep existing state.
                        }
                    }
            );
        }

        // =====================================================
        // LIKE ICON
        // =====================================================

        private void updateLikeIcon() {

            if (reelLikeButton == null ||
                    currentReel == null) {
                return;
            }

            if (currentReel.isLiked()) {

                reelLikeButton.setImageResource(
                        android.R.drawable.btn_star_big_on
                );

                reelLikeButton.setContentDescription(
                        "Unlike"
                );

            } else {

                reelLikeButton.setImageResource(
                        android.R.drawable.btn_star_big_off
                );

                reelLikeButton.setContentDescription(
                        "Like"
                );
            }
        }

        // =====================================================
        // CLICK LISTENERS
        // =====================================================

        private void setupClickListeners() {

            if (reelLikeButton != null) {

                reelLikeButton.setOnClickListener(
                        v -> toggleLike()
                );
            }

            if (reelCommentButton != null) {

                reelCommentButton.setOnClickListener(
                        v -> openComments()
                );
            }

            if (reelShareButton != null) {

                reelShareButton.setOnClickListener(
                        v -> shareReel()
                );
            }

            if (reelDeleteButton != null) {

                reelDeleteButton.setOnClickListener(
                        v -> deleteReel()
                );
            }

            /*
             * Tap video to pause/play.
             */
            if (reelPlayerView != null) {

                reelPlayerView.setOnClickListener(
                        v -> {

                            if (player == null) {
                                return;
                            }

                            if (player.isPlaying()) {
                                player.pause();
                            } else {
                                player.play();
                            }
                        }
                );
            }
        }

        // =====================================================
        // TOGGLE LIKE
        // =====================================================

        private void toggleLike() {

            if (currentReel == null ||
                    currentReel.getId() == null) {
                return;
            }

            if (!SupabaseAuthManager.isLoggedIn(
                    context)) {

                Toast.makeText(
                        context,
                        "Please login first.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (reelLikeButton != null) {
                reelLikeButton.setEnabled(false);
            }

            boolean oldLiked =
                    currentReel.isLiked();

            ReelSupabaseHelper.toggleReelLike(
                    context,
                    currentReel.getId(),
                    oldLiked,
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            boolean newLiked =
                                    !oldLiked;

                            currentReel.setLiked(
                                    newLiked
                            );

                            int likes =
                                    Math.max(
                                            0,
                                            currentReel.getLikes()
                                    );

                            if (newLiked) {
                                likes++;
                            } else if (likes > 0) {
                                likes--;
                            }

                            currentReel.setLikes(
                                    likes
                            );

                            reelLikesText.setText(
                                    String.valueOf(
                                            likes
                                    )
                            );

                            updateLikeIcon();

                            if (reelLikeButton != null) {
                                reelLikeButton.setEnabled(
                                        true
                                );
                            }
                        }

                        @Override
                        public void onError(
                                String message) {

                            if (reelLikeButton != null) {
                                reelLikeButton.setEnabled(
                                        true
                                );
                            }

                            Toast.makeText(
                                    context,
                                    message == null
                                            ? "Like update failed."
                                            : message,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        }

        // =====================================================
        // COMMENTS
        // =====================================================

        private void openComments() {

            if (currentReel == null ||
                    currentReel.getId() == null) {
                return;
            }

            Intent intent =
                    new Intent(
                            context,
                            ReelCommentsActivity.class
                    );

            intent.putExtra(
                    "reel_id",
                    currentReel.getId()
            );

            context.startActivity(intent);
        }

        // =====================================================
        // SHARE
        // =====================================================

        private void shareReel() {

            if (currentReel == null) {
                return;
            }

            String videoUrl =
                    currentReel.getVideoUrl();

            if (videoUrl == null ||
                    videoUrl.trim().isEmpty()) {

                Toast.makeText(
                        context,
                        "Video link available nahi hai.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String caption =
                    currentReel.getCaption();

            String shareText =
                    (caption == null
                            ? ""
                            : caption)
                            + "\n\n"
                            + videoUrl;

            Intent shareIntent =
                    new Intent(
                            Intent.ACTION_SEND
                    );

            shareIntent.setType(
                    "text/plain"
            );

            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    shareText
            );

            context.startActivity(
                    Intent.createChooser(
                            shareIntent,
                            "Share Reel"
                    )
            );
        }

        // =====================================================
        // DELETE
        // =====================================================

        private void deleteReel() {

            if (currentReel == null ||
                    currentReel.getId() == null) {
                return;
            }

            new androidx.appcompat.app.AlertDialog.Builder(
                    context
            )
                    .setTitle(
                            "Delete Reel?"
                    )
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
                                        currentReel.getId(),
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
                                                        "Reel deleted.",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }

                                            @Override
                                            public void onError(
                                                    String message) {

                                                Toast.makeText(
                                                        context,
                                                        message == null
                                                                ? "Reel delete failed."
                                                                : message,
                                                        Toast.LENGTH_LONG
                                                ).show();
                                            }
                                        }
                                );
                            }
                    )
                    .show();
        }

        // =====================================================
        // RECYCLE
        // =====================================================

        public void releasePlayer() {

            if (player != null) {

                try {
                    player.stop();
                } catch (Exception ignored) {
                }

                try {
                    player.release();
                } catch (Exception ignored) {
                }

                player = null;
            }
        }
    }

    // =========================================================
    // ON RECYCLED
    // =========================================================

    @Override
    public void onViewRecycled(
            @NonNull ReelViewHolder holder) {

        holder.releasePlayer();

        super.onViewRecycled(holder);
    }
}
