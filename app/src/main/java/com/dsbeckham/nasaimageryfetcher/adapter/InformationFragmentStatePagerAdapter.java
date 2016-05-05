package com.dsbeckham.nasaimageryfetcher.adapter;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.dsbeckham.nasaimageryfetcher.activity.InformationActivity;
import com.dsbeckham.nasaimageryfetcher.fragment.InformationFragment;
import com.dsbeckham.nasaimageryfetcher.util.ApodQueryUtils;
import com.dsbeckham.nasaimageryfetcher.util.PreferenceUtils;

public class InformationFragmentStatePagerAdapter extends SmartFragmentStatePagerAdapter {
    private Activity activity;

    public InformationFragmentStatePagerAdapter(final Activity activity, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        switch (PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "")) {
            case "iotd":
                return ((InformationActivity) activity).iotdModels.size();
            case "apod":
                 return ((InformationActivity) activity).apodModels.size();
        }
        return 0;
    }

    @Override
    public Fragment getItem(int position) {
        if (PreferenceManager.getDefaultSharedPreferences(activity).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "").equals("apod")) {
            if (position == getCount() - 1) {
                ApodQueryUtils.beginQuery(activity, ApodQueryUtils.VIEWPAGER_INFORMATION);
            }
        }
        return InformationFragment.newInstance(position);
    }
}
