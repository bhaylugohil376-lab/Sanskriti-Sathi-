package com.sanskritisathi.app;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class B2MediaHelper {

    private static final Handler MAIN =
            new Handler(Looper.getMainLooper());

    private B2MediaHelper() {
    }

    // =========================================================
    // CALLBACKS
    // =========================================================

    public interface UploadCallback {

        void onProgress(int progress);

        void onSuccess(String fileName);

        void onError(String message);
    }

    public interface UrlCallback {

        void onSuccess(String url);

        void onError(String message);
    }

    // =========================================================
    // VIDEO UPLOAD
    // =========================================================

    public static void uploadVideo(
            Context context,
            Uri uri,
            String folder,
            UploadCallback callback
    ) {

        if (context == null || uri == null) {

            error(
                    callback,
                    "Video select nahi hui."
            );

            return;
        }

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                String token =
                        SupabaseAuthManager.getAccessToken(
                                context
                        );

                if (TextUtils.isEmpty(token)) {
                    throw new Exception(
                            "Login session nahi mili."
                    );
                }

                long size =
                        fileSize(
                                context,
                                uri
                        );

                String extension =
                        extension(
                                context,
                                uri
                        );

                String fileName =
                        "reel_"
                                + System.currentTimeMillis()
                                + "_"
                                + Math.abs(
                                uri.toString().hashCode()
                        )
                                + "."
                                + extension;

                URL url =
                        new URL(
                                SupabaseConfig.PROJECT_URL
                                        + "/functions/v1/b2-media"
                                        + "?action=upload_media"
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                connection.setConnectTimeout(30000);
                connection.setReadTimeout(300000);

                connection.setRequestProperty(
                        "apikey",
                        SupabaseConfig.PUBLISHABLE_KEY
                );

                connection.setRequestProperty(
                        "Authorization",
                        "Bearer " + token
                );

                connection.setRequestProperty(
                        "Content-Type",
                        mime(context, uri)
                );

                connection.setRequestProperty(
                        "x-media-file-name",
                        fileName
                );

                connection.setRequestProperty(
                        "x-media-folder",
                        TextUtils.isEmpty(folder)
                                ? "media"
                                : folder
                );

                if (size >= 0) {

                    connection.setFixedLengthStreamingMode(
                            size
                    );

                } else {

                    connection.setChunkedStreamingMode(
                            64 * 1024
                    );
                }

                InputStream input =
                        context.getContentResolver()
                                .openInputStream(uri);

                if (input == null) {

                    throw new Exception(
                            "Video read nahi ho saki."
                    );
                }

                OutputStream output =
                        connection.getOutputStream();

                byte[] buffer =
                        new byte[64 * 1024];

                int count;
                long total = 0;

                progress(callback, 5);

                while (
                        (count = input.read(buffer))
                                != -1
                ) {

                    output.write(
                            buffer,
                            0,
                            count
                    );

                    total += count;

                    if (size > 0) {

                        int value =
                                5
                                        + (int) Math.min(
                                        90,
                                        total * 90L / size
                                );

                        progress(
                                callback,
                                value
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

                if (code < 200 || code >= 300) {

                    throw new Exception(
                            "B2 upload failed: HTTP "
                                    + code
                                    + " "
                                    + response
                    );
                }

                JSONObject json =
                        new JSONObject(response);

                if (!json.optBoolean(
                        "success",
                        false
                )) {

                    throw new Exception(
                            json.optString(
                                    "error",
                                    "B2 upload failed."
                            )
                    );
                }

                String saved =
                        json.optString(
                                "fileName",
                                ""
                        );

                if (TextUtils.isEmpty(saved)) {

                    throw new Exception(
                            "B2 fileName missing."
                    );
                }

                progress(
                        callback,
                        100
                );

                final String result =
                        saved;

                MAIN.post(
                        () -> {

                            if (callback != null) {
                                callback.onSuccess(
                                        result
                                );
                            }
                        }
                );

            } catch (Exception e) {

                error(
                        callback,
                        e.getMessage()
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    // =========================================================
    // IMAGE UPLOAD
    // =========================================================

    public static void uploadImage(
            Context context,
            Uri uri,
            String folder,
            UploadCallback callback
    ) {

        if (context == null || uri == null) {

            error(
                    callback,
                    "Image select nahi hui."
            );

            return;
        }

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                String token =
                        SupabaseAuthManager.getAccessToken(
                                context
                        );

                if (TextUtils.isEmpty(token)) {

                    throw new Exception(
                            "Login session nahi mili."
                    );
                }

                long size =
                        fileSize(
                                context,
                                uri
                        );

                String fileName =
                        "post_"
                                + System.currentTimeMillis()
                                + "_"
                                + Math.abs(
                                uri.toString().hashCode()
                        )
                                + ".jpg";

                URL url =
                        new URL(
                                SupabaseConfig.PROJECT_URL
                                        + "/functions/v1/b2-media"
                                        + "?action=upload_media"
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

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

                connection.setRequestProperty(
                        "Content-Type",
                        "image/jpeg"
                );

                connection.setRequestProperty(
                        "x-media-file-name",
                        fileName
                );

                connection.setRequestProperty(
                        "x-media-folder",
                        TextUtils.isEmpty(folder)
                                ? "posts"
                                : folder
                );

                if (size >= 0) {

                    connection.setFixedLengthStreamingMode(
                            size
                    );

                } else {

                    connection.setChunkedStreamingMode(
                            64 * 1024
                    );
                }

                InputStream input =
                        context.getContentResolver()
                                .openInputStream(uri);

                if (input == null) {

                    throw new Exception(
                            "Image read nahi ho saki."
                    );
                }

                OutputStream output =
                        connection.getOutputStream();

                byte[] buffer =
                        new byte[32 * 1024];

                int count;
                long total = 0;

                progress(
                        callback,
                        5
                );

                while (
                        (count = input.read(buffer))
                                != -1
                ) {

                    output.write(
                            buffer,
                            0,
                            count
                    );

                    total += count;

                    if (size > 0) {

                        int value =
                                5
                                        + (int) Math.min(
                                        90,
                                        total * 90L / size
                                );

                        progress(
                                callback,
                                value
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

                if (code < 200 || code >= 300) {

                    throw new Exception(
                            "B2 image upload failed: HTTP "
                                    + code
                                    + " "
                                    + response
                    );
                }

                JSONObject json =
                        new JSONObject(response);

                if (!json.optBoolean(
                        "success",
                        false
                )) {

                    throw new Exception(
                            json.optString(
                                    "error",
                                    "B2 image upload failed."
                            )
                    );
                }

                String saved =
                        json.optString(
                                "fileName",
                                ""
                        );

                if (TextUtils.isEmpty(saved)) {

                    throw new Exception(
                            "B2 fileName missing."
                    );
                }

                progress(
                        callback,
                        100
                );

                final String result =
                        saved;

                MAIN.post(
                        () -> {

                            if (callback != null) {
                                callback.onSuccess(
                                        result
                                );
                            }
                        }
                );

            } catch (Exception e) {

                error(
                        callback,
                        e.getMessage()
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    // =========================================================
    // RESOLVE B2 URL
    // =========================================================

    public static void resolveUrl(
            Context context,
            String fileName,
            UrlCallback callback
    ) {

        if (
                context == null
                        || TextUtils.isEmpty(fileName)
        ) {

            urlError(
                    callback,
                    "B2 file missing."
            );

            return;
        }

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                String token =
                        SupabaseAuthManager.getAccessToken(
                                context
                        );

                if (TextUtils.isEmpty(token)) {

                    throw new Exception(
                            "Login session nahi mili."
                    );
                }

                URL url =
                        new URL(
                                SupabaseConfig.PROJECT_URL
                                        + "/functions/v1/b2-media"
                                        + "?action=get_media"
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                connection.setConnectTimeout(30000);
                connection.setReadTimeout(60000);

                connection.setRequestProperty(
                        "apikey",
                        SupabaseConfig.PUBLISHABLE_KEY
                );

                connection.setRequestProperty(
                        "Authorization",
                        "Bearer " + token
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                JSONObject body =
                        new JSONObject();

                body.put(
                        "fileName",
                        fileName
                );

                body.put(
                        "validDurationInSeconds",
                        3600
                );

                try (
                        OutputStream output =
                                connection.getOutputStream()
                ) {

                    output.write(
                            body.toString()
                                    .getBytes(
                                            StandardCharsets.UTF_8
                                    )
                    );

                    output.flush();
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
                            "B2 URL failed: HTTP "
                                    + code
                                    + " "
                                    + response
                    );
                }

                JSONObject json =
                        new JSONObject(response);

                String downloadUrl =
                        json.optString(
                                "downloadUrl",
                                ""
                        );

                if (
                        !json.optBoolean(
                                "success",
                                false
                        )
                                || TextUtils.isEmpty(
                                downloadUrl
                        )
                ) {

                    throw new Exception(
                            json.optString(
                                    "error",
                                    "B2 download URL missing."
                            )
                    );
                }

                final String result =
                        downloadUrl;

                MAIN.post(
                        () -> {

                            if (callback != null) {
                                callback.onSuccess(
                                        result
                                );
                            }
                        }
                );

            } catch (Exception e) {

                urlError(
                        callback,
                        e.getMessage()
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    // =========================================================
    // PROGRESS
    // =========================================================

    private static void progress(
            UploadCallback callback,
            int value
    ) {

        if (callback == null) {
            return;
        }

        final int progress =
                Math.max(
                        0,
                        Math.min(
                                100,
                                value
                        )
                );

        MAIN.post(
                () -> callback.onProgress(
                        progress
                )
        );
    }

    // =========================================================
    // UPLOAD ERROR
    // =========================================================

    private static void error(
            UploadCallback callback,
            String message
    ) {

        if (callback == null) {
            return;
        }

        final String result =
                TextUtils.isEmpty(message)
                        ? "B2 upload failed."
                        : message;

        MAIN.post(
                () -> callback.onError(
                        result
                )
        );
    }

    // =========================================================
    // URL ERROR
    // =========================================================

    private static void urlError(
            UrlCallback callback,
            String message
    ) {

        if (callback == null) {
            return;
        }

        final String result =
                TextUtils.isEmpty(message)
                        ? "B2 URL failed."
                        : message;

        MAIN.post(
                () -> callback.onError(
                        result
                )
        );
    }

    // =========================================================
    // READ RESPONSE
    // =========================================================

    private static String readResponse(
            HttpURLConnection connection,
            int code
    ) throws Exception {

        InputStream input =
                code >= 400
                        ? connection.getErrorStream()
                        : connection.getInputStream();

        if (input == null) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        input,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while (
                    (line = reader.readLine())
                            != null
            ) {

                result.append(line);
            }
        }

        return result.toString();
    }

    // =========================================================
    // FILE SIZE
    // =========================================================

    private static long fileSize(
            Context context,
            Uri uri
    ) {

        try (
                Cursor cursor =
                        context.getContentResolver().query(
                                uri,
                                new String[]{
                                        OpenableColumns.SIZE
                                },
                                null,
                                null,
                                null
                        )
        ) {

            if (
                    cursor != null
                            && cursor.moveToFirst()
            ) {

                int index =
                        cursor.getColumnIndex(
                                OpenableColumns.SIZE
                        );

                if (
                        index >= 0
                                && !cursor.isNull(index)
                ) {

                    return cursor.getLong(index);
                }
            }

        } catch (Exception ignored) {
        }

        return -1L;
    }

    // =========================================================
    // MIME
    // =========================================================

    private static String mime(
            Context context,
            Uri uri
    ) {

        String value =
                context.getContentResolver()
                        .getType(uri);

        if (TextUtils.isEmpty(value)) {
            return "video/mp4";
        }

        return value;
    }

    // =========================================================
    // EXTENSION
    // =========================================================

    private static String extension(
            Context context,
            Uri uri
    ) {

        String value =
                mime(
                        context,
                        uri
                ).toLowerCase();

        if (value.contains("webm")) {
            return "webm";
        }

        if (value.contains("quicktime")) {
            return "mov";
        }

        if (value.contains("3gpp")) {
            return "3gp";
        }

        return "mp4";
    }
}
