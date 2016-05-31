/*
 * RealTimeAdapter
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

package com.lynn9388.datamonitor.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lynn9388.datamonitor.R;
import com.lynn9388.datamonitor.util.TrafficUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RealTimeAdapter extends RecyclerView.Adapter<AppHolder> {
    protected Context mContext;
    protected List<AppInfo> mDataSet;

    public RealTimeAdapter(Context context) {
        mContext = context;
        mDataSet = new ArrayList<>();
    }

    @Override
    public AppHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_data_usage, parent, false);
        return new AppHolder(mContext, view) {
            @Override
            String getActionBarTitle() {
                return mContext.getString(R.string.nav_real_time_title);
            }
        };
    }

    @Override
    public void onBindViewHolder(AppHolder holder, int position) {
        AppInfo appInfo = mDataSet.get(position);
        holder.mIconView.setImageDrawable(appInfo.icon);
        holder.mAppNameView.setText(appInfo.mAppName);
        holder.mPackageNameView.setText(appInfo.mPackageName);
        holder.mDataSendView.setText("↑" + TrafficUtil.getReadableValue(appInfo.mSendBytes));
        holder.mDataReceiveView.setText("↓" + TrafficUtil.getReadableValue(appInfo.mReceiveBytes));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void addItem(String packageName, long sendBytes, long receiveBytes) {
        AppInfo target = null;
        for (AppInfo info : mDataSet) {
            if (info.mPackageName.equals(packageName)) {
                target = info;
                break;
            }
        }
        if (target == null) {
            target = new AppInfo(mContext, packageName, sendBytes, receiveBytes);
            mDataSet.add(target);
        } else {
            target.mSendBytes += sendBytes;
            target.mReceiveBytes += receiveBytes;
        }
    }

    public void sortDataSet() {
        Collections.sort(mDataSet, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo lhs, AppInfo rhs) {
                Long lValue = lhs.mSendBytes + lhs.mReceiveBytes;
                Long rValue = rhs.mSendBytes + rhs.mReceiveBytes;
                return rValue.compareTo(lValue);
            }
        });
    }
}
