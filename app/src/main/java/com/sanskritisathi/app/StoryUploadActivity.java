package com.sanskritisathi.app;

import android.app.Activity;
import android.content.Intent;
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

public class StoryUploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private static final int CAMERA_REQUEST = 101;

    private ImageView selectedImage;
    private EditText captionInput;
    private RadioGroup visibilityGroup;
    private TextView selectedFileText;

    private Uri selectedImageUri;

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
        Button uploadButton = findViewById(R.id.uploadStoryButton);

        galleryButton.setOnClickListener(v -> openGallery());

        cameraButton.setOnClickListener(v -> openCamera());

        uploadButton.setOnClickListener(v -> createStory());
    }

    private void openGallery() {

        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );

        startActivityForResult(intent, PICK_IMAGE);
    }

    private void openCamera() {

        Intent intent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE
        );

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST);
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

        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }

        if (requestCode == PICK_IMAGE) {

            selectedImageUri = data.getData();

            if (selectedImageUri != null) {

                selectedImage.setImageURI(
                        selectedImageUri
                );

                selectedFileText.setText(
                        "Photo selected ✓"
                );
            }
        }

        if (requestCode == CAMERA_REQUEST) {

            selectedImage.setImageBitmap(
                    (android.graphics.Bitmap)
                            data.getExtras().get("data")
            );

            selectedFileText.setText(
                    "Camera photo selected ✓"
            );
        }
    }

    private void createStory() {

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

        RadioButton selectedVisibility =
                findViewById(selectedId);

        String visibility =
                selectedVisibility.getText().toString();

        String caption =
                captionInput.getText().toString().trim();

        if (caption.isEmpty()) {
            caption = "Meri Sanskriti Story 🇮🇳";
        }

        /*
         * Local Story creation.
         *
         * Firebase integration mein isi jagah:
         * 1. Image Storage mein upload hogi
         * 2. Story document Firestore mein banega
         * 3. Owner UID save hoga
         * 4. Visibility save hogi
         * 5. createdAt save hoga
         * 6. 24-hour expiry server-side handle hogi
         */

        Story newStory = new Story(
                "story_" + System.currentTimeMillis(),
                "Sanskriti Sathi",
                "icon_foreground",
                "icon_foreground",
                caption,
                visibility,
                System.currentTimeMillis(),
                0,
                true
        );

        StoryData.addStory(newStory);

        Toast.makeText(
                this,
                "Story create ho gayi ✓",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }
}
