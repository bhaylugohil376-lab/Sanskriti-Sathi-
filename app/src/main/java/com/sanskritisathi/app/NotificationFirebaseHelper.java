package com.sanskritisathi.app;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class NotificationFirebaseHelper {

    private static final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private static final FirebaseAuth auth =
            FirebaseAuth.getInstance();

    private NotificationFirebaseHelper() {
    }

    public interface NotificationCallback {
        void onSuccess(List<NotificationModel> notifications);

        void onError(String message);
    }

    // Create notification
    public static void createNotification(
            String targetUid,
            String title,
            String message,
            String type,
            String referenceId
    ) {

        if (targetUid == null || targetUid.trim().isEmpty()) {
            return;
        }

        if (auth.getCurrentUser() != null
                && targetUid.equals(auth.getCurrentUser().getUid())) {
            return;
        }

        Map<String, Object> data = new HashMap<>();

        data.put(
                "title",
                title == null || title.trim().isEmpty()
                        ? "New Notification"
                        : title
        );

        data.put(
                "message",
                message == null
                        ? ""
                        : message
        );

        data.put(
                "type",
                type == null
                        ? "activity"
                        : type
        );

        data.put(
                "referenceId",
                referenceId == null
                        ? ""
                        : referenceId
        );

        data.put(
                "actorUid",
                auth.getCurrentUser() == null
                        ? ""
                        : auth.getCurrentUser().getUid()
        );

        data.put(
                "createdAt",
                FieldValue.serverTimestamp()
        );

        data.put("read", false);

        db.collection("users")
                .document(targetUid)
                .collection("notifications")
                .add(data);
    }

    // Compatibility method for existing code
    public static void create(
            String targetUid,
            String type,
            String text,
            String referenceId
    ) {

        createNotification(
                targetUid,
                getTitleFromType(type),
                text,
                type,
                referenceId
        );
    }

    // Load current user's notifications
    public static void getNotifications(
            String uid,
            @NonNull NotificationCallback callback
    ) {

        if (uid == null || uid.trim().isEmpty()) {
            callback.onError("User ID missing.");
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("notifications")
                .orderBy(
                        "createdAt",
                        Query.Direction.DESCENDING
                )
                .limit(100)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<NotificationModel> result =
                            new ArrayList<>();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        String title =
                                document.getString("title");

                        String message =
                                document.getString("message");

                        boolean read =
                                Boolean.TRUE.equals(
                                        document.getBoolean("read")
                                );

                        Timestamp timestamp =
                                document.getTimestamp("createdAt");

                        String time =
                                formatTime(timestamp);

                        NotificationModel notification =
                                new NotificationModel(
                                        document.getId(),
                                        title == null
                                                ? "Notification"
                                                : title,
                                        message == null
                                                ? ""
                                                : message,
                                        time,
                                        read
                                );

                        result.add(notification);
                    }

                    callback.onSuccess(result);

                })
                .addOnFailureListener(error ->
                        callback.onError(
                                "Notifications load nahi hui: "
                                        + error.getMessage()
                        )
                );
    }

    // Mark notification as read
    public static void markAsRead(
            String uid,
            String notificationId
    ) {

        if (uid == null
                || uid.trim().isEmpty()
                || notificationId == null
                || notificationId.trim().isEmpty()) {
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("notifications")
                .document(notificationId)
                .update("read", true);
    }

    // Mark all notifications as read
    public static void markAllAsRead(
            String uid
    ) {

        if (uid == null || uid.trim().isEmpty()) {
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("notifications")
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        return;
                    }

                    com.google.firebase.firestore.WriteBatch batch =
                            db.batch();

                    for (DocumentSnapshot document :
                            snapshot.getDocuments()) {

                        batch.update(
                                document.getReference(),
                                "read",
                                true
                        );
                    }

                    batch.commit();
                });
    }

    private static String formatTime(
            Timestamp timestamp
    ) {

        if (timestamp == null) {
            return "Just now";
        }

        long timeMillis =
                timestamp.toDate().getTime();

        long difference =
                System.currentTimeMillis() - timeMillis;

        long seconds =
                difference / 1000;

        if (seconds < 60) {
            return "Just now";
        }

        long minutes =
                seconds / 60;

        if (minutes < 60) {
            return minutes
                    + (minutes == 1
                    ? " minute ago"
                    : " minutes ago");
        }

        long hours =
                minutes / 60;

        if (hours < 24) {
            return hours
                    + (hours == 1
                    ? " hour ago"
                    : " hours ago");
        }

        long days =
                hours / 24;

        if (days < 7) {
            return days
                    + (days == 1
                    ? " day ago"
                    : " days ago");
        }

        return new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
        ).format(timestamp.toDate());
    }

    private static String getTitleFromType(
            String type
    ) {

        if (type == null) {
            return "New Activity";
        }

        switch (type) {

            case "like":
                return "New Like";

            case "comment":
                return "New Comment";

            case "follow":
                return "New Follower";

            case "message":
                return "New Message";

            case "story":
                return "New Story";

            case "post":
                return "New Post";

            default:
                return "New Activity";
        }
    }
}
