package com.sanskritisathi.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

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

public final class ReelCommentSupabaseHelper {

    private static final Handler MAIN_HANDLER =
            new Handler(Looper.getMainLooper());

    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();

    private static final String COMMENTS_TABLE =
            "reel_comments";

    private ReelCommentSupabaseHelper() {
        // No instance
    }

    // =========================================================
    // CALLBACKS
    // =========================================================

    public interface CommentsCallback {

        void onSuccess(List<ReelComment> comments);

        void onError(String message);
    }

    public interface ActionCallback {

        void onSuccess();

        void onError(String message);
    }

    // =========================================================
    // LOAD COMMENTS
    // =========================================================

    public static void getComments(
            Context context,
            String reelId,
            CommentsCallback callback) {

        if (context == null) {
            postCommentsError(
                    callback,
                    "Context missing."
            );
            return;
        }

        if (TextUtils.isEmpty(reelId)) {
            postCommentsError(
                    callback,
                    "Invalid Reel."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String token =
                        SupabaseAuthManager
                                .getAccessToken(context);

                if (TextUtils.isEmpty(token)) {

                    postCommentsError(
                            callback,
                            "Please login first."
                    );

                    return;
                }

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + COMMENTS_TABLE
                                + "?select=*"
                                + "&reel_id=eq."
                                + URLEncoder.encode(
                                        reelId,
                                        "UTF-8"
                                )
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

                    postCommentsError(
                            callback,
                            "Comments load failed: "
                                    + response
                    );

                    return;
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

                final List<ReelComment> finalResult =
                        result;

                MAIN_HANDLER.post(() -> {

                    if (callback != null) {
                        callback.onSuccess(
                                finalResult
                        );
                    }
                });

            } catch (Exception e) {

                postCommentsError(
                        callback,
                        safeMessage(
                                e,
                                "Comments load nahi hui."
                        )
                );

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

    public static void addComment(
            Context context,
            String reelId,
            String text,
            ActionCallback callback) {

        if (context == null) {
            postActionError(
                    callback,
                    "Context missing."
            );
            return;
        }

        if (TextUtils.isEmpty(reelId)) {
            postActionError(
                    callback,
                    "Invalid Reel."
            );
            return;
        }

        if (TextUtils.isEmpty(text)) {
            postActionError(
                    callback,
                    "Comment empty hai."
            );
            return;
        }

        final String finalText =
                text.trim();

        if (finalText.length() > 500) {
            postActionError(
                    callback,
                    "Comment maximum 500 characters ka ho sakta hai."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String userId =
                        SupabaseAuthManager
                                .getUserId(context);

                String token =
                        SupabaseAuthManager
                                .getAccessToken(context);

                if (TextUtils.isEmpty(userId)
                        || TextUtils.isEmpty(token)) {

                    postActionError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
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
                        UUID.randomUUID()
                                .toString();

                JSONObject json =
                        new JSONObject();

                json.put(
                        "id",
                        commentId
                );

                json.put(
                        "reel_id",
                        reelId
                );

                json.put(
                        "user_id",
                        userId
                );

                json.put(
                        "username",
                        username
                );

                json.put(
                        "text",
                        finalText
                );

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

                OutputStream output =
                        connection.getOutputStream();

                output.write(
                        json.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );

                output.flush();
                output.close();

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code < 200 || code >= 300) {

                    postActionError(
                            callback,
                            "Comment add failed: "
                                    + response
                    );

                    return;
                }

                postActionSuccess(callback);

            } catch (Exception e) {

                postActionError(
                        callback,
                        safeMessage(
                                e,
                                "Comment add nahi hui."
                        )
                );

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

    public static void deleteComment(
            Context context,
            String commentId,
            ActionCallback callback) {

        if (context == null) {
            postActionError(
                    callback,
                    "Context missing."
            );
            return;
        }

        if (TextUtils.isEmpty(commentId)) {
            postActionError(
                    callback,
                    "Invalid comment."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String userId =
                        SupabaseAuthManager
                                .getUserId(context);

                String token =
                        SupabaseAuthManager
                                .getAccessToken(context);

                if (TextUtils.isEmpty(userId)
                        || TextUtils.isEmpty(token)) {

                    postActionError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
                }

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + COMMENTS_TABLE
                                + "?id=eq."
                                + URLEncoder.encode(
                                        commentId,
                                        "UTF-8"
                                )
                                + "&user_id=eq."
                                + URLEncoder.encode(
                                        userId,
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

                    postActionError(
                            callback,
                            "Comment delete failed: "
                                    + response
                    );

                    return;
                }

                postActionSuccess(callback);

            } catch (Exception e) {

                postActionError(
                        callback,
                        safeMessage(
                                e,
                                "Comment delete failed."
                        )
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

    private static ReelComment parseComment(
            JSONObject json) {

        try {

            String id =
                    json.optString(
                            "id",
                            ""
                    );

            String reelId =
                    json.optString(
                            "reel_id",
                            ""
                    );

            String userId =
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

            String createdAtText =
                    json.optString(
                            "created_at",
                            ""
                    );

            /*
             * IMPORTANT:
             *
             * ReelComment.createdAt is long.
             * Supabase created_at is normally ISO-8601 text.
             *
             * Convert String -> long before constructor.
             */

            long createdAt =
                    parseCreatedAt(
                            createdAtText
                    );

            return new ReelComment(
                    id,
                    reelId,
                    userId,
                    username,
                    text,
                    createdAt
            );

        } catch (Exception e) {

            return null;
        }
    }

    // =========================================================
    // CREATED AT
    // =========================================================

    private static long parseCreatedAt(
            String value) {

        if (TextUtils.isEmpty(value)) {
            return System.currentTimeMillis();
        }

        try {

            return Instant
                    .parse(value)
                    .toEpochMilli();

        } catch (Exception ignored) {

            return System.currentTimeMillis();
        }
    }

    // =========================================================
    // GET USERNAME
    // =========================================================

    private static String getUsername(
            String userId,
            String token) {

        if (TextUtils.isEmpty(userId)
                || TextUtils.isEmpty(token)) {

            return "";
        }

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

            return array
                    .getJSONObject(0)
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

    private static HttpURLConnection openConnection(
            String urlString,
            String method,
            String token)
            throws Exception {

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod(
                method
        );

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

        if (!TextUtils.isEmpty(token)) {

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token
            );
        }

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        return connection;
    }

    // =========================================================
    // READ RESPONSE
    // =========================================================

    private static String readResponse(
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

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                input,
                                StandardCharsets.UTF_8
                        )
                );

        StringBuilder builder =
                new StringBuilder();

        String line;

        while ((line =
                reader.readLine()) != null) {

            builder.append(line);
        }

        reader.close();

        return builder.toString();
    }

    // =========================================================
    // CALLBACK HELPERS
    // =========================================================

    private static void postCommentsError(
            CommentsCallback callback,
            String message) {

        MAIN_HANDLER.post(() -> {

            if (callback != null) {
                callback.onError(
                        message == null
                                ? "Comments error."
                                : message
                );
            }
        });
    }

    private static void postActionSuccess(
            ActionCallback callback) {

        MAIN_HANDLER.post(() -> {

            if (callback != null) {
                callback.onSuccess();
            }
        });
    }

    private static void postActionError(
            ActionCallback callback,
            String message) {

        MAIN_HANDLER.post(() -> {

            if (callback != null) {
                callback.onError(
                        message == null
                                ? "Comment action failed."
                                : message
                );
            }
        });
    }

    // =========================================================
    // ERROR
    // =========================================================

    private static String safeMessage(
            Exception e,
            String fallback) {

        if (e == null
                || TextUtils.isEmpty(
                        e.getMessage()
                )) {

            return fallback;
        }

        return e.getMessage();
    }
}
