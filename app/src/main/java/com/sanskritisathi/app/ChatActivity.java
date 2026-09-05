package com.sanskritisathi.app;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    private LinearLayout oneToOneChatButton;
    private LinearLayout groupChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        oneToOneChatButton = findViewById(R.id.oneToOneChatButton);
        groupChatButton = findViewById(R.id.groupChatButton);

        // 1-to-1 Chat
        oneToOneChatButton.setOnClickListener(v ->
                Toast.makeText(
                        ChatActivity.this,
                        "1-to-1 Chat next step mein connect hoga.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        // Group Chat
        groupChatButton.setOnClickListener(v ->
                Toast.makeText(
                        ChatActivity.this,
                        "Group Chat next step mein connect hoga.",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }
}
