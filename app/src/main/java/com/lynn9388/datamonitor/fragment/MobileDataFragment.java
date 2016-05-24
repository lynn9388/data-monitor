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
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.lynn9388.datamonitor.R;
import com.lynn9388.datamonitor.util.NetworkUtil;
import com.lynn9388.datamonitor.util.TrafficUtil;

import java.util.ArrayList;
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

    private static String[] sNetworkUsagePrefKeys = {
            "pref_key_2g",
            "pref_key_3g",
            "pref_key_4g",
            "pref_key_left"
    };
    private static NetworkUtil.NetworkType[] sNetworkTypes = {
            NetworkUtil.NetworkType.NETWORK_TYPE_2G,
            NetworkUtil.NetworkType.NETWORK_TYPE_3G,
            NetworkUtil.NetworkType.NETWORK_TYPE_4G,
            null
    };
    private int[] mColors;

    private PieChart mChart;
    private View[] mPanels;

    private SharedPreferences mSharedPreferences;
    private UpdateDataTask mUpdateDataTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mobile_data, container, false);

        mChart = (PieChart) view.findViewById(R.id.pie_chart);
        mChart.setUsePercentValues(true);
        mChart.getLegend().setEnabled(false);
        mChart.setDescription("");
        mChart.setCenterTextSize(14f);
        mChart.animateY(3000, Easing.EasingOption.EaseInOutQuad);

        mPanels = new View[sPanelViewIds.length];
        for (int i = 0; i < sPanelViewIds.length; i++) {
            mPanels[i] = view.findViewById(sPanelViewIds[i]);
        }

        mColors = new int[4];
        mColors[0] = ContextCompat.getColor(getContext(), R.color.color2GSend);
        mColors[1] = ContextCompat.getColor(getContext(), R.color.color3GSend);
        mColors[2] = ContextCompat.getColor(getContext(), R.color.color4GSend);
        mColors[3] = Color.GRAY;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        updateViews();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mUpdateDataTask = new UpdateDataTask();
        mUpdateDataTask.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        mUpdateDataTask.cancel(true);
    }

    private void updateViews() {
        mChart.setCenterText(generatePieCenterText());
        mChart.setData(generatePieData());
        mChart.invalidate();

        for (int i = 0; i < sPanelViewIds.length; i++) {
            TextView titleView = (TextView) mPanels[i].findViewById(R.id.title);
            TextView valueView = (TextView) mPanels[i].findViewById(R.id.value);
            titleView.setText(getString(sPanelTitles[i]));
            valueView.setText(mSharedPreferences.getString(sPanelValuePrefKeys[i], "--"));
        }
    }

    private SpannableString generatePieCenterText() {
        float usagePercentage = mSharedPreferences.getFloat(sUsagePercentagePrefKey, 0f);
        if (0 < usagePercentage && usagePercentage < 50) {
            mColors[3] = Color.GREEN;
        } else if (usagePercentage < 75) {
            mColors[3] = Color.YELLOW;
        } else {
            mColors[3] = Color.RED;
        }

        String percentageValue = String.format(Locale.getDefault(), "%.2f %%", usagePercentage);
        SpannableString s = new SpannableString(percentageValue + "\n"
                + getString(R.string.chart_mobile_data_message));
        s.setSpan(new RelativeSizeSpan(2f), 0, percentageValue.length(), 0);
        s.setSpan(new ForegroundColorSpan(mColors[3]), 0, s.length(), 0);
        return s;
    }

    private PieData generatePieData() {
        ArrayList<Entry> entries1 = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();

        int index = 0;
        int[] colors = new int[4];
        for (int i = 0; i < sNetworkUsagePrefKeys.length; i++) {
            long dataUsage = mSharedPreferences.getLong(sNetworkUsagePrefKeys[i], 0L);
            colors[i] = mColors[i];
            if (dataUsage != 0) {
                entries1.add(new Entry(dataUsage, index));
                if (i != 3) {
                    xVals.add(sNetworkTypes[i].toString());
                } else {
                    xVals.add(getString(R.string.chart_mobile_data_left));
                }
                colors[index] = mColors[i];
                index++;
            }
        }

        PieDataSet dataSet = new PieDataSet(entries1, "Mobile Data");

        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(xVals, dataSet);
        pieData.setValueFormatter(new PercentFormatter());

        return pieData;
    }

    private class UpdateDataTask extends AsyncTask<Void, Void, Void> {
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

            long usedToday = 0;
            long usedThisMonth = 0;
            for (int i = 0; i < sNetworkTypes.length - 1; i++) {
                long dataUsage = TrafficUtil.getTotalDataUsage(getContext(), sNetworkTypes[i],
                        TrafficUtil.getStartOfDay(now), TrafficUtil.getEndOfDay(now));
                usedToday += dataUsage;

                dataUsage = TrafficUtil.getTotalDataUsage(getContext(), sNetworkTypes[i],
                        TrafficUtil.getStartOfMonth(now), TrafficUtil.getEndOfMonth(now));
                editor.putLong(sNetworkUsagePrefKeys[i], dataUsage);
                usedThisMonth += dataUsage;
            }

            editor.putString(sPanelValuePrefKeys[0], TrafficUtil.getReadableValue(usedToday));
            editor.putString(SettingsFragment.PREF_KEY_USED_DATA_IN_LOG,
                    String.valueOf(usedThisMonth / (1024.0 * 1024.0)));
            usedThisMonth += usedDataError;
            editor.putString(SettingsFragment.PREF_KEY_USED_DATA,
                    String.format(Locale.getDefault(), "%.2f", usedThisMonth / (1024.0 * 1024.0)));
            editor.putString(sPanelValuePrefKeys[1], TrafficUtil.getReadableValue(usedThisMonth));

            long leftThisMonth = dataPlan - usedThisMonth;
            editor.putLong(sNetworkUsagePrefKeys[3], leftThisMonth);
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
                updateViews();
            }
        }
    }
}
