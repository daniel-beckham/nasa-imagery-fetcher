package com.beckhamd.nasaimageryfetcher.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.beckhamd.nasaimageryfetcher.activity.InformationActivity;
import com.beckhamd.nasaimageryfetcher.application.MainApplication;
import com.beckhamd.nasaimageryfetcher.fragment.InformationFragment;
import com.beckhamd.nasaimageryfetcher.util.ApodQueryUtils;

public class InformationFragmentStatePagerAdapter extends SmartFragmentStatePagerAdapter {
    private final Activity activity;

    public InformationFragmentStatePagerAdapter(final Activity activity, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        switch (((InformationActivity) activity).type) {
            case InformationActivity.EXTRA_TYPE_IOTD:
                return ((MainApplication) activity.getApplication()).getIotdModels().size();
            case InformationActivity.EXTRA_TYPE_APOD:
                return ((MainApplication) activity.getApplication()).getApodModels().size();
            case InformationActivity.EXTRA_TYPE_MIXED:
                return ((InformationActivity) activity).models.size();
        }

        return 0;
    }

    @Override
    public Fragment getItem(int position) {
        if (((InformationActivity) activity).type == InformationActivity.EXTRA_TYPE_APOD
                && position == (getCount() - 1)) {
            ApodQueryUtils.beginQuery(activity, ApodQueryUtils.TYPE_VIEWPAGER_INFORMATION, false);
        }

        return InformationFragment.newInstance(position);
    }
}
