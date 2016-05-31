/*
 * AppDetailFragment
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

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lynn9388.datamonitor.MainActivity;
import com.lynn9388.datamonitor.R;
import com.lynn9388.datamonitor.adapter.AppHolder;
import com.lynn9388.datamonitor.dao.App;
import com.lynn9388.datamonitor.dao.AppDao;
import com.lynn9388.datamonitor.dao.AppLog;
import com.lynn9388.datamonitor.dao.AppLogDao;
import com.lynn9388.datamonitor.util.DatabaseUtil;
import com.lynn9388.datamonitor.util.NetworkUtil;
import com.lynn9388.datamonitor.util.TrafficUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppDetailFragment extends TrafficDetailFragment {
    private String mActionBarTitle;
    private String mPackageName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        mActionBarTitle = bundle.getString(AppHolder.ACTION_BAR_TITLE);
        mPackageName = bundle.getString(AppHolder.PACKAGE_NAME);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void updateData() {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

        AppDao appDao = DatabaseUtil.getDaoSession(mContext).getAppDao();
        List<App> apps = appDao.queryBuilder()
                .where(AppDao.Properties.PackageName.eq(mPackageName))
                .list();

        List<AppLog> logs = null;
        if (apps.size() == 1) {
            AppLogDao appLogDao = DatabaseUtil.getDaoSession(mContext).getAppLogDao();
            logs = appLogDao.queryBuilder()
                    .where(AppLogDao.Properties.AppId.eq(apps.get(0).getId()))
                    .orderAsc(AppLogDao.Properties.Time)
                    .list();
        }

        Date start = null;
        if (logs.size() > 0) {
            start = TrafficUtil.getStartOfHour(logs.get(0).getTime());
        }

        int valueSize = getLabels().length;
        for (int i = 0; i < logs.size(); i++) {
            Date end = TrafficUtil.getOneHourLater(start);
            float[] values = new float[valueSize];
            while (i < logs.size() && logs.get(i).getTime().before(end)) {
                analyseLog(logs.get(i), values);
                i++;
            }
            addEntry(format.format(start), values);
            start = end;
            i--;
        }
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) mContext).getSupportActionBar().setTitle(mActionBarTitle);
    }

    @Override
    void analyseLog(Object log, float[] values) {
        AppLog appLog = (AppLog) log;
        String networkType = appLog.getNetworkType();
        if (networkType.equals(NetworkUtil.NetworkType.NETWORK_TYPE_WIFI.toString())) {
            values[0] += appLog.getSendBytes();
            values[1] += appLog.getReceiveBytes();
        } else if (networkType.equals(NetworkUtil.NetworkType.NETWORK_TYPE_2G.toString())) {
            values[2] += appLog.getSendBytes();
            values[3] += appLog.getReceiveBytes();
        } else if (networkType.equals(NetworkUtil.NetworkType.NETWORK_TYPE_3G.toString())) {
            values[4] += appLog.getSendBytes();
            values[5] += appLog.getReceiveBytes();
        } else if (networkType.equals(NetworkUtil.NetworkType.NETWORK_TYPE_4G.toString())) {
            values[6] += appLog.getSendBytes();
            values[7] += appLog.getReceiveBytes();
        }
    }

    @Override
    int[] getColors() {
        return new int[]{
                ContextCompat.getColor(mContext, R.color.colorWifiSend),
                ContextCompat.getColor(mContext, R.color.colorWifiReceive),
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
                NetworkUtil.NetworkType.NETWORK_TYPE_WIFI.toString() + "↑",
                NetworkUtil.NetworkType.NETWORK_TYPE_WIFI.toString() + "↓",
                NetworkUtil.NetworkType.NETWORK_TYPE_2G.toString() + "↑",
                NetworkUtil.NetworkType.NETWORK_TYPE_2G.toString() + "↓",
                NetworkUtil.NetworkType.NETWORK_TYPE_3G.toString() + "↑",
                NetworkUtil.NetworkType.NETWORK_TYPE_3G.toString() + "↓",
                NetworkUtil.NetworkType.NETWORK_TYPE_4G.toString() + "↑",
                NetworkUtil.NetworkType.NETWORK_TYPE_4G.toString() + "↓"
        };
    }
}
