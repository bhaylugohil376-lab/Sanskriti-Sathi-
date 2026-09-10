package com.sanskritisathi.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView notificationsRecyclerView;
    private TextView emptyText;
    private TextView loadingText;

    private final List<NotificationModel> notificationList = new ArrayList<>();
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        ImageButton backButton = findViewById(R.id.notificationsBackButton);
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        emptyText = findViewById(R.id.notificationsEmptyText);
        loadingText = findViewById(R.id.notificationsLoadingText);

        backButton.setOnClickListener(v -> finish());

        notificationsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new NotificationAdapter(
                this,
                notificationList
        );

        notificationsRecyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            showEmpty("Login to see your notifications.");
            return;
        }

        loadingText.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        notificationsRecyclerView.setVisibility(View.GONE);

        String uid = auth.getCurrentUser().getUid();

        NotificationFirebaseHelper.getNotifications(
                uid,
                notifications -> {

                    loadingText.setVisibility(View.GONE);

                    notificationList.clear();

                    if (notifications != null) {
                        notificationList.addAll(notifications);
                    }

                    adapter.notifyDataSetChanged();

                    if (notificationList.isEmpty()) {
                        showEmpty("You don't have any notifications yet.");
                    } else {
                        emptyText.setVisibility(View.GONE);
                        notificationsRecyclerView.setVisibility(View.VISIBLE);
                    }
                },
                error -> {

                    loadingText.setVisibility(View.GONE);

                    notificationList.clear();
                    adapter.notifyDataSetChanged();

                    showEmpty(
                            "Unable to load notifications.\nPlease check your internet connection."
                    );
                }
        );
    }

    private void showEmpty(String message) {
        emptyText.setText(message);
        emptyText.setVisibility(View.VISIBLE);
        notificationsRecyclerView.setVisibility(View.GONE);
    }
}
