package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReelCommentsActivity extends AppCompatActivity {

    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private ImageButton sendButton;
    private ImageButton backButton;

    private ReelCommentsAdapter adapter;

    private String reelId;

    private boolean sendingComment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_reel_comments
        );

        bindViews();
        setupRecyclerView();
        setupListeners();

        reelId = getIntent().getStringExtra(
                "reel_id"
        );

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

    // ============================================================
    // BIND VIEWS
    // ============================================================

    private void bindViews() {

        commentsRecyclerView =
                findViewById(
                        R.id.commentsRecyclerView
                );

        commentInput =
                findViewById(
                        R.id.commentInput
                );

        sendButton =
                findViewById(
                        R.id.sendButton
                );

        backButton =
                findViewById(
                        R.id.backButton
                );
    }

    // ============================================================
    // RECYCLER VIEW
    // ============================================================

    private void setupRecyclerView() {

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);

        commentsRecyclerView.setLayoutManager(
                layoutManager
        );

        adapter =
                new ReelCommentsAdapter(
                        this,
                        new ArrayList<>()
                );

        commentsRecyclerView.setAdapter(
                adapter
        );
    }

    // ============================================================
    // LISTENERS
    // ============================================================

    private void setupListeners() {

        backButton.setOnClickListener(
                v -> finish()
        );

        sendButton.setOnClickListener(
                v -> sendComment()
        );
    }

    // ============================================================
    // LOAD COMMENTS
    // ============================================================

    private void loadComments() {

        if (TextUtils.isEmpty(reelId)) {
            return;
        }

        ReelCommentSupabaseHelper.getComments(
                reelId,
                new ReelCommentSupabaseHelper.CommentsCallback() {

                    @Override
                    public void onSuccess(
                            List<ReelComment> comments) {

                        runOnUiThread(() -> {

                            adapter.setComments(
                                    comments
                            );

                            if (comments != null
                                    && !comments.isEmpty()) {

                                commentsRecyclerView.scrollToPosition(
                                        comments.size() - 1
                                );
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
                                                ? "Comments load nahi hue."
                                                : message,
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }
                }
        );
    }

    // ============================================================
    // SEND COMMENT
    // ============================================================

    private void sendComment() {

        if (sendingComment) {
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
                    "Comment empty nahi ho sakta.",
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

        if (TextUtils.isEmpty(reelId)) {

            Toast.makeText(
                    this,
                    "Invalid Reel.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!SupabaseAuthManager.isLoggedIn(this)) {

            Toast.makeText(
                    this,
                    "Comment karne ke liye login karein.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        setSendingState(true);

        ReelCommentSupabaseHelper.addComment(
                reelId,
                text,
                new ReelCommentSupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        runOnUiThread(() -> {

                            commentInput.setText("");

                            hideKeyboard();

                            setSendingState(false);

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
                                            ? "Comment send nahi hua."
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    // ============================================================
    // SEND STATE
    // ============================================================

    private void setSendingState(
            boolean sending) {

        sendingComment = sending;

        sendButton.setEnabled(
                !sending
        );

        commentInput.setEnabled(
                !sending
        );
    }

    // ============================================================
    // HIDE KEYBOARD
    // ============================================================

    private void hideKeyboard() {

        InputMethodManager imm =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (imm != null) {

            imm.hideSoftInputFromWindow(
                    commentInput.getWindowToken(),
                    0
            );
        }
    }

    // ============================================================
    // RESUME
    // ============================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (!TextUtils.isEmpty(reelId)
                && adapter != null) {

            loadComments();
        }
    }
}
