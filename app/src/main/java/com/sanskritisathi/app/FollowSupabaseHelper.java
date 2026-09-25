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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FollowSupabaseHelper {

    private static final String TABLE = "follows";

    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();

    private static final Handler MAIN_HANDLER =
            new Handler(Looper.getMainLooper());

    private FollowSupabaseHelper() {
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface StatusCallback {
        void onResult(boolean following);
        void onError(String message);
    }

    public interface CountCallback {
        void onResult(int count);
        void onError(String message);
    }

    // =========================================================
    // CHECK FOLLOWING
    // =========================================================

    public static void checkFollowing(
            Context context,
            String targetUid,
            StatusCallback callback) {

        if (context == null) {
            postStatusError(callback, "Context missing.");
            return;
        }

        if (TextUtils.isEmpty(targetUid)) {
            postStatusError(callback, "User profile nahi mili.");
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String currentUid =
                        SupabaseAuthManager.getUserId(context);

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(currentUid)
                        || TextUtils.isEmpty(token)) {

                    postStatusError(
                            callback,
                            "Pehle Login karein."
                    );
                    return;
                }

                if (currentUid.equals(targetUid)) {

                    postStatusResult(
                            callback,
                            false
                    );
                    return;
                }

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + TABLE
                                + "?select=id"
                                + "&follower_id=eq."
                                + encode(currentUid)
                                + "&following_id=eq."
                                + encode(targetUid)
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

                    postStatusError(
                            callback,
                            parseError(
                                    response,
                                    "Follow status check failed."
                            )
                    );
                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                postStatusResult(
                        callback,
                        array.length() > 0
                );

            } catch (Exception e) {

                postStatusError(
                        callback,
                        safeMessage(
                                e,
                                "Follow status check failed."
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
    // FOLLOW
    // =========================================================

    public static void followUser(
            Context context,
            String targetUid,
            ActionCallback callback) {

        if (context == null) {
            postActionError(callback, "Context missing.");
            return;
        }

        if (TextUtils.isEmpty(targetUid)) {
            postActionError(
                    callback,
                    "User profile nahi mili."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String currentUid =
                        SupabaseAuthManager.getUserId(context);

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(currentUid)
                        || TextUtils.isEmpty(token)) {

                    postActionError(
                            callback,
                            "Pehle Login karein."
                    );
                    return;
                }

                if (currentUid.equals(targetUid)) {

                    postActionError(
                            callback,
                            "Apne aap ko follow nahi kar sakte."
                    );
                    return;
                }

                if (isFollowingSync(
                        currentUid,
                        targetUid,
                        token)) {

                    postActionSuccess(callback);
                    return;
                }

                JSONObject body =
                        new JSONObject();

                body.put(
                        "id",
                        UUID.randomUUID().toString()
                );

                body.put(
                        "follower_id",
                        currentUid
                );

                body.put(
                        "following_id",
                        targetUid
                );

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + TABLE;

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
                        "return=minimal"
                );

                writeBody(
                        connection,
                        body.toString()
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
                            parseError(
                                    response,
                                    "Follow nahi hua."
                            )
                    );
                    return;
                }

                postActionSuccess(callback);

            } catch (Exception e) {

                postActionError(
                        callback,
                        safeMessage(
                                e,
                                "Follow nahi hua."
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
    // UNFOLLOW
    // =========================================================

    public static void unfollowUser(
            Context context,
            String targetUid,
            ActionCallback callback) {

        if (context == null) {
            postActionError(callback, "Context missing.");
            return;
        }

        if (TextUtils.isEmpty(targetUid)) {
            postActionError(
                    callback,
                    "User profile nahi mili."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String currentUid =
                        SupabaseAuthManager.getUserId(context);

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(currentUid)
                        || TextUtils.isEmpty(token)) {

                    postActionError(
                            callback,
                            "Pehle Login karein."
                    );
                    return;
                }

                if (currentUid.equals(targetUid)) {

                    postActionError(
                            callback,
                            "Invalid user."
                    );
                    return;
                }

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + TABLE
                                + "?follower_id=eq."
                                + encode(currentUid)
                                + "&following_id=eq."
                                + encode(targetUid);

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
                            parseError(
                                    response,
                                    "Unfollow nahi hua."
                            )
                    );
                    return;
                }

                postActionSuccess(callback);

            } catch (Exception e) {

                postActionError(
                        callback,
                        safeMessage(
                                e,
                                "Unfollow nahi hua."
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
    // FOLLOWERS COUNT
    // =========================================================

    public static void getFollowersCount(
            Context context,
            String userUid,
            CountCallback callback) {

        getCount(
                context,
                "following_id",
                userUid,
                callback
        );
    }

    // =========================================================
    // FOLLOWING COUNT
    // =========================================================

    public static void getFollowingCount(
            Context context,
            String userUid,
            CountCallback callback) {

        getCount(
                context,
                "follower_id",
                userUid,
                callback
        );
    }

    private static void getCount(
            Context context,
            String field,
            String userUid,
            CountCallback callback) {

        if (context == null
                || TextUtils.isEmpty(userUid)) {

            postCountError(
                    callback,
                    "Invalid user."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(token)) {

                    postCountError(
                            callback,
                            "Pehle Login karein."
                    );
                    return;
                }

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + TABLE
                                + "?select=id"
                                + "&"
                                + field
                                + "=eq."
                                + encode(userUid);

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

                    postCountError(
                            callback,
                            parseError(
                                    response,
                                    "Count load failed."
                            )
                    );
                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                postCountResult(
                        callback,
                        array.length()
                );

            } catch (Exception e) {

                postCountError(
                        callback,
                        safeMessage(
                                e,
                                "Count load failed."
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
    // INTERNAL CHECK
    // =========================================================

    private static boolean isFollowingSync(
            String followerUid,
            String followingUid,
            String token)
            throws Exception {

        String url =
                SupabaseConfig.PROJECT_URL
                        + "/rest/v1/"
                        + TABLE
                        + "?select=id"
                        + "&follower_id=eq."
                        + encode(followerUid)
                        + "&following_id=eq."
                        + encode(followingUid)
                        + "&limit=1";

        HttpURLConnection connection =
                null;

        try {

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
                return false;
            }

            JSONArray array =
                    new JSONArray(response);

            return array.length() > 0;

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

        connection.setRequestMethod(method);

        connection.setConnectTimeout(30000);

        connection.setReadTimeout(60000);

        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );

        connection.setRequestProperty(
                "Authorization",
                "Bearer " + token
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        return connection;
    }

    // =========================================================
    // BODY
    // =========================================================

    private static void writeBody(
            HttpURLConnection connection,
            String body)
            throws Exception {

        OutputStream output =
                connection.getOutputStream();

        output.write(
                body.getBytes(
                        StandardCharsets.UTF_8
                )
        );

        output.flush();
        output.close();
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
            input = connection.getErrorStream();
        } else {
            input = connection.getInputStream();
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

        StringBuilder result =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        reader.close();

        return result.toString();
    }

    // =========================================================
    // ERROR PARSER
    // =========================================================

    private static String parseError(
            String response,
            String fallback) {

        if (TextUtils.isEmpty(response)) {
            return fallback;
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

            message =
                    json.optString(
                            "error",
                            ""
                    );

            if (!TextUtils.isEmpty(message)) {
                return message;
            }

            message =
                    json.optString(
                            "details",
                            ""
                    );

            if (!TextUtils.isEmpty(message)) {
                return message;
            }

        } catch (Exception ignored) {
        }

        return response;
    }

    private static String safeMessage(
            Exception e,
            String fallback) {

        if (e == null) {
            return fallback;
        }

        String message =
                e.getMessage();

        return TextUtils.isEmpty(message)
                ? fallback
                : message;
    }

    // =========================================================
    // ENCODE
    // =========================================================

    private static String encode(
            String value)
            throws Exception {

        return URLEncoder.encode(
                value,
                "UTF-8"
        );
    }

    // =========================================================
    // CALLBACKS
    // =========================================================

    private static void postActionSuccess(
            ActionCallback callback) {

        if (callback == null) return;

        MAIN_HANDLER.post(
                callback::onSuccess
        );
    }

    private static void postActionError(
            ActionCallback callback,
            String message) {

        if (callback == null) return;

        MAIN_HANDLER.post(() ->
                callback.onError(message)
        );
    }

    private static void postStatusResult(
            StatusCallback callback,
            boolean result) {

        if (callback == null) return;

        MAIN_HANDLER.post(() ->
                callback.onResult(result)
        );
    }

    private static void postStatusError(
            StatusCallback callback,
            String message) {

        if (callback == null) return;

        MAIN_HANDLER.post(() ->
                callback.onError(message)
        );
    }

    private static void postCountResult(
            CountCallback callback,
            int count) {

        if (callback == null) return;

        MAIN_HANDLER.post(() ->
                callback.onResult(count)
        );
    }

    private static void postCountError(
            CountCallback callback,
            String message) {

        if (callback == null) return;

        MAIN_HANDLER.post(() ->
                callback.onError(message)
        );
    }
}
