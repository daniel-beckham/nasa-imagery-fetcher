package com.dsbeckham.nasaimageryfetcher.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.dsbeckham.nasaimageryfetcher.model.UniversalImageModel;
import com.dsbeckham.nasaimageryfetcher.util.ApodQueryUtils;
import com.dsbeckham.nasaimageryfetcher.util.DownloadUtils;
import com.dsbeckham.nasaimageryfetcher.util.PermissionUtils;
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
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);

        UiUtils.makeStatusBarTransparent(this);
        UiUtils.setUpToolBarForChildActivity(this, toolbar);
        UiUtils.showSystemUI(this);

        if (getIntent().getExtras() != null) {
            models = Parcels.unwrap(getIntent().getParcelableExtra(InformationActivity.EXTRA_MODELS));

            if (getIntent().getExtras().containsKey(InformationActivity.EXTRA_CALENDAR)) {
                calendar = (Calendar) getIntent().getSerializableExtra(InformationActivity.EXTRA_CALENDAR);
            }

            if (savedInstanceState == null) {
                viewPagerCurrentItem = getIntent().getIntExtra(InformationActivity.EXTRA_POSITION, 0);
            } else {
                viewPagerCurrentItem = savedInstanceState.getInt(VIEWPAGER_CURRENT_ITEM);
            }

            type = getIntent().getIntExtra(InformationActivity.EXTRA_TYPE, InformationActivity.EXTRA_TYPE_MIXED);
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
            case R.id.menu_toolbar_website:
                if (!models.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(models.get(viewPager.getCurrentItem()).getPageUrl()));
                    startActivity(intent);
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

        if (type == InformationActivity.EXTRA_TYPE_APOD) {
            intent.putExtra(InformationActivity.EXTRA_CALENDAR, calendar);
            intent.putExtra(InformationActivity.EXTRA_MODELS, Parcels.wrap(models));
        }

        intent.putExtra(InformationActivity.EXTRA_POSITION, viewPager.getCurrentItem());
        setResult(RESULT_OK, intent);
        super.finish();
    }
}
