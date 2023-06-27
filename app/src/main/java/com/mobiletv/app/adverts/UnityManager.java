/*******************************
 * @VanderclinRocha            *
 * Data de criação: 22/06/2023 *
 *******************************/
package com.mobiletv.app.adverts;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.IUnityBannerListener;
import com.unity3d.services.banners.UnityBanners;

public class UnityManager {

    private final Context context;
    private final String gameId;
    private final String bannerPlacementId;
    private final String interstitialPlacementId;
    private final String rewardsPlacementId;
    private final FrameLayout frameBanner;

    private UnityInterstitialListener interstitialListener;
    private UnityRewardedListener rewardedListener;
    private boolean completedInterstitialLoaded;
    private boolean completedRewardedLoaded;

    public interface UnityInterstitialListener {
        void onInterstitialShowFailure();

        void onInterstitialShowComplete(UnityAds.UnityAdsShowCompletionState state);

        void onInterstitialFailedToLoaded();
    }

    public interface UnityRewardedListener {
        void onRewardedShowFailure();

        void onRewardedShowComplete(UnityAds.UnityAdsShowCompletionState state);

        void onRewardedFailedToLoaded();
    }

    public UnityManager(Context context, String gameId, String bannerPlacementId, String interstitialPlacementId, String rewardsPlacementId, FrameLayout frameBanner) {
        this.context = context;
        this.gameId = gameId;
        this.bannerPlacementId = bannerPlacementId;
        this.interstitialPlacementId = interstitialPlacementId;
        this.rewardsPlacementId = rewardsPlacementId;
        this.frameBanner = frameBanner;
    }

    public void setUnityInterstitialListener(UnityInterstitialListener listener) {
        interstitialListener = listener;
    }

    public void setUnityRewardedListener(UnityRewardedListener listener) {
        rewardedListener = listener;
    }

    public void initializeUnity() {
        UnityAds.initialize(context, gameId, false, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                // Unity Ads initialization complete
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                // Unity Ads initialization failed
            }
        });
    }

    public void initializeUnityBanner() {
        IUnityBannerListener bannerListener = new IUnityBannerListener() {
            @Override
            public void onUnityBannerLoaded(String placementId, View view) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {
                    parent.removeView(view);
                }
                frameBanner.addView(view);
            }

            @Override
            public void onUnityBannerUnloaded(String placementId) {
                // Unity banner unloaded
            }

            @Override
            public void onUnityBannerShow(String placementId) {
                // Unity banner shown
            }

            @Override
            public void onUnityBannerClick(String placementId) {
                // Unity banner clicked
            }

            @Override
            public void onUnityBannerHide(String placementId) {
                // Unity banner hidden
            }

            @Override
            public void onUnityBannerError(String message) {
                // Unity banner error
            }
        };
        UnityBanners.setBannerListener(bannerListener);
        UnityBanners.loadBanner((Activity) context, bannerPlacementId);
    }

    public void initializeUnityInterstitial() {
        UnityAds.load(interstitialPlacementId, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                completedInterstitialLoaded = true;
                if (interstitialListener != null) {
                    interstitialListener.onInterstitialFailedToLoaded();
                }
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                completedInterstitialLoaded = false;
                if (interstitialListener != null) {
                    interstitialListener.onInterstitialFailedToLoaded();
                }
            }
        });
    }

    public void initializeUnityRewards() {
        UnityAds.load(rewardsPlacementId, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                completedRewardedLoaded = true;
                if (rewardedListener != null) {
                    rewardedListener.onRewardedFailedToLoaded();
                }
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                completedRewardedLoaded = false;
                if (rewardedListener != null) {
                    rewardedListener.onRewardedFailedToLoaded();
                }
            }
        });
    }

    public void showInterstitialAd() {
        if (completedInterstitialLoaded) {
            UnityAds.show((Activity) context, interstitialPlacementId, new IUnityAdsShowListener() {
                @Override
                public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                    if (interstitialListener != null) {
                        interstitialListener.onInterstitialShowFailure();
                    }
                }

                @Override
                public void onUnityAdsShowStart(String placementId) {
                    // Interstitial ad show start
                }

                @Override
                public void onUnityAdsShowClick(String placementId) {
                    // Interstitial ad clicked
                }

                @Override
                public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                    if (interstitialListener != null) {
                        interstitialListener.onInterstitialShowComplete(state);
                    }
                }
            });
        } else {
            if (interstitialListener != null) {
                interstitialListener.onInterstitialFailedToLoaded();
            }
        }
    }

    public void showRewardedAd() {
        if (completedRewardedLoaded) {
            UnityAds.show((Activity) context, rewardsPlacementId, new IUnityAdsShowListener() {
                @Override
                public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                    if (rewardedListener != null) {
                        rewardedListener.onRewardedShowFailure();
                    }
                }

                @Override
                public void onUnityAdsShowStart(String placementId) {
                    // Reward ad show start
                }

                @Override
                public void onUnityAdsShowClick(String placementId) {
                    // Reward ad clicked
                }

                @Override
                public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                    if (rewardedListener != null) {
                        rewardedListener.onRewardedShowComplete(state);
                    }
                }
            });
        } else {
            if (rewardedListener != null) {
                rewardedListener.onRewardedFailedToLoaded();
            }
        }
    }

    public boolean isInterstitialLoaded() {
        return completedInterstitialLoaded;
    }

    public boolean isRewardedLoaded() {
        return completedRewardedLoaded;
    }
}