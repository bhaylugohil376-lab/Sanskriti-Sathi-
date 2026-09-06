package com.sanskritisathi.app;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CulturePostFirebaseHelper {

    private static final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private static final FirebaseAuth auth =
            FirebaseAuth.getInstance();

    private static final String POSTS = "posts";

    // =====================================================
    // CALLBACKS
    // =====================================================

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

    // =====================================================
    // GET CURRENT USER
    // =====================================================

    private static FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
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

        FirebaseUser user = getCurrentUser();

        if (user == null) {
            callback.onError("Pehle Login karein.");
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
                                "Mere posts load nahi hue: "
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

        FirebaseUser user = getCurrentUser();

        if (user == null) {
            callback.onError("Pehle Login karein.");
            return;
        }

        if (caption == null ||
                caption.trim().isEmpty()) {

            callback.onError("Post caption khali hai.");
            return;
        }

        if (caption.trim().length() > 5000) {
            callback.onError(
                    "Caption maximum 5000 characters ka ho sakta hai."
            );
            return;
        }

        if (visibility == null ||
                (!visibility.equals("Public")
                        && !visibility.equals("Followers"))) {

            visibility = "Public";
        }

        String postId =
                db.collection(POSTS)
                        .document()
                        .getId();

        Map<String, Object> post =
                new HashMap<>();

        post.put("postId", postId);
        post.put("authorUid", user.getUid());

        String authorName =
                user.getDisplayName();

        if (authorName == null ||
                authorName.trim().isEmpty()) {

            authorName = "Sanskriti Sathi";
        }

        post.put("author", authorName);
        post.put(
                "category",
                category == null
                        ? "Indian Culture"
                        : category.trim()
        );
        post.put(
                "caption",
                caption.trim()
        );
        post.put(
                "imageUrl",
                imageUrl == null
                        ? ""
                        : imageUrl.trim()
        );
        post.put("visibility", visibility);
        post.put("likes", 0);
        post.put("comments", 0);
        post.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        db.collection(POSTS)
                .document(postId)
                .set(post)
                .addOnSuccessListener(unused ->
                        callback.onSuccess()
                )
                .addOnFailureListener(e ->
                        callback.onError(
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

        FirebaseUser user = getCurrentUser();

        if (user == null) {
            callback.onError("Pehle Login karein.");
            return;
        }

        if (postId == null ||
                postId.trim().isEmpty()) {

            callback.onError("Post ID available nahi hai.");
            return;
        }

        String uid = user.getUid();

        db.runTransaction(
                (Transaction.Function<LikeResult>) transaction -> {

                    com.google.firebase.firestore.DocumentReference postRef =
                            db.collection(POSTS)
                                    .document(postId);

                    com.google.firebase.firestore.DocumentReference likeRef =
                            postRef.collection("likes")
                                    .document(uid);

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

                        likes = Math.max(
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
                }
        )
                .addOnSuccessListener(result ->
                        callback.onSuccess(
                                result.liked,
                                result.likeCount
                        )
                )
                .addOnFailureListener(e ->
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

        FirebaseUser user = getCurrentUser();

        if (user == null) {
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
                        e -> callback.onResult(false)
                );
    }

    public interface LikeStatusCallback {
        void onResult(boolean liked);
    }

    // =====================================================
    // DELETE POST
    // =====================================================

    public static void deletePost(
            String postId,
            @NonNull ActionCallback callback) {

        FirebaseUser user = getCurrentUser();

        if (user == null) {
            callback.onError("Pehle Login karein.");
            return;
        }

        if (postId == null ||
                postId.trim().isEmpty()) {

            callback.onError("Post ID available nahi hai.");
            return;
        }

        com.google.firebase.firestore.DocumentReference postRef =
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

                    if (ownerUid == null ||
                            !ownerUid.equals(
                                    user.getUid()
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
                .addOnFailureListener(e ->
                        callback.onError(
                                "Post verify nahi hui: "
                                        + e.getMessage()
                        )
                );
    }

    // =====================================================
    // CONVERT FIREBASE DOCUMENT → CULTURE POST
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
                safeString(
                        document.getString(
                                "authorUid"
                        )
                );

        String author =
                safeString(
                        document.getString(
                                "author"
                        )
                );

        String category =
                safeString(
                        document.getString(
                                "category"
                        )
                );

        String caption =
                safeString(
                        document.getString(
                                "caption"
                        )
                );

        String imageUrl =
                safeString(
                        document.getString(
                                "imageUrl"
                        )
                );

        String visibility =
                safeString(
                        document.getString(
                                "visibility"
                        )
                );

        Long likes =
                document.getLong("likes");

        Long comments =
                document.getLong("comments");

        long createdAt = 0L;

        Object timestamp =
                document.get("createdAt");

        if (timestamp instanceof
                com.google.firebase.Timestamp) {

            createdAt =
                    ((com.google.firebase.Timestamp)
                            timestamp)
                            .toDate()
                            .getTime();
        }

        CulturePost post =
                new CulturePost(
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

        return post;
    }

    // =====================================================
    // SAFE STRING
    // =====================================================

    private static String safeString(
            String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    // =====================================================
    // LIKE RESULT
    // =====================================================

    private static class LikeResult {

        boolean liked;
        int likeCount;

        LikeResult(
                boolean liked,
                int likeCount) {

            this.liked = liked;
            this.likeCount = likeCount;
        }
    }
}
