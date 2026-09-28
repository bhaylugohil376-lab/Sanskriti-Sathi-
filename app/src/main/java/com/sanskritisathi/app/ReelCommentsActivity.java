package com.sanskritisathi.app;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReelCommentsActivity extends AppCompatActivity {

    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private ImageButton sendButton;
    private ImageButton closeButton;
    private ProgressBar progressBar;
    private TextView emptyText;
    private TextView commentTitle;

    private final List<ReelComment> commentList =
            new ArrayList<>();

    private CommentAdapter adapter;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private String reelId = "";

    private static final String COMMENTS_TABLE =
            "reel_comments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reel_comments);

        bindViews();
        setupRecyclerView();
        setupListeners();

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

        if (!SupabaseAuthManager.isLoggedIn(this)) {
            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        updateTitle();
        updateEmptyState();

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

        sendButton =
                findViewById(R.id.sendButton);

        closeButton =
                findViewById(R.id.closeButton);

        progressBar =
                findViewById(R.id.commentsProgress);

        emptyText =
                findViewById(R.id.emptyCommentsText);

        commentTitle =
                findViewById(R.id.commentTitle);
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

        adapter =
                new CommentAdapter(
                        this,
                        commentList
                );

        commentsRecyclerView.setAdapter(
                adapter
        );
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void setupListeners() {

        if (closeButton != null) {
            closeButton.setOnClickListener(
                    v -> finish()
            );
        }

        if (sendButton != null) {
            sendButton.setOnClickListener(
                    v -> addComment()
            );
        }

        if (commentInput != null) {

            commentInput.setOnEditorActionListener(
                    (v, actionId, event) -> {

                        boolean send =
                                actionId == EditorInfo.IME_ACTION_SEND
                                        || actionId == EditorInfo.IME_ACTION_DONE
                                        || (
                                        event != null
                                                && event.getKeyCode()
                                                == KeyEvent.KEYCODE_ENTER
                                                && event.getAction()
                                                == KeyEvent.ACTION_DOWN
                                );

                        if (send) {
                            addComment();
                            return true;
                        }

                        return false;
                    }
            );
        }
    }

    // =========================================================
    // LOAD COMMENTS
    // =========================================================

    private void loadComments() {

        showLoading(true);

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String token =
                        SupabaseAuthManager
                                .getAccessToken(this);

                if (TextUtils.isEmpty(token)) {
                    throw new Exception(
                            "Login session nahi mili."
                    );
                }

                String encodedReelId =
                        URLEncoder.encode(
                                reelId,
                                "UTF-8"
                        );

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + COMMENTS_TABLE
                                + "?select=*"
                                + "&reel_id=eq."
                                + encodedReelId
                                + "&order=created_at.asc";

                connection =
                        openConnection(
                                url,
                                "GET",
                                token
                        );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code < 200 || code >= 300) {
                    throw new Exception(
                            "Comments load failed: "
                                    + response
                    );
                }

                JSONArray array =
                        new JSONArray(response);

                List<ReelComment> result =
                        new ArrayList<>();

                for (int i = 0;
                     i < array.length();
                     i++) {

                    JSONObject json =
                            array.getJSONObject(i);

                    ReelComment comment =
                            parseComment(json);

                    if (comment != null) {
                        result.add(comment);
                    }
                }

                runOnUiThread(() -> {

                    commentList.clear();
                    commentList.addAll(result);

                    adapter.notifyDataSetChanged();

                    showLoading(false);

                    updateEmptyState();
                    updateTitle();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    showLoading(false);

                    updateEmptyState();

                    Toast.makeText(
                            ReelCommentsActivity.this,
                            safeMessage(
                                    e,
                                    "Comments load nahi hui."
                            ),
                            Toast.LENGTH_LONG
                    ).show();
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // =========================================================
    // ADD COMMENT
    // =========================================================

    private void addComment() {

        if (!SupabaseAuthManager.isLoggedIn(this)) {

            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (commentInput == null) {
            return;
        }

        String text =
                commentInput
                        .getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(text)) {
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

        setCommentSending(true);

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String userId =
                        SupabaseAuthManager
                                .getUserId(this);

                String token =
                        SupabaseAuthManager
                                .getAccessToken(this);

                if (TextUtils.isEmpty(userId)
                        || TextUtils.isEmpty(token)) {

                    throw new Exception(
                            "Login session nahi mili."
                    );
                }

                String username =
                        getUsername(
                                userId,
                                token
                        );

                if (TextUtils.isEmpty(username)) {
                    username = "Sanskriti User";
                }

                String commentId =
                        UUID.randomUUID().toString();

                JSONObject json =
                        new JSONObject();

                json.put("id", commentId);
                json.put("reel_id", reelId);
                json.put("user_id", userId);
                json.put("username", username);
                json.put("text", text);

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + COMMENTS_TABLE;

                connection =
                        openConnection(
                                url,
                                "POST",
                                token
                        );

                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                connection.setRequestProperty(
                        "Prefer",
                        "return=representation"
                );

                byte[] body =
                        json.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                try (OutputStream output =
                             connection.getOutputStream()) {

                    output.write(body);
                    output.flush();
                }

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code < 200 || code >= 300) {
                    throw new Exception(
                            "Comment add failed: "
                                    + response
                    );
                }

                long createdAt =
                        System.currentTimeMillis();

                ReelComment newComment =
                        new ReelComment(
                                commentId,
                                reelId,
                                userId,
                                username,
                                text,
                                createdAt
                        );

                runOnUiThread(() -> {

                    commentList.add(
                            newComment
                    );

                    adapter.notifyItemInserted(
                            commentList.size() - 1
                    );

                    updateEmptyState();
                    updateTitle();

                    commentsRecyclerView.post(() ->
                            commentsRecyclerView.scrollToPosition(
                                    commentList.size() - 1
                            )
                    );

                    commentInput.setText("");

                    hideKeyboard();

                    setCommentSending(false);
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    setCommentSending(false);

                    Toast.makeText(
                            ReelCommentsActivity.this,
                            safeMessage(
                                    e,
                                    "Comment add nahi hui."
                            ),
                            Toast.LENGTH_LONG
                    ).show();
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
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

        String currentUserId =
                SupabaseAuthManager
                        .getUserId(this);

        if (TextUtils.isEmpty(currentUserId)
                || !currentUserId.equals(
                        comment.getUserId()
                )) {

            Toast.makeText(
                    this,
                    "You can delete only your comment.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String token =
                        SupabaseAuthManager
                                .getAccessToken(this);

                if (TextUtils.isEmpty(token)) {
                    throw new Exception(
                            "Login session nahi mili."
                    );
                }

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + COMMENTS_TABLE
                                + "?id=eq."
                                + URLEncoder.encode(
                                        comment.getId(),
                                        "UTF-8"
                                )
                                + "&user_id=eq."
                                + URLEncoder.encode(
                                        currentUserId,
                                        "UTF-8"
                                );

                connection =
                        openConnection(
                                url,
                                "DELETE",
                                token
                        );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code < 200 || code >= 300) {
                    throw new Exception(
                            "Comment delete failed: "
                                    + response
                    );
                }

                runOnUiThread(() -> {

                    int safePosition =
                            position;

                    if (safePosition >= 0
                            && safePosition < commentList.size()) {

                        commentList.remove(
                                safePosition
                        );

                        adapter.notifyItemRemoved(
                                safePosition
                        );
                    }

                    updateEmptyState();
                    updateTitle();

                    Toast.makeText(
                            ReelCommentsActivity.this,
                            "Comment deleted.",
                            Toast.LENGTH_SHORT
                    ).show();
                });

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                ReelCommentsActivity.this,
                                safeMessage(
                                        e,
                                        "Comment delete failed."
                                ),
                                Toast.LENGTH_LONG
                        ).show()
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // =========================================================
    // PARSE COMMENT
    // =========================================================

    private ReelComment parseComment(
            JSONObject json) {

        try {

            String id =
                    json.optString(
                            "id",
                            ""
                    );

            String reel =
                    json.optString(
                            "reel_id",
                            ""
                    );

            String user =
                    json.optString(
                            "user_id",
                            ""
                    );

            String username =
                    json.optString(
                            "username",
                            "Sanskriti User"
                    );

            String text =
                    json.optString(
                            "text",
                            ""
                    );

            String created =
                    json.optString(
                            "created_at",
                            ""
                    );

            long createdAt =
                    parseCreatedAt(created);

            if (TextUtils.isEmpty(id)
                    || TextUtils.isEmpty(reel)
                    || TextUtils.isEmpty(user)) {
                return null;
            }

            return new ReelComment(
                    id,
                    reel,
                    user,
                    username,
                    text,
                    createdAt
            );

        } catch (Exception e) {

            return null;
        }
    }

    // =========================================================
    // GET USERNAME
    // =========================================================

    private String getUsername(
            String userId,
            String token) {

        HttpURLConnection connection = null;

        try {

            String url =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/profiles"
                            + "?select=username"
                            + "&id=eq."
                            + URLEncoder.encode(
                                    userId,
                                    "UTF-8"
                            )
                            + "&limit=1";

            connection =
                    openConnection(
                            url,
                            "GET",
                            token
                    );

            int code =
                    connection.getResponseCode();

            String response =
                    readResponse(
                            connection,
                            code
                    );

            if (code < 200 || code >= 300) {
                return "";
            }

            JSONArray array =
                    new JSONArray(response);

            if (array.length() == 0) {
                return "";
            }

            return array.getJSONObject(0)
                    .optString(
                            "username",
                            ""
                    );

        } catch (Exception ignored) {

            return "";

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // =========================================================
    // CONNECTION
    // =========================================================

    private HttpURLConnection openConnection(
            String urlString,
            String method,
            String token)
            throws Exception {

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod(method);

        connection.setConnectTimeout(
                30000
        );

        connection.setReadTimeout(
                60000
        );

        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        if (!TextUtils.isEmpty(token)) {

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token
            );
        }

        return connection;
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    private String readResponse(
            HttpURLConnection connection,
            int code)
            throws Exception {

        InputStream input;

        if (code >= 400) {
            input =
                    connection.getErrorStream();
        } else {
            input =
                    connection.getInputStream();
        }

        if (input == null) {
            return "";
        }

        StringBuilder builder =
                new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     input,
                                     StandardCharsets.UTF_8
                             )
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }

        return builder.toString();
    }

    // =========================================================
    // UI
    // =========================================================

    private void showLoading(
            boolean loading) {

        if (progressBar != null) {

            progressBar.setVisibility(
                    loading
                            ? View.VISIBLE
                            : View.GONE
            );
        }
    }

    private void setCommentSending(
            boolean sending) {

        if (sendButton != null) {
            sendButton.setEnabled(!sending);
        }

        if (commentInput != null) {
            commentInput.setEnabled(!sending);
        }
    }

    private void updateEmptyState() {

        if (emptyText == null) {
            return;
        }

        emptyText.setVisibility(
                commentList.isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void updateTitle() {

        if (commentTitle != null) {

            commentTitle.setText(
                    "Comments "
                            + commentList.size()
            );
        }
    }

    private void hideKeyboard() {

        View view =
                getCurrentFocus();

        if (view == null) {
            view = commentInput;
        }

        if (view == null) {
            return;
        }

        InputMethodManager manager =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (manager != null) {

            manager.hideSoftInputFromWindow(
                    view.getWindowToken(),
                    0
            );
        }
    }

    // =========================================================
    // TIME
    // =========================================================

    private long parseCreatedAt(
            String value) {

        if (TextUtils.isEmpty(value)) {
            return System.currentTimeMillis();
        }

        try {

            return Instant.parse(
                    value
            ).toEpochMilli();

        } catch (Exception ignored) {

            return System.currentTimeMillis();
        }
    }

    // =========================================================
    // ERROR
    // =========================================================

    private String safeMessage(
            Exception e,
            String fallback) {

        if (e == null) {
            return fallback;
        }

        String message =
                e.getMessage();

        if (TextUtils.isEmpty(message)) {
            return fallback;
        }

        return message;
    }

    // =========================================================
    // ADAPTER
    // =========================================================

    private class CommentAdapter
            extends RecyclerView.Adapter<
            CommentAdapter.CommentHolder> {

        private final Context context;
        private final List<ReelComment> list;

        CommentAdapter(
                Context context,
                List<ReelComment> list) {

            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public CommentHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType) {

            View view =
                    LayoutInflater.from(
                            parent.getContext()
                    ).inflate(
                            R.layout.item_reel_comment,
                            parent,
                            false
                    );

            return new CommentHolder(view);
        }

        @Override
        public void onBindViewHolder(
                @NonNull CommentHolder holder,
                int position) {

            ReelComment comment =
                    list.get(position);

            String username =
                    comment.getUsername();

            if (TextUtils.isEmpty(username)) {
                username = "Sanskriti User";
            }

            holder.usernameText.setText(
                    username
            );

            holder.commentText.setText(
                    comment.getText()
            );

            String currentUserId =
                    SupabaseAuthManager
                            .getUserId(
                                    ReelCommentsActivity.this
                            );

            boolean ownComment =
                    !TextUtils.isEmpty(
                            currentUserId
                    )
                            && currentUserId.equals(
                            comment.getUserId()
                    );

            holder.deleteButton.setVisibility(
                    ownComment
                            ? View.VISIBLE
                            : View.GONE
            );

            holder.deleteButton.setOnClickListener(
                    v -> {

                        int adapterPosition =
                                holder.getBindingAdapterPosition();

                        if (adapterPosition !=
                                RecyclerView.NO_POSITION) {

                            deleteComment(
                                    comment,
                                    adapterPosition
                            );
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class CommentHolder
                extends RecyclerView.ViewHolder {

            TextView usernameText;
            TextView commentText;
            ImageButton deleteButton;

            CommentHolder(
                    @NonNull View itemView) {

                super(itemView);

                usernameText =
                        itemView.findViewById(
                                R.id.commentUsername
                        );

                commentText =
                        itemView.findViewById(
                                R.id.commentText
                        );

                deleteButton =
                        itemView.findViewById(
                                R.id.commentDeleteButton
                        );
            }
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        executor.shutdownNow();
    }
}
