package com.dsbeckham.nasaimageryfetcher.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.adapter.ImageFragmentStatePagerAdapter;
import com.dsbeckham.nasaimageryfetcher.fragment.ApodFragment;
import com.dsbeckham.nasaimageryfetcher.fragment.IotdFragment;
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.util.ApodQueryUtils;
import com.dsbeckham.nasaimageryfetcher.util.DownloadUtils;
import com.dsbeckham.nasaimageryfetcher.util.PermissionUtils;
import com.dsbeckham.nasaimageryfetcher.util.PreferenceUtils;
import com.dsbeckham.nasaimageryfetcher.util.UiUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageActivity extends AppCompatActivity {
    @BindView(R.id.activity_image_toolbar)
    public Toolbar toolbar;
    @BindView(R.id.activity_image_viewpager)
    public ViewPager viewPager;

    public ImageFragmentStatePagerAdapter imageFragmentStatePagerAdapter;
    public GestureDetector gestureDetector;
    private boolean systemUiHidden = false;
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
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);

        UiUtils.makeStatusBarTransparent(this);
        UiUtils.setUpToolBarForChildActivity(this, toolbar);
        UiUtils.showSystemUI(this);

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

        imageFragmentStatePagerAdapter = new ImageFragmentStatePagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(imageFragmentStatePagerAdapter);

        viewPager.post(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(viewPagerCurrentItem, false);
            }
        });

        viewPager.setPageTransformer(true, new UiUtils.DepthPageTransformer());

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                if (systemUiHidden) {
                    UiUtils.showSystemUI(ImageActivity.this);
                } else {
                    UiUtils.hideSystemUI(ImageActivity.this);
                }
                return true;
            }
        });

        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().show();
                    }

                    systemUiHidden = false;
                } else {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().hide();
                    }

                    systemUiHidden = true;
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        UniversalImageModel universalImageModel = null;

        switch (PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "")) {
            case "iotd":
               universalImageModel = iotdModels.get(viewPagerCurrentItem);
                break;
            case "apod":
                universalImageModel = apodModels.get(viewPagerCurrentItem);
                break;
        }

        if (universalImageModel == null) {
            return super.onOptionsItemSelected(item);
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.menu_toolbar_share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, universalImageModel.getImageUrl());
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, null));
                break;
            case R.id.menu_toolbar_download:
                if (PermissionUtils.isStoragePermissionGranted(this)) {
                    DownloadUtils.validateUriAndDownloadFile(this, Uri.parse(universalImageModel.getImageUrl()), Uri.parse(universalImageModel.getImageThumbnailUrl()), false);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    UniversalImageModel universalImageModel = null;

                    switch (PreferenceManager.getDefaultSharedPreferences(this).getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "")) {
                        case "iotd":
                            universalImageModel = iotdModels.get(viewPagerCurrentItem);
                            break;
                        case "apod":
                            universalImageModel = apodModels.get(viewPagerCurrentItem);
                            break;
                    }

                    if (universalImageModel != null) {
                        DownloadUtils.validateUriAndDownloadFile(this, Uri.parse(universalImageModel.getImageUrl()), Uri.parse(universalImageModel.getImageThumbnailUrl()), false);
                    }
                }
            }
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
