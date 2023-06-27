package com.mobiletv.app.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

public class FragmentA extends Fragment implements UnityManager.UnityInterstitialListener, UnityManager.UnityRewardedListener {

    private DatabaseReference mData;
    private CarouselView carouselView;
    private int currentPage = 0;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_a, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(getString(R.string.home));
        mData = FirebaseDatabase.getInstance().getReference();
        initializeCarousel();
        initializeUnity();
    }

    private void initializeCarousel() {
        carouselView = requireActivity().findViewById(R.id.carouselView);
        mData.child("carousel").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Carousel> carouselList = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    carouselList.add(snap.getValue(Carousel.class));
                }
                AdapterCarousel adapterCarousel = new AdapterCarousel(requireActivity(), carouselList);
                carouselView.setPageTransformerType(CarouselView.TRANSFORMER_OVERLAP);
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
                }, 1000, 9000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MainActivity", "Falha ao obter os dados do Firebase", error.toException());
            }
        });
    }

    private void initializeUnity() {
        UnityManager mUnityManager = new UnityManager(requireActivity(), getString(R.string.unity_game_app), getString(R.string.unity_game_banner), getString(R.string.unity_game_interstitial), getString(R.string.unity_game_rewarded), requireActivity().findViewById(R.id.fragment_banner_navigation));
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

    }

    @Override
    public void onRewardedShowComplete(UnityAds.UnityAdsShowCompletionState state) {

    }

    @Override
    public void onRewardedFailedToLoaded() {

    }

}
