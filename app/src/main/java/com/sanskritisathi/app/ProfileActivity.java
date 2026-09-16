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

        saveProfileButton =
                findViewById(R.id.saveProfileButton);

        logoutButton =
                findViewById(R.id.logoutButton);

        profileImageView =
                findViewById(R.id.profileImageView);

        changePhotoButton =
                findViewById(R.id.changePhotoButton);

        backButton =
                findViewById(R.id.backButton);

        settingsButton =
                findViewById(R.id.settingsButton);
    }

    private void setupListeners() {

        if (saveProfileButton != null) {
            saveProfileButton.setOnClickListener(
                    v -> saveProfile()
            );
        }

        if (logoutButton != null) {
            logoutButton.setOnClickListener(
                    v -> logout()
            );
        }

        if (changePhotoButton != null) {
            changePhotoButton.setOnClickListener(
                    v -> openGallery()
            );
        }

        if (backButton != null) {
            backButton.setOnClickListener(
                    v -> finish()
            );
        }

        if (settingsButton != null) {
            settingsButton.setOnClickListener(
                    v -> {
                        Intent intent =
                                new Intent(
                                        ProfileActivity.this,
                                        SettingsActivity.class
                                );

                        startActivity(intent);
                    }
            );
        }
    }

    private void openGallery() {

        Intent intent =
                new Intent(Intent.ACTION_PICK);

        intent.setType("image/*");

        startActivityForResult(
                intent,
                PICK_PROFILE_PHOTO
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

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

        changePhotoButton.setEnabled(false);

        Toast.makeText(
                this,
                "Photo upload ho rahi hai...",
                Toast.LENGTH_SHORT
        ).show();

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                InputStream inputStream =
                        getContentResolver()
                                .openInputStream(imageUri);

                if (inputStream == null) {
                    throw new Exception(
                            "Image stream open nahi hua"
                    );
                }

                Bitmap originalBitmap =
                        BitmapFactory.decodeStream(
                                inputStream
                        );

                inputStream.close();

                if (originalBitmap == null) {
                    throw new Exception(
                            "Image read nahi hui"
                    );
                }

                /*
                 * Profile photo ko 900px ke andar resize.
                 * Isse Base64 request chhoti rahegi.
                 */
                int maxSize = 900;

                int width =
                        originalBitmap.getWidth();

                int height =
                        originalBitmap.getHeight();

                Bitmap bitmap = originalBitmap;

                if (width > maxSize
                        || height > maxSize) {

                    float ratio =
                            Math.min(
                                    (float) maxSize / width,
                                    (float) maxSize / height
                            );

                    int newWidth =
                            Math.max(
                                    1,
                                    Math.round(
                                            width * ratio
                                    )
                            );

                    int newHeight =
                            Math.max(
                                    1,
                                    Math.round(
                                            height * ratio
                                    )
                            );

                    bitmap =
                            Bitmap.createScaledBitmap(
                                    originalBitmap,
                                    newWidth,
                                    newHeight,
                                    true
                            );
                }

                ByteArrayOutputStream output =
                        new ByteArrayOutputStream();

                /*
                 * Quality 75:
                 * profile photo ke liye sufficient
                 * aur upload size chhota.
                 */
                boolean compressed =
                        bitmap.compress(
                                Bitmap.CompressFormat.JPEG,
                                75,
                                output
                        );

                if (!compressed) {
                    throw new Exception(
                            "JPEG compression failed"
                    );
                }

                byte[] imageBytes =
                        output.toByteArray();

                output.close();

                if (bitmap != originalBitmap) {
                    bitmap.recycle();
                }

                originalBitmap.recycle();

                if (imageBytes.length == 0) {
                    throw new Exception(
                            "Image data empty hai"
                    );
                }

                /*
                 * Safety limit.
                 * 1.5 MB se badi compressed image ko
                 * upload nahi karenge.
                 */
                if (imageBytes.length > 1572864) {
                    throw new Exception(
                            "Photo size abhi bhi bahut badi hai"
                    );
                }

                String base64 =
                        Base64.getEncoder()
                                .encodeToString(
                                        imageBytes
                                );

                String fileName =
                        "profile_"
                                + userId
                                + "_"
                                + System.currentTimeMillis()
                                + ".jpg";

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
                        "photos"
                );

                body.put(
                        "contentType",
                        "image/jpeg"
                );

                URL url =
                        new URL(
                                SupabaseConfig.PROJECT_URL
                                        + "/functions/v1/b2-upload"
                        );

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod(
                        "POST"
                );

                connection.setDoOutput(true);

                connection.setConnectTimeout(
                        30000
                );

                connection.setReadTimeout(
                        90000
                );

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setRequestProperty(
                        "Accept",
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

                byte[] bodyBytes =
                        body.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                connection.setFixedLengthStreamingMode(
                        bodyBytes.length
                );

                OutputStream stream =
                        connection.getOutputStream();

                stream.write(bodyBytes);
                stream.flush();
                stream.close();

                int responseCode =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                responseCode
                        );

                if (responseCode >= 200
                        && responseCode < 300) {

                    JSONObject result =
                            new JSONObject(
                                    response
                            );

                    boolean success =
                            result.optBoolean(
                                    "success",
                                    false
                            );

                    if (!success) {

                        String error =
                                result.optString(
                                        "error",
                                        "Unknown B2 error"
                                );

                        String details =
                                result.optString(
                                        "details",
                                        ""
                                );

                        if (!TextUtils.isEmpty(details)) {
                            error +=
                                    "\n" + details;
                        }

                        throw new Exception(error);
                    }

                    String fileNameFromB2 =
                            result.optString(
                                    "fileName",
                                    ""
                            );

                    if (TextUtils.isEmpty(
                            fileNameFromB2
                    )) {

                        throw new Exception(
                                "B2 fileName missing"
                        );
                    }

                    profileImageFileName =
                            fileNameFromB2;

                    saveProfileImageReference(
                            fileNameFromB2
                    );

                } else {

                    String serverError;

                    try {

                        JSONObject errorJson =
                                new JSONObject(response);

                        String error =
                                errorJson.optString(
                                        "error",
                                        ""
                                );

                        String details =
                                errorJson.optString(
                                        "details",
                                        ""
                                );

                        serverError = error;

                        if (!TextUtils.isEmpty(details)) {
                            serverError +=
                                    "\n" + details;
                        }

                    } catch (Exception ignored) {

                        serverError = response;
                    }

                    if (TextUtils.isEmpty(serverError)) {
                        serverError =
                                "HTTP " + responseCode;
                    }

                    throw new Exception(
                            "HTTP "
                                    + responseCode
                                    + ": "
                                    + serverError
                    );
                }

            } catch (Exception e) {

                runOnUiThread(() -> {

                    changePhotoButton
                            .setEnabled(true);

                    Toast.makeText(
                            ProfileActivity.this,
                            "Photo upload failed:\n"
                                    + e.getMessage(),
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

                changePhotoButton.setEnabled(true);

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

                connection.setRequestMethod(
                        "PATCH"
                );

                connection.setDoOutput(true);

                connection.setConnectTimeout(
                        15000
                );

                connection.setReadTimeout(
                        20000
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
                        "Content-Type",
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

                        changePhotoButton
                                .setEnabled(true);

                        Toast.makeText(
                                ProfileActivity.this,
                                "Profile photo B2 me save ho gayi ✅",
                                Toast.LENGTH_SHORT
                        ).show();
                    });

                } else {

                    throw new Exception(
                            "Profile update HTTP "
                                    + responseCode
                                    + ": "
                                    + response
                    );
                }

            } catch (Exception e) {

                runOnUiThread(() -> {

                    changePhotoButton
                            .setEnabled(true);

                    Toast.makeText(
                            ProfileActivity.this,
                            "Photo reference save failed:\n"
                                    + e.getMessage(),
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

                connection.setRequestMethod(
                        "GET"
                );

                connection.setConnectTimeout(
                        15000
                );

                connection.setReadTimeout(
                        20000
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
                                        + response,
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
                                    + e.getMessage(),
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
                            + e.getMessage(),
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

                connection.setRequestMethod(
                        "POST"
                );

                connection.setDoOutput(true);

                connection.setConnectTimeout(
                        15000
                );

                connection.setReadTimeout(
                        20000
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
                        "Content-Type",
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
                                        + response,
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
                                    + e.getMessage(),
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
