package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostCommentsActivity extends AppCompatActivity {

    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private ProgressBar progressBar;
    private TextView emptyText;

    private final List<PostComment> commentList =
            new ArrayList<>();

    private PostCommentAdapter adapter;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private String postId;
    private String postOwnerUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_post_comments
        );

        firestore =
                FirebaseFirestore.getInstance();

        auth =
                FirebaseAuth.getInstance();

        ImageButton backButton =
                findViewById(
                        R.id.commentsBackButton
                );

        commentsRecyclerView =
                findViewById(
                        R.id.commentsRecyclerView
                );

        commentInput =
                findViewById(
                        R.id.commentInput
                );

        ImageButton sendButton =
                findViewById(
                        R.id.commentSendButton
                );

        progressBar =
                findViewById(
                        R.id.commentProgress
                );

        emptyText =
                findViewById(
                        R.id.commentsEmptyText
                );

        postId =
                getIntent().getStringExtra(
                        "postId"
                );

        postOwnerUid =
                getIntent().getStringExtra(
                        "postOwnerUid"
                );

        if (postId == null
                || postId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Post not found.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        commentsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
                new PostCommentAdapter(
                        this,
                        commentList
                );

        commentsRecyclerView.setAdapter(
                adapter
        );

        backButton.setOnClickListener(
                v -> finish()
        );

        sendButton.setOnClickListener(
                v -> addComment()
        );

        loadPostOwner();
        loadComments();
    }

    private void loadPostOwner() {

        firestore.collection("posts")
                .document(postId)
                .get()
                .addOnSuccessListener(
                        document -> {

                            if (document.exists()) {

                                String owner =
                                        document.getString(
                                                "authorUid"
                                        );

                                if (owner != null
                                        && !owner.trim().isEmpty()) {

                                    postOwnerUid =
                                            owner;
                                }
                            }
                        }
                );
    }

    private void loadComments() {

        firestore.collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy(
                        "createdAt",
                        Query.Direction.ASCENDING
                )
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            commentList.clear();

                            for (
                                    DocumentSnapshot document
                                    : snapshot.getDocuments()
                            ) {

                                String id =
                                        document.getId();

                                String authorUid =
                                        document.getString(
                                                "authorUid"
                                        );

                                String author =
                                        document.getString(
                                                "author"
                                        );

                                String text =
                                        document.getString(
                                                "text"
                                        );

                                Timestamp createdAt =
                                        document.getTimestamp(
                                                "createdAt"
                                        );

                                PostComment comment =
                                        new PostComment(
                                                id,
                                                authorUid,
                                                author,
                                                text,
                                                createdAt
                                        );

                                commentList.add(
                                        comment
                                );
                            }

                            adapter.notifyDataSetChanged();

                            updateEmptyState();

                            if (!commentList.isEmpty()) {

                                commentsRecyclerView
                                        .scrollToPosition(
                                                commentList.size() - 1
                                        );
                            }
                        }
                )
                .addOnFailureListener(
                        error -> {

                            Toast.makeText(
                                    this,
                                    "Comments load failed.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            updateEmptyState();
                        }
                );
    }

    private void addComment() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String text =
                commentInput.getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(text)) {

            commentInput.setError(
                    "Write a comment"
            );

            return;
        }

        if (text.length() > 1000) {

            commentInput.setError(
                    "Comment is too long"
            );

            return;
        }

        setSending(true);

        String uid =
                auth.getCurrentUser().getUid();

        String displayName =
                auth.getCurrentUser()
                        .getDisplayName();

        final String commentAuthor;

        if (displayName == null
                || displayName.trim().isEmpty()) {

            commentAuthor =
                    "Sanskriti Sathi User";

        } else {

            commentAuthor =
                    displayName.trim();
        }

        Map<String, Object> comment =
                new HashMap<>();

        comment.put(
                "authorUid",
                uid
        );

        comment.put(
                "author",
                commentAuthor
        );

        comment.put(
                "text",
                text
        );

        comment.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        firestore.collection("posts")
                .document(postId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(
                        documentReference -> {

                            firestore.collection("posts")
                                    .document(postId)
                                    .update(
                                            "comments",
                                            FieldValue.increment(1)
                                    );

                            if (postOwnerUid != null
                                    && !postOwnerUid.isEmpty()
                                    && !postOwnerUid.equals(uid)) {

                                NotificationFirebaseHelper
                                        .createNotification(
                                                postOwnerUid,
                                                "New Comment",
                                                commentAuthor
                                                        + " commented on your post.",
                                                "comment",
                                                postId
                                        );
                            }

                            commentInput.setText("");

                            setSending(false);

                            loadComments();
                        }
                )
                .addOnFailureListener(
                        error -> {

                            setSending(false);

                            Toast.makeText(
                                    this,
                                    "Comment failed: "
                                            + error.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private void setSending(
            boolean sending
    ) {

        progressBar.setVisibility(
                sending
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void updateEmptyState() {

        if (commentList.isEmpty()) {

            emptyText.setVisibility(
                    View.VISIBLE
            );

        } else {

            emptyText.setVisibility(
                    View.GONE
            );
        }
    }

    public static class PostComment {

        private final String id;
        private final String authorUid;
        private final String author;
        private final String text;
        private final Timestamp createdAt;

        public PostComment(
                String id,
                String authorUid,
                String author,
                String text,
                Timestamp createdAt
        ) {
            this.id = id;
            this.authorUid = authorUid;
            this.author = author;
            this.text = text;
            this.createdAt = createdAt;
        }

        public String getId() {
            return id;
        }

        public String getAuthorUid() {
            return authorUid;
        }

        public String getAuthor() {
            return author;
        }

        public String getText() {
            return text;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }
    }
}
