package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserProfileActivity
        extends AppCompatActivity {

    private ImageView profileImage;
    private TextView nameText;
    private TextView usernameText;
    private TextView bioText;

    private TextView postsCountText;
    private TextView followersCountText;
    private TextView followingCountText;

    private TextView followButton;

    private String userUid;

    private boolean isFollowing = false;
    private boolean actionRunning = false;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_user_profile
        );

        profileImage =
                findViewById(R.id.profileImage);

        nameText =
                findViewById(R.id.nameText);

        usernameText =
                findViewById(R.id.usernameText);

        bioText =
                findViewById(R.id.bioText);

        postsCountText =
                findViewById(R.id.postsCountText);

        followersCountText =
                findViewById(
                        R.id.followersCountText
                );

        followingCountText =
                findViewById(
                        R.id.followingCountText
                );

        followButton =
                findViewById(R.id.followButton);

        userUid =
                getIntent().getStringExtra(
                        "user_uid"
                );

        if (TextUtils.isEmpty(userUid)) {

            Toast.makeText(
                    this,
                    "User profile nahi mili.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        loadUserProfile();
        checkFollowStatus();

        followButton.setOnClickListener(
                v -> toggleFollow()
        );
    }

    // =========================================================
    // LOAD PROFILE FROM SUPABASE
    // =========================================================

    private void loadUserProfile() {

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {

                String urlString =
                        SupabaseConfig.PROJECT_URL
                                + "/rest/v1/users"
                                + "?select=*"
                                + "&id=eq."
                                + encode(userUid)
                                + "&limit=1";

                URL url =
                        new URL(urlString);

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

                addHeaders(connection);

                int code =
                        connection.getResponseCode();

                String response =
                        readResponse(
                                connection,
                                code
                        );

                if (code < 200 ||
                        code >= 300) {

                    postToast(
                            "Profile load nahi hui."
                    );

                    return;
                }

                JSONArray array =
                        new JSONArray(response);

                if (array.length() == 0) {

                    postToast(
                            "User profile available nahi hai."
                    );

                    return;
                }

                JSONObject user =
                        array.getJSONObject(0);

                String name =
                        user.optString(
                                "name",
                                "Sanskriti Sathi User"
                        );

                String username =
                        user.optString(
                                "username",
                                "user"
                        );

                String bio =
                        user.optString(
                                "bio",
                                "Apni Sanskriti se judein."
                        );

                String imageUrl =
                        user.optString(
                                "profileImageUrl",
                                user.optString(
                                        "profile_image",
                                        ""
                                )
                        );

                int posts =
                        Math.max(
                                0,
                                user.optInt(
                                        "posts",
                                        0
                                )
                        );

                int followers =
                        Math.max(
                                0,
                                user.optInt(
                                        "followers",
                                        0
                                )
                        );

                int following =
                        Math.max(
                                0,
                                user.optInt(
                                        "following",
                                        0
                                )
                        );

                runOnUiThread(() -> {

                    nameText.setText(
                            TextUtils.isEmpty(name)
                                    ? "Sanskriti Sathi User"
                                    : name
                    );

                    if (username.startsWith("@")) {
                        usernameText.setText(
                                username
                        );
                    } else {
                        usernameText.setText(
                                "@" + username
                        );
                    }

                    bioText.setText(
                            TextUtils.isEmpty(bio)
                                    ? "Apni Sanskriti se judein."
                                    : bio
                    );

                    postsCountText.setText(
                            String.valueOf(posts)
                    );

                    followersCountText.setText(
                            String.valueOf(followers)
                    );

                    followingCountText.setText(
                            String.valueOf(following)
                    );

                    if (!TextUtils.isEmpty(
                            imageUrl
                    )) {

                        Glide.with(
                                UserProfileActivity.this
                        )
                                .load(imageUrl)
                                .placeholder(
                                        R.drawable.icon_foreground
                                )
                                .error(
                                        R.drawable.icon_foreground
                                )
                                .into(profileImage);
                    }
                });

            } catch (Exception e) {

                postToast(
                        "Profile load nahi hui."
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    // =========================================================
    // CHECK FOLLOW
    // =========================================================

    private void checkFollowStatus() {

        FollowSupabaseHelper.checkFollowing(
                this,
                userUid,
                new FollowSupabaseHelper.StatusCallback() {

                    @Override
                    public void onResult(
                            boolean following
                    ) {

                        isFollowing =
                                following;

                        updateFollowButton();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                UserProfileActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // =========================================================
    // TOGGLE
    // =========================================================

    private void toggleFollow() {

        if (actionRunning) {
            return;
        }

        if (isFollowing) {
            unfollowUser();
        } else {
            followUser();
        }
    }

    // =========================================================
    // FOLLOW
    // =========================================================

    private void followUser() {

        actionRunning = true;

        followButton.setEnabled(false);
        followButton.setText(
                "Following..."
        );

        FollowSupabaseHelper.followUser(
                this,
                userUid,
                new FollowSupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        isFollowing = true;
                        actionRunning = false;

                        followButton.setEnabled(
                                true
                        );

                        updateFollowButton();
                        loadUserProfile();

                        Toast.makeText(
                                UserProfileActivity.this,
                                "Following",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        actionRunning = false;

                        followButton.setEnabled(
                                true
                        );

                        updateFollowButton();

                        Toast.makeText(
                                UserProfileActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =========================================================
    // UNFOLLOW
    // =========================================================

    private void unfollowUser() {

        actionRunning = true;

        followButton.setEnabled(false);
        followButton.setText(
                "Unfollowing..."
        );

        FollowSupabaseHelper.unfollowUser(
                this,
                userUid,
                new FollowSupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        isFollowing = false;
                        actionRunning = false;

                        followButton.setEnabled(
                                true
                        );

                        updateFollowButton();
                        loadUserProfile();

                        Toast.makeText(
                                UserProfileActivity.this,
                                "Unfollowed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        actionRunning = false;

                        followButton.setEnabled(
                                true
                        );

                        updateFollowButton();

                        Toast.makeText(
                                UserProfileActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private void updateFollowButton() {

        followButton.setText(
                isFollowing
                        ? "Following"
                        : "Follow"
        );
    }

    // =========================================================
    // SUPABASE HEADERS
    // =========================================================

    private void addHeaders(
            HttpURLConnection connection
    ) {

        String token =
                SupabaseAuthManager
                        .getAccessToken(this);

        if (TextUtils.isEmpty(token)) {
            token =
                    SupabaseConfig.PUBLISHABLE_KEY;
        }

        connection.setRequestProperty(
                "apikey",
                SupabaseConfig.PUBLISHABLE_KEY
        );

        connection.setRequestProperty(
                "Authorization",
                "Bearer " + token
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    private String readResponse(
            HttpURLConnection connection,
            int code
    ) throws Exception {

        InputStream stream =
                code >= 200 && code < 400
                        ? connection.getInputStream()
                        : connection.getErrorStream();

        if (stream == null) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        stream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while ((line =
                    reader.readLine()) != null) {

                result.append(line);
            }
        }

        return result.toString();
    }

    // =========================================================
    // ENCODE
    // =========================================================

    private String encode(
            String value
    ) throws Exception {

        return java.net.URLEncoder.encode(
                value,
                StandardCharsets.UTF_8.name()
        );
    }

    // =========================================================
    // TOAST
    // =========================================================

    private void postToast(
            String message
    ) {

        runOnUiThread(() ->
                Toast.makeText(
                        UserProfileActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    @Override
    protected void onDestroy() {

        executor.shutdownNow();

        super.onDestroy();
    }
}
