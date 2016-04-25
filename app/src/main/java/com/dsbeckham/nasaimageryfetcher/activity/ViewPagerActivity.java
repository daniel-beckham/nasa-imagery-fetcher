package com.dsbeckham.nasaimageryfetcher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.adapter.ImageFragmentStatePagerAdapter;
import com.dsbeckham.nasaimageryfetcher.fragment.ApodFragment;
import com.dsbeckham.nasaimageryfetcher.fragment.IotdFragment;
import com.dsbeckham.nasaimageryfetcher.util.PreferenceUtils;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ViewPagerActivity extends AppCompatActivity {
    @Bind(R.id.view_pager)
    public ViewPager viewPager;

    public ImageFragmentStatePagerAdapter imageFragmentStatePagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);
        ButterKnife.bind(this);

        imageFragmentStatePagerAdapter = new ImageFragmentStatePagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(imageFragmentStatePagerAdapter);
        viewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // Code goes here
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        switch (PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "iotd")) {
            case "iotd":
                setTitle(getString(R.string.nav_iotd));
                break;
            case "apod":
                setTitle(getString(R.string.nav_apod));
                break;
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();

        switch (PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "iotd")) {
            case "iotd":
                intent.putExtra(IotdFragment.EXTRA_IOTD_POSITION, viewPager.getCurrentItem());
                break;
            case "apod":
                // Add a check here that determines which API should be used based
                // on the user settings. (Also, add the relevant setting.)
                intent.putExtra(ApodFragment.EXTRA_APOD_MORPH_IO_MODELS, Parcels.wrap(imageFragmentStatePagerAdapter.apodMorphIoModels));
                // intent.putExtra(ApodFragment.EXTRA_APOD_NASA_MODELS, Parcels.wrap(imageFragmentStatePagerAdapter.apodNasaModels));
                intent.putExtra(ApodFragment.EXTRA_APOD_CALENDAR, imageFragmentStatePagerAdapter.calendar);
                intent.putExtra(ApodFragment.EXTRA_APOD_POSITION, viewPager.getCurrentItem());
                break;
        }

        setResult(RESULT_OK, intent);
        super.finish();
    }
}
