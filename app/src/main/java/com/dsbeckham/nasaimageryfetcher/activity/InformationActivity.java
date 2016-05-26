package com.dsbeckham.nasaimageryfetcher.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.adapter.InformationFragmentStatePagerAdapter;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.util.ApodQueryUtils;
import com.dsbeckham.nasaimageryfetcher.util.DownloadUtils;
import com.dsbeckham.nasaimageryfetcher.util.PermissionUtils;
import com.dsbeckham.nasaimageryfetcher.util.UiUtils;
import com.xgc1986.parallaxPagerTransformer.ParallaxPagerTransformer;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InformationActivity extends AppCompatActivity {
    public static String EXTRA_CALENDAR = "com.dsbeckham.nasaimageryfetcher.extra.CALENDAR";
    public static String EXTRA_MODELS = "com.dsbeckham.nasaimageryfetcher.extra.MODELS";
    public static String EXTRA_POSITION = "com.dsbeckham.nasaimageryfetcher.extra.POSITION";
    public static String EXTRA_TYPE = "com.dsbeckham.nasaimageryfetcher.extra.TYPE";

    public static int EXTRA_TYPE_IOTD = 0;
    public static int EXTRA_TYPE_APOD = 1;
    public static int EXTRA_TYPE_MIXED = 2;

    @BindView(R.id.activity_information_toolbar)
    public Toolbar toolbar;
    @BindView(R.id.activity_information_viewpager)
    public ViewPager viewPager;

    public InformationFragmentStatePagerAdapter informationFragmentStatePagerAdapter;
    private int viewPagerCurrentItem;

    public List<UniversalImageModel> models = new ArrayList<>();

    public Calendar calendar = Calendar.getInstance();
    public boolean loadingData;
    public int nasaGovApiQueries = ApodQueryUtils.NASA_GOV_API_QUERIES;
    public int type;

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

        if (getIntent().getExtras() != null) {
            models = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_MODELS));

            if (getIntent().getExtras().containsKey(EXTRA_CALENDAR)) {
                calendar = (Calendar) getIntent().getSerializableExtra(EXTRA_CALENDAR);
            }

            if (savedInstanceState == null) {
                viewPagerCurrentItem = getIntent().getIntExtra(EXTRA_POSITION, 0);
            } else {
                viewPagerCurrentItem = savedInstanceState.getInt(VIEWPAGER_CURRENT_ITEM);
            }

            type = getIntent().getIntExtra(EXTRA_TYPE, EXTRA_TYPE_MIXED);
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
                if (!models.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, models.get(viewPager.getCurrentItem()).getImageUrl());
                    intent.setType("text/plain");
                    startActivity(Intent.createChooser(intent, null));
                }
                break;
            case R.id.menu_toolbar_download:
                if (!models.isEmpty()) {
                    if (PermissionUtils.isStoragePermissionGranted(this)) {
                        DownloadUtils.validateAndDownloadImage(this, models.get(viewPager.getCurrentItem()), false, false);
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!models.isEmpty()) {
                        DownloadUtils.validateAndDownloadImage(this, models.get(viewPager.getCurrentItem()), false, false);
                    }
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(VIEWPAGER_CURRENT_ITEM, viewPager.getCurrentItem());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void finish() {
        Intent intent = new Intent();

        if (type == EXTRA_TYPE_APOD) {
            intent.putExtra(EXTRA_CALENDAR, calendar);
            intent.putExtra(EXTRA_MODELS, Parcels.wrap(models));
        }

        intent.putExtra(EXTRA_POSITION, viewPager.getCurrentItem());
        setResult(RESULT_OK, intent);
        super.finish();
    }
}
