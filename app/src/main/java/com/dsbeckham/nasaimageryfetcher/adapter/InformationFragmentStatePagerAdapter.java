package com.dsbeckham.nasaimageryfetcher.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.dsbeckham.nasaimageryfetcher.activity.InformationActivity;
import com.dsbeckham.nasaimageryfetcher.fragment.InformationFragment;
import com.dsbeckham.nasaimageryfetcher.util.ApodQueryUtils;

public class InformationFragmentStatePagerAdapter extends SmartFragmentStatePagerAdapter {
    private Activity activity;

    public InformationFragmentStatePagerAdapter(final Activity activity, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.activity = activity;
    }

    @Override
    public int getCount() {
       return ((InformationActivity) activity).models.size();
    }

    @Override
    public Fragment getItem(int position) {
        if (((InformationActivity) activity).type == InformationActivity.EXTRA_TYPE_APOD) {
            if (position == getCount() - 1) {
                ApodQueryUtils.beginQuery(activity, ApodQueryUtils.VIEWPAGER_INFORMATION, false);
            }
        }

        return InformationFragment.newInstance(position);
    }
}
