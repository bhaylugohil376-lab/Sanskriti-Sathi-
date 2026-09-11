package com.sanskritisathi.app;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public final class FirebaseConnectionTest {

    private static final String TAG = "FirebaseConnectionTest";

    private FirebaseConnectionTest() {
    }

    public static void run(Context context) {

        // 1️⃣ Firebase SDK initialization test
        try {
            FirebaseApp.getInstance();
        } catch (Exception e) {
            show(context, "❌ Firebase SDK NOT connected");
            Log.e(TAG, "Firebase initialization failed", e);
            return;
        }

        // 2️⃣ Firebase Auth test
        FirebaseAuth auth;

        try {
            auth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            show(context, "❌ Firebase Auth NOT connected");
            Log.e(TAG, "Firebase Auth failed", e);
            return;
        }

        FirebaseUser user = auth.getCurrentUser();

        String authStatus;

        if (user != null) {
            authStatus = "✅ Auth: CONNECTED\nUser: " + user.getUid();
        } else {
            authStatus = "✅ Auth: CONNECTED\nNo user signed in";
        }

        Log.d(TAG, authStatus);

        // 3️⃣ REAL Firestore request
        FirebaseFirestore db;

        try {
            db = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            show(context, "❌ Firestore SDK NOT connected");
            Log.e(TAG, "Firestore initialization failed", e);
            return;
        }

        db.collection("firebase_connection_test")
                .document("connection")
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    String firestoreStatus =
                            "✅ Firestore: CONNECTED";

                    Log.d(TAG, firestoreStatus);

                    Toast.makeText(
                            context,
                            "🔥 FIREBASE TEST\n\n"
                                    + authStatus
                                    + "\n\n"
                                    + firestoreStatus,
                            Toast.LENGTH_LONG
                    ).show();
                })
                .addOnFailureListener(e -> {

                    String error = e.getMessage();

                    if (error == null) {
                        error = "Unknown Firestore error";
                    }

                    Log.e(TAG, "Firestore request failed", e);

                    Toast.makeText(
                            context,
                            "🔥 FIREBASE TEST\n\n"
                                    + authStatus
                                    + "\n\n"
                                    + "❌ Firestore request FAILED\n"
                                    + error,
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private static void show(Context context, String message) {
        Toast.makeText(
                context,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
