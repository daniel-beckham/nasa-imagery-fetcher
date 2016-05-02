package com.dsbeckham.nasaimageryfetcher.adapter;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.dsbeckham.nasaimageryfetcher.activity.ViewPagerActivity;
import com.dsbeckham.nasaimageryfetcher.fragment.ViewPagerFragment;
import com.dsbeckham.nasaimageryfetcher.util.ApodQueryUtils;
import com.dsbeckham.nasaimageryfetcher.util.PreferenceUtils;

public class ImageFragmentStatePagerAdapter extends SmartFragmentStatePagerAdapter {
    private Activity activity;

    public ImageFragmentStatePagerAdapter(final Activity activity, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        switch (PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "")) {
            case "iotd":
                return ((ViewPagerActivity) activity).iotdModels.size();
            case "apod":
                 return ((ViewPagerActivity) activity).apodModels.size();
        }
        return 0;
    }

    @Override
    public Fragment getItem(int position) {
        if (PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "").equals("apod")) {
            if (position == getCount() - 1) {
                ApodQueryUtils.beginQuery(activity, ApodQueryUtils.QUERY_MODE_VIEWPAGER);
            }
        }
        return ViewPagerFragment.newInstance(position);
    }
}
