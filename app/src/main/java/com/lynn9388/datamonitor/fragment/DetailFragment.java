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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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
import com.lynn9388.datamonitor.util.TrafficUtil;

import java.util.ArrayList;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {
    private static String[] sDataUsedPrefKeys = {"pref_key_mobile_down", "pref_key_mobile_up",
            "pref_key_wifi_down", "pref_key_wifi_up"};
    private static int[] sDataTypeStringIds =
            {R.string.mobile_down, R.string.mobile_up, R.string.wifi_down, R.string.wifi_up};
    private static int[] sRowIds = {R.id.row0, R.id.row1, R.id.row2, R.id.row3};
    private PieChart mChart;
    private View[] rows = new View[4];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        mChart = (PieChart) view.findViewById(R.id.pie_chart);
        mChart.setUsePercentValues(true);
        mChart.getLegend().setEnabled(false);
        mChart.setDescription("");
        mChart.setCenterTextSize(10f);
        mChart.animateY(3000, Easing.EasingOption.EaseInOutQuad);

        for (int i = 0; i < sRowIds.length; i++) {
            rows[i] = view.findViewById(sRowIds[i]);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updataData();
    }

    private void updataData() {
        mChart.setCenterText(generatePieCenterText());
        mChart.setData(generatePieData());

        Legend legend = mChart.getLegend();
        int[] colors = legend.getColors();
        String[] labels = legend.getLabels();
        IPieDataSet dataSet = mChart.getData().getDataSet();
        float[] angles = mChart.getDrawAngles();

        for (int i = 0; i < rows.length; i++) {
            View color = rows[i].findViewById(R.id.color);
            TextView dataType = (TextView) rows[i].findViewById(R.id.data_type);
            TextView total = (TextView) rows[i].findViewById(R.id.total);
            TextView percent = (TextView) rows[i].findViewById(R.id.percent);

            color.setBackgroundColor(colors[i]);
            dataType.setText(labels[i]);
            total.setText(TrafficUtil.getReadableValue((long) dataSet.getEntryForIndex(i).getVal()));
            percent.setText(String.format(Locale.getDefault(), "%.2f", angles[i] / 3.6));
        }
    }

    private SpannableString generatePieCenterText() {
        SharedPreferences localPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        long total = 0L;
        for (String key : sDataUsedPrefKeys) {
            total += localPreferences.getLong(key, 0L);
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

        for (int stringId : sDataTypeStringIds) {
            xVals.add(getString(stringId));
        }

        SharedPreferences localPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        for (int i = 0; i < sDataTypeStringIds.length; i++) {
            entries1.add(new Entry(localPreferences.getLong(sDataUsedPrefKeys[i], 2L), i));
        }

        PieDataSet dataSet = new PieDataSet(entries1, "Detail");
        int colors[] = {Color.rgb(217, 80, 138), Color.rgb(254, 149, 7),
                Color.rgb(106, 167, 134), Color.rgb(53, 194, 209)};
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(xVals, dataSet);
        pieData.setValueFormatter(new PercentFormatter());

        return pieData;
    }
}
