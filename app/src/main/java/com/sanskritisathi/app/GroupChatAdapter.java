package com.sanskritisathi.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupChatAdapter
        extends RecyclerView.Adapter<GroupChatAdapter.MessageViewHolder> {

    private final List<GroupMessage> messageList;

    public GroupChatAdapter(List<GroupMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_group_message,
                        parent,
                        false
                );

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MessageViewHolder holder,
            int position) {

        GroupMessage message = messageList.get(position);

        holder.usernameText.setText(message.getUsername());
        holder.messageText.setText(message.getText());

        String time = new SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
        ).format(new Date(message.getCreatedAt()));

        holder.timeText.setText(time);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder
            extends RecyclerView.ViewHolder {

        TextView usernameText;
        TextView messageText;
        TextView timeText;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameText =
                    itemView.findViewById(
                            R.id.groupMessageUsername
                    );

            messageText =
                    itemView.findViewById(
                            R.id.groupMessageText
                    );

            timeText =
                    itemView.findViewById(
                            R.id.groupMessageTime
                    );
        }
    }
}
