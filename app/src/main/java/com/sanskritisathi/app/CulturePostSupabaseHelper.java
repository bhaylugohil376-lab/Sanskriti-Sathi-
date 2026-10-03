package com.sanskritisathi.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PostCommentSupabaseHelper {

    private static final String TABLE = "post_comments";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private static final Handler MAIN =
            new Handler(Looper.getMainLooper());

    private PostCommentSupabaseHelper() {
    }

    public interface CommentsCallback {
        void onSuccess(List<PostComment> comments);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public static void getComments(
            Context context,
            String postId,
            @NonNull CommentsCallback callback
    ) {

        if (!isLoggedIn(context)) {
            callback.onError("Please login first.");
            return;
        }

        if (postId == null || postId.trim().isEmpty()) {
            callback.onError("Post not found.");
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String query =
                        "post_id=eq."
                                + encode(postId)
                                + "&select=*"
                                + "&order=created_at.asc"
                                + "&limit=500";

                connection =
                        openConnection(
                                TABLE + "?" + query,
                                "GET"
                        );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(connection, code);

                if (code < 200 || code >= 300) {
                    postError(
                            callback,
                            "Comments load nahi hui: "
                                    + cleanError(response)
                    );
                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                List<PostComment> list =
                        new ArrayList<>();

                for (int i = 0;
                     i < array.length();
                     i++) {

                    JSONObject json =
                            array.getJSONObject(i);

                    String id =
                            json.optString("id", "");

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

                    list.add(
                            new PostComment(
                                    id,
                                    authorUid,
                                    author,
                                    text,
                                    createdAt
                            )
                    );
                }

                postSuccess(callback, list);

            } catch (Exception e) {

                postError(
                        callback,
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

    public static void addComment(
            Context context,
            String postId,
            String text,
            @NonNull ActionCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("Please login first.");
            return;
        }

        if (postId == null ||
                postId.trim().isEmpty()) {

            callback.onError("Post not found.");
            return;
        }

        if (text == null ||
                text.trim().isEmpty()) {

            callback.onError("Write a comment.");
            return;
        }

        String cleanText =
                text.trim();

        if (cleanText.length() > 1000) {

            callback.onError(
                    "Comment maximum 1000 characters ka ho sakta hai."
            );

            return;
        }

        EXECUTOR.execute(() -> {

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
                        "Sanskriti User"
                );

                body.put(
                        "text",
                        cleanText
                );

                body.put(
                        "created_at",
                        java.time.Instant
                                .now()
                                .toString()
                );

                connection =
                        openConnection(
                                TABLE,
                                "POST"
                        );

                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Prefer",
                        "return=minimal"
                );

                writeBody(
                        connection,
                        body
                );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code >= 200 && code < 300) {

                    updatePostCommentCount(
                            postId
                    );

                    postSuccess(callback);

                } else {

                    postError(
                            callback,
                            "Comment failed: "
                                    + cleanError(response)
                    );
                }

            } catch (Exception e) {

                postError(
                        callback,
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

    private static void updatePostCommentCount(
            String postId
    ) {

        HttpURLConnection connection = null;

        try {

            String query =
                    "id=eq."
                            + encode(postId)
                            + "&select=comments";

            connection =
                    openConnection(
                            "posts?" + query,
                            "GET"
                    );

            int code =
                    connection.getResponseCode();

            String response =
                    readResponse(
                            connection,
                            code
                    );

            if (code < 200 || code >= 300) {
                return;
            }

            JSONArray array =
                    new JSONArray(response);

            if (array.length() == 0) {
                return;
            }

            JSONObject post =
                    array.getJSONObject(0);

            int current =
                    post.optInt(
                            "comments",
                            0
                    );

            int newCount =
                    Math.max(0, current) + 1;

            if (connection != null) {
                connection.disconnect();
            }

            JSONObject body =
                    new JSONObject();

            body.put(
                    "comments",
                    newCount
            );

            connection =
                    openConnection(
                            "posts?id=eq."
                                    + encode(postId),
                            "PATCH"
                    );

            connection.setDoOutput(true);

            connection.setRequestProperty(
                    "Prefer",
                    "return=minimal"
            );

            writeBody(
                    connection,
                    body
            );

            connection.getResponseCode();

        } catch (Exception ignored) {

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void deleteComment(
            Context context,
            String commentId,
            @NonNull ActionCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("Please login first.");
            return;
        }

        if (commentId == null ||
                commentId.trim().isEmpty()) {

            callback.onError("Invalid comment.");
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String query =
                        "id=eq."
                                + encode(commentId)
                                + "&author_uid=eq."
                                + encode(uid);

                connection =
                        openConnection(
                                TABLE + "?" + query,
                                "DELETE"
                        );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code >= 200 && code < 300) {

                    postSuccess(callback);

                } else {

                    postError(
                            callback,
                            "Comment delete nahi hua: "
                                    + cleanError(response)
                    );
                }

            } catch (Exception e) {

                postError(
                        callback,
                        "Delete error: "
                                + safeMessage(e)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private static boolean isLoggedIn(
            Context context
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        return uid != null &&
                !uid.trim().isEmpty();
    }

    private static HttpURLConnection openConnection(
            String path,
            String method
    ) throws Exception {

        URL url =
                new URL(
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + path
                );

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod(method);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);

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

        return connection;
    }

    private static void writeBody(
            HttpURLConnection connection,
            JSONObject body
    ) throws Exception {

        byte[] data =
                body.toString()
                        .getBytes(
                                StandardCharsets.UTF_8
                        );

        try (OutputStream output =
                     connection.getOutputStream()) {

            output.write(data);
        }
    }

    private static String readResponse(
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

    private static String encode(
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

    private static String cleanError(
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
                    "hint",
                    response
            );

        } catch (Exception ignored) {

            return response;
        }
    }

    private static String safeMessage(
            Exception e
    ) {

        return e.getMessage() == null
                ? e.getClass().getSimpleName()
                : e.getMessage();
    }

    private static void postSuccess(
            @NonNull CommentsCallback callback,
            List<PostComment> comments
    ) {

        MAIN.post(() ->
                callback.onSuccess(comments)
        );
    }

    private static void postError(
            @NonNull CommentsCallback callback,
            String message
    ) {

        MAIN.post(() ->
                callback.onError(message)
        );
    }

    private static void postSuccess(
            @NonNull ActionCallback callback
    ) {

        MAIN.post(callback::onSuccess);
    }

    private static void postError(
            @NonNull ActionCallback callback,
            String message
    ) {

        MAIN.post(() ->
                callback.onError(message)
        );
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
}
