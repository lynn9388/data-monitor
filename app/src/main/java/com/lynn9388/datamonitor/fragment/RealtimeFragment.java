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


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.lynn9388.datamonitor.R;
import com.lynn9388.datamonitor.RealTimeAdapter;
import com.lynn9388.datamonitor.dao.App;
import com.lynn9388.datamonitor.dao.AppDao;
import com.lynn9388.datamonitor.util.AppUtil;
import com.lynn9388.datamonitor.util.DatabaseUtil;
import com.lynn9388.datamonitor.util.TrafficUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class RealTimeFragment extends Fragment implements OnChartValueSelectedListener {
    private static Handler mHandler;
    private Context mContext;
    private LineChart mChart;
    private RealTimeAdapter mRealTimeAdapter;
    private int[] mColors;
    private String[] mDataTypes;
    private float[] mBytes;
    private long mLastTotalRxBytes;
    private long mLastTotalTxBytes;
    private List<App> mApps;
    private List<DataUsage> mAppLogs;
    private TimerTask mTimerTask;
    private TimerTask mUpdateAppTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();

        View view = inflater.inflate(R.layout.fragment_real_time, container, false);

        mChart = (LineChart) view.findViewById(R.id.line_chart);
        initChart();

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        mRealTimeAdapter = new RealTimeAdapter(mContext);
        recyclerView.setAdapter(mRealTimeAdapter);

        mColors = new int[]{
                ContextCompat.getColor(mContext, R.color.color0),
                ContextCompat.getColor(mContext, R.color.color1),
                ContextCompat.getColor(mContext, R.color.color2),
                ContextCompat.getColor(mContext, R.color.color3)
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
                    mRealTimeAdapter.notifyDataSetChanged();
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

        mUpdateAppTask = new TimerTask() {
            @Override
            public void run() {
                AppDao appDao = DatabaseUtil.getDaoSession(mContext).getAppDao();
                mApps = appDao.queryBuilder().list();
            }
        };

        mLastTotalRxBytes = TrafficStats.getTotalRxBytes();
        mLastTotalTxBytes = TrafficStats.getTotalTxBytes();

        new Timer().scheduleAtFixedRate(mTimerTask, 1000, 1000);
        new Timer().scheduleAtFixedRate(mUpdateAppTask, 0, 30 * 1000);
        mAppLogs = new ArrayList<>();
        initAppLogs();
    }

    @Override
    public void onPause() {
        super.onPause();
        mUpdateAppTask.cancel();
        mTimerTask.cancel();
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
        mChart.setNoDataText("");
        mChart.setDescription("");
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);
        mChart.setDrawGridBackground(false);

        mChart.setData(new LineData());

        Legend legend = mChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);

        int axisColor = ContextCompat.getColor(mContext, R.color.colorAccent);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextColor(axisColor);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setSpaceBetweenLabels(5);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(axisColor);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                return TrafficUtil.getReadableValue((long) value);
            }
        });

        mChart.getAxisRight().setEnabled(false);
    }

    private void updateData() {
        long currentMobileRxBytes = TrafficStats.getMobileRxBytes();
        long currentMobileTxBytes = TrafficStats.getMobileTxBytes();
        long currentTotalRxBytes = TrafficStats.getTotalRxBytes();
        long currentTotalTxBytes = TrafficStats.getTotalTxBytes();
        if (currentMobileRxBytes == 0 && currentMobileTxBytes == 0) {
            mBytes[2] = (currentTotalRxBytes - mLastTotalRxBytes);
            mBytes[3] = (currentTotalTxBytes - mLastTotalTxBytes);
        } else {
            mBytes[0] = (currentTotalRxBytes - mLastTotalRxBytes);
            mBytes[1] = (currentTotalTxBytes - mLastTotalTxBytes);
        }
        mLastTotalRxBytes = currentTotalRxBytes;
        mLastTotalTxBytes = currentTotalTxBytes;

        for (DataUsage log : mAppLogs) {
            log.mSendBytes = TrafficStats.getUidTxBytes(log.mUid) - log.mSendBytes;
            log.mReceiveBytes = TrafficStats.getUidRxBytes(log.mUid) - log.mReceiveBytes;
            if (log.mSendBytes != 0 && log.mReceiveBytes != 0) {
                mRealTimeAdapter.addItem(log.mPackageName, log.mSendBytes, log.mReceiveBytes);
            }
        }
        mRealTimeAdapter.sortDataset();

        initAppLogs();
    }

    private void initAppLogs() {
        if (mApps != null) {
            mAppLogs.clear();
            for (App app : mApps) {
                try {
                    int uid = AppUtil.getUid(mContext, app.getPackageName());
                    long sendBytes = TrafficStats.getUidTxBytes(uid);
                    long receiveBytes = TrafficStats.getUidRxBytes(uid);
                    mAppLogs.add(new DataUsage(uid, app.getPackageName(), sendBytes, receiveBytes));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
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

    private class DataUsage {
        private int mUid;
        private String mPackageName;
        private long mSendBytes;
        private long mReceiveBytes;

        private DataUsage(int uid, String packageName, long sendBytes, long receiveBytes) {
            mUid = uid;
            mPackageName = packageName;
            mSendBytes = sendBytes;
            mReceiveBytes = receiveBytes;
        }
    }
}
