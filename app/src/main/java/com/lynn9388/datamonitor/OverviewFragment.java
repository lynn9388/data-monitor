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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.philjay.circledisplay.CircleDisplay;

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
        CircleDisplay circleDisplay = (CircleDisplay) rootView.findViewById(R.id.data_usage_view);
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
    }

    private void initPanel(View panel, int title, String value) {
        TextView titleView = (TextView) panel.findViewById(R.id.title);
        TextView valueView = (TextView) panel.findViewById(R.id.value);
        titleView.setText(getString(title));
        valueView.setText(value);
    }
}
