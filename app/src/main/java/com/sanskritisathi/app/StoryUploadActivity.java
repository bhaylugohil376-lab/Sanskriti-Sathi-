package com.sanskritisathi.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;

public class StoryUploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private static final int CAMERA_REQUEST = 101;

    private ImageView selectedImage;
    private EditText captionInput;
    private RadioGroup visibilityGroup;
    private TextView selectedFileText;
    private Button uploadButton;

    private Uri selectedImageUri;

    private StoryFirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_story_upload);

        selectedImage = findViewById(R.id.selectedStoryImage);
        captionInput = findViewById(R.id.storyCaptionInput);
        visibilityGroup = findViewById(R.id.visibilityGroup);
        selectedFileText = findViewById(R.id.selectedFileText);

        Button galleryButton = findViewById(R.id.galleryButton);
        Button cameraButton = findViewById(R.id.cameraButton);
        uploadButton = findViewById(R.id.uploadStoryButton);

        firebaseHelper = new StoryFirebaseHelper();

        galleryButton.setOnClickListener(v -> openGallery());

        cameraButton.setOnClickListener(v -> openCamera());

        uploadButton.setOnClickListener(v -> uploadStory());
    }

    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
        );

        startActivityForResult(intent, PICK_IMAGE);
    }

    private void openCamera() {

        Intent intent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE
        );

        if (intent.resolveActivity(getPackageManager()) != null) {

            startActivityForResult(
                    intent,
                    CAMERA_REQUEST
            );

        } else {

            Toast.makeText(
                    this,
                    "Camera available nahi hai",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (resultCode != Activity.RESULT_OK ||
                data == null) {

            return;
        }

        if (requestCode == PICK_IMAGE) {

            Uri uri = data.getData();

            if (uri != null) {

                selectedImageUri = uri;

                selectedImage.setImageURI(
                        selectedImageUri
                );

                selectedFileText.setText(
                        "Gallery photo selected ✓"
                );
            }
        }

        else if (requestCode == CAMERA_REQUEST) {

            Bundle extras = data.getExtras();

            if (extras == null) {
                return;
            }

            Bitmap bitmap =
                    (Bitmap) extras.get("data");

            if (bitmap != null) {

                selectedImage.setImageBitmap(bitmap);

                selectedImageUri =
                        bitmapToUri(bitmap);

                selectedFileText.setText(
                        "Camera photo selected ✓"
                );
            }
        }
    }

    private Uri bitmapToUri(Bitmap bitmap) {

        String path =
                MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        bitmap,
                        "Sanskriti_Sathi_Story",
                        "Story image"
                );

        if (path == null) {
            return null;
        }

        return Uri.parse(path);
    }

    private void uploadStory() {

        if (selectedImageUri == null) {

            Toast.makeText(
                    this,
                    "Pehle photo select karein",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int selectedId =
                visibilityGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {

            Toast.makeText(
                    this,
                    "Public ya Followers select karein",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        RadioButton visibilityButton =
                findViewById(selectedId);

        String visibilityText =
                visibilityButton.getText().toString();

        String visibility;

        if (visibilityText.contains("Followers")) {
            visibility = "Followers";
        } else {
            visibility = "Public";
        }

        String caption =
                captionInput.getText()
                        .toString()
                        .trim();

        if (caption.isEmpty()) {
            caption = "Meri Sanskriti Story 🇮🇳";
        }

        setUploading(true);

        firebaseHelper.uploadStory(
                selectedImageUri,
                caption,
                visibility,
                new StoryFirebaseHelper.UploadCallback() {

                    @Override
                    public void onSuccess(String storyId) {

                        runOnUiThread(() -> {

                            setUploading(false);

                            Toast.makeText(
                                    StoryUploadActivity.this,
                                    "Story publish ho gayi ✓",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();
                        });
                    }

                    @Override
                    public void onError(String message) {

                        runOnUiThread(() -> {

                            setUploading(false);

                            Toast.makeText(
                                    StoryUploadActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    private void setUploading(boolean uploading) {

        uploadButton.setEnabled(!uploading);

        if (uploading) {

            uploadButton.setText(
                    "Uploading..."
            );

        } else {

            uploadButton.setText(
                    "➕ Publish Story"
            );
        }
    }
}
