package com.mobiletv.app.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javabot.player.PlayerController;
import com.javabot.player.PlayerVideo;
import com.mobiletv.app.adapter.AdapterEpisodes;
import com.mobiletv.app.R;
import com.mobiletv.app.pojo.EpisodeDetails;
import com.mobiletv.app.pojo.SeriesDetails;
import com.mobiletv.app.widget.Badge;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.IUnityBannerListener;
import com.unity3d.services.banners.UnityBanners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerActivity extends AppCompatActivity implements PlayerVideo.VideoViewCallback, AdapterEpisodes.OnEpisodeClickListener {

    private DatabaseReference mData;
    private PlayerVideo mPlayerVideo;
    private PlayerController mPlayerController;
    private View mPlayerLayout;
    private View mBottomLayout;
    private boolean isFullscreen;
    private AppCompatTextView playerTitle, playerDescription;
    private Badge playerViews;
    private RecyclerView playerEpisodes;
    private ViewGroup bannerContainer;
    private View viewBanner;
    private int cachedHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        String position = getIntent().getStringExtra("key");
        initializeFirebase(position);
        initializeUnity();


    }

    private void initializeFirebase(String position) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        mData = FirebaseDatabase.getInstance().getReference();
        if (mUser != null) {
            initializeViews();
            initializePlayer(position);
        } else {
            startActivity(new Intent(PlayerActivity.this, MainActivity.class));
            finish();
        }
    }

    private void initializeViews() {
        bannerContainer = findViewById(R.id.banner_view_player);
        mPlayerLayout = findViewById(R.id.video_layout);
        mPlayerVideo = findViewById(R.id.video_view);
        mPlayerController = findViewById(R.id.media_controller);
        mBottomLayout = findViewById(R.id.bottom_layout);
        playerTitle = findViewById(R.id.player_content_title);
        playerViews = findViewById(R.id.player_content_views);
        playerEpisodes = findViewById(R.id.player_content_episodes);
        playerDescription = findViewById(R.id.player_content_description);
        mPlayerVideo.setMediaController(mPlayerController);
        setVideoAreaSize();
        mPlayerVideo.setVideoViewCallback(this);
    }

    private void setVideoAreaSize() {
        mPlayerLayout.post(() -> {
            int width = mPlayerLayout.getWidth();
            /* TODO cachedHeight = (int) (width * 405f / 720f); */
            /* TODO cachedHeight = (int) (width * 3f / 4f); */
            cachedHeight = (int) (width * 9f / 16f);
            ViewGroup.LayoutParams videoLayoutParams = mPlayerLayout.getLayoutParams();
            videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            videoLayoutParams.height = cachedHeight;
            mPlayerLayout.setLayoutParams(videoLayoutParams);
        });
    }

    private void initializePlayer(String position) {
        if (position != null) {
            mData.child("series").child(position).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        SeriesDetails seriesDetails = snapshot.getValue(SeriesDetails.class);
                        if (seriesDetails != null) {
                            updatePlayerUI(seriesDetails);
                            setupEpisodeList(seriesDetails);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }

    private void updatePlayerUI(SeriesDetails seriesDetails) {
        playerTitle.setText(seriesDetails.getTitle());
        playerViews.setText(String.valueOf(seriesDetails.getViews()));
        playerDescription.setText(seriesDetails.getDescription());
    }

    private void setupEpisodeList(SeriesDetails seriesDetails) {
        playerEpisodes.setLayoutManager(new LinearLayoutManager(PlayerActivity.this, LinearLayoutManager.HORIZONTAL, false));
        List<EpisodeDetails> episodes = new ArrayList<>(seriesDetails.getEpisodes().values());
        Collections.sort(episodes, (episodeA, episodeB) -> episodeA.getTitle().compareTo(episodeB.getTitle()));
        AdapterEpisodes adapterEpisodes = new AdapterEpisodes(episodes, PlayerActivity.this);
        playerEpisodes.setAdapter(adapterEpisodes);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayerVideo != null && mPlayerVideo.isPlaying()) {
            mPlayerVideo.pause();
        }
    }

    @Override
    public void onScaleChange(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        View decorView = getWindow().getDecorView();
        if (isFullscreen) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
            ViewGroup.LayoutParams layoutParams = mPlayerLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mPlayerLayout.setLayoutParams(layoutParams);
            bannerContainer.setVisibility(View.GONE);
            mBottomLayout.setVisibility(View.GONE);
            onBanner();
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            ViewGroup.LayoutParams layoutParams = mPlayerLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = this.cachedHeight;
            mPlayerLayout.setLayoutParams(layoutParams);
            bannerContainer.setVisibility(View.VISIBLE);
            mBottomLayout.setVisibility(View.VISIBLE);
            onBanner();
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
            mPlayerVideo.setFullscreen(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onEpisodeClick(Uri address, String title, int pos) {
        if (address != null && title != null) {
            mPlayerController.setTitle(title);
            mPlayerVideo.setVideoURI(address);
            mPlayerVideo.requestFocus();
            mPlayerVideo.start();
        }
    }

    private void initializeUnity() {
        UnityAds.initialize(PlayerActivity.this, "5283279", false, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                initializeUnityBanner();
                Toast.makeText(PlayerActivity.this, "Inicialização completa", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                // Handle initialization failure
                Toast.makeText(PlayerActivity.this, "Falha na inicialização", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeUnityBanner() {
        IUnityBannerListener bannerListener = new IUnityBannerListener() {
            public void onUnityBannerLoaded(String s, View view) {
                ViewGroup parent = (ViewGroup) view.getParent();
                viewBanner = view;
                if (parent != null) {
                    parent.removeView(view);
                }
                bannerContainer.addView(view);
            }

            @Override
            public void onUnityBannerUnloaded(String s) {
                // Handle banner unload event
            }

            @Override
            public void onUnityBannerShow(String s) {
                // Handle banner show event
            }

            @Override
            public void onUnityBannerClick(String s) {
                // Handle banner click event
            }

            @Override
            public void onUnityBannerHide(String s) {
                // Handle banner hide event
            }

            @Override
            public void onUnityBannerError(String s) {
                // Handle banner error event
            }
        };
        UnityBanners.setBannerListener(bannerListener);
        UnityBanners.loadBanner(PlayerActivity.this, "Banner_Android_Player");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UnityBanners.destroy();
        UnityBanners.setBannerListener(null);
    }

    private void onBanner() {
        if (isFullscreen && viewBanner != null) {
            mPlayerController.setBanner(viewBanner);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFullscreen) {
            bannerContainer.setVisibility(View.GONE);
        } else {
            bannerContainer.setVisibility(View.VISIBLE);
        }
    }

}
