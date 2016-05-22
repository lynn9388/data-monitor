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

/**
 * A simple {@link Fragment} subclass.
 */
public class RealtimeFragment extends Fragment implements OnChartValueSelectedListener {
    public static int[] sDataTypes = {R.string.mobile_down, R.string.mobile_up,
            R.string.wifi_down, R.string.wifi_up};
    private LineChart mChart;
    private int[] colors;
    private Thread thread;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_realtime, container, false);

        mChart = (LineChart) view.findViewById(R.id.line_chart);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setNoDataText("");
        mChart.setDescription("");
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(true);
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
        leftAxis.setGranularityEnabled(true);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        colors = new int[]{
                ContextCompat.getColor(getContext(), R.color.colorMobileDown),
                ContextCompat.getColor(getContext(), R.color.colorMobileUp),
                ContextCompat.getColor(getContext(), R.color.colorWifiDown),
                ContextCompat.getColor(getContext(), R.color.colorWifiUp)
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        final float[] bytes = new float[4];

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {

                    LineData data = mChart.getData();
                    if (data != null) {
                        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                        data.addXValue(format.format(new Date()));

                        for (int i = 0; i < bytes.length; i++) {
                            ILineDataSet dataSet = data.getDataSetByIndex(i);
                            if (dataSet == null) {
                                dataSet = createSet(i);
                                data.addDataSet(dataSet);
                            }
                            data.addEntry(new Entry(bytes[i], dataSet.getEntryCount()), i);
                        }

                        // let the chart know it's data has changed
                        mChart.notifyDataSetChanged();

                        // limit the number of visible entries
                        mChart.setVisibleXRangeMaximum(30);
                        // mChart.setVisibleYRange(30, AxisDependency.LEFT);

                        // move to the latest entry
                        mChart.moveViewToX(data.getXValCount() - 31);
                    }
                }
            }
        };

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    long lastTotalRxBytes = TrafficStats.getTotalRxBytes();
                    long lastTotalTxBytes = TrafficStats.getTotalTxBytes();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long currentMobileRxBytes = TrafficStats.getMobileRxBytes();
                    long currentMobileTxBytes = TrafficStats.getMobileTxBytes();
                    long currentTotalRxBytes = TrafficStats.getTotalRxBytes();
                    long currentTotalTxBytes = TrafficStats.getTotalTxBytes();
                    if (currentMobileRxBytes == 0 && currentMobileTxBytes == 0) {
                        bytes[2] = (currentTotalRxBytes - lastTotalRxBytes) / (1024f * 1024f);
                        bytes[3] = (currentTotalTxBytes - lastTotalTxBytes) / (1024f * 1024f);
                    } else {
                        bytes[0] = (currentTotalRxBytes - lastTotalRxBytes) / (1024f * 1024f);
                        bytes[1] = (currentTotalTxBytes - lastTotalTxBytes) / (1024f * 1024f);
                    }
                    handler.sendEmptyMessage(0);
                }
            }
        });
        thread.start();
    }

    private LineDataSet createSet(int i) {
        LineDataSet set = new LineDataSet(null, getString(sDataTypes[i]));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawCircles(false);
        set.setColor(colors[i]);
        set.setLineWidth(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(colors[i]);
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
