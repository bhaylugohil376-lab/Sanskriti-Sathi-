package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ProfileActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText usernameInput;
    private EditText bioInput;

    private Button saveProfileButton;
    private Button logoutButton;

    private boolean loadingProfile = false;

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
                                + userId
                                + "&select=id,email,name,username,bio,profile_image_url";

                URL url = new URL(endpoint);

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("GET");
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
                                "Profile load failed",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
                }

            } catch (Exception e) {

                runOnUiThread(() -> {

                    loadingProfile = false;

                    Toast.makeText(
                            ProfileActivity.this,
                            "Network error",
                            Toast.LENGTH_SHORT
                    ).show();
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    private void displayProfile(String response) {

        try {

            if (TextUtils.isEmpty(response)) {
                return;
            }

            JSONArray array =
                    new JSONArray(response);

            if (array.length() == 0) {

                Toast.makeText(
                        this,
                        "Profile abhi create nahi hui",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            JSONObject profile =
                    array.getJSONObject(0);

            String name =
                    profile.optString("name", "");

            String username =
                    profile.optString("username", "");

            String bio =
                    profile.optString("bio", "");

            nameInput.setText(name);
            usernameInput.setText(username);
            bioInput.setText(bio);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Profile data read nahi hui",
                    Toast.LENGTH_SHORT
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
            nameInput.setError("Name डालें");
            nameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            usernameInput.setError("Username डालें");
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

                body.put("id", userId);
                body.put("email", email);
                body.put("name", name);
                body.put("username", username);
                body.put("bio", bio);

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

                        displayProfile(response);

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
                                "Profile save failed: "
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
                            "Network error",
                            Toast.LENGTH_SHORT
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

    private void setButtonsEnabled(boolean enabled) {

        if (saveProfileButton != null) {
            saveProfileButton.setEnabled(enabled);
        }

        if (logoutButton != null) {
            logoutButton.setEnabled(enabled);
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

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            reader.close();

            return result.toString();

        } catch (Exception e) {

            return "";
        }
    }
}
