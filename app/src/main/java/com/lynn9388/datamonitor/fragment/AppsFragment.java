/*
 * AppsFragment
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lynn9388.datamonitor.NetworkService;
import com.lynn9388.datamonitor.R;
import com.lynn9388.datamonitor.adapter.AppsAdapter;
import com.lynn9388.datamonitor.dao.App;
import com.lynn9388.datamonitor.util.DatabaseUtil;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppsFragment extends Fragment {
    private Context mContext;

    private AppsAdapter mAppsAdapter;

    private Handler mHandler;
    private TimerTask mTimerTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();

        View view = inflater.inflate(R.layout.fragment_apps, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mContext).build());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        mAppsAdapter = new AppsAdapter(mContext);
        recyclerView.setAdapter(mAppsAdapter);

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
                    mAppsAdapter.notifyDataSetChanged();
                }
            }
        };

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                updateData();
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
        List<App> apps = DatabaseUtil.getDaoSession(mContext).getAppDao().queryBuilder().list();
        for (App app : apps) {
            if (app.getTotalSendBytes() != 0 || app.getTotalReceiveBytes() != 0) {
                if (!mAppsAdapter.contains(app.getPackageName())) {
                    mAppsAdapter.addItem(app.getPackageName(), app.getTotalSendBytes(),
                            app.getTotalReceiveBytes());
                } else {
                    mAppsAdapter.updateItem(app.getPackageName(), app.getTotalSendBytes(),
                            app.getTotalReceiveBytes());
                }
            }
        }
        mAppsAdapter.sortDataSet();
        mHandler.sendEmptyMessage(0);
    }
}
