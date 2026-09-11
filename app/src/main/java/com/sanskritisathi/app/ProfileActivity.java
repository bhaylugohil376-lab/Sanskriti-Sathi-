package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText usernameInput;
    private EditText bioInput;
    private Button saveProfileButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        nameInput = findViewById(R.id.nameInput);
        usernameInput = findViewById(R.id.usernameInput);
        bioInput = findViewById(R.id.bioInput);
        saveProfileButton = findViewById(R.id.saveProfileButton);

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(
                    this,
                    "Pehle Login karein",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        loadProfile(currentUser.getUid());

        saveProfileButton.setOnClickListener(
                v -> saveProfile(currentUser)
        );
    }

    private void loadProfile(String uid) {

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {
                        return;
                    }

                    String name =
                            documentSnapshot.getString("name");

                    String username =
                            documentSnapshot.getString("username");

                    String bio =
                            documentSnapshot.getString("bio");

                    if (!TextUtils.isEmpty(name)) {
                        nameInput.setText(name);
                    }

                    if (!TextUtils.isEmpty(username)) {
                        usernameInput.setText(username);
                    }

                    if (!TextUtils.isEmpty(bio)) {
                        bioInput.setText(bio);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Profile load nahi hui",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void saveProfile(
            FirebaseUser currentUser
    ) {

        String name =
                nameInput.getText()
                        .toString()
                        .trim();

        String username =
                usernameInput.getText()
                        .toString()
                        .trim();

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

        /*
         * Username ko clean format mein rakhenge.
         * Example:
         * @raja123 -> raja123
         */
        username =
                username.replace(
                        "@",
                        ""
                ).trim();

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

        /*
         * Search ke liye normalized fields.
         */
        String nameLower =
                name.toLowerCase(
                        Locale.ROOT
                );

        String usernameLower =
                username.toLowerCase(
                        Locale.ROOT
                );

        Map<String, Object> profile =
                new HashMap<>();

        profile.put(
                "uid",
                currentUser.getUid()
        );

        profile.put(
                "name",
                name
        );

        profile.put(
                "nameLower",
                nameLower
        );

        profile.put(
                "username",
                username
        );

        profile.put(
                "usernameLower",
                usernameLower
        );

        profile.put(
                "email",
                currentUser.getEmail()
        );

        profile.put(
                "bio",
                bio
        );

        /*
         * Existing profile photo URL preserve karenge.
         * Isse profile save karne par photo URL blank nahi hoga.
         */
        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(existingDocument -> {

                    String oldProfileImageUrl =
                            existingDocument.getString(
                                    "profileImageUrl"
                            );

                    if (oldProfileImageUrl == null) {
                        oldProfileImageUrl = "";
                    }

                    profile.put(
                            "profileImageUrl",
                            oldProfileImageUrl
                    );

                    profile.put(
                            "updatedAt",
                            FieldValue.serverTimestamp()
                    );

                    saveProfileToFirestore(
                            currentUser.getUid(),
                            profile
                    );
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Profile data load nahi hua",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void saveProfileToFirestore(
            String uid,
            Map<String, Object> profile
    ) {

        saveProfileButton.setEnabled(false);

        firestore.collection("users")
                .document(uid)
                .set(profile)
                .addOnSuccessListener(unused -> {

                    saveProfileButton.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Profile save ho gayi ✅",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    saveProfileButton.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Profile save nahi hui: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
