package com.mobiletv.app.player;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobiletv.app.activity.MainActivity;
import com.mobiletv.app.adapter.AdapterEpisodes;
import com.mobiletv.app.R;
import com.mobiletv.app.pojo.EpisodeDetails;
import com.mobiletv.app.pojo.SeriesDetails;
import com.mobiletv.app.widget.Badge;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdvancedPlayer extends AppCompatActivity implements AdvancedVideo.VideoViewCallback, AdapterEpisodes.OnEpisodeClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mData;
    private View mVideoLayout;
    private AdvancedVideo mAdvancedVideo;
    private AdvancedController mAdvancedController;
    private View mBottomLayout;
    private int cachedHeight;
    private int originalHeight;
    private boolean isFullscreen;
    private AppCompatTextView playerTitle, playerDescription;
    private Badge playerViews;
    private RecyclerView playerEpisodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alternative_player_activity);
        String position = getIntent().getStringExtra("key");
        initializeFirebase(position);
        initializeUnity();

    }

    private void initializeFirebase(String position) {
        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance().getReference();
        if (mAuth != null) {
            initializeViews();
            initializePlayer(position);
        }
    }

    private void initializeViews() {
        mVideoLayout = findViewById(R.id.video_layout);
        mAdvancedVideo = findViewById(R.id.videoView);
        mAdvancedController = findViewById(R.id.media_controller);
        mBottomLayout = findViewById(R.id.bottom_layout);
        playerTitle = findViewById(R.id.player_content_title);
        playerViews = findViewById(R.id.player_content_views);
        playerEpisodes = findViewById(R.id.player_content_episodes);
        playerDescription = findViewById(R.id.player_content_description);
        mAdvancedVideo.setMediaController(mAdvancedController);
        mAdvancedVideo.setVideoViewCallback(this);
    }

    private void initializePlayer(String position) {
        if (position != null) {
            mData.child("series").child(position).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        SeriesDetails seriesDetails = snapshot.getValue(SeriesDetails.class);
                        if (seriesDetails != null) {
                            playerTitle.setText(seriesDetails.getTitle());
                            playerViews.setText(String.valueOf(seriesDetails.getViews()));
                            playerDescription.setText(seriesDetails.getDescription());
                            playerEpisodes.setLayoutManager(new LinearLayoutManager(AdvancedPlayer.this, LinearLayoutManager.HORIZONTAL, false));
                            List<EpisodeDetails> episodes = new ArrayList<>(seriesDetails.getEpisodes().values());
                            Collections.sort(episodes, (episodeA, episodeB) -> episodeA.getTitle().compareTo(episodeB.getTitle()));
                            AdapterEpisodes adapterEpisodes = new AdapterEpisodes(episodes, AdvancedPlayer.this);
                            playerEpisodes.setAdapter(adapterEpisodes);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        } else {
            startActivity(new Intent(AdvancedPlayer.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdvancedVideo != null && mAdvancedVideo.isPlaying()) {
            mAdvancedVideo.pause();
        }
    }

    @Override
    public void onScaleChange(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        View decorView = getWindow().getDecorView();
        if (isFullscreen) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mBottomLayout.setVisibility(View.GONE);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = originalHeight;
            mVideoLayout.setLayoutParams(layoutParams);
            mBottomLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause(MediaPlayer mediaPlayer) {
        // Handle onPause event
    }

    @Override
    public void onStart(MediaPlayer mediaPlayer) {
        // Handle onStart event
    }

    @Override
    public void onBufferingStart(MediaPlayer mediaPlayer) {
        // Handle onBufferingStart event
    }

    @Override
    public void onBufferingEnd(MediaPlayer mediaPlayer) {
        // Handle onBufferingEnd event
    }

    @Override
    public void onBackPressed() {
        if (this.isFullscreen) {
            mAdvancedVideo.setFullscreen(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onEpisodeClick(Uri address, String title, int pos) {
        if (address != null && title != null) {
            mAdvancedController.setTitle(title);
            int width = mVideoLayout.getWidth();
            cachedHeight = (int) (width * 410f / 720f);
            ViewGroup.LayoutParams videoLayoutParams = mVideoLayout.getLayoutParams();
            videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            videoLayoutParams.height = cachedHeight;
            originalHeight = cachedHeight;
            mVideoLayout.setLayoutParams(videoLayoutParams);
            mAdvancedVideo.setVideoURI(address);
            mAdvancedVideo.requestFocus();
            mAdvancedVideo.start();
        }
    }

    private void initializeUnity() {
        UnityAds.initialize(AdvancedPlayer.this, "5283279", false, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                // Unity Initialization Completed
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                // Unity Initialization Failed
            }
        });
    }
}