package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
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

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

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

        saveProfileButton.setOnClickListener(v -> saveProfile(currentUser));
    }

    private void loadProfile(String uid) {

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        String username = documentSnapshot.getString("username");
                        String bio = documentSnapshot.getString("bio");

                        if (name != null) {
                            nameInput.setText(name);
                        }

                        if (username != null) {
                            usernameInput.setText(username);
                        }

                        if (bio != null) {
                            bioInput.setText(bio);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Profile load nahi hua",
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void saveProfile(FirebaseUser currentUser) {

        String name = nameInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String bio = bioInput.getText().toString().trim();

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
            usernameInput.setError("Username में space नहीं होना चाहिए");
            usernameInput.requestFocus();
            return;
        }

        Map<String, Object> profile = new HashMap<>();

        profile.put("uid", currentUser.getUid());
        profile.put("name", name);
        profile.put("username", username);
        profile.put("email", currentUser.getEmail());
        profile.put("bio", bio);
        profile.put("profileImageUrl", "");
        profile.put("updatedAt",
                com.google.firebase.firestore.FieldValue.serverTimestamp());

        saveProfileButton.setEnabled(false);

        firestore.collection("users")
                .document(currentUser.getUid())
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
                            "Profile save nahi hui",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}
