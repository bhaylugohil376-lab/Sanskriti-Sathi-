package com.sanskritisathi.app;

import android.net.Uri;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoryFirebaseHelper {

    public interface UploadCallback {
        void onSuccess(String storyId);
        void onError(String message);
    }

    public interface StoriesCallback {
        void onSuccess(List<Story> stories);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
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

    // =========================================================
    // UPLOAD STORY
    // =========================================================

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

        String storyId = firestore
                .collection("stories")
                .document()
                .getId();

        StorageReference imageReference = storage
                .getReference()
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
        story.put(
                "caption",
                caption != null ? caption : ""
        );
        story.put(
                "visibility",
                visibility != null
                        ? visibility
                        : "Public"
        );
        story.put("createdAt", FieldValue.serverTimestamp());
        story.put("views", 0L);
        story.put("likes", 0L);

        firestore
                .collection("stories")
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

    // =========================================================
    // LOAD ACTIVE STORIES
    // =========================================================

    public void getActiveStories(StoriesCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onError("Please login first");
            return;
        }

        long twentyFourHoursAgo =
                System.currentTimeMillis()
                        - (24L * 60L * 60L * 1000L);

        Timestamp cutoff = new Timestamp(
                new Date(twentyFourHoursAgo)
        );

        firestore
                .collection("stories")
                .whereGreaterThan("createdAt", cutoff)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<Story> result =
                            new ArrayList<>();

                    for (DocumentSnapshot doc :
                            snapshot.getDocuments()) {

                        String id =
                                doc.getString("storyId");

                        String uid =
                                doc.getString("ownerUid");

                        String imageUrl =
                                doc.getString("imageUrl");

                        String caption =
                                doc.getString("caption");

                        String visibility =
                                doc.getString("visibility");

                        Timestamp timestamp =
                                doc.getTimestamp("createdAt");

                        long createdAt =
                                timestamp != null
                                        ? timestamp
                                        .toDate()
                                        .getTime()
                                        : System.currentTimeMillis();

                        Long viewsValue =
                                doc.getLong("views");

                        int views =
                                viewsValue != null
                                        ? viewsValue.intValue()
                                        : 0;

                        if (id == null ||
                                imageUrl == null) {
                            continue;
                        }

                        boolean ownStory =
                                uid != null &&
                                uid.equals(
                                        auth.getCurrentUser()
                                                .getUid()
                                );

                        Story story = new Story(
                                id,
                                uid != null
                                        ? uid
                                        : "Sanskriti User",
                                "",
                                imageUrl,
                                caption != null
                                        ? caption
                                        : "",
                                visibility != null
                                        ? visibility
                                        : "Public",
                                createdAt,
                                views,
                                ownStory
                        );

                        result.add(story);
                    }

                    callback.onSuccess(result);

                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Stories load nahi hui: "
                                        + e.getMessage()
                        )
                );
    }

    // =========================================================
    // UNIQUE STORY VIEW
    // =========================================================

    public void addStoryView(
            String storyId,
            ActionCallback callback) {

        if (auth.getCurrentUser() == null) {
            if (callback != null) {
                callback.onError("Please login first");
            }
            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        DocumentReference storyRef =
                firestore
                        .collection("stories")
                        .document(storyId);

        DocumentReference viewRef =
                storyRef
                        .collection("views")
                        .document(uid);

        firestore.runTransaction(
                transaction -> {

                    DocumentSnapshot storySnapshot =
                            transaction.get(storyRef);

                    DocumentSnapshot viewSnapshot =
                            transaction.get(viewRef);

                    if (!storySnapshot.exists()) {
                        throw new IllegalStateException(
                                "Story nahi mili"
                        );
                    }

                    // Already viewed
                    if (viewSnapshot.exists()) {
                        return null;
                    }

                    Map<String, Object> viewData =
                            new HashMap<>();

                    viewData.put(
                            "userId",
                            uid
                    );

                    viewData.put(
                            "createdAt",
                            FieldValue.serverTimestamp()
                    );

                    transaction.set(
                            viewRef,
                            viewData
                    );

                    Long currentViews =
                            storySnapshot.getLong("views");

                    long newViews =
                            currentViews != null
                                    ? currentViews + 1
                                    : 1;

                    transaction.update(
                            storyRef,
                            "views",
                            newViews
                    );

                    return null;
                }
        )
        .addOnSuccessListener(unused -> {

            if (callback != null) {
                callback.onSuccess();
            }

        })
        .addOnFailureListener(e -> {

            if (callback != null) {
                callback.onError(
                        "View update failed: "
                                + e.getMessage()
                );
            }

        });
    }

    // =========================================================
    // UNIQUE LIKE / UNLIKE
    // =========================================================

    public void toggleStoryLike(
            String storyId,
            boolean like,
            ActionCallback callback) {

        if (auth.getCurrentUser() == null) {
            if (callback != null) {
                callback.onError("Please login first");
            }
            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        DocumentReference storyRef =
                firestore
                        .collection("stories")
                        .document(storyId);

        DocumentReference likeRef =
                storyRef
                        .collection("likes")
                        .document(uid);

        firestore.runTransaction(
                transaction -> {

                    DocumentSnapshot storySnapshot =
                            transaction.get(storyRef);

                    DocumentSnapshot likeSnapshot =
                            transaction.get(likeRef);

                    if (!storySnapshot.exists()) {
                        throw new IllegalStateException(
                                "Story nahi mili"
                        );
                    }

                    Long currentLikes =
                            storySnapshot.getLong("likes");

                    long likes =
                            currentLikes != null
                                    ? currentLikes
                                    : 0;

                    if (like) {

                        // Already liked
                        if (!likeSnapshot.exists()) {

                            Map<String, Object> likeData =
                                    new HashMap<>();

                            likeData.put(
                                    "userId",
                                    uid
                            );

                            likeData.put(
                                    "createdAt",
                                    FieldValue.serverTimestamp()
                            );

                            transaction.set(
                                    likeRef,
                                    likeData
                            );

                            transaction.update(
                                    storyRef,
                                    "likes",
                                    likes + 1
                            );
                        }

                    } else {

                        // Unlike only if like exists
                        if (likeSnapshot.exists()) {

                            transaction.delete(
                                    likeRef
                            );

                            transaction.update(
                                    storyRef,
                                    "likes",
                                    Math.max(0, likes - 1)
                            );
                        }
                    }

                    return null;
                }
        )
        .addOnSuccessListener(unused -> {

            if (callback != null) {
                callback.onSuccess();
            }

        })
        .addOnFailureListener(e -> {

            if (callback != null) {
                callback.onError(
                        "Like update failed: "
                                + e.getMessage()
                );
            }

        });
    }

    // =========================================================
    // STORY REPLY
    // =========================================================

    public void addStoryReply(
            String storyId,
            String replyText,
            ActionCallback callback) {

        if (auth.getCurrentUser() == null) {
            if (callback != null) {
                callback.onError("Please login first");
            }
            return;
        }

        if (replyText == null ||
                replyText.trim().isEmpty()) {

            if (callback != null) {
                callback.onError(
                        "Reply empty nahi ho sakta"
                );
            }
            return;
        }

        String text = replyText.trim();

        if (text.length() > 500) {
            if (callback != null) {
                callback.onError(
                        "Reply 500 characters se kam hona chahiye"
                );
            }
            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        Map<String, Object> reply =
                new HashMap<>();

        reply.put(
                "userId",
                uid
        );

        reply.put(
                "text",
                text
        );

        reply.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        firestore
                .collection("stories")
                .document(storyId)
                .collection("replies")
                .add(reply)
                .addOnSuccessListener(documentReference -> {

                    if (callback != null) {
                        callback.onSuccess();
                    }

                })
                .addOnFailureListener(e -> {

                    if (callback != null) {
                        callback.onError(
                                "Reply send nahi hui: "
                                        + e.getMessage()
                        );
                    }

                });
    }

    // =========================================================
    // DELETE STORY
    // =========================================================

    public void deleteStory(
            String storyId,
            String imagePath,
            UploadCallback callback) {

        if (auth.getCurrentUser() == null) {
            callback.onError("Please login first");
            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        firestore
                .collection("stories")
                .document(storyId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        callback.onError(
                                "Story nahi mili"
                        );
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

                    firestore
                            .collection("stories")
                            .document(storyId)
                            .delete()
                            .addOnSuccessListener(unused -> {

                                if (imagePath == null ||
                                        imagePath.trim().isEmpty()) {

                                    callback.onSuccess(
                                            storyId
                                    );
                                    return;
                                }

                                StorageReference imageReference =
                                        storage
                                                .getReference()
                                                .child(imagePath);

                                imageReference
                                        .delete()
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
