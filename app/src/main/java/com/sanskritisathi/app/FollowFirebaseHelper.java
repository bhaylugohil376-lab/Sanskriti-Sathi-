package com.sanskritisathi.app;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class FollowFirebaseHelper {

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface StatusCallback {
        void onResult(boolean following);
        void onError(String message);
    }

    private static final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private static final FirebaseAuth auth =
            FirebaseAuth.getInstance();

    private FollowFirebaseHelper() {
    }

    public static void checkFollowing(
            String targetUid,
            StatusCallback callback) {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            callback.onError("Pehle Login karein.");
            return;
        }

        if (targetUid == null || targetUid.trim().isEmpty()) {
            callback.onError("User profile nahi mili.");
            return;
        }

        if (currentUser.getUid().equals(targetUid)) {
            callback.onResult(false);
            return;
        }

        db.collection("users")
                .document(currentUser.getUid())
                .collection("following")
                .document(targetUid)
                .get()
                .addOnSuccessListener(document ->
                        callback.onResult(document.exists())
                )
                .addOnFailureListener(e ->
                        callback.onError(
                                "Follow status check nahi hua."
                        )
                );
    }

    public static void followUser(
            String targetUid,
            ActionCallback callback) {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            callback.onError("Pehle Login karein.");
            return;
        }

        String currentUid = currentUser.getUid();

        if (targetUid == null || targetUid.trim().isEmpty()) {
            callback.onError("User profile nahi mili.");
            return;
        }

        if (currentUid.equals(targetUid)) {
            callback.onError("Apne aap ko follow nahi kar sakte.");
            return;
        }

        db.runTransaction(transaction -> {

            DocumentSnapshot currentUserDoc =
                    transaction.get(
                            db.collection("users")
                                    .document(currentUid)
                    );

            DocumentSnapshot targetUserDoc =
                    transaction.get(
                            db.collection("users")
                                    .document(targetUid)
                    );

            if (!targetUserDoc.exists()) {
                throw new IllegalStateException(
                        "User profile available nahi hai."
                );
            }

            com.google.firebase.firestore.DocumentReference followingRef =
                    db.collection("users")
                            .document(currentUid)
                            .collection("following")
                            .document(targetUid);

            com.google.firebase.firestore.DocumentReference followerRef =
                    db.collection("users")
                            .document(targetUid)
                            .collection("followers")
                            .document(currentUid);

            DocumentSnapshot existingFollowing =
                    transaction.get(followingRef);

            if (existingFollowing.exists()) {
                return null;
            }

            Map<String, Object> followingData =
                    new HashMap<>();

            followingData.put("uid", targetUid);
            followingData.put("createdAt",
                    System.currentTimeMillis());

            Map<String, Object> followerData =
                    new HashMap<>();

            followerData.put("uid", currentUid);
            followerData.put("createdAt",
                    System.currentTimeMillis());

            transaction.set(followingRef, followingData);
            transaction.set(followerRef, followerData);

            long currentFollowing =
                    getLong(
                            currentUserDoc.getLong("following")
                    );

            long targetFollowers =
                    getLong(
                            targetUserDoc.getLong("followers")
                    );

            transaction.set(
                    db.collection("users")
                            .document(currentUid),
                    createCountMap(
                            "following",
                            currentFollowing + 1
                    ),
                    SetOptions.merge()
            );

            transaction.set(
                    db.collection("users")
                            .document(targetUid),
                    createCountMap(
                            "followers",
                            targetFollowers + 1
                    ),
                    SetOptions.merge()
            );

            return null;

        }).addOnSuccessListener(unused ->
                callback.onSuccess()
        ).addOnFailureListener(e ->
                callback.onError(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Follow nahi hua."
                )
        );
    }

    public static void unfollowUser(
            String targetUid,
            ActionCallback callback) {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            callback.onError("Pehle Login karein.");
            return;
        }

        String currentUid = currentUser.getUid();

        if (targetUid == null || targetUid.trim().isEmpty()) {
            callback.onError("User profile nahi mili.");
            return;
        }

        if (currentUid.equals(targetUid)) {
            callback.onError("Invalid user.");
            return;
        }

        db.runTransaction(transaction -> {

            DocumentSnapshot currentUserDoc =
                    transaction.get(
                            db.collection("users")
                                    .document(currentUid)
                    );

            DocumentSnapshot targetUserDoc =
                    transaction.get(
                            db.collection("users")
                                    .document(targetUid)
                    );

            if (!targetUserDoc.exists()) {
                throw new IllegalStateException(
                        "User profile available nahi hai."
                );
            }

            com.google.firebase.firestore.DocumentReference followingRef =
                    db.collection("users")
                            .document(currentUid)
                            .collection("following")
                            .document(targetUid);

            com.google.firebase.firestore.DocumentReference followerRef =
                    db.collection("users")
                            .document(targetUid)
                            .collection("followers")
                            .document(currentUid);

            DocumentSnapshot existingFollowing =
                    transaction.get(followingRef);

            if (!existingFollowing.exists()) {
                return null;
            }

            long currentFollowing =
                    getLong(
                            currentUserDoc.getLong("following")
                    );

            long targetFollowers =
                    getLong(
                            targetUserDoc.getLong("followers")
                    );

            transaction.delete(followingRef);
            transaction.delete(followerRef);

            transaction.set(
                    db.collection("users")
                            .document(currentUid),
                    createCountMap(
                            "following",
                            Math.max(0, currentFollowing - 1)
                    ),
                    SetOptions.merge()
            );

            transaction.set(
                    db.collection("users")
                            .document(targetUid),
                    createCountMap(
                            "followers",
                            Math.max(0, targetFollowers - 1)
                    ),
                    SetOptions.merge()
            );

            return null;

        }).addOnSuccessListener(unused ->
                callback.onSuccess()
        ).addOnFailureListener(e ->
                callback.onError(
                        e.getMessage() != null
                                ? e.getMessage()
                                : "Unfollow nahi hua."
                )
        );
    }

    private static long getLong(Long value) {
        return value == null ? 0 : value;
    }

    private static Map<String, Object> createCountMap(
            String field,
            long value) {

        Map<String, Object> map =
                new HashMap<>();

        map.put(field, value);

        return map;
    }
}
