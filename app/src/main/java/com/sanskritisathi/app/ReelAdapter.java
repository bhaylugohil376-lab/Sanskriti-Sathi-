package com.sanskritisathi.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
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

    // =========================================================
    // STABLE ID
    // =========================================================

    @Override
    public long getItemId(int position) {

        if (position < 0 || position >= reelList.size()) {
            return RecyclerView.NO_ID;
        }

        Reel reel = reelList.get(position);

        if (reel == null
                || TextUtils.isEmpty(reel.getId())) {

            return position;
        }

        return reel.getId().hashCode();
    }

    // =========================================================
    // CREATE VIEW HOLDER
    // =========================================================

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

    // =========================================================
    // BIND
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull ReelViewHolder holder,
            int position) {

        Reel reel = reelList.get(position);

        if (reel == null) {
            return;
        }

        holder.bind(reel);
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
    // COUNT
    // =========================================================

    @Override
    public int getItemCount() {
        return reelList == null
                ? 0
                : reelList.size();
    }

    // =========================================================
    // PAUSE ALL
    // =========================================================

    public void pauseAllVideos() {

        if (reelList == null) {
            return;
        }

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

            RecyclerView.ViewHolder viewHolder =
                    recyclerView.getChildViewHolder(child);

            if (viewHolder instanceof ReelViewHolder) {

                ReelViewHolder holder =
                        (ReelViewHolder) viewHolder;

                holder.pausePlayer();
            }
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

        int completelyVisible =
                ((androidx.recyclerview.widget.LinearLayoutManager)
                        recyclerView.getLayoutManager())
                        .findFirstCompletelyVisibleItemPosition();

        if (completelyVisible != RecyclerView.NO_POSITION) {

            RecyclerView.ViewHolder holder =
                    recyclerView.findViewHolderForAdapterPosition(
                            completelyVisible
                    );

            if (holder instanceof ReelViewHolder) {

                ((ReelViewHolder) holder)
                        .playPlayer();

                return;
            }
        }

        int firstVisible =
                ((androidx.recyclerview.widget.LinearLayoutManager)
                        recyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition();

        if (firstVisible != RecyclerView.NO_POSITION) {

            RecyclerView.ViewHolder holder =
                    recyclerView.findViewHolderForAdapterPosition(
                            firstVisible
                    );

            if (holder instanceof ReelViewHolder) {

                ((ReelViewHolder) holder)
                        .playPlayer();
            }
        }
    }

    // =========================================================
    // RELEASE ALL
    // =========================================================

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

            RecyclerView.ViewHolder viewHolder =
                    recyclerView.getChildViewHolder(child);

            if (viewHolder instanceof ReelViewHolder) {

                ((ReelViewHolder) viewHolder)
                        .releasePlayer();
            }
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
    // VIEW HOLDER
    // =========================================================

    public class ReelViewHolder
            extends RecyclerView.ViewHolder {

        private final PlayerView reelPlayerView;

        private final TextView reelErrorText;

        private final ImageView reelProfileImage;

        private final TextView reelUsernameText;

        private final TextView reelCaptionText;

        private final TextView reelLikesText;

        private final TextView reelCommentsText;

        private final TextView reelViewsText;

        private final ImageButton reelLikeButton;

        private final ImageButton reelCommentButton;

        private final ImageButton reelShareButton;

        private final ImageButton reelDeleteButton;

        private final ImageButton reelEditButton;

        private ExoPlayer player;

        private String currentVideoUrl = "";

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

            reelEditButton =
                    itemView.findViewById(
                            R.id.reelEditButton
                    );
        }

        // =====================================================
        // BIND REEL
        // =====================================================

        void bind(Reel reel) {

            releasePlayer();

            currentVideoUrl =
                    reel.getVideoUrl();

            // -------------------------------------------------
            // USERNAME
            // -------------------------------------------------

            String username =
                    reel.getUsername();

            if (TextUtils.isEmpty(username)) {

                username =
                        "Sanskriti User";
            }

            reelUsernameText.setText(
                    username
            );

            // -------------------------------------------------
            // CAPTION
            // -------------------------------------------------

            String caption =
                    reel.getCaption();

            if (caption == null) {
                caption = "";
            }

            reelCaptionText.setText(
                    caption
            );

            // -------------------------------------------------
            // COUNTS
            // -------------------------------------------------

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

            // -------------------------------------------------
            // PROFILE
            // -------------------------------------------------

            if (reelProfileImage != null) {

                reelProfileImage.setImageResource(
                        R.drawable.icon_foreground
                );
            }

            // -------------------------------------------------
            // DELETE
            // -------------------------------------------------

            if (reel.isOwnReel()) {

                reelDeleteButton.setVisibility(
                        View.VISIBLE
                );

            } else {

                reelDeleteButton.setVisibility(
                        View.GONE
                );
            }

            // -------------------------------------------------
            // EDIT
            // -------------------------------------------------

            if (reel.isOwnReel()) {

                reelEditButton.setVisibility(
                        View.VISIBLE
                );

            } else {

                reelEditButton.setVisibility(
                        View.GONE
                );
            }

            // -------------------------------------------------
            // LIKE ICON
            // -------------------------------------------------

            updateLikeUI(
                    reel.isLiked()
            );

            // -------------------------------------------------
            // PLAYER
            // -------------------------------------------------

            setupPlayer(
                    reel
            );

            // -------------------------------------------------
            // LIKE
            // -------------------------------------------------

            reelLikeButton.setOnClickListener(
                    v -> handleLike(reel)
            );

            // -------------------------------------------------
            // COMMENTS
            // -------------------------------------------------

            reelCommentButton.setOnClickListener(
                    v -> openComments(reel)
            );

            // -------------------------------------------------
            // SHARE
            // -------------------------------------------------

            reelShareButton.setOnClickListener(
                    v -> shareReel(reel)
            );

            // -------------------------------------------------
            // DELETE
            // -------------------------------------------------

            reelDeleteButton.setOnClickListener(
                    v -> confirmDelete(reel)
            );

            // -------------------------------------------------
            // EDIT
            // -------------------------------------------------

            reelEditButton.setOnClickListener(
                    v -> openEdit(reel)
            );

            // -------------------------------------------------
            // TAP VIDEO = PLAY / PAUSE
            // -------------------------------------------------

            reelPlayerView.setOnClickListener(
                    v -> togglePlayer()
            );
        }

        // =====================================================
        // PLAYER SETUP
        // =====================================================

        private void setupPlayer(Reel reel) {

            String videoUrl =
                    reel.getVideoUrl();

            if (TextUtils.isEmpty(videoUrl)) {

                showVideoError(
                        "Video unavailable"
                );

                return;
            }

            reelErrorText.setVisibility(
                    View.GONE
            );

            player =
                    new ExoPlayer.Builder(context)
                            .build();

            reelPlayerView.setPlayer(
                    player
            );

            player.setRepeatMode(
                    Player.REPEAT_MODE_ONE
            );

            MediaItem mediaItem =
                    MediaItem.fromUri(
                            Uri.parse(videoUrl)
                    );

            player.setMediaItem(
                    mediaItem
            );

            player.addListener(
                    new Player.Listener() {

                        @Override
                        public void onPlaybackStateChanged(
                                int playbackState) {

                            if (playbackState ==
                                    Player.STATE_READY) {

                                reelErrorText.setVisibility(
                                        View.GONE
                                );
                            }

                            if (playbackState ==
                                    Player.STATE_BUFFERING) {

                                // Keep existing UI.
                            }
                        }

                        @Override
                        public void onPlayerError(
                                @NonNull androidx.media3.common.PlaybackException error) {

                            showVideoError(
                                    "Video unavailable"
                            );
                        }
                    }
            );

            player.prepare();

            /*
             * ReelActivity decides which visible reel
             * should start playing.
             */
            player.setPlayWhenReady(
                    false
            );
        }

        // =====================================================
        // LIKE
        // =====================================================

        private void handleLike(Reel reel) {

            if (!SupabaseAuthManager.isLoggedIn(
                    context
            )) {

                Toast.makeText(
                        context,
                        "Please login first.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (TextUtils.isEmpty(
                    reel.getId()
            )) {

                return;
            }

            reelLikeButton.setEnabled(
                    false
            );

            final boolean oldLiked =
                    reel.isLiked();

            ReelSupabaseHelper.toggleReelLike(
                    context,
                    reel.getId(),
                    oldLiked,
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            reel.setLiked(
                                    !oldLiked
                            );

                            int currentLikes =
                                    Math.max(
                                            0,
                                            reel.getLikes()
                                    );

                            if (!oldLiked) {

                                currentLikes++;

                            } else {

                                currentLikes =
                                        Math.max(
                                                0,
                                                currentLikes - 1
                                        );
                            }

                            reel.setLikes(
                                    currentLikes
                            );

                            updateLikeUI(
                                    reel.isLiked()
                            );

                            reelLikesText.setText(
                                    String.valueOf(
                                            currentLikes
                                    )
                            );

                            reelLikeButton.setEnabled(
                                    true
                            );
                        }

                        @Override
                        public void onError(
                                String message) {

                            reelLikeButton.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    context,
                                    TextUtils.isEmpty(message)
                                            ? "Like update failed."
                                            : message,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        }

        // =====================================================
        // LIKE UI
        // =====================================================

        private void updateLikeUI(
                boolean liked) {

            if (liked) {

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
        // COMMENTS
        // =====================================================

        private void openComments(
                Reel reel) {

            if (TextUtils.isEmpty(
                    reel.getId()
            )) {

                return;
            }

            Intent intent =
                    new Intent(
                            context,
                            ReelCommentsActivity.class
                    );

            intent.putExtra(
                    "reel_id",
                    reel.getId()
            );

            context.startActivity(
                    intent
            );
        }

        // =====================================================
        // SHARE
        // =====================================================

        private void shareReel(
                Reel reel) {

            String videoUrl =
                    reel.getVideoUrl();

            if (TextUtils.isEmpty(videoUrl)) {

                Toast.makeText(
                        context,
                        "Video link available nahi hai.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String caption =
                    reel.getCaption();

            String shareText =
                    TextUtils.isEmpty(caption)
                            ? videoUrl
                            : caption
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
        // DELETE CONFIRM
        // =====================================================

        private void confirmDelete(
                Reel reel) {

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
                            (dialog, which) ->
                                    deleteReel(reel)
                    )
                    .show();
        }

        // =====================================================
        // DELETE
        // =====================================================

        private void deleteReel(
                Reel reel) {

            reelDeleteButton.setEnabled(
                    false
            );

            ReelSupabaseHelper.deleteReel(
                    context,
                    reel.getId(),
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            int position =
                                    getBindingAdapterPosition();

                            if (position !=
                                    RecyclerView.NO_POSITION
                                    && position <
                                    reelList.size()) {

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

                            reelDeleteButton.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    context,
                                    TextUtils.isEmpty(message)
                                            ? "Reel delete failed."
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        }

        // =====================================================
        // EDIT
        // =====================================================

        private void openEdit(
                Reel reel) {

            /*
             * Edit screen ka final Activity agar project me
             * already available hai to yahan connect karenge.
             *
             * Filhaal unnecessary Activity crash avoid karne
             * ke liye safe message.
             */

            Toast.makeText(
                    context,
                    "Edit Reel option ready hai.",
                    Toast.LENGTH_SHORT
            ).show();
        }

        // =====================================================
        // PLAYER TOGGLE
        // =====================================================

        private void togglePlayer() {

            if (player == null) {
                return;
            }

            if (player.isPlaying()) {

                player.pause();

            } else {

                player.play();
            }
        }

        // =====================================================
        // PLAY
        // =====================================================

        void playPlayer() {

            if (player != null) {

                player.play();
            }
        }

        // =====================================================
        // PAUSE
        // =====================================================

        void pausePlayer() {

            if (player != null) {

                player.pause();
            }
        }

        // =====================================================
        // ERROR
        // =====================================================

        private void showVideoError(
                String message) {

            reelErrorText.setText(
                    TextUtils.isEmpty(message)
                            ? "Video unavailable"
                            : message
            );

            reelErrorText.setVisibility(
                    View.VISIBLE
            );
        }

        // =====================================================
        // RELEASE
        // =====================================================

        void releasePlayer() {

            if (player != null) {

                player.stop();

                player.release();

                player = null;
            }

            reelPlayerView.setPlayer(
                    null
            );

            currentVideoUrl = "";
        }
    }
}
