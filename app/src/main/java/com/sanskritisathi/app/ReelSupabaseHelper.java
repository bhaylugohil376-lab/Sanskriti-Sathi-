package com.sanskritisathi.app;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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

public final class ReelSupabaseHelper {

    private static final Handler MAIN_HANDLER =
            new Handler(Looper.getMainLooper());

    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();

    private static final String REELS_TABLE = "reels";
    private static final String LIKES_TABLE = "reel_likes";

    private static final String STORAGE_FOLDER = "reels";

    private ReelSupabaseHelper() {
    }

    // =========================================================
    // CALLBACKS
    // =========================================================

    public interface ReelsCallback {

        void onSuccess(List<Reel> reels);

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

    public interface UploadCallback {

        void onProgress(int progress);

        void onSuccess(String videoUrl);

        void onError(String message);
    }

    // =========================================================
    // GET ACTIVE REELS
    // =========================================================

    public static void getActiveReels(
            Context context,
            ReelsCallback callback) {

        if (context == null) {
            postError(
                    callback,
                    "Context missing."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String accessToken =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(userId)
                        || TextUtils.isEmpty(accessToken)) {

                    postError(
                            callback,
                            "Login session nahi mili."
                    );
                    return;
                }

                String encodedUserId =
                        URLEncoder.encode(
                                userId,
                                "UTF-8"
                        );

                String filter =
                        "or=("
                                + "visibility.eq.Public,"
                                + "user_id.eq."
                                + encodedUserId
                                + ")";

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + REELS_TABLE
                                + "?select=*"
                                + "&"
                                + filter
                                + "&order=created_at.desc";

                connection =
                        openConnection(
                                url,
                                "GET",
                                accessToken
                        );

                int responseCode =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                responseCode
                        );

                if (responseCode < 200
                        || responseCode >= 300) {

                    postError(
                            callback,
                            "Reels load failed: "
                                    + response
                    );
                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                List<Reel> result =
                        new ArrayList<>();

                for (int i = 0;
                     i < array.length();
                     i++) {

                    JSONObject json =
                            array.getJSONObject(i);

                    String reelId =
                            json.optString(
                                    "id",
                                    ""
                            );

                    String reelUserId =
                            json.optString(
                                    "user_id",
                                    ""
                            );

                    String videoUrl =
                            json.optString(
                                    "video_url",
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

                    int likes =
                            json.optInt(
                                    "likes",
                                    0
                            );

                    int comments =
                            json.optInt(
                                    "comments",
                                    0
                            );

                    int views =
                            json.optInt(
                                    "views",
                                    0
                            );

                    boolean ownReel =
                            userId.equals(
                                    reelUserId
                            );

                    String username =
                            json.optString(
                                    "username",
                                    ""
                            );

                    if (TextUtils.isEmpty(username)) {

                        username =
                                getUsername(
                                        reelUserId,
                                        accessToken
                                );
                    }

                    String thumbnailUrl =
                            json.optString(
                                    "thumbnail_url",
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

                    Reel reel =
                            new Reel(
                                    reelId,
                                    reelUserId,
                                    username,
                                    videoUrl,
                                    thumbnailUrl,
                                    caption,
                                    visibility,
                                    createdAt,
                                    likes,
                                    comments,
                                    views,
                                    false,
                                    ownReel
                            );

                    result.add(reel);
                }

                final List<Reel> finalResult =
                        result;

                MAIN_HANDLER.post(() ->
                        callback.onSuccess(
                                finalResult
                        )
                );

            } catch (Exception e) {

                postError(
                        callback,
                        safeMessage(
                                e,
                                "Reels load nahi hui."
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
    // UPLOAD REEL
    // =========================================================

    public static void uploadReel(
            Context context,
            Uri videoUri,
            String caption,
            String visibility,
            UploadCallback callback) {

        if (context == null) {
            postUploadError(
                    callback,
                    "Context missing."
            );
            return;
        }

        if (videoUri == null) {
            postUploadError(
                    callback,
                    "Video select nahi hui."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String accessToken =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(userId)
                        || TextUtils.isEmpty(accessToken)) {

                    postUploadError(
                            callback,
                            "Login session nahi mili."
                    );
                    return;
                }

                postProgress(
                        callback,
                        5
                );

                byte[] videoBytes =
                        readUriBytes(
                                context,
                                videoUri
                        );

                if (videoBytes == null
                        || videoBytes.length == 0) {

                    postUploadError(
                            callback,
                            "Video read nahi ho saki."
                    );
                    return;
                }

                postProgress(
                        callback,
                        15
                );

                String fileName =
                        "reel_"
                                + userId
                                + "_"
                                + System.currentTimeMillis()
                                + ".mp4";

                String fileBase64 =
                        android.util.Base64.encodeToString(
                                videoBytes,
                                android.util.Base64.NO_WRAP
                        );

                JSONObject uploadJson =
                        new JSONObject();

                uploadJson.put(
                        "action",
                        "upload"
                );

                uploadJson.put(
                        "fileName",
                        fileName
                );

                uploadJson.put(
                        "fileBase64",
                        fileBase64
                );

                uploadJson.put(
                        "folder",
                        STORAGE_FOLDER
                );

                uploadJson.put(
                        "contentType",
                        "video/mp4"
                );

                connection =
                        openConnection(
                                SupabaseConfig.PROJECT_URL
                                        + "/functions/v1/quick-handler",
                                "POST",
                                accessToken
                        );

                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                byte[] body =
                        uploadJson
                                .toString()
                                .getBytes(
                                        "UTF-8"
                                );

                OutputStream output =
                        connection.getOutputStream();

                output.write(body);
                output.flush();
                output.close();

                postProgress(
                        callback,
                        70
                );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code < 200
                        || code >= 300) {

                    postUploadError(
                            callback,
                            "Video upload failed: "
                                    + response
                    );
                    return;
                }

                JSONObject result =
                        new JSONObject(response);

                boolean success =
                        result.optBoolean(
                                "success",
                                true
                        );

                if (!success) {

                    String serverError =
                            result.optString(
                                    "error",
                                    ""
                            );

                    if (TextUtils.isEmpty(serverError)) {
                        serverError =
                                result.optString(
                                        "message",
                                        "Video upload failed."
                                );
                    }

                    postUploadError(
                            callback,
                            serverError
                    );
                    return;
                }

                String returnedFileName =
                        result.optString(
                                "fileName",
                                fileName
                        );

                String videoUrl =
                        result.optString(
                                "downloadUrl",
                                ""
                        );

                if (TextUtils.isEmpty(videoUrl)) {

                    videoUrl =
                            result.optString(
                                    "publicUrl",
                                    ""
                            );
                }

                if (TextUtils.isEmpty(videoUrl)) {

                    videoUrl =
                            SupabaseConfig.PROJECT_URL
                                    + "/storage/v1/object/public/"
                                    + STORAGE_FOLDER
                                    + "/"
                                    + returnedFileName;
                }

                postProgress(
                        callback,
                        85
                );

                try {
                    insertReel(
                            userId,
                            caption,
                            visibility,
                            videoUrl,
                            accessToken
                    );
                } catch (Exception dbError) {
                    postUploadError(
                            callback,
                            dbError.getMessage() == null
                                    ? "Reel database me save nahi hui."
                                    : dbError.getMessage()
                    );
                    return;
                }

                postProgress(
                        callback,
                        100
                );

                final String finalVideoUrl =
                        videoUrl;

                MAIN_HANDLER.post(() ->
                        callback.onSuccess(
                                finalVideoUrl
                        )
                );

            } catch (Exception e) {

                postUploadError(
                        callback,
                        safeMessage(
                                e,
                                "Reel upload failed."
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
    // INSERT REEL
    // =========================================================

    private static void insertReel(
            String userId,
            String caption,
            String visibility,
            String videoUrl,
            String accessToken)
            throws Exception {

        HttpURLConnection connection = null;

        try {
            JSONObject json = new JSONObject();

            json.put("id", UUID.randomUUID().toString());
            json.put("user_id", userId);
            json.put("video_url", videoUrl);
            json.put("caption", caption == null ? "" : caption);
            json.put("visibility", TextUtils.isEmpty(visibility) ? "Public" : visibility);
            json.put("likes", 0);
            json.put("comments", 0);
            json.put("views", 0);

            // Username intentionally omitted. Feed can read it from profiles.

            String url = SupabaseConfig.PROJECT_URL
                    + "/rest/v1/" + REELS_TABLE;

            connection = openConnection(url, "POST", accessToken);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Prefer", "return=minimal");

            OutputStream output = connection.getOutputStream();
            output.write(json.toString().getBytes(StandardCharsets.UTF_8));
            output.flush();
            output.close();

            int code = connection.getResponseCode();
            String response = readResponse(connection, code);

            if (code < 200 || code >= 300) {
                throw new Exception(
                        "Reel database save failed.\nHTTP "
                                + code + "\n" + response
                );
            }

        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    // =========================================================
    // ADD VIEW
    // =========================================================

    public static void addReelView(
            Context context,
            String reelId,
            ActionCallback callback) {

        if (context == null
                || TextUtils.isEmpty(reelId)) {

            postActionError(
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
                    postActionError(
                            callback,
                            "Login session nahi mili."
                    );
                    return;
                }

                JSONObject current =
                        getReel(
                                reelId,
                                token
                        );

                if (current == null) {
                    postActionError(
                            callback,
                            "Reel nahi mili."
                    );
                    return;
                }

                int views =
                        current.optInt(
                                "views",
                                0
                        );

                JSONObject update =
                        new JSONObject();

                update.put(
                        "views",
                        views + 1
                );

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + REELS_TABLE
                                + "?id=eq."
                                + URLEncoder.encode(
                                        reelId,
                                        "UTF-8"
                                );

                connection =
                        openConnection(
                                url,
                                "PATCH",
                                token
                        );

                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                OutputStream output =
                        connection.getOutputStream();

                output.write(
                        update.toString()
                                .getBytes("UTF-8")
                );

                output.flush();
                output.close();

                int code =
                        connection.getResponseCode();

                if (code < 200
                        || code >= 300) {

                    postActionError(
                            callback,
                            "View update failed."
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
                                "View update failed."
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
    // CHECK LIKE
    // =========================================================

    public static void checkReelLike(
            Context context,
            String reelId,
            LikeCheckCallback callback) {

        if (context == null
                || TextUtils.isEmpty(reelId)) {

            postLikeError(
                    callback,
                    "Invalid Reel."
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

                    postLikeError(
                            callback,
                            "Login session nahi mili."
                    );
                    return;
                }

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + LIKES_TABLE
                                + "?select=id"
                                + "&reel_id=eq."
                                + URLEncoder.encode(
                                        reelId,
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

                if (code < 200
                        || code >= 300) {

                    postLikeError(
                            callback,
                            "Like check failed."
                    );
                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                final boolean liked =
                        array.length() > 0;

                MAIN_HANDLER.post(() ->
                        callback.onResult(
                                liked
                        )
                );

            } catch (Exception e) {

                postLikeError(
                        callback,
                        safeMessage(
                                e,
                                "Like check failed."
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
    // TOGGLE LIKE
    // =========================================================

    public static void toggleReelLike(
            Context context,
            String reelId,
            boolean currentlyLiked,
            ActionCallback callback) {

        if (context == null
                || TextUtils.isEmpty(reelId)) {

            postActionError(
                    callback,
                    "Invalid Reel."
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

                if (currentlyLiked) {

                    String url =
                            SupabaseConfig.PROJECT_URL
                                    + "/rest/v1/"
                                    + LIKES_TABLE
                                    + "?reel_id=eq."
                                    + URLEncoder.encode(
                                            reelId,
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

                } else {

                    JSONObject json =
                            new JSONObject();

                    json.put(
                            "id",
                            UUID.randomUUID()
                                    .toString()
                    );

                    json.put(
                            "reel_id",
                            reelId
                    );

                    json.put(
                            "user_id",
                            userId
                    );

                    String url =
                            SupabaseConfig.PROJECT_URL
                                    + "/rest/v1/"
                                    + LIKES_TABLE;

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

                    OutputStream output =
                            connection.getOutputStream();

                    output.write(
                            json.toString()
                                    .getBytes("UTF-8")
                    );

                    output.flush();
                    output.close();
                }

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code < 200
                        || code >= 300) {

                    postActionError(
                            callback,
                            "Like update failed: "
                                    + response
                    );
                    return;
                }

                updateLikeCount(
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
                                "Like update failed."
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
    // UPDATE LIKE COUNT
    // =========================================================

    private static void updateLikeCount(
            String reelId,
            String token) {

        HttpURLConnection countConnection = null;
        HttpURLConnection patchConnection = null;

        try {

            String countUrl =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/"
                            + LIKES_TABLE
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
                    countConnection
                            .getResponseCode();

            String countResponse =
                    readResponse(
                            countConnection,
                            countCode
                    );

            if (countCode < 200
                    || countCode >= 300) {
                return;
            }

            JSONArray likes =
                    new JSONArray(
                            countResponse
                    );

            JSONObject update =
                    new JSONObject();

            update.put(
                    "likes",
                    likes.length()
            );

            String patchUrl =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/"
                            + REELS_TABLE
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
                    patchConnection
                            .getOutputStream();

            output.write(
                    update.toString()
                            .getBytes("UTF-8")
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
    // DELETE REEL
    // =========================================================

    public static void deleteReel(
            Context context,
            String reelId,
            ActionCallback callback) {

        if (context == null
                || TextUtils.isEmpty(reelId)) {

            postActionError(
                    callback,
                    "Invalid Reel."
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
                                + REELS_TABLE
                                + "?id=eq."
                                + URLEncoder.encode(
                                        reelId,
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

                if (code < 200
                        || code >= 300) {

                    String response =
                            readResponse(
                                    connection,
                                    code
                            );

                    postActionError(
                            callback,
                            "Reel delete failed: "
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
                                "Reel delete failed."
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
    // EDIT REEL
    // =========================================================

    public static void updateReel(
            Context context,
            String reelId,
            String caption,
            String visibility,
            ActionCallback callback) {

        if (context == null || TextUtils.isEmpty(reelId)) {
            postActionError(callback, "Invalid Reel.");
            return;
        }

        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;

            try {
                String userId = SupabaseAuthManager.getUserId(context);
                String token = SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(token)) {
                    postActionError(callback, "Login session nahi mili.");
                    return;
                }

                JSONObject update = new JSONObject();
                update.put("caption", caption == null ? "" : caption);
                update.put("visibility", TextUtils.isEmpty(visibility) ? "Public" : visibility);

                String url = SupabaseConfig.PROJECT_URL
                        + "/rest/v1/" + REELS_TABLE
                        + "?id=eq." + URLEncoder.encode(reelId, "UTF-8")
                        + "&user_id=eq." + URLEncoder.encode(userId, "UTF-8");

                connection = openConnection(url, "PATCH", token);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Prefer", "return=minimal");

                OutputStream output = connection.getOutputStream();
                output.write(update.toString().getBytes(StandardCharsets.UTF_8));
                output.flush();
                output.close();

                int code = connection.getResponseCode();
                String response = readResponse(connection, code);

                if (code < 200 || code >= 300) {
                    postActionError(callback, "Reel update failed: " + response);
                    return;
                }

                postActionSuccess(callback);

            } catch (Exception e) {
                postActionError(callback, safeMessage(e, "Reel update failed."));
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    // =========================================================
    // GET SINGLE REEL
    // =========================================================

    private static JSONObject getReel(
            String reelId,
            String token)
            throws Exception {

        HttpURLConnection connection = null;

        try {

            String url =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/"
                            + REELS_TABLE
                            + "?select=*"
                            + "&id=eq."
                            + URLEncoder.encode(
                                    reelId,
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

            if (code < 200
                    || code >= 300) {
                return null;
            }

            JSONArray array =
                    new JSONArray(response);

            if (array.length() == 0) {
                return null;
            }

            return array.getJSONObject(0);

        } finally {

            if (connection != null) {
                connection.disconnect();
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

            if (code < 200
                    || code >= 300) {
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
    // PARSE CREATED AT
    // =========================================================

    private static long parseCreatedAt(String value) {

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
    // READ URI
    // =========================================================

    private static byte[] readUriBytes(
            Context context,
            Uri uri)
            throws Exception {

        InputStream input =
                context.getContentResolver()
                        .openInputStream(uri);

        if (input == null) {
            return null;
        }

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        byte[] buffer =
                new byte[16 * 1024];

        int read;

        while ((read =
                input.read(buffer)) != -1) {

            output.write(
                    buffer,
                    0,
                    read
            );
        }

        input.close();

        return output.toByteArray();
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
                120000
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
                                "UTF-8"
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

    private static void postError(
            ReelsCallback callback,
            String message) {

        if (callback == null) return;

        MAIN_HANDLER.post(() ->
                callback.onError(
                        message
                )
        );
    }

    private static void postUploadError(
            UploadCallback callback,
            String message) {

        if (callback == null) return;

        MAIN_HANDLER.post(() ->
                callback.onError(
                        message
                )
        );
    }

    private static void postProgress(
            UploadCallback callback,
            int progress) {

        if (callback == null) return;

        MAIN_HANDLER.post(() ->
                callback.onProgress(
                        Math.max(
                                0,
                                Math.min(
                                        100,
                                        progress
                                )
                        )
                )
        );
    }

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
                callback.onError(
                        message
                )
        );
    }

    private static void postLikeError(
            LikeCheckCallback callback,
            String message) {

        if (callback == null) return;

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
