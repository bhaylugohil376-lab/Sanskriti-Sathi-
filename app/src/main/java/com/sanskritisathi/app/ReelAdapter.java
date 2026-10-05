package com.sanskritisathi.app;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public ReelAdapter(
            Context context,
            ReelInteractionListener listener
    ) {
        this.context = context;
        this.interactionListener = listener;
        setHasStableIds(true);
    }

    public void setReels(List<Reel> reels) {

        releaseVisiblePlayers();

        reelList.clear();

        if (reels != null) {
            reelList.addAll(reels);
        }

        notifyDataSetChanged();
    }

    public void removeReel(int position) {

        if (position < 0 || position >= reelList.size()) {
            return;
        }

        reelList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public long getItemId(int position) {

        if (position < 0 || position >= reelList.size()) {
            return RecyclerView.NO_ID;
        }

        String id = reelList.get(position).getId();

        return id == null
                ? position
                : id.hashCode();
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reel, parent, false);

        return new ReelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ReelViewHolder holder,
            int position
    ) {

        holder.bind(
                reelList.get(position),
                interactionListener
        );
    }

    @Override
    public void onViewAttachedToWindow(
            @NonNull ReelViewHolder holder
    ) {

        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(
            @NonNull ReelViewHolder holder
    ) {

        holder.pausePlayer();

        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(
            @NonNull ReelViewHolder holder
    ) {

        holder.releasePlayer();

        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return reelList.size();
    }

    private void releaseVisiblePlayers() {

        if (!(context instanceof ReelActivity)) {
            return;
        }

        RecyclerView rv =
                ((ReelActivity) context).getReelRecyclerView();

        if (rv == null) {
            return;
        }

        for (int i = 0; i < rv.getChildCount(); i++) {

            View child = rv.getChildAt(i);

            RecyclerView.ViewHolder holder =
                    rv.getChildViewHolder(child);

            if (holder instanceof ReelViewHolder) {

                ((ReelViewHolder) holder).releasePlayer();
            }
        }
    }

    public static class ReelViewHolder
            extends RecyclerView.ViewHolder {

        private final PlayerView playerView;
        private final TextView tvCaption;
        private final TextView tvLikesCount;
        private final TextView tvCommentsCount;
        private final TextView tvViewsCount;
        private final TextView tvUsername;
        private final TextView tvErrorText;
        private final ProgressBar playbackProgress;

        private final ImageButton btnLike;
        private final ImageButton btnComment;
        private final ImageButton btnShare;
        private final ImageButton btnDelete;

        private ExoPlayer player;
        private Reel currentReel;
        private String preparedUrl;

        public ReelViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            playerView =
                    itemView.findViewById(R.id.playerView);

            tvCaption =
                    itemView.findViewById(R.id.tvCaption);

            tvUsername =
                    itemView.findViewById(R.id.tvUsername);

            tvLikesCount =
                    itemView.findViewById(R.id.tvLikesCount);

            tvCommentsCount =
                    itemView.findViewById(R.id.tvCommentsCount);

            tvViewsCount =
                    itemView.findViewById(R.id.tvViewsCount);

            tvErrorText =
                    itemView.findViewById(R.id.tvErrorText);

            playbackProgress =
                    itemView.findViewById(R.id.playbackProgress);

            btnLike =
                    itemView.findViewById(R.id.btnLike);

            btnComment =
                    itemView.findViewById(R.id.btnComment);

            btnShare =
                    itemView.findViewById(R.id.btnShare);

            btnDelete =
                    itemView.findViewById(R.id.btnDelete);

            playerView.setShutterBackgroundColor(
                    Color.TRANSPARENT
            );

            playerView.setKeepContentOnPlayerReset(
                    true
            );

            playerView.setControllerAutoShow(
                    false
            );

            playerView.setUseController(
                    false
            );
        }

        public void bind(
                Reel reel,
                ReelInteractionListener listener
        ) {

            releasePlayer();

            currentReel = reel;
            preparedUrl = null;

            if (reel == null) {
                return;
            }

            tvUsername.setText(
                    TextUtils.isEmpty(reel.getUsername())
                            ? "Sanskriti User"
                            : reel.getUsername()
            );

            tvCaption.setText(
                    reel.getCaption() == null
                            ? ""
                            : reel.getCaption()
            );

            tvLikesCount.setText(
                    String.valueOf(
                            Math.max(0, reel.getLikes())
                    )
            );

            tvCommentsCount.setText(
                    String.valueOf(
                            Math.max(0, reel.getComments())
                    )
            );

            tvViewsCount.setText(
                    String.valueOf(
                            Math.max(0, reel.getViews())
                    )
            );

            btnDelete.setVisibility(
                    reel.isOwnReel()
                            ? View.VISIBLE
                            : View.GONE
            );

            tvErrorText.setVisibility(View.GONE);

            playbackProgress.setVisibility(View.GONE);

            updateLikeIcon();

            btnLike.setOnClickListener(v -> {

                int p = getBindingAdapterPosition();

                if (listener != null
                        && p != RecyclerView.NO_POSITION) {

                    listener.onLikeClicked(reel, p);
                }
            });

            btnComment.setOnClickListener(v -> {

                int p = getBindingAdapterPosition();

                if (listener != null
                        && p != RecyclerView.NO_POSITION) {

                    listener.onCommentClicked(reel, p);
                }
            });

            btnShare.setOnClickListener(v -> {

                int p = getBindingAdapterPosition();

                if (listener != null
                        && p != RecyclerView.NO_POSITION) {

                    listener.onShareClicked(reel, p);
                }
            });

            btnDelete.setOnClickListener(v -> {

                int p = getBindingAdapterPosition();

                if (listener != null
                        && p != RecyclerView.NO_POSITION) {

                    listener.onDeleteClicked(reel, p);
                }
            });

            playerView.setOnClickListener(v -> {

                if (player == null) {

                    playPlayer();

                } else if (player.isPlaying()) {

                    player.pause();

                } else {

                    player.play();
                }
            });
        }

        private void updateLikeIcon() {

            if (currentReel == null) {
                return;
            }

            btnLike.setImageResource(
                    currentReel.isLiked()
                            ? android.R.drawable.btn_star_big_on
                            : android.R.drawable.btn_star_big_off
            );

            btnLike.setContentDescription(
                    currentReel.isLiked()
                            ? "Unlike"
                            : "Like"
            );
        }

        @OptIn(markerClass = UnstableApi.class)
        private void createPlayerIfNeeded() {

            if (player != null) {
                return;
            }

            Context ctx = itemView.getContext();

            Map<String, String> headers =
                    new HashMap<>();

            headers.put(
                    "apikey",
                    SupabaseConfig.PUBLISHABLE_KEY
            );

            String token =
                    SupabaseAuthManager.getAccessToken(ctx);

            if (!TextUtils.isEmpty(token)) {

                headers.put(
                        "Authorization",
                        "Bearer " + token
                );
            }

            DefaultHttpDataSource.Factory httpFactory =
                    new DefaultHttpDataSource.Factory()
                            .setConnectTimeoutMs(30000)
                            .setReadTimeoutMs(30000)
                            .setAllowCrossProtocolRedirects(true)
                            .setDefaultRequestProperties(headers);

            DefaultDataSource.Factory dataSourceFactory =
                    new DefaultDataSource.Factory(
                            ctx,
                            httpFactory
                    );

            player =
                    new ExoPlayer.Builder(ctx)
                            .setMediaSourceFactory(
                                    new DefaultMediaSourceFactory(
                                            dataSourceFactory
                                    )
                            )
                            .build();

            player.setRepeatMode(
                    Player.REPEAT_MODE_ONE
            );

            playerView.setPlayer(player);

            player.addListener(
                    new Player.Listener() {

                        @Override
                        public void onPlaybackStateChanged(
                                int state
                        ) {

                            if (state ==
                                    Player.STATE_BUFFERING) {

                                playbackProgress.setVisibility(
                                        View.VISIBLE
                                );

                            } else if (state ==
                                    Player.STATE_READY) {

                                playbackProgress.setVisibility(
                                        View.GONE
                                );

                                tvErrorText.setVisibility(
                                        View.GONE
                                );

                            } else if (state ==
                                    Player.STATE_ENDED) {

                                playbackProgress.setVisibility(
                                        View.GONE
                                );
                            }
                        }

                        @Override
                        public void onRenderedFirstFrame() {

                            playbackProgress.setVisibility(
                                    View.GONE
                            );

                            tvErrorText.setVisibility(
                                    View.GONE
                            );
                        }

                        @Override
                        public void onPlayerError(
                                @NonNull PlaybackException error
                        ) {

                            playbackProgress.setVisibility(
                                    View.GONE
                            );

                            String url =
                                    currentReel == null
                                            ? ""
                                            : currentReel.getVideoUrl();

                            Log.e(
                                    TAG,
                                    "Playback error. URL="
                                            + url
                                            + " code="
                                            + error.errorCode,
                                    error
                            );

                            showError(
                                    "Video play nahi ho rahi "
                                            + "(code "
                                            + error.errorCode
                                            + ")"
                            );
                        }
                    }
            );
        }

        @OptIn(markerClass = UnstableApi.class)
        public void playPlayer() {

            if (currentReel == null) {
                return;
            }

            final String url =
                    currentReel.getVideoUrl() == null
                            ? ""
                            : currentReel.getVideoUrl().trim();

            if (TextUtils.isEmpty(url)) {

                showError("Video URL missing");

                return;
            }

            createPlayerIfNeeded();

            playbackProgress.setVisibility(
                    View.VISIBLE
            );

            tvErrorText.setVisibility(
                    View.GONE
            );

            if (url.startsWith("b2://")) {

                final String fileName =
                        url.substring("b2://".length());

                B2MediaHelper.resolveUrl(
                        itemView.getContext(),
                        fileName,
                        new B2MediaHelper.UrlCallback() {

                            @Override
                            public void onSuccess(
                                    String resolvedUrl
                            ) {

                                if (currentReel == null
                                        || !url.equals(
                                        currentReel.getVideoUrl()
                                )) {

                                    return;
                                }

                                prepareResolvedUrl(
                                        resolvedUrl == null
                                                ? ""
                                                : resolvedUrl.trim()
                                );
                            }

                            @Override
                            public void onError(
                                    String message
                            ) {

                                playbackProgress.setVisibility(
                                        View.GONE
                                );

                                showError(message);
                            }
                        }
                );

                return;
            }

            String playbackUrl = url;

            String token =
                    SupabaseAuthManager.getAccessToken(
                            itemView.getContext()
                    );

            if (!TextUtils.isEmpty(token)
                    && playbackUrl.contains(
                    "/storage/v1/object/public/"
            )) {

                playbackUrl =
                        playbackUrl.replace(
                                "/storage/v1/object/public/",
                                "/storage/v1/object/"
                        );
            }

            prepareResolvedUrl(playbackUrl);
        }

        @OptIn(markerClass = UnstableApi.class)
        private void prepareResolvedUrl(
                String playbackUrl
        ) {

            if (player == null
                    || TextUtils.isEmpty(playbackUrl)) {

                playbackProgress.setVisibility(
                        View.GONE
                );

                showError(
                        "Video URL resolve nahi hui"
                );

                return;
            }

            if (!playbackUrl.equals(preparedUrl)) {

                preparedUrl = playbackUrl;

                playbackProgress.setVisibility(
                        View.VISIBLE
                );

                tvErrorText.setVisibility(
                        View.GONE
                );

                player.setMediaItem(
                        MediaItem.fromUri(
                                Uri.parse(playbackUrl)
                        )
                );

                player.prepare();
            }

            player.setPlayWhenReady(true);
            player.play();
        }

        public void pausePlayer() {

            if (player != null) {

                player.setPlayWhenReady(false);
                player.pause();
            }
        }

        public void releasePlayer() {

            playbackProgress.setVisibility(
                    View.GONE
            );

            if (player != null) {

                playerView.setPlayer(null);

                player.release();

                player = null;
            }

            preparedUrl = null;
        }

        private void showError(
                String message
        ) {

            if (tvErrorText != null) {

                tvErrorText.setText(message);

                tvErrorText.setVisibility(
                        View.VISIBLE
                );
            }
        }
    }
}
