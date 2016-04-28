package com.dsbeckham.nasaimageryfetcher.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.dsbeckham.nasaimageryfetcher.activity.ViewPagerActivity;
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
        switch (((ViewPagerActivity) activity).currentFragment) {
            case "iotd":
                return ((ViewPagerActivity) activity).iotdRssModels.size();
            case "apod":
                switch (((ViewPagerActivity) activity).apodFetchService) {
                    case "morph_io":
                        return ((ViewPagerActivity) activity).apodMorphIoModels.size();
                    case "nasa_gov":
                        return ((ViewPagerActivity) activity).apodNasaGovModels.size();
                }
        }
        return 0;
    }

    @Override
    public Fragment getItem(int position) {
        if (((ViewPagerActivity) activity).currentFragment.equals("apod")) {
            if (position == getCount() - 1) {
                ApodQueryUtils.beginQuery(activity, ApodQueryUtils.QUERY_MODE_VIEWPAGER);
            }
        }
        return ImageFragment.newInstance(position);
    }
}
