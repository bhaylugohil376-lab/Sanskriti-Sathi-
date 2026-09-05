package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {

    private RecyclerView groupMessagesRecyclerView;
    private EditText messageInput;
    private Button sendButton;

    private GroupChatAdapter adapter;
    private final List<GroupMessage> messageList =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_chat);

        groupMessagesRecyclerView =
                findViewById(R.id.groupMessagesRecyclerView);

        messageInput =
                findViewById(R.id.groupMessageInput);

        sendButton =
                findViewById(R.id.groupSendButton);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        layoutManager.setStackFromEnd(true);

        groupMessagesRecyclerView.setLayoutManager(
                layoutManager
        );

        adapter = new GroupChatAdapter(messageList);

        groupMessagesRecyclerView.setAdapter(adapter);

        loadMessages();

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {

        GroupChatFirebaseHelper.getMessages(
                new GroupChatFirebaseHelper.MessagesCallback() {

                    @Override
                    public void onSuccess(
                            List<GroupMessage> messages) {

                        messageList.clear();
                        messageList.addAll(messages);

                        adapter.notifyDataSetChanged();

                        if (!messageList.isEmpty()) {
                            groupMessagesRecyclerView.scrollToPosition(
                                    messageList.size() - 1
                            );
                        }
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                GroupChatActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void sendMessage() {

        String message =
                messageInput.getText()
                        .toString()
                        .trim();

        if (message.isEmpty()) {

            Toast.makeText(
                    this,
                    "Message likho.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        sendButton.setEnabled(false);

        GroupChatFirebaseHelper.sendMessage(
                message,
                new GroupChatFirebaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        messageInput.setText("");

                        sendButton.setEnabled(true);

                        loadMessages();
                    }

                    @Override
                    public void onError(String message) {

                        sendButton.setEnabled(true);

                        Toast.makeText(
                                GroupChatActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
}
