package com.sanskritisathi.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SupabaseAuthManager {

    private static final String AUTH_URL =
            SupabaseConfig.PROJECT_URL + "/auth/v1";

    private static final String PREFS_NAME =
            "SupabaseAuthPrefs";

    private static final String ACCESS_TOKEN =
            "access_token";

    private static final String REFRESH_TOKEN =
            "refresh_token";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private SupabaseAuthManager() {
    }

    public interface AuthCallback {
        void onSuccess(String message, JSONObject data);

        void onError(String message);
    }

    // =========================
    // REGISTER
    // =========================

    public static void register(
            String email,
            String password,
            AuthCallback callback
    ) {
        JSONObject body = new JSONObject();

        try {
            body.put("email", email);
            body.put("password", password);
        } catch (JSONException e) {
            callback.onError("Request create nahi ho saka.");
            return;
        }

        executeRequest(
                AUTH_URL + "/signup",
                "POST",
                body.toString(),
                callback,
                true
        );
    }

    // =========================
    // LOGIN
    // =========================

    public static void login(
            String email,
            String password,
            AuthCallback callback
    ) {
        JSONObject body = new JSONObject();

        try {
            body.put("email", email);
            body.put("password", password);
        } catch (JSONException e) {
            callback.onError("Request create nahi ho saka.");
            return;
        }

        executeRequest(
                AUTH_URL + "/token?grant_type=password",
                "POST",
                body.toString(),
                callback,
                true
        );
    }

    // =========================
    // FORGOT PASSWORD
    // =========================

    public static void forgotPassword(
            String email,
            AuthCallback callback
    ) {
        JSONObject body = new JSONObject();

        try {
            body.put("email", email);
        } catch (JSONException e) {
            callback.onError("Request create nahi ho saka.");
            return;
        }

        executeRequest(
                AUTH_URL + "/recover",
                "POST",
                body.toString(),
                callback,
                false
        );
    }

    // =========================
    // LOGOUT
    // =========================

    public static void logout(Context context) {

        String token = getAccessToken(context);

        if (token != null && !token.isEmpty()) {

            EXECUTOR.execute(() -> {

                HttpURLConnection connection = null;

                try {
                    URL url =
                            new URL(AUTH_URL + "/logout");

                    connection =
                            (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty(
                            "apikey",
                            SupabaseConfig.PUBLISHABLE_KEY
                    );

                    connection.setRequestProperty(
                            "Authorization",
                            "Bearer " + token
                    );

                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);

                    connection.getResponseCode();

                } catch (Exception ignored) {

                } finally {

                    if (connection != null) {
                        connection.disconnect();
                    }

                    clearSession(context);
                }
            });

        } else {
            clearSession(context);
        }
    }

    // =========================
    // SESSION CHECK
    // =========================

    public static boolean isLoggedIn(Context context) {

        String token = getAccessToken(context);

        return token != null
                && !token.trim().isEmpty();
    }

    // =========================
    // GET ACCESS TOKEN
    // =========================

    public static String getAccessToken(Context context) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                ACCESS_TOKEN,
                ""
        );
    }

    // =========================
    // CLEAR SESSION
    // =========================

    public static void clearSession(Context context) {

        context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                )
                .edit()
                .remove(ACCESS_TOKEN)
                .remove(REFRESH_TOKEN)
                .apply();
    }

    // =========================
    // HTTP REQUEST
    // =========================

    private static void executeRequest(
            String urlString,
            String method,
            String requestBody,
            AuthCallback callback,
            boolean saveSession
    ) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                URL url = new URL(urlString);

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod(method);

                connection.setRequestProperty(
                        "apikey",
                        SupabaseConfig.PUBLISHABLE_KEY
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                if (requestBody != null) {

                    connection.setDoOutput(true);

                    byte[] bodyBytes =
                            requestBody.getBytes(
                                    StandardCharsets.UTF_8
                            );

                    connection.setRequestProperty(
                            "Content-Length",
                            String.valueOf(bodyBytes.length)
                    );

                    try (OutputStream outputStream =
                                 connection.getOutputStream()) {

                        outputStream.write(bodyBytes);
                        outputStream.flush();
                    }
                }

                int responseCode =
                        connection.getResponseCode();

                InputStream inputStream;

                if (responseCode >= 200
                        && responseCode < 300) {

                    inputStream =
                            connection.getInputStream();

                } else {

                    inputStream =
                            connection.getErrorStream();

                    if (inputStream == null) {
                        inputStream =
                                connection.getInputStream();
                    }
                }

                String response =
                        readResponse(inputStream);

                JSONObject json = null;

                if (response != null
                        && !response.trim().isEmpty()) {

                    try {
                        json = new JSONObject(response);
                    } catch (JSONException ignored) {
                    }
                }

                if (responseCode >= 200
                        && responseCode < 300) {

                    if (saveSession
                            && json != null) {

                        saveSessionTokens(
                                null,
                                json
                        );
                    }

                    final JSONObject result = json;

                    runOnMainThread(() ->
                            callback.onSuccess(
                                    getSuccessMessage(result),
                                    result
                            )
                    );

                } else {

                    final String errorMessage =
                            getErrorMessage(
                                    json,
                                    responseCode
                            );

                    runOnMainThread(() ->
                            callback.onError(errorMessage)
                    );
                }

            } catch (Exception e) {

                final String message =
                        getNetworkErrorMessage(e);

                runOnMainThread(() ->
                        callback.onError(message)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // =========================
    // SAVE SESSION TOKENS
    // =========================

    private static void saveSessionTokens(
            Context context,
            JSONObject json
    ) {

        /*
         * Context callback se available nahi hota,
         * isliye token saving LoginActivity ke callback
         * me save karne ke liye public helper use hoga.
         */
    }

    public static void saveSession(
            Context context,
            JSONObject json
    ) {

        if (context == null || json == null) {
            return;
        }

        String accessToken =
                json.optString(
                        "access_token",
                        ""
                );

        String refreshToken =
                json.optString(
                        "refresh_token",
                        ""
                );

        if (accessToken.isEmpty()) {
            return;
        }

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .putString(
                        ACCESS_TOKEN,
                        accessToken
                )
                .putString(
                        REFRESH_TOKEN,
                        refreshToken
                )
                .apply();
    }

    // =========================
    // RESPONSE READER
    // =========================

    private static String readResponse(
            InputStream inputStream
    ) throws Exception {

        if (inputStream == null) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     inputStream,
                                     StandardCharsets.UTF_8
                             )
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }

    // =========================
    // ERROR MESSAGE
    // =========================

    private static String getErrorMessage(
            JSONObject json,
            int responseCode
    ) {

        if (json != null) {

            String message =
                    json.optString(
                            "msg",
                            ""
                    );

            if (message.isEmpty()) {
                message =
                        json.optString(
                                "message",
                                ""
                        );
            }

            if (message.isEmpty()) {
                message =
                        json.optString(
                                "error_description",
                                ""
                        );
            }

            if (!message.isEmpty()) {

                String lower =
                        message.toLowerCase();

                if (lower.contains(
                        "invalid login credentials")) {

                    return "Email ya password galat hai.";
                }

                if (lower.contains(
                        "email not confirmed")) {

                    return "Email confirm karna zaroori hai.";
                }

                if (lower.contains(
                        "already registered")
                        || lower.contains(
                        "already exists")) {

                    return "Is email se account pehle se bana hua hai.";
                }

                if (lower.contains(
                        "password should be")) {

                    return "Password Supabase ki required policy ke according nahi hai.";
                }

                return message;
            }
        }

        return "Request failed. Error code: "
                + responseCode;
    }

    // =========================
    // SUCCESS MESSAGE
    // =========================

    private static String getSuccessMessage(
            JSONObject json
    ) {

        if (json == null) {
            return "Operation successful.";
        }

        if (json.has("access_token")) {
            return "Login successful.";
        }

        if (json.has("user")) {
            return "Account created successfully.";
        }

        return "Password reset request sent.";
    }

    // =========================
    // NETWORK ERROR
    // =========================

    private static String getNetworkErrorMessage(
            Exception exception
    ) {

        String message =
                exception.getMessage();

        if (message == null
                || message.trim().isEmpty()) {

            return "Internet connection check karein.";
        }

        return "Network error. Internet connection check karein.";
    }

    // =========================
    // MAIN THREAD HELPER
    // =========================

    private static void runOnMainThread(
            Runnable runnable
    ) {

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).post(runnable);
    }
}
