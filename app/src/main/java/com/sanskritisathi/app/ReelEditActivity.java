package com.sanskritisathi.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ReelEditActivity extends AppCompatActivity {

    private EditText captionInput;
    private RadioButton publicRadio;
    private RadioButton followersRadio;
    private Button saveButton;
    private ImageButton backButton;
    private ProgressBar editProgress;

    private String reelId = "";
    private boolean saving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_reel_edit
        );

        reelId =
                getIntent().getStringExtra(
                        "reel_id"
                );

        String caption =
                getIntent().getStringExtra(
                        "caption"
                );

        String visibility =
                getIntent().getStringExtra(
                        "visibility"
                );

        if (TextUtils.isEmpty(reelId)) {

            Toast.makeText(
                    this,
                    "Reel ID missing",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        bindViews();

        captionInput.setText(
                caption == null ? "" : caption
        );

        if ("Followers".equalsIgnoreCase(
                visibility
        )) {

            followersRadio.setChecked(true);

        } else {

            publicRadio.setChecked(true);
        }

        backButton.setOnClickListener(
                v -> finish()
        );

        saveButton.setOnClickListener(
                v -> saveChanges()
        );
    }

    private void bindViews() {

        captionInput =
                findViewById(
                        R.id.captionInput
                );

        publicRadio =
                findViewById(
                        R.id.publicRadio
                );

        followersRadio =
                findViewById(
                        R.id.followersRadio
                );

        saveButton =
                findViewById(
                        R.id.saveButton
                );

        backButton =
                findViewById(
                        R.id.backButton
                );

        editProgress =
                findViewById(
                        R.id.editProgress
                );
    }

    private void saveChanges() {

        if (saving) return;

        String caption =
                captionInput
                        .getText()
                        .toString()
                        .trim();

        String visibility =
                publicRadio.isChecked()
                        ? "Public"
                        : "Followers";

        saving = true;

        saveButton.setEnabled(false);
        editProgress.setVisibility(
                ProgressBar.VISIBLE
        );

        ReelSupabaseHelper.updateReel(
                this,
                reelId,
                caption,
                visibility,
                new ReelSupabaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        runOnUiThread(() -> {

                            saving = false;

                            saveButton.setEnabled(
                                    true
                            );

                            editProgress.setVisibility(
                                    ProgressBar.GONE
                            );

                            Toast.makeText(
                                    ReelEditActivity.this,
                                    "Reel updated",
                                    Toast.LENGTH_SHORT
                            ).show();

                            setResult(
                                    RESULT_OK
                            );

                            finish();
                        });
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        runOnUiThread(() -> {

                            saving = false;

                            saveButton.setEnabled(
                                    true
                            );

                            editProgress.setVisibility(
                                    ProgressBar.GONE
                            );

                            Toast.makeText(
                                    ReelEditActivity.this,
                                    error == null
                                            ? "Update failed"
                                            : error,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }
}
