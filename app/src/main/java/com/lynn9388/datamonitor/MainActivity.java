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
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lynn9388.datamonitor.dao.DaoMaster;
import com.lynn9388.datamonitor.dao.DaoSession;
import com.lynn9388.datamonitor.introduction.IntroductionActivity;
import com.lynn9388.datamonitor.util.NetworkUtil;
import com.philjay.circledisplay.CircleDisplay;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String DATABASE_NAME = "DataMonitor.db";

    private SQLiteDatabase mDatabase;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String firstTimePreference = "first_time";
        boolean isFirstStart = preferences.getBoolean(firstTimePreference, true);
        if (isFirstStart) {
            startActivity(new Intent(MainActivity.this, IntroductionActivity.class));
            finish();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(firstTimePreference, false);
            editor.apply();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initOverView();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, DATABASE_NAME, null);
        mDatabase = helper.getWritableDatabase();
        mDaoMaster = new DaoMaster(mDatabase);
        mDaoSession = mDaoMaster.newSession();

        if (NetworkUtil.isNetworkConnected(this)) {
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_help_and_feedback) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initOverView() {
        View container = findViewById(R.id.main_activity_container);
        CircleDisplay circleDisplay = (CircleDisplay) container.findViewById(R.id.data_usage_view);
        View panel1 = container.findViewById(R.id.panel1);
        View panel2 = container.findViewById(R.id.panel2);
        View panel3 = container.findViewById(R.id.panel3);
        View panel4 = container.findViewById(R.id.panel4);

        initPanel(panel1, R.string.used_today, "--");
        initPanel(panel2, R.string.used_this_month, "--");
        initPanel(panel3, R.string.monthly_data_plan, "--");
        initPanel(panel4, R.string.remaining_this_month, "--");
    }

    private void initPanel(View panel, int title, String value) {
        TextView titleView = (TextView) panel.findViewById(R.id.title);
        TextView valueView = (TextView) panel.findViewById(R.id.value);
        titleView.setText(getString(title));
        valueView.setText(value);
    }
}
