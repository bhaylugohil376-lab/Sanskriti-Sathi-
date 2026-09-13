package com.sanskritisathi.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GoogleAuthActivity extends AppCompatActivity {

    private static final String REDIRECT_URL =
            "sanskritisathi://auth/callback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startGoogleOAuth();
    }

    private void startGoogleOAuth() {

        try {

            String encodedRedirect =
                    URLEncoder.encode(
                            REDIRECT_URL,
                            "UTF-8"
                    );

            String authUrl =
                    SupabaseConfig.PROJECT_URL
                            + "/auth/v1/authorize"
                            + "?provider=google"
                            + "&redirect_to="
                            + encodedRedirect;

            Intent browserIntent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(authUrl)
                    );

            startActivity(browserIntent);

        } catch (UnsupportedEncodingException e) {

            Toast.makeText(
                    this,
                    "Google Sign-In configuration error.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
        }
    }
}
