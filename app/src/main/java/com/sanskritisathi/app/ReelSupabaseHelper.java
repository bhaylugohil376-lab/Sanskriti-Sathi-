package com.sanskritisathi.app;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReelSupabaseHelper {

    private static final String TAG = "ReelSupabaseHelper";

    // FIXME: Set your project SUPABASE_URL and SUPABASE_ANON_KEY
    private static final String SUPABASE_URL = "https://YOUR_SUPABASE_PROJECT_ID.supabase.co";
    private static final String SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY";

    private final OkHttpClient client;
    private final Gson gson;

    public interface UploadCallback {
        void onSuccess(String videoUrl);
        void onError(Exception e);
    }

    public interface GetReelsCallback {
        void onSuccess(List<Reel> reels);
        void onError(Exception e);
    }

    public ReelSupabaseHelper() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public void uploadReelVideo(Context context, Uri videoUri, UploadCallback callback) {
        new Thread(() -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(videoUri);
                if (inputStream == null) {
                    runOnMain(() -> callback.onError(new Exception("Unable to open video stream")));
                    return;
                }

                byte[] bytes = new byte[inputStream.available()];
                int bytesRead = inputStream.read(bytes);
                inputStream.close();

                if (bytesRead <= 0) {
                    runOnMain(() -> callback.onError(new Exception("Selected video is empty")));
                    return;
                }

                String fileName = "reel_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + ".mp4";
                String uploadUrl = SUPABASE_URL + "/storage/v1/object/reels/" + fileName;

                RequestBody body = RequestBody.create(bytes, MediaType.parse("video/mp4"));
                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                        .addHeader("Content-Type", "video/mp4")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull java.io.IOException e) {
                        Log.e(TAG, "Upload failed: " + e.getMessage());
                        runOnMain(() -> callback.onError(e));
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        if (response.isSuccessful()) {
                            String publicUrl = SUPABASE_URL + "/storage/v1/object/public/reels/" + fileName;
                            Log.d(TAG, "Generated public URL: " + publicUrl);
                            runOnMain(() -> callback.onSuccess(publicUrl));
                        } else {
                            Log.e(TAG, "Upload failure HTTP Code: " + response.code());
                            runOnMain(() -> callback.onError(new Exception("Upload failed with HTTP " + response.code())));
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Upload Exception: " + e.getMessage(), e);
                runOnMain(() -> callback.onError(e));
            }
        }).start();
    }

    public void saveReelToDatabase(Reel reel, UploadCallback callback) {
        String dbUrl = SUPABASE_URL + "/rest/v1/reels";
        String jsonBody = gson.toJson(reel);

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(dbUrl)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull java.io.IOException e) {
                runOnMain(() -> callback.onError(e));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    runOnMain(() -> callback.onSuccess(reel.getVideoUrl()));
                } else {
                    runOnMain(() -> callback.onError(new Exception("Database Save Error: " + response.code())));
                }
            }
        });
    }

    public void getActiveReels(GetReelsCallback callback) {
        String fetchUrl = SUPABASE_URL + "/rest/v1/reels?select=*&order=created_at.desc";

        Request request = new Request.Builder()
                .url(fetchUrl)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull java.io.IOException e) {
                runOnMain(() -> callback.onError(e));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Type listType = new TypeToken<ArrayList<Reel>>() {}.getType();
                    List<Reel> reels = gson.fromJson(responseData, listType);
                    runOnMain(() -> callback.onSuccess(reels));
                } else {
                    runOnMain(() -> callback.onError(new Exception("Fetch Error: " + response.code())));
                }
            }
        });
    }

    private void runOnMain(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
