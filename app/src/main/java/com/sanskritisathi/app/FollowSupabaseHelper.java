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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FollowSupabaseHelper {

    private FollowSupabaseHelper() {
    }

    private static final String FOLLOW_TABLE = "follows";
    private static final String USERS_TABLE = "users";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private static final Handler MAIN =
            new Handler(Looper.getMainLooper());

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface StatusCallback {
        void onResult(boolean following);
        void onError(String message);
    }

    // =========================================================
    // CHECK FOLLOWING
    // =========================================================

    public static void checkFollowing(
            Context context,
            String targetUid,
            @NonNull StatusCallback callback
    ) {

        String currentUid =
                SupabaseAuthManager.getUserId(context);

        if (currentUid == null ||
                currentUid.trim().isEmpty()) {

            callback.onError("Pehle Login karein.");
            return;
        }

        if (targetUid == null ||
                targetUid.trim().isEmpty()) {

            callback.onError("User profile nahi mili.");
            return;
        }

        if (currentUid.equals(targetUid)) {
            callback.onResult(false);
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String query =
                        "follower_id=eq."
                                + encode(currentUid)
                                + "&following_id=eq."
                                + encode(targetUid)
                                + "&select=id&limit=1";

                URL url = new URL(
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + FOLLOW_TABLE
                                + "?"
                                + query
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

                    postStatusError(
                            callback,
                            "Follow status check nahi hua."
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
                        "Follow status check nahi hua."
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
            @NonNull ActionCallback callback
    ) {

        String currentUid =
                SupabaseAuthManager.getUserId(context);

        if (currentUid == null ||
                currentUid.trim().isEmpty()) {

            callback.onError("Pehle Login karein.");
            return;
        }

        if (targetUid == null ||
                targetUid.trim().isEmpty()) {

            callback.onError("User profile nahi mili.");
            return;
        }

        if (currentUid.equals(targetUid)) {

            callback.onError(
                    "Apne aap ko follow nahi kar sakte."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            try {

                boolean alreadyFollowing =
                        checkFollowingSync(
                                currentUid,
                                targetUid
                        );

                if (alreadyFollowing) {
                    postSuccess(callback);
                    return;
                }

                boolean targetExists =
                        userExistsSync(targetUid);

                if (!targetExists) {

                    postError(
                            callback,
                            "User profile available nahi hai."
                    );
                    return;
                }

                JSONObject body =
                        new JSONObject();

                body.put(
                        "follower_id",
                        currentUid
                );

                body.put(
                        "following_id",
                        targetUid
                );

                body.put(
                        "created_at",
                        java.time.Instant
                                .now()
                                .toString()
                );

                int code =
                        postJson(
                                FOLLOW_TABLE,
                                body
                        );

                if (code >= 200 && code < 300) {

                    updateUserCount(
                            currentUid,
                            "following",
                            1
                    );

                    updateUserCount(
                            targetUid,
                            "followers",
                            1
                    );

                    postSuccess(callback);

                } else {

                    postError(
                            callback,
                            "Follow nahi hua."
                    );
                }

            } catch (Exception e) {

                postError(
                        callback,
                        "Follow nahi hua: "
                                + safeMessage(e)
                );
            }
        });
    }

    // =========================================================
    // UNFOLLOW
    // =========================================================

    public static void unfollowUser(
            Context context,
            String targetUid,
            @NonNull ActionCallback callback
    ) {

        String currentUid =
                SupabaseAuthManager.getUserId(context);

        if (currentUid == null ||
                currentUid.trim().isEmpty()) {

            callback.onError("Pehle Login karein.");
            return;
        }

        if (targetUid == null ||
                targetUid.trim().isEmpty()) {

            callback.onError("User profile nahi mili.");
            return;
        }

        if (currentUid.equals(targetUid)) {

            callback.onError("Invalid user.");
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String query =
                        "follower_id=eq."
                                + encode(currentUid)
                                + "&following_id=eq."
                                + encode(targetUid);

                URL url =
                        new URL(
                                SupabaseConfig.PROJECT_URL
                                        + "/rest/v1/"
                                        + FOLLOW_TABLE
                                        + "?"
                                        + query
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("DELETE");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

                addHeaders(connection);

                int code =
                        connection.getResponseCode();

                if (code >= 200 && code < 300) {

                    updateUserCount(
                            currentUid,
                            "following",
                            -1
                    );

                    updateUserCount(
                            targetUid,
                            "followers",
                            -1
                    );

                    postSuccess(callback);

                } else {

                    postError(
                            callback,
                            "Unfollow nahi hua."
                    );
                }

            } catch (Exception e) {

                postError(
                        callback,
                        "Unfollow nahi hua."
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // =========================================================
    // SYNC FOLLOW CHECK
    // =========================================================

    private static boolean checkFollowingSync(
            String followerUid,
            String followingUid
    ) throws Exception {

        String query =
                "follower_id=eq."
                        + encode(followerUid)
                        + "&following_id=eq."
                        + encode(followingUid)
                        + "&select=id&limit=1";

        HttpURLConnection connection =
                openGet(
                        FOLLOW_TABLE,
                        query
                );

        try {

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

            connection.disconnect();
        }
    }

    // =========================================================
    // USER EXISTS
    // =========================================================

    private static boolean userExistsSync(
            String uid
    ) throws Exception {

        String query =
                "id=eq."
                        + encode(uid)
                        + "&select=id&limit=1";

        HttpURLConnection connection =
                openGet(
                        USERS_TABLE,
                        query
                );

        try {

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

            connection.disconnect();
        }
    }

    // =========================================================
    // UPDATE USER COUNTER
    // =========================================================

    private static void updateUserCount(
            String uid,
            String field,
            int difference
    ) {

        try {

            int current =
                    getUserCount(
                            uid,
                            field
                    );

            int updated =
                    Math.max(
                            0,
                            current + difference
                    );

            JSONObject body =
                    new JSONObject();

            body.put(
                    field,
                    updated
            );

            patchUser(
                    uid,
                    body
            );

        } catch (Exception ignored) {
        }
    }

    private static int getUserCount(
            String uid,
            String field
    ) throws Exception {

        String query =
                "id=eq."
                        + encode(uid)
                        + "&select="
                        + field
                        + "&limit=1";

        HttpURLConnection connection =
                openGet(
                        USERS_TABLE,
                        query
                );

        try {

            int code =
                    connection.getResponseCode();

            String response =
                    readResponse(
                            connection,
                            code
                    );

            if (code < 200 || code >= 300) {
                return 0;
            }

            JSONArray array =
                    new JSONArray(response);

            if (array.length() == 0) {
                return 0;
            }

            JSONObject user =
                    array.getJSONObject(0);

            return user.optInt(
                    field,
                    0
            );

        } finally {

            connection.disconnect();
        }
    }

    private static void patchUser(
            String uid,
            JSONObject body
    ) throws Exception {

        URL url =
                new URL(
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + USERS_TABLE
                                + "?id=eq."
                                + encode(uid)
                );

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        try {

            connection.setRequestMethod("PATCH");
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

            try (OutputStream output =
                         connection.getOutputStream()) {

                output.write(data);
            }

            connection.getResponseCode();

        } finally {

            connection.disconnect();
        }
    }

    // =========================================================
    // POST JSON
    // =========================================================

    private static int postJson(
            String table,
            JSONObject body
    ) throws Exception {

        URL url =
                new URL(
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + table
                );

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        try {

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

            try (OutputStream output =
                         connection.getOutputStream()) {

                output.write(data);
            }

            return connection.getResponseCode();

        } finally {

            connection.disconnect();
        }
    }

    // =========================================================
    // GET
    // =========================================================

    private static HttpURLConnection openGet(
            String table,
            String query
    ) throws Exception {

        URL url =
                new URL(
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + table
                                + "?"
                                + query
                );

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);

        addHeaders(connection);

        return connection;
    }

    // =========================================================
    // HEADERS
    // =========================================================

    private static void addHeaders(
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
                "Content-Type",
                "application/json"
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );
    }

    // =========================================================
    // RESPONSE
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

        StringBuilder builder =
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

                builder.append(line);
            }
        }

        return builder.toString();
    }

    // =========================================================
    // HELPERS
    // =========================================================

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

    private static String safeMessage(
            Exception e
    ) {

        return e.getMessage() == null
                ? e.getClass().getSimpleName()
                : e.getMessage();
    }

    private static void postSuccess(
            ActionCallback callback
    ) {

        MAIN.post(
                callback::onSuccess
        );
    }

    private static void postError(
            ActionCallback callback,
            String message
    ) {

        MAIN.post(
                () -> callback.onError(message)
        );
    }

    private static void postStatusResult(
            StatusCallback callback,
            boolean following
    ) {

        MAIN.post(
                () -> callback.onResult(following)
        );
    }

    private static void postStatusError(
            StatusCallback callback,
            String message
    ) {

        MAIN.post(
                () -> callback.onError(message)
        );
    }
}
