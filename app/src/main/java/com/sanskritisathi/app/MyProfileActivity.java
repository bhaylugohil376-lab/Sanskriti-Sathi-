package com.sanskritisathi.app;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private android.widget.TextView profileName;
    private android.widget.TextView profileUsername;
    private android.widget.TextView profileBio;

    private final OkHttpClient client = new OkHttpClient();

    private String currentProfilePhotoFileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileName);
        profileUsername = findViewById(R.id.profileUsername);
        profileBio = findViewById(R.id.profileBio);

        setupListeners();
        loadProfile();
    }

    private void setupListeners() {

        ImageButton backButton = findViewById(R.id.backButton);
        ImageButton settingsButton = findViewById(R.id.settingsButton);

        android.widget.Button editProfileButton =
                findViewById(R.id.editProfileButton);

        android.widget.Button shareProfileButton =
                findViewById(R.id.shareProfileButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                Toast.makeText(
                        this,
                        "Settings",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }

        if (editProfileButton != null) {
            editProfileButton.setOnClickListener(v -> {
                try {
                    startActivity(
                            new android.content.Intent(
                                    this,
                                    ProfileActivity.class
                            )
                    );
                } catch (Exception e) {
                    Toast.makeText(
                            this,
                            "Edit Profile open nahi ho raha",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }

        if (shareProfileButton != null) {
            shareProfileButton.setOnClickListener(v -> shareProfile());
        }

        // PROFILE PHOTO CLICK
        if (profileImage != null) {
            profileImage.setClickable(true);
            profileImage.setFocusable(true);

            profileImage.setOnClickListener(v ->
                    openProfilePhotoViewer()
            );
        }
    }

    private void loadProfile() {

        if (SupabaseConfig.SUPABASE == null) {
            showEmptyProfile();
            return;
        }

        String userId = SupabaseConfig.SUPABASE.auth.currentUser != null
                ? SupabaseConfig.SUPABASE.auth.currentUser.id
                : null;

        if (userId == null || userId.isEmpty()) {
            showEmptyProfile();
            return;
        }

        String url = SupabaseConfig.PROJECT_URL
                + "/rest/v1/profiles"
                + "?id=eq."
                + userId
                + "&select=name,username,bio,profile_image_url";

        Request request = new Request.Builder()
                .url(url)
                .addHeader(
                        "apikey",
                        SupabaseConfig.ANON_KEY
                )
                .addHeader(
                        "Authorization",
                        "Bearer " + SupabaseConfig.ANON_KEY
                )
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showEmptyProfile());
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {

                String responseBody = response.body() != null
                        ? response.body().string()
                        : "";

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> showEmptyProfile());
                    return;
                }

                try {

                    org.json.JSONArray array =
                            new org.json.JSONArray(responseBody);

                    if (array.length() == 0) {
                        runOnUiThread(() -> showEmptyProfile());
                        return;
                    }

                    JSONObject profile =
                            array.getJSONObject(0);

                    runOnUiThread(() ->
                            displayProfile(profile)
                    );

                } catch (Exception e) {
                    runOnUiThread(() -> showEmptyProfile());
                }
            }
        });
    }

    private void displayProfile(JSONObject profile) {

        try {

            String name = profile.optString(
                    "name",
                    ""
            );

            String username = profile.optString(
                    "username",
                    ""
            );

            String bio = profile.optString(
                    "bio",
                    ""
            );

            String photoFileName = profile.optString(
                    "profile_image_url",
                    ""
            );

            if (profileName != null) {
                profileName.setText(
                        name.isEmpty() ? "User" : name
                );
            }

            if (profileUsername != null) {

                if (!username.isEmpty()) {
                    if (username.startsWith("@")) {
                        profileUsername.setText(username);
                    } else {
                        profileUsername.setText(
                                "@" + username
                        );
                    }
                } else {
                    profileUsername.setText("");
                }
            }

            if (profileBio != null) {
                profileBio.setText(bio);
            }

            if (photoFileName != null
                    && !photoFileName.isEmpty()
                    && !photoFileName.equals("null")) {

                currentProfilePhotoFileName =
                        photoFileName;

                fetchAndDisplayB2Image(
                        photoFileName
                );

            } else {
                currentProfilePhotoFileName = null;

                if (profileImage != null) {
                    profileImage.setImageResource(
                            android.R.drawable.ic_menu_myplaces
                    );
                }
            }

        } catch (Exception e) {
            showEmptyProfile();
        }
    }

    private void fetchAndDisplayB2Image(
            String photoFileName
    ) {

        try {

            JSONObject json = new JSONObject();

            json.put(
                    "action",
                    "get_profile_photo"
            );

            json.put(
                    "fileName",
                    photoFileName
            );

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(
                            SupabaseConfig.PROJECT_URL
                                    + "/functions/v1/bright-action"
                    )
                    .addHeader(
                            "Authorization",
                            "Bearer "
                                    + SupabaseConfig.ANON_KEY
                    )
                    .addHeader(
                            "apikey",
                            SupabaseConfig.ANON_KEY
                    )
                    .post(body)
                    .build();

            client.newCall(request).enqueue(
                    new Callback() {

                        @Override
                        public void onFailure(
                                Call call,
                                IOException e
                        ) {
                            runOnUiThread(() -> {

                                if (profileImage != null) {
                                    profileImage.setImageResource(
                                            android.R.drawable.ic_menu_myplaces
                                    );
                                }
                            });
                        }

                        @Override
                        public void onResponse(
                                Call call,
                                Response response
                        ) throws IOException {

                            if (response.body() == null) {
                                return;
                            }

                            String contentType =
                                    response.header(
                                            "Content-Type",
                                            ""
                                    );

                            byte[] responseBytes =
                                    response.body().bytes();

                            /*
                             * CASE 1:
                             * Edge Function directly image return kar rahi hai
                             */
                            if (contentType != null
                                    && contentType
                                    .toLowerCase()
                                    .startsWith("image/")) {

                                runOnUiThread(() -> {

                                    android.graphics.Bitmap bitmap =
                                            android.graphics.BitmapFactory
                                                    .decodeByteArray(
                                                            responseBytes,
                                                            0,
                                                            responseBytes.length
                                                    );

                                    if (bitmap != null
                                            && profileImage != null) {

                                        profileImage.setImageBitmap(
                                                bitmap
                                        );
                                    }
                                });

                                return;
                            }

                            /*
                             * CASE 2:
                             * Edge Function JSON return karti hai
                             */
                            try {

                                String jsonText =
                                        new String(
                                                responseBytes,
                                                java.nio.charset.StandardCharsets
                                                        .UTF_8
                                        );

                                JSONObject result =
                                        new JSONObject(jsonText);

                                String downloadUrl =
                                        result.optString(
                                                "downloadUrl",
                                                ""
                                        );

                                String authorizationToken =
                                        result.optString(
                                                "authorizationToken",
                                                ""
                                        );

                                if (downloadUrl.isEmpty()) {
                                    return;
                                }

                                Request.Builder imageRequest =
                                        new Request.Builder()
                                                .url(downloadUrl)
                                                .get();

                                if (!authorizationToken.isEmpty()) {

                                    imageRequest.addHeader(
                                            "Authorization",
                                            authorizationToken
                                    );
                                }

                                client.newCall(
                                        imageRequest.build()
                                ).enqueue(new Callback() {

                                    @Override
                                    public void onFailure(
                                            Call call,
                                            IOException e
                                    ) {
                                    }

                                    @Override
                                    public void onResponse(
                                            Call call,
                                            Response imageResponse
                                    ) throws IOException {

                                        if (imageResponse.body()
                                                == null) {
                                            return;
                                        }

                                        byte[] imageBytes =
                                                imageResponse
                                                        .body()
                                                        .bytes();

                                        runOnUiThread(() -> {

                                            android.graphics.Bitmap bitmap =
                                                    android.graphics.BitmapFactory
                                                            .decodeByteArray(
                                                                    imageBytes,
                                                                    0,
                                                                    imageBytes.length
                                                            );

                                            if (bitmap != null
                                                    && profileImage != null) {

                                                profileImage
                                                        .setImageBitmap(
                                                                bitmap
                                                        );
                                            }
                                        });
                                    }
                                });

                            } catch (Exception ignored) {
                            }
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // FULL SCREEN PROFILE PHOTO VIEWER
    // =========================================================

    private void openProfilePhotoViewer() {

        if (profileImage == null) {
            return;
        }

        if (profileImage.getDrawable() == null) {

            Toast.makeText(
                    this,
                    "Profile photo available nahi hai",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        final Dialog dialog =
                new Dialog(this);

        dialog.requestWindowFeature(
                android.view.Window.FEATURE_NO_TITLE
        );

        dialog.setContentView(
                R.layout.dialog_profile_photo
        );

        ImageView viewerImage =
                dialog.findViewById(
                        R.id.viewerImage
                );

        ImageButton closeButton =
                dialog.findViewById(
                        R.id.closeButton
                );

        if (viewerImage == null) {
            dialog.dismiss();
            return;
        }

        /*
         * Current profile image ko viewer me show karo.
         */
        viewerImage.setImageDrawable(
                profileImage.getDrawable()
        );

        /*
         * Close button
         */
        if (closeButton != null) {
            closeButton.setOnClickListener(
                    v -> dialog.dismiss()
            );
        }

        /*
         * Photo par tap = close
         */
        viewerImage.setOnClickListener(
                v -> dialog.dismiss()
        );

        /*
         * Dialog window setup
         */
        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.BLACK)
            );

            dialog.getWindow().setDimAmount(0f);

            dialog.getWindow().setGravity(
                    Gravity.CENTER
            );
        }

        dialog.show();

        /*
         * IMPORTANT:
         * setLayout() show() ke BAAD karna hai.
         */
        if (dialog.getWindow() != null) {

            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.BLACK)
            );
        }
    }

    // =========================================================
    // SHARE PROFILE
    // =========================================================

    private void shareProfile() {

        String username = "";

        if (profileUsername != null) {
            username =
                    profileUsername
                            .getText()
                            .toString();
        }

        String shareText;

        if (username.isEmpty()) {
            shareText =
                    "Check out my profile on Sanskriti Sathi!";
        } else {
            shareText =
                    "Check out "
                            + username
                            + " on Sanskriti Sathi!";
        }

        android.content.Intent shareIntent =
                new android.content.Intent(
                        android.content.Intent.ACTION_SEND
                );

        shareIntent.setType("text/plain");

        shareIntent.putExtra(
                android.content.Intent.EXTRA_TEXT,
                shareText
        );

        startActivity(
                android.content.Intent.createChooser(
                        shareIntent,
                        "Share Profile"
                )
        );
    }

    // =========================================================
    // EMPTY PROFILE
    // =========================================================

    private void showEmptyProfile() {

        if (profileName != null) {
            profileName.setText("User");
        }

        if (profileUsername != null) {
            profileUsername.setText("");
        }

        if (profileBio != null) {
            profileBio.setText("");
        }

        currentProfilePhotoFileName = null;

        if (profileImage != null) {
            profileImage.setImageResource(
                    android.R.drawable.ic_menu_myplaces
            );
        }
    }
}
