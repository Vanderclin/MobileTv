package com.mobiletv.app.activity;

import android.annotation.SuppressLint;
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
import com.mobiletv.app.adapter.AdapterEpisodes;
import com.mobiletv.app.R;
import com.mobiletv.app.player.MediaController;
import com.mobiletv.app.player.VideoView;
import com.mobiletv.app.pojo.EpisodeDetails;
import com.mobiletv.app.pojo.SeriesDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AlternativePlayer extends AppCompatActivity implements VideoView.VideoViewCallback, AdapterEpisodes.OnEpisodeClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mData;
    private View mVideoLayout;
    private VideoView mVideoView;
    private MediaController mMediaController;
    private View mBottomLayout;
    private int cachedHeight;
    private boolean isFullscreen;
    private AppCompatTextView playerTitle, playerDescription;
    private RecyclerView playerEpisodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alternative_player);
        String position = getIntent().getExtras().getString("position");
        initializeFirebase(position);
    }

    private void initializeFirebase(String position) {
        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance().getReference();
        if (mAuth != null) {
            initializeFindView();
            initializePlayer(position);
        }
    }

    private void initializeFindView() {
        mVideoLayout = findViewById(R.id.video_layout);
        mVideoView = findViewById(R.id.videoView);
        mMediaController = findViewById(R.id.media_controller);
        mBottomLayout = findViewById(R.id.bottom_layout);
        playerTitle = findViewById(R.id.player_content_title);
        playerEpisodes = findViewById(R.id.player_content_episodes);
        playerDescription = findViewById(R.id.player_content_description);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setVideoViewCallback(this);
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
                            playerDescription.setText(seriesDetails.getDescription());
                            playerEpisodes.setLayoutManager(new LinearLayoutManager(AlternativePlayer.this, LinearLayoutManager.HORIZONTAL, false));
                            Map<String, EpisodeDetails> episodesMap = seriesDetails.getEpisodes();
                            List<EpisodeDetails> episodes = new ArrayList<>(episodesMap.values());
                            Collections.sort(episodes, (episodeA, episodeB) -> episodeA.getTitle().compareTo(episodeB.getTitle()));
                            AdapterEpisodes adapterEpisodes = new AdapterEpisodes(episodes, AlternativePlayer.this);
                            playerEpisodes.setAdapter(adapterEpisodes);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Error Here
                }
            });
        } else {
            startActivity(new Intent(AlternativePlayer.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    @Override
    @SuppressLint("SourceLockedOrientationActivity")
    public void onScaleChange(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        View decorView = getWindow().getDecorView();
        if (isFullscreen) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoLayout.setLayoutParams(layoutParams);
            mBottomLayout.setVisibility(View.GONE);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = this.cachedHeight;
            mVideoLayout.setLayoutParams(layoutParams);
            mBottomLayout.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onPause(MediaPlayer mediaPlayer) {

    }


    @Override
    public void onStart(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onBufferingStart(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onBufferingEnd(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onBackPressed() {
        if (this.isFullscreen) {
            mVideoView.setFullscreen(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onEpisodeClick(Uri address, String title, int pos) {
        if (address != null && title != null) {
            mMediaController.setTitle(title);
            int width = mVideoLayout.getWidth();
            cachedHeight = (int) (width * 410f / 720f);
            ViewGroup.LayoutParams videoLayoutParams = mVideoLayout.getLayoutParams();
            videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            videoLayoutParams.height = cachedHeight;
            mVideoLayout.setLayoutParams(videoLayoutParams);
            mVideoView.setVideoURI(address);
            mVideoView.requestFocus();
            mVideoView.start();
        }
    }

}
