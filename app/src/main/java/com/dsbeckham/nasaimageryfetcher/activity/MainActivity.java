package com.dsbeckham.nasaimageryfetcher.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.dsbeckham.nasaimageryfetcher.R;
import com.dsbeckham.nasaimageryfetcher.fragment.ApodFragment;
import com.dsbeckham.nasaimageryfetcher.fragment.IotdFragment;
import com.dsbeckham.nasaimageryfetcher.util.PreferenceUtils;
import com.dsbeckham.nasaimageryfetcher.util.QueryUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.nav_view)
    NavigationView navigationView;
    @Bind(R.id.main_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        PreferenceUtils.setDefaultValuesForPreferences(this);
        QueryUtils.setUpIoServices();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.coordinatorlayout, new IotdFragment(), "iotd")
                    .commit();

            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "iotd").apply();
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
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_iotd:
                if (getFragmentManager().findFragmentByTag("iotd") != null) {
                    getFragmentManager().beginTransaction()
                            .show(getFragmentManager().findFragmentByTag("iotd"))
                            .commit();
                } else {
                    getFragmentManager().beginTransaction()
                            .add(R.id.coordinatorlayout, new IotdFragment(), "iotd")
                            .commit();
                }

                if (getFragmentManager().findFragmentByTag("apod") != null) {
                    getFragmentManager().beginTransaction()
                            .hide(getFragmentManager().findFragmentByTag("apod"))
                            .commit();
                }

                navigationView.setCheckedItem(R.id.nav_iotd);
                setTitle(getString(R.string.nav_iotd));
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "iotd").apply();
                break;
            case R.id.nav_apod:
                if (getFragmentManager().findFragmentByTag("apod") != null) {
                    getFragmentManager().beginTransaction()
                            .show(getFragmentManager().findFragmentByTag("apod"))
                            .commit();
                } else {
                    getFragmentManager().beginTransaction()
                            .add(R.id.coordinatorlayout, new ApodFragment(), "apod")
                            .commit();
                }

                if (getFragmentManager().findFragmentByTag("iotd") != null) {
                    getFragmentManager().beginTransaction()
                            .hide(getFragmentManager().findFragmentByTag("iotd"))
                            .commit();
                }

                navigationView.setCheckedItem(R.id.nav_apod);
                setTitle(getString(R.string.nav_apod));
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PreferenceUtils.PREF_CURRENT_FRAGMENT, "apod").apply();
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
}
