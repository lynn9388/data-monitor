/*
 * AppsAdapter
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lynn9388.datamonitor.R;

public class AppsAdapter extends RealTimeAdapter {
    private static final String TAG = AppsAdapter.class.getSimpleName();

    public AppsAdapter(Context context) {
        super(context);
    }

    @Override
    public AppHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_data_usage, parent, false);
        return new AppHolder(mContext, view) {
            @Override
            String getActionBarTitle() {
                return mContext.getString(R.string.nav_apps_title);
            }
        };
    }

    public boolean contains(String packageName) {
        boolean contains = false;
        for (AppInfo info : mDataSet) {
            if (info.mPackageName.equals(packageName)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    @Override
    public void addItem(String packageName, long totalSendBytes, long totalReceiveBytes) {
        mDataSet.add(new AppInfo(mContext, packageName, totalSendBytes, totalReceiveBytes));
    }

    public void updateItem(String packageName, long totalSendBytes, long totalReceiveBytes) {
        for (AppInfo info : mDataSet) {
            if (info.mPackageName.equals(packageName)) {
                info.mSendBytes = totalSendBytes;
                info.mReceiveBytes = totalReceiveBytes;
                break;
            }
        }
    }
}
