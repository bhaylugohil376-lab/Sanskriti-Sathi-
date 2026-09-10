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

    public interface LikeStatusCallback {
        void onResult(boolean liked);
    }

    // =====================================================
    // GET PUBLIC POSTS
    // =====================================================

    public static void getPublicPosts(
            @NonNull PostsCallback callback) {

        db.collection(POSTS)
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

                        if (post == null) {
                            continue;
                        }

                        String visibility =
                                post.getVisibility();

                        if (visibility == null ||
                                visibility.trim().isEmpty() ||
                                "Public".equalsIgnoreCase(
                                        visibility
                                )) {

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
    // GET MY POSTS
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

        /*
         * Composite index avoid karne ke liye
         * sirf authorUid query kar rahe hain.
         * Sorting client side hogi.
         */
        db.collection(POSTS)
                .whereEqualTo(
                        "authorUid",
                        user.getUid()
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

                    posts.sort(
                            (a, b) ->
                                    Long.compare(
                                            b.getCreatedAt(),
                                            a.getCreatedAt()
                                    )
                    );

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

        String cleanCaption =
                caption.trim();

        if (cleanCaption.length() > 5000) {

            callback.onError(
                    "Caption maximum 5000 characters ka ho sakta hai."
            );

            return;
        }

        String cleanVisibility =
                "Followers".equalsIgnoreCase(
                        visibility
                )
                        ? "Followers"
                        : "Public";

        String postId =
                db.collection(POSTS)
                        .document()
                        .getId();

        String author =
                user.getDisplayName();

        if (author == null ||
                author.trim().isEmpty()) {

            author = "Sanskriti Sathi User";
        }

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "postId",
                postId
        );

        data.put(
                "authorUid",
                user.getUid()
        );

        data.put(
                "author",
                author
        );

        data.put(
                "category",
                category == null ||
                        category.trim().isEmpty()
                        ? "Indian Culture"
                        : category.trim()
        );

        data.put(
                "caption",
                cleanCaption
        );

        data.put(
                "imageUrl",
                imageUrl == null
                        ? ""
                        : imageUrl.trim()
        );

        data.put(
                "visibility",
                cleanVisibility
        );

        data.put(
                "likes",
                0
        );

        data.put(
                "likeCount",
                0
        );

        data.put(
                "comments",
                0
        );

        data.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        db.collection(POSTS)
                .document(postId)
                .set(data)
                .addOnSuccessListener(
                        unused ->
                                callback.onSuccess()
                )
                .addOnFailureListener(
                        e ->
                                callback.onError(
                                        "Post create nahi hui: "
                                                + e.getMessage()
                                )
                );
    }

    // =====================================================
    // LIKE / UNLIKE
    //
    // Main adapter ke liye:
    // toggleLike(postId, liked, count, success, error)
    // =====================================================

    public static void toggleLike(
            String postId,
            boolean requestedLiked,
            int currentLikeCount,
            @NonNull final SimpleLikeSuccess success,
            @NonNull final SimpleLikeError error) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {

            error.onError(
                    "Pehle Login karein."
            );

            return;
        }

        if (postId == null ||
                postId.trim().isEmpty()) {

            error.onError(
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

            boolean alreadyLiked =
                    likeSnapshot.exists();

            Long likesValue =
                    postSnapshot.getLong("likes");

            if (likesValue == null) {

                likesValue =
                        postSnapshot.getLong(
                                "likeCount"
                        );
            }

            int likes =
                    likesValue == null
                            ? 0
                            : likesValue.intValue();

            /*
             * Actual Firebase state ke according
             * toggle karo. Isse double-like issue
             * kam hota hai.
             */
            if (alreadyLiked) {

                transaction.delete(
                        likeRef
                );

                likes =
                        Math.max(
                                0,
                                likes - 1
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
            }

            Map<String, Object> update =
                    new HashMap<>();

            update.put(
                    "likes",
                    likes
            );

            update.put(
                    "likeCount",
                    likes
            );

            transaction.update(
                    postRef,
                    update
            );

            return new LikeResult(
                    !alreadyLiked,
                    likes
            );

        }).addOnSuccessListener(result -> {

            success.onSuccess(
                    result.liked,
                    result.likeCount
            );

            /*
             * Like hone par post owner ko
             * in-app notification.
             */
            if (result.liked) {

                postRef.get()
                        .addOnSuccessListener(
                                document -> {

                                    if (!document.exists()) {
                                        return;
                                    }

                                    String ownerUid =
                                            document.getString(
                                                    "authorUid"
                                            );

                                    if (ownerUid == null ||
                                            ownerUid.isEmpty() ||
                                            ownerUid.equals(uid)) {
                                        return;
                                    }

                                    String actorName =
                                            user.getDisplayName();

                                    if (actorName == null ||
                                            actorName.trim().isEmpty()) {

                                        actorName =
                                                "Sanskriti User";
                                    }

                                    try {

                                        NotificationFirebaseHelper
                                                .create(
                                                        ownerUid,
                                                        "like",
                                                        actorName
                                                                + " liked your post",
                                                        postId
                                                );

                                    } catch (Exception ignored) {
                                    }
                                }
                        );
            }

        }).addOnFailureListener(
                e ->
                        error.onError(
                                "Like update nahi hua: "
                                        + e.getMessage()
                        )
        );
    }

    // =====================================================
    // COMPATIBILITY LIKE METHOD
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

        DocumentReference postRef =
                db.collection(POSTS)
                        .document(postId);

        DocumentReference likeRef =
                postRef.collection("likes")
                        .document(
                                user.getUid()
                        );

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

            boolean liked;

            if (likeSnapshot.exists()) {

                transaction.delete(
                        likeRef
                );

                likes =
                        Math.max(
                                0,
                                likes - 1
                        );

                liked = false;

            } else {

                Map<String, Object> likeData =
                        new HashMap<>();

                likeData.put(
                        "userId",
                        user.getUid()
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
                liked = true;
            }

            Map<String, Object> update =
                    new HashMap<>();

            update.put(
                    "likes",
                    likes
            );

            update.put(
                    "likeCount",
                    likes
            );

            transaction.update(
                    postRef,
                    update
            );

            return new LikeResult(
                    liked,
                    likes
            );

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
                .document(
                        user.getUid()
                )
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
                .addOnSuccessListener(
                        document -> {

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
                        }
                )
                .addOnFailureListener(
                        e ->
                                callback.onError(
                                        "Post verify nahi hui: "
                                                + e.getMessage()
                                )
                );
    }

    // =====================================================
    // DOCUMENT → CULTURE POST
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
                document.getLong(
                        "likes"
                );

        if (likes == null) {

            likes =
                    document.getLong(
                            "likeCount"
                    );
        }

        Long comments =
                document.getLong(
                        "comments"
                );

        long createdAt = 0L;

        Object timestamp =
                document.get(
                        "createdAt"
                );

        if (timestamp instanceof
                com.google.firebase.Timestamp) {

            createdAt =
                    ((com.google.firebase.Timestamp)
                            timestamp)
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
                        : Math.max(
                                0,
                                likes.intValue()
                        ),
                comments == null
                        ? 0
                        : Math.max(
                                0,
                                comments.intValue()
                        ),
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

    // =====================================================
    // SIMPLE CALLBACKS
    // Adapter ke 4-argument toggleLike ke liye
    // =====================================================

    public interface SimpleLikeSuccess {
        void onSuccess(
                boolean liked,
                int likeCount
        );
    }

    public interface SimpleLikeError {
        void onError(
                String message
        );
    }
}
