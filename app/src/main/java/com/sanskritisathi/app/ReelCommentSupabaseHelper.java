package com.sanskritisathi.app;

import android.content.Context;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public final class ReelCommentSupabaseHelper {

    public interface CommentsCallback {
        void onSuccess(List<ReelComment> comments);

        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();

        void onError(String message);
    }

    private ReelCommentSupabaseHelper() {
    }

    // =========================================================
    // ADD COMMENT
    // =========================================================

    public static void addComment(
            Context context,
            String reelId,
            String text,
            ActionCallback callback) {

        if (callback == null) {
            return;
        }

        if (context == null) {
            callback.onError("App context nahi mila.");
            return;
        }

        Context appContext =
                context.getApplicationContext();

        if (!SupabaseAuthManager.isLoggedIn(appContext)) {
            callback.onError("Please login first.");
            return;
        }

        if (reelId == null ||
                reelId.trim().isEmpty()) {

            callback.onError("Invalid Reel.");
            return;
        }

        if (text == null ||
                text.trim().isEmpty()) {

            callback.onError(
                    "Comment empty nahi ho sakta."
            );
            return;
        }

        final String cleanReelId =
                reelId.trim();

        final String cleanText =
                text.trim();

        if (cleanText.length() > 500) {

            callback.onError(
                    "Comment maximum 500 characters ka ho sakta hai."
            );

            return;
        }

        final String userId =
                SupabaseAuthManager.getUserId(
                        appContext
                );

        if (userId == null ||
                userId.trim().isEmpty()) {

            callback.onError(
                    "User session nahi mili."
            );

            return;
        }

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                String username =
                        getCurrentUsername(
                                appContext,
                                userId
                        );

                if (username == null ||
                        username.trim().isEmpty()) {

                    username = "Sanskriti User";
                }

                JSONObject comment =
                        new JSONObject();

                comment.put(
                        "id",
                        UUID.randomUUID().toString()
                );

                comment.put(
                        "reel_id",
                        cleanReelId
                );

                comment.put(
                        "user_id",
                        userId
                );

                comment.put(
                        "username",
                        username
                );

                comment.put(
                        "text",
                        cleanText
                );

                /*
                 * Supabase timestamptz ke liye ISO-8601
                 * UTC timestamp use kar rahe hain.
                 */
                comment.put(
                        "created_at",
                        getCurrentUtcTime()
                );

                URL url =
                        new URL(
                                SupabaseConfig.PROJECT_URL
                                        + "/rest/v1/reel_comments"
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("POST");

                connection.setConnectTimeout(30000);

                connection.setReadTimeout(30000);

                connection.setDoOutput(true);

                setHeaders(
                        connection,
                        appContext
                );

                connection.setRequestProperty(
                        "Prefer",
                        "return=minimal"
                );

                byte[] body =
                        comment.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                connection.setFixedLengthStreamingMode(
                        body.length
                );

                try (OutputStream output =
                             connection.getOutputStream()) {

                    output.write(body);
                    output.flush();
                }

                int responseCode =
                        connection.getResponseCode();

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    updateReelCommentCount(
                            appContext,
                            cleanReelId
                    );

                    postSuccess(callback);

                } else {

                    String error =
                            readError(connection);

                    postError(
                            callback,
                            "Comment save failed: "
                                    + error
                    );
                }

            } catch (Exception e) {

                postError(
                        callback,
                        "Comment save failed: "
                                + safeMessage(e)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    // =========================================================
    // LOAD COMMENTS
    // =========================================================

    public static void getComments(
            Context context,
            String reelId,
            CommentsCallback callback) {

        if (callback == null) {
            return;
        }

        if (context == null) {
            callback.onError("App context nahi mila.");
            return;
        }

        Context appContext =
                context.getApplicationContext();

        if (!SupabaseAuthManager.isLoggedIn(appContext)) {
            callback.onError("Please login first.");
            return;
        }

        if (reelId == null ||
                reelId.trim().isEmpty()) {

            callback.onError("Invalid Reel.");
            return;
        }

        final String cleanReelId =
                reelId.trim();

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                String encodedReelId =
                        URLEncoder.encode(
                                cleanReelId,
                                "UTF-8"
                        );

                String urlString =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/reel_comments"
                                + "?reel_id=eq."
                                + encodedReelId
                                + "&order=created_at.asc";

                URL url =
                        new URL(urlString);

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("GET");

                connection.setConnectTimeout(30000);

                connection.setReadTimeout(30000);

                setHeaders(
                        connection,
                        appContext
                );

                int responseCode =
                        connection.getResponseCode();

                String response =
                        readResponse(connection);

                if (responseCode < 200 ||
                        responseCode >= 300) {

                    postCommentsError(
                            callback,
                            "Comments load failed: "
                                    + response
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                List<ReelComment> comments =
                        new ArrayList<>();

                for (int i = 0;
                     i < array.length();
                     i++) {

                    JSONObject object =
                            array.getJSONObject(i);

                    String id =
                            object.optString(
                                    "id",
                                    ""
                            );

                    String returnedReelId =
                            object.optString(
                                    "reel_id",
                                    cleanReelId
                            );

                    String userId =
                            object.optString(
                                    "user_id",
                                    ""
                            );

                    String username =
                            object.optString(
                                    "username",
                                    "Sanskriti User"
                            );

                    String commentText =
                            object.optString(
                                    "text",
                                    ""
                            );

                    long createdAt =
                            parseCreatedAt(object);

                    comments.add(
                            new ReelComment(
                                    id,
                                    returnedReelId,
                                    userId,
                                    username,
                                    commentText,
                                    createdAt
                            )
                    );
                }

                postCommentsSuccess(
                        callback,
                        comments
                );

            } catch (Exception e) {

                postCommentsError(
                        callback,
                        "Comments load failed: "
                                + safeMessage(e)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    // =========================================================
    // DELETE COMMENT
    // =========================================================

    public static void deleteComment(
            Context context,
            String reelId,
            String commentId,
            ActionCallback callback) {

        if (callback == null) {
            return;
        }

        if (context == null) {
            callback.onError("App context nahi mila.");
            return;
        }

        Context appContext =
                context.getApplicationContext();

        if (!SupabaseAuthManager.isLoggedIn(appContext)) {
            callback.onError("Please login first.");
            return;
        }

        if (reelId == null ||
                reelId.trim().isEmpty()) {

            callback.onError("Invalid Reel.");
            return;
        }

        if (commentId == null ||
                commentId.trim().isEmpty()) {

            callback.onError("Invalid comment.");
            return;
        }

        final String cleanReelId =
                reelId.trim();

        final String cleanCommentId =
                commentId.trim();

        final String userId =
                SupabaseAuthManager.getUserId(
                        appContext
                );

        if (userId == null ||
                userId.trim().isEmpty()) {

            callback.onError(
                    "User session nahi mili."
            );

            return;
        }

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                String encodedCommentId =
                        URLEncoder.encode(
                                cleanCommentId,
                                "UTF-8"
                        );

                URL checkUrl =
                        new URL(
                                SupabaseConfig.PROJECT_URL
                                        + "/rest/v1/reel_comments"
                                        + "?id=eq."
                                        + encodedCommentId
                                        + "&select=id,user_id,reel_id"
                        );

                HttpURLConnection checkConnection =
                        (HttpURLConnection)
                                checkUrl.openConnection();

                checkConnection.setRequestMethod("GET");

                checkConnection.setConnectTimeout(30000);

                checkConnection.setReadTimeout(30000);

                setHeaders(
                        checkConnection,
                        appContext
                );

                int checkCode =
                        checkConnection.getResponseCode();

                String checkResponse =
                        readResponse(
                                checkConnection
                        );

                checkConnection.disconnect();

                if (checkCode < 200 ||
                        checkCode >= 300) {

                    postError(
                            callback,
                            "Comment verify failed: "
                                    + checkResponse
                    );

                    return;
                }

                JSONArray result =
                        new JSONArray(checkResponse);

                if (result.length() == 0) {

                    postError(
                            callback,
                            "Comment not found."
                    );

                    return;
                }

                JSONObject comment =
                        result.getJSONObject(0);

                String ownerId =
                        comment.optString(
                                "user_id",
                                ""
                        );

                String storedReelId =
                        comment.optString(
                                "reel_id",
                                ""
                        );

                if (!userId.equals(ownerId)) {

                    postError(
                            callback,
                            "Aap sirf apna comment delete kar sakte ho."
                    );

                    return;
                }

                if (!cleanReelId.equals(
                        storedReelId
                )) {

                    postError(
                            callback,
                            "Invalid Reel."
                    );

                    return;
                }

                URL deleteUrl =
                        new URL(
                                SupabaseConfig.PROJECT_URL
                                        + "/rest/v1/reel_comments"
                                        + "?id=eq."
                                        + encodedCommentId
                        );

                connection =
                        (HttpURLConnection)
                                deleteUrl.openConnection();

                connection.setRequestMethod("DELETE");

                connection.setConnectTimeout(30000);

                connection.setReadTimeout(30000);

                setHeaders(
                        connection,
                        appContext
                );

                connection.setRequestProperty(
                        "Prefer",
                        "return=minimal"
                );

                int deleteCode =
                        connection.getResponseCode();

                if (deleteCode >= 200 &&
                        deleteCode < 300) {

                    updateReelCommentCount(
                            appContext,
                            cleanReelId
                    );

                    postSuccess(callback);

                } else {

                    String error =
                            readError(connection);

                    postError(
                            callback,
                            "Comment delete failed: "
                                    + error
                    );
                }

            } catch (Exception e) {

                postError(
                        callback,
                        "Comment delete failed: "
                                + safeMessage(e)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    // =========================================================
    // UPDATE REEL COMMENT COUNT
    // =========================================================

    private static void updateReelCommentCount(
            Context context,
            String reelId) {

        HttpURLConnection connection = null;

        try {

            String encodedReelId =
                    URLEncoder.encode(
                            reelId,
                            "UTF-8"
                    );

            URL countUrl =
                    new URL(
                            SupabaseConfig.PROJECT_URL
                                    + "/rest/v1/reel_comments"
                                    + "?reel_id=eq."
                                    + encodedReelId
                                    + "&select=id"
                    );

            HttpURLConnection countConnection =
                    (HttpURLConnection)
                            countUrl.openConnection();

            countConnection.setRequestMethod("GET");

            countConnection.setConnectTimeout(30000);

            countConnection.setReadTimeout(30000);

            setHeaders(
                    countConnection,
                    context
            );

            int code =
                    countConnection.getResponseCode();

            String response =
                    readResponse(countConnection);

            countConnection.disconnect();

            if (code < 200 ||
                    code >= 300) {
                return;
            }

            JSONArray comments =
                    new JSONArray(response);

            int count =
                    comments.length();

            URL updateUrl =
                    new URL(
                            SupabaseConfig.PROJECT_URL
                                    + "/rest/v1/reels"
                                    + "?id=eq."
                                    + encodedReelId
                    );

            connection =
                    (HttpURLConnection)
                            updateUrl.openConnection();

            connection.setRequestMethod("PATCH");

            connection.setConnectTimeout(30000);

            connection.setReadTimeout(30000);

            connection.setDoOutput(true);

            setHeaders(
                    connection,
                    context
            );

            connection.setRequestProperty(
                    "Prefer",
                    "return=minimal"
            );

            JSONObject update =
                    new JSONObject();

            update.put(
                    "comments",
                    count
            );

            byte[] body =
                    update.toString()
                            .getBytes(
                                    StandardCharsets.UTF_8
                            );

            connection.setFixedLengthStreamingMode(
                    body.length
            );

            try (OutputStream output =
                         connection.getOutputStream()) {

                output.write(body);
                output.flush();
            }

            connection.getResponseCode();

        } catch (Exception ignored) {

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // =========================================================
    // GET CURRENT USERNAME
    // =========================================================

    private static String getCurrentUsername(
            Context context,
            String userId) {

        HttpURLConnection connection = null;

        try {

            String encodedUserId =
                    URLEncoder.encode(
                            userId,
                            "UTF-8"
                    );

            URL url =
                    new URL(
                            SupabaseConfig.PROJECT_URL
                                    + "/rest/v1/profiles"
                                    + "?id=eq."
                                    + encodedUserId
                                    + "&select=username"
                    );

            connection =
                    (HttpURLConnection)
                            url.openConnection();

            connection.setRequestMethod("GET");

            connection.setConnectTimeout(30000);

            connection.setReadTimeout(30000);

            setHeaders(
                    connection,
                    context
            );

            int code =
                    connection.getResponseCode();

            if (code < 200 ||
                    code >= 300) {

                return "Sanskriti User";
            }

            String response =
                    readResponse(connection);

            JSONArray array =
                    new JSONArray(response);

            if (array.length() == 0) {
                return "Sanskriti User";
            }

            String username =
                    array.getJSONObject(0)
                            .optString(
                                    "username",
                                    "Sanskriti User"
                            );

            if (username == null ||
                    username.trim().isEmpty()) {

                return "Sanskriti User";
            }

            return username.trim();

        } catch (Exception e) {

            return "Sanskriti User";

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // =========================================================
    // AUTH HEADERS
    // =========================================================

    private static void setHeaders(
            HttpURLConnection connection,
            Context context) {

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );

        String accessToken =
                SupabaseAuthManager.getAccessToken(
                        context
                );

        if (accessToken != null &&
                !accessToken.trim().isEmpty()) {

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + accessToken
            );
        }
    }

    // =========================================================
    // CURRENT UTC TIME
    // =========================================================

    private static String getCurrentUtcTime() {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        Locale.US
                );

        format.setTimeZone(
                TimeZone.getTimeZone("UTC")
        );

        return format.format(
                new Date()
        );
    }

    // =========================================================
    // PARSE CREATED AT
    // =========================================================

    private static long parseCreatedAt(
            JSONObject object) {

        try {

            Object value =
                    object.opt("created_at");

            if (value instanceof Number) {

                return ((Number) value).longValue();
            }

            if (value != null) {

                String text =
                        String.valueOf(value).trim();

                if (text.isEmpty()) {
                    return 0L;
                }

                try {

                    return Long.parseLong(text);

                } catch (Exception ignored) {
                }

                String normalized =
                        text.replace(
                                "Z",
                                "+0000"
                        );

                if (normalized.length() >= 6) {

                    int plusIndex =
                            normalized.lastIndexOf('+');

                    int minusIndex =
                            normalized.lastIndexOf('-');

                    int index =
                            Math.max(
                                    plusIndex,
                                    minusIndex
                            );

                    if (index > 10 &&
                            normalized.length()
                                    - index == 5) {

                        try {

                            SimpleDateFormat format =
                                    new SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                                            Locale.US
                                    );

                            return format.parse(
                                    normalized
                            ).getTime();

                        } catch (Exception ignored) {
                        }
                    }
                }

                try {

                    SimpleDateFormat format =
                            new SimpleDateFormat(
                                    "yyyy-MM-dd'T'HH:mm:ssZ",
                                    Locale.US
                            );

                    return format.parse(
                            normalized
                    ).getTime();

                } catch (Exception ignored) {
                }
            }

        } catch (Exception ignored) {
        }

        return 0L;
    }

    // =========================================================
    // RESPONSE HELPERS
    // =========================================================

    private static String readResponse(
            HttpURLConnection connection) {

        try {

            InputStream stream;

            if (connection.getResponseCode() >= 400) {

                stream =
                        connection.getErrorStream();

            } else {

                stream =
                        connection.getInputStream();
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

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }

            return result.toString();

        } catch (Exception e) {

            return "";
        }
    }

    private static String readError(
            HttpURLConnection connection) {

        String error =
                readResponse(connection);

        if (error == null ||
                error.trim().isEmpty()) {

            try {

                return "Server error "
                        + connection.getResponseCode();

            } catch (Exception ignored) {

                return "Server error";
            }
        }

        return error;
    }

    private static String safeMessage(
            Exception e) {

        if (e == null ||
                e.getMessage() == null ||
                e.getMessage().trim().isEmpty()) {

            return "Unknown error";
        }

        return e.getMessage();
    }

    // =========================================================
    // CALLBACK HELPERS
    // =========================================================

    private static void postSuccess(
            ActionCallback callback) {

        if (callback == null) {
            return;
        }

        new Handler(
                Looper.getMainLooper()
        ).post(
                callback::onSuccess
        );
    }

    private static void postError(
            ActionCallback callback,
            String message) {

        if (callback == null) {
            return;
        }

        new Handler(
                Looper.getMainLooper()
        ).post(
                () -> callback.onError(message)
        );
    }

    private static void postCommentsSuccess(
            CommentsCallback callback,
            List<ReelComment> comments) {

        if (callback == null) {
            return;
        }

        new Handler(
                Looper.getMainLooper()
        ).post(
                () -> callback.onSuccess(comments)
        );
    }

    private static void postCommentsError(
            CommentsCallback callback,
            String message) {

        if (callback == null) {
            return;
        }

        new Handler(
                Looper.getMainLooper()
        ).post(
                () -> callback.onError(message)
        );
    }
}
