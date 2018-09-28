package com.beckhamd.nasaimageryfetcher.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.beckhamd.nasaimageryfetcher.activity.ImageActivity;
import com.beckhamd.nasaimageryfetcher.application.MainApplication;
import com.beckhamd.nasaimageryfetcher.fragment.ImageFragment;
import com.beckhamd.nasaimageryfetcher.util.ApodQueryUtils;

public class ImageFragmentStatePagerAdapter extends SmartFragmentStatePagerAdapter {
    private final Activity activity;

    public ImageFragmentStatePagerAdapter(final Activity activity, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        switch (((ImageActivity) activity).type) {
            case ImageActivity.EXTRA_TYPE_IOTD:
                return ((MainApplication) activity.getApplication()).getIotdModels().size();
            case ImageActivity.EXTRA_TYPE_APOD:
                return ((MainApplication) activity.getApplication()).getApodModels().size();
            case ImageActivity.EXTRA_TYPE_MIXED:
                return ((ImageActivity) activity).models.size();
        }

        return 0;
    }

    @Override
    public Fragment getItem(int position) {
        if (((ImageActivity) activity).type == ImageActivity.EXTRA_TYPE_APOD
                && position == (getCount() - 1)) {
            ApodQueryUtils.beginQuery(activity, ApodQueryUtils.TYPE_VIEWPAGER_IMAGE, false);
        }

        return ImageFragment.newInstance(position);
    }
}
