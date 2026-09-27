package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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
    private ProgressBar commentsProgress;
    private TextView commentsTitle;
    private TextView emptyCommentsText;

    private ReelCommentsAdapter commentsAdapter;
    private final List<ReelComment> commentList = new ArrayList<>();

    private String reelId;
    private boolean sendingComment = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reel_comments);

        reelId = getIntent().getStringExtra("reel_id");

        if (TextUtils.isEmpty(reelId)) {
            Toast.makeText(
                    this,
                    "Invalid Reel.",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        bindViews();
        setupRecyclerView();
        setupListeners();

        loadComments();
    }

    // =========================================================
    // BIND VIEWS
    // =========================================================

    private void bindViews() {

        commentsRecyclerView =
                findViewById(R.id.commentsRecyclerView);

        commentInput =
                findViewById(R.id.commentInput);

        backButton =
                findViewById(R.id.backButton);

        sendButton =
                findViewById(R.id.sendButton);

        commentsProgress =
                findViewById(R.id.commentsProgress);

        commentsTitle =
                findViewById(R.id.commentsTitle);

        emptyCommentsText =
                findViewById(R.id.emptyCommentsText);
    }

    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(false);

        commentsRecyclerView.setLayoutManager(
                layoutManager
        );

        commentsRecyclerView.setHasFixedSize(false);

        commentsAdapter =
                new ReelCommentsAdapter(
                        this,
                        commentList
                );

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
                v -> addComment()
        );

        commentInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId != 0) {
                        addComment();
                        return true;
                    }

                    return false;
                }
        );
    }

    // =========================================================
    // LOAD COMMENTS
    // =========================================================

    private void loadComments() {

        showLoading(true);

        ReelSupabaseCommentsHelper.getComments(
                this,
                reelId,
                new ReelSupabaseCommentsHelper.CommentsCallback() {

                    @Override
                    public void onSuccess(
                            List<ReelComment> comments) {

                        runOnUiThread(() -> {

                            showLoading(false);

                            commentList.clear();

                            if (comments != null) {
                                commentList.addAll(
                                        comments
                                );
                            }

                            commentsAdapter.notifyDataSetChanged();

                            updateEmptyState();

                            updateTitle();
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() -> {

                            showLoading(false);

                            updateEmptyState();

                            Toast.makeText(
                                    ReelCommentsActivity.this,
                                    message == null
                                            ? "Comments load failed."
                                            : message,
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                }
        );
    }

    // =========================================================
    // ADD COMMENT
    // =========================================================

    private void addComment() {

        if (sendingComment) {
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

        ReelSupabaseCommentsHelper.addComment(
                this,
                reelId,
                text,
                new ReelSupabaseCommentsHelper.ActionCallback() {

                    @Override
                    public void onSuccess(
                            ReelComment newComment) {

                        runOnUiThread(() -> {

                            setSendingState(false);

                            commentInput.setText("");

                            hideKeyboard();

                            if (newComment != null) {

                                commentList.add(
                                        0,
                                        newComment
                                );

                                commentsAdapter
                                        .notifyItemInserted(0);

                                commentsRecyclerView
                                        .scrollToPosition(0);
                            } else {

                                loadComments();
                            }

                            updateEmptyState();
                            updateTitle();

                            Toast.makeText(
                                    ReelCommentsActivity.this,
                                    "Comment added ✓",
                                    Toast.LENGTH_SHORT
                            ).show();
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
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                }
        );
    }

    // =========================================================
    // EMPTY STATE
    // =========================================================

    private void updateEmptyState() {

        if (emptyCommentsText == null) {
            return;
        }

        if (commentList.isEmpty()) {

            emptyCommentsText.setVisibility(
                    View.VISIBLE
            );

            emptyCommentsText.setText(
                    "No comments yet.\nBe the first to comment!"
            );

        } else {

            emptyCommentsText.setVisibility(
                    View.GONE
            );
        }
    }

    // =========================================================
    // TITLE
    // =========================================================

    private void updateTitle() {

        if (commentsTitle == null) {
            return;
        }

        commentsTitle.setText(
                "Comments ("
                        + commentList.size()
                        + ")"
        );
    }

    // =========================================================
    // LOADING
    // =========================================================

    private void showLoading(
            boolean loading) {

        if (commentsProgress != null) {

            commentsProgress.setVisibility(
                    loading
                            ? View.VISIBLE
                            : View.GONE
            );
        }

        if (loading) {

            if (commentsRecyclerView != null) {
                commentsRecyclerView.setVisibility(
                        View.INVISIBLE
                );
            }

        } else {

            if (commentsRecyclerView != null) {
                commentsRecyclerView.setVisibility(
                        View.VISIBLE
                );
            }
        }
    }

    // =========================================================
    // SENDING STATE
    // =========================================================

    private void setSendingState(
            boolean sending) {

        sendingComment = sending;

        if (sendButton != null) {

            sendButton.setEnabled(
                    !sending
            );

            sendButton.setAlpha(
                    sending ? 0.5f : 1.0f
            );
        }

        if (commentInput != null) {

            commentInput.setEnabled(
                    !sending
            );
        }
    }

    // =========================================================
    // KEYBOARD
    // =========================================================

    private void hideKeyboard() {

        View view =
                getCurrentFocus();

        if (view == null) {
            return;
        }

        InputMethodManager imm =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (imm != null) {

            imm.hideSoftInputFromWindow(
                    view.getWindowToken(),
                    0
            );
        }

        view.clearFocus();
    }

    // =========================================================
    // BACK
    // =========================================================

    @Override
    public void onBackPressed() {

        if (sendingComment) {
            return;
        }

        super.onBackPressed();
    }
}
