package com.sanskritisathi.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;

public class PostUploadActivity extends AppCompatActivity {

    private ImageView postPreviewImage;
    private Button selectPostImageButton;
    private Button uploadPostButton;
    private EditText postCaptionInput;
    private Spinner postCategorySpinner;
    private Spinner postVisibilitySpinner;
    private ProgressBar postUploadProgress;

    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private Uri selectedImageUri;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_upload);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        initializeViews();
        setupSpinners();
        setupImagePicker();
        setupButtons();
    }

    private void initializeViews() {

        postPreviewImage =
                findViewById(R.id.postPreviewImage);

        selectPostImageButton =
                findViewById(R.id.selectPostImageButton);

        uploadPostButton =
                findViewById(R.id.uploadPostButton);

        postCaptionInput =
                findViewById(R.id.postCaptionInput);

        postCategorySpinner =
                findViewById(R.id.postCategorySpinner);

        postVisibilitySpinner =
                findViewById(R.id.postVisibilitySpinner);

        postUploadProgress =
                findViewById(R.id.postUploadProgress);
    }

    private void setupSpinners() {

        List<String> categories = Arrays.asList(
                "Raja",
                "Temple",
                "Devi Devta",
                "Bhagavad Gita",
                "Indian Culture",
                "History"
        );

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories
                );

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        postCategorySpinner.setAdapter(
                categoryAdapter
        );

        List<String> visibilityOptions = Arrays.asList(
                "Public",
                "Followers"
        );

        ArrayAdapter<String> visibilityAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        visibilityOptions
                );

        visibilityAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        postVisibilitySpinner.setAdapter(
                visibilityAdapter
        );
    }

    private void setupImagePicker() {

        imagePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {

                            if (uri != null) {

                                selectedImageUri = uri;

                                postPreviewImage.setImageURI(
                                        selectedImageUri
                                );
                            }
                        }
                );
    }

    private void setupButtons() {

        selectPostImageButton.setOnClickListener(
                view -> openImagePicker()
        );

        uploadPostButton.setOnClickListener(
                view -> publishPost()
        );
    }

    private void openImagePicker() {

        imagePickerLauncher.launch(
                "image/*"
        );
    }

    private void publishPost() {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {

            Toast.makeText(
                    this,
                    "Pehle Login karein.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String caption =
                postCaptionInput
                        .getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(caption)) {

            postCaptionInput.setError(
                    "Caption likhiye"
            );

            postCaptionInput.requestFocus();

            return;
        }

        if (caption.length() > 2000) {

            postCaptionInput.setError(
                    "Caption maximum 2000 characters ka ho sakta hai."
            );

            return;
        }

        String category =
                postCategorySpinner
                        .getSelectedItem()
                        .toString();

        String visibility =
                postVisibilitySpinner
                        .getSelectedItem()
                        .toString();

        setUploading(true);

        /*
         * Photo optional rakhi gayi hai.
         * Agar photo select nahi hai to Firestore
         * mein empty imageUrl save hoga.
         */
        if (selectedImageUri == null) {

            createFirestorePost(
                    category,
                    caption,
                    "",
                    visibility
            );

            return;
        }

        uploadImageToFirebase(
                category,
                caption,
                visibility
        );
    }

    private void uploadImageToFirebase(
            String category,
            String caption,
            String visibility) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {

            setUploading(false);

            Toast.makeText(
                    this,
                    "Login session nahi mila.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String fileName =
                "post_" +
                System.currentTimeMillis() +
                ".jpg";

        StorageReference imageReference =
                storage
                        .getReference()
                        .child("culture_posts")
                        .child(user.getUid())
                        .child(fileName);

        imageReference
                .putFile(selectedImageUri)
                .addOnProgressListener(
                        taskSnapshot -> {

                            long total =
                                    taskSnapshot.getTotalByteCount();

                            long uploaded =
                                    taskSnapshot.getBytesTransferred();

                            if (total > 0) {

                                int progress =
                                        (int)
                                                ((uploaded * 100)
                                                        / total);

                                postUploadProgress
                                        .setProgress(progress);
                            }
                        }
                )
                .continueWithTask(
                        task -> {

                            if (!task.isSuccessful()
                                    && task.getException() != null) {

                                throw task.getException();
                            }

                            return imageReference
                                    .getDownloadUrl();
                        }
                )
                .addOnSuccessListener(
                        downloadUri -> {

                            String imageUrl =
                                    downloadUri.toString();

                            createFirestorePost(
                                    category,
                                    caption,
                                    imageUrl,
                                    visibility
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            setUploading(false);

                            Toast.makeText(
                                    PostUploadActivity.this,
                                    "Photo upload failed: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    private void createFirestorePost(
            String category,
            String caption,
            String imageUrl,
            String visibility) {

        CulturePostFirebaseHelper.createPost(
                category,
                caption,
                imageUrl,
                visibility,
                new CulturePostFirebaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        setUploading(false);

                        Toast.makeText(
                                PostUploadActivity.this,
                                "Post successfully publish ho gayi.",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();
                    }

                    @Override
                    public void onError(
                            String message) {

                        setUploading(false);

                        Toast.makeText(
                                PostUploadActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void setUploading(
            boolean uploading) {

        if (uploading) {

            postUploadProgress.setVisibility(
                    View.VISIBLE
            );

            postUploadProgress.setIndeterminate(
                    selectedImageUri == null
            );

            selectPostImageButton.setEnabled(
                    false
            );

            uploadPostButton.setEnabled(
                    false
            );

        } else {

            postUploadProgress.setVisibility(
                    View.GONE
            );

            postUploadProgress.setIndeterminate(
                    false
            );

            selectPostImageButton.setEnabled(
                    true
            );

            uploadPostButton.setEnabled(
                    true
            );
        }
    }
}
