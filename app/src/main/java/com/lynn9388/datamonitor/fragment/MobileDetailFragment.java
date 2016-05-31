/*
 * MobileDetailFragment
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

import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.lynn9388.datamonitor.R;
import com.lynn9388.datamonitor.dao.TrafficLog;
import com.lynn9388.datamonitor.util.NetworkUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class MobileDetailFragment extends TrafficDetailFragment {
    @Override
    void analyseLog(Object log, float[] values) {
        TrafficLog trafficLog = (TrafficLog) log;
        String networkType = trafficLog.getNetworkType();
        if (networkType.equals(NetworkUtil.NetworkType.NETWORK_TYPE_2G.toString())) {
            values[0] += trafficLog.getSendBytes();
            values[1] += trafficLog.getReceiveBytes();
        } else if (networkType.equals(NetworkUtil.NetworkType.NETWORK_TYPE_3G.toString())) {
            values[2] += trafficLog.getSendBytes();
            values[3] += trafficLog.getReceiveBytes();
        } else if (networkType.equals(NetworkUtil.NetworkType.NETWORK_TYPE_4G.toString())) {
            values[4] += trafficLog.getSendBytes();
            values[5] += trafficLog.getReceiveBytes();
        }
    }

    @Override
    int[] getColors() {
        return new int[]{
                ContextCompat.getColor(mContext, R.color.color2GSend),
                ContextCompat.getColor(mContext, R.color.color2GReceive),
                ContextCompat.getColor(mContext, R.color.color3GSend),
                ContextCompat.getColor(mContext, R.color.color3GReceive),
                ContextCompat.getColor(mContext, R.color.color4GSend),
                ContextCompat.getColor(mContext, R.color.color4GReceive)
        };
    }

    @Override
    String[] getLabels() {
        return new String[]{
                NetworkUtil.NetworkType.NETWORK_TYPE_2G.toString() + "↑",
                NetworkUtil.NetworkType.NETWORK_TYPE_2G.toString() + "↓",
                NetworkUtil.NetworkType.NETWORK_TYPE_3G.toString() + "↑",
                NetworkUtil.NetworkType.NETWORK_TYPE_3G.toString() + "↓",
                NetworkUtil.NetworkType.NETWORK_TYPE_4G.toString() + "↑",
                NetworkUtil.NetworkType.NETWORK_TYPE_4G.toString() + "↓"
        };
    }
}
