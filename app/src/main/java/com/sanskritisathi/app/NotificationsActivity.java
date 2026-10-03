package com.sanskritisathi.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView notificationsRecyclerView;
    private TextView notificationsLoadingText;
    private TextView notificationsEmptyText;

    private NotificationAdapter adapter;

    private final List<NotificationModel> notificationList =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notifications);

        notificationsRecyclerView =
                findViewById(R.id.notificationsRecyclerView);

        notificationsLoadingText =
                findViewById(R.id.notificationsLoadingText);

        notificationsEmptyText =
                findViewById(R.id.notificationsEmptyText);

        findViewById(R.id.notificationsBackButton)
                .setOnClickListener(v -> finish());

        notificationsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        notificationsRecyclerView.setHasFixedSize(false);

        adapter = new NotificationAdapter(
                this,
                notificationList
        );

        notificationsRecyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {

        String uid =
                SupabaseAuthManager.getUserId(this);

        if (uid == null || uid.trim().isEmpty()) {

            showEmptyState(
                    "Please login to see notifications."
            );

            return;
        }

        showLoading(true);

        NotificationSupabaseHelper.getNotifications(
                this,
                uid,
                new NotificationSupabaseHelper.NotificationCallback() {

                    @Override
                    public void onSuccess(
                            List<NotificationModel> notifications
                    ) {

                        notificationList.clear();

                        if (notifications != null) {
                            notificationList.addAll(
                                    notifications
                            );
                        }

                        adapter.notifyDataSetChanged();

                        showLoading(false);

                        if (notificationList.isEmpty()) {

                            showEmptyState(
                                    "You don't have any notifications yet."
                            );

                        } else {

                            notificationsEmptyText
                                    .setVisibility(View.GONE);

                            notificationsRecyclerView
                                    .setVisibility(View.VISIBLE);

                            markAllNotificationsAsRead(uid);
                        }
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        showLoading(false);

                        notificationList.clear();

                        adapter.notifyDataSetChanged();

                        notificationsRecyclerView
                                .setVisibility(View.GONE);

                        notificationsEmptyText
                                .setVisibility(View.VISIBLE);

                        notificationsEmptyText.setText(
                                message == null ||
                                        message.trim().isEmpty()
                                        ? "Notifications load nahi hui."
                                        : message
                        );

                        Toast.makeText(
                                NotificationsActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void markAllNotificationsAsRead(
            String uid
    ) {

        NotificationSupabaseHelper.markAllAsRead(
                this,
                uid
        );

        // UI immediately update
        for (NotificationModel notification :
                notificationList) {

            if (!notification.isRead()) {
                // Adapter model may not have a setter.
                // Reload will reflect the server state.
            }
        }
    }

    private void showLoading(
            boolean loading
    ) {

        notificationsLoadingText.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        if (loading) {

            notificationsRecyclerView
                    .setVisibility(View.GONE);

            notificationsEmptyText
                    .setVisibility(View.GONE);

        } else {

            notificationsRecyclerView
                    .setVisibility(
                            notificationList.isEmpty()
                                    ? View.GONE
                                    : View.VISIBLE
                    );
        }
    }

    private void showEmptyState(
            String message
    ) {

        notificationsLoadingText
                .setVisibility(View.GONE);

        notificationsRecyclerView
                .setVisibility(View.GONE);

        notificationsEmptyText
                .setVisibility(View.VISIBLE);

        notificationsEmptyText.setText(
                message
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh when returning to notification screen.
        if (adapter != null) {
            loadNotifications();
        }
    }
}
