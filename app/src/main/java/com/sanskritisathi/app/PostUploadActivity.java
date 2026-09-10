package com.sanskritisathi.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class PostUploadActivity extends AppCompatActivity {

    private ImageView postImagePreview;
    private EditText captionInput;
    private Spinner categorySpinner;
    private ProgressBar progressBar;
    private Button publishButton;

    private Uri selectedImageUri;
    private Bitmap cameraBitmap;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) {
                            openCamera();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Camera permission required.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_upload);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        ImageButton backButton =
                findViewById(R.id.postBackButton);

        postImagePreview =
                findViewById(R.id.postImagePreview);

        captionInput =
                findViewById(R.id.postCaptionInput);

        categorySpinner =
                findViewById(R.id.postCategorySpinner);

        progressBar =
                findViewById(R.id.postUploadProgress);

        publishButton =
                findViewById(R.id.postPublishButton);

        Button galleryButton =
                findViewById(R.id.postGalleryButton);

        Button cameraButton =
                findViewById(R.id.postCameraButton);

        setupCategorySpinner();

        galleryLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                selectedImageUri = uri;
                                cameraBitmap = null;
                                postImagePreview.setImageURI(uri);
                            }
                        }
                );

        cameraLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode()
                                    == Activity.RESULT_OK
                                    && result.getData() != null) {

                                Bundle extras =
                                        result.getData().getExtras();

                                if (extras != null) {

                                    Object image =
                                            extras.get("data");

                                    if (image instanceof Bitmap) {

                                        cameraBitmap =
                                                (Bitmap) image;

                                        selectedImageUri = null;

                                        postImagePreview
                                                .setImageBitmap(
                                                        cameraBitmap
                                                );
                                    }
                                }
                            }
                        }
                );

        backButton.setOnClickListener(
                v -> finish()
        );

        galleryButton.setOnClickListener(
                v -> galleryLauncher.launch("image/*")
        );

        cameraButton.setOnClickListener(
                v -> checkCameraPermission()
        );

        publishButton.setOnClickListener(
                v -> publishPost()
        );
    }

    private void setupCategorySpinner() {

        String[] categories = {
                "Raja",
                "Temple",
                "Devi Devta",
                "Bhagavad Gita",
                "Culture",
                "Festival",
                "General"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        categorySpinner.setAdapter(adapter);
    }

    private void checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            openCamera();

        } else {

            cameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
            );
        }
    }

    private void openCamera() {

        Intent intent =
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        cameraLauncher.launch(intent);
    }

    private void publishPost() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String caption =
                captionInput.getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(caption)) {

            captionInput.setError(
                    "Write something about your post"
            );

            captionInput.requestFocus();

            return;
        }

        if (selectedImageUri == null
                && cameraBitmap == null) {

            Toast.makeText(
                    this,
                    "Please select a photo first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        setUploading(true);

        String uid =
                auth.getCurrentUser().getUid();

        String category =
                categorySpinner
                        .getSelectedItem()
                        .toString();

        String postId =
                firestore.collection("posts")
                        .document()
                        .getId();

        StorageReference imageReference =
                storage.getReference()
                        .child("posts")
                        .child(uid)
                        .child(postId + ".jpg");

        if (selectedImageUri != null) {

            uploadUriImage(
                    imageReference,
                    selectedImageUri,
                    uid,
                    postId,
                    category,
                    caption
            );

        } else {

            uploadCameraImage(
                    imageReference,
                    cameraBitmap,
                    uid,
                    postId,
                    category,
                    caption
            );
        }
    }

    private void uploadUriImage(
            StorageReference imageReference,
            Uri imageUri,
            String uid,
            String postId,
            String category,
            String caption
    ) {

        imageReference
                .putFile(imageUri)
                .continueWithTask(task -> {

                    if (!task.isSuccessful()
                            && task.getException() != null) {

                        throw task.getException();
                    }

                    return imageReference
                            .getDownloadUrl();

                })
                .addOnSuccessListener(downloadUri -> {

                    savePost(
                            uid,
                            postId,
                            category,
                            caption,
                            downloadUri.toString()
                    );

                })
                .addOnFailureListener(error -> {

                    setUploading(false);

                    Toast.makeText(
                            this,
                            "Image upload failed: "
                                    + error.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void uploadCameraImage(
            StorageReference imageReference,
            Bitmap bitmap,
            String uid,
            String postId,
            String category,
            String caption
    ) {

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                90,
                outputStream
        );

        byte[] imageBytes =
                outputStream.toByteArray();

        imageReference
                .putBytes(imageBytes)
                .continueWithTask(task -> {

                    if (!task.isSuccessful()
                            && task.getException() != null) {

                        throw task.getException();
                    }

                    return imageReference
                            .getDownloadUrl();

                })
                .addOnSuccessListener(downloadUri -> {

                    savePost(
                            uid,
                            postId,
                            category,
                            caption,
                            downloadUri.toString()
                    );

                })
                .addOnFailureListener(error -> {

                    setUploading(false);

                    Toast.makeText(
                            this,
                            "Image upload failed: "
                                    + error.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void savePost(
            String uid,
            String postId,
            String category,
            String caption,
            String imageUrl
    ) {

        String authorName =
                auth.getCurrentUser().getDisplayName();

        if (authorName == null
                || authorName.trim().isEmpty()) {

            authorName = "Sanskriti Sathi User";
        }

        Map<String, Object> post =
                new HashMap<>();

        post.put("authorUid", uid);
        post.put("author", authorName);
        post.put("category", category);
        post.put("caption", caption);
        post.put("imageUrl", imageUrl);

        post.put("profileImageUrl", "");

        post.put("likeCount", 0L);
        post.put("comments", 0L);

        post.put("liked", false);
        post.put("saved", false);

        post.put("visibility", "public");

        post.put(
                "createdAt",
                com.google.firebase.firestore.FieldValue
                        .serverTimestamp()
        );

        firestore.collection("posts")
                .document(postId)
                .set(post)
                .addOnSuccessListener(unused -> {

                    setUploading(false);

                    Toast.makeText(
                            this,
                            "Post published successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(error -> {

                    setUploading(false);

                    Toast.makeText(
                            this,
                            "Post save failed: "
                                    + error.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void setUploading(boolean uploading) {

        progressBar.setVisibility(
                uploading
                        ? View.VISIBLE
                        : View.GONE
        );

        publishButton.setEnabled(!uploading);

        publishButton.setText(
                uploading
                        ? "Publishing..."
                        : "Publish Post"
        );
    }
}
