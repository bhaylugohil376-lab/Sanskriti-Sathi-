package com.sanskritisathi.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

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

    private SupabaseAuthManager() {
        // Prevent object creation
    }

    private static final String PREFS_NAME =
            "SupabaseAuthPrefs";

    private static final String ACCESS_TOKEN_KEY =
            "access_token";

    private static final String REFRESH_TOKEN_KEY =
            "refresh_token";

    private static final String USER_ID_KEY =
            "user_id";

    private static final String USER_EMAIL_KEY =
            "user_email";

    private static final ExecutorService EXECUTOR =
            Executors.newSingleThreadExecutor();

    private static final Handler MAIN_HANDLER =
            new Handler(Looper.getMainLooper());

    public interface AuthCallback {

        void onSuccess(
                String accessToken,
                String refreshToken,
                String userId,
                String userEmail
        );

        void onError(String message);
    }

    // =========================================================
    // REGISTER
    // =========================================================

    public static void register(
            Context context,
            String email,
            String password,
            AuthCallback callback
    ) {

        EXECUTOR.execute(() -> {

            try {

                JSONObject body =
                        new JSONObject();

                body.put("email", email);
                body.put("password", password);

                HttpURLConnection connection =
                        createConnection(
                                "/auth/v1/signup",
                                "POST"
                        );

                writeBody(
                        connection,
                        body.toString()
                );

                int responseCode =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                responseCode
                        );

                if (responseCode >= 200
                        && responseCode < 300) {

                    JSONObject json =
                            new JSONObject(response);

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

                    String userEmail = email;

                    if (user != null) {

                        userId =
                                user.optString(
                                        "id",
                                        ""
                                );

                        userEmail =
                                user.optString(
                                        "email",
                                        email
                                );
                    }

                    /*
                     * Agar Supabase email confirmation
                     * enabled hai to tokens empty ho sakte hain.
                     */
                    if (!accessToken.isEmpty()) {

                        saveSession(
                                context,
                                accessToken,
                                refreshToken,
                                userId,
                                userEmail
                        );
                    }

                    final String finalAccessToken =
                            accessToken;

                    final String finalRefreshToken =
                            refreshToken;

                    final String finalUserId =
                            userId;

                    final String finalUserEmail =
                            userEmail;

                    postSuccess(
                            callback,
                            finalAccessToken,
                            finalRefreshToken,
                            finalUserId,
                            finalUserEmail
                    );

                } else {

                    postError(
                            callback,
                            parseError(response)
                    );
                }

                connection.disconnect();

            } catch (Exception e) {

                postError(
                        callback,
                        getExceptionMessage(e)
                );
            }
        });
    }

    // =========================================================
    // LOGIN
    // =========================================================

    public static void login(
            Context context,
            String email,
            String password,
            AuthCallback callback
    ) {

        EXECUTOR.execute(() -> {

            try {

                JSONObject body =
                        new JSONObject();

                body.put("email", email);
                body.put("password", password);

                HttpURLConnection connection =
                        createConnection(
                                "/auth/v1/token?grant_type=password",
                                "POST"
                        );

                writeBody(
                        connection,
                        body.toString()
                );

                int responseCode =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                responseCode
                        );

                if (responseCode >= 200
                        && responseCode < 300) {

                    JSONObject json =
                            new JSONObject(response);

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

                    String userEmail = email;

                    if (user != null) {

                        userId =
                                user.optString(
                                        "id",
                                        ""
                                );

                        userEmail =
                                user.optString(
                                        "email",
                                        email
                                );
                    }

                    saveSession(
                            context,
                            accessToken,
                            refreshToken,
                            userId,
                            userEmail
                    );

                    postSuccess(
                            callback,
                            accessToken,
                            refreshToken,
                            userId,
                            userEmail
                    );

                } else {

                    postError(
                            callback,
                            parseError(response)
                    );
                }

                connection.disconnect();

            } catch (Exception e) {

                postError(
                        callback,
                        getExceptionMessage(e)
                );
            }
        });
    }

    // =========================================================
    // FORGOT PASSWORD / RESET EMAIL
    // =========================================================

    public static void resetPassword(
            Context context,
            String email,
            AuthCallback callback
    ) {

        EXECUTOR.execute(() -> {

            try {

                JSONObject body =
                        new JSONObject();

                body.put("email", email);

                HttpURLConnection connection =
                        createConnection(
                                "/auth/v1/recover",
                                "POST"
                        );

                writeBody(
                        connection,
                        body.toString()
                );

                int responseCode =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                responseCode
                        );

                if (responseCode >= 200
                        && responseCode < 300) {

                    postSuccess(
                            callback,
                            "",
                            "",
                            "",
                            email
                    );

                } else {

                    postError(
                            callback,
                            parseError(response)
                    );
                }

                connection.disconnect();

            } catch (Exception e) {

                postError(
                        callback,
                        getExceptionMessage(e)
                );
            }
        });
    }

    // =========================================================
    // REFRESH SESSION
    // =========================================================

    public static void refreshSession(
            Context context,
            String refreshToken,
            AuthCallback callback
    ) {

        EXECUTOR.execute(() -> {

            try {

                JSONObject body =
                        new JSONObject();

                body.put(
                        "refresh_token",
                        refreshToken
                );

                HttpURLConnection connection =
                        createConnection(
                                "/auth/v1/token?grant_type=refresh_token",
                                "POST"
                        );

                writeBody(
                        connection,
                        body.toString()
                );

                int responseCode =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                responseCode
                        );

                if (responseCode >= 200
                        && responseCode < 300) {

                    JSONObject json =
                            new JSONObject(response);

                    String newAccessToken =
                            json.optString(
                                    "access_token",
                                    ""
                            );

                    String newRefreshToken =
                            json.optString(
                                    "refresh_token",
                                    refreshToken
                            );

                    JSONObject user =
                            json.optJSONObject("user");

                    String userId =
                            user != null
                                    ? user.optString(
                                    "id",
                                    ""
                            )
                                    : getUserId(context);

                    String userEmail =
                            user != null
                                    ? user.optString(
                                    "email",
                                    getUserEmail(context)
                            )
                                    : getUserEmail(context);

                    saveSession(
                            context,
                            newAccessToken,
                            newRefreshToken,
                            userId,
                            userEmail
                    );

                    postSuccess(
                            callback,
                            newAccessToken,
                            newRefreshToken,
                            userId,
                            userEmail
                    );

                } else {

                    postError(
                            callback,
                            parseError(response)
                    );
                }

                connection.disconnect();

            } catch (Exception e) {

                postError(
                        callback,
                        getExceptionMessage(e)
                );
            }
        });
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    public static void logout(
            Context context,
            AuthCallback callback
    ) {

        EXECUTOR.execute(() -> {

            try {

                String accessToken =
                        getAccessToken(context);

                if (!accessToken.isEmpty()) {

                    HttpURLConnection connection =
                            createConnection(
                                    "/auth/v1/logout",
                                    "POST"
                            );

                    connection.setRequestProperty(
                            "Authorization",
                            "Bearer " + accessToken
                    );

                    int responseCode =
                            connection.getResponseCode();

                    connection.disconnect();

                    if (responseCode >= 200
                            && responseCode < 300) {

                        clearSession(context);

                        postSuccess(
                                callback,
                                "",
                                "",
                                "",
                                ""
                        );

                        return;
                    }
                }

                clearSession(context);

                postSuccess(
                        callback,
                        "",
                        "",
                        "",
                        ""
                );

            } catch (Exception e) {

                clearSession(context);

                postError(
                        callback,
                        getExceptionMessage(e)
                );
            }
        });
    }

    // =========================================================
    // GOOGLE OAUTH SESSION
    // =========================================================

    public static void saveOAuthSession(
            Context context,
            String accessToken,
            String refreshToken
    ) {

        if (accessToken == null
                || accessToken.trim().isEmpty()) {

            return;
        }

        /*
         * Google OAuth callback se token milne par
         * pehle token save kar rahe hain.
         */
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .putString(
                        ACCESS_TOKEN_KEY,
                        accessToken
                )
                .putString(
                        REFRESH_TOKEN_KEY,
                        refreshToken == null
                                ? ""
                                : refreshToken
                )
                .apply();
    }

    // =========================================================
    // SESSION CHECK
    // =========================================================

    public static boolean isLoggedIn(
            Context context
    ) {

        String token =
                getAccessToken(context);

        return token != null
                && !token.trim().isEmpty();
    }

    // =========================================================
    // GET ACCESS TOKEN
    // =========================================================

    public static String getAccessToken(
            Context context
    ) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                ACCESS_TOKEN_KEY,
                ""
        );
    }

    // =========================================================
    // GET REFRESH TOKEN
    // =========================================================

    public static String getRefreshToken(
            Context context
    ) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                REFRESH_TOKEN_KEY,
                ""
        );
    }

    // =========================================================
    // GET USER ID
    // =========================================================

    public static String getUserId(
            Context context
    ) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                USER_ID_KEY,
                ""
        );
    }

    // =========================================================
    // GET USER EMAIL
    // =========================================================

    public static String getUserEmail(
            Context context
    ) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        return preferences.getString(
                USER_EMAIL_KEY,
                ""
        );
    }

    // =========================================================
    // CLEAR SESSION
    // =========================================================

    public static void clearSession(
            Context context
    ) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .remove(ACCESS_TOKEN_KEY)
                .remove(REFRESH_TOKEN_KEY)
                .remove(USER_ID_KEY)
                .remove(USER_EMAIL_KEY)
                .apply();
    }

    // =========================================================
    // SAVE SESSION
    // =========================================================

    private static void saveSession(
            Context context,
            String accessToken,
            String refreshToken,
            String userId,
            String userEmail
    ) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .putString(
                        ACCESS_TOKEN_KEY,
                        accessToken == null
                                ? ""
                                : accessToken
                )
                .putString(
                        REFRESH_TOKEN_KEY,
                        refreshToken == null
                                ? ""
                                : refreshToken
                )
                .putString(
                        USER_ID_KEY,
                        userId == null
                                ? ""
                                : userId
                )
                .putString(
                        USER_EMAIL_KEY,
                        userEmail == null
                                ? ""
                                : userEmail
                )
                .apply();
    }

    // =========================================================
    // CONNECTION
    // =========================================================

    private static HttpURLConnection createConnection(
            String endpoint,
            String method
    ) throws Exception {

        URL url =
                new URL(
                        SupabaseConfig.PROJECT_URL
                                + endpoint
                );

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

        connection.setDoInput(true);

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

        return connection;
    }

    // =========================================================
    // WRITE REQUEST BODY
    // =========================================================

    private static void writeBody(
            HttpURLConnection connection,
            String body
    ) throws Exception {

        connection.setDoOutput(true);

        byte[] bytes =
                body.getBytes(
                        StandardCharsets.UTF_8
                );

        connection.setFixedLengthStreamingMode(
                bytes.length
        );

        OutputStream output =
                connection.getOutputStream();

        output.write(bytes);
        output.flush();
        output.close();
    }

    // =========================================================
    // READ RESPONSE
    // =========================================================

    private static String readResponse(
            HttpURLConnection connection,
            int responseCode
    ) {

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

            while ((line = reader.readLine())
                    != null) {

                result.append(line);
            }

            reader.close();

            return result.toString();

        } catch (Exception e) {

            return "";
        }
    }

    // =========================================================
    // ERROR PARSER
    // =========================================================

    private static String parseError(
            String response
    ) {

        if (response == null
                || response.trim().isEmpty()) {

            return "Operation failed. Please try again.";
        }

        try {

            JSONObject json =
                    new JSONObject(response);

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

            if (!message.isEmpty()) {
                return translateError(message);
            }

        } catch (JSONException ignored) {
        }

        return translateError(response);
    }

    // =========================================================
    // ERROR TRANSLATION
    // =========================================================

    private static String translateError(
            String error
    ) {

        if (error == null) {
            return "Operation failed. Please try again.";
        }

        String lower =
                error.toLowerCase();

        if (lower.contains("invalid login credentials")
                || lower.contains("invalid credentials")
                || lower.contains("invalid credential")) {

            return "Email ya password galat hai.";
        }

        if (lower.contains("email not confirmed")) {

            return "Email confirm karke login karein.";
        }

        if (lower.contains("user already registered")
                || lower.contains("already registered")) {

            return "Is email se account pehle se bana hua hai.";
        }

        if (lower.contains("password")
                && lower.contains("6")) {

            return "Password kam se kam 6 characters ka hona chahiye.";
        }

        if (lower.contains("invalid email")) {

            return "Email address valid nahi hai.";
        }

        if (lower.contains("rate limit")
                || lower.contains("too many requests")) {

            return "Bahut zyada attempts ho gaye. Thodi der baad try karein.";
        }

        if (lower.contains("network")
                || lower.contains("unable to resolve")
                || lower.contains("failed to connect")) {

            return "Internet connection check karein.";
        }

        return error;
    }

    // =========================================================
    // EXCEPTION MESSAGE
    // =========================================================

    private static String getExceptionMessage(
            Exception exception
    ) {

        if (exception == null) {
            return "Operation failed. Please try again.";
        }

        String message =
                exception.getMessage();

        if (message == null
                || message.trim().isEmpty()) {

            return "Operation failed. Please try again.";
        }

        String lower =
                message.toLowerCase();

        if (lower.contains("network")
                || lower.contains("connect")
                || lower.contains("timeout")) {

            return "Internet connection check karein.";
        }

        return "Operation failed. Please try again.";
    }

    // =========================================================
    // MAIN THREAD SUCCESS
    // =========================================================

    private static void postSuccess(
            AuthCallback callback,
            String accessToken,
            String refreshToken,
            String userId,
            String userEmail
    ) {

        MAIN_HANDLER.post(() -> {

            if (callback != null) {

                callback.onSuccess(
                        accessToken,
                        refreshToken,
                        userId,
                        userEmail
                );
            }
        });
    }

    // =========================================================
    // MAIN THREAD ERROR
    // =========================================================

    private static void postError(
            AuthCallback callback,
            String message
    ) {

        MAIN_HANDLER.post(() -> {

            if (callback != null) {

                callback.onError(
                        message
                );
            }
        });
    }
}
