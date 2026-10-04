package com.sanskritisathi.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class StorySupabaseHelper {

    private StorySupabaseHelper() {
    }

    private static final String STORIES_TABLE = "stories";
    private static final String LIKES_TABLE = "story_likes";
    private static final String REPLIES_TABLE = "story_replies";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private static final Handler MAIN_HANDLER =
            new Handler(Looper.getMainLooper());

    public interface StoriesCallback {
        void onSuccess(List<Story> stories);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface LikeCheckCallback {
        void onResult(boolean liked);
        void onError(String message);
    }

    // =========================================================
    // GET ACTIVE STORIES
    // =========================================================

    public static void getActiveStories(
            Context context,
            @NonNull StoriesCallback callback
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + STORIES_TABLE
                                + "?select=*"
                                + "&order=created_at.desc"
                                + "&limit=100";

                connection = openConnection(
                        url,
                        "GET",
                        getToken(context)
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

                List<Story> result =
                        new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {

                    Story story =
                            jsonToStory(
                                    array.getJSONObject(i),
                                    context
                            );

                    if (story != null &&
                            !story.isExpired()) {

                        result.add(story);
                    }
                }

                postSuccess(
                        callback,
                        result
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

    // =========================================================
    // ADD VIEW
    // =========================================================

    public static void addStoryView(
            Context context,
            String storyId,
            @NonNull ActionCallback callback
    ) {

        if (TextUtils.isEmpty(storyId)) {
            postError(
                    callback,
                    "Invalid Story."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String getUrl =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + STORIES_TABLE
                                + "?select=views"
                                + "&id=eq."
                                + encode(storyId);

                connection = openConnection(
                        getUrl,
                        "GET",
                        getToken(context)
                );

                int getCode =
                        connection.getResponseCode();

                String getResponse =
                        readResponse(
                                connection,
                                getCode
                        );

                connection.disconnect();
                connection = null;

                if (getCode < 200 ||
                        getCode >= 300) {

                    postError(
                            callback,
                            cleanError(getResponse)
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(getResponse);

                if (array.length() == 0) {

                    postError(
                            callback,
                            "Story not found."
                    );

                    return;
                }

                int oldViews =
                        Math.max(
                                0,
                                array.getJSONObject(0)
                                        .optInt(
                                                "views",
                                                0
                                        )
                        );

                int newViews =
                        oldViews + 1;

                String patchUrl =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + STORIES_TABLE
                                + "?id=eq."
                                + encode(storyId);

                JSONObject body =
                        new JSONObject();

                body.put(
                        "views",
                        newViews
                );

                connection = openConnection(
                        patchUrl,
                        "PATCH",
                        getToken(context)
                );

                connection.setDoOutput(true);

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

                if (code >= 200 &&
                        code < 300) {

                    postSuccess(callback);

                } else {

                    postError(
                            callback,
                            cleanError(response)
                    );
                }

            } catch (Exception e) {

                postError(
                        callback,
                        "View update failed: "
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
    // CHECK LIKE
    // =========================================================

    public static void checkStoryLike(
            Context context,
            String storyId,
            @NonNull LikeCheckCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (TextUtils.isEmpty(uid) ||
                TextUtils.isEmpty(storyId)) {

            postLikeResult(
                    callback,
                    false
            );

            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + LIKES_TABLE
                                + "?select=id"
                                + "&story_id=eq."
                                + encode(storyId)
                                + "&user_id=eq."
                                + encode(uid)
                                + "&limit=1";

                connection = openConnection(
                        url,
                        "GET",
                        getToken(context)
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

                    postLikeError(
                            callback,
                            cleanError(response)
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                postLikeResult(
                        callback,
                        array.length() > 0
                );

            } catch (Exception e) {

                postLikeError(
                        callback,
                        safeMessage(e)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // =========================================================
    // LIKE / UNLIKE
    // =========================================================

    public static void toggleStoryLike(
            Context context,
            String storyId,
            boolean shouldLike,
            @NonNull ActionCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (TextUtils.isEmpty(uid)) {
            postError(
                    callback,
                    "Please login first."
            );
            return;
        }

        if (TextUtils.isEmpty(storyId)) {
            postError(
                    callback,
                    "Invalid Story."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                if (shouldLike) {

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

                    String url =
                            SupabaseConfig.PROJECT_URL
                                    + "/rest/v1/"
                                    + LIKES_TABLE;

                    connection = openConnection(
                            url,
                            "POST",
                            getToken(context)
                    );

                    connection.setDoOutput(true);

                    connection.setRequestProperty(
                            "Prefer",
                            "return=minimal"
                    );

                    writeBody(
                            connection,
                            body.toString()
                    );

                } else {

                    String url =
                            SupabaseConfig.PROJECT_URL
                                    + "/rest/v1/"
                                    + LIKES_TABLE
                                    + "?story_id=eq."
                                    + encode(storyId)
                                    + "&user_id=eq."
                                    + encode(uid);

                    connection = openConnection(
                            url,
                            "DELETE",
                            getToken(context)
                    );
                }

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code >= 200 &&
                        code < 300) {

                    postSuccess(callback);

                } else {

                    postError(
                            callback,
                            cleanError(response)
                    );
                }

            } catch (Exception e) {

                postError(
                        callback,
                        "Like update failed: "
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
    // ADD REPLY
    // =========================================================

    public static void addStoryReply(
            Context context,
            String storyId,
            String text,
            @NonNull ActionCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (TextUtils.isEmpty(uid)) {
            postError(
                    callback,
                    "Please login first."
            );
            return;
        }

        if (TextUtils.isEmpty(storyId)) {
            postError(
                    callback,
                    "Invalid Story."
            );
            return;
        }

        if (TextUtils.isEmpty(text)) {
            postError(
                    callback,
                    "Reply likhein."
            );
            return;
        }

        String finalText =
                text.trim();

        if (finalText.length() > 1000) {
            postError(
                    callback,
                    "Reply maximum 1000 characters."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

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
                        finalText
                );

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + REPLIES_TABLE;

                connection = openConnection(
                        url,
                        "POST",
                        getToken(context)
                );

                connection.setDoOutput(true);

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

                if (code >= 200 &&
                        code < 300) {

                    postSuccess(callback);

                } else {

                    postError(
                            callback,
                            cleanError(response)
                    );
                }

            } catch (Exception e) {

                postError(
                        callback,
                        "Reply failed: "
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
    // DELETE STORY
    // =========================================================

    public static void deleteStory(
            Context context,
            String storyId,
            @NonNull ActionCallback callback
    ) {

        String uid =
                SupabaseAuthManager.getUserId(context);

        if (TextUtils.isEmpty(uid)) {
            postError(
                    callback,
                    "Please login first."
            );
            return;
        }

        if (TextUtils.isEmpty(storyId)) {
            postError(
                    callback,
                    "Invalid Story."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + STORIES_TABLE
                                + "?id=eq."
                                + encode(storyId)
                                + "&owner_uid=eq."
                                + encode(uid);

                connection = openConnection(
                        url,
                        "DELETE",
                        getToken(context)
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

                    postSuccess(callback);

                } else {

                    postError(
                            callback,
                            "Story delete failed: "
                                    + cleanError(response)
                    );
                }

            } catch (Exception e) {

                postError(
                        callback,
                        "Delete error: "
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
    // JSON → STORY
    // =========================================================

    private static Story jsonToStory(
            JSONObject json,
            Context context
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
                            json.optString(
                                    "user_id",
                                    ""
                            )
                    );

            String username =
                    json.optString(
                            "username",
                            json.optString(
                                    "author",
                                    "Sanskriti User"
                            )
                    );

            String profileImage =
                    json.optString(
                            "profile_image",
                            json.optString(
                                    "profileImage",
                                    ""
                            )
                    );

            String storyImage =
                    json.optString(
                            "image_url",
                            json.optString(
                                    "story_image",
                                    json.optString(
                                            "image_file",
                                            ""
                                    )
                            )
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
                    parseCreatedAt(
                            json.optString(
                                    "created_at",
                                    ""
                            )
                    );

            int views =
                    Math.max(
                            0,
                            json.optInt(
                                    "views",
                                    0
                            )
                    );

            String currentUid =
                    SupabaseAuthManager
                            .getUserId(context);

            boolean ownStory =
                    !TextUtils.isEmpty(ownerUid)
                            && ownerUid.equals(
                            currentUid
                    );

            return new Story(
                    id,
                    username,
                    profileImage,
                    storyImage,
                    caption,
                    visibility,
                    createdAt,
                    views,
                    ownStory
            );

        } catch (Exception e) {

            return null;
        }
    }

    // =========================================================
    // CREATED AT
    // =========================================================

    private static long parseCreatedAt(
            String value
    ) {

        if (TextUtils.isEmpty(value)) {
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

    // =========================================================
    // CONNECTION
    // =========================================================

    private static HttpURLConnection openConnection(
            String urlString,
            String method,
            String token
    ) throws Exception {

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod(method);

        connection.setConnectTimeout(
                15000
        );

        connection.setReadTimeout(
                20000
        );

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

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        return connection;
    }

    // =========================================================
    // TOKEN
    // =========================================================

    private static String getToken(
            Context context
    ) {

        String token =
                SupabaseAuthManager
                        .getAccessToken(context);

        if (TextUtils.isEmpty(token)) {

            return SupabaseConfig.PUBLISHABLE_KEY;
        }

        return token;
    }

    // =========================================================
    // WRITE BODY
    // =========================================================

    private static void writeBody(
            HttpURLConnection connection,
            String body
    ) throws Exception {

        byte[] data =
                body.getBytes(
                        StandardCharsets.UTF_8
                );

        connection.setFixedLengthStreamingMode(
                data.length
        );

        try (OutputStream output =
                     connection.getOutputStream()) {

            output.write(data);
            output.flush();
        }
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

    // =========================================================
    // ERROR
    // =========================================================

    private static String cleanError(
            String response
    ) {

        if (TextUtils.isEmpty(response)) {
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

    // =========================================================
    // CALLBACK HELPERS
    // =========================================================

    private static void postSuccess(
            @NonNull ActionCallback callback
    ) {

        MAIN_HANDLER.post(
                callback::onSuccess
        );
    }

    private static void postError(
            @NonNull ActionCallback callback,
            String message
    ) {

        MAIN_HANDLER.post(
                () -> callback.onError(message)
        );
    }

    private static void postSuccess(
            @NonNull StoriesCallback callback,
            List<Story> stories
    ) {

        MAIN_HANDLER.post(
                () -> callback.onSuccess(stories)
        );
    }

    private static void postError(
            @NonNull StoriesCallback callback,
            String message
    ) {

        MAIN_HANDLER.post(
                () -> callback.onError(message)
        );
    }

    private static void postLikeResult(
            @NonNull LikeCheckCallback callback,
            boolean liked
    ) {

        MAIN_HANDLER.post(
                () -> callback.onResult(liked)
        );
    }

    private static void postLikeError(
            @NonNull LikeCheckCallback callback,
            String message
    ) {

        MAIN_HANDLER.post(
                () -> callback.onError(message)
        );
    }
}
