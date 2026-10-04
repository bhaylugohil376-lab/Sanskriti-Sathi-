package com.sanskritisathi.app;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CulturePostSupabaseHelper {

    private static final String TABLE = "posts";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private static final Handler MAIN =
            new Handler(Looper.getMainLooper());

    private CulturePostSupabaseHelper() {
    }

    public interface PostsCallback {
        void onSuccess(List<CulturePost> posts);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface LikeCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface LikeStatusCallback {
        void onResult(boolean liked);
        void onError(String message);
    }

    // =========================================================
    // PUBLIC POSTS
    // =========================================================

    public static void getPublicPosts(
            @NonNull PostsCallback callback
    ) {
        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {
                String urlString =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + TABLE
                                + "?select=*"
                                + "&visibility=eq.Public"
                                + "&order=created_at.desc"
                                + "&limit=100";

                connection = openConnection(
                        urlString,
                        "GET"
                );

                int code = connection.getResponseCode();

                String response =
                        readResponse(connection, code);

                if (code < 200 || code >= 300) {
                    postError(
                            callback,
                            cleanError(response)
                    );
                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                List<CulturePost> posts =
                        new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {

                    JSONObject json =
                            array.getJSONObject(i);

                    CulturePost post =
                            jsonToCulturePost(json);

                    if (post != null) {
                        posts.add(post);
                    }
                }

                postSuccess(callback, posts);

            } catch (Exception e) {

                postError(
                        callback,
                        "Culture posts load error: "
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

    private static CulturePost jsonToCulturePost(
            JSONObject json
    ) {
        try {

            String id =
                    json.optString("id", "");

            String authorUid =
                    json.optString("author_uid", "");

            String author =
                    json.optString(
                            "author",
                            "Sanskriti User"
                    );

            String category =
                    json.optString("category", "");

            String caption =
                    json.optString("caption", "");

            String imageUrl =
                    json.optString("image_url", "");

            String visibility =
                    json.optString(
                            "visibility",
                            "Public"
                    );

            /*
             * Supabase created_at normally comes as:
             * 2026-09-30T10:20:30.000Z
             *
             * CulturePost currently stores createdAt as long,
             * so convert ISO timestamp to epoch millis.
             */
            long createdAt =
                    parseCreatedAt(
                            json.optString(
                                    "created_at",
                                    ""
                            )
                    );

            int likes =
                    Math.max(
                            0,
                            json.optInt(
                                    "likes",
                                    0
                            )
                    );

            int comments =
                    Math.max(
                            0,
                            json.optInt(
                                    "comments",
                                    0
                            )
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
                    likes,
                    comments,
                    R.drawable.icon_foreground,
                    R.drawable.icon_foreground,
                    false,
                    false
            );

        } catch (Exception e) {
            return null;
        }
    }

    // =========================================================
    // CREATED AT PARSER
    // =========================================================

    private static long parseCreatedAt(
            String value
    ) {

        if (TextUtils.isEmpty(value)) {
            return 0L;
        }

        try {

            if (android.os.Build.VERSION.SDK_INT >=
                    android.os.Build.VERSION_CODES.O) {

                return java.time.Instant
                        .parse(value)
                        .toEpochMilli();
            }

        } catch (Exception ignored) {
        }

        return 0L;
    }

    // =========================================================
    // CONNECTION
    // =========================================================

    private static HttpURLConnection openConnection(
            String urlString,
            String method
    ) throws Exception {

        URL url =
                new URL(urlString);

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

    // =========================================================
    // READ RESPONSE
    // =========================================================

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

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        stream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while (
                    (line = reader.readLine())
                            != null
            ) {
                result.append(line);
            }
        }

        return result.toString();
    }

    // =========================================================
    // ERROR
    // =========================================================

    private static String cleanError(
            String response
    ) {

        if (TextUtils.isEmpty(response)) {
            return "Supabase server error.";
        }

        try {

            JSONObject json =
                    new JSONObject(response);

            String message =
                    json.optString(
                            "message",
                            ""
                    );

            if (!TextUtils.isEmpty(message)) {
                return message;
            }

            String error =
                    json.optString(
                            "error",
                            ""
                    );

            if (!TextUtils.isEmpty(error)) {
                return error;
            }

            String hint =
                    json.optString(
                            "hint",
                            ""
                    );

            if (!TextUtils.isEmpty(hint)) {
                return hint;
            }

        } catch (Exception ignored) {
        }

        return response;
    }

    // =========================================================
    // SAFE MESSAGE
    // =========================================================

    private static String safeMessage(
            Exception e
    ) {

        return e.getMessage() == null
                ? e.getClass().getSimpleName()
                : e.getMessage();
    }

    // =========================================================
    // CALLBACK SUCCESS
    // =========================================================

    private static void postSuccess(
            @NonNull PostsCallback callback,
            List<CulturePost> posts
    ) {

        MAIN.post(
                () -> callback.onSuccess(posts)
        );
    }

    // =========================================================
    // CALLBACK ERROR
    // =========================================================

    private static void postError(
            @NonNull PostsCallback callback,
            String message
    ) {

        MAIN.post(
                () -> callback.onError(
                        TextUtils.isEmpty(message)
                                ? "Posts load failed."
                                : message
                )
        );
    }
}
