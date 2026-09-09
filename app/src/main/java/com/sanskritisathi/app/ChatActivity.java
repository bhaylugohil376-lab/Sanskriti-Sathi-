package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    private EditText chatSearchInput;

    private LinearLayout chatItem1;
    private LinearLayout chatItem2;
    private LinearLayout chatItem3;

    private TextView primaryTab;
    private TextView generalTab;
    private TextView requestsTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        chatSearchInput = findViewById(R.id.chatSearchInput);

        chatItem1 = findViewById(R.id.chatItem1);
        chatItem2 = findViewById(R.id.chatItem2);
        chatItem3 = findViewById(R.id.chatItem3);

        primaryTab = findViewById(R.id.primaryTab);
        generalTab = findViewById(R.id.generalTab);
        requestsTab = findViewById(R.id.requestsTab);

        setupChatItems();
        setupTabs();
        setupSearch();
    }

    private void setupChatItems() {

        // Sanskriti Sathi demo conversation
        chatItem1.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            ChatActivity.this,
                            OneToOneChatActivity.class
                    );

            intent.putExtra(
                    "username",
                    "Sanskriti Sathi"
            );

            /*
             * Jab actual user search/profile system connect hoga,
             * yahan real recipient UID pass hoga.
             */

            intent.putExtra(
                    "userId",
                    ""
            );

            startActivity(intent);
        });


        // Shiv Bhakt conversation
        chatItem2.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            ChatActivity.this,
                            OneToOneChatActivity.class
                    );

            intent.putExtra(
                    "username",
                    "Shiv Bhakt"
            );

            intent.putExtra(
                    "userId",
                    ""
            );

            startActivity(intent);
        });


        // Group conversation
        chatItem3.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            ChatActivity.this,
                            GroupChatActivity.class
                    );

            intent.putExtra(
                    "groupId",
                    "sanskriti_sathi_group"
            );

            intent.putExtra(
                    "groupName",
                    "Temple Group"
            );

            startActivity(intent);
        });
    }

    private void setupTabs() {

        selectPrimary();

        primaryTab.setOnClickListener(v ->
                selectPrimary()
        );

        generalTab.setOnClickListener(v ->
                selectGeneral()
        );

        requestsTab.setOnClickListener(v ->
                selectRequests()
        );
    }

    private void selectPrimary() {

        primaryTab.setTextColor(
                getColor(android.R.color.holo_orange_light)
        );

        generalTab.setTextColor(
                getColor(android.R.color.darker_gray)
        );

        requestsTab.setTextColor(
                getColor(android.R.color.darker_gray)
        );
    }

    private void selectGeneral() {

        primaryTab.setTextColor(
                getColor(android.R.color.darker_gray)
        );

        generalTab.setTextColor(
                getColor(android.R.color.holo_orange_light)
        );

        requestsTab.setTextColor(
                getColor(android.R.color.darker_gray)
        );
    }

    private void selectRequests() {

        primaryTab.setTextColor(
                getColor(android.R.color.darker_gray)
        );

        generalTab.setTextColor(
                getColor(android.R.color.darker_gray)
        );

        requestsTab.setTextColor(
                getColor(android.R.color.holo_orange_light)
        );
    }

    private void setupSearch() {

        chatSearchInput.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        filterChats(
                                s.toString().trim()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }

    private void filterChats(String query) {

        String search =
                query.toLowerCase();

        filterItem(
                chatItem1,
                "sanskriti sathi",
                search
        );

        filterItem(
                chatItem2,
                "shiv bhakt",
                search
        );

        filterItem(
                chatItem3,
                "temple group",
                search
        );
    }

    private void filterItem(
            LinearLayout item,
            String itemName,
            String query
    ) {

        if (query.isEmpty()) {

            item.setVisibility(
                    LinearLayout.VISIBLE
            );

            return;
        }

        if (itemName.contains(query)) {

            item.setVisibility(
                    LinearLayout.VISIBLE
            );

        } else {

            item.setVisibility(
                    LinearLayout.GONE
            );
        }
    }
}
