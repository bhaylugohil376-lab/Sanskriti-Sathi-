package com.sanskritisathi.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private EditText phoneInput;
    private EditText otpInput;

    private Button loginButton;
    private Button registerButton;
    private Button sendOtpButton;
    private Button verifyOtpButton;

    private TextView forgotPasswordText;

    private FirebaseAuth firebaseAuth;

    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        phoneInput = findViewById(R.id.phoneInput);
        otpInput = findViewById(R.id.otpInput);

        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        loginButton.setOnClickListener(v -> loginUser());

        registerButton.setOnClickListener(v -> registerUser());

        forgotPasswordText.setOnClickListener(v -> resetPassword());

        sendOtpButton.setOnClickListener(v -> sendOtp());

        verifyOtpButton.setOnClickListener(v -> verifyOtp());
    }

    // --------------------------------------------------
    // EMAIL LOGIN
    // --------------------------------------------------

    private void loginUser() {

        String email =
                emailInput.getText()
                        .toString()
                        .trim();

        String password =
                passwordInput.getText()
                        .toString()
                        .trim();

        if (!validateInput(email, password)) {
            return;
        }

        loginButton.setEnabled(false);

        firebaseAuth
                .signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(this, task -> {

                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Login successful",
                                Toast.LENGTH_SHORT
                        ).show();

                        openMainActivity();

                    } else {

                        String errorMessage =
                                "Login failed";

                        if (task.getException() != null &&
                                task.getException().getMessage() != null) {

                            errorMessage =
                                    task.getException()
                                            .getMessage();
                        }

                        Toast.makeText(
                                LoginActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // --------------------------------------------------
    // EMAIL REGISTER
    // --------------------------------------------------

    private void registerUser() {

        String email =
                emailInput.getText()
                        .toString()
                        .trim();

        String password =
                passwordInput.getText()
                        .toString()
                        .trim();

        if (!validateInput(email, password)) {
            return;
        }

        registerButton.setEnabled(false);

        firebaseAuth
                .createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(this, task -> {

                    registerButton.setEnabled(true);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Account created successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        openMainActivity();

                    } else {

                        String errorMessage =
                                "Registration failed";

                        if (task.getException() != null &&
                                task.getException().getMessage() != null) {

                            errorMessage =
                                    task.getException()
                                            .getMessage();
                        }

                        Toast.makeText(
                                LoginActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // --------------------------------------------------
    // FORGOT PASSWORD
    // --------------------------------------------------

    private void resetPassword() {

        String email =
                emailInput.getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(email)) {

            emailInput.setError(
                    "Email डालें"
            );

            emailInput.requestFocus();

            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            emailInput.setError(
                    "Valid email डालें"
            );

            emailInput.requestFocus();

            return;
        }

        firebaseAuth
                .sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Password reset email भेज दिया गया",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        Toast.makeText(
                                LoginActivity.this,
                                "Password reset failed",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // --------------------------------------------------
    // SEND PHONE OTP
    // --------------------------------------------------

    private void sendOtp() {

        String phone =
                phoneInput.getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(phone)) {

            phoneInput.setError(
                    "Phone number डालें"
            );

            phoneInput.requestFocus();

            return;
        }

        if (!phone.startsWith("+")) {

            phoneInput.setError(
                    "Country code के साथ नंबर डालें, जैसे +91XXXXXXXXXX"
            );

            phoneInput.requestFocus();

            return;
        }

        if (phone.length() < 10) {

            phoneInput.setError(
                    "Valid phone number डालें"
            );

            phoneInput.requestFocus();

            return;
        }

        sendOtpButton.setEnabled(false);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(
                                60L,
                                TimeUnit.SECONDS
                        )
                        .setActivity(this)
                        .setCallbacks(
                                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                    @Override
                                    public void onVerificationCompleted(
                                            @NonNull PhoneAuthCredential credential) {

                                        signInWithPhoneCredential(
                                                credential
                                        );
                                    }

                                    @Override
                                    public void onVerificationFailed(
                                            @NonNull FirebaseException e) {

                                        sendOtpButton.setEnabled(true);

                                        Toast.makeText(
                                                LoginActivity.this,
                                                "OTP भेजना failed: "
                                                        + e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }

                                    @Override
                                    public void onCodeSent(
                                            @NonNull String id,
                                            @NonNull PhoneAuthProvider.ForceResendingToken token) {

                                        verificationId = id;
                                        resendToken = token;

                                        sendOtpButton.setEnabled(true);

                                        otpInput.requestFocus();

                                        Toast.makeText(
                                                LoginActivity.this,
                                                "OTP भेज दिया गया",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                        )
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(
                options
        );
    }

    // --------------------------------------------------
    // VERIFY OTP
    // --------------------------------------------------

    private void verifyOtp() {

        String otp =
                otpInput.getText()
                        .toString()
                        .trim();

        if (verificationId == null ||
                verificationId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "पहले OTP भेजें",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (otp.length() != 6) {

            otpInput.setError(
                    "6 digit OTP डालें"
            );

            otpInput.requestFocus();

            return;
        }

        verifyOtpButton.setEnabled(false);

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(
                        verificationId,
                        otp
                );

        signInWithPhoneCredential(
                credential
        );
    }

    // --------------------------------------------------
    // PHONE LOGIN
    // --------------------------------------------------

    private void signInWithPhoneCredential(
            PhoneAuthCredential credential) {

        firebaseAuth
                .signInWithCredential(credential)
                .addOnCompleteListener(
                        this,
                        task -> {

                            verifyOtpButton.setEnabled(true);

                            if (task.isSuccessful()) {

                                FirebaseUser user =
                                        firebaseAuth.getCurrentUser();

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Phone login successful",
                                        Toast.LENGTH_SHORT
                                ).show();

                                if (user != null) {
                                    openMainActivity();
                                }

                            } else {

                                String errorMessage =
                                        "OTP verification failed";

                                if (task.getException() != null &&
                                        task.getException().getMessage() != null) {

                                    errorMessage =
                                            task.getException()
                                                    .getMessage();
                                }

                                Toast.makeText(
                                        LoginActivity.this,
                                        errorMessage,
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    // --------------------------------------------------
    // VALIDATE EMAIL
    // --------------------------------------------------

    private boolean validateInput(
            String email,
            String password) {

        if (TextUtils.isEmpty(email)) {

            emailInput.setError(
                    "Email डालें"
            );

            emailInput.requestFocus();

            return false;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            emailInput.setError(
                    "Valid email डालें"
            );

            emailInput.requestFocus();

            return false;
        }

        if (TextUtils.isEmpty(password)) {

            passwordInput.setError(
                    "Password डालें"
            );

            passwordInput.requestFocus();

            return false;
        }

        if (password.length() < 6) {

            passwordInput.setError(
                    "Password कम से कम 6 characters का होना चाहिए"
            );

            passwordInput.requestFocus();

            return false;
        }

        return true;
    }

    // --------------------------------------------------
    // OPEN MAIN
    // --------------------------------------------------

    private void openMainActivity() {

        Intent intent =
                new Intent(
                        LoginActivity.this,
                        MainActivity.class
                );

        startActivity(intent);

        finish();
    }
}
