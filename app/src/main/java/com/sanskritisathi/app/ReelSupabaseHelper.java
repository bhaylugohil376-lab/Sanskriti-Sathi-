package com.sanskritisathi.app;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReelSupabaseHelper {

    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();

    private static final Handler MAIN_HANDLER =
            new Handler(Looper.getMainLooper());

    private ReelSupabaseHelper() {
    }

    // ============================================================
    // CALLBACKS
    // ============================================================

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

    // ============================================================
    // UPLOAD REEL
    // ============================================================

    public static void uploadReel(
            Context context,
            Uri videoUri,
            String caption,
            String visibility,
            UploadCallback callback) {

        if (context == null) {
            callbackError(callback, "Context missing.");
            return;
        }

        if (videoUri == null) {
            callbackError(callback, "Video select nahi hua.");
            return;
        }

        if (TextUtils.isEmpty(visibility)) {
            visibility = "Public";
        }

        final String finalVisibility = visibility;
        final String finalCaption =
                caption == null ? "" : caption.trim();

        EXECUTOR.execute(() -> {

            try {

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String accessToken =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(userId)
                        || TextUtils.isEmpty(accessToken)) {

                    callbackError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
                }

                callbackProgress(callback, 5);

                byte[] videoBytes =
                        readUriBytes(
                                context,
                                videoUri,
                                callback
                        );

                if (videoBytes == null
                        || videoBytes.length == 0) {

                    callbackError(
                            callback,
                            "Video read nahi ho paaya."
                    );

                    return;
                }

                callbackProgress(callback, 35);

                String fileName =
                        "reel_"
                                + userId
                                + "_"
                                + System.currentTimeMillis()
                                + ".mp4";

                String encodedFileName =
                        URLEncoder.encode(
                                fileName,
                                "UTF-8"
                        );

                /*
                 * Supabase Storage bucket:
                 *
                 * reels
                 *
                 * Agar aapke Storage bucket ka naam alag hai,
                 * sirf REELS_BUCKET change karna hoga.
                 */
                String uploadUrl =
                        SupabaseConfig.PROJECT_URL
                                + "/storage/v1/object/"
                                + REELS_BUCKET
                                + "/"
                                + encodedFileName;

                HttpURLConnection connection =
                        (HttpURLConnection)
                                new URL(uploadUrl)
                                        .openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                connection.setConnectTimeout(30000);
                connection.setReadTimeout(120000);

                connection.setRequestProperty(
                        "apikey",
                        SupabaseConfig.PUBLISHABLE_KEY
                );

                connection.setRequestProperty(
                        "Authorization",
                        "Bearer " + accessToken
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "video/mp4"
                );

                connection.setRequestProperty(
                        "x-upsert",
                        "false"
                );

                OutputStream outputStream =
                        connection.getOutputStream();

                outputStream.write(videoBytes);
                outputStream.flush();
                outputStream.close();

                int responseCode =
                        connection.getResponseCode();

                String response =
                        readResponse(connection, responseCode);

                connection.disconnect();

                if (responseCode < 200
                        || responseCode >= 300) {

                    callbackError(
                            callback,
                            "Video upload failed: "
                                    + response
                    );

                    return;
                }

                callbackProgress(callback, 70);

                String videoUrl =
                        SupabaseConfig.PROJECT_URL
                                + "/storage/v1/object/public/"
                                + REELS_BUCKET
                                + "/"
                                + encodedFileName;

                /*
                 * Reel database record
                 */
                JSONObject reelJson =
                        new JSONObject();

                reelJson.put(
                        "user_id",
                        userId
                );

                reelJson.put(
                        "caption",
                        finalCaption
                );

                reelJson.put(
                        "video_url",
                        videoUrl
                );

                reelJson.put(
                        "visibility",
                        finalVisibility
                );

                reelJson.put(
                        "likes",
                        0
                );

                reelJson.put(
                        "comments",
                        0
                );

                reelJson.put(
                        "views",
                        0
                );

                reelJson.put(
                        "created_at",
                        getIsoTime()
                );

                String insertUrl =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/reels";

                HttpURLConnection insertConnection =
                        (HttpURLConnection)
                                new URL(insertUrl)
                                        .openConnection();

                insertConnection.setRequestMethod("POST");
                insertConnection.setDoOutput(true);

                insertConnection.setConnectTimeout(30000);
                insertConnection.setReadTimeout(60000);

                insertConnection.setRequestProperty(
                        "apikey",
                        SupabaseConfig.PUBLISHABLE_KEY
                );

                insertConnection.setRequestProperty(
                        "Authorization",
                        "Bearer " + accessToken
                );

                insertConnection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                insertConnection.setRequestProperty(
                        "Prefer",
                        "return=representation"
                );

                OutputStream dbOutput =
                        insertConnection.getOutputStream();

                dbOutput.write(
                        reelJson.toString()
                                .getBytes("UTF-8")
                );

                dbOutput.flush();
                dbOutput.close();

                int dbCode =
                        insertConnection.getResponseCode();

                String dbResponse =
                        readResponse(
                                insertConnection,
                                dbCode
                        );

                insertConnection.disconnect();

                if (dbCode < 200
                        || dbCode >= 300) {

                    callbackError(
                            callback,
                            "Reel database save failed: "
                                    + dbResponse
                    );

                    return;
                }

                callbackProgress(callback, 100);

                callbackSuccess(
                        callback,
                        videoUrl
                );

            } catch (Exception e) {

                callbackError(
                        callback,
                        getSafeError(
                                e,
                                "Reel upload failed."
                        )
                );
            }
        });
    }

    // ============================================================
    // ADD VIEW
    // ============================================================

    public static void addReelView(
            Context context,
            String reelId,
            ActionCallback callback) {

        if (TextUtils.isEmpty(reelId)) {
            callbackError(
                    callback,
                    "Invalid Reel ID."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            try {

                String accessToken =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(accessToken)) {
                    callbackError(
                            callback,
                            "Login session nahi mili."
                    );
                    return;
                }

                String selectUrl =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/reels"
                                + "?select=views"
                                + "&id=eq."
                                + URLEncoder.encode(
                                        reelId,
                                        "UTF-8"
                                );

                HttpURLConnection getConnection =
                        openConnection(
                                selectUrl,
                                "GET",
                                accessToken
                        );

                int code =
                        getConnection.getResponseCode();

                String response =
                        readResponse(
                                getConnection,
                                code
                        );

                getConnection.disconnect();

                if (code < 200 || code >= 300) {
                    callbackError(
                            callback,
                            "View count load failed."
                    );
                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                if (array.length() == 0) {
                    callbackError(
                            callback,
                            "Reel nahi mila."
                    );
                    return;
                }

                JSONObject reel =
                        array.getJSONObject(0);

                int views =
                        reel.optInt(
                                "views",
                                0
                        );

                views++;

                JSONObject update =
                        new JSONObject();

                update.put(
                        "views",
                        views
                );

                String patchUrl =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/reels"
                                + "?id=eq."
                                + URLEncoder.encode(
                                        reelId,
                                        "UTF-8"
                                );

                HttpURLConnection patchConnection =
                        openConnection(
                                patchUrl,
                                "PATCH",
                                accessToken
                        );

                patchConnection.setDoOutput(true);

                patchConnection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                patchConnection.setRequestProperty(
                        "Prefer",
                        "return=minimal"
                );

                OutputStream output =
                        patchConnection.getOutputStream();

                output.write(
                        update.toString()
                                .getBytes("UTF-8")
                );

                output.flush();
                output.close();

                int patchCode =
                        patchConnection.getResponseCode();

                patchConnection.disconnect();

                if (patchCode < 200
                        || patchCode >= 300) {

                    callbackError(
                            callback,
                            "View count update failed."
                    );

                    return;
                }

                callbackSuccess(callback);

            } catch (Exception e) {

                callbackError(
                        callback,
                        getSafeError(
                                e,
                                "View update failed."
                        )
                );
            }
        });
    }

    // ============================================================
    // CHECK LIKE
    // ============================================================

    public static void checkReelLike(
            Context context,
            String reelId,
            LikeCheckCallback callback) {

        if (TextUtils.isEmpty(reelId)) {
            callbackLikeError(
                    callback,
                    "Invalid Reel ID."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            try {

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String accessToken =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(userId)
                        || TextUtils.isEmpty(accessToken)) {

                    callbackLikeError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
                }

                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/reel_likes"
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

                HttpURLConnection connection =
                        openConnection(
                                url,
                                "GET",
                                accessToken
                        );

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                connection.disconnect();

                if (code < 200 || code >= 300) {

                    callbackLikeError(
                            callback,
                            "Like status load failed."
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                callbackLikeResult(
                        callback,
                        array.length() > 0
                );

            } catch (Exception e) {

                callbackLikeError(
                        callback,
                        getSafeError(
                                e,
                                "Like status check failed."
                        )
                );
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
            ActionCallback callback) {

        if (TextUtils.isEmpty(reelId)) {
            callbackError(
                    callback,
                    "Invalid Reel ID."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            try {

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String accessToken =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(userId)
                        || TextUtils.isEmpty(accessToken)) {

                    callbackError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
                }

                if (currentlyLiked) {

                    deleteLike(
                            reelId,
                            userId,
                            accessToken,
                            callback
                    );

                } else {

                    addLike(
                            reelId,
                            userId,
                            accessToken,
                            callback
                    );
                }

            } catch (Exception e) {

                callbackError(
                        callback,
                        getSafeError(
                                e,
                                "Like update failed."
                        )
                );
            }
        });
    }

    // ============================================================
    // ADD LIKE
    // ============================================================

    private static void addLike(
            String reelId,
            String userId,
            String accessToken,
            ActionCallback callback) {

        try {

            JSONObject json =
                    new JSONObject();

            json.put(
                    "reel_id",
                    reelId
            );

            json.put(
                    "user_id",
                    userId
            );

            json.put(
                    "created_at",
                    getIsoTime()
            );

            String url =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/reel_likes";

            HttpURLConnection connection =
                    openConnection(
                            url,
                            "POST",
                            accessToken
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

            int code =
                    connection.getResponseCode();

            String response =
                    readResponse(
                            connection,
                            code
                    );

            connection.disconnect();

            if (code < 200 || code >= 300) {

                callbackError(
                        callback,
                        "Like failed: " + response
                );

                return;
            }

            callbackSuccess(callback);

        } catch (Exception e) {

            callbackError(
                    callback,
                    getSafeError(
                            e,
                            "Like failed."
                    )
            );
        }
    }

    // ============================================================
    // DELETE LIKE
    // ============================================================

    private static void deleteLike(
            String reelId,
            String userId,
            String accessToken,
            ActionCallback callback) {

        try {

            String url =
                    SupabaseConfig.PROJECT_URL
                            + "/rest/v1/reel_likes"
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

            HttpURLConnection connection =
                    openConnection(
                            url,
                            "DELETE",
                            accessToken
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

            if (code < 200 || code >= 300) {

                callbackError(
                        callback,
                        "Unlike failed: " + response
                );

                return;
            }

            callbackSuccess(callback);

        } catch (Exception e) {

            callbackError(
                    callback,
                    getSafeError(
                            e,
                            "Unlike failed."
                    )
            );
        }
    }

    // ============================================================
    // DELETE REEL
    // ============================================================

    public static void deleteReel(
            Context context,
            String reelId,
            ActionCallback callback) {

        if (TextUtils.isEmpty(reelId)) {
            callbackError(
                    callback,
                    "Invalid Reel ID."
            );
            return;
        }

        EXECUTOR.execute(() -> {

            try {

                String userId =
                        SupabaseAuthManager.getUserId(context);

                String accessToken =
                        SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(userId)
                        || TextUtils.isEmpty(accessToken)) {

                    callbackError(
                            callback,
                            "Login session nahi mili."
                    );

                    return;
                }

                /*
                 * User ID filter bhi lagaya gaya hai,
                 * taaki user sirf apni reel delete kare.
                 */
                String url =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/reels"
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

                HttpURLConnection connection =
                        openConnection(
                                url,
                                "DELETE",
                                accessToken
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

                if (code < 200 || code >= 300) {

                    callbackError(
                            callback,
                            "Reel delete failed: "
                                    + response
                    );

                    return;
                }

                callbackSuccess(callback);

            } catch (Exception e) {

                callbackError(
                        callback,
                        getSafeError(
                                e,
                                "Reel delete failed."
                        )
                );
            }
        });
    }

    // ============================================================
    // STORAGE BUCKET
    // ============================================================

    private static final String REELS_BUCKET =
            "reels";

    // ============================================================
    // READ URI
    // ============================================================

    private static byte[] readUriBytes(
            Context context,
            Uri uri,
            UploadCallback callback)
            throws Exception {

        InputStream inputStream =
                context.getContentResolver()
                        .openInputStream(uri);

        if (inputStream == null) {
            throw new Exception(
                    "Video input stream unavailable."
            );
        }

        BufferedInputStream input =
                new BufferedInputStream(
                        inputStream
                );

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        byte[] buffer =
                new byte[8192];

        int read;
        long total = 0;

        long fileSize = -1;

        try {

            android.database.Cursor cursor =
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

            if (cursor != null) {

                int sizeIndex =
                        cursor.getColumnIndex(
                                android.provider.OpenableColumns.SIZE
                        );

                if (sizeIndex >= 0
                        && cursor.moveToFirst()) {

                    fileSize =
                            cursor.getLong(
                                    sizeIndex
                            );
                }

                cursor.close();
            }

        } catch (Exception ignored) {
        }

        while ((read = input.read(buffer)) != -1) {

            output.write(
                    buffer,
                    0,
                    read
            );

            total += read;

            if (fileSize > 0) {

                int progress =
                        5
                                + (int)
                                Math.min(
                                        30,
                                        (total * 30L)
                                                / fileSize
                                );

                callbackProgress(
                        callback,
                        progress
                );
            }
        }

        input.close();

        return output.toByteArray();
    }

    // ============================================================
    // CONNECTION
    // ============================================================

    private static HttpURLConnection openConnection(
            String url,
            String method,
            String accessToken)
            throws Exception {

        HttpURLConnection connection =
                (HttpURLConnection)
                        new URL(url)
                                .openConnection();

        connection.setRequestMethod(method);

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

    // ============================================================
    // RESPONSE
    // ============================================================

    private static String readResponse(
            HttpURLConnection connection,
            int responseCode) {

        try {

            InputStream stream;

            if (responseCode >= 200
                    && responseCode < 400) {

                stream =
                        connection.getInputStream();

            } else {

                stream =
                        connection.getErrorStream();
            }

            if (stream == null) {
                return "";
            }

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            byte[] buffer =
                    new byte[4096];

            int read;

            while ((read = stream.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        read
                );
            }

            stream.close();

            return output.toString("UTF-8");

        } catch (Exception e) {

            return "";
        }
    }

    // ============================================================
    // TIME
    // ============================================================

    private static String getIsoTime() {

        return new java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                java.util.Locale.US
        ).format(
                new java.util.Date()
        );
    }

    // ============================================================
    // CALLBACK HELPERS
    // ============================================================

    private static void callbackProgress(
            UploadCallback callback,
            int progress) {

        if (callback == null) return;

        MAIN_HANDLER.post(
                () -> callback.onProgress(progress)
        );
    }

    private static void callbackSuccess(
            ActionCallback callback) {

        if (callback == null) return;

        MAIN_HANDLER.post(
                callback::onSuccess
        );
    }

    private static void callbackError(
            ActionCallback callback,
            String message) {

        if (callback == null) return;

        MAIN_HANDLER.post(
                () -> callback.onError(
                        message == null
                                ? "Unknown error."
                                : message
                )
        );
    }

    private static void callbackSuccess(
            UploadCallback callback,
            String videoUrl) {

        if (callback == null) return;

        MAIN_HANDLER.post(
                () -> callback.onSuccess(videoUrl)
        );
    }

    private static void callbackError(
            UploadCallback callback,
            String message) {

        if (callback == null) return;

        MAIN_HANDLER.post(
                () -> callback.onError(
                        message == null
                                ? "Unknown error."
                                : message
                )
        );
    }

    private static void callbackLikeResult(
            LikeCheckCallback callback,
            boolean liked) {

        if (callback == null) return;

        MAIN_HANDLER.post(
                () -> callback.onResult(liked)
        );
    }

    private static void callbackLikeError(
            LikeCheckCallback callback,
            String message) {

        if (callback == null) return;

        MAIN_HANDLER.post(
                () -> callback.onError(
                        message == null
                                ? "Unknown error."
                                : message
                )
        );
    }

    // ============================================================
    // ERROR
    // ============================================================

    private static String getSafeError(
            Exception e,
            String fallback) {

        if (e == null) {
            return fallback;
        }

        String message =
                e.getMessage();

        if (message == null
                || message.trim().isEmpty()) {

            return fallback;
        }

        return message;
    }
}
