package com.sanskritisathi.app;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class GroupChatFirebaseHelper {

    private static final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private static final String GROUP_ID =
            "sanskriti_sathi_group";

    public interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface MessagesCallback {
        void onSuccess(
                java.util.List<GroupMessage> messages
        );

        void onError(String message);
    }

    public static void sendMessage(
            String text,
            ActionCallback callback
    ) {

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onError("Pehle login karein.");
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            callback.onError("Message likho.");
            return;
        }

        String messageText = text.trim();

        if (messageText.length() > 500) {
            callback.onError(
                    "Message maximum 500 characters ka ho sakta hai."
            );
            return;
        }

        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    String username =
                            snapshot.getString("username");

                    if (username == null ||
                            username.trim().isEmpty()) {

                        username =
                                snapshot.getString("name");
                    }

                    if (username == null ||
                            username.trim().isEmpty()) {

                        username = "User";
                    }

                    String messageId =
                            db.collection("group_messages")
                                    .document()
                                    .getId();

                    Map<String, Object> data =
                            new HashMap<>();

                    data.put("messageId", messageId);
                    data.put("groupId", GROUP_ID);
                    data.put("userId", uid);
                    data.put("username", username);
                    data.put("text", messageText);
                    data.put(
                            "createdAt",
                            System.currentTimeMillis()
                    );

                    db.collection("group_messages")
                            .document(messageId)
                            .set(data)
                            .addOnSuccessListener(unused ->
                                    callback.onSuccess()
                            )
                            .addOnFailureListener(e ->
                                    callback.onError(
                                            "Message send nahi hua: "
                                                    + e.getMessage()
                                    )
                            );
                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "User profile load nahi hua."
                        )
                );
    }

    public static void getMessages(
            MessagesCallback callback
    ) {

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onError("Pehle login karein.");
            return;
        }

        db.collection("group_messages")
                .whereEqualTo("groupId", GROUP_ID)
                .orderBy(
                        "createdAt",
                        Query.Direction.ASCENDING
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    java.util.List<GroupMessage> list =
                            new java.util.ArrayList<>();

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        String messageId =
                                document.getString("messageId");

                        String userId =
                                document.getString("userId");

                        String username =
                                document.getString("username");

                        String text =
                                document.getString("text");

                        Long createdAt =
                                document.getLong("createdAt");

                        if (messageId == null) {
                            messageId = document.getId();
                        }

                        if (userId == null) {
                            userId = "";
                        }

                        if (username == null) {
                            username = "User";
                        }

                        if (text == null) {
                            text = "";
                        }

                        long time =
                                createdAt != null
                                        ? createdAt
                                        : 0;

                        list.add(
                                new GroupMessage(
                                        messageId,
                                        userId,
                                        username,
                                        text,
                                        time
                                )
                        );
                    }

                    callback.onSuccess(list);
                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Messages load nahi hue: "
                                        + e.getMessage()
                        )
                );
    }

    public static void deleteMessage(
            String messageId,
            ActionCallback callback
    ) {

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onError("Pehle login karein.");
            return;
        }

        if (messageId == null ||
                messageId.trim().isEmpty()) {

            callback.onError("Invalid message.");
            return;
        }

        db.collection("group_messages")
                .document(messageId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        callback.onError(
                                "Message nahi mila."
                        );
                        return;
                    }

                    String ownerUid =
                            document.getString("userId");

                    if (!user.getUid().equals(ownerUid)) {
                        callback.onError(
                                "Sirf apna message delete kar sakte hain."
                        );
                        return;
                    }

                    db.collection("group_messages")
                            .document(messageId)
                            .delete()
                            .addOnSuccessListener(unused ->
                                    callback.onSuccess()
                            )
                            .addOnFailureListener(e ->
                                    callback.onError(
                                            "Message delete nahi hua."
                                    )
                            );
                })
                .addOnFailureListener(e ->
                        callback.onError(
                                "Message check nahi ho saka."
                        )
                );
    }
}
