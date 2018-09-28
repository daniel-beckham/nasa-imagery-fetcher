package com.beckhamd.nasaimageryfetcher.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.beckhamd.nasaimageryfetcher.R;
import com.beckhamd.nasaimageryfetcher.adapter.InformationFragmentStatePagerAdapter;
import com.beckhamd.nasaimageryfetcher.application.MainApplication;
import com.beckhamd.nasaimageryfetcher.model.UniversalImageModel;
import com.beckhamd.nasaimageryfetcher.util.ApodQueryUtils;
import com.beckhamd.nasaimageryfetcher.util.DownloadUtils;
import com.beckhamd.nasaimageryfetcher.util.IotdQueryUtils;
import com.beckhamd.nasaimageryfetcher.util.PermissionUtils;
import com.beckhamd.nasaimageryfetcher.util.UiUtils;
import com.beckhamd.nasaimageryfetcher.util.WallpaperUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InformationActivity extends AppCompatActivity {
    public static final String EXTRA_MODELS = "com.beckhamd.nasaimageryfetcher.extra.MODELS";
    public static final String EXTRA_POSITION = "com.beckhamd.nasaimageryfetcher.extra.POSITION";
    public static final String EXTRA_TYPE = "com.beckhamd.nasaimageryfetcher.extra.TYPE";

    public static final int EXTRA_TYPE_IOTD = 0;
    public static final int EXTRA_TYPE_APOD = 1;
    public static final int EXTRA_TYPE_MIXED = 2;

    private final String VIEWPAGER_CURRENT_ITEM = "viewPagerCurrentItem";

    @BindView(R.id.activity_information_toolbar)
    public Toolbar toolbar;
    @BindView(R.id.activity_information_viewpager)
    public ViewPager viewPager;

    public InformationFragmentStatePagerAdapter informationFragmentStatePagerAdapter;
    private int viewPagerCurrentItem;

    public ArrayList<UniversalImageModel> models = new ArrayList<>();
    public int type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        ButterKnife.bind(this);

        UiUtils.makeStatusBarTransparent(this, true);
        UiUtils.setUpToolBarForChildActivity(this, toolbar);
        UiUtils.showFullscreen(this);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(EXTRA_MODELS)) {
                models = getIntent().getParcelableArrayListExtra(EXTRA_MODELS);
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
                int size = 0;

                switch (type) {
                    case ImageActivity.EXTRA_TYPE_IOTD:
                        size = ((MainApplication) getApplication()).getIotdModels().size();

                        if (size == 0) {
                            IotdQueryUtils.beginFetch(InformationActivity.this, IotdQueryUtils.TYPE_VIEWPAGER_INFORMATION);
                        }
                        break;
                    case ImageActivity.EXTRA_TYPE_APOD:
                        size = ((MainApplication) getApplication()).getApodModels().size();

                        if (size == 0) {
                            ApodQueryUtils.beginQuery(InformationActivity.this, ApodQueryUtils.TYPE_VIEWPAGER_INFORMATION, false);
                        }
                        break;
                    case ImageActivity.EXTRA_TYPE_MIXED:
                        size = models.size();
                        break;
                }

                if (viewPagerCurrentItem < size) {
                    viewPager.setCurrentItem(viewPagerCurrentItem, false);
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
        UniversalImageModel universalImageModel = getModel(viewPager.getCurrentItem());

        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.menu_toolbar_share:
                if (universalImageModel !=  null) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, universalImageModel.getImageUrl());
                    intent.setType("text/plain");
                    startActivity(Intent.createChooser(intent, null));
                }
                break;
            case R.id.menu_toolbar_download:
                if (universalImageModel !=  null) {
                    if (PermissionUtils.isStoragePermissionGranted(this)) {
                        DownloadUtils.validateAndDownloadImage(this, universalImageModel, false, false, true);
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                }
                break;
            case R.id.menu_toolbar_wallpaper:
                if (universalImageModel !=  null) {
                    WallpaperUtils.setWallpaper(this, universalImageModel.getImageUrl(), true);
                }
                break;
            case R.id.menu_toolbar_website:
                if (universalImageModel !=  null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(universalImageModel.getPageUrl()));
                    startActivity(intent);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PermissionUtils.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    UniversalImageModel universalImageModel = getModel(viewPager.getCurrentItem());

                    if (universalImageModel != null) {
                        DownloadUtils.validateAndDownloadImage(this, universalImageModel, false, false, true);
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
        intent.putExtra(EXTRA_POSITION, viewPager.getCurrentItem());
        setResult(RESULT_OK, intent);
        super.finish();
    }

    public UniversalImageModel getModel(int position) {
        UniversalImageModel universalImageModel = null;

        switch (type) {
            case EXTRA_TYPE_IOTD:
                if (!((MainApplication) getApplication()).getIotdModels().isEmpty()) {
                    universalImageModel = ((MainApplication) getApplication()).getIotdModels().get(position);
                }
                break;
            case EXTRA_TYPE_APOD:
                if (!((MainApplication) getApplication()).getApodModels().isEmpty()) {
                    universalImageModel = ((MainApplication) getApplication()).getApodModels().get(position);
                }
                break;
            case EXTRA_TYPE_MIXED:
                if (!models.isEmpty()) {
                    universalImageModel = models.get(position);
                }
                break;
        }

        return universalImageModel;
    }
}
