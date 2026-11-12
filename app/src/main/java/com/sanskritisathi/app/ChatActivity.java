package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    private EditText chatSearchInput;
    private TextView primaryTab, generalTab, requestsTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatSearchInput = findViewById(R.id.searchBar);

        LinearLayout tabContainer = findViewById(R.id.chatTabsContainer);
        if (tabContainer != null && tabContainer.getChildCount() >= 3) {
            primaryTab = (TextView) tabContainer.getChildAt(0);
            generalTab = (TextView) tabContainer.getChildAt(1);
            requestsTab = (TextView) tabContainer.getChildAt(2);
        }

        // Existing hard-coded example conversations are not real user chats.
        hideExampleConversation(R.id.chatAvatar1);
        hideExampleConversation(R.id.chatAvatar2);

        View newChat = findViewById(R.id.newChatButton);
        if (newChat != null) {
            newChat.setOnClickListener(v ->
                    startActivity(new Intent(this, SearchActivity.class)));
        }

        if (primaryTab != null) primaryTab.setOnClickListener(v -> openCommunityChat());
        if (generalTab != null) generalTab.setOnClickListener(v -> openCommunityChat());
        if (requestsTab != null) requestsTab.setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));

        if (chatSearchInput != null) {
            chatSearchInput.setOnEditorActionListener((v, actionId, event) -> {
                String query = chatSearchInput.getText().toString().trim();
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("search_query", query);
                startActivity(intent);
                return true;
            });
        }

        // The community chat is the existing server-backed conversation.
        View content = findViewById(R.id.chatTabsContainer);
        if (content != null) {
            content.setOnLongClickListener(v -> {
                openCommunityChat();
                return true;
            });
        }
    }

    private void hideExampleConversation(int avatarId) {
        View avatar = findViewById(avatarId);
        if (avatar == null) return;
        View row = (View) avatar.getParent();
        if (row != null) row.setVisibility(View.GONE);
    }

    private void openCommunityChat() {
        startActivity(new Intent(this, GroupChatActivity.class));
    }
}
