package com.sanskritisathi.app;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CulturePostFirebaseHelper {

    private static final String POSTS = "posts";

    private static final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private static final FirebaseAuth auth =
            FirebaseAuth.getInstance();

    public interface PostsCallback {
        void onSuccess(List<CulturePost> posts);
        void onError(String message);
    }

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface LikeCallback {
        void onSuccess(boolean liked, int likeCount);
        void onError(String message);
    }

    public interface LikeStatusCallback {
        void onResult(boolean liked);
    }

    // =====================================================
    // LOAD PUBLIC POSTS
    // =====================================================

    public static void getPublicPosts(
            @NonNull PostsCallback callback) {

        db.collection(POSTS)
                .whereEqualTo("visibility", "Public")
                .orderBy(
                        "createdAt",
                        Query.Direction.DESCENDING
                )
                .limit(100)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<CulturePost> posts =
                            new ArrayList<>();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        CulturePost post =
                                documentToPost(document);

                        if (post != null) {
                            posts.add(post);
                        }
                    }

                    callback.onSuccess(posts);
                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Posts load nahi hue: "
                                        + e.getMessage()
                        )
                );
    }

    // =====================================================
    // LOAD MY POSTS
    // =====================================================

    public static void getMyPosts(
            @NonNull PostsCallback callback) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {
            callback.onError(
                    "Pehle Login karein."
            );
            return;
        }

        db.collection(POSTS)
                .whereEqualTo(
                        "authorUid",
                        user.getUid()
                )
                .orderBy(
                        "createdAt",
                        Query.Direction.DESCENDING
                )
                .limit(100)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<CulturePost> posts =
                            new ArrayList<>();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        CulturePost post =
                                documentToPost(document);

                        if (post != null) {
                            posts.add(post);
                        }
                    }

                    callback.onSuccess(posts);
                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Posts load nahi hue: "
                                        + e.getMessage()
                        )
                );
    }

    // =====================================================
    // CREATE POST
    // =====================================================

    public static void createPost(
            String category,
            String caption,
            String imageUrl,
            String visibility,
            @NonNull ActionCallback callback) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {
            callback.onError(
                    "Pehle Login karein."
            );
            return;
        }

        if (caption == null ||
                caption.trim().isEmpty()) {

            callback.onError(
                    "Caption khali hai."
            );
            return;
        }

        if (caption.trim().length() > 5000) {

            callback.onError(
                    "Caption maximum 5000 characters ka ho sakta hai."
            );
            return;
        }

        if (!"Followers".equals(visibility)
                && !"Public".equals(visibility)) {

            visibility = "Public";
        }

        String postId =
                db.collection(POSTS)
                        .document()
                        .getId();

        String author =
                user.getDisplayName();

        if (author == null ||
                author.trim().isEmpty()) {

            author = "Sanskriti Sathi";
        }

        Map<String, Object> data =
                new HashMap<>();

        data.put("postId", postId);
        data.put("authorUid", user.getUid());
        data.put("author", author);

        data.put(
                "category",
                category == null ||
                        category.trim().isEmpty()
                        ? "Indian Culture"
                        : category.trim()
        );

        data.put(
                "caption",
                caption.trim()
        );

        data.put(
                "imageUrl",
                imageUrl == null
                        ? ""
                        : imageUrl.trim()
        );

        data.put(
                "visibility",
                visibility
        );

        data.put("likes", 0);
        data.put("comments", 0);

        data.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        db.collection(POSTS)
                .document(postId)
                .set(data)
                .addOnSuccessListener(
                        unused -> callback.onSuccess()
                )
                .addOnFailureListener(
                        e -> callback.onError(
                                "Post create nahi hui: "
                                        + e.getMessage()
                        )
                );
    }

    // =====================================================
    // LIKE / UNLIKE
    // =====================================================

    public static void toggleLike(
            String postId,
            @NonNull LikeCallback callback) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {
            callback.onError(
                    "Pehle Login karein."
            );
            return;
        }

        if (postId == null ||
                postId.trim().isEmpty()) {

            callback.onError(
                    "Post ID available nahi hai."
            );
            return;
        }

        String uid =
                user.getUid();

        DocumentReference postRef =
                db.collection(POSTS)
                        .document(postId);

        DocumentReference likeRef =
                postRef.collection("likes")
                        .document(uid);

        db.runTransaction(transaction -> {

            DocumentSnapshot postSnapshot =
                    transaction.get(postRef);

            if (!postSnapshot.exists()) {
                throw new IllegalStateException(
                        "Post available nahi hai."
                );
            }

            DocumentSnapshot likeSnapshot =
                    transaction.get(likeRef);

            Long likesValue =
                    postSnapshot.getLong("likes");

            int likes =
                    likesValue == null
                            ? 0
                            : likesValue.intValue();

            if (likeSnapshot.exists()) {

                transaction.delete(likeRef);

                likes =
                        Math.max(
                                0,
                                likes - 1
                        );

                transaction.update(
                        postRef,
                        "likes",
                        likes
                );

                return new LikeResult(
                        false,
                        likes
                );

            } else {

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

                likes++;

                transaction.update(
                        postRef,
                        "likes",
                        likes
                );

                return new LikeResult(
                        true,
                        likes
                );
            }

        }).addOnSuccessListener(
                result ->
                        callback.onSuccess(
                                result.liked,
                                result.likeCount
                        )
        ).addOnFailureListener(
                e ->
                        callback.onError(
                                "Like update nahi hua: "
                                        + e.getMessage()
                        )
        );
    }

    // =====================================================
    // CHECK LIKE
    // =====================================================

    public static void checkLiked(
            String postId,
            @NonNull LikeStatusCallback callback) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null ||
                postId == null ||
                postId.trim().isEmpty()) {

            callback.onResult(false);
            return;
        }

        db.collection(POSTS)
                .document(postId)
                .collection("likes")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(
                        document ->
                                callback.onResult(
                                        document.exists()
                                )
                )
                .addOnFailureListener(
                        e ->
                                callback.onResult(false)
                );
    }

    // =====================================================
    // DELETE POST
    // =====================================================

    public static void deletePost(
            String postId,
            @NonNull ActionCallback callback) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {
            callback.onError(
                    "Pehle Login karein."
            );
            return;
        }

        if (postId == null ||
                postId.trim().isEmpty()) {

            callback.onError(
                    "Post ID available nahi hai."
            );
            return;
        }

        DocumentReference postRef =
                db.collection(POSTS)
                        .document(postId);

        postRef.get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        callback.onError(
                                "Post available nahi hai."
                        );
                        return;
                    }

                    String ownerUid =
                            document.getString(
                                    "authorUid"
                            );

                    if (!user.getUid().equals(
                            ownerUid
                    )) {

                        callback.onError(
                                "Aap sirf apni post delete kar sakte hain."
                        );
                        return;
                    }

                    postRef.delete()
                            .addOnSuccessListener(
                                    unused ->
                                            callback.onSuccess()
                            )
                            .addOnFailureListener(
                                    e ->
                                            callback.onError(
                                                    "Post delete nahi hui: "
                                                            + e.getMessage()
                                            )
                            );
                })
                .addOnFailureListener(
                        e ->
                                callback.onError(
                                        "Post verify nahi hui: "
                                                + e.getMessage()
                                )
                );
    }

    // =====================================================
    // FIRESTORE DOCUMENT → CULTURE POST
    // =====================================================

    private static CulturePost documentToPost(
            DocumentSnapshot document) {

        if (document == null ||
                !document.exists()) {

            return null;
        }

        String id =
                document.getId();

        String authorUid =
                safe(
                        document.getString(
                                "authorUid"
                        )
                );

        String author =
                safe(
                        document.getString(
                                "author"
                        )
                );

        String category =
                safe(
                        document.getString(
                                "category"
                        )
                );

        String caption =
                safe(
                        document.getString(
                                "caption"
                        )
                );

        String imageUrl =
                safe(
                        document.getString(
                                "imageUrl"
                        )
                );

        String visibility =
                safe(
                        document.getString(
                                "visibility"
                        )
                );

        Long likes =
                document.getLong("likes");

        Long comments =
                document.getLong("comments");

        long createdAt = 0L;

        Object time =
                document.get("createdAt");

        if (time instanceof
                com.google.firebase.Timestamp) {

            createdAt =
                    ((com.google.firebase.Timestamp)
                            time)
                            .toDate()
                            .getTime();
        }

        return new CulturePost(
                id,
                authorUid,
                author,
                category,
                caption,
                imageUrl,
                visibility,
                createdAt,
                likes == null
                        ? 0
                        : likes.intValue(),
                comments == null
                        ? 0
                        : comments.intValue(),
                R.drawable.icon_foreground,
                R.drawable.icon_foreground
        );
    }

    // =====================================================
    // SAFE STRING
    // =====================================================

    private static String safe(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    // =====================================================
    // LIKE RESULT
    // =====================================================

    private static class LikeResult {

        final boolean liked;
        final int likeCount;

        LikeResult(
                boolean liked,
                int likeCount) {

            this.liked = liked;
            this.likeCount = likeCount;
        }
    }
}
