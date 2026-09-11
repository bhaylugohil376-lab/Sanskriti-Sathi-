package com.sanskritisathi.app;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public final class FirebaseStorageConnectionTest {

    private FirebaseStorageConnectionTest() {
    }

    public static void run(Context context) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(
                    context,
                    "⚠️ Storage test ke liye pehle Login karo",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();

            StorageReference testReference = storage
                    .getReference()
                    .child("firebase_connection_test")
                    .child("connection.txt");

            testReference.getMetadata()
                    .addOnSuccessListener(metadata -> {

                        Toast.makeText(
                                context,
                                "✅ Firebase Storage CONNECTED",
                                Toast.LENGTH_LONG
                        ).show();
                    })
                    .addOnFailureListener(e -> {

                        String error = e.getMessage();

                        if (error == null) {
                            error = "Unknown Storage error";
                        }

                        Toast.makeText(
                                context,
                                "❌ Firebase Storage request FAILED\n\n"
                                        + error,
                                Toast.LENGTH_LONG
                        ).show();
                    });

        } catch (Exception e) {

            Toast.makeText(
                    context,
                    "❌ Firebase Storage SDK error\n\n"
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
