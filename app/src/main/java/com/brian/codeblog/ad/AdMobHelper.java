package com.brian.codeblog.ad;

import android.app.Activity;
import android.content.Context;

import com.brian.common.utils.LogUtil;
import com.brian.csdnblog.R;
import com.google.android.gms.ads.AdActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.lang.reflect.Field;
import java.util.Map;

import static com.brian.common.utils.ResourceUtil.getString;

/**
 * Created by huamm on 2017/4/17 0017.
 */

public class AdMobHelper {

    public static void init(Context context) {
        MobileAds.initialize(context, "ca-app-pub-7153470135701214~7792214484");
        initInterstitialAd(context);
    }

    private static InterstitialAd mInterstitialAd;

    private static void initInterstitialAd(Context context) {
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_interstitial_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }
        });
        requestNewInterstitial();
    }

    private static void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("913EF1D19EF6B17728D4288870AC1D6F") // P9
                .addTestDevice("5A8A78116D5F5F5EB0A7CFF87C6AC0D6") // 荣耀6
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    public static void show() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    public static void hide() {
        Activity activity = getAdActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    public static Activity getAdActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                LogUtil.log("activity=" + activity);
                if (activity instanceof AdActivity) {
                    return activity;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
