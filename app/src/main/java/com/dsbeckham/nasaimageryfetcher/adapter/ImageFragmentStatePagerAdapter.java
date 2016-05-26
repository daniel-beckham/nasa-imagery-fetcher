package com.dsbeckham.nasaimageryfetcher.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.dsbeckham.nasaimageryfetcher.activity.ImageActivity;
import com.dsbeckham.nasaimageryfetcher.activity.InformationActivity;
import com.dsbeckham.nasaimageryfetcher.fragment.ImageFragment;
import com.dsbeckham.nasaimageryfetcher.util.ApodQueryUtils;

public class ImageFragmentStatePagerAdapter extends SmartFragmentStatePagerAdapter {
    private Activity activity;

    public ImageFragmentStatePagerAdapter(final Activity activity, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.activity = activity;
    }

    @Override
    public int getCount() {
       return ((ImageActivity) activity).models.size();
    }

    @Override
    public Fragment getItem(int position) {
        if (((ImageActivity) activity).type == InformationActivity.EXTRA_TYPE_APOD) {
            if (position == getCount() - 1) {
                ApodQueryUtils.beginQuery(activity, ApodQueryUtils.VIEWPAGER_IMAGE, false);
            }
        }

        return ImageFragment.newInstance(position);
    }
}
