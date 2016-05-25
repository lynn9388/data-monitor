/*
 * NetworkService
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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.IBinder;
import android.util.Log;

import com.lynn9388.datamonitor.dao.App;
import com.lynn9388.datamonitor.dao.AppLog;
import com.lynn9388.datamonitor.dao.AppLogDao;
import com.lynn9388.datamonitor.dao.DaoSession;
import com.lynn9388.datamonitor.dao.TrafficLog;
import com.lynn9388.datamonitor.dao.TrafficLogDao;
import com.lynn9388.datamonitor.util.AppUtil;
import com.lynn9388.datamonitor.util.DatabaseUtil;
import com.lynn9388.datamonitor.util.NetworkUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.dao.query.Query;

public class NetworkService extends Service {
    public static final int LOG_INTERVAL = 10 * 1000;
    private static final String TAG = NetworkReceiver.class.getSimpleName();

    private LogTimerTask mLogTimerTask;

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroy network log service");
        mLogTimerTask.cancel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start network log service");
        AppUtil.updateApps(this);

        mLogTimerTask = new LogTimerTask(this);
        new Timer().scheduleAtFixedRate(mLogTimerTask, LOG_INTERVAL, LOG_INTERVAL);
        return START_STICKY;
    }

    private final class LogTimerTask extends TimerTask {
        private Context mContext;

        private TrafficLogDao mTrafficLogDao;
        private AppLogDao mAppLogDao;
        private Query mAppQuery;

        private TrafficLog mTrafficLog;
        private Map<App, AppLog> mAppLogs;

        private long currentTotalTxBytes;
        private long currentTotalRxBytes;

        public LogTimerTask(Context context) {
            mContext = context;

            DaoSession daoSession = DatabaseUtil.getDaoSession(context);
            mTrafficLogDao = daoSession.getTrafficLogDao();
            mAppLogDao = daoSession.getAppLogDao();
            mAppQuery = daoSession.getAppDao().queryBuilder().build();

            mAppLogs = new HashMap<>();

            currentTotalTxBytes = TrafficStats.getTotalTxBytes();
            currentTotalRxBytes = TrafficStats.getTotalRxBytes();
            initLog();
        }

        @Override
        public void run() {
            currentTotalTxBytes = TrafficStats.getTotalTxBytes();
            currentTotalRxBytes = TrafficStats.getTotalRxBytes();

            // Calculate total network usage after interval
            mTrafficLog.setSendBytes(currentTotalTxBytes - mTrafficLog.getSendBytes());
            mTrafficLog.setReceiveBytes(currentTotalRxBytes - mTrafficLog.getReceiveBytes());
            mTrafficLogDao.insert(mTrafficLog);

            // Calculate network usage of each app, don't save log if it doesn't use network
            for (Map.Entry<App, AppLog> entry : mAppLogs.entrySet()) {
                try {
                    int uid = AppUtil.getUid(mContext, entry.getKey().getPackageName());
                    AppLog log = entry.getValue();
                    long sendBytes = TrafficStats.getUidTxBytes(uid) - log.getSendBytes();
                    long receiveBytes = TrafficStats.getUidRxBytes(uid) - log.getReceiveBytes();
                    if (sendBytes != 0 || receiveBytes != 0) {
                        log.setSendBytes(sendBytes);
                        log.setReceiveBytes(receiveBytes);
                        mAppLogDao.insert(log);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

            initLog();
        }

        private void initLog() {
            Date now = new Date();

            String networkType;
            if (TrafficStats.getMobileTxBytes() == 0 && TrafficStats.getMobileRxBytes() == 0) {
                networkType = NetworkUtil.NetworkType.NETWORK_TYPE_WIFI.toString();
            } else {
                networkType = NetworkUtil.getMobileNetworkType(mContext).toString();
            }

            mTrafficLog = new TrafficLog(null, now, currentTotalTxBytes,
                    currentTotalRxBytes, networkType);

            mAppLogs.clear();
            List<App> apps = mAppQuery.forCurrentThread().list();
            for (App app : apps) {
                try {
                    int uid = AppUtil.getUid(mContext, app.getPackageName());
                    mAppLogs.put(app, new AppLog(null, now, app.getId(),
                            TrafficStats.getUidTxBytes(uid),
                            TrafficStats.getUidRxBytes(uid),
                            networkType));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
