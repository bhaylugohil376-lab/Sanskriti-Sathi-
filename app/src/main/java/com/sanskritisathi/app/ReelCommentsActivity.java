package com.sanskritisathi.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReelCommentsActivity extends AppCompatActivity {

    private RecyclerView commentsRecyclerView;

    private EditText commentInput;

    private ImageButton backButton;
    private ImageButton sendButton;

    private ProgressBar loadingProgress;

    private TextView emptyText;
    private TextView commentCountText;

    private LinearLayout inputContainer;

    private final List<ReelComment> commentList =
            new ArrayList<>();

    private CommentsAdapter commentsAdapter;

    private String reelId;

    private boolean loading = false;
    private boolean sending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_reel_comments
        );

        reelId =
                getIntent().getStringExtra(
                        "reel_id"
                );

        bindViews();

        setupRecyclerView();

        setupListeners();

        setupBackHandler();

        if (TextUtils.isEmpty(reelId)) {

            Toast.makeText(
                    this,
                    "Invalid Reel.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        loadComments();
    }

    // =========================================================
    // BIND VIEWS
    // =========================================================

    private void bindViews() {

        commentsRecyclerView =
                findViewById(
                        R.id.commentsRecyclerView
                );

        commentInput =
                findViewById(
                        R.id.commentInput
                );

        backButton =
                findViewById(
                        R.id.backButton
                );

        sendButton =
                findViewById(
                        R.id.sendButton
                );

        loadingProgress =
                findViewById(
                        R.id.loadingProgress
                );

        emptyText =
                findViewById(
                        R.id.emptyText
                );

        commentCountText =
                findViewById(
                        R.id.commentCountText
                );

        inputContainer =
                findViewById(
                        R.id.inputContainer
                );
    }

    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        commentsAdapter =
                new CommentsAdapter(
                        this,
                        commentList
                );

        commentsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        commentsRecyclerView.setHasFixedSize(false);

        commentsRecyclerView.setAdapter(
                commentsAdapter
        );
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        backButton.setOnClickListener(
                v -> finish()
        );

        sendButton.setOnClickListener(
                v -> sendComment()
        );

        commentInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId != 0) {
                        sendComment();
                        return true;
                    }

                    return false;
                }
        );
    }

    // =========================================================
    // BACK
    // =========================================================

    private void setupBackHandler() {

        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                finish();
                            }
                        }
                );
    }

    // =========================================================
    // LOAD COMMENTS
    // =========================================================

    private void loadComments() {

        if (loading) {
            return;
        }

        loading = true;

        showLoading(true);

        ReelCommentSupabaseHelper.getComments(
                this,
                reelId,
                new ReelCommentSupabaseHelper.CommentsCallback() {

                    @Override
                    public void onSuccess(
                            List<ReelComment> comments) {

                        runOnUiThread(() -> {

                            loading = false;

                            showLoading(false);

                            commentList.clear();

                            if (comments != null) {
                                commentList.addAll(
                                        comments
                                );
                            }

                            commentsAdapter.notifyDataSetChanged();

                            updateEmptyState();

                            updateCommentCount();
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() -> {

                            loading = false;

                            showLoading(false);

                            Toast.makeText(
                                    ReelCommentsActivity.this,
                                    message == null
                                            ? "Comments load failed."
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();

                            updateEmptyState();
                        });
                    }
                }
        );
    }

    // =========================================================
    // SEND COMMENT
    // =========================================================

    private void sendComment() {

        if (sending) {
            return;
        }

        if (!SupabaseAuthManager.isLoggedIn(this)) {

            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String text =
                commentInput
                        .getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(text)) {

            Toast.makeText(
                    this,
                    "Comment likho.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (text.length() > 500) {

            Toast.makeText(
                    this,
                    "Comment maximum 500 characters ka ho sakta hai.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        setSendingState(true);

        ReelCommentSupabaseHelper.addComment(
                this,
                reelId,
                text,
                new ReelCommentSupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        runOnUiThread(() -> {

                            setSendingState(false);

                            commentInput.setText("");

                            hideKeyboard();

                            loadComments();
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() -> {

                            setSendingState(false);

                            Toast.makeText(
                                    ReelCommentsActivity.this,
                                    message == null
                                            ? "Comment add failed."
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    // =========================================================
    // DELETE COMMENT
    // =========================================================

    private void deleteComment(
            ReelComment comment,
            int position) {

        if (comment == null) {
            return;
        }

        if (!SupabaseAuthManager.isLoggedIn(this)) {
            return;
        }

        ReelCommentSupabaseHelper.deleteComment(
                this,
                comment.getId(),
                new ReelCommentSupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        runOnUiThread(() -> {

                            if (position >= 0
                                    && position < commentList.size()) {

                                commentList.remove(
                                        position
                                );

                                commentsAdapter.notifyItemRemoved(
                                        position
                                );

                                updateEmptyState();

                                updateCommentCount();
                            }
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() ->
                                Toast.makeText(
                                        ReelCommentsActivity.this,
                                        message == null
                                                ? "Comment delete failed."
                                                : message,
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }
                }
        );
    }

    // =========================================================
    // LOADING
    // =========================================================

    private void showLoading(
            boolean show) {

        if (loadingProgress != null) {

            loadingProgress.setVisibility(
                    show
                            ? View.VISIBLE
                            : View.GONE
            );
        }
    }

    // =========================================================
    // EMPTY STATE
    // =========================================================

    private void updateEmptyState() {

        if (emptyText == null) {
            return;
        }

        boolean empty =
                commentList.isEmpty();

        emptyText.setVisibility(
                empty
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    // =========================================================
    // COUNT
    // =========================================================

    private void updateCommentCount() {

        if (commentCountText == null) {
            return;
        }

        commentCountText.setText(
                commentList.size()
                        + " comments"
        );
    }

    // =========================================================
    // SENDING STATE
    // =========================================================

    private void setSendingState(
            boolean value) {

        sending = value;

        commentInput.setEnabled(
                !value
        );

        sendButton.setEnabled(
                !value
        );

        if (value) {

            sendButton.setAlpha(
                    0.45f
            );

        } else {

            sendButton.setAlpha(
                    1.0f
            );
        }
    }

    // =========================================================
    // KEYBOARD
    // =========================================================

    private void hideKeyboard() {

        InputMethodManager manager =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (manager != null) {

            manager.hideSoftInputFromWindow(
                    commentInput.getWindowToken(),
                    0
            );
        }

        commentInput.clearFocus();
    }

    // =========================================================
    // COMMENTS ADAPTER
    // =========================================================

    private class CommentsAdapter
            extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

        private final Context context;

        private final List<ReelComment> list;

        CommentsAdapter(
                Context context,
                List<ReelComment> list) {

            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType) {

            LinearLayout root =
                    new LinearLayout(
                            context
                    );

            root.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            root.setGravity(
                    Gravity.TOP
            );

            root.setPadding(
                    dp(16),
                    dp(12),
                    dp(16),
                    dp(8)
            );

            TextView avatar =
                    new TextView(context);

            avatar.setGravity(
                    Gravity.CENTER
            );

            avatar.setText(
                    "S"
            );

            avatar.setTextColor(
                    Color.WHITE
            );

            avatar.setTextSize(
                    15
            );

            avatar.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            avatar.setBackgroundColor(
                    Color.rgb(
                            210,
                            165,
                            70
                    )
            );

            LinearLayout.LayoutParams avatarParams =
                    new LinearLayout.LayoutParams(
                            dp(40),
                            dp(40)
                    );

            root.addView(
                    avatar,
                    avatarParams
            );

            LinearLayout content =
                    new LinearLayout(
                            context
                    );

            content.setOrientation(
                    LinearLayout.VERTICAL
            );

            content.setPadding(
                    dp(12),
                    0,
                    0,
                    0
            );

            LinearLayout.LayoutParams contentParams =
                    new LinearLayout.LayoutParams(
                            0,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1
                    );

            TextView username =
                    new TextView(context);

            username.setTextColor(
                    Color.WHITE
            );

            username.setTextSize(
                    14
            );

            username.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );

            content.addView(
                    username
            );

            TextView comment =
                    new TextView(context);

            comment.setTextColor(
                    Color.rgb(
                            225,
                            225,
                            225
                    )
            );

            comment.setTextSize(
                    14
            );

            comment.setPadding(
                    0,
                    dp(3),
                    0,
                    0
            );

            content.addView(
                    comment
            );

            TextView time =
                    new TextView(context);

            time.setTextColor(
                    Color.rgb(
                            145,
                            145,
                            145
                    )
            );

            time.setTextSize(
                    11
            );

            time.setPadding(
                    0,
                    dp(5),
                    0,
                    0
            );

            content.addView(
                    time
            );

            root.addView(
                    content,
                    contentParams
            );

            ImageButton more =
                    new ImageButton(
                            context
                    );

            more.setImageResource(
                    android.R.drawable.ic_menu_more
            );

            more.setColorFilter(
                    Color.WHITE
            );

            more.setBackgroundColor(
                    Color.TRANSPARENT
            );

            more.setContentDescription(
                    "Comment options"
            );

            LinearLayout.LayoutParams moreParams =
                    new LinearLayout.LayoutParams(
                            dp(40),
                            dp(40)
                    );

            root.addView(
                    more,
                    moreParams
            );

            return new CommentViewHolder(
                    root,
                    avatar,
                    username,
                    comment,
                    time,
                    more
            );
        }

        @Override
        public void onBindViewHolder(
                @NonNull CommentViewHolder holder,
                int position) {

            ReelComment item =
                    list.get(position);

            String username =
                    item.getUsername();

            if (TextUtils.isEmpty(username)) {
                username = "Sanskriti User";
            }

            holder.username.setText(
                    username
            );

            holder.comment.setText(
                    item.getText()
            );

            holder.avatar.setText(
                    getInitial(username)
            );

            holder.time.setText(
                    formatTime(
                            item.getCreatedAt()
                    )
            );

            String currentUserId =
                    SupabaseAuthManager.getUserId(
                            ReelCommentsActivity.this
                    );

            boolean ownComment =
                    !TextUtils.isEmpty(
                            currentUserId
                    )
                    && currentUserId.equals(
                            item.getUserId()
                    );

            holder.moreButton.setVisibility(
                    ownComment
                            ? View.VISIBLE
                            : View.INVISIBLE
            );

            holder.moreButton.setOnClickListener(
                    v -> {

                        int adapterPosition =
                                holder.getBindingAdapterPosition();

                        if (adapterPosition == RecyclerView.NO_POSITION) {
                            return;
                        }

                        showDeleteDialog(
                                list.get(adapterPosition),
                                adapterPosition
                        );
                    }
            );
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        private int dp(int value) {

            return (int)
                    (
                            value
                                    * getResources()
                                    .getDisplayMetrics()
                                    .density
                    );
        }

        class CommentViewHolder
                extends RecyclerView.ViewHolder {

            TextView avatar;
            TextView username;
            TextView comment;
            TextView time;
            ImageButton moreButton;

            CommentViewHolder(
                    @NonNull View itemView,
                    TextView avatar,
                    TextView username,
                    TextView comment,
                    TextView time,
                    ImageButton moreButton) {

                super(itemView);

                this.avatar = avatar;
                this.username = username;
                this.comment = comment;
                this.time = time;
                this.moreButton = moreButton;
            }
        }
    }

    // =========================================================
    // DELETE DIALOG
    // =========================================================

    private void showDeleteDialog(
            ReelComment comment,
            int position) {

        new androidx.appcompat.app.AlertDialog.Builder(
                this
        )
                .setTitle(
                        "Delete comment?"
                )
                .setMessage(
                        "Kya aap ye comment delete karna chahte ho?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteComment(
                                        comment,
                                        position
                                )
                )
                .show();
    }

    // =========================================================
    // INITIAL
    // =========================================================

    private String getInitial(
            String username) {

        if (TextUtils.isEmpty(username)) {
            return "S";
        }

        return username
                .substring(
                        0,
                        1
                )
                .toUpperCase();
    }

    // =========================================================
    // TIME
    // =========================================================

    private String formatTime(
            long timestamp) {

        if (timestamp <= 0) {
            return "";
        }

        long now =
                System.currentTimeMillis();

        long difference =
                Math.max(
                        0,
                        now - timestamp
                );

        long minutes =
                difference / 60000L;

        if (minutes < 1) {
            return "now";
        }

        if (minutes < 60) {
            return minutes + "m";
        }

        long hours =
                minutes / 60L;

        if (hours < 24) {
            return hours + "h";
        }

        long days =
                hours / 24L;

        if (days < 7) {
            return days + "d";
        }

        long weeks =
                days / 7L;

        if (weeks < 5) {
            return weeks + "w";
        }

        return days / 30L + "mo";
    }

    // =========================================================
    // DP
    // =========================================================

    private int dp(int value) {

        return (int)
                (
                        value
                                * getResources()
                                .getDisplayMetrics()
                                .density
                );
    }
}
