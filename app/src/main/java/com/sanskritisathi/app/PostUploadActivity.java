package com.sanskritisathi.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class PostUploadActivity extends AppCompatActivity {
    private Uri selected;
    private ImageView preview;
    private final ActivityResultLauncher<String> picker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) { selected = uri; preview.setImageURI(uri); }
            });
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_post_upload);
        preview = findViewById(R.id.postPreviewImage);
        Button pick = findViewById(R.id.selectPostImageButton);
        Button publish = findViewById(R.id.uploadPostButton);
        pick.setOnClickListener(v -> picker.launch("image/*"));
        publish.setOnClickListener(v -> {
            if (selected == null) {
                Toast.makeText(this, "Photo select karo.", Toast.LENGTH_SHORT).show();
                return;
            }
            B2MediaHelper.uploadImage(this, selected, "posts", new B2MediaHelper.UploadCallback() {
                @Override public void onProgress(int progress) { }
                @Override public void onSuccess(String fileName) {
                    runOnUiThread(() -> Toast.makeText(PostUploadActivity.this, "Post image B2 me upload ho gayi ✓", Toast.LENGTH_LONG).show());
                }
                @Override public void onError(String message) {
                    runOnUiThread(() -> Toast.makeText(PostUploadActivity.this, message, Toast.LENGTH_LONG).show());
                }
            });
        });
    }
}
