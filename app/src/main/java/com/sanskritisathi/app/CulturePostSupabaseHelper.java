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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CulturePostSupabaseHelper {

    private CulturePostSupabaseHelper() {
    }

    private static final String TABLE = "posts";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private static final Handler MAIN =
            new Handler(Looper.getMainLooper());

    public interface PostsCallback {
        void onSuccess(List<CulturePost> posts);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface LikeCallback {
        void onSuccess(boolean liked, int likeCount);
        void onError(String message);
    }

    public interface LikeStatusCallback {
        void onResult(boolean liked);
    }

    // =========================================================
    // PUBLIC POSTS
    // =========================================================

    public static void getPublicPosts(
            @NonNull PostsCallback callback
    ) {
        requestPosts(
                "visibility=eq.Public&order=created_at.desc&limit=100",
                callback
        );
    }

    // =========================================================
    // MY POSTS
    // =========================================================

    public static void getMyPosts(
            Context context,
            @NonNull PostsCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("Pehle Login karein.");
            return;
        }

        requestPosts(
                "author_uid=eq."
                        + encode(uid)
                        + "&order=created_at.desc&limit=100",
                callback
        );
    }

    // =========================================================
    // LOAD POSTS
    // =========================================================

    private static void requestPosts(
            String query,
            @NonNull PostsCallback callback
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                URL url = new URL(
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + TABLE
                                + "?"
                                + query
                );

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

                addHeaders(connection, false);

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code < 200 || code >= 300) {
                    postError(
                            callback,
                            "Posts load nahi hue: "
                                    + cleanError(response)
                    );
                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                List<CulturePost> posts =
                        new ArrayList<>();

                for (int i = 0;
                     i < array.length();
                     i++) {

                    JSONObject json =
                            array.getJSONObject(i);

                    CulturePost post =
                            jsonToPost(json);

                    if (post != null) {
                        posts.add(post);
                    }
                }

                postSuccess(
                        callback,
                        posts
                );

            } catch (Exception e) {

                postError(
                        callback,
                        "Posts load error: "
                                + safeMessage(e)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // =========================================================
    // CREATE POST
    // =========================================================

    public static void createPost(
            Context context,
            String category,
            String caption,
            String imageUrl,
            String visibility,
            @NonNull ActionCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("Pehle Login karein.");
            return;
        }

        if (caption == null ||
                caption.trim().isEmpty()) {

            callback.onError("Caption khali hai.");
            return;
        }

        String cleanCaption =
                caption.trim();

        if (cleanCaption.length() > 5000) {
            callback.onError(
                    "Caption maximum 5000 characters ka ho sakta hai."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                JSONObject body =
                        new JSONObject();

                body.put(
                        "author_uid",
                        uid
                );

                body.put(
                        "author",
                        "Sanskriti User"
                );

                body.put(
                        "category",
                        category == null ||
                                category.trim().isEmpty()
                                ? "Indian Culture"
                                : category.trim()
                );

                body.put(
                        "caption",
                        cleanCaption
                );

                body.put(
                        "image_url",
                        imageUrl == null
                                ? ""
                                : imageUrl.trim()
                );

                body.put(
                        "visibility",
                        "Followers".equalsIgnoreCase(
                                visibility
                        )
                                ? "Followers"
                                : "Public"
                );

                body.put("likes", 0);
                body.put("comments", 0);

                URL url =
                        new URL(
                                SupabaseConfig.PROJECT_URL
                                        + "/rest/v1/"
                                        + TABLE
                                );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

                addHeaders(connection, true);

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

                    postSuccess(callback);

                } else {

                    postError(
                            callback,
                            "Post create nahi hui: "
                                    + cleanError(response)
                    );
                }

            } catch (Exception e) {

                postError(
                        callback,
                        "Post create error: "
                                + safeMessage(e)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // =========================================================
    // LIKE / UNLIKE
    // =========================================================

    public static void toggleLike(
            Context context,
            String postId,
            @NonNull LikeCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("Pehle Login karein.");
            return;
        }

        if (postId == null ||
                postId.trim().isEmpty()) {

            callback.onError(
                    "Post ID available nahi hai."
            );
            return;
        }

        /*
         * IMPORTANT:
         *
         * Supabase schema mein likes table/relationship
         * confirm kiye bina fake endpoint nahi banaya.
         *
         * Isliye yahan existing schema ke according
         * likes implementation connect karna hoga.
         */
        callback.onError(
                "Likes ke liye Supabase posts/likes schema verify karna hai."
        );
    }

    // =========================================================
    // CHECK LIKE
    // =========================================================

    public static void checkLiked(
            Context context,
            String postId,
            @NonNull LikeStatusCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null ||
                uid.trim().isEmpty() ||
                postId == null ||
                postId.trim().isEmpty()) {

            callback.onResult(false);
            return;
        }

        /*
         * Existing Supabase schema verify hone tak
         * false return — Firebase fallback nahi.
         */
        callback.onResult(false);
    }

    // =========================================================
    // DELETE POST
    // =========================================================

    public static void deletePost(
            Context context,
            String postId,
            @NonNull ActionCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("Pehle Login karein.");
            return;
        }

        if (postId == null ||
                postId.trim().isEmpty()) {

            callback.onError(
                    "Post ID available nahi hai."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                URL url =
                        new URL(
                                SupabaseConfig.PROJECT_URL
                                        + "/rest/v1/"
                                        + TABLE
                                        + "?id=eq."
                                        + encode(postId)
                                        + "&author_uid=eq."
                                        + encode(uid)
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("DELETE");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

                addHeaders(connection, false);

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
                            "Post delete nahi hui: "
                                    + cleanError(response)
                    );
                }

            } catch (Exception e) {

                postError(
                        callback,
                        "Post delete error: "
                                + safeMessage(e)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // =========================================================
    // JSON -> CULTURE POST
    // =========================================================

    private static CulturePost jsonToPost(
            JSONObject json
    ) {

        try {

            String id =
                    json.optString(
                            "id",
                            json.optString(
                                    "post_id",
                                    ""
                            )
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

            String category =
                    json.optString(
                            "category",
                            "Indian Culture"
                    );

            String caption =
                    json.optString(
                            "caption",
                            ""
                    );

            String imageUrl =
                    json.optString(
                            "image_url",
                            json.optString(
                                    "imageUrl",
                                    ""
                            )
                    );

            String visibility =
                    json.optString(
                            "visibility",
                            "Public"
                    );

            long createdAt =
                    parseCreatedAt(
                            json.optString(
                                    "created_at",
                                    ""
                            )
                    );

            int likes =
                    json.optInt(
                            "likes",
                            0
                    );

            int comments =
                    json.optInt(
                            "comments",
                            0
                    );

            return new CulturePost(
                    id,
                    authorUid,
                    author,
                    category,
                    caption,
                    imageUrl,
                    visibility,
                    createdAt,
                    Math.max(0, likes),
                    Math.max(0, comments),
                    R.drawable.icon_foreground,
                    R.drawable.icon_foreground,
                    false,
                    false
            );

        } catch (Exception e) {
            return null;
        }
    }

    private static long parseCreatedAt(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {
            return 0L;
        }

        try {

            return java.time.Instant
                    .parse(value)
                    .toEpochMilli();

        } catch (Exception ignored) {
            return 0L;
        }
    }

    // =========================================================
    // HEADERS
    // =========================================================

    private static void addHeaders(
            HttpURLConnection connection,
            boolean write
    ) {

        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        /*
         * Supabase publishable key is sent here.
         *
         * User session token is intentionally not guessed
         * from another API. RLS/schema should decide access.
         */
        connection.setRequestProperty(
                "Authorization",
                "Bearer "
                        + SupabaseConfig.PUBLISHABLE_KEY
        );

        if (write) {
            connection.setRequestProperty(
                    "Prefer",
                    "return=minimal"
            );
        }
    }

    // =========================================================
    // HTTP HELPERS
    // =========================================================

    private static String readResponse(
            HttpURLConnection connection,
            int code
    ) throws Exception {

        InputStream stream;

        if (code >= 200 && code < 400) {
            stream =
                    connection.getInputStream();
        } else {
            stream =
                    connection.getErrorStream();
        }

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

    private static String cleanError(
            String response
    ) {

        if (response == null ||
                response.trim().isEmpty()) {

            return "Unknown server error";
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

            String error =
                    json.optString(
                            "error",
                            ""
                    );

            if (!error.isEmpty()) {
                return error;
            }

        } catch (Exception ignored) {
        }

        return response;
    }

    private static String safeMessage(
            Exception e
    ) {

        String message =
                e.getMessage();

        return message == null ||
                message.trim().isEmpty()
                ? e.getClass().getSimpleName()
                : message;
    }

    private static String encode(
            String value
    ) {

        try {

            return java.net.URLEncoder
                    .encode(
                            value,
                            StandardCharsets.UTF_8.name()
                    );

        } catch (Exception e) {

            return value;
        }
    }

    // =========================================================
    // MAIN THREAD CALLBACKS
    // =========================================================

    private static void postSuccess(
            @NonNull PostsCallback callback,
            List<CulturePost> posts
    ) {

        MAIN.post(() ->
                callback.onSuccess(posts)
        );
    }

    private static void postSuccess(
            @NonNull ActionCallback callback
    ) {

        MAIN.post(callback::onSuccess);
    }

    private static void postError(
            @NonNull PostsCallback callback,
            String message
    ) {

        MAIN.post(() ->
                callback.onError(message)
        );
    }

    private static void postError(
            @NonNull ActionCallback callback,
            String message
    ) {

        MAIN.post(() ->
                callback.onError(message)
        );
    }
}
