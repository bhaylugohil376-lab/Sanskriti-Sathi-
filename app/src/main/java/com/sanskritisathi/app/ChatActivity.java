package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    private EditText chatSearchInput;
    private View chatItem1, chatItem2, chatItem3;
    private View primaryTab, generalTab, requestsTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Bind Views matching updated XML IDs
        chatSearchInput = findViewById(R.id.chatSearchInput);
        chatItem1 = findViewById(R.id.chatItem1);
        chatItem2 = findViewById(R.id.chatItem2);
        chatItem3 = findViewById(R.id.chatItem3);

        primaryTab = findViewById(R.id.primaryTab);
        generalTab = findViewById(R.id.generalTab);
        requestsTab = findViewById(R.id.requestsTab);

        // Top Pencil Button Click
        if (findViewById(R.id.newChatButton) != null) {
            findViewById(R.id.newChatButton).setOnClickListener(v -> 
                Toast.makeText(ChatActivity.this, "New Chat option clicked", Toast.LENGTH_SHORT).show()
            );
        }

        // Tab Selection Click Listeners
        if (primaryTab != null) {
            primaryTab.setOnClickListener(v -> selectTab("Primary"));
        }
        if (generalTab != null) {
            generalTab.setOnClickListener(v -> selectTab("General"));
        }
        if (requestsTab != null) {
            requestsTab.setOnClickListener(v -> selectTab("Requests"));
        }

        // Search Input Filter
        if (chatSearchInput != null) {
            chatSearchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterChats(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void selectTab(String tabName) {
        Toast.makeText(this, tabName + " tab selected", Toast.LENGTH_SHORT).show();
    }

    private void filterChats(String query) {
        // Simple search visibility logic
        if (query.isEmpty()) {
            if (chatItem1 != null) chatItem1.setVisibility(View.VISIBLE);
            if (chatItem2 != null) chatItem2.setVisibility(View.VISIBLE);
            if (chatItem3 != null) chatItem3.setVisibility(View.VISIBLE);
        } else {
            if (chatItem1 != null) chatItem1.setVisibility(View.GONE);
            if (chatItem2 != null) chatItem2.setVisibility(View.GONE);
            if (chatItem3 != null) chatItem3.setVisibility(View.GONE);
        }
    }
}
