package com.sanskritisathi.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

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

    private static final String PREFS_NAME = "SupabaseAuthSession";

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String USER_ID = "user_id";
    private static final String USER_EMAIL = "user_email";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private static final Handler MAIN_HANDLER =
            new Handler(Looper.getMainLooper());

    private SupabaseAuthManager() {
        // Prevent object creation
    }

    public interface AuthCallback {
        void onSuccess(String message, JSONObject data);
        void onError(String message);
    }

    // =========================
    // REGISTER
    // =========================

    public static void register(
            Context context,
            String email,
            String password,
            AuthCallback callback) {

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);

            request(
                    context,
                    "/signup",
                    "POST",
                    body,
                    null,
                    true,
                    callback
            );

        } catch (Exception e) {
            postError(callback, "Account create nahi ho saka.");
        }
    }

    // =========================
    // LOGIN
    // =========================

    public static void login(
            Context context,
            String email,
            String password,
            AuthCallback callback) {

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);

            request(
                    context,
                    "/token?grant_type=password",
                    "POST",
                    body,
                    null,
                    true,
                    callback
            );

        } catch (Exception e) {
            postError(callback, "Login request failed.");
        }
    }

    // =========================
    // FORGOT PASSWORD
    // =========================

    public static void resetPassword(
            Context context,
            String email,
            AuthCallback callback) {

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);

            request(
                    context,
                    "/recover",
                    "POST",
                    body,
                    null,
                    false,
                    callback
            );

        } catch (Exception e) {
            postError(callback, "Password reset request failed.");
        }
    }

    // =========================
    // REFRESH SESSION
    // =========================

    public static void refreshSession(
            Context context,
            AuthCallback callback) {

        String refreshToken = getRefreshToken(context);

        if (refreshToken.isEmpty()) {
            postError(callback, "Session available nahi hai.");
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("refresh_token", refreshToken);

            request(
                    context,
                    "/token?grant_type=refresh_token",
                    "POST",
                    body,
                    null,
                    true,
                    callback
            );

        } catch (Exception e) {
            postError(callback, "Session refresh failed.");
        }
    }

    // =========================
    // LOGOUT
    // =========================

    public static void logout(
            Context context,
            AuthCallback callback) {

        String token = getAccessToken(context);

        if (token.isEmpty()) {
            clearSession(context);
            postSuccess(
                    callback,
                    "Logout successful.",
                    new JSONObject()
            );
            return;
        }

        request(
                context,
                "/logout",
                "POST",
                null,
                token,
                false,
                new AuthCallback() {

                    @Override
                    public void onSuccess(
                            String message,
                            JSONObject data) {

                        clearSession(context);

                        postSuccess(
                                callback,
                                "Logout successful.",
                                data
                        );
                    }

                    @Override
                    public void onError(String message) {

                        // Local session clear even if server logout fails.
                        clearSession(context);

                        postSuccess(
                                callback,
                                "Logout successful.",
                                new JSONObject()
                        );
                    }
                }
        );
    }

    // =========================
    // SESSION CHECK
    // =========================

    public static boolean isLoggedIn(Context context) {

        return !getAccessToken(context).isEmpty();
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
    // GET REFRESH TOKEN
    // =========================

    public static String getRefreshToken(Context context) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                REFRESH_TOKEN,
                ""
        );
    }

    // =========================
    // GET USER ID
    // =========================

    public static String getUserId(Context context) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                USER_ID,
                ""
        );
    }

    // =========================
    // GET USER EMAIL
    // =========================

    public static String getUserEmail(Context context) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                USER_EMAIL,
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
                .clear()
                .apply();
    }

    // =========================
    // HTTP REQUEST
    // =========================

    private static void request(
            Context context,
            String endpoint,
            String method,
            JSONObject body,
            String bearerToken,
            boolean saveSession,
            AuthCallback callback) {

        EXECUTOR.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String baseUrl =
                        SupabaseConfig.PROJECT_URL
                                + "/auth/v1";

                URL url =
                        new URL(baseUrl + endpoint);

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod(method);

                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

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

                if (bearerToken != null
                        && !bearerToken.isEmpty()) {

                    connection.setRequestProperty(
                            "Authorization",
                            "Bearer " + bearerToken
                    );
                }

                if (body != null) {

                    connection.setDoOutput(true);

                    byte[] data =
                            body.toString()
                                    .getBytes(StandardCharsets.UTF_8);

                    try (OutputStream outputStream =
                                 connection.getOutputStream()) {

                        outputStream.write(data);
                        outputStream.flush();
                    }
                }

                int responseCode =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                responseCode
                        );

                JSONObject json =
                        parseJson(response);

                if (responseCode >= 200
                        && responseCode < 300) {

                    if (saveSession) {
                        saveSession(
                                context,
                                json
                        );
                    }

                    String successMessage =
                            getSuccessMessage(
                                    endpoint,
                                    json
                            );

                    postSuccess(
                            callback,
                            successMessage,
                            json
                    );

                } else {

                    String errorMessage =
                            getErrorMessage(
                                    json,
                                    responseCode
                            );

                    postError(
                            callback,
                            errorMessage
                    );
                }

            } catch (Exception e) {

                postError(
                        callback,
                        getNetworkError(e)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // =========================
    // SAVE SESSION
    // =========================

    private static void saveSession(
            Context context,
            JSONObject json) {

        try {

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

            JSONObject user =
                    json.optJSONObject("user");

            String userId = "";
            String email = "";

            if (user != null) {

                userId =
                        user.optString(
                                "id",
                                ""
                        );

                email =
                        user.optString(
                                "email",
                                ""
                        );
            }

            SharedPreferences.Editor editor =
                    context.getSharedPreferences(
                            PREFS_NAME,
                            Context.MODE_PRIVATE
                    )
                            .edit();

            if (!accessToken.isEmpty()) {

                editor.putString(
                        ACCESS_TOKEN,
                        accessToken
                );
            }

            if (!refreshToken.isEmpty()) {

                editor.putString(
                        REFRESH_TOKEN,
                        refreshToken
                );
            }

            if (!userId.isEmpty()) {

                editor.putString(
                        USER_ID,
                        userId
                );
            }

            if (!email.isEmpty()) {

                editor.putString(
                        USER_EMAIL,
                        email
                );
            }

            editor.apply();

        } catch (Exception ignored) {
            // Session saving failure should not crash the app.
        }
    }

    // =========================
    // READ RESPONSE
    // =========================

    private static String readResponse(
            HttpURLConnection connection,
            int responseCode) {

        try {

            InputStream inputStream;

            if (responseCode >= 200
                    && responseCode < 400) {

                inputStream =
                        connection.getInputStream();

            } else {

                inputStream =
                        connection.getErrorStream();

                if (inputStream == null) {
                    return "";
                }
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

        } catch (Exception e) {
            return "";
        }
    }

    // =========================
    // PARSE JSON
    // =========================

    private static JSONObject parseJson(
            String response) {

        try {

            if (response == null
                    || response.trim().isEmpty()) {

                return new JSONObject();
            }

            return new JSONObject(response);

        } catch (Exception e) {

            return new JSONObject();
        }
    }

    // =========================
    // ERROR MESSAGE
    // =========================

    private static String getErrorMessage(
            JSONObject json,
            int responseCode) {

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

        if (message.isEmpty()) {

            message =
                    json.optString(
                            "error",
                            ""
                    );
        }

        String lower =
                message.toLowerCase();

        if (lower.contains("invalid login credentials")
                || lower.contains("invalid credentials")) {

            return "Email ya password galat hai.";
        }

        if (lower.contains("email not confirmed")) {

            return "Pehle email confirm karein.";
        }

        if (lower.contains("already registered")
                || lower.contains("already exists")) {

            return "Is email se account pehle se bana hua hai.";
        }

        if (lower.contains("invalid email")) {

            return "Email address valid nahi hai.";
        }

        if (lower.contains("password")) {

            if (lower.contains("6")
                    || lower.contains("weak")) {

                return "Password kam se kam 6 characters ka hona chahiye.";
            }
        }

        if (lower.contains("rate limit")
                || lower.contains("too many")) {

            return "Bahut zyada attempts ho gaye. Thodi der baad try karein.";
        }

        if (lower.contains("network")
                || lower.contains("timeout")) {

            return "Internet connection check karein.";
        }

        if (!message.isEmpty()) {
            return message;
        }

        return "Request failed. Error code: "
                + responseCode;
    }

    // =========================
    // SUCCESS MESSAGE
    // =========================

    private static String getSuccessMessage(
            String endpoint,
            JSONObject json) {

        if (endpoint.startsWith("/signup")) {

            String accessToken =
                    json.optString(
                            "access_token",
                            ""
                    );

            if (accessToken.isEmpty()) {

                return "Account create ho gaya. Email confirmation check karein.";
            }

            return "Account created successfully.";
        }

        if (endpoint.startsWith("/token")) {

            return "Login successful.";
        }

        if (endpoint.startsWith("/recover")) {

            return "Password reset email request bhej di gayi.";
        }

        return "Request successful.";
    }

    // =========================
    // NETWORK ERROR
    // =========================

    private static String getNetworkError(
            Exception exception) {

        String message =
                exception.getMessage();

        if (message == null
                || message.trim().isEmpty()) {

            return "Internet connection check karein.";
        }

        return "Network error. Internet connection check karein.";
    }

    // =========================
    // MAIN THREAD SUCCESS
    // =========================

    private static void postSuccess(
            AuthCallback callback,
            String message,
            JSONObject data) {

        if (callback == null) {
            return;
        }

        MAIN_HANDLER.post(() ->
                callback.onSuccess(
                        message,
                        data
                )
        );
    }

    // =========================
    // MAIN THREAD ERROR
    // =========================

    private static void postError(
            AuthCallback callback,
            String message) {

        if (callback == null) {
            return;
        }

        MAIN_HANDLER.post(() ->
                callback.onError(message)
        );
    }
}
