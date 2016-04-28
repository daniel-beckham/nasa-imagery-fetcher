package com.dsbeckham.nasaimageryfetcher.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.adapter.ImageFragmentStatePagerAdapter;
import com.dsbeckham.nasaimageryfetcher.fragment.ApodFragment;
import com.dsbeckham.nasaimageryfetcher.fragment.IotdFragment;
import com.dsbeckham.nasaimageryfetcher.model.ApodMorphIoModel;
import com.dsbeckham.nasaimageryfetcher.model.ApodNasaGovModel;
import com.dsbeckham.nasaimageryfetcher.model.IotdRssModel;
import com.dsbeckham.nasaimageryfetcher.util.ApodQueryUtils;
import com.dsbeckham.nasaimageryfetcher.util.PreferenceUtils;
import com.xgc1986.parallaxPagerTransformer.ParallaxPagerTransformer;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ViewPagerActivity extends AppCompatActivity {
    @Bind(R.id.viewpager_toolbar)
    Toolbar toolbar;
    @Bind(R.id.viewpager)
    public ViewPager viewPager;

    public String apodFetchService;
    public String currentFragment;
    private int viewPagerCurrentItem;

    public ImageFragmentStatePagerAdapter imageFragmentStatePagerAdapter;

    public List<ApodMorphIoModel> apodMorphIoModels = new ArrayList<>();
    public List<ApodNasaGovModel> apodNasaGovModels = new ArrayList<>();
    public List<IotdRssModel.Channel.Item> iotdRssModels = new ArrayList<>();

    public Calendar calendar = Calendar.getInstance();
    public boolean loadingData = false;
    public int nasaGovApiQueries = ApodQueryUtils.NASA_GOV_API_QUERIES;

    static final String VIEW_PAGER_CURRENT_ITEM = "viewPagerCurrentItem";

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else {
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
        }

        apodFetchService = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceUtils.PREF_APOD_FETCH_SERVICE, "");
        currentFragment = PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "");

        switch (currentFragment) {
            case "iotd":
                iotdRssModels = Parcels.unwrap(getIntent().getParcelableExtra(IotdFragment.EXTRA_IOTD_RSS_MODELS));
                break;
            case "apod":
                switch (apodFetchService) {
                    case "morph_io":
                        apodMorphIoModels = Parcels.unwrap(getIntent().getParcelableExtra(ApodFragment.EXTRA_APOD_MORPH_IO_MODELS));
                        break;
                    case "nasa_gov":
                        apodNasaGovModels = Parcels.unwrap(getIntent().getParcelableExtra(ApodFragment.EXTRA_APOD_NASA_GOV_MODELS));
                        break;
                }

                calendar = (Calendar) getIntent().getSerializableExtra(ApodFragment.EXTRA_APOD_CALENDAR);
                break;
        }

        if (savedInstanceState == null) {
            switch (currentFragment) {
                case "iotd":
                    viewPagerCurrentItem = getIntent().getIntExtra(IotdFragment.EXTRA_IOTD_POSITION, 0);
                    break;
                case "apod":
                    viewPagerCurrentItem = getIntent().getIntExtra(ApodFragment.EXTRA_APOD_POSITION, 0);
                    break;
            }
        } else {
            viewPagerCurrentItem = savedInstanceState.getInt(VIEW_PAGER_CURRENT_ITEM);
        }

        imageFragmentStatePagerAdapter = new ImageFragmentStatePagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(imageFragmentStatePagerAdapter);

        viewPager.post(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(viewPagerCurrentItem, false);
            }
        });

        viewPager.setPageTransformer(true, new ParallaxPagerTransformer(R.id.fragment_image_imageview));
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(VIEW_PAGER_CURRENT_ITEM, viewPager.getCurrentItem());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void finish() {
        Intent intent = new Intent();

        switch (currentFragment) {
            case "iotd":
                intent.putExtra(IotdFragment.EXTRA_IOTD_POSITION, viewPager.getCurrentItem());
                break;
            case "apod":
                switch (apodFetchService) {
                    case "morph_io":
                        intent.putExtra(ApodFragment.EXTRA_APOD_MORPH_IO_MODELS, Parcels.wrap(apodMorphIoModels));
                        break;
                    case "nasa_gov":
                        intent.putExtra(ApodFragment.EXTRA_APOD_NASA_GOV_MODELS, Parcels.wrap(apodNasaGovModels));
                        break;
                }

                intent.putExtra(ApodFragment.EXTRA_APOD_CALENDAR, calendar);
                intent.putExtra(ApodFragment.EXTRA_APOD_POSITION, viewPager.getCurrentItem());
                break;
        }

        setResult(RESULT_OK, intent);
        super.finish();
    }
}
