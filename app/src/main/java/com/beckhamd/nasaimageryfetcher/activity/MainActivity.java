package com.beckhamd.nasaimageryfetcher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.beckhamd.nasaimageryfetcher.R;
import com.beckhamd.nasaimageryfetcher.fragment.ApodFragment;
import com.beckhamd.nasaimageryfetcher.fragment.IotdFragment;
import com.beckhamd.nasaimageryfetcher.job.BackgroundJob;
import com.beckhamd.nasaimageryfetcher.util.ApodQueryUtils;
import com.beckhamd.nasaimageryfetcher.util.IotdQueryUtils;
import com.beckhamd.nasaimageryfetcher.util.PreferenceUtils;
import com.beckhamd.nasaimageryfetcher.util.UiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.drawerlayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.navigationview)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        UiUtils.makeStatusBarTransparent(this, false);

        ApodQueryUtils.setUpIoServices(this);
        IotdQueryUtils.setUpIoService(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceUtils.PREF_NOTIFICATIONS, false)
                || PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceUtils.PREF_SAVE_TO_EXTERNAL_STORAGE, false)
                || PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferenceUtils.PREF_SET_AS_WALLPAPER, false)) {
            BackgroundJob.scheduleJob();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, new IotdFragment(), PreferenceUtils.FRAGMENT_IOTD)
                    .commit();

            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString(PreferenceUtils.PREF_CURRENT_FRAGMENT, PreferenceUtils.FRAGMENT_IOTD).apply();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String title = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "");

        if (title != null) {
            switch (title) {
                case PreferenceUtils.FRAGMENT_IOTD:
                    setTitle(getString(R.string.app_iotd));
                    break;
                case PreferenceUtils.FRAGMENT_APOD:
                    setTitle(getString(R.string.app_apod));
                    break;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        final ApodFragment apodFragment = (ApodFragment) getSupportFragmentManager()
                .findFragmentByTag(PreferenceUtils.FRAGMENT_APOD);
        final IotdFragment iotdFragment = (IotdFragment) getSupportFragmentManager()
                .findFragmentByTag(PreferenceUtils.FRAGMENT_IOTD);

        switch (menuItem.getItemId()) {
            case R.id.nav_iotd:
                if (iotdFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .show(iotdFragment)
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.content, new IotdFragment(), PreferenceUtils.FRAGMENT_IOTD)
                            .commit();
                }

                if (apodFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .hide(apodFragment)
                            .commit();
                }

                navigationView.setCheckedItem(R.id.nav_iotd);
                setTitle(getString(R.string.app_iotd));
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putString(PreferenceUtils.PREF_CURRENT_FRAGMENT, PreferenceUtils.FRAGMENT_IOTD).apply();
                break;
            case R.id.nav_apod:
                if (apodFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .show(apodFragment)
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.content, new ApodFragment(), PreferenceUtils.FRAGMENT_APOD)
                            .commit();
                }

                if (iotdFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .hide(iotdFragment)
                            .commit();
                }

                navigationView.setCheckedItem(R.id.nav_apod);
                setTitle(getString(R.string.app_apod));
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putString(PreferenceUtils.PREF_CURRENT_FRAGMENT, PreferenceUtils.FRAGMENT_APOD).apply();
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, com.beckhamd.nasaimageryfetcher.activity.SettingsActivity.class);
                startActivity(intent);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
