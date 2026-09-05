package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OneToOneChatActivity extends AppCompatActivity {

    private TextView chatUsernameText;
    private EditText messageInput;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_one_to_one_chat);

        chatUsernameText = findViewById(R.id.chatUsernameText);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        String username =
                getIntent().getStringExtra("username");

        if (username == null || username.trim().isEmpty()) {
            username = "User";
        }

        chatUsernameText.setText(username);

        sendButton.setOnClickListener(v -> {

            String message =
                    messageInput.getText().toString().trim();

            if (message.isEmpty()) {
                Toast.makeText(
                        OneToOneChatActivity.this,
                        "Message likho.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Toast.makeText(
                    OneToOneChatActivity.this,
                    "Message Firebase mein next step mein save hoga.",
                    Toast.LENGTH_SHORT
            ).show();

            messageInput.setText("");
        });
    }
}
