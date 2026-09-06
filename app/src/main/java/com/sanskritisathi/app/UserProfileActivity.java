package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView nameText;
    private TextView usernameText;
    private TextView bioText;

    private TextView postsCountText;
    private TextView followersCountText;
    private TextView followingCountText;

    private TextView followButton;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        profileImage = findViewById(R.id.profileImage);
        nameText = findViewById(R.id.nameText);
        usernameText = findViewById(R.id.usernameText);
        bioText = findViewById(R.id.bioText);

        postsCountText = findViewById(R.id.postsCountText);
        followersCountText = findViewById(R.id.followersCountText);
        followingCountText = findViewById(R.id.followingCountText);

        followButton = findViewById(R.id.followButton);

        userUid = getIntent().getStringExtra("user_uid");

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

        followButton.setOnClickListener(v -> {
            Toast.makeText(
                    UserProfileActivity.this,
                    "Follow system next step mein connect hoga.",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void loadUserProfile() {

        firestore.collection("users")
                .document(userUid)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        Toast.makeText(
                                UserProfileActivity.this,
                                "User profile available nahi hai.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    String name =
                            document.getString("name");

                    String username =
                            document.getString("username");

                    String bio =
                            document.getString("bio");

                    if (!TextUtils.isEmpty(name)) {
                        nameText.setText(name);
                    } else {
                        nameText.setText("Sanskriti Sathi User");
                    }

                    if (!TextUtils.isEmpty(username)) {

                        if (username.startsWith("@")) {
                            usernameText.setText(username);
                        } else {
                            usernameText.setText(
                                    "@" + username
                            );
                        }

                    } else {
                        usernameText.setText("@user");
                    }

                    if (!TextUtils.isEmpty(bio)) {
                        bioText.setText(bio);
                    } else {
                        bioText.setText(
                                "Apni Sanskriti se judein."
                        );
                    }

                    Long posts =
                            document.getLong("posts");

                    Long followers =
                            document.getLong("followers");

                    Long following =
                            document.getLong("following");

                    postsCountText.setText(
                            String.valueOf(
                                    posts != null ? posts : 0
                            )
                    );

                    followersCountText.setText(
                            String.valueOf(
                                    followers != null ? followers : 0
                            )
                    );

                    followingCountText.setText(
                            String.valueOf(
                                    following != null ? following : 0
                            )
                    );

                    String imageUrl =
                            document.getString(
                                    "profileImageUrl"
                            );

                    if (!TextUtils.isEmpty(imageUrl)) {
                        profileImage.setContentDescription(
                                "User profile photo"
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            UserProfileActivity.this,
                            "Profile load nahi hui.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}
