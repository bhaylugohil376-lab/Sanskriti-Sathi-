package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    private EditText chatSearchInput;
    private View chatItem1, chatItem2;
    private TextView primaryTab, generalTab, requestsTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Bind Views matching XML IDs
        chatSearchInput = findViewById(R.id.searchBar);
        chatItem1 = findViewById(R.id.chatAvatar1);
        chatItem2 = findViewById(R.id.chatAvatar2);

        // Get tab references from container
        LinearLayout tabContainer = findViewById(R.id.chatTabsContainer);
        if (tabContainer != null && tabContainer.getChildCount() >= 3) {
            primaryTab = (TextView) tabContainer.getChildAt(0);
            generalTab = (TextView) tabContainer.getChildAt(1);
            requestsTab = (TextView) tabContainer.getChildAt(2);
        }

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
        if (query.isEmpty()) {
            if (chatItem1 != null) chatItem1.setVisibility(View.VISIBLE);
            if (chatItem2 != null) chatItem2.setVisibility(View.VISIBLE);
        } else {
            if (chatItem1 != null) chatItem1.setVisibility(View.GONE);
            if (chatItem2 != null) chatItem2.setVisibility(View.GONE);
        }
    }
}
