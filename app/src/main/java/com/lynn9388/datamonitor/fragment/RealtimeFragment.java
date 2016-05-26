/*
 * RealtimeFragment
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


import android.graphics.Color;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.lynn9388.datamonitor.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class RealtimeFragment extends Fragment implements OnChartValueSelectedListener {
    private LineChart mChart;

    private int[] mColors;
    private String[] mDataTypes;
    private float[] mBytes;

    private long mLastTotalRxBytes;
    private long mLastTotalTxBytes;

    private Handler mHandler;
    private TimerTask mTimerTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_realtime, container, false);

        mChart = (LineChart) view.findViewById(R.id.line_chart);
        initChart();

        mColors = new int[]{
                ContextCompat.getColor(getContext(), R.color.color0),
                ContextCompat.getColor(getContext(), R.color.color1),
                ContextCompat.getColor(getContext(), R.color.color2),
                ContextCompat.getColor(getContext(), R.color.color3)
        };

        mDataTypes = new String[]{
                getString(R.string.mobile_down),
                getString(R.string.mobile_up),
                getString(R.string.wifi_down),
                getString(R.string.wifi_up)
        };

        mBytes = new float[4];

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    addEntry();
                }
            }
        };

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                updateData();
                mHandler.sendEmptyMessage(0);
            }
        };

        mLastTotalRxBytes = TrafficStats.getTotalRxBytes();
        mLastTotalTxBytes = TrafficStats.getTotalTxBytes();
        new Timer().scheduleAtFixedRate(mTimerTask, 1000, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimerTask.cancel();
        mHandler.removeMessages(0);
    }

    private void initChart() {
        mChart.setOnChartValueSelectedListener(this);
        mChart.setNoDataText("");
        mChart.setDescription("");
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);

        LineData data = new LineData();
        data.setValueTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        mChart.setData(data);

        Legend legend = mChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setSpaceBetweenLabels(5);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        leftAxis.setAxisMinValue(0f);
        leftAxis.setAxisMaxValue(2048f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void updateData() {
        long currentMobileRxBytes = TrafficStats.getMobileRxBytes();
        long currentMobileTxBytes = TrafficStats.getMobileTxBytes();
        long currentTotalRxBytes = TrafficStats.getTotalRxBytes();
        long currentTotalTxBytes = TrafficStats.getTotalTxBytes();
        if (currentMobileRxBytes == 0 && currentMobileTxBytes == 0) {
            mBytes[2] = (currentTotalRxBytes - mLastTotalRxBytes) / 1024f;
            mBytes[3] = (currentTotalTxBytes - mLastTotalTxBytes) / 1024f;
        } else {
            mBytes[0] = (currentTotalRxBytes - mLastTotalRxBytes) / 1024f;
            mBytes[1] = (currentTotalTxBytes - mLastTotalTxBytes) / 1024f;
        }
        mLastTotalRxBytes = currentTotalRxBytes;
        mLastTotalTxBytes = currentTotalTxBytes;
    }

    private void addEntry() {
        LineData data = mChart.getData();
        if (data != null) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            data.addXValue(format.format(new Date()));

            for (int i = 0; i < mBytes.length; i++) {
                ILineDataSet dataSet = data.getDataSetByIndex(i);
                if (dataSet == null) {
                    dataSet = createSet(i);
                    data.addDataSet(dataSet);
                }
                data.addEntry(new Entry(mBytes[i], dataSet.getEntryCount()), i);
            }

            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(100);
            mChart.moveViewToX(data.getXValCount() - 101);
        }
    }

    private LineDataSet createSet(int i) {
        LineDataSet set = new LineDataSet(null, mDataTypes[i]);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawCircles(false);
        set.setColor(mColors[i]);
        set.setLineWidth(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(mColors[i]);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
