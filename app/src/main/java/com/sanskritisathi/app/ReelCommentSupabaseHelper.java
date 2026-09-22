package com.sanskritisathi.app;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReelCommentSupabaseHelper {

    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();

    private static final String TABLE =
            "/rest/v1/reel_comments";

    public interface CommentsCallback {
        void onSuccess(List<ReelComment> comments);
        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String error);
    }

    public static void getComments(
            Context context,
            String reelId,
            CommentsCallback callback) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String url =
                        SupabaseConfig.PROJECT_URL
                                + TABLE
                                + "?reel_id=eq."
                                + encode(reelId)
                                + "&order=created_at.asc";

                connection =
                        openConnection(
                                context,
                                url,
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
                    throw new Exception(
                            extractError(response, code)
                    );
                }

                JSONArray array =
                        new JSONArray(response);

                List<ReelComment> result =
                        new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {

                    JSONObject object =
                            array.getJSONObject(i);

                    String id =
                            object.optString("id", "");

                    String returnedReelId =
                            object.optString(
                                    "reel_id",
                                    reelId
                            );

                    String userId =
                            object.optString(
                                    "user_id",
                                    ""
                            );

                    String username =
                            object.optString(
                                    "username",
                                    "User"
                            );

                    String text =
                            object.optString(
                                    "text",
                                    ""
                            );

                    long createdAt =
                            parseDate(
                                    object.optString(
                                            "created_at",
                                            ""
                                    )
                            );

                    result.add(
                            new ReelComment(
                                    id,
                                    returnedReelId,
                                    userId,
                                    username,
                                    text,
                                    createdAt
                            )
                    );
                }

                callback.onSuccess(result);

            } catch (Exception e) {

                callback.onError(
                        e.getMessage() == null
                                ? "Comments load failed"
                                : e.getMessage()
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
            String reelId,
            String text,
            ActionCallback callback) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String userId =
                        SupabaseAuthManager.getUserId(
                                context
                        );

                String accessToken =
                        SupabaseAuthManager.getAccessToken(
                                context
                        );

                if (userId == null ||
                        userId.trim().isEmpty() ||
                        accessToken == null ||
                        accessToken.trim().isEmpty()) {

                    throw new Exception(
                            "Login session nahi mili"
                    );
                }

                String username =
                        "User";

                String id =
                        UUID.randomUUID().toString();

                JSONObject body =
                        new JSONObject();

                body.put("id", id);
                body.put("reel_id", reelId);
                body.put("user_id", userId);
                body.put("username", username);
                body.put("text", text);
                body.put(
                        "created_at",
                        getCurrentUtcTime()
                );

                String url =
                        SupabaseConfig.PROJECT_URL
                                + TABLE;

                connection =
                        openConnection(
                                context,
                                url,
                                "POST"
                        );

                connection.setRequestProperty(
                        "Prefer",
                        "return=minimal"
                );

                connection.setDoOutput(true);

                byte[] bytes =
                        body.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                try (OutputStream output =
                             connection.getOutputStream()) {

                    output.write(bytes);
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
                            extractError(
                                    response,
                                    code
                            )
                    );
                }

                updateReelCommentCount(
                        context,
                        reelId,
                        accessToken
                );

                callback.onSuccess();

            } catch (Exception e) {

                callback.onError(
                        e.getMessage() == null
                                ? "Comment send failed"
                                : e.getMessage()
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private static void updateReelCommentCount(
            Context context,
            String reelId,
            String accessToken) {

        HttpURLConnection getConnection = null;
        HttpURLConnection patchConnection = null;

        try {

            String getUrl =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/reel_comments"
                            + "?reel_id=eq."
                            + encode(reelId);

            getConnection =
                    openConnection(
                            context,
                            getUrl,
                            "GET"
                    );

            int getCode =
                    getConnection.getResponseCode();

            String getResponse =
                    readResponse(
                            getConnection,
                            getCode
                    );

            if (getCode < 200 ||
                    getCode >= 300) {
                return;
            }

            JSONArray comments =
                    new JSONArray(getResponse);

            int count =
                    comments.length();

            JSONObject update =
                    new JSONObject();

            update.put("comments", count);

            String patchUrl =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/reels"
                            + "?id=eq."
                            + encode(reelId);

            patchConnection =
                    openConnection(
                            context,
                            patchUrl,
                            "PATCH"
                    );

            patchConnection.setRequestProperty(
                    "Prefer",
                    "return=minimal"
            );

            patchConnection.setDoOutput(true);

            byte[] bytes =
                    update.toString()
                            .getBytes(
                                    StandardCharsets.UTF_8
                            );

            try (OutputStream output =
                         patchConnection.getOutputStream()) {

                output.write(bytes);
            }

            patchConnection.getResponseCode();

        } catch (Exception ignored) {

        } finally {

            if (getConnection != null) {
                getConnection.disconnect();
            }

            if (patchConnection != null) {
                patchConnection.disconnect();
            }
        }
    }

    private static HttpURLConnection openConnection(
            Context context,
            String urlString,
            String method) throws Exception {

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(method);

        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);

        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );

        String token =
                SupabaseAuthManager.getAccessToken(
                        context
                );

        if (token != null &&
                !token.trim().isEmpty()) {

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token
            );
        }

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

    private static String readResponse(
            HttpURLConnection connection,
            int code) throws Exception {

        InputStream stream;

        if (code >= 200 && code < 300) {
            stream = connection.getInputStream();
        } else {
            stream = connection.getErrorStream();
        }

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

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }

        return builder.toString();
    }

    private static String extractError(
            String response,
            int code) {

        if (response == null ||
                response.trim().isEmpty()) {

            return "HTTP " + code;
        }

        try {

            JSONObject object =
                    new JSONObject(response);

            String message =
                    object.optString(
                            "message",
                            ""
                    );

            if (!message.isEmpty()) {
                return message;
            }

            String error =
                    object.optString(
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

    private static String encode(
            String value) {

        try {

            return java.net.URLEncoder
                    .encode(
                            value == null ? "" : value,
                            "UTF-8"
                    );

        } catch (Exception e) {

            return value == null
                    ? ""
                    : value;
        }
    }

    private static long parseDate(
            String value) {

        if (value == null ||
                value.trim().isEmpty()) {
            return 0L;
        }

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                            Locale.US
                    );

            format.setTimeZone(
                    TimeZone.getTimeZone("UTC")
            );

            Date date =
                    format.parse(value);

            return date == null
                    ? 0L
                    : date.getTime();

        } catch (Exception ignored) {

            return 0L;
        }
    }

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
}
