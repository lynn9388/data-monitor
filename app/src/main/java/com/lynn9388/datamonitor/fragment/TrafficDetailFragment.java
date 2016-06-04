/*
 * NetworkDetailFragment
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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.lynn9388.datamonitor.R;
import com.lynn9388.datamonitor.dao.TrafficLog;
import com.lynn9388.datamonitor.dao.TrafficLogDao;
import com.lynn9388.datamonitor.util.DatabaseUtil;
import com.lynn9388.datamonitor.util.TrafficUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class TrafficDetailFragment extends Fragment implements OnChartValueSelectedListener {
    protected Context mContext;
    protected Handler mHandler;
    private BarChart mChart;
    private Thread mThread;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();

        View view = inflater.inflate(R.layout.fragment_traffic_detail, container, false);
        mChart = (BarChart) view.findViewById(R.id.bar_chart);

        initChart();
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
                    mChart.notifyDataSetChanged();
                    mChart.animateX(3000);
                }
            }
        };

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateData();
            }
        });
        mThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mThread.interrupt();
        mHandler.removeMessages(0);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
    }

    @Override
    public void onNothingSelected() {
    }

    private void initChart() {
        mChart.setOnChartValueSelectedListener(this);
        mChart.setNoDataText(getString(R.string.no_network_data_message));
        mChart.setDescription("");
        mChart.setMaxVisibleValueCount(10);

        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(false);

        int axisColor = ContextCompat.getColor(mContext, R.color.colorAccent);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setAxisMinValue(0f);
        leftAxis.setTextColor(axisColor);
        leftAxis.setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                return TrafficUtil.getReadableValue((long) value);
            }
        });
        mChart.getAxisRight().setEnabled(false);

        XAxis xLabels = mChart.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.TOP);
        xLabels.setTextColor(axisColor);

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);
    }

    protected void updateData() {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

        Date start = TrafficUtil.getSeveralDaysAgo(new Date(), 30);
        start = TrafficUtil.getStartOfDay(start);

        TrafficLogDao trafficLogDao = DatabaseUtil.getDaoSession(mContext).getTrafficLogDao();
        List<TrafficLog> logs = trafficLogDao.queryBuilder()
                .where(TrafficLogDao.Properties.Time.ge(start))
                .orderAsc(TrafficLogDao.Properties.Time)
                .list();

        int valueSize = getLabels().length;
        boolean isUseful = false;
        for (int i = 0; i < logs.size(); i++) {
            Date end = TrafficUtil.getOneHourLater(start);
            float[] values = new float[valueSize];
            while (i < logs.size() && logs.get(i).getTime().before(end)) {
                analyseLog(logs.get(i), values);
                i++;
            }
            if (!isUseful) {
                for (Float value : values) {
                    if (value != 0) {
                        isUseful = true;
                        break;
                    }
                }
            }
            if (isUseful) {
                addEntry(format.format(start), values);
            }
            start = end;
            i--;
        }
        mHandler.sendEmptyMessage(0);
    }

    protected void addEntry(String xVal, float[] values) {
        BarData data = mChart.getData();
        if (data != null && data.getDataSetCount() > 0) {
            data.addXValue(xVal);

            BarDataSet dataSet = (BarDataSet) data.getDataSetByIndex(0);
            dataSet.addEntry(new BarEntry(values, dataSet.getEntryCount()));
            data.notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            List<String> xVals = new ArrayList<>();
            xVals.add(xVal);
            List<BarEntry> yVals = new ArrayList<>();
            yVals.add(new BarEntry(values, 0));
            BarDataSet dataSet = new BarDataSet(yVals, "");
            dataSet.setColors(getColors());
            dataSet.setStackLabels(getLabels());
            data = new BarData(xVals, dataSet);
            mChart.setData(data);
        }
    }

    abstract void analyseLog(Object log, float[] values);

    abstract int[] getColors();

    abstract String[] getLabels();
}
