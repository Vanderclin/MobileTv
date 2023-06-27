package com.mobiletv.app.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.mobiletv.app.R;
import com.mobiletv.app.adapter.AdapterCarousel;
import com.mobiletv.app.adverts.UnityManager;
import com.mobiletv.app.pojo.Carousel;
import com.mobiletv.app.widget.CarouselView;
import com.unity3d.ads.UnityAds;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentC extends Fragment implements UnityManager.UnityInterstitialListener, UnityManager.UnityRewardedListener {

    private FirebaseUser mUser;
    private DatabaseReference mData;
    private CarouselView carouselView;
    private int currentPage = 0;
    private UnityManager mUnityManager;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mData = FirebaseDatabase.getInstance().getReference();
        return inflater.inflate(R.layout.fragment_c, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(getString(R.string.earn_points));
        initializeCarousel();
        initializeUnity();
    }

    private void initializeCarousel() {
        carouselView = requireActivity().findViewById(R.id.carousel_view);
        ExtendedFloatingActionButton floatingActionButton = requireActivity().findViewById(R.id.earn_points);

        String[] titles = requireActivity().getResources().getStringArray(R.array.titles);
        String[] descriptions = requireActivity().getResources().getStringArray(R.array.descriptions);
        String[] images = requireActivity().getResources().getStringArray(R.array.images);
        List<Carousel> carouselList = new ArrayList<>();
        Carousel item1 = new Carousel("", descriptions[0], images[0], "", titles[0]);
        carouselList.add(item1);
        Carousel item2 = new Carousel("", descriptions[1], images[1], "", titles[1]);
        carouselList.add(item2);
        Carousel item3 = new Carousel("", descriptions[2], images[2], "", titles[2]);
        carouselList.add(item3);
        AdapterCarousel adapterCarousel = new AdapterCarousel(requireActivity(), carouselList);
        carouselView.setPageTransformerType(CarouselView.TRANSFORMER_ZOOM);
        carouselView.setAdapter(adapterCarousel);
        final Handler handler = new Handler();
        final Runnable Update = () -> {
            if (currentPage == adapterCarousel.getCount()) {
                currentPage = 0;
            }
            carouselView.setCurrentItem(currentPage++, true);
        };
        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 1000, 20000);

        floatingActionButton.setOnClickListener(v -> {
            if (mUnityManager.isRewardedLoaded()) {
                mUnityManager.showRewardedAd();
            } else {
                Toast.makeText(requireActivity(), getString(R.string.wait_a_moment), Toast.LENGTH_SHORT).show();
                mUnityManager.initializeUnityRewards();
            }
        });

    }

    private void updatePoints(int points) {
        if (mUser != null) {
            String uid = mUser.getUid();
            mData.child("users").child(uid).child("points").setValue(ServerValue.increment(points));
        }
    }

    private void initializeUnity() {
        mUnityManager = new UnityManager(requireActivity(), getString(R.string.unity_game_app), getString(R.string.unity_game_banner), getString(R.string.unity_game_interstitial), getString(R.string.unity_game_rewarded), requireActivity().findViewById(R.id.fragment_banner_navigation));
        mUnityManager.initializeUnity();
        if (isFragmentAttached()) {
            mUnityManager.initializeUnityBanner();
        }
        mUnityManager.initializeUnityInterstitial();
        mUnityManager.initializeUnityRewards();
        mUnityManager.setUnityInterstitialListener(this);
        mUnityManager.setUnityRewardedListener(this);
    }

    private boolean isFragmentAttached() {
        return isAdded() && getActivity() != null;
    }

    @Override
    public void onInterstitialShowFailure() {

    }

    @Override
    public void onInterstitialShowComplete(UnityAds.UnityAdsShowCompletionState state) {

    }

    @Override
    public void onInterstitialFailedToLoaded() {

    }

    @Override
    public void onRewardedShowFailure() {
        if (!mUnityManager.isRewardedLoaded()) {
            mUnityManager.initializeUnityRewards();
        }
    }

    @Override
    public void onRewardedShowComplete(UnityAds.UnityAdsShowCompletionState state) {
        mUnityManager.initializeUnityRewards();
        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
            updatePoints(10);
        } else if (state == UnityAds.UnityAdsShowCompletionState.SKIPPED) {
            updatePoints(5);
        }
    }

    @Override
    public void onRewardedFailedToLoaded() {
        if (!mUnityManager.isRewardedLoaded()) {
            mUnityManager.initializeUnityRewards();
        }
    }

}
