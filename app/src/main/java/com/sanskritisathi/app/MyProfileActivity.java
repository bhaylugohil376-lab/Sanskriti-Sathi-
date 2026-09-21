package com.sanskritisathi.app;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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

    private ImageView profileImage;

    private ImageButton postsTab;
    private ImageButton reelsTab;
    private ImageButton savedTab;

    private String profileImageFileName = "";

    private boolean profilePhotoLoaded = false;
    private boolean loadingProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_profile);

        setupSystemBars();
        bindViews();
        setupListeners();
        setupTabs();
        makeProfileImageCircular();

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

    private void setupSystemBars() {

        Window window = getWindow();

        window.setStatusBarColor(
                Color.parseColor("#10151D")
        );

        window.setNavigationBarColor(
                Color.parseColor("#10151D")
        );

        /*
         * Do NOT use:
         * setDecorFitsSystemWindows(false)
         *
         * XML already uses fitsSystemWindows="true".
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            window.getDecorView().setSystemUiVisibility(0);
        }
    }

    private void bindViews() {

        profileName = findViewById(R.id.profileName);
        profileUsername = findViewById(R.id.profileUsername);
        profileBio = findViewById(R.id.profileBio);

        postsCount = findViewById(R.id.postsCount);
        followersCount = findViewById(R.id.followersCount);
        followingCount = findViewById(R.id.followingCount);

        emptyProfileText = findViewById(R.id.emptyProfileText);

        backButton = findViewById(R.id.backButton);
        settingsButton = findViewById(R.id.settingsButton);

        profileImage = findViewById(R.id.profileImage);

        postsTab = findViewById(R.id.postsTab);
        reelsTab = findViewById(R.id.reelsTab);
        savedTab = findViewById(R.id.savedTab);
    }

    private void setupListeners() {

        if (backButton != null) {

            backButton.setOnClickListener(v -> finish());
        }

        if (settingsButton != null) {

            settingsButton.setOnClickListener(v -> {

                Intent intent = new Intent(
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

                Intent intent = new Intent(
                        MyProfileActivity.this,
                        ProfileActivity.class
                );

                startActivity(intent);
            });
        }

        View shareButton =
                findViewById(R.id.shareProfileButton);

        if (shareButton != null) {

            shareButton.setOnClickListener(
                    v -> shareProfile()
            );
        }

        if (profileImage != null) {

            profileImage.setClickable(true);
            profileImage.setFocusable(true);

            profileImage.setOnClickListener(
                    v -> showProfilePhotoViewer()
            );
        }
    }

    private void setupTabs() {

        if (postsTab != null) {

            postsTab.setOnClickListener(v -> {

                setTabSelected(postsTab);

                Toast.makeText(
                        this,
                        "Posts",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }

        if (reelsTab != null) {

            reelsTab.setOnClickListener(v -> {

                setTabSelected(reelsTab);

                Toast.makeText(
                        this,
                        "Reels",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }

        if (savedTab != null) {

            savedTab.setOnClickListener(v -> {

                setTabSelected(savedTab);

                Toast.makeText(
                        this,
                        "Saved",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }
    }

    private void setTabSelected(ImageButton selected) {

        if (postsTab != null) {
            postsTab.setColorFilter(
                    Color.parseColor("#858E9B")
            );
        }

        if (reelsTab != null) {
            reelsTab.setColorFilter(
                    Color.parseColor("#858E9B")
            );
        }

        if (savedTab != null) {
            savedTab.setColorFilter(
                    Color.parseColor("#858E9B")
            );
        }

        if (selected != null) {

            selected.setColorFilter(
                    Color.WHITE
            );
        }
    }

    private void makeProfileImageCircular() {

        if (profileImage == null) {
            return;
        }

        profileImage.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            profileImage.setClipToOutline(true);

            profileImage.setOutlineProvider(
                    new ViewOutlineProvider() {

                        @Override
                        public void getOutline(
                                View view,
                                Outline outline
                        ) {

                            outline.setOval(
                                    0,
                                    0,
                                    view.getWidth(),
                                    view.getHeight()
                            );
                        }
                    }
            );
        }
    }

    private void loadProfile() {

        if (loadingProfile) {
            return;
        }

        String userId =
                SupabaseAuthManager.getUserId(this);

        String accessToken =
                SupabaseAuthManager.getAccessToken(this);

        if (TextUtils.isEmpty(userId)
                || TextUtils.isEmpty(accessToken)) {

            openLogin();
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

                    runOnUiThread(() -> {

                        loadingProfile = false;

                        displayProfile(response);
                    });

                } else {

                    runOnUiThread(() -> {

                        loadingProfile = false;

                        Toast.makeText(
                                MyProfileActivity.this,
                                "Profile load failed",
                                Toast.LENGTH_SHORT
                        ).show();
                    });
                }

            } catch (Exception e) {

                runOnUiThread(() -> {

                    loadingProfile = false;

                    Toast.makeText(
                            MyProfileActivity.this,
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

        profilePhotoLoaded = false;

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

            profileImageFileName =
                    profile.optString(
                            "profile_image_url",
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

            if (profileName != null) {
                profileName.setText(name);
            }

            if (profileUsername != null) {

                profileUsername.setText(
                        "@" + username.replace("@", "")
                );
            }

            if (profileBio != null) {
                profileBio.setText(bio);
            }

            if (emptyProfileText != null) {

                emptyProfileText.setVisibility(
                        View.GONE
                );
            }

            if (!TextUtils.isEmpty(
                    profileImageFileName
            )) {

                fetchAndDisplayB2Image(
                        profileImageFileName
                );

            } else {

                showDefaultProfileImage();
            }

        } catch (Exception e) {

            showEmptyProfile();
        }
    }

    private void showDefaultProfileImage() {

        profilePhotoLoaded = false;

        if (profileImage != null) {

            profileImage.setImageResource(
                    android.R.drawable.ic_menu_myplaces
            );

            makeProfileImageCircular();
        }
    }

    private void fetchAndDisplayB2Image(
            String photoFileName
    ) {

        if (TextUtils.isEmpty(photoFileName)) {
            return;
        }

        final String accessToken =
                SupabaseAuthManager.getAccessToken(this);

        if (TextUtils.isEmpty(accessToken)) {
            return;
        }

        new Thread(() -> {

            HttpURLConnection connection = null;
            HttpURLConnection imageConnection = null;

            try {

                String endpoint =
                        SupabaseConfig.PROJECT_URL
                                + "/functions/v1/bright-action";

                JSONObject request =
                        new JSONObject();

                request.put(
                        "action",
                        "get_profile_photo"
                );

                request.put(
                        "fileName",
                        photoFileName
                );

                URL url =
                        new URL(endpoint);

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("POST");

                connection.setDoOutput(true);
                connection.setUseCaches(false);

                connection.setConnectTimeout(30000);
                connection.setReadTimeout(60000);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json, image/*"
                );

                connection.setRequestProperty(
                        "apikey",
                        SupabaseConfig.PUBLISHABLE_KEY
                );

                connection.setRequestProperty(
                        "Authorization",
                        "Bearer " + accessToken
                );

                byte[] requestBytes =
                        request.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                connection.setFixedLengthStreamingMode(
                        requestBytes.length
                );

                OutputStream output =
                        connection.getOutputStream();

                output.write(requestBytes);
                output.flush();
                output.close();

                int responseCode =
                        connection.getResponseCode();

                String contentType =
                        connection.getHeaderField(
                                "Content-Type"
                        );

                if (responseCode < 200
                        || responseCode >= 300) {

                    String error =
                            readResponse(
                                    connection,
                                    responseCode
                            );

                    throw new Exception(
                            "Photo load failed HTTP "
                                    + responseCode
                                    + " "
                                    + error
                    );
                }

                /*
                 * Direct image response.
                 */
                if (contentType != null
                        && contentType
                        .toLowerCase()
                        .startsWith("image/")) {

                    InputStream input =
                            connection.getInputStream();

                    Bitmap bitmap =
                            BitmapFactory.decodeStream(
                                    input
                            );

                    input.close();

                    if (bitmap == null) {

                        throw new Exception(
                                "Image decode failed"
                        );
                    }

                    final Bitmap finalBitmap =
                            bitmap;

                    runOnUiThread(() ->
                            setProfileBitmap(finalBitmap)
                    );

                    return;
                }

                /*
                 * JSON response.
                 */
                String jsonResponse =
                        readResponse(
                                connection,
                                responseCode
                        );

                JSONObject result =
                        new JSONObject(jsonResponse);

                if (!result.optBoolean(
                        "success",
                        false
                )) {

                    throw new Exception(
                            "Photo authorization failed"
                    );
                }

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

                if (TextUtils.isEmpty(downloadUrl)
                        || TextUtils.isEmpty(
                        authorizationToken
                )) {

                    throw new Exception(
                            "Photo URL/token missing"
                    );
                }

                URL imageUrl =
                        new URL(downloadUrl);

                imageConnection =
                        (HttpURLConnection)
                                imageUrl.openConnection();

                imageConnection.setRequestMethod(
                        "GET"
                );

                imageConnection.setUseCaches(false);

                imageConnection.setConnectTimeout(
                        30000
                );

                imageConnection.setReadTimeout(
                        60000
                );

                imageConnection.setRequestProperty(
                        "Authorization",
                        authorizationToken
                );

                int imageResponseCode =
                        imageConnection.getResponseCode();

                if (imageResponseCode < 200
                        || imageResponseCode >= 300) {

                    throw new Exception(
                            "B2 image download failed HTTP "
                                    + imageResponseCode
                    );
                }

                InputStream imageInput =
                        imageConnection.getInputStream();

                Bitmap bitmap =
                        BitmapFactory.decodeStream(
                                imageInput
                        );

                imageInput.close();

                if (bitmap == null) {

                    throw new Exception(
                            "B2 image decode failed"
                    );
                }

                final Bitmap finalBitmap =
                        bitmap;

                runOnUiThread(() ->
                        setProfileBitmap(finalBitmap)
                );

            } catch (Exception e) {

                runOnUiThread(() -> {

                    profilePhotoLoaded = false;
                });

            } finally {

                if (imageConnection != null) {
                    imageConnection.disconnect();
                }

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    private void setProfileBitmap(Bitmap bitmap) {

        if (isFinishing()
                || (Build.VERSION.SDK_INT >= 17
                && isDestroyed())) {
            return;
        }

        if (profileImage == null
                || bitmap == null) {
            return;
        }

        profileImage.setImageBitmap(bitmap);

        profileImage.setScaleType(
                ImageView.ScaleType.CENTER_CROP
        );

        makeProfileImageCircular();

        profilePhotoLoaded = true;
    }

    private void showProfilePhotoViewer() {

        if (profileImage == null
                || !profilePhotoLoaded
                || profileImage.getDrawable() == null) {

            Toast.makeText(
                    this,
                    "Profile photo available nahi hai",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        final Dialog dialog =
                new Dialog(
                        this,
                        android.R.style.Theme_Black_NoTitleBar_Fullscreen
                );

        dialog.setContentView(
                R.layout.dialog_profile_photo
        );

        ImageView backgroundImage =
                dialog.findViewById(
                        R.id.backgroundImage
                );

        ImageView viewerImage =
                dialog.findViewById(
                        R.id.viewerImage
                );

        ImageButton closeButton =
                dialog.findViewById(
                        R.id.closeButton
                );

        View followingAction =
                dialog.findViewById(
                        R.id.followingAction
                );

        View shareAction =
                dialog.findViewById(
                        R.id.shareAction
                );

        View copyAction =
                dialog.findViewById(
                        R.id.copyAction
                );

        View qrAction =
                dialog.findViewById(
                        R.id.qrAction
                );

        if (backgroundImage != null) {

            backgroundImage.setImageDrawable(
                    profileImage.getDrawable()
            );

            backgroundImage.setScaleType(
                    ImageView.ScaleType.CENTER_CROP
            );

            if (Build.VERSION.SDK_INT >= 31) {

                backgroundImage.setRenderEffect(
                        RenderEffect.createBlurEffect(
                                28f,
                                28f,
                                Shader.TileMode.CLAMP
                        )
                );
            }
        }

        if (viewerImage != null) {

            viewerImage.setImageDrawable(
                    profileImage.getDrawable()
            );

            viewerImage.setScaleType(
                    ImageView.ScaleType.CENTER_CROP
            );

            if (Build.VERSION.SDK_INT >= 21) {

                viewerImage.setClipToOutline(true);

                viewerImage.setOutlineProvider(
                        new ViewOutlineProvider() {

                            @Override
                            public void getOutline(
                                    View view,
                                    Outline outline
                            ) {

                                outline.setOval(
                                        0,
                                        0,
                                        view.getWidth(),
                                        view.getHeight()
                                );
                            }
                        }
                );
            }

            viewerImage.setOnClickListener(
                    v -> dialog.dismiss()
            );
        }

        if (closeButton != null) {

            closeButton.setOnClickListener(
                    v -> dialog.dismiss()
            );
        }

        if (followingAction != null) {

            followingAction.setOnClickListener(v ->
                    Toast.makeText(
                            MyProfileActivity.this,
                            "Following",
                            Toast.LENGTH_SHORT
                    ).show()
            );
        }

        if (shareAction != null) {

            shareAction.setOnClickListener(
                    v -> shareProfile()
            );
        }

        if (copyAction != null) {

            copyAction.setOnClickListener(v -> {

                String username =
                        profileUsername != null
                                ? profileUsername
                                .getText()
                                .toString()
                                .replace("@", "")
                                .trim()
                                : "username";

                String profileLink =
                        "https://sanskritisathi.app/"
                                + username;

                ClipboardManager clipboard =
                        (ClipboardManager)
                                getSystemService(
                                        CLIPBOARD_SERVICE
                                );

                if (clipboard != null) {

                    clipboard.setPrimaryClip(
                            ClipData.newPlainText(
                                    "Profile link",
                                    profileLink
                            )
                    );

                    Toast.makeText(
                            MyProfileActivity.this,
                            "Profile link copied",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }

        if (qrAction != null) {

            qrAction.setOnClickListener(v ->
                    Toast.makeText(
                            MyProfileActivity.this,
                            "QR code",
                            Toast.LENGTH_SHORT
                    ).show()
            );
        }

        dialog.show();

        Window window =
                dialog.getWindow();

        if (window != null) {

            window.setBackgroundDrawableResource(
                    android.R.color.transparent
            );

            window.setDimAmount(0.20f);

            window.addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
            );

            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }
    }

    private void showEmptyProfile() {

        profilePhotoLoaded = false;

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

        showDefaultProfileImage();

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
                        ? profileName
                        .getText()
                        .toString()
                        : "Sanskriti Sathi User";

        String username =
                profileUsername != null
                        ? profileUsername
                        .getText()
                        .toString()
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
