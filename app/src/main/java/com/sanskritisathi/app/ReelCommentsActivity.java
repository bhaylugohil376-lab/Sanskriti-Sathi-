package com.sanskritisathi.app;

import android.os.Bundle;
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

    private final List<ReelComment> commentList =
            new ArrayList<>();

    private ReelCommentsAdapter adapter;

    private String reelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reel_comments);

        reelId = getIntent().getStringExtra("reel_id");

        if (reelId == null || reelId.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Invalid Reel.",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        commentsRecyclerView =
                findViewById(R.id.commentsRecyclerView);

        commentInput =
                findViewById(R.id.commentInput);

        sendButton =
                findViewById(R.id.sendButton);

        commentsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new ReelCommentsAdapter(
                this,
                commentList,
                new ReelCommentsAdapter.CommentActionListener() {

                    @Override
                    public void onDelete(
                            ReelComment comment,
                            int position) {

                        deleteComment(comment, position);
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                ReelCommentsActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        commentsRecyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(v ->
                addComment()
        );

        loadComments();
    }

    private void loadComments() {

        ReelCommentFirebaseHelper.getComments(
                reelId,
                new ReelCommentFirebaseHelper.CommentsCallback() {

                    @Override
                    public void onSuccess(
                            List<ReelComment> comments) {

                        commentList.clear();
                        commentList.addAll(comments);

                        adapter.notifyDataSetChanged();

                        if (!commentList.isEmpty()) {
                            commentsRecyclerView.scrollToPosition(
                                    commentList.size() - 1
                            );
                        }
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                ReelCommentsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void addComment() {

        String text =
                commentInput.getText()
                        .toString()
                        .trim();

        if (text.isEmpty()) {
            Toast.makeText(
                    this,
                    "Comment likho.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        sendButton.setEnabled(false);

        ReelCommentFirebaseHelper.addComment(
                reelId,
                text,
                new ReelCommentFirebaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        runOnUiThread(() -> {

                            commentInput.setText("");

                            sendButton.setEnabled(true);

                            loadComments();
                        });
                    }

                    @Override
                    public void onError(String message) {

                        runOnUiThread(() -> {

                            sendButton.setEnabled(true);

                            Toast.makeText(
                                    ReelCommentsActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    private void deleteComment(
            ReelComment comment,
            int position) {

        ReelCommentFirebaseHelper.deleteComment(
                reelId,
                comment.getId(),
                new ReelCommentFirebaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        if (position >= 0 &&
                                position < commentList.size()) {

                            commentList.remove(position);

                            adapter.notifyItemRemoved(
                                    position
                            );
                        }

                        Toast.makeText(
                                ReelCommentsActivity.this,
                                "Comment deleted.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                ReelCommentsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
}
