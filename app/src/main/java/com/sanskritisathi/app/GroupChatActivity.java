package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GroupChatActivity extends AppCompatActivity {

    private EditText messageInput;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_chat);

        messageInput = findViewById(R.id.groupMessageInput);
        sendButton = findViewById(R.id.groupSendButton);

        sendButton.setOnClickListener(v -> {

            String message =
                    messageInput.getText().toString().trim();

            if (message.isEmpty()) {
                Toast.makeText(
                        GroupChatActivity.this,
                        "Message likho.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Toast.makeText(
                    GroupChatActivity.this,
                    "Group message Firebase mein next step mein save hoga.",
                    Toast.LENGTH_SHORT
            ).show();

            messageInput.setText("");
        });
    }
}
