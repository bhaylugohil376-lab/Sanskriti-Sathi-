package com.sanskritisathi.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private final List<NotificationModel> notificationList;

    public NotificationAdapter(
            Context context,
            List<NotificationModel> notificationList
    ) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_notification,
                parent,
                false
        );

        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull NotificationViewHolder holder,
            int position
    ) {
        NotificationModel notification = notificationList.get(position);

        String title = notification.getTitle();
        String message = notification.getMessage();
        String time = notification.getTime();

        holder.title.setText(
                title == null || title.trim().isEmpty()
                        ? "Notification"
                        : title
        );

        holder.message.setText(
                message == null || message.trim().isEmpty()
                        ? ""
                        : message
        );

        holder.time.setText(
                time == null || time.trim().isEmpty()
                        ? "Just now"
                        : time
        );

        if (notification.isRead()) {
            holder.unreadDot.setVisibility(View.INVISIBLE);
        } else {
            holder.unreadDot.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList == null
                ? 0
                : notificationList.size();
    }

    static class NotificationViewHolder
            extends RecyclerView.ViewHolder {

        TextView title;
        TextView message;
        TextView time;
        View unreadDot;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(
                    R.id.notificationTitle
            );

            message = itemView.findViewById(
                    R.id.notificationMessage
            );

            time = itemView.findViewById(
                    R.id.notificationTime
            );

            unreadDot = itemView.findViewById(
                    R.id.notificationUnreadDot
            );
        }
    }
}
