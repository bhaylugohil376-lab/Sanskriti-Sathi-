package com.sanskritisathi.app;

import android.content.Context;
import android.text.TextUtils;
import android.os.Handler;
import android.os.Looper;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ReelCommentSupabaseHelper {

    private static final String COMMENTS_TABLE = "reel_comments";

    private static final Handler MAIN_HANDLER =
            new Handler(Looper.getMainLooper());

    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();

    private ReelCommentSupabaseHelper() {
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
    // GET COMMENTS
    // =========================================================

    public static void getComments(
            Context context,
            String reelId,
            CommentsCallback callback) {

        if (context == null) {
            postCommentsError(callback, "Context missing.");
            return;
        }

        if (TextUtils.isEmpty(reelId)) {
            postCommentsError(callback, "Invalid Reel.");
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(token)) {
                    postCommentsError(
                            callback,
                            "Login session nahi mili."
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

                    String id =
                            json.optString(
                                    "id",
                                    ""
                            );

                    String commentReelId =
                            json.optString(
                                    "reel_id",
                                    reelId
                            );

                    String userId =
                            json.optString(
                                    "user_id",
                                    ""
                            );

                    String username =
                            json.optString(
                                    "username",
                                    ""
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

                    long createdAt =
                            parseCreatedAt(
                                    createdAtText
                            );

                    if (TextUtils.isEmpty(username)) {
                        username = "Sanskriti User";
                    }

                    ReelComment comment =
                            new ReelComment(
                                    id,
                                    commentReelId,
                                    userId,
                                    username,
                                    text,
                                    createdAt
                            );

                    result.add(comment);
                }

                final List<ReelComment> finalResult =
                        result;

                MAIN_HANDLER.post(() ->
                        callback.onSuccess(
                                finalResult
                        )
                );

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

        if (TextUtils.isEmpty(text)
                || text.trim().isEmpty()) {

            postActionError(
                    callback,
                    "Comment empty nahi ho sakta."
            );
            return;
        }

        final String cleanText =
                text.trim();

        if (cleanText.length() > 500) {

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
                        SupabaseAuthManager.getUserId(
                                context
                        );

                String token =
                        SupabaseAuthManager.getAccessToken(
                                context
                        );

                if (TextUtils.isEmpty(userId)
                        || TextUtils.isEmpty(token)) {

                    postActionError(
                            callback,
                            "Please login first."
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

                JSONObject json =
                        new JSONObject();

                json.put(
                        "id",
                        UUID.randomUUID().toString()
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
                        cleanText
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

                connection.setDoOutput(
                        true
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                connection.setRequestProperty(
                        "Prefer",
                        "return=minimal"
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

                updateCommentCount(
                        reelId,
                        token
                );

                postActionSuccess(
                        callback
                );

            } catch (Exception e) {

                postActionError(
                        callback,
                        safeMessage(
                                e,
                                "Comment add failed."
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

        if (context == null
                || TextUtils.isEmpty(commentId)) {

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
                        SupabaseAuthManager.getUserId(
                                context
                        );

                String token =
                        SupabaseAuthManager.getAccessToken(
                                context
                        );

                if (TextUtils.isEmpty(userId)
                        || TextUtils.isEmpty(token)) {

                    postActionError(
                            callback,
                            "Please login first."
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

                postActionSuccess(
                        callback
                );

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
    // UPDATE COMMENT COUNT
    // =========================================================

    private static void updateCommentCount(
            String reelId,
            String token) {

        HttpURLConnection countConnection = null;
        HttpURLConnection patchConnection = null;

        try {

            String countUrl =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/"
                            + COMMENTS_TABLE
                            + "?select=id"
                            + "&reel_id=eq."
                            + URLEncoder.encode(
                                    reelId,
                                    "UTF-8"
                            );

            countConnection =
                    openConnection(
                            countUrl,
                            "GET",
                            token
                    );

            int countCode =
                    countConnection.getResponseCode();

            String countResponse =
                    readResponse(
                            countConnection,
                            countCode
                    );

            if (countCode < 200
                    || countCode >= 300) {
                return;
            }

            JSONArray comments =
                    new JSONArray(
                            countResponse
                    );

            JSONObject update =
                    new JSONObject();

            update.put(
                    "comments",
                    comments.length()
            );

            String patchUrl =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/reels"
                            + "?id=eq."
                            + URLEncoder.encode(
                                    reelId,
                                    "UTF-8"
                            );

            patchConnection =
                    openConnection(
                            patchUrl,
                            "PATCH",
                            token
                    );

            patchConnection.setDoOutput(
                    true
            );

            patchConnection.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );

            OutputStream output =
                    patchConnection.getOutputStream();

            output.write(
                    update.toString()
                            .getBytes(
                                    StandardCharsets.UTF_8
                            )
            );

            output.flush();
            output.close();

            patchConnection.getResponseCode();

        } catch (Exception ignored) {

        } finally {

            if (countConnection != null) {
                countConnection.disconnect();
            }

            if (patchConnection != null) {
                patchConnection.disconnect();
            }
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

    private static HttpURLConnection openConnection(
            String urlString,
            String method,
            String accessToken)
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

        if (!TextUtils.isEmpty(accessToken)) {

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + accessToken
            );
        }

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        return connection;
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    private static String readResponse(
            HttpURLConnection connection,
            int responseCode)
            throws Exception {

        InputStream input;

        if (responseCode >= 400) {

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
    // CREATED AT
    // =========================================================

    private static long parseCreatedAt(
            String value) {

        if (TextUtils.isEmpty(value)) {
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
    // CALLBACK HELPERS
    // =========================================================

    private static void postCommentsError(
            CommentsCallback callback,
            String message) {

        if (callback == null) {
            return;
        }

        MAIN_HANDLER.post(() ->
                callback.onError(
                        message
                )
        );
    }

    private static void postActionSuccess(
            ActionCallback callback) {

        if (callback == null) {
            return;
        }

        MAIN_HANDLER.post(
                callback::onSuccess
        );
    }

    private static void postActionError(
            ActionCallback callback,
            String message) {

        if (callback == null) {
            return;
        }

        MAIN_HANDLER.post(() ->
                callback.onError(
                        message
                )
        );
    }

    private static String safeMessage(
            Exception exception,
            String fallback) {

        if (exception == null) {
            return fallback;
        }

        String message =
                exception.getMessage();

        if (TextUtils.isEmpty(message)) {
            return fallback;
        }

        return message;
    }
}
