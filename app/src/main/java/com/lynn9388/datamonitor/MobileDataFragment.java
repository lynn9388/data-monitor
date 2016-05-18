/*
 * OverviewFragment
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

import android.content.Context;
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

import com.lynn9388.datamonitor.util.TrafficUtil;
import com.philjay.circledisplay.CircleDisplay;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class MobileDataFragment extends Fragment {
    private static String[] sPanelPrefKeys = {"pref_key_panel0", "pref_key_panel1",
            "pref_key_panel2", "pref_key_panel3"};
    private static int[] sPanelIds = {R.id.panel0, R.id.panel1, R.id.panel2, R.id.panel3};
    private static int[] sPanelTitles = {R.string.used_today, R.string.used_this_month,
            R.string.remaining_this_month, R.string.till_next_settlement};
    private CircleDisplay mCircleDisplay;
    private View[] mPanels;
    private SetValueTask setValueTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mobile_data, container, false);

        mCircleDisplay = (CircleDisplay) view.findViewById(R.id.data_usage_view);
        mPanels = new View[sPanelIds.length];
        for (int i = 0; i < sPanelIds.length; i++) {
            mPanels[i] = view.findViewById(sPanelIds[i]);
        }
        updatePanelValues();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setValueTask = new SetValueTask();
        setValueTask.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        setValueTask.cancel(true);
    }

    private void updatePanelValues() {
        SharedPreferences localPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        for (int i = 0; i < sPanelIds.length; i++) {
            TextView titleView = (TextView) mPanels[i].findViewById(R.id.title);
            TextView valueView = (TextView) mPanels[i].findViewById(R.id.value);
            titleView.setText(getString(sPanelTitles[i]));
            valueView.setText(localPreferences.getString(sPanelPrefKeys[i], "--"));
        }
    }

    private class SetValueTask extends AsyncTask<Void, Void, Float> {
        @Override
        protected Float doInBackground(Void... params) {
            // Save data for panels' loading
            SharedPreferences localPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor localEditor = localPreferences.edit();
            // Access data from settings
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getContext());

            // Get settings of data plan and used data error
            String dataPlanValue =
                    sharedPreferences.getString(SettingsFragment.PREF_KEY_DATA_PLAN, "0");
            String usedDataErrorValue =
                    sharedPreferences.getString(SettingsFragment.PREF_KEY_USED_DATA_ERROR, "0");
            long dataPlan = Long.valueOf(dataPlanValue) * 1024 * 1024;
            long usedDataError = (long) (Float.valueOf(usedDataErrorValue) * 1024 * 1024);

            Date now = new Date();

            // Calculate mobile data usage of today
            long usedToday = TrafficUtil.getTotalMobileDataBytes(getContext(),
                    TrafficUtil.getStartOfDay(now), TrafficUtil.getEndOfDay(now));
            localEditor.putString(sPanelPrefKeys[0], getReadableValue(usedToday));

            // Calculate mobile data usage of this month, and update preferences
            long usedThisMonth = TrafficUtil.getTotalMobileDataBytes(getContext(),
                    TrafficUtil.getStartOfMonth(now), TrafficUtil.getEndOfMonth(now));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SettingsFragment.PREF_KEY_USED_DATA_IN_LOG,
                    String.valueOf(usedThisMonth / (1024.0 * 1024.0)));
            usedThisMonth += usedDataError;
            editor.putString(SettingsFragment.PREF_KEY_USED_DATA,
                    String.format(Locale.getDefault(), "%.2f", usedThisMonth / (1024.0 * 1024.0)));
            editor.apply();
            localEditor.putString(sPanelPrefKeys[1], getReadableValue(usedThisMonth));

            long leftThisMonth = dataPlan - usedThisMonth;
            localEditor.putString(sPanelPrefKeys[2], getReadableValue(leftThisMonth));

            Calendar calendar = Calendar.getInstance();
            int daysLeft = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    - calendar.get(Calendar.DAY_OF_MONTH);
            localEditor.putString(sPanelPrefKeys[3],
                    String.valueOf(daysLeft) + (daysLeft > 1 ? " Days" : " Day"));

            localEditor.apply();

            return (float) (100.0 * usedThisMonth / dataPlan);
        }

        @Override
        protected void onPostExecute(Float usagePercentage) {
            if (!isCancelled()) {
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
                mCircleDisplay.showValue(usagePercentage, 100f, true);

                updatePanelValues();
            }
        }

        private String getReadableValue(long bytes) {
            String value;
            if (Math.abs(bytes) < 1024) {
                value = String.valueOf(bytes) + " B";
            } else if (Math.abs(bytes) < 1024 * 1024) {
                value = String.format(Locale.getDefault(), "%.2f K", bytes / 1024.0);
            } else {
                value = String.format(Locale.getDefault(), "%.2f M", bytes / (1024.0 * 1024.0));
            }
            return value;
        }
    }
}
