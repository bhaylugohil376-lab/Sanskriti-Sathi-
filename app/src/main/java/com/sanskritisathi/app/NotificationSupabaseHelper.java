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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class NotificationSupabaseHelper {

    private NotificationSupabaseHelper() {
    }

    private static final String TABLE = "notifications";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private static final Handler MAIN =
            new Handler(Looper.getMainLooper());

    public interface NotificationCallback {
        void onSuccess(List<NotificationModel> notifications);

        void onError(String message);
    }

    // =========================================================
    // CREATE NOTIFICATION
    // =========================================================

    public static void createNotification(
            Context context,
            String targetUid,
            String title,
            String message,
            String type,
            String referenceId
    ) {

        if (targetUid == null ||
                targetUid.trim().isEmpty()) {
            return;
        }

        String actorUid =
                SupabaseAuthManager.getUserId(context);

        if (actorUid != null &&
                targetUid.equals(actorUid)) {
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                JSONObject body =
                        new JSONObject();

                body.put(
                        "user_id",
                        targetUid
                );

                body.put(
                        "title",
                        title == null ||
                                title.trim().isEmpty()
                                ? "New Notification"
                                : title
                );

                body.put(
                        "message",
                        message == null
                                ? ""
                                : message
                );

                body.put(
                        "type",
                        type == null
                                ? "activity"
                                : type
                );

                body.put(
                        "reference_id",
                        referenceId == null
                                ? ""
                                : referenceId
                );

                body.put(
                        "actor_uid",
                        actorUid == null
                                ? ""
                                : actorUid
                );

                body.put(
                        "read",
                        false
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

                connection.getResponseCode();

            } catch (Exception ignored) {

                // Notification failure should not
                // break the main action.

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // =========================================================
    // COMPATIBILITY METHOD
    // =========================================================

    public static void create(
            Context context,
            String targetUid,
            String type,
            String text,
            String referenceId
    ) {

        createNotification(
                context,
                targetUid,
                getTitleFromType(type),
                text,
                type,
                referenceId
        );
    }

    // =========================================================
    // GET NOTIFICATIONS
    // =========================================================

    public static void getNotifications(
            Context context,
            String uid,
            @NonNull NotificationCallback callback
    ) {

        if (uid == null ||
                uid.trim().isEmpty()) {

            callback.onError(
                    "User ID missing."
            );

            return;
        }

        String loggedInUid =
                SupabaseAuthManager.getUserId(context);

        if (loggedInUid == null ||
                !loggedInUid.equals(uid)) {

            callback.onError(
                    "Pehle login karein."
            );

            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String query =
                        "user_id=eq."
                                + encode(uid)
                                + "&select=*"
                                + "&order=created_at.desc"
                                + "&limit=100";

                connection =
                        openConnection(
                                TABLE + "?" + query,
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

                    postError(
                            callback,
                            "Notifications load nahi hui: "
                                    + cleanError(response)
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                List<NotificationModel> result =
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

                    String title =
                            json.optString(
                                    "title",
                                    "Notification"
                            );

                    String message =
                            json.optString(
                                    "message",
                                    ""
                            );

                    boolean read =
                            json.optBoolean(
                                    "read",
                                    false
                            );

                    String createdAt =
                            json.optString(
                                    "created_at",
                                    ""
                            );

                    String time =
                            formatTime(
                                    createdAt
                            );

                    result.add(
                            new NotificationModel(
                                    id,
                                    title,
                                    message,
                                    time,
                                    read
                            )
                    );
                }

                postSuccess(
                        callback,
                        result
                );

            } catch (Exception e) {

                postError(
                        callback,
                        "Notifications load nahi hui: "
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
    // MARK AS READ
    // =========================================================

    public static void markAsRead(
            Context context,
            String uid,
            String notificationId
    ) {

        if (!isValid(uid, notificationId)) {
            return;
        }

        String loggedInUid =
                SupabaseAuthManager.getUserId(context);

        if (loggedInUid == null ||
                !loggedInUid.equals(uid)) {
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String query =
                        "?id=eq."
                                + encode(notificationId)
                                + "&user_id=eq."
                                + encode(uid);

                connection =
                        openConnection(
                                TABLE + query,
                                "PATCH"
                        );

                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Prefer",
                        "return=minimal"
                );

                JSONObject body =
                        new JSONObject();

                body.put(
                        "read",
                        true
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
        });
    }

    // =========================================================
    // MARK ALL AS READ
    // =========================================================

    public static void markAllAsRead(
            Context context,
            String uid
    ) {

        if (uid == null ||
                uid.trim().isEmpty()) {
            return;
        }

        String loggedInUid =
                SupabaseAuthManager.getUserId(context);

        if (loggedInUid == null ||
                !loggedInUid.equals(uid)) {
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String query =
                        "?user_id=eq."
                                + encode(uid)
                                + "&read=eq.false";

                connection =
                        openConnection(
                                TABLE + query,
                                "PATCH"
                        );

                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Prefer",
                        "return=minimal"
                );

                JSONObject body =
                        new JSONObject();

                body.put(
                        "read",
                        true
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
        });
    }

    // =========================================================
    // TIME
    // =========================================================

    private static String formatTime(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "Just now";
        }

        try {

            long timeMillis =
                    java.time.Instant
                            .parse(value)
                            .toEpochMilli();

            long difference =
                    System.currentTimeMillis()
                            - timeMillis;

            long seconds =
                    Math.max(
                            0,
                            difference / 1000
                    );

            if (seconds < 60) {
                return "Just now";
            }

            long minutes =
                    seconds / 60;

            if (minutes < 60) {
                return minutes
                        + (minutes == 1
                        ? " minute ago"
                        : " minutes ago");
            }

            long hours =
                    minutes / 60;

            if (hours < 24) {
                return hours
                        + (hours == 1
                        ? " hour ago"
                        : " hours ago");
            }

            long days =
                    hours / 24;

            if (days < 7) {
                return days
                        + (days == 1
                        ? " day ago"
                        : " days ago");
            }

            return new SimpleDateFormat(
                    "dd MMM yyyy, hh:mm a",
                    Locale.getDefault()
            ).format(
                    new Date(timeMillis)
            );

        } catch (Exception e) {

            return "Just now";
        }
    }

    // =========================================================
    // TITLE
    // =========================================================

    private static String getTitleFromType(
            String type
    ) {

        if (type == null) {
            return "New Activity";
        }

        switch (type) {

            case "like":
                return "New Like";

            case "comment":
                return "New Comment";

            case "follow":
                return "New Follower";

            case "message":
                return "New Message";

            case "story":
                return "New Story";

            case "post":
                return "New Post";

            default:
                return "New Activity";
        }
    }

    // =========================================================
    // CONNECTION
    // =========================================================

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

    private static boolean isValid(
            String uid,
            String notificationId
    ) {

        return uid != null
                && !uid.trim().isEmpty()
                && notificationId != null
                && !notificationId.trim().isEmpty();
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
            NotificationCallback callback,
            List<NotificationModel> list
    ) {

        MAIN.post(
                () -> callback.onSuccess(list)
        );
    }

    private static void postError(
            NotificationCallback callback,
            String message
    ) {

        MAIN.post(
                () -> callback.onError(message)
        );
    }
}
