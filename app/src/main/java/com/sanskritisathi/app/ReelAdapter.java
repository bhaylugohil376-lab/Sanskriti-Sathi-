package com.sanskritisathi.app;

import android.app.AlertDialog;
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

        if (position < 0
                || position >= reelList.size()) {

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
    // CREATE HOLDER
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

                ((ReelViewHolder) viewHolder)
                        .pauseVideo();
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

        int firstCompletelyVisible =
                ((androidx.recyclerview.widget.LinearLayoutManager)
                        recyclerView.getLayoutManager())
                        .findFirstCompletelyVisibleItemPosition();

        int position =
                firstCompletelyVisible != RecyclerView.NO_POSITION
                        ? firstCompletelyVisible
                        : ((androidx.recyclerview.widget.LinearLayoutManager)
                        recyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition();

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        View child =
                recyclerView.getLayoutManager()
                        .findViewByPosition(position);

        if (child == null) {
            return;
        }

        RecyclerView.ViewHolder viewHolder =
                recyclerView.getChildViewHolder(child);

        if (viewHolder instanceof ReelViewHolder) {

            ((ReelViewHolder) viewHolder)
                    .playVideo();
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

        private final PlayerView playerView;
        private final TextView errorText;

        private final ImageView profileImage;
        private final TextView usernameText;
        private final TextView captionText;

        private final ImageButton likeButton;
        private final ImageButton commentButton;
        private final ImageButton shareButton;
        private final ImageButton deleteButton;
        private final ImageButton editButton;

        private final TextView likesText;
        private final TextView commentsText;
        private final TextView viewsText;

        private ExoPlayer player;

        public ReelViewHolder(
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

            editButton =
                    itemView.findViewById(
                            R.id.reelEditButton
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
        }

        // =====================================================
        // BIND REEL
        // =====================================================

        public void bind(Reel reel) {

            releasePlayer();

            errorText.setVisibility(
                    View.GONE
            );

            String username =
                    reel.getUsername();

            if (TextUtils.isEmpty(username)) {

                username =
                        "Sanskriti User";
            }

            usernameText.setText(
                    username
            );

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

            // -------------------------------------------------
            // PROFILE
            // -------------------------------------------------

            if (profileImage != null) {

                profileImage.setImageResource(
                        R.drawable.icon_foreground
                );
            }

            // -------------------------------------------------
            // LIKE ICON
            // -------------------------------------------------

            updateLikeIcon(
                    reel.isLiked()
            );

            // -------------------------------------------------
            // DELETE
            // -------------------------------------------------

            if (deleteButton != null) {

                deleteButton.setVisibility(
                        reel.isOwnReel()
                                ? View.VISIBLE
                                : View.GONE
                );
            }

            // -------------------------------------------------
            // EDIT
            // -------------------------------------------------

            if (editButton != null) {

                editButton.setVisibility(
                        reel.isOwnReel()
                                ? View.VISIBLE
                                : View.GONE
                );

                editButton.setOnClickListener(
                        v -> showEditDialog(reel)
                );
            }

            // -------------------------------------------------
            // VIDEO
            // -------------------------------------------------

            setupPlayer(reel);

            // -------------------------------------------------
            // LIKE
            // -------------------------------------------------

            likeButton.setOnClickListener(
                    v -> handleLike(reel)
            );

            // -------------------------------------------------
            // COMMENT
            // -------------------------------------------------

            commentButton.setOnClickListener(
                    v -> openComments(reel)
            );

            // -------------------------------------------------
            // SHARE
            // -------------------------------------------------

            shareButton.setOnClickListener(
                    v -> shareReel(reel)
            );

            // -------------------------------------------------
            // DELETE
            // -------------------------------------------------

            if (deleteButton != null) {

                deleteButton.setOnClickListener(
                        v -> confirmDelete(reel)
                );
            }
        }

        // =====================================================
        // PLAYER
        // =====================================================

        private void setupPlayer(Reel reel) {

            String videoUrl =
                    reel.getVideoUrl();

            if (TextUtils.isEmpty(videoUrl)) {

                errorText.setText(
                        "Video unavailable"
                );

                errorText.setVisibility(
                        View.VISIBLE
                );

                return;
            }

            player =
                    new ExoPlayer.Builder(context)
                            .build();

            playerView.setPlayer(
                    player
            );

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

                                errorText.setVisibility(
                                        View.GONE
                                );
                            }
                        }

                        @Override
                        public void onPlayerError(
                                androidx.media3.common.PlaybackException error) {

                            errorText.setText(
                                    "Video unavailable"
                            );

                            errorText.setVisibility(
                                    View.VISIBLE
                            );
                        }
                    }
            );

            try {

                MediaItem mediaItem =
                        MediaItem.fromUri(
                                Uri.parse(videoUrl)
                        );

                player.setMediaItem(
                        mediaItem
                );

                player.prepare();

                // ReelActivity decides which
                // visible reel should play.
                player.setPlayWhenReady(
                        false
                );

            } catch (Exception e) {

                errorText.setText(
                        "Video unavailable"
                );

                errorText.setVisibility(
                        View.VISIBLE
                );
            }
        }

        // =====================================================
        // PLAY
        // =====================================================

        public void playVideo() {

            if (player == null) {
                return;
            }

            player.setPlayWhenReady(
                    true
            );

            player.play();
        }

        // =====================================================
        // PAUSE
        // =====================================================

        public void pauseVideo() {

            if (player == null) {
                return;
            }

            player.pause();
        }

        // =====================================================
        // RELEASE
        // =====================================================

        public void releasePlayer() {

            if (player != null) {

                player.pause();

                playerView.setPlayer(
                        null
                );

                player.release();

                player = null;
            }
        }

        // =====================================================
        // LIKE
        // =====================================================

        private void handleLike(Reel reel) {

            if (!SupabaseAuthManager.isLoggedIn(
                    context)) {

                Toast.makeText(
                        context,
                        "Please login first.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (TextUtils.isEmpty(
                    reel.getId())) {

                return;
            }

            likeButton.setEnabled(
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

                            int likes =
                                    reel.getLikes();

                            if (oldLiked) {

                                likes =
                                        Math.max(
                                                0,
                                                likes - 1
                                        );

                            } else {

                                likes =
                                        likes + 1;
                            }

                            reel.setLikes(
                                    likes
                            );

                            updateLikeIcon(
                                    reel.isLiked()
                            );

                            likesText.setText(
                                    String.valueOf(
                                            likes
                                    )
                            );

                            likeButton.setEnabled(
                                    true
                            );
                        }

                        @Override
                        public void onError(
                                String message) {

                            likeButton.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    context,
                                    TextUtils.isEmpty(
                                            message
                                    )
                                            ? "Like update failed."
                                            : message,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        }

        // =====================================================
        // LIKE ICON
        // =====================================================

        private void updateLikeIcon(
                boolean liked) {

            likeButton.setImageResource(
                    liked
                            ? android.R.drawable
                            .btn_star_big_on
                            : android.R.drawable
                            .btn_star_big_off
            );

            likeButton.setContentDescription(
                    liked
                            ? "Unlike"
                            : "Like"
            );
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

            context.startActivity(
                    intent
            );
        }

        // =====================================================
        // SHARE
        // =====================================================

        private void shareReel(Reel reel) {

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

            StringBuilder text =
                    new StringBuilder();

            if (!TextUtils.isEmpty(caption)) {

                text.append(
                        caption
                );

                text.append(
                        "\n\n"
                );
            }

            text.append(
                    videoUrl
            );

            Intent shareIntent =
                    new Intent(
                            Intent.ACTION_SEND
                    );

            shareIntent.setType(
                    "text/plain"
            );

            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    text.toString()
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

            new AlertDialog.Builder(context)
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

            deleteButton.setEnabled(
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

                            deleteButton.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    context,
                                    TextUtils.isEmpty(
                                            message
                                    )
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

        private void showEditDialog(
                Reel reel) {

            final android.widget.EditText input =
                    new android.widget.EditText(
                            context
                    );

            input.setSingleLine(false);
            input.setMaxLines(4);
            input.setText(
                    reel.getCaption()
            );
            input.setHint(
                    "Write a caption..."
            );

            int padding =
                    (int) (
                            20 *
                                    context.getResources()
                                            .getDisplayMetrics()
                                            .density
                    );

            input.setPadding(
                    padding,
                    padding,
                    padding,
                    padding
            );

            AlertDialog dialog =
                    new AlertDialog.Builder(
                            context
                    )
                            .setTitle(
                                    "Edit Reel"
                            )
                            .setView(
                                    input
                            )
                            .setNegativeButton(
                                    "Cancel",
                                    null
                            )
                            .setPositiveButton(
                                    "Save",
                                    null
                            )
                            .create();

            dialog.setOnShowListener(
                    d -> {

                        android.widget.Button save =
                                dialog.getButton(
                                        AlertDialog.BUTTON_POSITIVE
                                );

                        save.setOnClickListener(
                                v -> {

                                    String newCaption =
                                            input.getText()
                                                    .toString()
                                                    .trim();

                                    if (newCaption.length()
                                            > 1000) {

                                        input.setError(
                                                "Maximum 1000 characters"
                                        );

                                        return;
                                    }

                                    save.setEnabled(
                                            false
                                    );

                                    ReelSupabaseHelper
                                            .updateReel(
                                                    context,
                                                    reel.getId(),
                                                    newCaption,
                                                    reel.getVisibility(),
                                                    new ReelSupabaseHelper
                                                            .ActionCallback() {

                                                        @Override
                                                        public void onSuccess() {

                                                            // Reel model currently
                                                            // has no caption setter.
                                                            // Refreshing the item
                                                            // is safer than keeping
                                                            // stale UI.

                                                            dialog.dismiss();

                                                            Toast.makeText(
                                                                    context,
                                                                    "Reel updated",
                                                                    Toast.LENGTH_SHORT
                                                            ).show();

                                                            notifyItemChanged(
                                                                    getBindingAdapterPosition()
                                                            );
                                                        }

                                                        @Override
                                                        public void onError(
                                                                String message) {

                                                            save.setEnabled(
                                                                    true
                                                            );

                                                            Toast.makeText(
                                                                    context,
                                                                    TextUtils.isEmpty(
                                                                            message
                                                                    )
                                                                            ? "Reel update failed."
                                                                            : message,
                                                                    Toast.LENGTH_LONG
                                                            ).show();
                                                        }
                                                    }
                                            );
                                }
                        );
                    }
            );

            dialog.show();
        }
    }
}
