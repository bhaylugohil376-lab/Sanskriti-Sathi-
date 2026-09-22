package com.sanskritisathi.app;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
    private final List<ReelComment> comments = new ArrayList<>();

    private String reelId = "";
    private boolean sendingComment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reel_comments);

        reelId = getIntent().getStringExtra("reel_id");

        if (reelId == null || reelId.trim().isEmpty()) {
            Toast.makeText(this, "Reel ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupRecyclerView();
        setupListeners();

        if (!SupabaseAuthManager.isLoggedIn(this)) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadComments();
    }

    private void bindViews() {
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentInput = findViewById(R.id.commentInput);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupRecyclerView() {
        adapter = new ReelCommentsAdapter(this, comments);
        commentsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );
        commentsRecyclerView.setAdapter(adapter);
        commentsRecyclerView.setHasFixedSize(false);
    }

    private void setupListeners() {

        backButton.setOnClickListener(v -> finish());

        sendButton.setOnClickListener(v -> {

            if (sendingComment) {
                return;
            }

            String text = commentInput.getText()
                    .toString()
                    .trim();

            if (text.isEmpty()) {
                return;
            }

            if (text.length() > 500) {
                Toast.makeText(
                        this,
                        "Comment maximum 500 characters ka ho sakta hai",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            addComment(text);
        });
    }

    private void loadComments() {

        ReelCommentSupabaseHelper.getComments(
                this,
                reelId,
                new ReelCommentSupabaseHelper.CommentsCallback() {

                    @Override
                    public void onSuccess(List<ReelComment> result) {

                        runOnUiThread(() -> {

                            comments.clear();

                            if (result != null) {
                                comments.addAll(result);
                            }

                            adapter.notifyDataSetChanged();

                            if (!comments.isEmpty()) {
                                commentsRecyclerView.scrollToPosition(
                                        comments.size() - 1
                                );
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {

                        runOnUiThread(() ->
                                Toast.makeText(
                                        ReelCommentsActivity.this,
                                        error == null
                                                ? "Comments load nahi ho paaye"
                                                : error,
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                }
        );
    }

    private void addComment(String text) {

        sendingComment = true;
        sendButton.setEnabled(false);
        commentInput.setEnabled(false);

        ReelCommentSupabaseHelper.addComment(
                this,
                reelId,
                text,
                new ReelCommentSupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        runOnUiThread(() -> {

                            sendingComment = false;
                            sendButton.setEnabled(true);
                            commentInput.setEnabled(true);

                            commentInput.setText("");

                            hideKeyboard();

                            loadComments();
                        });
                    }

                    @Override
                    public void onError(String error) {

                        runOnUiThread(() -> {

                            sendingComment = false;
                            sendButton.setEnabled(true);
                            commentInput.setEnabled(true);

                            Toast.makeText(
                                    ReelCommentsActivity.this,
                                    error == null
                                            ? "Comment send nahi ho paaya"
                                            : error,
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                }
        );
    }

    private void hideKeyboard() {

        View currentFocus = getCurrentFocus();

        if (currentFocus == null) {
            return;
        }

        InputMethodManager imm =
                (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );

        if (imm != null) {
            imm.hideSoftInputFromWindow(
                    currentFocus.getWindowToken(),
                    0
            );
        }
    }
}

Important: XML mein "sendButton" agar "ImageButton" hai, to upar wala "ImageButton sendButton" hi rakho. "MaterialButton" mat karna.
