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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.lynn9388.datamonitor.NetworkService;
import com.lynn9388.datamonitor.R;
import com.lynn9388.datamonitor.util.NetworkUtil;
import com.lynn9388.datamonitor.util.TrafficUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class MobileDataFragment extends Fragment {
    public static String sDataLeftThisMonthPrefKey = "pref_key_data_left_this_month";
    private static String[] sNetworkUsageTodayPrefKeys = {
            "pref_key_2g_today", "pref_key_3g_today", "pref_key_4g_today"
    };
    private static String[] sNetworkUsageThisMonthPrefKeys = {
            "pref_key_2g_this_month", "pref_key_3g_this_month", "pref_key_4g_this_month"
    };
    private static String sUsagePercentagePrefKey = "pref_key_usage_percentage";
    private static String[] sPanelValuePrefKeys = {
            "pref_key_panel0", "pref_key_panel1", "pref_key_panel2", "pref_key_panel3"
    };

    private static NetworkUtil.NetworkType[] sNetworkTypes = {
            NetworkUtil.NetworkType.NETWORK_TYPE_2G,
            NetworkUtil.NetworkType.NETWORK_TYPE_3G,
            NetworkUtil.NetworkType.NETWORK_TYPE_4G
    };

    private PieChart mChart;
    private View[] mPanels;

    private int[] mColors;
    private SharedPreferences mSharedPreferences;

    private Handler mHandler;
    private TimerTask mTimerTask;

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

        mPanels = new View[4];
        int[] panelViewIds = {R.id.panel0, R.id.panel1, R.id.panel2, R.id.panel3};
        int[] titleStringIds = {
                R.string.used_today, R.string.used_this_month,
                R.string.remaining_this_month, R.string.till_next_settlement
        };
        for (int i = 0; i < mPanels.length; i++) {
            mPanels[i] = view.findViewById(panelViewIds[i]);
            TextView titleView = (TextView) mPanels[i].findViewById(R.id.title);
            titleView.setText(getString(titleStringIds[i]));
        }
        mPanels[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = getString(R.string.dialog_used_today_title);
                showDialog(title, sNetworkUsageTodayPrefKeys);
            }
        });
        mPanels[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = getString(R.string.dialog_used_this_month_title);
                showDialog(title, sNetworkUsageThisMonthPrefKeys);
            }
        });

        mColors = new int[4];
        mColors[0] = ContextCompat.getColor(getContext(), R.color.color2GSend);
        mColors[1] = ContextCompat.getColor(getContext(), R.color.color3GSend);
        mColors[2] = ContextCompat.getColor(getContext(), R.color.color4GSend);
        mColors[3] = Color.GRAY;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViews();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    updateViews();
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
        new Timer().scheduleAtFixedRate(mTimerTask, 0, NetworkService.LOG_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimerTask.cancel();
        mHandler.removeMessages(0);
    }

    private void updateData() {
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
        for (int i = 0; i < sNetworkTypes.length; i++) {
            long dataUsage = TrafficUtil.getTotalDataUsage(getContext(), sNetworkTypes[i],
                    TrafficUtil.getStartOfDay(now), TrafficUtil.getEndOfDay(now));
            editor.putLong(sNetworkUsageTodayPrefKeys[i], dataUsage);
            usedToday += dataUsage;

            dataUsage = TrafficUtil.getTotalDataUsage(getContext(), sNetworkTypes[i],
                    TrafficUtil.getStartOfMonth(now), TrafficUtil.getEndOfMonth(now));
            editor.putLong(sNetworkUsageThisMonthPrefKeys[i], dataUsage);
            usedThisMonth += dataUsage;
        }

        editor.putString(sPanelValuePrefKeys[0], TrafficUtil.getReadableValue(usedToday));
        editor.putString(SettingsFragment.PREF_KEY_USED_DATA_IN_LOG,
                String.valueOf(usedThisMonth / (1024f * 1024f)));
        usedThisMonth += usedDataError;
        editor.putString(SettingsFragment.PREF_KEY_USED_DATA,
                String.format(Locale.getDefault(), "%.2f", usedThisMonth / (1024f * 1024f)));
        editor.putString(sPanelValuePrefKeys[1], TrafficUtil.getReadableValue(usedThisMonth));

        long leftThisMonth = dataPlan - usedThisMonth;
        if (leftThisMonth < 0) {
            leftThisMonth = 0;
        }
        editor.putLong(sDataLeftThisMonthPrefKey, leftThisMonth);
        editor.putString(sPanelValuePrefKeys[2], TrafficUtil.getReadableValue(leftThisMonth));

        Calendar calendar = Calendar.getInstance();
        int daysLeft = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                - calendar.get(Calendar.DAY_OF_MONTH);
        editor.putString(sPanelValuePrefKeys[3],
                String.valueOf(daysLeft) + (daysLeft > 1 ? " Days" : " Day"));

        float usagePercentage = 100f * usedThisMonth / dataPlan;
        editor.putFloat(sUsagePercentagePrefKey, usagePercentage);

        editor.apply();
    }

    private void updateViews() {
        mChart.setCenterText(generatePieCenterText());
        mChart.setData(generatePieData());
        mChart.invalidate();

        for (int i = 0; i < mPanels.length; i++) {
            TextView valueView = (TextView) mPanels[i].findViewById(R.id.value);
            valueView.setText(mSharedPreferences.getString(sPanelValuePrefKeys[i], "--"));
        }
    }

    private SpannableString generatePieCenterText() {
        float usagePercentage = mSharedPreferences.getFloat(sUsagePercentagePrefKey, 0f);
        int warningPercent = mSharedPreferences.getInt(SettingsFragment.PREF_KEY_WARNING_PERCENT, 0);
        if (0 < usagePercentage && usagePercentage < warningPercent * 0.8) {
            mColors[3] = ContextCompat.getColor(getContext(), R.color.colorSufficiency);
        } else if (usagePercentage < warningPercent) {
            mColors[3] = ContextCompat.getColor(getContext(), R.color.colorNormal);
        } else {
            mColors[3] = ContextCompat.getColor(getContext(), R.color.colorLack);
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
        for (int i = 0; i < 4; i++) {
            long dataUsage;
            if (i != 3) {
                dataUsage = mSharedPreferences.getLong(sNetworkUsageThisMonthPrefKeys[i], 0L);
            } else {
                dataUsage = mSharedPreferences.getLong(sDataLeftThisMonthPrefKey, 0L);
            }
            colors[i] = mColors[i];
            if (dataUsage > 0) {
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

    private void showDialog(String title, String[] prefKeys) {
        Context context = getContext();
        final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(context);

        long[] dataUsages = new long[3];
        for (int i = 0; i < dataUsages.length; i++) {
            dataUsages[i] = mSharedPreferences.getLong(prefKeys[i], 0L);
        }

        adapter.add(new MaterialSimpleListItem.Builder(context)
                .content(TrafficUtil.getReadableValue(dataUsages[0]))
                .icon(R.drawable.ic_2g)
                .iconPaddingDp(8)
                .build());
        adapter.add(new MaterialSimpleListItem.Builder(context)
                .content(TrafficUtil.getReadableValue(dataUsages[1]))
                .icon(R.drawable.ic_3g)
                .iconPaddingDp(8)
                .build());
        adapter.add(new MaterialSimpleListItem.Builder(context)
                .content(TrafficUtil.getReadableValue(dataUsages[2]))
                .icon(R.drawable.ic_4g)
                .iconPaddingDp(8)
                .build());

        new MaterialDialog.Builder(context)
                .title(title)
                .adapter(adapter, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView,
                                            int which, CharSequence text) {
                        MaterialSimpleListItem item = adapter.getItem(which);
                    }
                })
                .show();
    }
}
