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
import com.mobiletv.app.pojo.Account;
import com.mobiletv.app.pojo.Carousel;
import com.mobiletv.app.widget.CarouselView;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentC extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mData;
    private CarouselView carouselView;
    private int currentPage = 0;

    private String UNITY_GAME_ID = "5283279";
    private final String UNITY_BANNER_ID = "Banner_Android";
    private final String UNITY_INTERSTITIAL_ID = "Interstitial_Android";
    private final String UNITY_REWARDED_ID = "Rewarded_Android";
    private final Boolean UNITY_TEST_MODE = false;

    private ExtendedFloatingActionButton floatingActionButton;
    private boolean isPermission = true;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mData = FirebaseDatabase.getInstance().getReference();
        return inflater.inflate(R.layout.fragment_c, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().setTitle(getString(R.string.earn_points));
        initialize();
        initializeUnity();
    }

    private void initialize() {
        carouselView = requireActivity().findViewById(R.id.carousel_view);
        floatingActionButton = requireActivity().findViewById(R.id.earn_points);

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

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPermission) {
                    initializeUnityRewards();
                    isPermission = false;
                } else {
                    Toast.makeText(requireActivity(), getString(R.string.waita_moment), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    // ADS UNITY
    private void initializeUnity() {
        UnityAds.initialize(requireActivity(), UNITY_GAME_ID, UNITY_TEST_MODE, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                //
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                //
            }
        });
    }

    public void initializeUnityRewards() {
        UnityAds.load(UNITY_REWARDED_ID, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                UnityAds.show(requireActivity(), placementId, new IUnityAdsShowListener() {
                    @Override
                    public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                        isPermission = true;
                    }

                    @Override
                    public void onUnityAdsShowStart(String placementId) {
                        isPermission = false;
                    }

                    @Override
                    public void onUnityAdsShowClick(String placementId) {
                        isPermission = true;
                    }

                    @Override
                    public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                        isPermission = true;
                        if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                            updatePoints(10);
                        } else if (state == UnityAds.UnityAdsShowCompletionState.SKIPPED) {
                            updatePoints(5);
                        }
                    }
                });
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                isPermission = true;
            }
        });

    }

    private void updatePoints(int points) {
        if (mUser != null) {
            String uid = mUser.getUid();
            mData.child("users").child(uid).child("points").setValue(ServerValue.increment(points));
        }
    }

}
