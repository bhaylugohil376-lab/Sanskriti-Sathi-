package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** Settings screen using Supabase authentication. */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView backButton = findViewById(R.id.backButton);
        TextView logoutButton = findViewById(R.id.logoutButton);

        if (backButton != null) backButton.setOnClickListener(v -> finish());
        if (logoutButton != null) logoutButton.setOnClickListener(v -> logout());

        setRowClick(R.id.accountRow, "Account");
        setRowClick(R.id.notificationsRow, "Notifications");
        setRowClick(R.id.privacyRow, "Privacy & Security");
        setRowClick(R.id.savedRow, "Saved");
        setRowClick(R.id.archiveRow, "Archive");
        setRowClick(R.id.helpRow, "Help");
    }

    private void setRowClick(int id, String message) {
        LinearLayout row = findViewById(id);
        if (row != null) {
            row.setOnClickListener(v -> Toast.makeText(
                    this, message + " — jaldi available hoga", Toast.LENGTH_SHORT).show());
        }
    }

    private void logout() {
        SupabaseAuthManager.logout(this, new SupabaseAuthManager.AuthCallback() {
            @Override public void onSuccess(String accessToken, String refreshToken, String userId, String userEmail) {
                openLogin();
            }
            @Override public void onError(String message) {
                SupabaseAuthManager.clearSession(SettingsActivity.this);
                openLogin();
            }
        });
    }

    private void openLogin() {
        Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
