package com.sanskritisathi.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

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
import java.util.Base64;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_PROFILE_PHOTO = 1001;

    // Keep the request comfortably below common gateway limits.
    private static final int MAX_IMAGE_BYTES = 600 * 1024;

    private EditText nameInput;
    private EditText usernameInput;
    private EditText bioInput;

    private Button saveProfileButton;
    private Button logoutButton;

    private ImageView profileImageView;
    private MaterialButton changePhotoButton;

    private ImageButton backButton;
    private ImageButton settingsButton;

    private boolean loadingProfile = false;

    private String profileImageFileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        bindViews();
        setupListeners();

        if (!SupabaseAuthManager.isLoggedIn(this)) {
            openLogin();
            return;
        }

        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (SupabaseAuthManager.isLoggedIn(this)
                && !loadingProfile) {
            loadProfile();
        }
    }

    private void bindViews() {
        nameInput = findViewById(R.id.nameInput);
        usernameInput = findViewById(R.id.usernameInput);
        bioInput = findViewById(R.id.bioInput);

        saveProfileButton = findViewById(R.id.saveProfileButton);
        logoutButton = findViewById(R.id.logoutButton);

        profileImageView = findViewById(R.id.profileImageView);
        changePhotoButton = findViewById(R.id.changePhotoButton);

        backButton = findViewById(R.id.backButton);
        settingsButton = findViewById(R.id.settingsButton);
    }

    private void setupListeners() {
        if (saveProfileButton != null) {
            saveProfileButton.setOnClickListener(v -> saveProfile());
        }

        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> logout());
        }

        if (changePhotoButton != null) {
            changePhotoButton.setOnClickListener(v -> openGallery());
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                Intent intent = new Intent(
                        ProfileActivity.this,
                        SettingsActivity.class
                );
                startActivity(intent);
            });
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PROFILE_PHOTO);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != PICK_PROFILE_PHOTO
                || resultCode != RESULT_OK
                || data == null
                || data.getData() == null) {
            return;
        }

        Uri imageUri = data.getData();

        profileImageView.setImageURI(imageUri);

        uploadProfilePhoto(imageUri);
    }

    private void uploadProfilePhoto(Uri imageUri) {

        String userId = SupabaseAuthManager.getUserId(this);
        String accessToken = SupabaseAuthManager.getAccessToken(this);

        if (TextUtils.isEmpty(userId)
                || TextUtils.isEmpty(accessToken)) {

            Toast.makeText(
                    this,
                    "Login session nahi mili",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (changePhotoButton != null) {
            changePhotoButton.setEnabled(false);
        }

        Toast.makeText(
                this,
                "Photo upload ho rahi hai...",
                Toast.LENGTH_SHORT
        ).show();

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {
                byte[] imageBytes = compressProfileImage(imageUri);

                String base64 = Base64.getEncoder()
                        .encodeToString(imageBytes);

                String fileName =
                        "profile_"
                                + userId
                                + "_"
                                + System.currentTimeMillis()
                                + ".jpg";

                JSONObject body = new JSONObject();

                body.put("fileName", fileName);
                body.put("fileBase64", base64);
                body.put("folder", "photos");
                body.put("contentType", "image/jpeg");

                URL url = new URL(
                        SupabaseConfig.PROJECT_URL
                                + "/functions/v1/b2-upload"
                );

                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                connection.setConnectTimeout(30000);
                connection.setReadTimeout(90000);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                connection.setRequestProperty(
                        "Cache-Control",
                        "no-cache"
                );

                connection.setRequestProperty(
                        "Connection",
                        "close"
                );

                connection.setRequestProperty(
                        "apikey",
                        SupabaseConfig.PUBLISHABLE_KEY
                );

                connection.setRequestProperty(
                        "Authorization",
                        "Bearer " + accessToken
                );

                byte[] bodyBytes = body.toString()
                        .getBytes(StandardCharsets.UTF_8);

                connection.setFixedLengthStreamingMode(
                        bodyBytes.length
                );

                OutputStream stream = connection.getOutputStream();
                stream.write(bodyBytes);
                stream.flush();
                stream.close();

                int responseCode = connection.getResponseCode();

                String response = readResponse(
                        connection,
                        responseCode
                );

                if (responseCode >= 200
                        && responseCode < 300) {

                    JSONObject result;

                    try {
                        result = new JSONObject(response);
                    } catch (Exception jsonError) {
                        throw new Exception(
                                "B2 response JSON invalid.\nHTTP "
                                        + responseCode
                                        + "\nRaw response:\n"
                                        + limitForToast(response)
                        );
                    }

                    boolean success = result.optBoolean(
                            "success",
                            false
                    );

                    if (!success) {
                        throw new Exception(
                                formatB2Error(
                                        responseCode,
                                        result,
                                        response
                                )
                        );
                    }

                    String fileNameFromB2 = result.optString(
                            "fileName",
                            ""
                    );

                    if (TextUtils.isEmpty(fileNameFromB2)) {
                        throw new Exception(
                                "B2 fileName missing.\nResponse:\n"
                                        + limitForToast(response)
                        );
                    }

                    profileImageFileName = fileNameFromB2;

                    saveProfileImageReference(fileNameFromB2);

                } else {
                    throw new Exception(
                            formatHttpError(
                                    responseCode,
                                    response
                            )
                    );
                }

            } catch (Exception e) {

                runOnUiThread(() -> {
                    enablePhotoButton();

                    showUploadErrorDialog(
                            "Photo upload failed",
                            safeMessage(e)
                    );
                });

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    /**
     * Converts the selected image to JPEG and keeps it <= 600 KB.
     * Smaller requests help prevent Supabase gateway/function request-size failures.
     */
    private byte[] compressProfileImage(Uri imageUri) throws Exception {

        InputStream inputStream =
                getContentResolver().openInputStream(imageUri);

        if (inputStream == null) {
            throw new Exception("Image stream open nahi hua");
        }

        Bitmap originalBitmap;

        try {
            originalBitmap = BitmapFactory.decodeStream(inputStream);
        } finally {
            inputStream.close();
        }

        if (originalBitmap == null) {
            throw new Exception("Image read nahi hui");
        }

        Bitmap workingBitmap = originalBitmap;

        try {
            int[] sizes = {900, 800, 720, 640, 560};
            int[] qualities = {75, 68, 62, 56, 50, 45, 40};

            for (int maxSize : sizes) {

                Bitmap resizedBitmap = scaleBitmap(
                        originalBitmap,
                        maxSize
                );

                if (resizedBitmap != originalBitmap) {
                    if (workingBitmap != originalBitmap) {
                        workingBitmap.recycle();
                    }
                    workingBitmap = resizedBitmap;
                }

                for (int quality : qualities) {

                    ByteArrayOutputStream output =
                            new ByteArrayOutputStream();

                    boolean compressed = workingBitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            quality,
                            output
                    );

                    if (!compressed) {
                        output.close();
                        throw new Exception(
                                "JPEG compression failed"
                        );
                    }

                    byte[] bytes = output.toByteArray();
                    output.close();

                    if (bytes.length > 0
                            && bytes.length <= MAX_IMAGE_BYTES) {
                        return bytes;
                    }
                }
            }

            throw new Exception(
                    "Photo ko 600 KB ke andar compress nahi kar paaya."
            );

        } finally {
            if (workingBitmap != originalBitmap) {
                workingBitmap.recycle();
            }

            originalBitmap.recycle();
        }
    }

    private Bitmap scaleBitmap(
            Bitmap source,
            int maxSize
    ) {
        int width = source.getWidth();
        int height = source.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return source;
        }

        float ratio = Math.min(
                (float) maxSize / width,
                (float) maxSize / height
        );

        int newWidth = Math.max(
                1,
                Math.round(width * ratio)
        );

        int newHeight = Math.max(
                1,
                Math.round(height * ratio)
        );

        return Bitmap.createScaledBitmap(
                source,
                newWidth,
                newHeight,
                true
        );
    }

    private String formatB2Error(
            int responseCode,
            JSONObject result,
            String rawResponse
    ) {
        StringBuilder message = new StringBuilder();

        String step = result.optString("step", "");
        String status = result.optString("status", "");
        String error = result.optString("error", "");
        String details = result.optString("details", "");

        message.append("HTTP ")
                .append(responseCode);

        if (!TextUtils.isEmpty(step)) {
            message.append("\nStep: ")
                    .append(step);
        }

        if (!TextUtils.isEmpty(status)) {
            message.append("\nStatus: ")
                    .append(status);
        }

        if (!TextUtils.isEmpty(error)) {
            message.append("\nError: ")
                    .append(error);
        }

        if (!TextUtils.isEmpty(details)) {
            message.append("\nDetails: ")
                    .append(details);
        }

        if (TextUtils.isEmpty(error)
                && TextUtils.isEmpty(details)) {
            message.append("\nRaw response:\n")
                    .append(limitForToast(rawResponse));
        }

        return message.toString();
    }

    private String formatHttpError(
            int responseCode,
            String response
    ) {
        StringBuilder message = new StringBuilder();

        message.append("HTTP ")
                .append(responseCode);

        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject json = new JSONObject(response);

                String messageText = json.optString(
                        "message",
                        ""
                );

                String error = json.optString(
                        "error",
                        ""
                );

                String details = json.optString(
                        "details",
                        ""
                );

                if (!TextUtils.isEmpty(error)) {
                    message.append("\nError: ")
                            .append(error);
                }

                if (!TextUtils.isEmpty(messageText)) {
                    message.append("\nMessage: ")
                            .append(messageText);
                }

                if (!TextUtils.isEmpty(details)) {
                    message.append("\nDetails: ")
                            .append(details);
                }

                if (TextUtils.isEmpty(error)
                        && TextUtils.isEmpty(messageText)
                        && TextUtils.isEmpty(details)) {
                    message.append("\nResponse:\n")
                            .append(limitForToast(response));
                }

            } catch (Exception ignored) {
                message.append("\nResponse:\n")
                        .append(limitForToast(response));
            }
        } else {
            message.append("\nServer ne empty response diya.");
        }

        return message.toString();
    }

    private String safeMessage(Exception e) {
        String message = e.getMessage();

        if (TextUtils.isEmpty(message)) {
            return e.getClass().getSimpleName();
        }

        return limitForToast(message);
    }

    private String limitForToast(String text) {
        if (text == null) {
            return "";
        }

        final int maxLength = 1800;

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength)
                + "\n...[response truncated]";
    }

    private void enablePhotoButton() {
        if (changePhotoButton != null) {
            changePhotoButton.setEnabled(true);
        }
    }

    private void showUploadErrorDialog(
            String title,
            String message
    ) {
        new androidx.appcompat.app.AlertDialog.Builder(
                ProfileActivity.this
        )
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNegativeButton(
                        "Copy",
                        (dialog, which) -> {
                            android.content.ClipboardManager clipboard =
                                    (android.content.ClipboardManager)
                                            getSystemService(
                                                    CLIPBOARD_SERVICE
                                            );

                            if (clipboard != null) {
                                android.content.ClipData clip =
                                        android.content.ClipData.newPlainText(
                                                "B2 Upload Error",
                                                message
                                        );

                                clipboard.setPrimaryClip(clip);

                                Toast.makeText(
                                        ProfileActivity.this,
                                        "Error copied",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .show();
    }

    private void saveProfileImageReference(
            String fileName
    ) {

        String userId =
                SupabaseAuthManager.getUserId(this);

        String accessToken =
                SupabaseAuthManager.getAccessToken(this);

        if (TextUtils.isEmpty(userId)
                || TextUtils.isEmpty(accessToken)) {

            runOnUiThread(() -> {

                enablePhotoButton();

                Toast.makeText(
                        this,
                        "Login session nahi mili",
                        Toast.LENGTH_LONG
                ).show();
            });

            return;
        }

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                JSONObject body =
                        new JSONObject();

                body.put(
                        "profile_image_url",
                        fileName
                );

                String endpoint =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/profiles"
                                + "?id=eq."
                                + URLEncoder.encode(
                                        userId,
                                        "UTF-8"
                                );

                URL url =
                        new URL(endpoint);

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("PATCH");
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

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
                        "application/json"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                connection.setRequestProperty(
                        "Prefer",
                        "return=representation"
                );

                byte[] bytes =
                        body.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                OutputStream output =
                        connection.getOutputStream();

                output.write(bytes);
                output.flush();
                output.close();

                int responseCode =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                responseCode
                        );

                if (responseCode >= 200
                        && responseCode < 300) {

                    runOnUiThread(() -> {

                        enablePhotoButton();

                        Toast.makeText(
                                ProfileActivity.this,
                                "Profile photo B2 me save ho gayi ✅",
                                Toast.LENGTH_SHORT
                        ).show();
                    });

                } else {

                    throw new Exception(
                            "Profile update failed:\n"
                                    + formatHttpError(
                                    responseCode,
                                    response
                            )
                    );
                }

            } catch (Exception e) {

                runOnUiThread(() -> {

                    enablePhotoButton();

                    Toast.makeText(
                            ProfileActivity.this,
                            "Photo reference save failed:\n"
                                    + safeMessage(e),
                            Toast.LENGTH_LONG
                    ).show();
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    private void loadProfile() {

        String userId =
                SupabaseAuthManager.getUserId(this);

        String accessToken =
                SupabaseAuthManager.getAccessToken(this);

        if (TextUtils.isEmpty(userId)
                || TextUtils.isEmpty(accessToken)) {

            Toast.makeText(
                    this,
                    "Login session nahi mili",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        loadingProfile = true;

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                String endpoint =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/profiles"
                                + "?id=eq."
                                + URLEncoder.encode(
                                        userId,
                                        "UTF-8"
                                )
                                + "&select=id,email,name,username,bio,profile_image_url";

                URL url =
                        new URL(endpoint);

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("GET");
                connection.setUseCaches(false);

                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

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

                int responseCode =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                responseCode
                        );

                if (responseCode >= 200
                        && responseCode < 300) {

                    runOnUiThread(() -> {

                        loadingProfile = false;

                        displayProfile(response);
                    });

                } else {

                    runOnUiThread(() -> {

                        loadingProfile = false;

                        Toast.makeText(
                                ProfileActivity.this,
                                "Profile load failed:\n"
                                        + formatHttpError(
                                        responseCode,
                                        response
                                ),
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }

            } catch (Exception e) {

                runOnUiThread(() -> {

                    loadingProfile = false;

                    Toast.makeText(
                            ProfileActivity.this,
                            "Profile network error:\n"
                                    + safeMessage(e),
                            Toast.LENGTH_LONG
                    ).show();
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void displayProfile(
            String response
    ) {

        try {

            if (TextUtils.isEmpty(response)) {
                return;
            }

            JSONArray array =
                    new JSONArray(response);

            if (array.length() == 0) {
                return;
            }

            JSONObject profile =
                    array.getJSONObject(0);

            nameInput.setText(
                    profile.optString(
                            "name",
                            ""
                    )
            );

            usernameInput.setText(
                    profile.optString(
                            "username",
                            ""
                    )
            );

            bioInput.setText(
                    profile.optString(
                            "bio",
                            ""
                    )
            );

            profileImageFileName =
                    profile.optString(
                            "profile_image_url",
                            ""
                    );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Profile data read nahi hui:\n"
                            + safeMessage(e),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void saveProfile() {

        String name =
                nameInput.getText()
                        .toString()
                        .trim();

        String username =
                usernameInput.getText()
                        .toString()
                        .trim()
                        .replace("@", "");

        String bio =
                bioInput.getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(name)) {

            nameInput.setError(
                    "Name डालें"
            );

            nameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {

            usernameInput.setError(
                    "Username डालें"
            );

            usernameInput.requestFocus();
            return;
        }

        if (username.contains(" ")) {

            usernameInput.setError(
                    "Username में space नहीं होना चाहिए"
            );

            usernameInput.requestFocus();
            return;
        }

        if (username.length() < 3) {

            usernameInput.setError(
                    "Username कम से कम 3 characters का होना चाहिए"
            );

            usernameInput.requestFocus();
            return;
        }

        String userId =
                SupabaseAuthManager.getUserId(this);

        String email =
                SupabaseAuthManager.getUserEmail(this);

        String accessToken =
                SupabaseAuthManager.getAccessToken(this);

        if (TextUtils.isEmpty(userId)
                || TextUtils.isEmpty(accessToken)) {

            Toast.makeText(
                    this,
                    "Login session nahi mili",
                    Toast.LENGTH_SHORT
            ).show();

            openLogin();
            return;
        }

        setButtonsEnabled(false);

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                JSONObject body =
                        new JSONObject();

                body.put(
                        "id",
                        userId
                );

                body.put(
                        "email",
                        email
                );

                body.put(
                        "name",
                        name
                );

                body.put(
                        "username",
                        username
                );

                body.put(
                        "bio",
                        bio
                );

                if (!TextUtils.isEmpty(
                        profileImageFileName
                )) {

                    body.put(
                            "profile_image_url",
                            profileImageFileName
                    );
                }

                String endpoint =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/profiles"
                                + "?on_conflict=id";

                URL url =
                        new URL(endpoint);

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

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
                        "application/json"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                connection.setRequestProperty(
                        "Prefer",
                        "resolution=merge-duplicates,return=representation"
                );

                byte[] bytes =
                        body.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                OutputStream output =
                        connection.getOutputStream();

                output.write(bytes);
                output.flush();
                output.close();

                int responseCode =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                responseCode
                        );

                if (responseCode >= 200
                        && responseCode < 300) {

                    runOnUiThread(() -> {

                        setButtonsEnabled(true);

                        Toast.makeText(
                                ProfileActivity.this,
                                "Profile save ho gayi ✅",
                                Toast.LENGTH_SHORT
                        ).show();
                    });

                } else {

                    runOnUiThread(() -> {

                        setButtonsEnabled(true);

                        Toast.makeText(
                                ProfileActivity.this,
                                "Profile save failed:\n"
                                        + formatHttpError(
                                        responseCode,
                                        response
                                ),
                                Toast.LENGTH_LONG
                        ).show();
                    });
                }

            } catch (Exception e) {

                runOnUiThread(() -> {

                    setButtonsEnabled(true);

                    Toast.makeText(
                            ProfileActivity.this,
                            "Network error:\n"
                                    + safeMessage(e),
                            Toast.LENGTH_LONG
                    ).show();
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    private void logout() {

        setButtonsEnabled(false);

        SupabaseAuthManager.logout(
                this,
                new SupabaseAuthManager.AuthCallback() {

                    @Override
                    public void onSuccess(
                            String accessToken,
                            String refreshToken,
                            String userId,
                            String userEmail
                    ) {

                        setButtonsEnabled(true);

                        Toast.makeText(
                                ProfileActivity.this,
                                "Logout successful",
                                Toast.LENGTH_SHORT
                        ).show();

                        openLogin();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        SupabaseAuthManager.clearSession(
                                ProfileActivity.this
                        );

                        setButtonsEnabled(true);

                        openLogin();
                    }
                }
        );
    }

    private void openLogin() {

        Intent intent =
                new Intent(
                        ProfileActivity.this,
                        LoginActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    private void setButtonsEnabled(
            boolean enabled
    ) {

        if (saveProfileButton != null) {
            saveProfileButton.setEnabled(
                    enabled
            );
        }

        if (logoutButton != null) {
            logoutButton.setEnabled(
                    enabled
            );
        }
    }

    private String readResponse(
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

            while ((line =
                    reader.readLine()) != null) {

                result.append(line);
            }

            reader.close();

            return result.toString();

        } catch (Exception e) {

            return "";
        }
    }
}
