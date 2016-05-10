package com.dsbeckham.nasaimageryfetcher.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.adapter.InformationFragmentStatePagerAdapter;
import com.dsbeckham.nasaimageryfetcher.fragment.ApodFragment;
import com.dsbeckham.nasaimageryfetcher.fragment.IotdFragment;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.util.ApodQueryUtils;
import com.dsbeckham.nasaimageryfetcher.util.PreferenceUtils;
import com.dsbeckham.nasaimageryfetcher.util.UiUtils;
import com.xgc1986.parallaxPagerTransformer.ParallaxPagerTransformer;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InformationActivity extends AppCompatActivity {
    @BindView(R.id.activity_information_toolbar)
    public Toolbar toolbar;
    @BindView(R.id.activity_information_viewpager)
    public ViewPager viewPager;

    public InformationFragmentStatePagerAdapter informationFragmentStatePagerAdapter;
    private int viewPagerCurrentItem = 0;

    public Calendar apodCalendar = Calendar.getInstance();

    public List<UniversalImageModel> apodModels = new ArrayList<>();
    public List<UniversalImageModel> iotdModels = new ArrayList<>();

    public boolean loadingData = false;
    public int nasaGovApiQueries = ApodQueryUtils.NASA_GOV_API_QUERIES;

    private final String VIEWPAGER_CURRENT_ITEM = "viewPagerCurrentItem";

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        ButterKnife.bind(this);

        UiUtils.makeStatusBarTransparent(this);
        UiUtils.setUpToolBarForChildActivity(this, toolbar);
        UiUtils.showFullscreen(this);

        switch (PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "")) {
            case "iotd":
                iotdModels = Parcels.unwrap(getIntent().getParcelableExtra(IotdFragment.EXTRA_IOTD_MODELS));
                break;
            case "apod":
                apodCalendar = (Calendar) getIntent().getSerializableExtra(ApodFragment.EXTRA_APOD_CALENDAR);
                apodModels = Parcels.unwrap(getIntent().getParcelableExtra(ApodFragment.EXTRA_APOD_MODELS));
                break;
        }

        if (savedInstanceState == null) {
            switch (PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "")) {
                case "iotd":
                    viewPagerCurrentItem = getIntent().getIntExtra(IotdFragment.EXTRA_IOTD_POSITION, 0);
                    break;
                case "apod":
                    viewPagerCurrentItem = getIntent().getIntExtra(ApodFragment.EXTRA_APOD_POSITION, 0);
                    break;
            }
        } else {
            viewPagerCurrentItem = savedInstanceState.getInt(VIEWPAGER_CURRENT_ITEM);
        }

        informationFragmentStatePagerAdapter = new InformationFragmentStatePagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(informationFragmentStatePagerAdapter);

        viewPager.post(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(viewPagerCurrentItem, false);
            }
        });

        viewPager.setPageTransformer(true, new ParallaxPagerTransformer(R.id.fragment_information_imageview));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.menu_toolbar_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");

                switch (PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "")) {
                    case "iotd":
                        intent.putExtra(Intent.EXTRA_TEXT, iotdModels.get(viewPagerCurrentItem).getImageUrl());
                        break;
                    case "apod":
                        intent.putExtra(Intent.EXTRA_TEXT, apodModels.get(viewPagerCurrentItem).getImageUrl());
                        break;
                }

                startActivity(Intent.createChooser(intent, null));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(VIEWPAGER_CURRENT_ITEM, viewPager.getCurrentItem());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void finish() {
        Intent intent = new Intent();

        switch (PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "")) {
            case "iotd":
                intent.putExtra(IotdFragment.EXTRA_IOTD_POSITION, viewPager.getCurrentItem());
                break;
            case "apod":
                intent.putExtra(ApodFragment.EXTRA_APOD_CALENDAR, apodCalendar);
                intent.putExtra(ApodFragment.EXTRA_APOD_MODELS, Parcels.wrap(apodModels));
                intent.putExtra(ApodFragment.EXTRA_APOD_POSITION, viewPager.getCurrentItem());
                break;
        }

        setResult(RESULT_OK, intent);
        super.finish();
    }
}
