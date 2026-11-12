package com.sanskritisathi.app;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PostUploadActivity extends AppCompatActivity {
    private Uri selected;
    private ImageView preview;
    private EditText captionInput;
    private Spinner categorySpinner, visibilitySpinner;
    private Button publishButton;
    private ProgressBar progress;

    private final ActivityResultLauncher<String> picker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selected = uri;
                    preview.setImageURI(uri);
                }
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_post_upload);
        preview = findViewById(R.id.postPreviewImage);
        captionInput = findViewById(R.id.postCaptionInput);
        categorySpinner = findViewById(R.id.postCategorySpinner);
        visibilitySpinner = findViewById(R.id.postVisibilitySpinner);
        publishButton = findViewById(R.id.uploadPostButton);
        progress = findViewById(R.id.postUploadProgress);

        categorySpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Culture", "Festival", "Food", "Art", "Travel", "Other"}));
        visibilitySpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Public", "Followers", "Private"}));

        findViewById(R.id.selectPostImageButton).setOnClickListener(v -> picker.launch("image/*"));
        publishButton.setOnClickListener(v -> publishPost());
    }

    private void publishPost() {
        if (!SupabaseAuthManager.isLoggedIn(this)) {
            Toast.makeText(this, "Pehle login karein.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selected == null) {
            Toast.makeText(this, "Photo select karo.", Toast.LENGTH_SHORT).show();
            return;
        }
        String caption = captionInput.getText().toString().trim();
        if (caption.isEmpty()) {
            Toast.makeText(this, "Caption likho.", Toast.LENGTH_SHORT).show();
            return;
        }
        publishButton.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                String uid = SupabaseAuthManager.getUserId(this);
                String token = SupabaseAuthManager.getAccessToken(this);
                String email = SupabaseAuthManager.getUserEmail(this);
                if (uid == null || uid.trim().isEmpty() || token == null || token.isEmpty())
                    throw new Exception("Login session nahi mili.");

                String mime = getContentResolver().getType(selected);
                if (mime == null || !(mime.equals("image/jpeg") || mime.equals("image/png") || mime.equals("image/webp")))
                    throw new Exception("JPG, PNG ya WEBP photo select karein.");

                String ext = mime.equals("image/png") ? ".png" : mime.equals("image/webp") ? ".webp" : ".jpg";
                String objectPath = uid + "/post_" + UUID.randomUUID() + ext;
                String encodedPath = encodePath(objectPath);

                HttpURLConnection upload = (HttpURLConnection) new URL(
                        SupabaseConfig.PROJECT_URL + "/storage/v1/object/posts/" + encodedPath).openConnection();
                upload.setRequestMethod("POST");
                upload.setDoOutput(true);
                upload.setConnectTimeout(20000);
                upload.setReadTimeout(120000);
                upload.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY);
                upload.setRequestProperty("Authorization", "Bearer " + token);
                upload.setRequestProperty("Content-Type", mime);
                upload.setRequestProperty("x-upsert", "false");
                try (InputStream in = getContentResolver().openInputStream(selected);
                     OutputStream out = upload.getOutputStream()) {
                    if (in == null) throw new Exception("Photo read nahi ho saki.");
                    byte[] buffer = new byte[32768];
                    int n;
                    while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
                }
                int uploadCode = upload.getResponseCode();
                String uploadResponse = readResponse(upload, uploadCode);
                upload.disconnect();
                if (uploadCode < 200 || uploadCode >= 300)
                    throw new Exception("Photo upload HTTP " + uploadCode + ": " + uploadResponse);

                String publicUrl = SupabaseConfig.PROJECT_URL
                        + "/storage/v1/object/public/posts/" + encodedPath;

                JSONObject row = new JSONObject();
                row.put("author_uid", uid);
                row.put("author", (email == null || email.trim().isEmpty()) ? "Sanskriti User" : email.split("@")[0]);
                row.put("category", String.valueOf(categorySpinner.getSelectedItem()));
                row.put("caption", caption);
                row.put("image_url", publicUrl);
                row.put("visibility", String.valueOf(visibilitySpinner.getSelectedItem()));

                HttpURLConnection insert = (HttpURLConnection) new URL(
                        SupabaseConfig.PROJECT_URL + "/rest/v1/posts").openConnection();
                insert.setRequestMethod("POST");
                insert.setDoOutput(true);
                insert.setConnectTimeout(20000);
                insert.setReadTimeout(30000);
                insert.setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY);
                insert.setRequestProperty("Authorization", "Bearer " + token);
                insert.setRequestProperty("Content-Type", "application/json");
                insert.setRequestProperty("Prefer", "return=minimal");
                try (OutputStream out = insert.getOutputStream()) {
                    out.write(row.toString().getBytes(StandardCharsets.UTF_8));
                }
                int insertCode = insert.getResponseCode();
                String insertResponse = readResponse(insert, insertCode);
                insert.disconnect();
                if (insertCode < 200 || insertCode >= 300)
                    throw new Exception("Post save HTTP " + insertCode + ": " + insertResponse);

                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    publishButton.setEnabled(true);
                    Toast.makeText(this, "Post publish ho gaya ✓", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    publishButton.setEnabled(true);
                    Toast.makeText(this, "Publish failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private static String encodePath(String path) throws Exception {
        String[] parts = path.split("/");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (result.length() > 0) result.append("/");
            result.append(URLEncoder.encode(part, "UTF-8").replace("+", "%20"));
        }
        return result.toString();
    }

    private static String readResponse(HttpURLConnection c, int code) throws Exception {
        InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
        if (in == null) return "";
        try (InputStream stream = in; java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096]; int n;
            while ((n = stream.read(buffer)) != -1) out.write(buffer, 0, n);
            return out.toString("UTF-8");
        }
    }
}
