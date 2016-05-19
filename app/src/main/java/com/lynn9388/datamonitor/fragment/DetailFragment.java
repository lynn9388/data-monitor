/*
 * DetailFragment
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
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.lynn9388.datamonitor.R;
import com.lynn9388.datamonitor.dao.TrafficLog;
import com.lynn9388.datamonitor.util.TrafficUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {
    private static int[] sRowViewIds = {R.id.row0, R.id.row1, R.id.row2, R.id.row3};
    private static int[] sDataTypes = {R.string.mobile_down, R.string.mobile_up,
            R.string.wifi_down, R.string.wifi_up};
    private static String[] sDataUsedPrefKeys = {"pref_key_mobile_down", "pref_key_mobile_up",
            "pref_key_wifi_down", "pref_key_wifi_up"};

    private PieChart mChart;
    private View[] mRows = new View[4];

    private SharedPreferences mSharedPreferences;
    private UpdateDataTask mUpdateDataTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        mChart = (PieChart) view.findViewById(R.id.pie_chart);
        mChart.setUsePercentValues(true);
        mChart.getLegend().setEnabled(false);
        mChart.setDescription("");
        mChart.setCenterTextSize(14f);
        mChart.animateY(3000, Easing.EasingOption.EaseInOutQuad);

        for (int i = 0; i < sRowViewIds.length; i++) {
            mRows[i] = view.findViewById(sRowViewIds[i]);
        }

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

        Legend legend = mChart.getLegend();
        int[] colors = legend.getColors();
        String[] labels = legend.getLabels();
        IPieDataSet dataSet = mChart.getData().getDataSet();
        float[] angles = mChart.getDrawAngles();

        for (int i = 0; i < mRows.length; i++) {
            View color = mRows[i].findViewById(R.id.color);
            TextView dataType = (TextView) mRows[i].findViewById(R.id.data_type);
            TextView total = (TextView) mRows[i].findViewById(R.id.total);
            TextView percent = (TextView) mRows[i].findViewById(R.id.percent);

            color.setBackgroundColor(colors[i]);
            dataType.setText(labels[i]);
            total.setText(TrafficUtil.getReadableValue((long) dataSet.getEntryForIndex(i).getVal()));
            percent.setText(String.format(Locale.getDefault(), "%.2f", angles[i] / 3.6));
        }
    }

    private SpannableString generatePieCenterText() {
        long total = 0L;
        for (String key : sDataUsedPrefKeys) {
            total += mSharedPreferences.getLong(key, 0L);
        }
        String readableValue = TrafficUtil.getReadableValue(total);
        SpannableString s = new SpannableString(readableValue + "\nTotal this month");
        s.setSpan(new RelativeSizeSpan(2f), 0, readableValue.length(), 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 0, s.length(), 0);
        return s;
    }

    private PieData generatePieData() {
        ArrayList<Entry> entries1 = new ArrayList<>();
        ArrayList<String> xVals = new ArrayList<>();

        for (int stringId : sDataTypes) {
            xVals.add(getString(stringId));
        }

        for (int i = 0; i < sDataTypes.length; i++) {
            entries1.add(new Entry(mSharedPreferences.getLong(sDataUsedPrefKeys[i], 0L), i));
        }

        PieDataSet dataSet = new PieDataSet(entries1, "Detail");
        int colors[] = {Color.rgb(217, 80, 138), Color.rgb(254, 149, 7),
                Color.rgb(106, 167, 134), Color.rgb(53, 194, 209)};
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
            Date now = new Date();
            Date start = TrafficUtil.getStartOfMonth(now);
            Date end = TrafficUtil.getEndOfMonth(now);
            List<TrafficLog> trafficLogs = TrafficUtil.getTrafficLogs(getContext(), start, end);

            long mobileDown = 0;
            long mobileUp = 0;
            long wifiDown = 0;
            long wifiUp = 0;
            for (TrafficLog log : trafficLogs) {
                mobileDown += log.getMobileRxBytes();
                mobileUp += log.getMobileTxBytes();
                wifiDown += log.getWifiRxBytes();
                wifiUp += log.getWifiTxBytes();
            }

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putLong(sDataUsedPrefKeys[0], mobileDown);
            editor.putLong(sDataUsedPrefKeys[1], mobileUp);
            editor.putLong(sDataUsedPrefKeys[2], wifiDown);
            editor.putLong(sDataUsedPrefKeys[3], wifiUp);
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
