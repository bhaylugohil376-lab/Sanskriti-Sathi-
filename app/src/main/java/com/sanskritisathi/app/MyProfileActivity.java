package com.sanskritisathi.app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MyProfileActivity extends AppCompatActivity {

    private ImageView profileImage;

    private android.widget.TextView nameText;
    private android.widget.TextView usernameText;
    private android.widget.TextView bioText;

    private android.widget.TextView postsCountText;
    private android.widget.TextView followersCountText;
    private android.widget.TextView followingCountText;

    private Button editProfileButton;
    private Button shareProfileButton;

    private ImageView settingsButton;

    private ImageView postsTab;
    private ImageView reelsTab;
    private ImageView repostsTab;
    private ImageView taggedTab;

    private LinearLayout newHighlightButton;
    private LinearLayout templeHighlightButton;
    private LinearLayout deviHighlightButton;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private final int ACTIVE_COLOR =
            Color.parseColor("#FFB300");

    private final int INACTIVE_COLOR =
            Color.parseColor("#AAB2C0");

    private final ActivityResultLauncher<String> profileImagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri != null) {
                            uploadProfilePhoto(uri);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_profile);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        profileImage = findViewById(R.id.profileImage);

        nameText = findViewById(R.id.nameText);
        usernameText = findViewById(R.id.usernameText);
        bioText = findViewById(R.id.bioText);

        postsCountText = findViewById(R.id.postsCountText);
        followersCountText = findViewById(R.id.followersCountText);
        followingCountText = findViewById(R.id.followingCountText);

        editProfileButton =
                findViewById(R.id.editProfileButton);

        shareProfileButton =
                findViewById(R.id.shareProfileButton);

        settingsButton =
                findViewById(R.id.settingsButton);

        postsTab =
                findViewById(R.id.postsTab);

        reelsTab =
                findViewById(R.id.reelsTab);

        repostsTab =
                findViewById(R.id.repostsTab);

        taggedTab =
                findViewById(R.id.taggedTab);

        newHighlightButton =
                findViewById(R.id.newHighlightButton);

        templeHighlightButton =
                findViewById(R.id.templeHighlightButton);

        deviHighlightButton =
                findViewById(R.id.deviHighlightButton);

        FirebaseUser currentUser =
                auth.getCurrentUser();

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

        // PROFILE PHOTO
        profileImage.setOnClickListener(v ->
                profileImagePicker.launch("image/*")
        );

        // EDIT PROFILE
        editProfileButton.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            MyProfileActivity.this,
                            ProfileActivity.class
                    )
            );
        });

        // SHARE PROFILE
        shareProfileButton.setOnClickListener(v ->
                shareProfile()
        );

        // SETTINGS
        settingsButton.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            MyProfileActivity.this,
                            SettingsActivity.class
                    )
            );
        });

        // TABS
        postsTab.setOnClickListener(v ->
                selectTab(postsTab)
        );

        reelsTab.setOnClickListener(v ->
                selectTab(reelsTab)
        );

        repostsTab.setOnClickListener(v ->
                selectTab(repostsTab)
        );

        taggedTab.setOnClickListener(v ->
                selectTab(taggedTab)
        );

        updateTabColors(postsTab);

        // HIGHLIGHTS

        newHighlightButton.setOnClickListener(v ->
                createNewHighlight()
        );

        templeHighlightButton.setOnClickListener(v ->
                openTempleHighlight()
        );

        deviHighlightButton.setOnClickListener(v ->
                openDeviDevtaHighlight()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser != null) {
            loadProfile(currentUser);
        }
    }

    // ==============================
    // LOAD PROFILE
    // ==============================

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

                    String name =
                            document.getString("name");

                    String username =
                            document.getString("username");

                    String bio =
                            document.getString("bio");

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
                            document.getString(
                                    "profileImageUrl"
                            );

                    if (!TextUtils.isEmpty(imageUrl)) {

                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(
                                        R.drawable.icon_foreground
                                )
                                .error(
                                        R.drawable.icon_foreground
                                )
                                .circleCrop()
                                .into(profileImage);

                    } else {

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

    // ==============================
    // DEFAULT PROFILE
    // ==============================

    private void showDefaultProfile() {

        nameText.setText(
                "Sanskriti Sathi User"
        );

        usernameText.setText("@user");

        bioText.setText(
                "Apni Sanskriti se judein."
        );

        postsCountText.setText("0");
        followersCountText.setText("0");
        followingCountText.setText("0");

        profileImage.setImageResource(
                R.drawable.icon_foreground
        );
    }

    // ==============================
    // PROFILE PHOTO UPLOAD
    // ==============================

    private void uploadProfilePhoto(Uri imageUri) {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String uid =
                currentUser.getUid();

        Toast.makeText(
                this,
                "Profile photo upload ho rahi hai...",
                Toast.LENGTH_SHORT
        ).show();

        StorageReference imageReference =
                storage.getReference()
                        .child(
                                "profile_images/"
                                        + uid
                                        + ".jpg"
                        );

        imageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {

                    imageReference
                            .getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {

                                String imageUrl =
                                        downloadUri.toString();

                                firestore.collection("users")
                                        .document(uid)
                                        .update(
                                                "profileImageUrl",
                                                imageUrl
                                        )
                                        .addOnSuccessListener(unused -> {

                                            Glide.with(
                                                    MyProfileActivity.this
                                            )
                                                    .load(imageUrl)
                                                    .circleCrop()
                                                    .into(profileImage);

                                            Toast.makeText(
                                                    MyProfileActivity.this,
                                                    "Profile photo update ho gayi ✅",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(
                                                        MyProfileActivity.this,
                                                        "Photo URL save nahi hua.",
                                                        Toast.LENGTH_SHORT
                                                ).show()
                                        );

                            });

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            MyProfileActivity.this,
                            "Profile photo upload failed.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // ==============================
    // HIGHLIGHTS
    // ==============================

    private void createNewHighlight() {

        Toast.makeText(
                this,
                "New Highlight feature ready hai ✨",
                Toast.LENGTH_SHORT
        ).show();

        /*
         * Next phase:
         * User apni Story se Highlight create karega.
         * Firebase me highlight data save hoga.
         */
    }

    private void openTempleHighlight() {

        Toast.makeText(
                this,
                "Temples Highlight",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent =
                new Intent(
                        MyProfileActivity.this,
                        TempleActivity.class
                );

        startActivity(intent);
    }

    private void openDeviDevtaHighlight() {

        Toast.makeText(
                this,
                "Devi Devta Highlight",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent =
                new Intent(
                        MyProfileActivity.this,
                        DeviDevtaActivity.class
                );

        startActivity(intent);
    }

    // ==============================
    // TABS
    // ==============================

    private void selectTab(ImageView selected) {

        updateTabColors(selected);

        String message;

        if (selected == postsTab) {

            message = "Posts";

        } else if (selected == reelsTab) {

            message = "Reels";

        } else if (selected == repostsTab) {

            message = "Reposts";

        } else {

            message = "Tagged";
        }

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void updateTabColors(ImageView selected) {

        postsTab.setImageTintList(
                ColorStateList.valueOf(
                        selected == postsTab
                                ? ACTIVE_COLOR
                                : INACTIVE_COLOR
                )
        );

        reelsTab.setImageTintList(
                ColorStateList.valueOf(
                        selected == reelsTab
                                ? ACTIVE_COLOR
                                : INACTIVE_COLOR
                )
        );

        repostsTab.setImageTintList(
                ColorStateList.valueOf(
                        selected == repostsTab
                                ? ACTIVE_COLOR
                                : INACTIVE_COLOR
                )
        );

        taggedTab.setImageTintList(
                ColorStateList.valueOf(
                        selected == taggedTab
                                ? ACTIVE_COLOR
                                : INACTIVE_COLOR
                )
        );
    }

    // ==============================
    // SHARE PROFILE
    // ==============================

    private void shareProfile() {

        FirebaseUser currentUser =
                auth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String username =
                usernameText.getText().toString();

        String shareText =
                "Sanskriti Sathi par "
                        + username
                        + " ki profile dekhein.";

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

    // ==============================
    // SAFE LONG VALUE
    // ==============================

    private long getLongValue(Long value) {

        if (value == null) {
            return 0;
        }

        return value;
    }
}
