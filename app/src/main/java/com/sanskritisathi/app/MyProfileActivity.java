package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView nameText;
    private TextView usernameText;
    private TextView bioText;

    private TextView postsCountText;
    private TextView followersCountText;
    private TextView followingCountText;

    private Button editProfileButton;
    private Button shareProfileButton;

    private TextView settingsButton;
    private TextView postsTab;
    private TextView reelsTab;
    private TextView repostsTab;
    private TextView taggedTab;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        profileImage = findViewById(R.id.profileImage);
        nameText = findViewById(R.id.nameText);
        usernameText = findViewById(R.id.usernameText);
        bioText = findViewById(R.id.bioText);

        postsCountText = findViewById(R.id.postsCountText);
        followersCountText = findViewById(R.id.followersCountText);
        followingCountText = findViewById(R.id.followingCountText);

        editProfileButton = findViewById(R.id.editProfileButton);
        shareProfileButton = findViewById(R.id.shareProfileButton);

        settingsButton = findViewById(R.id.settingsButton);

        postsTab = findViewById(R.id.postsTab);
        reelsTab = findViewById(R.id.reelsTab);
        repostsTab = findViewById(R.id.repostsTab);
        taggedTab = findViewById(R.id.taggedTab);

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(
                    this,
                    "Pehle Login karein",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        loadProfile(currentUser);

        editProfileButton.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            MyProfileActivity.this,
                            ProfileActivity.class
                    )
            );
        });

        shareProfileButton.setOnClickListener(v -> shareProfile());

        settingsButton.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            MyProfileActivity.this,
                            SettingsActivity.class
                    )
            );
        });

        postsTab.setOnClickListener(v -> selectTab(postsTab));
        reelsTab.setOnClickListener(v -> selectTab(reelsTab));
        repostsTab.setOnClickListener(v -> selectTab(repostsTab));
        taggedTab.setOnClickListener(v -> selectTab(taggedTab));
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            loadProfile(currentUser);
        }
    }

    private void loadProfile(FirebaseUser currentUser) {

        String uid = currentUser.getUid();

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        showDefaultProfile();
                        return;
                    }

                    String name = document.getString("name");
                    String username = document.getString("username");
                    String bio = document.getString("bio");

                    nameText.setText(
                            TextUtils.isEmpty(name)
                                    ? "Sanskriti Sathi User"
                                    : name
                    );

                    if (TextUtils.isEmpty(username)) {
                        usernameText.setText("@user");
                    } else if (username.startsWith("@")) {
                        usernameText.setText(username);
                    } else {
                        usernameText.setText("@" + username);
                    }

                    bioText.setText(
                            TextUtils.isEmpty(bio)
                                    ? "Apni Sanskriti se judein."
                                    : bio
                    );

                    postsCountText.setText(
                            String.valueOf(
                                    getLongValue(
                                            document.getLong("posts")
                                    )
                            )
                    );

                    followersCountText.setText(
                            String.valueOf(
                                    getLongValue(
                                            document.getLong("followers")
                                    )
                            )
                    );

                    followingCountText.setText(
                            String.valueOf(
                                    getLongValue(
                                            document.getLong("following")
                                    )
                            )
                    );

                    String imageUrl =
                            document.getString("profileImageUrl");

                    if (TextUtils.isEmpty(imageUrl)) {
                        profileImage.setImageResource(
                                R.drawable.icon_foreground
                        );
                    }

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            MyProfileActivity.this,
                            "Profile load nahi hui.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void showDefaultProfile() {

        nameText.setText("Sanskriti Sathi User");
        usernameText.setText("@user");
        bioText.setText("Apni Sanskriti se judein.");

        postsCountText.setText("0");
        followersCountText.setText("0");
        followingCountText.setText("0");

        profileImage.setImageResource(
                R.drawable.icon_foreground
        );
    }

    private long getLongValue(Long value) {

        if (value == null) {
            return 0;
        }

        return value;
    }

    private void selectTab(TextView selected) {

        postsTab.setTextColor(
                selected == postsTab
                        ? 0xFFFFB300
                        : 0xFFAAB2C0
        );

        reelsTab.setTextColor(
                selected == reelsTab
                        ? 0xFFFFB300
                        : 0xFFAAB2C0
        );

        repostsTab.setTextColor(
                selected == repostsTab
                        ? 0xFFFFB300
                        : 0xFFAAB2C0
        );

        taggedTab.setTextColor(
                selected == taggedTab
                        ? 0xFFFFB300
                        : 0xFFAAB2C0
        );

        Toast.makeText(
                this,
                selected == postsTab
                        ? "Posts"
                        : selected == reelsTab
                        ? "Reels"
                        : selected == repostsTab
                        ? "Reposts"
                        : "Tagged",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void shareProfile() {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String username =
                usernameText.getText().toString();

        String shareText =
                "Sanskriti Sathi par " +
                username +
                " ki profile dekhein.";

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
}
