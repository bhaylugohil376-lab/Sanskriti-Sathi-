package com.sanskritisathi.app;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReelSupabaseHelper {

    private static final String TAG = "ReelSupabaseHelper";
    private static final String TABLE = "reels";
    private static final String BUCKET = "reels";

    private static final Handler MAIN =
            new Handler(Looper.getMainLooper());
    private static final ExecutorService EXECUTOR =
            Executors.newCachedThreadPool();

    private final Context context;

    public interface UploadCallback {
        void onProgress(int progress);
        void onSuccess(String videoUrl);
        void onError(String message);
    }

    public interface GetReelsCallback {
        void onSuccess(List<Reel> reels);
        void onError(Exception e);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public ReelSupabaseHelper() {
        this.context = null;
    }

    public ReelSupabaseHelper(Context context) {
        this.context = context == null ? null : context.getApplicationContext();
    }

    // =========================================================
    // LOAD REELS
    // =========================================================

    public void getActiveReels(GetReelsCallback callback) {
        Context context = this.context;
        if (context == null) {
            post(() -> callback.onError(new Exception("Context missing.")));
            return;
        }

        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String userId = SupabaseAuthManager.getUserId(context);
                String token = SupabaseAuthManager.getAccessToken(context);

                if (TextUtils.isEmpty(token)) {
                    throw new Exception("Login session nahi mili.");
                }

                StringBuilder url = new StringBuilder(
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/" + TABLE
                                + "?select=*"
                                + "&or=(visibility.eq.Public,user_id.eq."
                                + encode(userId)
                                + ")"
                                + "&order=created_at.desc"
                );

                connection = openConnection(url.toString(), "GET", token);
                int code = connection.getResponseCode();
                String response = readResponse(connection, code);

                if (code < 200 || code >= 300) {
                    throw new Exception("Reels load failed: HTTP " + code + " " + response);
                }

                JSONArray array = new JSONArray(response);
                List<Reel> reels = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = array.getJSONObject(i);

                    String id = json.optString("id", "");
                    String user = json.optString("user_id", "");
                    String videoUrl = json.optString("video_url", "");

                    if (TextUtils.isEmpty(videoUrl)) {
                        Log.w(TAG, "Skipping reel with empty video_url: " + id);
                        continue;
                    }

                    String username = json.optString("username", "Sanskriti User");
                    if (TextUtils.isEmpty(username)) username = "Sanskriti User";

                    String thumbnail = json.optString("thumbnail_url", "");
                    String caption = json.optString("caption", "");
                    String visibility = json.optString("visibility", "Public");
                    long createdAt = parseCreatedAt(json.optString("created_at", ""));

                    Reel reel = new Reel(
                            id,
                            user,
                            username,
                            videoUrl,
                            thumbnail,
                            caption,
                            visibility,
                            createdAt,
                            Math.max(0, json.optInt("likes", 0)),
                            Math.max(0, json.optInt("comments", 0)),
                            Math.max(0, json.optInt("views", 0)),
                            false,
                            userId.equals(user)
                    );

                    reels.add(reel);
                }

                final List<Reel> result = reels;
                post(() -> callback.onSuccess(result));

            } catch (Exception e) {
                Log.e(TAG, "getActiveReels failed", e);
                post(() -> callback.onError(e));
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    // =========================================================
    // UPLOAD + DATABASE INSERT
    // =========================================================

    public static void uploadReel(
            Context context,
            Uri videoUri,
            String caption,
            String visibility,
            UploadCallback callback) {

        if (context == null || videoUri == null) {
            if (callback != null) MAIN.post(() -> callback.onError("Video select nahi hui."));
            return;
        }

        // New reels are B2-backed. The Supabase table stores a b2:// file reference.
        B2MediaHelper.uploadVideo(context, videoUri, "reels", new B2MediaHelper.UploadCallback() {
            @Override public void onProgress(int progress) {
                postProgress(callback, progress);
            }

            @Override public void onSuccess(String fileName) {
                EXECUTOR.execute(() -> {
                    try {
                        String userId = SupabaseAuthManager.getUserId(context);
                        String token = SupabaseAuthManager.getAccessToken(context);
                        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(token)) throw new Exception("Login session nahi mili.");
                        String mediaRef = "b2://" + fileName;
                        insertReel(context, userId, mediaRef, caption, visibility, token);
                        MAIN.post(() -> callback.onSuccess(mediaRef));
                    } catch (Exception e) {
                        MAIN.post(() -> callback.onError(e.getMessage() == null ? "Reel database save failed." : e.getMessage()));
                    }
                });
            }

            @Override public void onError(String message) {
                MAIN.post(() -> callback.onError(message));
            }
        });
    }

    private static void insertReel(
            Context context,
            String userId,
            String videoUrl,
            String caption,
            String visibility,
            String token) throws Exception {

        JSONObject json = new JSONObject();
        json.put("id", UUID.randomUUID().toString());
        json.put("user_id", userId);
        json.put("video_url", videoUrl);
        json.put("caption", caption == null ? "" : caption);
        json.put("visibility", TextUtils.isEmpty(visibility) ? "Public" : visibility);
        json.put("likes", 0);
        json.put("comments", 0);
        json.put("views", 0);

        String url = SupabaseConfig.PROJECT_URL + "/rest/v1/" + TABLE;
        HttpURLConnection connection = null;
        try {
            connection = openConnection(url, "POST", token);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Prefer", "return=minimal");

            try (OutputStream output = connection.getOutputStream()) {
                output.write(json.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = connection.getResponseCode();
            String response = readResponse(connection, code);
            if (code < 200 || code >= 300) {
                throw new Exception(
                        "Database save failed: HTTP " + code + " " + response
                );
            }
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    // =========================================================
    // UPDATE / DELETE
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
                    throw new Exception("Login session nahi mili.");
                }

                JSONObject update = new JSONObject();
                update.put("caption", caption == null ? "" : caption);
                update.put("visibility", TextUtils.isEmpty(visibility) ? "Public" : visibility);

                String url = SupabaseConfig.PROJECT_URL
                        + "/rest/v1/" + TABLE
                        + "?id=eq." + encode(reelId)
                        + "&user_id=eq." + encode(userId);

                connection = openConnection(url, "PATCH", token);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Prefer", "return=minimal");

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(update.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = connection.getResponseCode();
                String response = readResponse(connection, code);
                if (code < 200 || code >= 300) {
                    throw new Exception("Reel update failed: HTTP " + code + " " + response);
                }

                postActionSuccess(callback);
            } catch (Exception e) {
                postActionError(callback, e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    public static void deleteReel(
            Context context,
            String reelId,
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
                    throw new Exception("Login session nahi mili.");
                }

                String url = SupabaseConfig.PROJECT_URL
                        + "/rest/v1/" + TABLE
                        + "?id=eq." + encode(reelId)
                        + "&user_id=eq." + encode(userId);

                connection = openConnection(url, "DELETE", token);
                int code = connection.getResponseCode();
                String response = readResponse(connection, code);
                if (code < 200 || code >= 300) {
                    throw new Exception("Reel delete failed: HTTP " + code + " " + response);
                }
                postActionSuccess(callback);
            } catch (Exception e) {
                postActionError(callback, e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    // =========================================================
    // LIKE / VIEW
    // =========================================================

    public static void toggleReelLike(
            Context context,
            String reelId,
            boolean currentlyLiked,
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
                    throw new Exception("Login session nahi mili.");
                }
                if (currentlyLiked) {
                    String url = SupabaseConfig.PROJECT_URL + "/rest/v1/reel_likes?reel_id=eq."
                            + encode(reelId) + "&user_id=eq." + encode(userId);
                    connection = openConnection(url, "DELETE", token);
                } else {
                    JSONObject body = new JSONObject();
                    body.put("id", UUID.randomUUID().toString());
                    body.put("reel_id", reelId);
                    body.put("user_id", userId);
                    String url = SupabaseConfig.PROJECT_URL + "/rest/v1/reel_likes";
                    connection = openConnection(url, "POST", token);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Prefer", "return=minimal");
                    try (OutputStream out = connection.getOutputStream()) {
                        out.write(body.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }
                int code = connection.getResponseCode();
                String response = readResponse(connection, code);
                if (code < 200 || code >= 300) {
                    throw new Exception("Like update failed: HTTP " + code + " " + response);
                }
                updateLikeCount(reelId, token);
                postActionSuccess(callback);
            } catch (Exception e) {
                postActionError(callback, e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private static void updateLikeCount(String reelId, String token) {
        HttpURLConnection count = null;
        HttpURLConnection patch = null;
        try {
            String url = SupabaseConfig.PROJECT_URL + "/rest/v1/reel_likes?select=id&reel_id=eq." + encode(reelId);
            count = openConnection(url, "GET", token);
            int code = count.getResponseCode();
            String response = readResponse(count, code);
            if (code < 200 || code >= 300) return;
            int likes = new JSONArray(response).length();
            JSONObject body = new JSONObject();
            body.put("likes", likes);
            String patchUrl = SupabaseConfig.PROJECT_URL + "/rest/v1/" + TABLE + "?id=eq." + encode(reelId);
            patch = openConnection(patchUrl, "PATCH", token);
            patch.setDoOutput(true);
            patch.setRequestProperty("Content-Type", "application/json");
            patch.setRequestProperty("Prefer", "return=minimal");
            try (OutputStream out = patch.getOutputStream()) {
                out.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }
            patch.getResponseCode();
        } catch (Exception ignored) {
        } finally {
            if (count != null) count.disconnect();
            if (patch != null) patch.disconnect();
        }
    }

    // =========================================================
    // NETWORK HELPERS
    // =========================================================

    private static HttpURLConnection openConnection(
            String urlString,
            String method,
            String token) throws Exception {

        HttpURLConnection connection =
                (HttpURLConnection) new URL(urlString).openConnection();

        connection.setRequestMethod(method);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(180000);
        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );
        connection.setRequestProperty(
                "Authorization",
                "Bearer " + token
        );
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    private static String readResponse(
            HttpURLConnection connection,
            int code) throws Exception {

        InputStream input = code >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();

        if (input == null) return "";

        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8")
                    .replace("+", "%20");
        } catch (Exception e) {
            return value;
        }
    }

    private static String encodePath(String value) {
        String[] parts = value.split("/");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) result.append('/');
            result.append(encode(parts[i]));
        }
        return result.toString();
    }

    private static String getMimeType(Context context, Uri uri) {
        String type = context.getContentResolver().getType(uri);
        return TextUtils.isEmpty(type) ? "video/mp4" : type;
    }

    private static String getExtension(Context context, Uri uri) {
        String mime = getMimeType(context, uri).toLowerCase();
        if (mime.contains("webm")) return "webm";
        if (mime.contains("quicktime")) return "mov";
        if (mime.contains("3gpp")) return "3gp";
        return "mp4";
    }

    private static long getFileSize(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    uri,
                    new String[]{OpenableColumns.SIZE},
                    null,
                    null,
                    null
            );
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (index >= 0 && !cursor.isNull(index)) {
                    return cursor.getLong(index);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return -1L;
    }

    private static long parseCreatedAt(String value) {
        if (TextUtils.isEmpty(value)) return 0L;
        try {
            return Instant.parse(value).toEpochMilli();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private static void post(Runnable runnable) {
        MAIN.post(runnable);
    }

    private static void postProgress(UploadCallback callback, int progress) {
        if (callback == null) return;
        int safe = Math.max(0, Math.min(100, progress));
        MAIN.post(() -> callback.onProgress(safe));
    }

    private static void postActionSuccess(ActionCallback callback) {
        if (callback != null) MAIN.post(callback::onSuccess);
    }

    private static void postActionError(ActionCallback callback, String message) {
        if (callback != null) {
            MAIN.post(() -> callback.onError(
                    TextUtils.isEmpty(message) ? "Reel action failed." : message
            ));
        }
    }

}
