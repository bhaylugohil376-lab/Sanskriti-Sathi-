package com.sanskritisathi.app;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReelFirebaseHelper {

    public interface UploadCallback {
        void onProgress(int progress);
        void onSuccess(String reelId);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    private ReelFirebaseHelper() {
    }

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

        String uid = auth.getCurrentUser().getUid();
        String reelId = UUID.randomUUID().toString();

        StorageReference videoRef = storage
                .getReference()
                .child("reels")
                .child(uid)
                .child(reelId + ".mp4");

        UploadTask uploadTask = videoRef.putFile(videoUri);

        uploadTask.addOnProgressListener(snapshot -> {
            long total = snapshot.getTotalByteCount();
            long uploaded = snapshot.getBytesTransferred();

            int progress = total > 0
                    ? (int) ((uploaded * 100L) / total)
                    : 0;

            callback.onProgress(progress);

        }).addOnSuccessListener(taskSnapshot ->
                videoRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {

                    saveReelDocument(
                            reelId,
                            uid,
                            downloadUri.toString(),
                            caption,
                            visibility,
                            callback
                    );

                }).addOnFailureListener(e ->
                        callback.onError("Video URL failed: " + e.getMessage())
                )

        ).addOnFailureListener(e ->
                callback.onError("Video upload failed: " + e.getMessage())
        );
    }

    private static void saveReelDocument(
            String reelId,
            String uid,
            String videoUrl,
            String caption,
            String visibility,
            UploadCallback callback) {

        String username = auth.getCurrentUser().getDisplayName();

        if (username == null || username.trim().isEmpty()) {
            username = "Sanskriti User";
        }

        Map<String, Object> reel = new HashMap<>();

        reel.put("reelId", reelId);
        reel.put("ownerUid", uid);
        reel.put("username", username);
        reel.put("videoUrl", videoUrl);
        reel.put("thumbnailUrl", "");
        reel.put("caption", caption == null ? "" : caption.trim());
        reel.put("visibility",
                "Followers".equals(visibility) ? "Followers" : "Public");

        reel.put("createdAt", System.currentTimeMillis());
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
                        callback.onError("Reel save failed: " + e.getMessage())
                );
    }

    public static void deleteReel(
            String reelId,
            ActionCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onError("Please login first.");
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("reels")
                .document(reelId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        callback.onError("Reel not found.");
                        return;
                    }

                    String ownerUid = document.getString("ownerUid");

                    if (ownerUid == null || !uid.equals(ownerUid)) {
                        callback.onError("You can delete only your own reel.");
                        return;
                    }

                    deleteStorageFile(
                            document.getString("videoUrl"),
                            reelId,
                            callback
                    );

                })
                .addOnFailureListener(e ->
                        callback.onError("Permission check failed: " + e.getMessage())
                );
    }

    private static void deleteStorageFile(
            String videoUrl,
            String reelId,
            ActionCallback callback) {

        StorageReference reference;

        if (videoUrl != null && !videoUrl.isEmpty()) {
            try {
                reference = storage.getReferenceFromUrl(videoUrl);
            } catch (Exception e) {
                reference = null;
            }
        } else {
            reference = null;
        }

        if (reference == null) {
            deleteReelDocument(reelId, callback);
            return;
        }

        reference.delete()
                .addOnSuccessListener(unused ->
                        deleteReelDocument(reelId, callback)
                )
                .addOnFailureListener(e ->
                        deleteReelDocument(reelId, callback)
                );
    }

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
                        callback.onError("Reel delete failed: " + e.getMessage())
                );
    }
}
