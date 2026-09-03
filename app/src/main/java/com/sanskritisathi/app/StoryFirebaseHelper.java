package com.sanskritisathi.app;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class StoryFirebaseHelper {

    public interface UploadCallback {
        void onSuccess(String storyId);
        void onError(String message);
    }

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    public StoryFirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public void uploadStory(
            Uri imageUri,
            String caption,
            String visibility,
            UploadCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onError("Please login first");
            return;
        }

        if (imageUri == null) {
            callback.onError("Story image select karein");
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        String storyId =
                firestore.collection("stories")
                        .document()
                        .getId();

        StorageReference imageReference =
                storage.getReference()
                        .child("stories")
                        .child(uid)
                        .child(storyId + ".jpg");

        imageReference
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageReference.getDownloadUrl()
                                .addOnSuccessListener(downloadUri ->
                                        saveStoryDocument(
                                                storyId,
                                                uid,
                                                downloadUri.toString(),
                                                caption,
                                                visibility,
                                                callback
                                        )
                                )
                                .addOnFailureListener(e ->
                                        callback.onError(
                                                "Image URL create nahi hui: "
                                                        + e.getMessage()
                                        )
                                )
                )
                .addOnFailureListener(e ->
                        callback.onError(
                                "Story upload failed: "
                                        + e.getMessage()
                        )
                );
    }

    private void saveStoryDocument(
            String storyId,
            String uid,
            String imageUrl,
            String caption,
            String visibility,
            UploadCallback callback) {

        Map<String, Object> story = new HashMap<>();

        story.put("storyId", storyId);
        story.put("ownerUid", uid);
        story.put("imageUrl", imageUrl);
        story.put("caption", caption);
        story.put("visibility", visibility);
        story.put("createdAt", FieldValue.serverTimestamp());
        story.put("views", 0L);
        story.put("likes", 0L);

        firestore.collection("stories")
                .document(storyId)
                .set(story)
                .addOnSuccessListener(unused ->
                        callback.onSuccess(storyId)
                )
                .addOnFailureListener(e ->
                        callback.onError(
                                "Story database mein save nahi hui: "
                                        + e.getMessage()
                        )
                );
    }

    public void deleteStory(
            String storyId,
            String imagePath,
            UploadCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onError("Please login first");
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        firestore.collection("stories")
                .document(storyId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        callback.onError("Story nahi mili");
                        return;
                    }

                    String ownerUid =
                            document.getString("ownerUid");

                    if (ownerUid == null ||
                            !ownerUid.equals(uid)) {

                        callback.onError(
                                "Aap sirf apni Story delete kar sakte hain"
                        );

                        return;
                    }

                    firestore.collection("stories")
                            .document(storyId)
                            .delete()
                            .addOnSuccessListener(unused -> {

                                StorageReference imageReference =
                                        storage.getReference()
                                                .child(imagePath);

                                imageReference.delete()
                                        .addOnCompleteListener(task ->
                                                callback.onSuccess(
                                                        storyId
                                                )
                                        );
                            })
                            .addOnFailureListener(e ->
                                    callback.onError(
                                            "Story delete failed: "
                                                    + e.getMessage()
                                    )
                            );
                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Story check failed: "
                                        + e.getMessage()
                        )
                );
    }
}
