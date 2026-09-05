package com.sanskritisathi.app;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReelCommentFirebaseHelper {

    public interface CommentsCallback {
        void onSuccess(List<ReelComment> comments);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    private static final FirebaseAuth auth =
            FirebaseAuth.getInstance();

    private static final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private ReelCommentFirebaseHelper() {
    }

    // =========================
    // ADD COMMENT
    // =========================

    public static void addComment(
            String reelId,
            String text,
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

        if (text == null ||
                text.trim().isEmpty()) {

            callback.onError(
                    "Comment empty nahi ho sakta."
            );
            return;
        }

        String cleanText =
                text.trim();

        if (cleanText.length() > 500) {

            callback.onError(
                    "Comment maximum 500 characters ka ho sakta hai."
            );

            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        String username =
                auth.getCurrentUser().getDisplayName();

        if (username == null ||
                username.trim().isEmpty()) {

            username = "Sanskriti User";
        }

        String commentId =
                UUID.randomUUID().toString();

        DocumentReference reelRef =
                db.collection("reels")
                        .document(reelId);

        DocumentReference commentRef =
                reelRef.collection("comments")
                        .document(commentId);

        Map<String, Object> comment =
                new HashMap<>();

        comment.put(
                "commentId",
                commentId
        );

        comment.put(
                "reelId",
                reelId
        );

        comment.put(
                "userId",
                uid
        );

        comment.put(
                "username",
                username
        );

        comment.put(
                "text",
                cleanText
        );

        comment.put(
                "createdAt",
                System.currentTimeMillis()
        );

        // Comment + count in ONE transaction
        db.runTransaction(transaction -> {

            DocumentSnapshot reelSnapshot =
                    transaction.get(reelRef);

            if (!reelSnapshot.exists()) {

                throw new FirebaseFirestoreException(
                        "Reel not found.",
                        FirebaseFirestoreException.Code.NOT_FOUND
                );
            }

            Long currentCount =
                    reelSnapshot.getLong("comments");

            long count =
                    currentCount == null
                            ? 0
                            : currentCount;

            transaction.set(
                    commentRef,
                    comment
            );

            Map<String, Object> update =
                    new HashMap<>();

            update.put(
                    "comments",
                    count + 1
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
                        "Comment save failed: "
                                + e.getMessage()
                )
        );
    }

    // =========================
    // LOAD COMMENTS
    // =========================

    public static void getComments(
            String reelId,
            CommentsCallback callback) {

        if (auth.getCurrentUser() == null) {

            callback.onError(
                    "Please login first."
            );

            return;
        }

        if (reelId == null ||
                reelId.trim().isEmpty()) {

            callback.onError(
                    "Invalid Reel."
            );

            return;
        }

        db.collection("reels")
                .document(reelId)
                .collection("comments")
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<ReelComment> comments =
                            new ArrayList<>();

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        String id =
                                document.getString(
                                        "commentId"
                                );

                        if (id == null ||
                                id.trim().isEmpty()) {

                            id = document.getId();
                        }

                        String userId =
                                document.getString(
                                        "userId"
                                );

                        String username =
                                document.getString(
                                        "username"
                                );

                        String text =
                                document.getString(
                                        "text"
                                );

                        Long createdAt =
                                document.getLong(
                                        "createdAt"
                                );

                        comments.add(
                                new ReelComment(
                                        id,
                                        reelId,
                                        userId,
                                        username == null ||
                                                username.trim().isEmpty()
                                                ? "Sanskriti User"
                                                : username,
                                        text == null
                                                ? ""
                                                : text,
                                        createdAt == null
                                                ? 0
                                                : createdAt
                                )
                        );
                    }

                    callback.onSuccess(
                            comments
                    );

                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Comments load failed: "
                                        + e.getMessage()
                        )
                );
    }

    // =========================
    // DELETE COMMENT
    // =========================

    public static void deleteComment(
            String reelId,
            String commentId,
            ActionCallback callback) {

        if (auth.getCurrentUser() == null) {

            callback.onError(
                    "Please login first."
            );

            return;
        }

        if (reelId == null ||
                reelId.trim().isEmpty()) {

            callback.onError(
                    "Invalid Reel."
            );

            return;
        }

        if (commentId == null ||
                commentId.trim().isEmpty()) {

            callback.onError(
                    "Invalid comment."
            );

            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        DocumentReference reelRef =
                db.collection("reels")
                        .document(reelId);

        DocumentReference commentRef =
                reelRef.collection("comments")
                        .document(commentId);

        // Delete + count update in ONE transaction
        db.runTransaction(transaction -> {

            DocumentSnapshot reelSnapshot =
                    transaction.get(reelRef);

            if (!reelSnapshot.exists()) {

                throw new FirebaseFirestoreException(
                        "Reel not found.",
                        FirebaseFirestoreException.Code.NOT_FOUND
                );
            }

            DocumentSnapshot commentSnapshot =
                    transaction.get(commentRef);

            if (!commentSnapshot.exists()) {

                throw new FirebaseFirestoreException(
                        "Comment not found.",
                        FirebaseFirestoreException.Code.NOT_FOUND
                );
            }

            String ownerId =
                    commentSnapshot.getString(
                            "userId"
                    );

            if (ownerId == null ||
                    !uid.equals(ownerId)) {

                throw new FirebaseFirestoreException(
                        "Aap sirf apna comment delete kar sakte ho.",
                        FirebaseFirestoreException.Code.PERMISSION_DENIED
                );
            }

            Long currentCount =
                    reelSnapshot.getLong(
                            "comments"
                    );

            long count =
                    currentCount == null
                            ? 0
                            : currentCount;

            long newCount =
                    Math.max(
                            0,
                            count - 1
                    );

            transaction.delete(
                    commentRef
            );

            Map<String, Object> update =
                    new HashMap<>();

            update.put(
                    "comments",
                    newCount
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
                        "Comment delete failed: "
                                + e.getMessage()
                )
        );
    }
}
