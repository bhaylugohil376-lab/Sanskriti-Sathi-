package com.sanskritisathi.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReelActivity extends AppCompatActivity {

    private RecyclerView reelRecyclerView;
    private ReelAdapter reelAdapter;
    private final List<Reel> reelList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reel);

        reelRecyclerView = findViewById(R.id.reelRecyclerView);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);

        layoutManager.setOrientation(
                LinearLayoutManager.VERTICAL
        );

        reelRecyclerView.setLayoutManager(layoutManager);
        reelRecyclerView.setHasFixedSize(false);

        reelAdapter = new ReelAdapter(
                this,
                reelList,
                new ReelAdapter.ReelActionListener() {

                    @Override
                    public void onDelete(Reel reel, int position) {
                        deleteReel(reel, position);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(
                                ReelActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        reelRecyclerView.setAdapter(reelAdapter);

        loadReels();
    }

    private void loadReels() {

        reelList.clear();
        reelAdapter.notifyDataSetChanged();

        ReelFirebaseHelper.getActiveReels(
                new ReelFirebaseHelper.ReelsCallback() {

                    @Override
                    public void onSuccess(List<Reel> reels) {

                        reelList.clear();
                        reelList.addAll(reels);

                        reelAdapter.notifyDataSetChanged();

                        if (reels.isEmpty()) {
                            Toast.makeText(
                                    ReelActivity.this,
                                    "Abhi koi Reel available nahi hai.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(
                                ReelActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void deleteReel(Reel reel, int position) {

        if (reel == null) {
            return;
        }

        ReelFirebaseHelper.deleteReel(
                reel.getId(),
                new ReelFirebaseHelper.ActionCallback() {

                    @Override
                    public void onSuccess() {

                        if (position >= 0 &&
                                position < reelList.size()) {

                            reelList.remove(position);
                            reelAdapter.notifyItemRemoved(position);
                        }

                        Toast.makeText(
                                ReelActivity.this,
                                "Reel deleted.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(String message) {

                        Toast.makeText(
                                ReelActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (reelAdapter != null) {
            reelAdapter.pauseAllVideos();
        }
    }

    @Override
    protected void onDestroy() {
        if (reelAdapter != null) {
            reelAdapter.releaseAllVideos();
        }

        super.onDestroy();
    }
}
