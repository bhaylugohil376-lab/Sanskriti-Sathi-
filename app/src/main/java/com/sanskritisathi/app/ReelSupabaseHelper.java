package com.sanskritisathi.app;

import android.net.Uri;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ReelSupabaseHelper {

    // ============================================================
    // CALLBACKS
    // ============================================================

    public interface UploadCallback {
        void onProgress(int progress);
        void onSuccess(String reelId);
        void onError(String message);
    }

    public interface ReelsCallback {
        void onSuccess(List<Reel> reels);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    // ============================================================
    // CONSTANTS
    // ============================================================

    private static final String PROJECT_URL =
            SupabaseConfig.PROJECT_URL;

    private static final String API_KEY =
            SupabaseConfig.PUBLISHABLE_KEY;

    private static final String BRIGHT_ACTION =
            PROJECT_URL + "/functions/v1/bright-action";

    private static final String PROFILES_TABLE =
            PROJECT_URL + "/rest/v1/profiles";

    private static final String REELS_TABLE =
            PROJECT_URL + "/rest/v1/reels";

    private static final String REEL_LIKES_TABLE =
            PROJECT_URL + "/rest/v1/reel_likes";

    private static final String REEL_VIEWS_TABLE =
            PROJECT_URL + "/rest/v1/reel_views";

    private static final String REEL_COMMENTS_TABLE =
            PROJECT_URL + "/rest/v1/reel_comments";

    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();

    private ReelSupabaseHelper() {
    }

    // ============================================================
    // AUTH
    // ============================================================

    private static String getUserId() {

        String userId =
                SupabaseAuthManager.getUserId();

        if (userId == null ||
                userId.trim().isEmpty()) {
            return null;
        }

        return userId;
    }

    private static String getAccessToken() {

        String token =
                SupabaseAuthManager.getAccessToken();

        if (token == null ||
                token.trim().isEmpty()) {
            return null;
        }

        return token;
    }

    private static String getUsername() {

        String userId = getUserId();

        if (userId == null) {
            return "Sanskriti User";
        }

        HttpURLConnection connection = null;

        try {

            String url =
                    PROFILES_TABLE
                            + "?id=eq."
                            + encode(userId)
                            + "&select=username,name";

            connection =
                    openConnection(
                            url,
                            "GET"
                    );

            int code =
                    connection.getResponseCode();

            String response =
                    readResponse(connection, code);

            if (code >= 200 &&
                    code < 300) {

                JSONArray array =
                        new JSONArray(response);

                if (array.length() > 0) {

                    JSONObject object =
                            array.getJSONObject(0);

                    String username =
                            object.optString(
                                    "username",
                                    ""
                            );

                    if (!username.trim().isEmpty()) {
                        return username;
                    }

                    String name =
                            object.optString(
                                    "name",
                                    ""
                            );

                    if (!name.trim().isEmpty()) {
                        return name;
                    }
                }
            }

        } catch (Exception ignored) {

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }

        return "Sanskriti User";
    }

    // ============================================================
    // UPLOAD REEL
    // ============================================================

    public static void uploadReel(
            Uri videoUri,
            String caption,
            String visibility,
            UploadCallback callback) {

        if (getUserId() == null) {
            callback.onError("Please login first.");
            return;
        }

        if (getAccessToken() == null) {
            callback.onError("Login session nahi mili.");
            return;
        }

        if (videoUri == null) {
            callback.onError("Please select a video.");
            return;
        }

        EXECUTOR.execute(() -> {

            try {

                String uid =
                        getUserId();

                String reelId =
                        UUID.randomUUID().toString();

                callback.onProgress(5);

                byte[] videoBytes =
                        readUriBytes(videoUri);

                if (videoBytes == null ||
                        videoBytes.length == 0) {

                    callback.onError(
                            "Video read nahi ho paaya."
                    );
                    return;
                }

                callback.onProgress(15);

                String base64 =
                        Base64.encodeToString(
                                videoBytes,
                                Base64.NO_WRAP
                        );

                String fileName =
                        "reel_"
                                + uid
                                + "_"
                                + reelId
                                + ".mp4";

                JSONObject body =
                        new JSONObject();

                body.put(
                        "fileName",
                        fileName
                );

                body.put(
                        "fileBase64",
                        base64
                );

                body.put(
                        "folder",
                        "reels"
                );

                body.put(
                        "contentType",
                        "video/mp4"
                );

                JSONObject uploadResponse =
                        postBrightAction(body);

                boolean success =
                        uploadResponse.optBoolean(
                                "success",
                                false
                        );

                if (!success) {

                    callback.onError(
                            uploadResponse.optString(
                                    "message",
                                    "Video upload failed."
                            )
                    );
                    return;
                }

                String b2FileName =
                        uploadResponse.optString(
                                "fileName",
                                fileName
                        );

                callback.onProgress(80);

                String username =
                        getUsername();

                saveReel(
                        reelId,
                        uid,
                        username,
                        b2FileName,
                        caption,
                        visibility,
                        callback
                );

            } catch (Exception e) {

                callback.onError(
                        "Video upload failed: "
                                + safeMessage(e)
                );
            }
        });
    }

    // ============================================================
    // SAVE REEL
    // ============================================================

    private static void saveReel(
            String reelId,
            String uid,
            String username,
            String videoFileName,
            String caption,
            String visibility,
            UploadCallback callback) {

        HttpURLConnection connection = null;

        try {

            JSONObject reel =
                    new JSONObject();

            reel.put(
                    "reel_id",
                    reelId
            );

            reel.put(
                    "owner_uid",
                    uid
            );

            reel.put(
                    "username",
                    username == null
                            ? "Sanskriti User"
                            : username
            );

            // B2 filename/reference.
            reel.put(
                    "video_url",
                    videoFileName
            );

            reel.put(
                    "thumbnail_url",
                    ""
            );

            reel.put(
                    "caption",
                    caption == null
                            ? ""
                            : caption.trim()
            );

            reel.put(
                    "visibility",
                    "Followers".equalsIgnoreCase(
                            visibility
                    )
                            ? "Followers"
                            : "Public"
            );

            reel.put(
                    "created_at",
                    System.currentTimeMillis()
            );

            reel.put(
                    "likes",
                    0
            );

            reel.put(
                    "comments",
                    0
            );

            reel.put(
                    "views",
                    0
            );

            connection =
                    openConnection(
                            REELS_TABLE,
                            "POST"
                    );

            connection.setRequestProperty(
                    "Prefer",
                    "return=representation"
            );

            writeBody(
                    connection,
                    reel.toString()
            );

            int code =
                    connection.getResponseCode();

            String response =
                    readResponse(connection, code);

            if (code >= 200 &&
                    code < 300) {

                callback.onProgress(100);
                callback.onSuccess(reelId);

            } else {

                callback.onError(
                        "Reel save failed: "
                                + response
                );
            }

        } catch (Exception e) {

            callback.onError(
                    "Reel save failed: "
                            + safeMessage(e)
            );

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // ============================================================
    // GET ACTIVE REELS
    // ============================================================

    public static void getActiveReels(
            ReelsCallback callback) {

        if (getUserId() == null) {
            callback.onError("Please login first.");
            return;
        }

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                long twentyFourHoursAgo =
                        System.currentTimeMillis()
                                - (24L * 60L * 60L * 1000L);

                String url =
                        REELS_TABLE
                                + "?created_at=gte."
                                + twentyFourHoursAgo
                                + "&order=created_at.desc";

                connection =
                        openConnection(
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

                if (code < 200 ||
                        code >= 300) {

                    callback.onError(
                            "Reels load failed: "
                                    + response
                    );
                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                List<Reel> reels =
                        new ArrayList<>();

                String currentUid =
                        getUserId();

                for (int i = 0;
                     i < array.length();
                     i++) {

                    JSONObject object =
                            array.getJSONObject(i);

                    Reel reel =
                            parseReel(
                                    object,
                                    currentUid
                            );

                    if (reel != null) {
                        reels.add(reel);
                    }
                }

                Collections.sort(
                        reels,
                        (a, b) ->
                                Long.compare(
                                        b.getCreatedAt(),
                                        a.getCreatedAt()
                                )
                );

                callback.onSuccess(reels);

            } catch (Exception e) {

                callback.onError(
                        "Reels load failed: "
                                + safeMessage(e)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // ============================================================
    // PARSE REEL
    // ============================================================

    private static Reel parseReel(
            JSONObject object,
            String currentUid) {

        try {

            String id =
                    object.optString(
                            "reel_id",
                            object.optString(
                                    "reelId",
                                    object.optString(
                                            "id",
                                            ""
                                    )
                            )
                    );

            String ownerUid =
                    object.optString(
                            "owner_uid",
                            object.optString(
                                    "ownerUid",
                                    ""
                            )
                    );

            String username =
                    object.optString(
                            "username",
                            "Sanskriti User"
                    );

            String videoUrl =
                    object.optString(
                            "video_url",
                            object.optString(
                                    "videoUrl",
                                    ""
                            )
                    );

            String thumbnailUrl =
                    object.optString(
                            "thumbnail_url",
                            object.optString(
                                    "thumbnailUrl",
                                    ""
                            )
                    );

            String caption =
                    object.optString(
                            "caption",
                            ""
                    );

            String visibility =
                    object.optString(
                            "visibility",
                            "Public"
                    );

            long createdAt =
                    object.optLong(
                            "created_at",
                            object.optLong(
                                    "createdAt",
                                    0
                            )
                    );

            int likes =
                    object.optInt(
                            "likes",
                            0
                    );

            int comments =
                    object.optInt(
                            "comments",
                            0
                    );

            int views =
                    object.optInt(
                            "views",
                            0
                    );

            boolean ownReel =
                    currentUid != null &&
                            currentUid.equals(
                                    ownerUid
                            );

            return new Reel(
                    id,
                    ownerUid,
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

        } catch (Exception e) {

            return null;
        }
    }

    // ============================================================
    // ADD UNIQUE VIEW
    // ============================================================

    public static void addReelView(
            String reelId,
            ActionCallback callback) {

        if (getUserId() == null) {
            callback.onError("Please login first.");
            return;
        }

        if (reelId == null ||
                reelId.trim().isEmpty()) {

            callback.onError("Invalid Reel.");
            return;
        }

        EXECUTOR.execute(() -> {

            try {

                String uid =
                        getUserId();

                String checkUrl =
                        REEL_VIEWS_TABLE
                                + "?reel_id=eq."
                                + encode(reelId)
                                + "&user_id=eq."
                                + encode(uid)
                                + "&select=id";

                JSONArray existing =
                        getJsonArray(checkUrl);

                if (existing.length() > 0) {
                    callback.onSuccess();
                    return;
                }

                JSONObject view =
                        new JSONObject();

                view.put(
                        "reel_id",
                        reelId
                );

                view.put(
                        "user_id",
                        uid
                );

                view.put(
                        "created_at",
                        System.currentTimeMillis()
                );

                postJson(
                        REEL_VIEWS_TABLE,
                        view
                );

                incrementCounter(
                        reelId,
                        "views"
                );

                callback.onSuccess();

            } catch (Exception e) {

                callback.onError(
                        "View update failed: "
                                + safeMessage(e)
                );
            }
        });
    }

    // ============================================================
    // TOGGLE LIKE
    // ============================================================

    public static void toggleReelLike(
            String reelId,
            boolean currentlyLiked,
            ActionCallback callback) {

        if (getUserId() == null) {
            callback.onError("Please login first.");
            return;
        }

        if (reelId == null ||
                reelId.trim().isEmpty()) {

            callback.onError("Invalid Reel.");
            return;
        }

        EXECUTOR.execute(() -> {

            try {

                String uid =
                        getUserId();

                String checkUrl =
                        REEL_LIKES_TABLE
                                + "?reel_id=eq."
                                + encode(reelId)
                                + "&user_id=eq."
                                + encode(uid)
                                + "&select=id";

                JSONArray existing =
                        getJsonArray(checkUrl);

                boolean liked =
                        existing.length() > 0;

                if (currentlyLiked ||
                        liked) {

                    deleteJson(
                            REEL_LIKES_TABLE
                                    + "?reel_id=eq."
                                    + encode(reelId)
                                    + "&user_id=eq."
                                    + encode(uid)
                    );

                    decrementCounter(
                            reelId,
                            "likes"
                    );

                } else {

                    JSONObject like =
                            new JSONObject();

                    like.put(
                            "reel_id",
                            reelId
                    );

                    like.put(
                            "user_id",
                            uid
                    );

                    like.put(
                            "created_at",
                            System.currentTimeMillis()
                    );

                    postJson(
                            REEL_LIKES_TABLE,
                            like
                    );

                    incrementCounter(
                            reelId,
                            "likes"
                    );
                }

                callback.onSuccess();

            } catch (Exception e) {

                callback.onError(
                        "Like update failed: "
                                + safeMessage(e)
                );
            }
        });
    }

    // ============================================================
    // CHECK LIKE
    // ============================================================

    public static void checkReelLike(
            String reelId,
            ActionCallback callback) {

        if (getUserId() == null) {
            callback.onError("Please login first.");
            return;
        }

        EXECUTOR.execute(() -> {

            try {

                String url =
                        REEL_LIKES_TABLE
                                + "?reel_id=eq."
                                + encode(reelId)
                                + "&user_id=eq."
                                + encode(getUserId())
                                + "&select=id";

                JSONArray result =
                        getJsonArray(url);

                if (result.length() > 0) {
                    callback.onSuccess();
                } else {
                    callback.onError(
                            "NOT_LIKED"
                    );
                }

            } catch (Exception e) {

                callback.onError(
                        "Like check failed: "
                                + safeMessage(e)
                );
            }
        });
    }

    // ============================================================
    // DELETE REEL
    // ============================================================

    public static void deleteReel(
            String reelId,
            ActionCallback callback) {

        if (getUserId() == null) {
            callback.onError("Please login first.");
            return;
        }

        if (reelId == null ||
                reelId.trim().isEmpty()) {

            callback.onError("Invalid Reel.");
            return;
        }

        EXECUTOR.execute(() -> {

            try {

                String url =
                        REELS_TABLE
                                + "?reel_id=eq."
                                + encode(reelId)
                                + "&owner_uid=eq."
                                + encode(getUserId());

                deleteJson(url);

                callback.onSuccess();

            } catch (Exception e) {

                callback.onError(
                        "Reel delete failed: "
                                + safeMessage(e)
                );
            }
        });
    }

    // ============================================================
    // B2 VIDEO URL
    // ============================================================

    public static void getReelVideoUrl(
            String fileName,
            ActionCallback callback) {

        if (fileName == null ||
                fileName.trim().isEmpty()) {

            callback.onError(
                    "Invalid video."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            try {

                JSONObject body =
                        new JSONObject();

                body.put(
                        "action",
                        "get_reel_video"
                );

                body.put(
                        "fileName",
                        fileName
                );

                JSONObject response =
                        postBrightAction(body);

                boolean success =
                        response.optBoolean(
                                "success",
                                false
                        );

                if (!success) {

                    callback.onError(
                            response.optString(
                                    "message",
                                    "Video URL failed."
                            )
                    );
                    return;
                }

                /*
                 * This callback API is kept compatible
                 * with existing ActionCallback.
                 *
                 * If ReelActivity directly uses videoUrl,
                 * use the Bright Action response there.
                 */

                callback.onSuccess();

            } catch (Exception e) {

                callback.onError(
                        "Video load failed: "
                                + safeMessage(e)
                );
            }
        });
    }

    // ============================================================
    // COUNTER UPDATE
    // ============================================================

    private static void incrementCounter(
            String reelId,
            String column) {

        updateCounter(
                reelId,
                column,
                true
        );
    }

    private static void decrementCounter(
            String reelId,
            String column) {

        updateCounter(
                reelId,
                column,
                false
        );
    }

    private static void updateCounter(
            String reelId,
            String column,
            boolean increment) {

        try {

            String url =
                    REELS_TABLE
                            + "?reel_id=eq."
                            + encode(reelId)
                            + "&select="
                            + column;

            JSONArray array =
                    getJsonArray(url);

            if (array.length() == 0) {
                return;
            }

            JSONObject row =
                    array.getJSONObject(0);

            int current =
                    row.optInt(
                            column,
                            0
                    );

            int value;

            if (increment) {
                value = current + 1;
            } else {
                value = Math.max(
                        0,
                        current - 1
                );
            }

            JSONObject update =
                    new JSONObject();

            update.put(
                    column,
                    value
            );

            patchJson(
                    REELS_TABLE
                            + "?reel_id=eq."
                            + encode(reelId),
                    update
            );

        } catch (Exception ignored) {
        }
    }

    // ============================================================
    // BRIGHT ACTION
    // ============================================================

    private static JSONObject postBrightAction(
            JSONObject body) throws Exception {

        HttpURLConnection connection =
                openConnection(
                        BRIGHT_ACTION,
                        "POST"
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

        connection.disconnect();

        if (code < 200 ||
                code >= 300) {

            throw new Exception(
                    response
            );
        }

        return new JSONObject(
                response
        );
    }

    // ============================================================
    // HTTP GET JSON
    // ============================================================

    private static JSONArray getJsonArray(
            String urlString) throws Exception {

        HttpURLConnection connection =
                openConnection(
                        urlString,
                        "GET"
                );

        int code =
                connection.getResponseCode();

        String response =
                readResponse(
                        connection,
                        code
                );

        connection.disconnect();

        if (code < 200 ||
                code >= 300) {

            throw new Exception(
                    response
            );
        }

        return new JSONArray(
                response
        );
    }

    // ============================================================
    // HTTP POST JSON
    // ============================================================

    private static void postJson(
            String urlString,
            JSONObject body) throws Exception {

        HttpURLConnection connection =
                openConnection(
                        urlString,
                        "POST"
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

        connection.disconnect();

        if (code < 200 ||
                code >= 300) {

            throw new Exception(
                    response
            );
        }
    }

    // ============================================================
    // HTTP PATCH JSON
    // ============================================================

    private static void patchJson(
            String urlString,
            JSONObject body) throws Exception {

        HttpURLConnection connection =
                openConnection(
                        urlString,
                        "PATCH"
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

        connection.disconnect();

        if (code < 200 ||
                code >= 300) {

            throw new Exception(
                    response
            );
        }
    }

    // ============================================================
    // HTTP DELETE
    // ============================================================

    private static void deleteJson(
            String urlString) throws Exception {

        HttpURLConnection connection =
                openConnection(
                        urlString,
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

        connection.disconnect();

        if (code < 200 ||
                code >= 300) {

            throw new Exception(
                    response
            );
        }
    }

    // ============================================================
    // CONNECTION
    // ============================================================

    private static HttpURLConnection openConnection(
            String urlString,
            String method) throws Exception {

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
                "Accept",
                "application/json"
        );

        connection.setRequestProperty(
                "apikey",
                API_KEY
        );

        String token =
                getAccessToken();

        if (token != null) {

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token
            );
        }

        if ("POST".equals(method) ||
                "PATCH".equals(method)) {

            connection.setDoOutput(true);

            connection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );
        }

        return connection;
    }

    // ============================================================
    // WRITE BODY
    // ============================================================

    private static void writeBody(
            HttpURLConnection connection,
            String body) throws Exception {

        byte[] bytes =
                body.getBytes(
                        "UTF-8"
                );

        OutputStream output =
                connection.getOutputStream();

        output.write(bytes);
        output.flush();
        output.close();
    }

    // ============================================================
    // READ RESPONSE
    // ============================================================

    private static String readResponse(
            HttpURLConnection connection,
            int responseCode) {

        try {

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

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            byte[] buffer =
                    new byte[8192];

            int length;

            while ((length =
                    input.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        length
                );
            }

            input.close();

            return output.toString(
                    "UTF-8"
            );

        } catch (Exception e) {

            return e.getMessage() == null
                    ? ""
                    : e.getMessage();
        }
    }

    // ============================================================
    // READ URI
    // ============================================================

    private static byte[] readUriBytes(
            Uri uri) throws Exception {

        android.content.Context context =
                AppContext.get();

        InputStream input =
                context.getContentResolver()
                        .openInputStream(uri);

        if (input == null) {
            throw new Exception(
                    "Video open nahi hua."
            );
        }

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        byte[] buffer =
                new byte[16 * 1024];

        int length;

        while ((length =
                input.read(buffer)) != -1) {

            output.write(
                    buffer,
                    0,
                    length
            );
        }

        input.close();

        return output.toByteArray();
    }

    // ============================================================
    // URL ENCODE
    // ============================================================

    private static String encode(
            String value) {

        try {

            return java.net.URLEncoder
                    .encode(
                            value,
                            "UTF-8"
                    );

        } catch (Exception e) {

            return value;
        }
    }

    // ============================================================
    // ERROR
    // ============================================================

    private static String safeMessage(
            Exception e) {

        String message =
                e.getMessage();

        if (message == null ||
                message.trim().isEmpty()) {

            return e.getClass()
                    .getSimpleName();
        }

        return message;
    }
}
