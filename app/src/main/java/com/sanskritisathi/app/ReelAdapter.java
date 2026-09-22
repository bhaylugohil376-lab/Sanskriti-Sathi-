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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReelAdapter extends RecyclerView.Adapter<ReelAdapter.ReelViewHolder> {

    private final Context context;
    private final List<Reel> reelList;

    public ReelAdapter(Context context, List<Reel> reelList) {
        this.context = context;
        this.reelList = reelList;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        String id = reelList.get(position).getId();
        return id == null ? position : id.hashCode();
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reel, parent, false);

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
    public void onViewRecycled(@NonNull ReelViewHolder holder) {
        holder.releasePlayer();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return reelList == null ? 0 : reelList.size();
    }

    public void pauseAllVideos() {
        for (ReelViewHolder holder : getAttachedHolders()) {
            holder.pausePlayer();
        }
    }

    public void resumeCurrentVideo() {

        RecyclerView recyclerView = findRecyclerView();

        if (recyclerView == null ||
                recyclerView.getLayoutManager() == null) {
            return;
        }

        RecyclerView.LayoutManager layoutManager =
                recyclerView.getLayoutManager();

        if (!(layoutManager instanceof androidx.recyclerview.widget.LinearLayoutManager)) {
            return;
        }

        androidx.recyclerview.widget.LinearLayoutManager linearLayoutManager =
                (androidx.recyclerview.widget.LinearLayoutManager) layoutManager;

        int position =
                linearLayoutManager.findFirstCompletelyVisibleItemPosition();

        if (position == RecyclerView.NO_POSITION) {
            position =
                    linearLayoutManager.findFirstVisibleItemPosition();
        }

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        RecyclerView.ViewHolder vh =
                recyclerView.findViewHolderForAdapterPosition(position);

        if (vh instanceof ReelViewHolder) {
            ((ReelViewHolder) vh).resumePlayer();
        }
    }

    public void releaseAllVideos() {
        for (ReelViewHolder holder : getAttachedHolders()) {
            holder.releasePlayer();
        }
    }

    private List<ReelViewHolder> getAttachedHolders() {

        ArrayList<ReelViewHolder> result =
                new ArrayList<>();

        RecyclerView recyclerView = findRecyclerView();

        if (recyclerView == null) {
            return result;
        }

        for (int i = 0; i < recyclerView.getChildCount(); i++) {

            View child = recyclerView.getChildAt(i);

            RecyclerView.ViewHolder holder =
                    recyclerView.getChildViewHolder(child);

            if (holder instanceof ReelViewHolder) {
                result.add((ReelViewHolder) holder);
            }
        }

        return result;
    }

    private RecyclerView findRecyclerView() {

        if (context instanceof ReelActivity) {
            return ((ReelActivity) context).getReelRecyclerView();
        }

        return null;
    }

    public class ReelViewHolder extends RecyclerView.ViewHolder {

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
        private boolean released = false;

        ReelViewHolder(@NonNull View itemView) {
            super(itemView);

            playerView =
                    itemView.findViewById(R.id.reelPlayerView);

            errorText =
                    itemView.findViewById(R.id.reelErrorText);

            profileImage =
                    itemView.findViewById(R.id.reelProfileImage);

            usernameText =
                    itemView.findViewById(R.id.reelUsernameText);

            captionText =
                    itemView.findViewById(R.id.reelCaptionText);

            likeButton =
                    itemView.findViewById(R.id.reelLikeButton);

            commentButton =
                    itemView.findViewById(R.id.reelCommentButton);

            shareButton =
                    itemView.findViewById(R.id.reelShareButton);

            deleteButton =
                    itemView.findViewById(R.id.reelDeleteButton);

            likesText =
                    itemView.findViewById(R.id.reelLikesText);

            commentsText =
                    itemView.findViewById(R.id.reelCommentsText);

            viewsText =
                    itemView.findViewById(R.id.reelViewsText);
        }

        void bind(Reel reel) {

            releasePlayer();

            released = false;

            errorText.setVisibility(View.GONE);

            String username = reel.getUsername();

            if (username == null ||
                    username.trim().isEmpty()) {
                username = "Sanskriti User";
            }

            usernameText.setText(username);

            String caption = reel.getCaption();

            captionText.setText(
                    caption == null ? "" : caption
            );

            likesText.setText(
                    String.valueOf(
                            Math.max(0, reel.getLikes())
                    )
            );

            commentsText.setText(
                    String.valueOf(
                            Math.max(0, reel.getComments())
                    )
            );

            viewsText.setText(
                    String.valueOf(
                            Math.max(0, reel.getViews())
                    )
            );

            deleteButton.setVisibility(
                    reel.isOwnReel()
                            ? View.VISIBLE
                            : View.GONE
            );

            updateLikeIcon(reel.isLiked());

            setupPlayer(reel);

            likeButton.setOnClickListener(
                    v -> toggleLike(reel)
            );

            commentButton.setOnClickListener(
                    v -> openComments(reel)
            );

            shareButton.setOnClickListener(
                    v -> shareReel(reel)
            );

            deleteButton.setOnClickListener(
                    v -> confirmDelete(reel)
            );
        }

        private void setupPlayer(Reel reel) {

            String videoUrl = reel.getVideoUrl();

            if (videoUrl == null ||
                    videoUrl.trim().isEmpty()) {

                errorText.setText("Video unavailable");
                errorText.setVisibility(View.VISIBLE);
                return;
            }

            player =
                    new ExoPlayer.Builder(context).build();

            playerView.setPlayer(player);

            MediaItem mediaItem =
                    MediaItem.fromUri(
                            Uri.parse(videoUrl)
                    );

            player.setMediaItem(mediaItem);

            player.setRepeatMode(
                    Player.REPEAT_MODE_ONE
            );

            player.addListener(
                    new Player.Listener() {

                        @Override
                        public void onPlayerError(
                                @NonNull androidx.media3.common.PlaybackException error) {

                            errorText.setVisibility(
                                    View.VISIBLE
                            );

                            errorText.setText(
                                    "Video unavailable"
                            );
                        }

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
                    }
            );

            player.prepare();

            // Activity snapped Reel ko play karegi.
            player.setPlayWhenReady(false);
        }

        private void toggleLike(Reel reel) {

            if (!SupabaseAuthManager.isLoggedIn(context)) {

                Toast.makeText(
                        context,
                        "Please login first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (reel.getId() == null ||
                    reel.getId().trim().isEmpty()) {

                Toast.makeText(
                        context,
                        "Reel ID missing",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            likeButton.setEnabled(false);

            /*
             * IMPORTANT:
             * Current ReelSupabaseHelper signature:
             *
             * toggleReelLike(
             *     Context,
             *     String reelId,
             *     boolean currentlyLiked,
             *     ActionCallback
             * )
             */
            ReelSupabaseHelper.toggleReelLike(
                    context,
                    reel.getId(),
                    reel.isLiked(),
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            boolean newState =
                                    !reel.isLiked();

                            reel.setLiked(
                                    newState
                            );

                            int newCount =
                                    Math.max(
                                            0,
                                            reel.getLikes()
                                                    + (newState ? 1 : -1)
                                    );

                            reel.setLikes(
                                    newCount
                            );

                            likesText.setText(
                                    String.valueOf(
                                            newCount
                                    )
                            );

                            updateLikeIcon(
                                    newState
                            );

                            likeButton.setEnabled(
                                    true
                            );
                        }

                        @Override
                        public void onError(
                                String error) {

                            likeButton.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    context,
                                    error == null
                                            ? "Like update failed"
                                            : error,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        }

        private void updateLikeIcon(boolean liked) {

            likeButton.setImageResource(
                    liked
                            ? android.R.drawable.btn_star_big_on
                            : android.R.drawable.btn_star_big_off
            );

            likeButton.setContentDescription(
                    liked
                            ? "Unlike"
                            : "Like"
            );
        }

        private void openComments(Reel reel) {

            if (reel.getId() == null ||
                    reel.getId().trim().isEmpty()) {

                Toast.makeText(
                        context,
                        "Reel ID missing",
                        Toast.LENGTH_SHORT
                ).show();

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

            context.startActivity(intent);
        }

        private void shareReel(Reel reel) {

            String url = reel.getVideoUrl();

            if (url == null ||
                    url.trim().isEmpty()) {

                Toast.makeText(
                        context,
                        "Video link available nahi hai",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Intent shareIntent =
                    new Intent(
                            Intent.ACTION_SEND
                    );

            shareIntent.setType(
                    "text/plain"
            );

            String caption =
                    reel.getCaption() == null
                            ? ""
                            : reel.getCaption();

            String shareText;

            if (caption.trim().isEmpty()) {
                shareText = url;
            } else {
                shareText =
                        caption + "\n\n" + url;
            }

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

        private void confirmDelete(Reel reel) {

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
                            (dialog, which) ->
                                    deleteReel(reel)
                    )
                    .show();
        }

        private void deleteReel(Reel reel) {

            if (reel.getId() == null ||
                    reel.getId().trim().isEmpty()) {

                Toast.makeText(
                        context,
                        "Reel ID missing",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            deleteButton.setEnabled(false);

            ReelSupabaseHelper.deleteReel(
                    context,
                    reel.getId(),
                    new ReelSupabaseHelper.ActionCallback() {

                        @Override
                        public void onSuccess() {

                            int position =
                                    getBindingAdapterPosition();

                            if (position !=
                                    RecyclerView.NO_POSITION &&
                                    position <
                                            reelList.size()) {

                                releasePlayer();

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
                                String error) {

                            deleteButton.setEnabled(
                                    true
                            );

                            Toast.makeText(
                                    context,
                                    error == null
                                            ? "Delete failed"
                                            : error,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        }

        public void pausePlayer() {

            if (player != null) {
                player.pause();
            }
        }

        public void resumePlayer() {

            if (player != null &&
                    !released) {

                player.play();
            }
        }

        public void releasePlayer() {

            if (player != null) {

                playerView.setPlayer(
                        null
                );

                player.release();

                player = null;
            }

            released = true;
        }
    }
}
