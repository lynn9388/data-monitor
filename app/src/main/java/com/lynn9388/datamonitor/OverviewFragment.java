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

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lynn9388.datamonitor.util.TrafficUtil;
import com.philjay.circledisplay.CircleDisplay;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class OverviewFragment extends Fragment {
    private CircleDisplay mCircleDisplay;
    private View mPanel1;
    private View mPanel2;
    private View mPanel3;
    private View mPanel4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View rootView = getView();
        mCircleDisplay = (CircleDisplay) rootView.findViewById(R.id.data_usage_view);
        mPanel1 = rootView.findViewById(R.id.panel1);
        mPanel2 = rootView.findViewById(R.id.panel2);
        mPanel3 = rootView.findViewById(R.id.panel3);
        mPanel4 = rootView.findViewById(R.id.panel4);
    }

    @Override
    public void onResume() {
        super.onResume();
        initPanel(mPanel1, R.string.used_today, "--");
        initPanel(mPanel2, R.string.used_this_month, "--");
        initPanel(mPanel3, R.string.remaining_this_month, "--");
        initPanel(mPanel4, R.string.till_next_settlement, "--");

        new SetValueTask().execute();
    }

    private void initPanel(View panel, int title, String value) {
        TextView titleView = (TextView) panel.findViewById(R.id.title);
        TextView valueView = (TextView) panel.findViewById(R.id.value);
        titleView.setText(getString(title));
        valueView.setText(value);
    }

    private class SetValueTask extends AsyncTask<Void, Void, Map> {
        @Override
        protected Map doInBackground(Void... params) {
            Map<Integer, Object> result = new ArrayMap<>();

            // Get settings of data plan and used data error
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getContext());
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
            result.put(R.id.panel1, usedToday);

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
            result.put(R.id.panel2, usedThisMonth);

            long leftThisMonth = dataPlan - usedThisMonth;
            result.put(R.id.panel3, leftThisMonth);

            Calendar calendar = Calendar.getInstance();
            int daysLeft = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    - calendar.get(Calendar.DAY_OF_MONTH);
            result.put(R.id.panel4, daysLeft);

            float usagePercentage = (float) (100.0 * usedThisMonth / dataPlan);
            result.put(R.id.data_usage_view, usagePercentage);

            return result;
        }

        @Override
        protected void onPostExecute(Map map) {
            super.onPostExecute(map);
            float usagePercentage = (float) map.get(R.id.data_usage_view);
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

            setValue(mPanel1, getReadableValue((long) map.get(R.id.panel1)));
            setValue(mPanel2, getReadableValue((long) map.get(R.id.panel2)));
            setValue(mPanel3, getReadableValue((long) map.get(R.id.panel3)));

            int daysLeft = (int) map.get(R.id.panel4);
            String value = String.valueOf(daysLeft) + (daysLeft > 1 ? " Days" : " Day");
            ((TextView) (mPanel4.findViewById(R.id.value))).setText(value);
        }

        private void setValue(View panel, String value) {
            TextView textView = (TextView) panel.findViewById(R.id.value);
            textView.setText(value);
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
