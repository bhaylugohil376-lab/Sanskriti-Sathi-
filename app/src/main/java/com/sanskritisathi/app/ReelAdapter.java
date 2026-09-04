package com.sanskritisathi.app;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReelFirebaseHelper {

    // =========================
    // CALLBACKS
    // =========================

    public interface UploadCallback {
        void onProgress(int progress);
        void onSuccess(String reelId);
        void onError(String message);
    }

    public interface ReelsCallback {
        void onSuccess(List<Reel> reels);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    // =========================
    // FIREBASE
    // =========================

    private static final FirebaseAuth auth =
            FirebaseAuth.getInstance();

    private static final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private static final FirebaseStorage storage =
            FirebaseStorage.getInstance();

    private ReelFirebaseHelper() {
        // Prevent object creation
    }

    // =========================
    // UPLOAD REEL
    // =========================

    public static void uploadReel(
            Uri videoUri,
            String caption,
            String visibility,
            UploadCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onError("Please login first.");
            return;
        }

        if (videoUri == null) {
            callback.onError("Please select a video.");
            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        String reelId =
                UUID.randomUUID().toString();

        StorageReference videoRef =
                storage.getReference()
                        .child("reels")
                        .child(uid)
                        .child(reelId + ".mp4");

        UploadTask uploadTask =
                videoRef.putFile(videoUri);

        uploadTask.addOnProgressListener(snapshot -> {

            long total =
                    snapshot.getTotalByteCount();

            long uploaded =
                    snapshot.getBytesTransferred();

            int progress =
                    total > 0
                            ? (int) ((uploaded * 100L) / total)
                            : 0;

            callback.onProgress(progress);

        }).addOnSuccessListener(taskSnapshot -> {

            videoRef.getDownloadUrl()
                    .addOnSuccessListener(downloadUri -> {

                        saveReelDocument(
                                reelId,
                                uid,
                                downloadUri.toString(),
                                caption,
                                visibility,
                                callback
                        );

                    })
                    .addOnFailureListener(e ->
                            callback.onError(
                                    "Video URL failed: "
                                            + e.getMessage()
                            )
                    );

        }).addOnFailureListener(e ->
                callback.onError(
                        "Video upload failed: "
                                + e.getMessage()
                )
        );
    }

    // =========================
    // SAVE REEL
    // =========================

    private static void saveReelDocument(
            String reelId,
            String uid,
            String videoUrl,
            String caption,
            String visibility,
            UploadCallback callback) {

        String username =
                auth.getCurrentUser().getDisplayName();

        if (username == null ||
                username.trim().isEmpty()) {

            username = "Sanskriti User";
        }

        Map<String, Object> reel =
                new HashMap<>();

        reel.put("reelId", reelId);
        reel.put("ownerUid", uid);
        reel.put("username", username);
        reel.put("videoUrl", videoUrl);
        reel.put("thumbnailUrl", "");

        reel.put(
                "caption",
                caption == null
                        ? ""
                        : caption.trim()
        );

        reel.put(
                "visibility",
                "Followers".equals(visibility)
                        ? "Followers"
                        : "Public"
        );

        reel.put(
                "createdAt",
                System.currentTimeMillis()
        );

        reel.put("likes", 0);
        reel.put("comments", 0);
        reel.put("views", 0);

        db.collection("reels")
                .document(reelId)
                .set(reel)
                .addOnSuccessListener(unused ->
                        callback.onSuccess(reelId)
                )
                .addOnFailureListener(e ->
                        callback.onError(
                                "Reel save failed: "
                                        + e.getMessage()
                        )
                );
    }

    // =========================
    // GET ACTIVE REELS
    // =========================

    public static void getActiveReels(
            ReelsCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onError("Please login first.");
            return;
        }

        long twentyFourHoursAgo =
                System.currentTimeMillis()
                        - (24L * 60L * 60L * 1000L);

        db.collection("reels")
                .whereGreaterThan(
                        "createdAt",
                        twentyFourHoursAgo
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<Reel> reels =
                            new ArrayList<>();

                    String currentUid =
                            auth.getCurrentUser()
                                    .getUid();

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        String id =
                                document.getId();

                        String ownerUid =
                                document.getString("ownerUid");

                        String username =
                                document.getString("username");

                        String videoUrl =
                                document.getString("videoUrl");

                        String thumbnailUrl =
                                document.getString("thumbnailUrl");

                        String caption =
                                document.getString("caption");

                        String visibility =
                                document.getString("visibility");

                        Long createdAtValue =
                                document.getLong("createdAt");

                        Long likesValue =
                                document.getLong("likes");

                        Long commentsValue =
                                document.getLong("comments");

                        Long viewsValue =
                                document.getLong("views");

                        long createdAt =
                                createdAtValue != null
                                        ? createdAtValue
                                        : 0;

                        int likes =
                                likesValue != null
                                        ? likesValue.intValue()
                                        : 0;

                        int comments =
                                commentsValue != null
                                        ? commentsValue.intValue()
                                        : 0;

                        int views =
                                viewsValue != null
                                        ? viewsValue.intValue()
                                        : 0;

                        boolean ownReel =
                                currentUid.equals(ownerUid);

                        Reel reel =
                                new Reel(
                                        id,
                                        ownerUid,
                                        username == null
                                                ? "Sanskriti User"
                                                : username,
                                        videoUrl,
                                        thumbnailUrl,
                                        caption == null
                                                ? ""
                                                : caption,
                                        visibility == null
                                                ? "Public"
                                                : visibility,
                                        createdAt,
                                        likes,
                                        comments,
                                        views,
                                        false,
                                        ownReel
                                );

                        reels.add(reel);
                    }

                    reels.sort((a, b) ->
                            Long.compare(
                                    b.getCreatedAt(),
                                    a.getCreatedAt()
                            )
                    );

                    callback.onSuccess(reels);

                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Reels load failed: "
                                        + e.getMessage()
                        )
                );
    }

    // =========================
    // TOGGLE REEL LIKE ❤️
    // =========================

    public static void toggleReelLike(
            String reelId,
            boolean currentlyLiked,
            ActionCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onError("Please login first.");
            return;
        }

        if (reelId == null ||
                reelId.trim().isEmpty()) {

            callback.onError("Invalid Reel.");
            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        com.google.firebase.firestore.DocumentReference reelRef =
                db.collection("reels")
                        .document(reelId);

        com.google.firebase.firestore.DocumentReference likeRef =
                reelRef.collection("likes")
                        .document(uid);

        db.runTransaction(transaction -> {

            DocumentSnapshot reelSnapshot =
                    transaction.get(reelRef);

            if (!reelSnapshot.exists()) {
                throw new FirebaseFirestoreException(
                        "Reel not found.",
                        FirebaseFirestoreException.Code.NOT_FOUND
                );
            }

            Long likesValue =
                    reelSnapshot.getLong("likes");

            int likes =
                    likesValue != null
                            ? likesValue.intValue()
                            : 0;

            if (currentlyLiked) {

                // UNLIKE
                transaction.delete(likeRef);

                likes =
                        Math.max(
                                0,
                                likes - 1
                        );

            } else {

                // LIKE
                DocumentSnapshot likeSnapshot =
                        transaction.get(likeRef);

                if (!likeSnapshot.exists()) {

                    Map<String, Object> likeData =
                            new HashMap<>();

                    likeData.put(
                            "userId",
                            uid
                    );

                    likeData.put(
                            "createdAt",
                            System.currentTimeMillis()
                    );

                    transaction.set(
                            likeRef,
                            likeData
                    );

                    likes++;
                }
            }

            Map<String, Object> update =
                    new HashMap<>();

            update.put(
                    "likes",
                    likes
            );

            transaction.update(
                    reelRef,
                    update
            );

            return null;

        }).addOnSuccessListener(unused ->
                callback.onSuccess()
        ).addOnFailureListener(e ->
                callback.onError(
                        "Like update failed: "
                                + e.getMessage()
                )
        );
    }

    // =========================
    // CHECK REEL LIKE ❤️
    // =========================

    public static void checkReelLike(
            String reelId,
            ActionCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onError("Please login first.");
            return;
        }

        if (reelId == null ||
                reelId.trim().isEmpty()) {

            callback.onError("Invalid Reel.");
            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        db.collection("reels")
                .document(reelId)
                .collection("likes")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        callback.onSuccess();

                    } else {

                        callback.onError(
                                "NOT_LIKED"
                        );
                    }

                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Like check failed: "
                                        + e.getMessage()
                        )
                );
    }

    // =========================
    // DELETE REEL
    // =========================

    public static void deleteReel(
            String reelId,
            ActionCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onError("Please login first.");
            return;
        }

        if (reelId == null ||
                reelId.trim().isEmpty()) {

            callback.onError("Invalid Reel.");
            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        db.collection("reels")
                .document(reelId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        callback.onError(
                                "Reel not found."
                        );

                        return;
                    }

                    String ownerUid =
                            document.getString(
                                    "ownerUid"
                            );

                    if (ownerUid == null ||
                            !uid.equals(ownerUid)) {

                        callback.onError(
                                "You can delete only your own reel."
                        );

                        return;
                    }

                    String videoUrl =
                            document.getString(
                                    "videoUrl"
                            );

                    deleteStorageFile(
                            videoUrl,
                            reelId,
                            callback
                    );

                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Permission check failed: "
                                        + e.getMessage()
                        )
                );
    }

    // =========================
    // DELETE STORAGE VIDEO
    // =========================

    private static void deleteStorageFile(
            String videoUrl,
            String reelId,
            ActionCallback callback) {

        StorageReference reference = null;

        if (videoUrl != null &&
                !videoUrl.isEmpty()) {

            try {

                reference =
                        storage.getReferenceFromUrl(
                                videoUrl
                        );

            } catch (Exception ignored) {
                reference = null;
            }
        }

        if (reference == null) {

            deleteReelDocument(
                    reelId,
                    callback
            );

            return;
        }

        StorageReference finalReference =
                reference;

        finalReference.delete()
                .addOnSuccessListener(unused ->
                        deleteReelDocument(
                                reelId,
                                callback
                        )
                )
                .addOnFailureListener(e ->
                        deleteReelDocument(
                                reelId,
                                callback
                        )
                );
    }

    // =========================
    // DELETE FIRESTORE DOCUMENT
    // =========================

    private static void deleteReelDocument(
            String reelId,
            ActionCallback callback) {

        db.collection("reels")
                .document(reelId)
                .delete()
                .addOnSuccessListener(unused ->
                        callback.onSuccess()
                )
                .addOnFailureListener(e ->
                        callback.onError(
                                "Reel delete failed: "
                                        + e.getMessage()
                        )
                );
    }
}
