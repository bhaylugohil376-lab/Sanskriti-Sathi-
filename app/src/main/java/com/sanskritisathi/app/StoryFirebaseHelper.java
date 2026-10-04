package com.sanskritisathi.app;

import android.content.Context;
import android.net.Uri;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class StorySupabaseHelper {

    private static final String TABLE = "stories";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private static final Handler MAIN =
            new Handler(Looper.getMainLooper());

    private StorySupabaseHelper() {
    }

    public interface UploadCallback {
        void onSuccess(String storyId);
        void onError(String message);
    }

    public interface StoriesCallback {
        void onSuccess(List<Story> stories);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    // =========================================================
    // UPLOAD STORY
    // =========================================================

    public static void uploadStory(
            Context context,
            Uri imageUri,
            String caption,
            String visibility,
            UploadCallback callback
    ) {

        if (context == null) {
            callback.onError("Context missing.");
            return;
        }

        if (imageUri == null) {
            callback.onError("Story image select karein.");
            return;
        }

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("Please login first.");
            return;
        }

        B2MediaHelper.uploadImage(
                context,
                imageUri,
                "stories",
                new B2MediaHelper.UploadCallback() {

                    @Override
                    public void onProgress(int progress) {
                    }

                    @Override
                    public void onSuccess(String fileName) {

                        saveStory(
                                context,
                                uid,
                                fileName,
                                caption,
                                visibility,
                                callback
                        );
                    }

                    @Override
                    public void onError(String message) {

                        callback.onError(
                                message == null
                                        ? "Story image upload failed."
                                        : message
                        );
                    }
                }
        );
    }

    private static void saveStory(
            Context context,
            String uid,
            String fileName,
            String caption,
            String visibility,
            UploadCallback callback
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                JSONObject body = new JSONObject();

                body.put("owner_uid", uid);
                body.put("image_file", fileName);
                body.put(
                        "caption",
                        caption == null
                                ? ""
                                : caption.trim()
                );
                body.put(
                        "visibility",
                        "Followers".equalsIgnoreCase(
                                visibility
                        )
                                ? "Followers"
                                : "Public"
                );
                body.put("views", 0);
                body.put("likes", 0);

                URL url = new URL(
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
                connection.setReadTimeout(30000);

                addHeaders(
                        connection,
                        context
                );

                connection.setRequestProperty(
                        "Prefer",
                        "return=representation"
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

                if (code < 200 || code >= 300) {

                    postError(
                            callback,
                            "Story save nahi hui: "
                                    + cleanError(response)
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                String storyId = "";

                if (array.length() > 0) {

                    JSONObject result =
                            array.getJSONObject(0);

                    storyId =
                            result.optString(
                                    "id",
                                    ""
                            );
                }

                final String finalStoryId =
                        storyId;

                MAIN.post(() ->
                        callback.onSuccess(
                                finalStoryId
                        )
                );

            } catch (Exception e) {

                postError(
                        callback,
                        "Story save error: "
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
    // LOAD ACTIVE STORIES
    // =========================================================

    public static void getActiveStories(
            Context context,
            @NonNull StoriesCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {

            callback.onError(
                    "Please login first."
            );

            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                long cutoff =
                        System.currentTimeMillis()
                                - 24L * 60L * 60L * 1000L;

                String query =
                        "created_at=gte."
                                + encode(
                                java.time.Instant
                                        .ofEpochMilli(cutoff)
                                        .toString()
                        )
                                + "&order=created_at.desc"
                                + "&limit=100";

                URL url =
                        new URL(
                                SupabaseConfig.PROJECT_URL
                                        + "/rest/v1/"
                                        + TABLE
                                        + "?"
                                        + query
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);

                addHeaders(
                        connection,
                        context
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
                            "Stories load nahi hui: "
                                    + cleanError(response)
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                List<Story> stories =
                        new ArrayList<>();

                for (int i = 0;
                     i < array.length();
                     i++) {

                    JSONObject json =
                            array.getJSONObject(i);

                    Story story =
                            jsonToStory(
                                    context,
                                    json,
                                    uid
                            );

                    if (story != null) {
                        stories.add(story);
                    }
                }

                MAIN.post(() ->
                        callback.onSuccess(
                                stories
                        )
                );

            } catch (Exception e) {

                postError(
                        callback,
                        "Stories load error: "
                                + safeMessage(e)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private static Story jsonToStory(
            Context context,
            JSONObject json,
            String currentUid
    ) {

        try {

            String id =
                    json.optString(
                            "id",
                            ""
                    );

            String ownerUid =
                    json.optString(
                            "owner_uid",
                            ""
                    );

            String fileName =
                    json.optString(
                            "image_file",
                            ""
                    );

            String caption =
                    json.optString(
                            "caption",
                            ""
                    );

            String visibility =
                    json.optString(
                            "visibility",
                            "Public"
                    );

            long createdAt =
                    parseTime(
                            json.optString(
                                    "created_at",
                                    ""
                            )
                    );

            int views =
                    json.optInt(
                            "views",
                            0
                    );

            if (id.isEmpty() ||
                    fileName.isEmpty()) {

                return null;
            }

            String username =
                    "Sanskriti User";

            boolean ownStory =
                    ownerUid.equals(currentUid);

            Story story =
                    new Story(
                            id,
                            username,
                            "",
                            "",
                            caption,
                            visibility,
                            createdAt,
                            Math.max(0, views),
                            ownStory
                    );

            // B2 URL async resolve baad mein adapter/activity
            // se kiya ja sakta hai. File name temporarily
            // story image field mein rakha gaya hai.
            story.setLiked(false);

            return story;

        } catch (Exception e) {

            return null;
        }
    }

    // =========================================================
    // UNIQUE VIEW
    // =========================================================

    public static void addStoryView(
            Context context,
            String storyId,
            ActionCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {

            if (callback != null) {
                callback.onError(
                        "Please login first."
                );
            }

            return;
        }

        if (storyId == null ||
                storyId.trim().isEmpty()) {

            if (callback != null) {
                callback.onError(
                        "Story ID missing."
                );
            }

            return;
        }

        EXECUTOR.execute(() -> {

            try {

                // Unique view table:
                // story_views
                //
                // columns:
                // story_id
                // user_id

                JSONObject body =
                        new JSONObject();

                body.put(
                        "story_id",
                        storyId
                );

                body.put(
                        "user_id",
                        uid
                );

                postJson(
                        context,
                        "story_views",
                        body
                );

                MAIN.post(() -> {

                    if (callback != null) {
                        callback.onSuccess();
                    }
                });

            } catch (Exception e) {

                MAIN.post(() -> {

                    if (callback != null) {
                        callback.onError(
                                safeMessage(e)
                        );
                    }
                });
            }
        });
    }

    // =========================================================
    // LIKE / UNLIKE
    // =========================================================

    public static void toggleStoryLike(
            Context context,
            String storyId,
            boolean like,
            ActionCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {

            callback.onError(
                    "Please login first."
            );

            return;
        }

        if (storyId == null ||
                storyId.trim().isEmpty()) {

            callback.onError(
                    "Story ID missing."
            );

            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                if (like) {

                    JSONObject body =
                            new JSONObject();

                    body.put(
                            "story_id",
                            storyId
                    );

                    body.put(
                            "user_id",
                            uid
                    );

                    postJson(
                            context,
                            "story_likes",
                            body
                    );

                } else {

                    URL url =
                            new URL(
                                    SupabaseConfig.PROJECT_URL
                                            + "/rest/v1/story_likes"
                                            + "?story_id=eq."
                                            + encode(storyId)
                                            + "&user_id=eq."
                                            + encode(uid)
                            );

                    connection =
                            (HttpURLConnection)
                                    url.openConnection();

                    connection.setRequestMethod(
                            "DELETE"
                    );

                    connection.setConnectTimeout(
                            15000
                    );

                    connection.setReadTimeout(
                            30000
                    );

                    addHeaders(
                            connection,
                            context
                    );

                    int code =
                            connection.getResponseCode();

                    String response =
                            readResponse(
                                    connection,
                                    code
                            );

                    if (code < 200 ||
                            code >= 300) {

                        throw new Exception(
                                cleanError(response)
                        );
                    }
                }

                MAIN.post(
                        callback::onSuccess
                );

            } catch (Exception e) {

                MAIN.post(() ->
                        callback.onError(
                                "Like update failed: "
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
    // STORY REPLY
    // =========================================================

    public static void addStoryReply(
            Context context,
            String storyId,
            String replyText,
            ActionCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {

            callback.onError(
                    "Please login first."
            );

            return;
        }

        if (replyText == null ||
                replyText.trim().isEmpty()) {

            callback.onError(
                    "Reply empty nahi ho sakta."
            );

            return;
        }

        String text =
                replyText.trim();

        if (text.length() > 500) {

            callback.onError(
                    "Reply 500 characters se kam hona chahiye."
            );

            return;
        }

        EXECUTOR.execute(() -> {

            try {

                JSONObject body =
                        new JSONObject();

                body.put(
                        "story_id",
                        storyId
                );

                body.put(
                        "user_id",
                        uid
                );

                body.put(
                        "text",
                        text
                );

                postJson(
                        context,
                        "story_replies",
                        body
                );

                MAIN.post(
                        callback::onSuccess
                );

            } catch (Exception e) {

                MAIN.post(() ->
                        callback.onError(
                                "Reply send nahi hui: "
                                        + safeMessage(e)
                        )
                );
            }
        });
    }

    // =========================================================
    // DELETE STORY
    // =========================================================

    public static void deleteStory(
            Context context,
            String storyId,
            ActionCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (uid == null || uid.trim().isEmpty()) {

            callback.onError(
                    "Please login first."
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
                                        + encode(storyId)
                                        + "&owner_uid=eq."
                                        + encode(uid)
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod(
                        "DELETE"
                );

                connection.setConnectTimeout(
                        15000
                );

                connection.setReadTimeout(
                        30000
                );

                addHeaders(
                        connection,
                        context
                );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code >= 200 &&
                        code < 300) {

                    MAIN.post(
                            callback::onSuccess
                    );

                } else {

                    MAIN.post(() ->
                            callback.onError(
                                    "Story delete failed: "
                                            + cleanError(
                                            response
                                    )
                            )
                    );
                }

            } catch (Exception e) {

                MAIN.post(() ->
                        callback.onError(
                                "Story delete error: "
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
    // HTTP
    // =========================================================

    private static void postJson(
            Context context,
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
            connection.setReadTimeout(30000);

            addHeaders(
                    connection,
                    context
            );

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

            if (code < 200 || code >= 300) {

                throw new Exception(
                        cleanError(response)
                );
            }

        } finally {

            connection.disconnect();
        }
    }

    private static void addHeaders(
            HttpURLConnection connection,
            Context context
    ) {

        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );

        String token =
                SupabaseAuthManager.getAccessToken(
                        context
                );

        if (token == null ||
                token.trim().isEmpty()) {

            token =
                    SupabaseConfig.PUBLISHABLE_KEY;
        }

        connection.setRequestProperty(
                "Authorization",
                "Bearer " + token
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );
    }

    private static String readResponse(
            HttpURLConnection connection,
            int code
    ) throws Exception {

        InputStream stream =
                code >= 400
                        ? connection.getErrorStream()
                        : connection.getInputStream();

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
                    "error",
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

    private static long parseTime(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return System.currentTimeMillis();
        }

        try {

            return java.time.Instant
                    .parse(value)
                    .toEpochMilli();

        } catch (Exception e) {

            return System.currentTimeMillis();
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

    private static void postError(
            StoriesCallback callback,
            String message
    ) {

        MAIN.post(() ->
                callback.onError(message)
        );
    }
}
