package com.sanskritisathi.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OneToOneChatActivity extends AppCompatActivity {
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private TextView chatUsernameText;
    private EditText messageInput;
    private Button sendButton;
    private LinearLayout messagesContainer;
    private ScrollView messagesScroll;
    private String receiverId;
    private String myId;
    private String token;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_one_to_one_chat);
        chatUsernameText = findViewById(R.id.chatUsernameText);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messagesContainer = findViewById(R.id.messagesContainer);
        messagesScroll = (ScrollView) messagesContainer.getParent();

        String username = getIntent().getStringExtra("username");
        chatUsernameText.setText(username == null ? "User" : username);
        receiverId = getIntent().getStringExtra("receiver_id");
        if (receiverId == null) receiverId = getIntent().getStringExtra("user_id");
        myId = SupabaseAuthManager.getUserId(this);
        token = SupabaseAuthManager.getAccessToken(this);

        if (receiverId == null || receiverId.trim().isEmpty() || receiverId.equals(myId)) {
            sendButton.setEnabled(false);
            addBubble("Chat recipient missing. Chat open karte waqt receiver_id bhejein.", false);
        } else if (myId == null || token == null || token.isEmpty()) {
            sendButton.setEnabled(false);
            addBubble("Pehle login karein.", false);
        } else {
            loadMessages();
        }
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        io.execute(() -> {
            HttpURLConnection c = null;
            try {
                String filter = "or=(and(sender_id.eq." + myId + ",receiver_id.eq." + receiverId
                        + "),and(sender_id.eq." + receiverId + ",receiver_id.eq." + myId + "))";
                URL url = new URL(SupabaseConfig.PROJECT_URL + "/rest/v1/direct_messages?"
                        + filter + "&select=id,sender_id,receiver_id,message,created_at&order=created_at.asc&limit=300");
                c = open(url, "GET");
                int code = c.getResponseCode();
                String response = read(c, code);
                if (code < 200 || code >= 300) throw new Exception(response);
                JSONArray rows = new JSONArray(response);
                runOnUiThread(() -> {
                    messagesContainer.removeAllViews();
                    for (int i = 0; i < rows.length(); i++) {
                        JSONObject row = rows.optJSONObject(i);
                        if (row != null) addBubble(row.optString("message"), myId.equals(row.optString("sender_id")));
                    }
                    messagesScroll.post(() -> messagesScroll.fullScroll(ScrollView.FOCUS_DOWN));
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Chat load failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally { if (c != null) c.disconnect(); }
        });
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) return;
        if (receiverId == null || receiverId.isEmpty() || myId == null || myId.isEmpty()) {
            Toast.makeText(this, "Chat recipient missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        sendButton.setEnabled(false);
        io.execute(() -> {
            HttpURLConnection c = null;
            try {
                JSONObject body = new JSONObject();
                body.put("sender_id", myId);
                body.put("receiver_id", receiverId);
                body.put("message", message);
                c = open(new URL(SupabaseConfig.PROJECT_URL + "/rest/v1/direct_messages"), "POST");
                c.setDoOutput(true);
                c.setRequestProperty("Prefer", "return=minimal");
                try (OutputStream out = c.getOutputStream()) {
                    out.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
                int code = c.getResponseCode();
                String response = read(c, code);
                if (code < 200 || code >= 300) throw new Exception(response);
                runOnUiThread(() -> {
                    messageInput.setText("");
                    sendButton.setEnabled(true);
                    addBubble(message, true);
                    messagesScroll.post(() -> messagesScroll.fullScroll(ScrollView.FOCUS_DOWN));
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    sendButton.setEnabled(true);
                    Toast.makeText(this, "Message send failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            } finally { if (c != null) c.disconnect(); }
        });
    }

    private HttpURLConnection open(URL url, String method) throws Exception {
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        c.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY);
        c.setRequestProperty("Authorization", "Bearer " + token);
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("Content-Type", "application/json");
        return c;
    }

    private String read(HttpURLConnection c, int code) throws Exception {
        InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
        if (in == null) return "";
        try (InputStream stream = in; java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            byte[] b = new byte[4096]; int n;
            while ((n = stream.read(b)) != -1) out.write(b, 0, n);
            return out.toString("UTF-8");
        }
    }

    private void addBubble(String message, boolean mine) {
        TextView bubble = new TextView(this);
        bubble.setText(message);
        bubble.setTextSize(15);
        bubble.setTextColor(mine ? Color.WHITE : Color.rgb(59, 33, 23));
        bubble.setBackgroundColor(mine ? Color.rgb(217, 107, 39) : Color.rgb(255, 241, 230));
        bubble.setPadding(18, 12, 18, 12);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.75f),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = mine ? Gravity.END : Gravity.START;
        lp.bottomMargin = 10;
        bubble.setLayoutParams(lp);
        messagesContainer.addView(bubble);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        io.shutdownNow();
    }
}
