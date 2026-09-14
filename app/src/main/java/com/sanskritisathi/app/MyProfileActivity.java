package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MyProfileActivity extends AppCompatActivity {

    private TextView profileName;
    private TextView profileUsername;
    private TextView profileBio;

    private TextView postsCount;
    private TextView followersCount;
    private TextView followingCount;

    private TextView emptyProfileText;

    private ImageButton backButton;
    private ImageButton settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_profile);

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

        if (SupabaseAuthManager.isLoggedIn(this)) {
            loadProfile();
        }
    }

    private void bindViews() {

        profileName = findViewById(R.id.profileName);
        profileUsername = findViewById(R.id.profileUsername);
        profileBio = findViewById(R.id.profileBio);

        postsCount = findViewById(R.id.postsCount);
        followersCount = findViewById(R.id.followersCount);
        followingCount = findViewById(R.id.followingCount);

        emptyProfileText =
                findViewById(R.id.emptyProfileText);

        backButton =
                findViewById(R.id.backButton);

        settingsButton =
                findViewById(R.id.settingsButton);
    }

    private void setupListeners() {

        if (backButton != null) {
            backButton.setOnClickListener(v ->
                    finish()
            );
        }

        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                MyProfileActivity.this,
                                SettingsActivity.class
                        );

                startActivity(intent);
            });
        }

        View editButton =
                findViewById(R.id.editProfileButton);

        if (editButton != null) {
            editButton.setOnClickListener(v -> {

                Intent intent =
                        new Intent(
                                MyProfileActivity.this,
                                ProfileActivity.class
                        );

                startActivity(intent);
            });
        }

        View shareButton =
                findViewById(R.id.shareProfileButton);

        if (shareButton != null) {
            shareButton.setOnClickListener(v ->
                    shareProfile()
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

            openLogin();
            return;
        }

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                String endpoint =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/profiles"
                                + "?id=eq."
                                + userId
                                + "&select=name,username,bio,profile_image_url";

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

                    runOnUiThread(() ->
                            displayProfile(response)
                    );

                } else {

                    runOnUiThread(() ->
                            Toast.makeText(
                                    MyProfileActivity.this,
                                    "Profile load failed",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }

            } catch (Exception e) {

                runOnUiThread(() ->
                        Toast.makeText(
                                MyProfileActivity.this,
                                "Network error",
                                Toast.LENGTH_SHORT
                        ).show()
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    private void displayProfile(String response) {

        try {

            JSONArray array =
                    new JSONArray(response);

            if (array.length() == 0) {

                showEmptyProfile();
                return;
            }

            JSONObject profile =
                    array.getJSONObject(0);

            String name =
                    profile.optString(
                            "name",
                            ""
                    );

            String username =
                    profile.optString(
                            "username",
                            ""
                    );

            String bio =
                    profile.optString(
                            "bio",
                            ""
                    );

            if (TextUtils.isEmpty(name)) {
                name = "Sanskriti Sathi User";
            }

            if (TextUtils.isEmpty(username)) {
                username = "username";
            }

            if (TextUtils.isEmpty(bio)) {
                bio = "Apni Sanskriti • Apna Gaurav";
            }

            profileName.setText(name);

            profileUsername.setText(
                    "@" + username.replace("@", "")
            );

            profileBio.setText(bio);

            if (emptyProfileText != null) {
                emptyProfileText.setVisibility(
                        View.GONE
                );
            }

        } catch (Exception e) {

            showEmptyProfile();
        }
    }

    private void showEmptyProfile() {

        if (profileName != null) {
            profileName.setText(
                    "Complete Your Profile"
            );
        }

        if (profileUsername != null) {
            profileUsername.setText(
                    "@username"
            );
        }

        if (profileBio != null) {
            profileBio.setText(
                    "Apni Sanskriti • Apna Gaurav"
            );
        }

        if (emptyProfileText != null) {
            emptyProfileText.setText(
                    "Profile data abhi available nahi hai"
            );

            emptyProfileText.setVisibility(
                    View.VISIBLE
            );
        }
    }

    private void shareProfile() {

        String name =
                profileName != null
                        ? profileName.getText().toString()
                        : "Sanskriti Sathi User";

        String username =
                profileUsername != null
                        ? profileUsername.getText().toString()
                        : "@username";

        String shareText =
                name
                        + "\n"
                        + username
                        + "\n\n"
                        + "Sanskriti Sathi par mera profile dekhiye.";

        Intent shareIntent =
                new Intent(Intent.ACTION_SEND);

        shareIntent.setType("text/plain");

        shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                shareText
        );

        startActivity(
                Intent.createChooser(
                        shareIntent,
                        "Share Profile"
                )
        );
    }

    private void openLogin() {

        Intent intent =
                new Intent(
                        MyProfileActivity.this,
                        LoginActivity.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
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
