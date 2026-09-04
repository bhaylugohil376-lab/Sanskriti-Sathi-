package com.sanskritisathi.app;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (reelId == null || reelId.trim().isEmpty()) {
            callback.onError("Invalid Reel.");
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            callback.onError("Comment empty nahi ho sakta.");
            return;
        }

        String cleanText = text.trim();

        if (cleanText.length() > 500) {
            callback.onError(
                    "Comment maximum 500 characters ka ho sakta hai."
            );
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        String username =
                auth.getCurrentUser().getDisplayName();

        if (username == null ||
                username.trim().isEmpty()) {

            username = "Sanskriti User";
        }

        String commentId =
                db.collection("reels")
                        .document(reelId)
                        .collection("comments")
                        .document()
                        .getId();

        Map<String, Object> comment =
                new HashMap<>();

        comment.put("commentId", commentId);
        comment.put("reelId", reelId);
        comment.put("userId", uid);
        comment.put("username", username);
        comment.put("text", cleanText);
        comment.put(
                "createdAt",
                System.currentTimeMillis()
        );

        db.collection("reels")
                .document(reelId)
                .collection("comments")
                .document(commentId)
                .set(comment)
                .addOnSuccessListener(unused ->
                        updateCommentCount(
                                reelId,
                                1,
                                callback
                        )
                )
                .addOnFailureListener(e ->
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
            callback.onError("Please login first.");
            return;
        }

        if (reelId == null || reelId.trim().isEmpty()) {
            callback.onError("Invalid Reel.");
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
                                document.getString("commentId");

                        if (id == null) {
                            id = document.getId();
                        }

                        String userId =
                                document.getString("userId");

                        String username =
                                document.getString("username");

                        String text =
                                document.getString("text");

                        Long createdAt =
                                document.getLong("createdAt");

                        comments.add(
                                new ReelComment(
                                        id,
                                        reelId,
                                        userId,
                                        username == null
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

                    callback.onSuccess(comments);

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
            callback.onError("Please login first.");
            return;
        }

        if (reelId == null ||
                reelId.trim().isEmpty()) {

            callback.onError("Invalid Reel.");
            return;
        }

        if (commentId == null ||
                commentId.trim().isEmpty()) {

            callback.onError("Invalid comment.");
            return;
        }

        String uid =
                auth.getCurrentUser().getUid();

        db.collection("reels")
                .document(reelId)
                .collection("comments")
                .document(commentId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        callback.onError(
                                "Comment not found."
                        );
                        return;
                    }

                    String ownerId =
                            document.getString("userId");

                    if (ownerId == null ||
                            !uid.equals(ownerId)) {

                        callback.onError(
                                "Aap sirf apna comment delete kar sakte ho."
                        );
                        return;
                    }

                    document.getReference()
                            .delete()
                            .addOnSuccessListener(unused ->
                                    updateCommentCount(
                                            reelId,
                                            -1,
                                            callback
                                    )
                            )
                            .addOnFailureListener(e ->
                                    callback.onError(
                                            "Comment delete failed: "
                                                    + e.getMessage()
                                    )
                            );
                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Comment check failed: "
                                        + e.getMessage()
                        )
                );
    }

    // =========================
    // UPDATE COMMENT COUNT
    // =========================

    private static void updateCommentCount(
            String reelId,
            int change,
            ActionCallback callback) {

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

                    Long currentCount =
                            document.getLong("comments");

                    long count =
                            currentCount == null
                                    ? 0
                                    : currentCount;

                    count += change;

                    if (count < 0) {
                        count = 0;
                    }

                    Map<String, Object> update =
                            new HashMap<>();

                    update.put("comments", count);

                    document.getReference()
                            .update(update)
                            .addOnSuccessListener(unused ->
                                    callback.onSuccess()
                            )
                            .addOnFailureListener(e ->
                                    callback.onError(
                                            "Comment count update failed: "
                                                    + e.getMessage()
                                    )
                            );

                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Reel count check failed: "
                                        + e.getMessage()
                        )
                );
    }
}
