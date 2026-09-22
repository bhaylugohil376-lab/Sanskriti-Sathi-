package com.sanskritisathi.app;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ReelCommentsActivity extends AppCompatActivity {

    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private MaterialButton sendButton;
    private ImageButton backButton;

    private ReelCommentsAdapter adapter;

    private final List<ReelComment> commentList =
            new ArrayList<>();

    private String reelId = "";

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

        if (reelId == null ||
                reelId.trim().isEmpty()) {

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

        if (!SupabaseAuthManager.isLoggedIn(this)) {

            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        loadComments();
    }

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
                        commentList
                );

        commentsRecyclerView.setAdapter(
                adapter
        );
    }

    private void setupListeners() {

        backButton.setOnClickListener(
                v -> finish()
        );

        sendButton.setOnClickListener(
                v -> sendComment()
        );
    }

    // =========================================================
    // LOAD COMMENTS
    // =========================================================

    private void loadComments() {

        ReelCommentSupabaseHelper.getComments(
                this,
                reelId,
                new ReelCommentSupabaseHelper.CommentsCallback() {

                    @Override
                    public void onSuccess(
                            List<ReelComment> comments) {

                        runOnUiThread(() -> {

                            commentList.clear();

                            if (comments != null) {
                                commentList.addAll(
                                        comments
                                );
                            }

                            adapter.setComments(
                                    commentList
                            );

                            commentsRecyclerView.post(
                                    () -> {

                                        if (adapter.getItemCount()
                                                > 0) {

                                            commentsRecyclerView
                                                    .scrollToPosition(
                                                            adapter.getItemCount()
                                                                    - 1
                                                    );
                                        }
                                    }
                            );
                        });
                    }

                    @Override
                    public void onError(
                            String message) {

                        runOnUiThread(() ->
                                Toast.makeText(
                                        ReelCommentsActivity.this,
                                        message == null
                                                ? "Comments load nahi hui."
                                                : message,
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }
                }
        );
    }

    // =========================================================
    // SEND COMMENT
    // =========================================================

    private void sendComment() {

        String text =
                commentInput.getText()
                        .toString()
                        .trim();

        if (text.isEmpty()) {

            commentInput.setError(
                    "Comment likho"
            );

            commentInput.requestFocus();

            return;
        }

        if (text.length() > 500) {

            commentInput.setError(
                    "Maximum 500 characters"
            );

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

                            commentInput.setText("");

                            hideKeyboard();

                            setSendingState(false);

                            loadComments();

                            Toast.makeText(
                                    ReelCommentsActivity.this,
                                    "Comment added",
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
                                            ? "Comment send nahi hua."
                                            : message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    // =========================================================
    // SEND STATE
    // =========================================================

    private void setSendingState(
            boolean sending) {

        sendButton.setEnabled(!sending);

        commentInput.setEnabled(!sending);

        if (sending) {

            sendButton.setText(
                    "Sending..."
            );

        } else {

            sendButton.setText(
                    "Send"
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
    // REFRESH WHEN RETURNING
    // =========================================================

    @Override
    protected void onResume() {
        super.onResume();

        if (reelId != null &&
                !reelId.trim().isEmpty() &&
                SupabaseAuthManager.isLoggedIn(this)) {

            loadComments();
        }
    }
}
