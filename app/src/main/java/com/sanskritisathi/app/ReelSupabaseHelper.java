package com.sanskritisathi.app;

import android.content.Context;
import android.net.Uri;
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

    /*
     * IMPORTANT:
     * Supabase Storage bucket name must be exactly "reels"
     */
    private static final String STORAGE_BUCKET = "reels";

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
    // GET REELS
    // =========================================================

    public static void getActiveReels(
            Context context,
            ReelsCallback callback) {

        if (context == null) {
            postError(callback, "Context missing.");
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(userId)
                        || TextUtils.isEmpty(token)) {

                    postError(
                            callback,
                            "Login session nahi mili."
                    );
                    return;
                }

                /*
                 * Public reels + current user's reels
                 */
                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + REELS_TABLE
                                + "?select=*"
                                + "&or=("
                                + "visibility.eq.Public,"
                                + "user_id.eq."
                                + encode(userId)
                                + ")"
                                + "&order=created_at.desc";

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

                    postError(
                            callback,
                            "Reels load failed: " + response
                    );
                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                List<Reel> reels =
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
                            Math.max(
                                    0,
                                    json.optInt(
                                            "likes",
                                            0
                                    )
                            );

                    int comments =
                            Math.max(
                                    0,
                                    json.optInt(
                                            "comments",
                                            0
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

                    String username =
                            json.optString(
                                    "username",
                                    ""
                            );

                    /*
                     * If reels table doesn't have username,
                     * get it from profiles.
                     */
                    if (TextUtils.isEmpty(username)
                            && !TextUtils.isEmpty(reelUserId)) {

                        username =
                                getUsername(
                                        reelUserId,
                                        token
                                );
                    }

                    if (TextUtils.isEmpty(username)) {
                        username = "Sanskriti User";
                    }

                    String thumbnailUrl =
                            json.optString(
                                    "thumbnail_url",
                                    ""
                            );

                    long createdAt =
                            parseCreatedAt(
                                    json.optString(
                                            "created_at",
                                            ""
                                    )
                            );

                    boolean ownReel =
                            userId.equals(reelUserId);

                    /*
                     * Important:
                     * Default liked = false.
                     * Adapter can call checkReelLike().
                     */
                    Reel reel =
                            new Reel(
                                    id,
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

                    reels.add(reel);
                }

                MAIN_HANDLER.post(() ->
                        callback.onSuccess(reels)
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
    // DIRECT SUPABASE STORAGE
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

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(userId)
                        || TextUtils.isEmpty(token)) {

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

                String extension =
                        getVideoExtension(
                                context,
                                videoUri
                        );

                String fileName =
                        "reel_"
                                + userId
                                + "_"
                                + System.currentTimeMillis()
                                + "."
                                + extension;

                String storagePath =
                        fileName;

                String uploadUrl =
                        SupabaseConfig.PROJECT_URL
                                + "/storage/v1/object/"
                                + STORAGE_BUCKET
                                + "/"
                                + encodePath(storagePath);

                connection =
                        openConnection(
                                uploadUrl,
                                "POST",
                                token
                        );

                connection.setDoOutput(true);

                connection.setFixedLengthStreamingMode(
                        getFileSize(
                                context,
                                videoUri
                        )
                );

                connection.setRequestProperty(
                        "Content-Type",
                        getMimeType(
                                context,
                                videoUri
                        )
                );

                connection.setRequestProperty(
                        "x-upsert",
                        "true"
                );

                connection.setRequestProperty(
                        "Cache-Control",
                        "3600"
                );

                postProgress(
                        callback,
                        10
                );

                InputStream input =
                        context.getContentResolver()
                                .openInputStream(videoUri);

                if (input == null) {

                    postUploadError(
                            callback,
                            "Video read nahi ho saki."
                    );
                    return;
                }

                OutputStream output =
                        connection.getOutputStream();

                byte[] buffer =
                        new byte[32 * 1024];

                int read;
                long total = 0;

                long fileSize =
                        getFileSize(
                                context,
                                videoUri
                        );

                while ((read =
                        input.read(buffer)) != -1) {

                    output.write(
                            buffer,
                            0,
                            read
                    );

                    total += read;

                    if (fileSize > 0) {

                        int progress =
                                10
                                        + (int)
                                        ((total * 75L)
                                                / fileSize);

                        postProgress(
                                callback,
                                Math.min(
                                        85,
                                        progress
                                )
                        );
                    }
                }

                output.flush();
                output.close();
                input.close();

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
                            "Video upload failed.\nHTTP "
                                    + code
                                    + "\n"
                                    + response
                    );
                    return;
                }

                postProgress(
                        callback,
                        88
                );

                /*
                 * Public bucket URL.
                 */
                String videoUrl =
                        SupabaseConfig.PROJECT_URL
                                + "/storage/v1/object/public/"
                                + STORAGE_BUCKET
                                + "/"
                                + encodePath(storagePath);

                /*
                 * Save reel record.
                 */
                insertReel(
                        userId,
                        caption,
                        visibility,
                        videoUrl,
                        token
                );

                postProgress(
                        callback,
                        100
                );

                final String finalUrl =
                        videoUrl;

                MAIN_HANDLER.post(() ->
                        callback.onSuccess(
                                finalUrl
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
            String token)
            throws Exception {

        HttpURLConnection connection = null;

        try {

            JSONObject json =
                    new JSONObject();

            json.put(
                    "id",
                    UUID.randomUUID().toString()
            );

            json.put(
                    "user_id",
                    userId
            );

            json.put(
                    "video_url",
                    videoUrl
            );

            json.put(
                    "caption",
                    caption == null
                            ? ""
                            : caption
            );

            json.put(
                    "visibility",
                    TextUtils.isEmpty(
                            visibility
                    )
                            ? "Public"
                            : visibility
            );

            json.put(
                    "likes",
                    0
            );

            json.put(
                    "comments",
                    0
            );

            json.put(
                    "views",
                    0
            );

            String url =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/"
                            + REELS_TABLE;

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

            if (code < 200
                    || code >= 300) {

                throw new Exception(
                        "Reel database save failed.\n"
                                + "HTTP "
                                + code
                                + "\n"
                                + response
                );
            }

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
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
                                .getAccessToken(
                                        context
                                );

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
                                + encode(reelId);

                connection =
                        openConnection(
                                url,
                                "PATCH",
                                token
                        );

                connection.setDoOutput(
                        true
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                OutputStream output =
                        connection.getOutputStream();

                output.write(
                        update.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
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
                                + encode(reelId)
                                + "&user_id=eq."
                                + encode(userId);

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
                            "Like check failed: "
                                    + response
                    );
                    return;
                }

                JSONArray array =
                        new JSONArray(
                                response
                        );

                boolean liked =
                        array.length() > 0;

                final boolean finalLiked =
                        liked;

                MAIN_HANDLER.post(() ->
                        callback.onResult(
                                finalLiked
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
                                    + encode(reelId)
                                    + "&user_id=eq."
                                    + encode(userId);

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

                /*
                 * Recalculate count.
                 */
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

    /*
     * Compatibility overload.
     *
     * If your current ReelAdapter has:
     *
     * ReelSupabaseHelper.toggleReelLike(
     *     context,
     *     reelId,
     *     callback
     * );
     *
     * this method will first check current like state.
     */
    public static void toggleReelLike(
            Context context,
            String reelId,
            ActionCallback callback) {

        checkReelLike(
                context,
                reelId,
                new LikeCheckCallback() {

                    @Override
                    public void onResult(
                            boolean liked) {

                        toggleReelLike(
                                context,
                                reelId,
                                liked,
                                callback
                        );
                    }

                    @Override
                    public void onError(
                            String message) {

                        postActionError(
                                callback,
                                message
                        );
                    }
                }
        );
    }

    // =========================================================
    // UPDATE LIKE COUNT
    // =========================================================

    private static void updateLikeCount(
            String reelId,
            String token) {

        HttpURLConnection countConnection =
                null;

        HttpURLConnection patchConnection =
                null;

        try {

            String countUrl =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/"
                            + LIKES_TABLE
                            + "?select=id"
                            + "&reel_id=eq."
                            + encode(reelId);

            countConnection =
                    openConnection(
                            countUrl,
                            "GET",
                            token
                    );

            int code =
                    countConnection
                            .getResponseCode();

            String response =
                    readResponse(
                            countConnection,
                            code
                    );

            if (code < 200
                    || code >= 300) {
                return;
            }

            JSONArray likes =
                    new JSONArray(
                            response
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
                            + encode(reelId);

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

            HttpURLConnection connection =
                    null;

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
                                + encode(reelId)
                                + "&user_id=eq."
                                + encode(userId);

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

                if (code < 200
                        || code >= 300) {

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
    // UPDATE REEL
    // =========================================================

    public static void updateReel(
            Context context,
            String reelId,
            String caption,
            String visibility,
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

            HttpURLConnection connection =
                    null;

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

                JSONObject update =
                        new JSONObject();

                update.put(
                        "caption",
                        caption == null
                                ? ""
                                : caption
                );

                update.put(
                        "visibility",
                        TextUtils.isEmpty(
                                visibility
                        )
                                ? "Public"
                                : visibility
                );

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/"
                                + REELS_TABLE
                                + "?id=eq."
                                + encode(reelId)
                                + "&user_id=eq."
                                + encode(userId);

                connection =
                        openConnection(
                                url,
                                "PATCH",
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
                        update.toString()
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

                if (code < 200
                        || code >= 300) {

                    postActionError(
                            callback,
                            "Reel update failed: "
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
                                "Reel update failed."
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
    // GET SINGLE REEL
    // =========================================================

    private static JSONObject getReel(
            String reelId,
            String token)
            throws Exception {

        HttpURLConnection connection =
                null;

        try {

            String url =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/"
                            + REELS_TABLE
                            + "?select=*"
                            + "&id=eq."
                            + encode(reelId)
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
                    new JSONArray(
                            response
                    );

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

        HttpURLConnection connection =
                null;

        try {

            String url =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/profiles"
                            + "?select=username"
                            + "&id=eq."
                            + encode(userId)
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
                    new JSONArray(
                            response
                    );

            if (array.length() == 0) {
                return "";
            }

            return array
                    .getJSONObject(0)
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
    // VIDEO MIME TYPE
    // =========================================================

    private static String getMimeType(
            Context context,
            Uri uri) {

        String type =
                context.getContentResolver()
                        .getType(uri);

        if (TextUtils.isEmpty(type)) {
            return "video/mp4";
        }

        return type;
    }

    // =========================================================
    // VIDEO EXTENSION
    // =========================================================

    private static String getVideoExtension(
            Context context,
            Uri uri) {

        String mime =
                getMimeType(
                        context,
                        uri
                );

        if ("video/webm".equalsIgnoreCase(mime)) {
            return "webm";
        }

        if ("video/3gpp".equalsIgnoreCase(mime)) {
            return "3gp";
        }

        if ("video/quicktime".equalsIgnoreCase(mime)) {
            return "mov";
        }

        return "mp4";
    }

    // =========================================================
    // FILE SIZE
    // =========================================================

    private static long getFileSize(
            Context context,
            Uri uri) {

        android.database.Cursor cursor =
                null;

        try {

            cursor =
                    context.getContentResolver()
                            .query(
                                    uri,
                                    new String[]{
                                            android.provider.OpenableColumns.SIZE
                                    },
                                    null,
                                    null,
                                    null
                            );

            if (cursor != null
                    && cursor.moveToFirst()) {

                int index =
                        cursor.getColumnIndex(
                                android.provider.OpenableColumns.SIZE
                        );

                if (index >= 0
                        && !cursor.isNull(index)) {

                    return cursor.getLong(index);
                }
            }

        } catch (Exception ignored) {

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return -1L;
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

        connection.setRequestMethod(
                method
        );

        connection.setConnectTimeout(
                30000
        );

        connection.setReadTimeout(
                180000
        );

        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );

        if (!TextUtils.isEmpty(token)) {

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token
            );
        }

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        return connection;
    }

    // =========================================================
    // READ RESPONSE
    // =========================================================

    private static String readResponse(
            HttpURLConnection connection,
            int code)
            throws Exception {

        InputStream input;

        if (code >= 400) {

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
    // URL ENCODE
    // =========================================================

    private static String encode(
            String value) {

        try {

            return URLEncoder
                    .encode(
                            value,
                            "UTF-8"
                    )
                    .replace(
                            "+",
                            "%20"
                    );

        } catch (Exception e) {

            return value;
        }
    }

    private static String encodePath(
            String value) {

        String[] parts =
                value.split("/");

        StringBuilder result =
                new StringBuilder();

        for (int i = 0;
             i < parts.length;
             i++) {

            if (i > 0) {
                result.append("/");
            }

            result.append(
                    encode(parts[i])
            );
        }

        return result.toString();
    }

    // =========================================================
    // CALLBACK HELPERS
    // =========================================================

    private static void postError(
            ReelsCallback callback,
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

    private static void postUploadError(
            UploadCallback callback,
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

    private static void postProgress(
            UploadCallback callback,
            int progress) {

        if (callback == null) {
            return;
        }

        int safeProgress =
                Math.max(
                        0,
                        Math.min(
                                100,
                                progress
                        )
                );

        MAIN_HANDLER.post(() ->
                callback.onProgress(
                        safeProgress
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

    private static void postLikeError(
            LikeCheckCallback callback,
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

    // =========================================================
    // SAFE ERROR
    // =========================================================

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
