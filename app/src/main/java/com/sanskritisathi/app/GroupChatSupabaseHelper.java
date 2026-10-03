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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GroupChatSupabaseHelper {

    private GroupChatSupabaseHelper() {
    }

    private static final String TABLE = "group_messages";
    private static final String GROUP_ID = "sanskriti_sathi_group";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private static final Handler MAIN =
            new Handler(Looper.getMainLooper());

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface MessagesCallback {
        void onSuccess(List<GroupMessage> messages);
        void onError(String message);
    }

    // =========================================================
    // SEND MESSAGE
    // =========================================================

    public static void sendMessage(
            Context context,
            String text,
            @NonNull ActionCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("Pehle login karein.");
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            callback.onError("Message likho.");
            return;
        }

        String messageText = text.trim();

        if (messageText.length() > 500) {
            callback.onError(
                    "Message maximum 500 characters ka ho sakta hai."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            try {

                String username =
                        getUsername(uid);

                JSONObject body =
                        new JSONObject();

                body.put(
                        "group_id",
                        GROUP_ID
                );

                body.put(
                        "user_id",
                        uid
                );

                body.put(
                        "username",
                        username
                );

                body.put(
                        "text",
                        messageText
                );

                body.put(
                        "created_at",
                        Instant.now().toString()
                );

                HttpURLConnection connection =
                        openConnection(
                                TABLE,
                                "POST"
                        );

                connection.setDoOutput(true);

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

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                connection.disconnect();

                if (code >= 200 && code < 300) {

                    MAIN.post(
                            callback::onSuccess
                    );

                } else {

                    MAIN.post(() ->
                            callback.onError(
                                    "Message send nahi hua: "
                                            + cleanError(response)
                            )
                    );
                }

            } catch (Exception e) {

                MAIN.post(() ->
                        callback.onError(
                                "Message send nahi hua: "
                                        + safeMessage(e)
                        )
                );
            }
        });
    }

    // =========================================================
    // GET MESSAGES
    // =========================================================

    public static void getMessages(
            Context context,
            @NonNull MessagesCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("Pehle login karein.");
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String query =
                        "group_id=eq."
                                + encode(GROUP_ID)
                                + "&select=*"
                                + "&order=created_at.asc"
                                + "&limit=200";

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

                    MAIN.post(() ->
                            callback.onError(
                                    "Messages load nahi hue: "
                                            + cleanError(response)
                            )
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                List<GroupMessage> list =
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

                    String userId =
                            json.optString(
                                    "user_id",
                                    ""
                            );

                    String username =
                            json.optString(
                                    "username",
                                    "User"
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

                    long timestamp =
                            parseTimestamp(
                                    createdAt
                            );

                    list.add(
                            new GroupMessage(
                                    id,
                                    userId,
                                    username,
                                    text,
                                    timestamp
                            )
                    );
                }

                MAIN.post(() ->
                        callback.onSuccess(list)
                );

            } catch (Exception e) {

                MAIN.post(() ->
                        callback.onError(
                                "Messages load nahi hue: "
                                        + safeMessage(e)
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
    // DELETE MESSAGE
    // =========================================================

    public static void deleteMessage(
            Context context,
            String messageId,
            @NonNull ActionCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("Pehle login karein.");
            return;
        }

        if (messageId == null ||
                messageId.trim().isEmpty()) {

            callback.onError("Invalid message.");
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                /*
                 * user_id condition important hai.
                 * Isse user sirf apna message delete kar sakta hai.
                 */

                String query =
                        "id=eq."
                                + encode(messageId)
                                + "&user_id=eq."
                                + encode(uid);

                connection =
                        openConnection(
                                TABLE + "?" + query,
                                "DELETE"
                        );

                connection.setRequestProperty(
                        "Prefer",
                        "return=minimal"
                );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code >= 200 && code < 300) {

                    MAIN.post(
                            callback::onSuccess
                    );

                } else {

                    MAIN.post(() ->
                            callback.onError(
                                    "Message delete nahi hua: "
                                            + cleanError(response)
                            )
                    );
                }

            } catch (Exception e) {

                MAIN.post(() ->
                        callback.onError(
                                "Message delete nahi hua: "
                                        + safeMessage(e)
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
    // USERNAME
    // =========================================================

    private static String getUsername(
            String uid
    ) {

        HttpURLConnection connection = null;

        try {

            String query =
                    "id=eq."
                            + encode(uid)
                            + "&select=username,name"
                            + "&limit=1";

            connection =
                    openConnection(
                            "users?" + query,
                            "GET"
                    );

            int code =
                    connection.getResponseCode();

            if (code < 200 || code >= 300) {
                return "User";
            }

            String response =
                    readResponse(
                            connection,
                            code
                    );

            JSONArray array =
                    new JSONArray(response);

            if (array.length() == 0) {
                return "User";
            }

            JSONObject user =
                    array.getJSONObject(0);

            String username =
                    user.optString(
                            "username",
                            ""
                    );

            if (!username.trim().isEmpty()) {
                return username.trim();
            }

            String name =
                    user.optString(
                            "name",
                            ""
                    );

            if (!name.trim().isEmpty()) {
                return name.trim();
            }

        } catch (Exception ignored) {

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }

        return "User";
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

    // =========================================================
    // RESPONSE
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

    private static long parseTimestamp(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return 0L;
        }

        try {

            return Instant
                    .parse(value)
                    .toEpochMilli();

        } catch (Exception ignored) {
            return 0L;
        }
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

            String hint =
                    json.optString(
                            "hint",
                            ""
                    );

            if (!hint.isEmpty()) {
                return hint;
            }

        } catch (Exception ignored) {
        }

        return response;
    }

    private static String safeMessage(
            Exception e
    ) {

        return e.getMessage() == null
                ? e.getClass().getSimpleName()
                : e.getMessage();
    }
}
