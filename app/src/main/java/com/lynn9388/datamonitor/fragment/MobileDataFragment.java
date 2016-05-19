/*
 * MobileDataFragment
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

package com.lynn9388.datamonitor.fragment;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lynn9388.datamonitor.R;
import com.lynn9388.datamonitor.util.TrafficUtil;
import com.philjay.circledisplay.CircleDisplay;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class MobileDataFragment extends Fragment {
    private static int[] sPanelViewIds = {R.id.panel0, R.id.panel1, R.id.panel2, R.id.panel3};
    private static int[] sPanelTitles = {R.string.used_today, R.string.used_this_month,
            R.string.remaining_this_month, R.string.till_next_settlement};
    private static String[] sPanelValuePrefKeys = {"pref_key_panel0", "pref_key_panel1",
            "pref_key_panel2", "pref_key_panel3"};
    private static String sUsagePercentagePrefKey = "pref_key_usage_percentage";

    private CircleDisplay mCircleDisplay;
    private View[] mPanels;

    private SharedPreferences mSharedPreferences;
    private UpdateTask mUpdateTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mobile_data, container, false);

        mCircleDisplay = (CircleDisplay) view.findViewById(R.id.data_usage_view);
        mPanels = new View[sPanelViewIds.length];
        for (int i = 0; i < sPanelViewIds.length; i++) {
            mPanels[i] = view.findViewById(sPanelViewIds[i]);
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        updatePanels(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mUpdateTask = new UpdateTask();
        mUpdateTask.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        mUpdateTask.cancel(true);
    }

    private void updatePanels(boolean animated) {
        float usagePercentage = mSharedPreferences.getFloat(sUsagePercentagePrefKey, 0f);
        if (usagePercentage < 50) {
            mCircleDisplay.setColor(Color.GREEN);
        } else if (usagePercentage < 75) {
            mCircleDisplay.setColor(Color.YELLOW);
        } else {
            mCircleDisplay.setColor(Color.RED);
        }
        mCircleDisplay.setAnimDuration(3000);
        mCircleDisplay.setStepSize(0.5f);
        mCircleDisplay.setTouchEnabled(false);
        mCircleDisplay.showValue(usagePercentage, 100f, animated);

        for (int i = 0; i < sPanelViewIds.length; i++) {
            TextView titleView = (TextView) mPanels[i].findViewById(R.id.title);
            TextView valueView = (TextView) mPanels[i].findViewById(R.id.value);
            titleView.setText(getString(sPanelTitles[i]));
            valueView.setText(mSharedPreferences.getString(sPanelValuePrefKeys[i], "--"));
        }
    }

    private class UpdateTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // Get settings of data plan and used data error
            String dataPlanSetting =
                    mSharedPreferences.getString(SettingsFragment.PREF_KEY_DATA_PLAN, "0");
            String usedDataErrorSetting =
                    mSharedPreferences.getString(SettingsFragment.PREF_KEY_USED_DATA_ERROR, "0");
            long dataPlan = Long.valueOf(dataPlanSetting) * 1024 * 1024;
            long usedDataError = (long) (Float.valueOf(usedDataErrorSetting) * 1024 * 1024);

            Date now = new Date();
            SharedPreferences.Editor editor = mSharedPreferences.edit();

            // Calculate mobile data usage of today
            long usedToday = TrafficUtil.getTotalMobileDataBytes(getContext(),
                    TrafficUtil.getStartOfDay(now), TrafficUtil.getEndOfDay(now));
            editor.putString(sPanelValuePrefKeys[0], TrafficUtil.getReadableValue(usedToday));

            // Calculate mobile data usage of this month, and update preferences
            long usedThisMonth = TrafficUtil.getTotalMobileDataBytes(getContext(),
                    TrafficUtil.getStartOfMonth(now), TrafficUtil.getEndOfMonth(now));
            editor.putString(SettingsFragment.PREF_KEY_USED_DATA_IN_LOG,
                    String.valueOf(usedThisMonth / (1024.0 * 1024.0)));
            usedThisMonth += usedDataError;
            editor.putString(SettingsFragment.PREF_KEY_USED_DATA,
                    String.format(Locale.getDefault(), "%.2f", usedThisMonth / (1024.0 * 1024.0)));
            editor.putString(sPanelValuePrefKeys[1], TrafficUtil.getReadableValue(usedThisMonth));

            long leftThisMonth = dataPlan - usedThisMonth;
            editor.putString(sPanelValuePrefKeys[2], TrafficUtil.getReadableValue(leftThisMonth));

            Calendar calendar = Calendar.getInstance();
            int daysLeft = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    - calendar.get(Calendar.DAY_OF_MONTH);
            editor.putString(sPanelValuePrefKeys[3],
                    String.valueOf(daysLeft) + (daysLeft > 1 ? " Days" : " Day"));

            float usagePercentage = 100f * usedThisMonth / dataPlan;
            editor.putFloat(sUsagePercentagePrefKey, usagePercentage);

            editor.apply();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isCancelled()) {
                updatePanels(false);
            }
        }
    }
}
