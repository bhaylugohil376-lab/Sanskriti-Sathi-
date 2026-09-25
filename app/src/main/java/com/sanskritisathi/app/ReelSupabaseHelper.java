package com.sanskritisathi.app;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReelSupabaseHelper {

    private static final String TAG = "ReelSupabaseHelper";

    private static final String SUPABASE_URL =
            SupabaseConfig.PROJECT_URL;

    private static final String BRIGHT_ACTION_URL =
            SUPABASE_URL + "/functions/v1/bright-action";

    private static final String REELS_TABLE =
            SUPABASE_URL + "/rest/v1/reels";

    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();


    // ============================================================
    // CALLBACKS
    // ============================================================

    public interface UploadCallback {
        void onSuccess(String videoUrl, String fileName);

        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess();

        void onError(String error);
    }

    public interface ReelsCallback {
        void onSuccess(java.util.List<Reel> reels);

        void onError(String error);
    }

    public interface ReelCallback {
        void onSuccess(Reel reel);

        void onError(String error);
    }

    public interface BooleanCallback {
        void onResult(boolean value);

        void onError(String error);
    }


    // ============================================================
    // UPLOAD REEL VIDEO
    // ============================================================

    public static void uploadReel(
            Context context,
            Uri videoUri,
            String caption,
            String visibility,
            UploadCallback callback
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                if (videoUri == null) {
                    postError(callback, "Video select nahi hua.");
                    return;
                }

                if (context == null) {
                    postError(callback, "Context missing hai.");
                    return;
                }

                InputStream inputStream =
                        context.getContentResolver()
                                .openInputStream(videoUri);

                if (inputStream == null) {
                    postError(callback, "Video read nahi ho pa raha.");
                    return;
                }

                byte[] videoBytes =
                        readAllBytes(inputStream);

                inputStream.close();

                if (videoBytes.length == 0) {
                    postError(callback, "Video empty hai.");
                    return;
                }

                /*
                 * Current bright-action function expects:
                 *
                 * action
                 * fileName
                 * fileBase64
                 * folder
                 * contentType
                 */

                String extension =
                        getExtension(context, videoUri);

                if (extension == null ||
                        extension.trim().isEmpty()) {

                    extension = "mp4";
                }

                extension =
                        extension.toLowerCase()
                                .replace(".", "");

                String fileName =
                        "reel_" +
                        UUID.randomUUID().toString() +
                        "." +
                        extension;

                String base64 =
                        Base64.encodeToString(
                                videoBytes,
                                Base64.NO_WRAP
                        );

                JSONObject uploadBody =
                        new JSONObject();

                uploadBody.put(
                        "action",
                        "upload"
                );

                uploadBody.put(
                        "fileName",
                        fileName
                );

                uploadBody.put(
                        "fileBase64",
                        base64
                );

                uploadBody.put(
                        "folder",
                        "reels"
                );

                uploadBody.put(
                        "contentType",
                        getContentType(extension)
                );

                connection =
                        openPostConnection(
                                BRIGHT_ACTION_URL
                        );

                writeJson(
                        connection,
                        uploadBody.toString()
                );

                int responseCode =
                        connection.getResponseCode();

                String response =
                        readResponse(connection);

                if (responseCode < 200 ||
                        responseCode >= 300) {

                    postError(
                            callback,
                            "Upload failed (" +
                                    responseCode +
                                    "): " +
                                    extractError(response)
                    );

                    return;
                }

                JSONObject json =
                        new JSONObject(response);

                boolean success =
                        json.optBoolean(
                                "success",
                                false
                        );

                if (!success) {

                    postError(
                            callback,
                            json.optString(
                                    "error",
                                    "B2 upload failed."
                            )
                    );

                    return;
                }

                String downloadUrl =
                        json.optString(
                                "downloadUrl",
                                ""
                        );

                String returnedFileName =
                        json.optString(
                                "fileName",
                                ""
                        );

                if (returnedFileName.isEmpty()) {
                    returnedFileName = fileName;
                }

                /*
                 * downloadUrl public B2 URL ho sakta hai.
                 * Agar function URL return karta hai,
                 * wahi use karenge.
                 */

                if (downloadUrl == null ||
                        downloadUrl.trim().isEmpty()) {

                    postError(
                            callback,
                            "Upload successful hai, lekin downloadUrl nahi mila."
                    );

                    return;
                }

                final String finalUrl =
                        downloadUrl;

                final String finalFileName =
                        returnedFileName;

                postSuccess(
                        callback,
                        finalUrl,
                        finalFileName
                );

            } catch (Exception e) {

                postError(
                        callback,
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Reel upload failed."
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    // ============================================================
    // INSERT REEL INTO SUPABASE
    // ============================================================

    public static void insertReel(
            Context context,
            String videoUrl,
            String caption,
            String visibility,
            String fileName,
            ActionCallback callback
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String accessToken =
                        SupabaseAuthManager.getAccessToken(context);

                if (userId == null ||
                        userId.trim().isEmpty()) {

                    postActionError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
                }

                if (accessToken == null ||
                        accessToken.trim().isEmpty()) {

                    postActionError(
                            callback,
                            "Access token nahi mila."
                    );

                    return;
                }

                JSONObject body =
                        new JSONObject();

                body.put(
                        "user_id",
                        userId
                );

                body.put(
                        "video_url",
                        videoUrl
                );

                body.put(
                        "caption",
                        caption == null
                                ? ""
                                : caption
                );

                body.put(
                        "visibility",
                        visibility == null
                                ? "Public"
                                : visibility
                );

                body.put(
                        "likes",
                        0
                );

                body.put(
                        "comments",
                        0
                );

                body.put(
                        "views",
                        0
                );

                /*
                 * profile username yahan intentionally nahi
                 * bhej rahe hain. Isse username column missing
                 * hone par insert fail nahi hoga.
                 */

                connection =
                        openAuthenticatedPostConnection(
                                REELS_TABLE,
                                accessToken
                        );

                connection.setRequestProperty(
                        "Prefer",
                        "return=representation"
                );

                writeJson(
                        connection,
                        body.toString()
                );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(connection);

                if (code < 200 ||
                        code >= 300) {

                    postActionError(
                            callback,
                            "Reel database save failed (" +
                                    code +
                                    "): " +
                                    extractError(response)
                    );

                    return;
                }

                postActionSuccess(callback);

            } catch (Exception e) {

                postActionError(
                        callback,
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Reel save failed."
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    // ============================================================
    // GET ACTIVE REELS
    // ============================================================

    public static void getActiveReels(
            Context context,
            ReelsCallback callback
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String accessToken =
                        SupabaseAuthManager.getAccessToken(context);

                if (accessToken == null ||
                        accessToken.trim().isEmpty()) {

                    postReelsError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
                }

                String filter;

                if (userId != null &&
                        !userId.trim().isEmpty()) {

                    filter =
                            "or=(visibility.eq.Public,user_id.eq." +
                                    userId +
                                    ")";
                } else {

                    filter =
                            "visibility.eq.Public";
                }

                String url =
                        REELS_TABLE +
                                "?select=*" +
                                "&" +
                                filter +
                                "&order=created_at.desc";

                connection =
                        openGetConnection(
                                url,
                                accessToken
                        );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(connection);

                if (code < 200 ||
                        code >= 300) {

                    postReelsError(
                            callback,
                            extractError(response)
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                java.util.List<Reel> result =
                        new java.util.ArrayList<>();

                for (int i = 0;
                     i < array.length();
                     i++) {

                    JSONObject item =
                            array.getJSONObject(i);

                    String id =
                            item.optString(
                                    "id",
                                    ""
                            );

                    String ownerUid =
                            item.optString(
                                    "user_id",
                                    ""
                            );

                    String username =
                            item.optString(
                                    "username",
                                    "User"
                            );

                    String videoUrl =
                            item.optString(
                                    "video_url",
                                    ""
                            );

                    String thumbnailUrl =
                            item.optString(
                                    "thumbnail_url",
                                    ""
                            );

                    String caption =
                            item.optString(
                                    "caption",
                                    ""
                            );

                    String visibility =
                            item.optString(
                                    "visibility",
                                    "Public"
                            );

                    long createdAt =
                            parseCreatedAt(
                                    item.optString(
                                            "created_at",
                                            ""
                                    )
                            );

                    int likes =
                            item.optInt(
                                    "likes",
                                    0
                            );

                    int comments =
                            item.optInt(
                                    "comments",
                                    0
                            );

                    int views =
                            item.optInt(
                                    "views",
                                    0
                            );

                    boolean ownReel =
                            userId != null &&
                                    userId.equals(ownerUid);

                    result.add(
                            new Reel(
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
                            )
                    );
                }

                postReelsSuccess(
                        callback,
                        result
                );

            } catch (Exception e) {

                postReelsError(
                        callback,
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Reels load failed."
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    // ============================================================
    // UPDATE REEL
    // ============================================================

    public static void updateReel(
            Context context,
            String reelId,
            String caption,
            String visibility,
            ActionCallback callback
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                if (userId == null ||
                        token == null) {

                    postActionError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
                }

                String url =
                        REELS_TABLE +
                                "?id=eq." +
                                encodeQuery(reelId) +
                                "&user_id=eq." +
                                encodeQuery(userId);

                connection =
                        openPatchConnection(
                                url,
                                token
                        );

                JSONObject body =
                        new JSONObject();

                body.put(
                        "caption",
                        caption == null
                                ? ""
                                : caption
                );

                body.put(
                        "visibility",
                        visibility == null
                                ? "Public"
                                : visibility
                );

                writeJson(
                        connection,
                        body.toString()
                );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(connection);

                if (code < 200 ||
                        code >= 300) {

                    postActionError(
                            callback,
                            "Update failed (" +
                                    code +
                                    "): " +
                                    extractError(response)
                    );

                    return;
                }

                postActionSuccess(callback);

            } catch (Exception e) {

                postActionError(
                        callback,
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Reel update failed."
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    // ============================================================
    // DELETE REEL
    // ============================================================

    public static void deleteReel(
            Context context,
            String reelId,
            ActionCallback callback
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                if (userId == null ||
                        token == null) {

                    postActionError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
                }

                String url =
                        REELS_TABLE +
                                "?id=eq." +
                                encodeQuery(reelId) +
                                "&user_id=eq." +
                                encodeQuery(userId);

                connection =
                        openDeleteConnection(
                                url,
                                token
                        );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(connection);

                if (code < 200 ||
                        code >= 300) {

                    postActionError(
                            callback,
                            "Delete failed (" +
                                    code +
                                    "): " +
                                    extractError(response)
                    );

                    return;
                }

                postActionSuccess(callback);

            } catch (Exception e) {

                postActionError(
                        callback,
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Reel delete failed."
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    // ============================================================
    // ADD VIEW
    // ============================================================

    public static void addReelView(
            Context context,
            String reelId,
            ActionCallback callback
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                if (token == null ||
                        token.trim().isEmpty()) {

                    postActionError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
                }

                String getUrl =
                        REELS_TABLE +
                                "?id=eq." +
                                encodeQuery(reelId) +
                                "&select=views";

                connection =
                        openGetConnection(
                                getUrl,
                                token
                        );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(connection);

                connection.disconnect();
                connection = null;

                if (code < 200 ||
                        code >= 300) {

                    postActionError(
                            callback,
                            extractError(response)
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                if (array.length() == 0) {

                    postActionError(
                            callback,
                            "Reel nahi mila."
                    );

                    return;
                }

                int currentViews =
                        array.getJSONObject(0)
                                .optInt(
                                        "views",
                                        0
                                );

                int newViews =
                        currentViews + 1;

                String patchUrl =
                        REELS_TABLE +
                                "?id=eq." +
                                encodeQuery(reelId);

                connection =
                        openPatchConnection(
                                patchUrl,
                                token
                        );

                JSONObject body =
                        new JSONObject();

                body.put(
                        "views",
                        newViews
                );

                writeJson(
                        connection,
                        body.toString()
                );

                code =
                        connection.getResponseCode();

                response =
                        readResponse(connection);

                if (code < 200 ||
                        code >= 300) {

                    postActionError(
                            callback,
                            extractError(response)
                    );

                    return;
                }

                postActionSuccess(callback);

            } catch (Exception e) {

                postActionError(
                        callback,
                        e.getMessage() != null
                                ? e.getMessage()
                                : "View update failed."
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    // ============================================================
    // CHECK LIKE
    // ============================================================

    public static void checkReelLike(
            Context context,
            String reelId,
            BooleanCallback callback
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                if (userId == null ||
                        token == null) {

                    postBooleanError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
                }

                String url =
                        SUPABASE_URL +
                                "/rest/v1/reel_likes" +
                                "?select=id" +
                                "&reel_id=eq." +
                                encodeQuery(reelId) +
                                "&user_id=eq." +
                                encodeQuery(userId);

                connection =
                        openGetConnection(
                                url,
                                token
                        );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(connection);

                if (code < 200 ||
                        code >= 300) {

                    postBooleanError(
                            callback,
                            extractError(response)
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                postBooleanSuccess(
                        callback,
                        array.length() > 0
                );

            } catch (Exception e) {

                postBooleanError(
                        callback,
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Like check failed."
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    // ============================================================
    // TOGGLE LIKE
    // ============================================================

    public static void toggleReelLike(
            Context context,
            String reelId,
            boolean currentlyLiked,
            ActionCallback callback
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                if (userId == null ||
                        token == null) {

                    postActionError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
                }

                String url =
                        SUPABASE_URL +
                                "/rest/v1/reel_likes";

                if (currentlyLiked) {

                    url +=
                            "?reel_id=eq." +
                                    encodeQuery(reelId) +
                                    "&user_id=eq." +
                                    encodeQuery(userId);

                    connection =
                            openDeleteConnection(
                                    url,
                                    token
                            );

                    int code =
                            connection.getResponseCode();

                    String response =
                            readResponse(connection);

                    if (code < 200 ||
                            code >= 300) {

                        postActionError(
                                callback,
                                extractError(response)
                        );

                        return;
                    }

                } else {

                    connection =
                            openAuthenticatedPostConnection(
                                    url,
                                    token
                            );

                    JSONObject body =
                            new JSONObject();

                    body.put(
                            "reel_id",
                            reelId
                    );

                    body.put(
                            "user_id",
                            userId
                    );

                    writeJson(
                            connection,
                            body.toString()
                    );

                    int code =
                            connection.getResponseCode();

                    String response =
                            readResponse(connection);

                    if (code < 200 ||
                            code >= 300) {

                        postActionError(
                                callback,
                                extractError(response)
                        );

                        return;
                    }
                }

                postActionSuccess(callback);

            } catch (Exception e) {

                postActionError(
                        callback,
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Like update failed."
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    // ============================================================
    // UPDATE LIKE COUNT
    // ============================================================

    public static void updateLikeCount(
            Context context,
            String reelId,
            int likes,
            ActionCallback callback
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                if (token == null) {

                    postActionError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
                }

                String url =
                        REELS_TABLE +
                                "?id=eq." +
                                encodeQuery(reelId);

                connection =
                        openPatchConnection(
                                url,
                                token
                        );

                JSONObject body =
                        new JSONObject();

                body.put(
                        "likes",
                        Math.max(
                                0,
                                likes
                        )
                );

                writeJson(
                        connection,
                        body.toString()
                );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(connection);

                if (code < 200 ||
                        code >= 300) {

                    postActionError(
                            callback,
                            extractError(response)
                    );

                    return;
                }

                postActionSuccess(callback);

            } catch (Exception e) {

                postActionError(
                        callback,
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Like count update failed."
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    // ============================================================
    // GET SINGLE REEL
    // ============================================================

    public static void getReel(
            Context context,
            String reelId,
            ReelCallback callback
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String url =
                        REELS_TABLE +
                                "?id=eq." +
                                encodeQuery(reelId) +
                                "&select=*";

                connection =
                        openGetConnection(
                                url,
                                token
                        );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(connection);

                if (code < 200 ||
                        code >= 300) {

                    postReelError(
                            callback,
                            extractError(response)
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                if (array.length() == 0) {

                    postReelError(
                            callback,
                            "Reel nahi mila."
                    );

                    return;
                }

                JSONObject item =
                        array.getJSONObject(0);

                Reel reel =
                        parseReel(
                                item,
                                userId
                        );

                postReelSuccess(
                        callback,
                        reel
                );

            } catch (Exception e) {

                postReelError(
                        callback,
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Reel load failed."
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }


    // ============================================================
    // IS REEL LIKED
    // ============================================================

    public static void isReelLiked(
            Context context,
            String reelId,
            BooleanCallback callback
    ) {
        checkReelLike(
                context,
                reelId,
                callback
        );
    }


    // ============================================================
    // USERNAME
    // ============================================================

    public static void getUsername(
            Context context,
            String userId,
            android.widget.TextView target
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String token =
                        SupabaseAuthManager.getAccessToken(context);

                String url =
                        SUPABASE_URL +
                                "/rest/v1/profiles" +
                                "?select=username" +
                                "&id=eq." +
                                encodeQuery(userId);

                connection =
                        openGetConnection(
                                url,
                                token
                        );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(connection);

                if (code >= 200 &&
                        code < 300) {

                    JSONArray array =
                            new JSONArray(response);

                    if (array.length() > 0) {

                        String username =
                                array.getJSONObject(0)
                                        .optString(
                                                "username",
                                                "User"
                                        );

                        target.post(
                                () ->
                                        target.setText(
                                                username
                                        )
                        );
                    }
                }

            } catch (Exception ignored) {

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
            JSONObject item,
            String currentUserId
    ) {

        String id =
                item.optString(
                        "id",
                        ""
                );

        String ownerUid =
                item.optString(
                        "user_id",
                        ""
                );

        String username =
                item.optString(
                        "username",
                        "User"
                );

        String videoUrl =
                item.optString(
                        "video_url",
                        ""
                );

        String thumbnailUrl =
                item.optString(
                        "thumbnail_url",
                        ""
                );

        String caption =
                item.optString(
                        "caption",
                        ""
                );

        String visibility =
                item.optString(
                        "visibility",
                        "Public"
                );

        long createdAt =
                parseCreatedAt(
                        item.optString(
                                "created_at",
                                ""
                        )
                );

        int likes =
                item.optInt(
                        "likes",
                        0
                );

        int comments =
                item.optInt(
                        "comments",
                        0
                );

        int views =
                item.optInt(
                        "views",
                        0
                );

        boolean ownReel =
                currentUserId != null &&
                        currentUserId.equals(
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
    }


    // ============================================================
    // HTTP
    // ============================================================

    private static HttpURLConnection openPostConnection(
            String urlString
    ) throws Exception {

        HttpURLConnection connection =
                (HttpURLConnection)
                        new URL(urlString)
                                .openConnection();

        connection.setRequestMethod("POST");

        connection.setDoOutput(true);

        connection.setConnectTimeout(30000);

        connection.setReadTimeout(120000);

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        return connection;
    }


    private static HttpURLConnection
    openAuthenticatedPostConnection(
            String urlString,
            String accessToken
    ) throws Exception {

        HttpURLConnection connection =
                openPostConnection(
                        urlString
                );

        connection.setRequestProperty(
                "Authorization",
                "Bearer " + accessToken
        );

        return connection;
    }


    private static HttpURLConnection
    openGetConnection(
            String urlString,
            String accessToken
    ) throws Exception {

        HttpURLConnection connection =
                (HttpURLConnection)
                        new URL(urlString)
                                .openConnection();

        connection.setRequestMethod("GET");

        connection.setConnectTimeout(30000);

        connection.setReadTimeout(60000);

        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );

        if (accessToken != null &&
                !accessToken.trim().isEmpty()) {

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


    private static HttpURLConnection
    openPatchConnection(
            String urlString,
            String accessToken
    ) throws Exception {

        HttpURLConnection connection =
                (HttpURLConnection)
                        new URL(urlString)
                                .openConnection();

        connection.setRequestMethod("PATCH");

        connection.setDoOutput(true);

        connection.setConnectTimeout(30000);

        connection.setReadTimeout(60000);

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );

        connection.setRequestProperty(
                "Authorization",
                "Bearer " + accessToken
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        return connection;
    }


    private static HttpURLConnection
    openDeleteConnection(
            String urlString,
            String accessToken
    ) throws Exception {

        HttpURLConnection connection =
                (HttpURLConnection)
                        new URL(urlString)
                                .openConnection();

        connection.setRequestMethod("DELETE");

        connection.setConnectTimeout(30000);

        connection.setReadTimeout(60000);

        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );

        connection.setRequestProperty(
                "Authorization",
                "Bearer " + accessToken
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        return connection;
    }


    private static void writeJson(
            HttpURLConnection connection,
            String json
    ) throws Exception {

        byte[] data =
                json.getBytes(
                        StandardCharsets.UTF_8
                );

        OutputStream output =
                connection.getOutputStream();

        output.write(data);

        output.flush();

        output.close();
    }


    private static String readResponse(
            HttpURLConnection connection
    ) {

        try {

            InputStream stream;

            int code =
                    connection.getResponseCode();

            if (code >= 400) {

                stream =
                        connection.getErrorStream();

            } else {

                stream =
                        connection.getInputStream();
            }

            if (stream == null) {
                return "";
            }

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    stream,
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

        } catch (Exception e) {

            return e.getMessage() != null
                    ? e.getMessage()
                    : "";
        }
    }


    // ============================================================
    // FILE HELPERS
    // ============================================================

    private static byte[] readAllBytes(
            InputStream input
    ) throws Exception {

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

        return output.toByteArray();
    }


    private static String getExtension(
            Context context,
            Uri uri
    ) {

        try {

            String type =
                    context.getContentResolver()
                            .getType(uri);

            if (type != null) {

                if (type.equals("video/mp4")) {
                    return "mp4";
                }

                if (type.equals("video/webm")) {
                    return "webm";
                }

                if (type.equals("video/3gpp")) {
                    return "3gp";
                }
            }

            String path =
                    uri.getPath();

            if (path != null) {

                int dot =
                        path.lastIndexOf('.');

                if (dot >= 0 &&
                        dot < path.length() - 1) {

                    return path.substring(
                            dot + 1
                    );
                }
            }

        } catch (Exception ignored) {
        }

        return "mp4";
    }


    private static String getContentType(
            String extension
    ) {

        if (extension == null) {
            return "video/mp4";
        }

        switch (
                extension.toLowerCase()
        ) {

            case "webm":
                return "video/webm";

            case "3gp":
                return "video/3gpp";

            case "mov":
                return "video/quicktime";

            case "mkv":
                return "video/x-matroska";

            default:
                return "video/mp4";
        }
    }


    private static String encodeQuery(
            String value
    ) {

        try {

            return java.net.URLEncoder
                    .encode(
                            value == null
                                    ? ""
                                    : value,
                            "UTF-8"
                    );

        } catch (Exception e) {

            return value == null
                    ? ""
                    : value;
        }
    }


    private static long parseCreatedAt(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return 0;
        }

        try {

            return java.time.Instant
                    .parse(value)
                    .toEpochMilli();

        } catch (Exception ignored) {

            return 0;
        }
    }


    private static String extractError(
            String response
    ) {

        if (response == null ||
                response.trim().isEmpty()) {

            return "Unknown server error.";
        }

        try {

            JSONObject json =
                    new JSONObject(response);

            String error =
                    json.optString(
                            "error",
                            ""
                    );

            if (!error.isEmpty()) {
                return error;
            }

            String message =
                    json.optString(
                            "message",
                            ""
                    );

            if (!message.isEmpty()) {
                return message;
            }

        } catch (Exception ignored) {
        }

        return response;
    }


    // ============================================================
    // CALLBACK HELPERS
    // ============================================================

    private static void postSuccess(
            UploadCallback callback,
            String url,
            String fileName
    ) {

        if (callback == null) return;

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).post(
                () -> callback.onSuccess(
                        url,
                        fileName
                )
        );
    }


    private static void postError(
            UploadCallback callback,
            String error
    ) {

        if (callback == null) return;

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).post(
                () -> callback.onError(
                        error
                )
        );
    }


    private static void postActionSuccess(
            ActionCallback callback
    ) {

        if (callback == null) return;

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).post(
                callback::onSuccess
        );
    }


    private static void postActionError(
            ActionCallback callback,
            String error
    ) {

        if (callback == null) return;

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).post(
                () -> callback.onError(
                        error
                )
        );
    }


    private static void postBooleanSuccess(
            BooleanCallback callback,
            boolean value
    ) {

        if (callback == null) return;

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).post(
                () -> callback.onResult(
                        value
                )
        );
    }


    private static void postBooleanError(
            BooleanCallback callback,
            String error
    ) {

        if (callback == null) return;

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).post(
                () -> callback.onError(
                        error
                )
        );
    }


    private static void postReelsSuccess(
            ReelsCallback callback,
            java.util.List<Reel> reels
    ) {

        if (callback == null) return;

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).post(
                () -> callback.onSuccess(
                        reels
                )
        );
    }


    private static void postReelsError(
            ReelsCallback callback,
            String error
    ) {

        if (callback == null) return;

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).post(
                () -> callback.onError(
                        error
                )
        );
    }


    private static void postReelSuccess(
            ReelCallback callback,
            Reel reel
    ) {

        if (callback == null) return;

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).post(
                () -> callback.onSuccess(
                        reel
                )
        );
    }


    private static void postReelError(
            ReelCallback callback,
            String error
    ) {

        if (callback == null) return;

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).post(
                () -> callback.onError(
                        error
                )
        );
    }
}
