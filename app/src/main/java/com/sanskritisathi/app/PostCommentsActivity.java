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

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostCommentsActivity extends AppCompatActivity {

    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private ProgressBar progressBar;
    private TextView emptyText;

    private final List<PostComment> commentList =
            new ArrayList<>();

    private PostCommentAdapter adapter;

    private String postId;
    private String postOwnerUid;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private static final String COMMENTS_TABLE =
            "post_comments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_post_comments
        );

        ImageButton backButton =
                findViewById(R.id.commentsBackButton);

        commentsRecyclerView =
                findViewById(R.id.commentsRecyclerView);

        commentInput =
                findViewById(R.id.commentInput);

        ImageButton sendButton =
                findViewById(R.id.commentSendButton);

        progressBar =
                findViewById(R.id.commentProgress);

        emptyText =
                findViewById(R.id.commentsEmptyText);

        postId =
                getIntent().getStringExtra("postId");

        postOwnerUid =
                getIntent().getStringExtra("postOwnerUid");

        if (postId == null ||
                postId.trim().isEmpty()) {

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

        commentsRecyclerView.setAdapter(adapter);

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

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {

                URL url = new URL(
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/posts"
                                + "?id=eq."
                                + encode(postId)
                                + "&select=author_uid"
                );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

                addHeaders(connection);

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(connection, code);

                if (code < 200 || code >= 300) {
                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                if (array.length() == 0) {
                    return;
                }

                JSONObject json =
                        array.getJSONObject(0);

                String owner =
                        json.optString(
                                "author_uid",
                                ""
                        );

                runOnUiThread(() -> {

                    if (!owner.trim().isEmpty()) {
                        postOwnerUid = owner;
                    }
                });

            } catch (Exception ignored) {

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void loadComments() {

        runOnUiThread(() ->
                progressBar.setVisibility(View.VISIBLE)
        );

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {

                URL url = new URL(
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + COMMENTS_TABLE
                                + "?post_id=eq."
                                + encode(postId)
                                + "&order=created_at.asc"
                );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

                addHeaders(connection);

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(connection, code);

                if (code < 200 || code >= 300) {

                    showError(
                            "Comments load failed: "
                                    + cleanError(response)
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                List<PostComment> result =
                        new ArrayList<>();

                for (int i = 0;
                     i < array.length();
                     i++) {

                    JSONObject json =
                            array.getJSONObject(i);

                    PostComment comment =
                            jsonToComment(json);

                    if (comment != null) {
                        result.add(comment);
                    }
                }

                runOnUiThread(() -> {

                    commentList.clear();
                    commentList.addAll(result);

                    adapter.notifyDataSetChanged();

                    progressBar.setVisibility(
                            View.GONE
                    );

                    updateEmptyState();

                    if (!commentList.isEmpty()) {

                        commentsRecyclerView.scrollToPosition(
                                commentList.size() - 1
                        );
                    }
                });

            } catch (Exception e) {

                showError(
                        "Comments load error: "
                                + safeMessage(e)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private void addComment() {

        String uid =
                SupabaseAuthManager.getUserId(this);

        if (uid == null ||
                uid.trim().isEmpty()) {

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

        String author =
                "Sanskriti Sathi User";

        addCommentToSupabase(
                uid,
                author,
                text
        );
    }

    private void addCommentToSupabase(
            String uid,
            String author,
            String text
    ) {

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {

                JSONObject body =
                        new JSONObject();

                body.put(
                        "post_id",
                        postId
                );

                body.put(
                        "author_uid",
                        uid
                );

                body.put(
                        "author",
                        author
                );

                body.put(
                        "text",
                        text
                );

                URL url =
                        new URL(
                                SupabaseConfig.PROJECT_URL
                                        + "/rest/v1/"
                                        + COMMENTS_TABLE
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

                addHeaders(connection);

                connection.setRequestProperty(
                        "Prefer",
                        "return=minimal"
                );

                byte[] data =
                        body.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                connection.setFixedLengthStreamingMode(
                        data.length
                );

                try (OutputStream output =
                             connection.getOutputStream()) {

                    output.write(data);
                }

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code >= 200 && code < 300) {

                    runOnUiThread(() -> {

                        commentInput.setText("");
                        setSending(false);

                        loadComments();
                    });

                } else {

                    showError(
                            "Comment failed: "
                                    + cleanError(response)
                    );
                }

            } catch (Exception e) {

                showError(
                        "Comment error: "
                                + safeMessage(e)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private PostComment jsonToComment(
            JSONObject json
    ) {

        try {

            String id =
                    json.optString(
                            "id",
                            ""
                    );

            String authorUid =
                    json.optString(
                            "author_uid",
                            ""
                    );

            String author =
                    json.optString(
                            "author",
                            "Sanskriti User"
                    );

            String text =
                    json.optString(
                            "text",
                            ""
                    );

            String createdAt =
                    json.optString(
                            "created_at",
                            ""
                    );

            return new PostComment(
                    id,
                    authorUid,
                    author,
                    text,
                    createdAt
            );

        } catch (Exception e) {

            return null;
        }
    }

    private void setSending(
            boolean sending
    ) {

        runOnUiThread(() ->
                progressBar.setVisibility(
                        sending
                                ? View.VISIBLE
                                : View.GONE
                )
        );
    }

    private void updateEmptyState() {

        emptyText.setVisibility(
                commentList.isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void showError(
            String message
    ) {

        runOnUiThread(() -> {

            setSending(false);

            Toast.makeText(
                    PostCommentsActivity.this,
                    message,
                    Toast.LENGTH_LONG
            ).show();

            updateEmptyState();
        });
    }

    private void addHeaders(
            HttpURLConnection connection
    ) {

        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );

        connection.setRequestProperty(
                "Authorization",
                "Bearer "
                        + SupabaseConfig.PUBLISHABLE_KEY
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );
    }

    private String readResponse(
            HttpURLConnection connection,
            int code
    ) throws Exception {

        InputStream stream =
                code >= 200 && code < 400
                        ? connection.getInputStream()
                        : connection.getErrorStream();

        if (stream == null) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     stream,
                                     StandardCharsets.UTF_8
                             )
                     )) {

            String line;

            while ((line = reader.readLine())
                    != null) {

                result.append(line);
            }
        }

        return result.toString();
    }

    private String cleanError(
            String response
    ) {

        if (response == null ||
                response.trim().isEmpty()) {

            return "Server error";
        }

        try {

            JSONObject json =
                    new JSONObject(response);

            String message =
                    json.optString(
                            "message",
                            ""
                    );

            if (!message.isEmpty()) {
                return message;
            }

            return json.optString(
                    "error",
                    response
            );

        } catch (Exception ignored) {

            return response;
        }
    }

    private String safeMessage(
            Exception e
    ) {

        return e.getMessage() == null
                ? e.getClass().getSimpleName()
                : e.getMessage();
    }

    private String encode(
            String value
    ) {

        try {

            return URLEncoder.encode(
                    value,
                    StandardCharsets.UTF_8.name()
            );

        } catch (Exception e) {

            return value;
        }
    }

    public static class PostComment {

        private final String id;
        private final String authorUid;
        private final String author;
        private final String text;
        private final String createdAt;

        public PostComment(
                String id,
                String authorUid,
                String author,
                String text,
                String createdAt
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

        public String getCreatedAt() {
            return createdAt;
        }
    }

    @Override
    protected void onDestroy() {

        executor.shutdownNow();

        super.onDestroy();
    }
}
