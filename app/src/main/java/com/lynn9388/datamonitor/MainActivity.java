/*
 * MainActivity
 * Copyright (C) 2016  Lynn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lynn9388.datamonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.lynn9388.datamonitor.dao.DaoMaster;
import com.lynn9388.datamonitor.fragment.DetailFragment;
import com.lynn9388.datamonitor.fragment.MobileDataFragment;
import com.lynn9388.datamonitor.fragment.RealtimeFragment;
import com.lynn9388.datamonitor.fragment.SettingsFragment;
import com.lynn9388.datamonitor.introduction.IntroductionActivity;
import com.lynn9388.datamonitor.util.NetworkUtil;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    // Indicates whether user has opened the app before
    public static final String PREF_KEY_HAS_OPENED = "pref_key_has_opened";

    public static final String DATABASE_NAME = "DataMonitor.db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean(PREF_KEY_HAS_OPENED, false)) {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            startActivity(new Intent(MainActivity.this, IntroductionActivity.class));
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
        replaceFragment(new RealtimeFragment(), getString(R.string.nav_realtime_title));

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, DATABASE_NAME, null);
        SQLiteDatabase database = helper.getWritableDatabase();

        boolean isDataMonitoringEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(SettingsFragment.PREF_KEY_ENABLE_DATA_MONITORING, true);
        if (NetworkUtil.isNetworkConnected(this) && isDataMonitoringEnabled) {
            preferences.edit().putBoolean(NetworkReceiver.PREF_KEY_NETWORK_CONNECTED, true).apply();
            startService(new Intent(this, NetworkService.class));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_mobile_data) {
            replaceFragment(new MobileDataFragment(), getString(R.string.nav_mobile_data_title));
        } else if (id == R.id.nav_detail) {
            replaceFragment(new DetailFragment(), getString(R.string.nav_detail_title));
        } else if (id == R.id.nav_realtime) {
            replaceFragment(new RealtimeFragment(), getString(R.string.nav_realtime_title));
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_send_feedback) {
            sendFeedback();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Replaces main activity's content to a fragment, and changes the toolbar's title.
     *
     * @param fragment The new fragment to place in main activity.
     * @param title    The new title for the toolbar.
     */
    private void replaceFragment(Fragment fragment, String title) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_content, fragment).commit();
        getSupportActionBar().setTitle(title);
    }

    private void sendFeedback() {
        String addresses = "lynn9388@gmail.com";
        String subject = getString(R.string.app_name) + " Feedback/Bug Report (v"
                + BuildConfig.VERSION_NAME + ")";
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", addresses, null));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        startActivity(Intent.createChooser(intent, "Report Problem"));
    }
}
