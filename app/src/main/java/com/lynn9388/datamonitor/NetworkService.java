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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.lynn9388.datamonitor.dao.App;
import com.lynn9388.datamonitor.dao.AppDao;
import com.lynn9388.datamonitor.dao.AppLog;
import com.lynn9388.datamonitor.dao.AppLogDao;
import com.lynn9388.datamonitor.dao.DaoSession;
import com.lynn9388.datamonitor.dao.TrafficLog;
import com.lynn9388.datamonitor.dao.TrafficLogDao;
import com.lynn9388.datamonitor.fragment.MobileDataFragment;
import com.lynn9388.datamonitor.fragment.SettingsFragment;
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
    private static String sWarnedPrefKey = "pref_key_warned";

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
        private String networkType;

        private AppDao mAppDao;
        private AppLogDao mAppLogDao;
        private TrafficLogDao mTrafficLogDao;

        private Query mAppQuery;

        private int mLogCount;
        private TrafficLog mTrafficLog;
        private Map<App, Integer> mAppUids;
        private Map<App, AppLog> mAppLogs;

        private long mCurrentTotalTxBytes;
        private long mCurrentTotalRxBytes;
        private long mCurrentTxBytes;
        private long mCurrentRxBytes;

        private LogTimerTask(Context context) {
            mContext = context;

            DaoSession daoSession = DatabaseUtil.getDaoSession(context);
            mAppDao = daoSession.getAppDao();
            mAppLogDao = daoSession.getAppLogDao();
            mTrafficLogDao = daoSession.getTrafficLogDao();
            mAppQuery = daoSession.getAppDao().queryBuilder().build();

            mLogCount = 0;
            mAppUids = new HashMap<>();
            mAppLogs = new HashMap<>();

            mCurrentTotalTxBytes = TrafficStats.getTotalTxBytes();
            mCurrentTotalRxBytes = TrafficStats.getTotalRxBytes();
            initLog();
        }

        @Override
        public void run() {
            mLogCount++;
            mCurrentTotalTxBytes = TrafficStats.getTotalTxBytes();
            mCurrentTotalRxBytes = TrafficStats.getTotalRxBytes();
            mCurrentTxBytes = mCurrentTotalTxBytes - mTrafficLog.getSendBytes();
            mCurrentRxBytes = mCurrentTotalRxBytes - mTrafficLog.getReceiveBytes();

            // Calculate total network usage after interval
            mTrafficLog.setSendBytes(mCurrentTxBytes);
            mTrafficLog.setReceiveBytes(mCurrentRxBytes);
            mTrafficLogDao.insert(mTrafficLog);

            // Calculate network usage of each app, don't save log if it doesn't use network
            for (Map.Entry<App, AppLog> entry : mAppLogs.entrySet()) {
                App app = entry.getKey();
                int uid = mAppUids.get(app);
                AppLog log = entry.getValue();
                long sendBytes = TrafficStats.getUidTxBytes(uid) - log.getSendBytes();
                long receiveBytes = TrafficStats.getUidRxBytes(uid) - log.getReceiveBytes();
                if (sendBytes != 0 || receiveBytes != 0) {
                    app.setTotalSendBytes(app.getTotalSendBytes() + sendBytes);
                    app.setTotalReceiveBytes(app.getTotalReceiveBytes() + receiveBytes);
                    mAppDao.insertOrReplace(app);
                    log.setSendBytes(sendBytes);
                    log.setReceiveBytes(receiveBytes);
                    mAppLogDao.insert(log);
                }
            }

            if (!networkType.equals(NetworkUtil.NetworkType.NETWORK_TYPE_WIFI.toString())) {
                checkDataUsage();
            }
            initLog();
        }

        private void initLog() {
            if (mLogCount % 10 == 0) {
                Log.d(TAG, "Uid updated.");
                mAppUids.clear();
                List<App> apps = mAppQuery.forCurrentThread().list();
                for (App app : apps) {
                    try {
                        int uid = AppUtil.getUid(mContext, app.getPackageName());
                        mAppUids.put(app, uid);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            Date now = new Date();

            if (TrafficStats.getMobileTxBytes() == 0 && TrafficStats.getMobileRxBytes() == 0) {
                networkType = NetworkUtil.NetworkType.NETWORK_TYPE_WIFI.toString();
            } else {
                networkType = NetworkUtil.getMobileNetworkType(mContext).toString();
            }

            mTrafficLog = new TrafficLog(null, now, mCurrentTotalTxBytes,
                    mCurrentTotalRxBytes, networkType);

            mAppLogs.clear();
            for (Map.Entry<App, Integer> entry : mAppUids.entrySet()) {
                App app = entry.getKey();
                int uid = entry.getValue();
                mAppLogs.put(app, new AppLog(null, now, app.getId(),
                        TrafficStats.getUidTxBytes(uid),
                        TrafficStats.getUidRxBytes(uid),
                        networkType));
            }
        }

        private void checkDataUsage() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String dataPlanSetting = preferences.getString(SettingsFragment.PREF_KEY_DATA_PLAN, "0");
            long dataPlan = Long.valueOf(dataPlanSetting) * 1024 * 1024;
            int warningPercent = preferences.getInt(SettingsFragment.PREF_KEY_WARNING_PERCENT, 0);
            long dataLeftThisMonth =
                    preferences.getLong(MobileDataFragment.sDataLeftThisMonthPrefKey, 0L);
            boolean warned = preferences.getBoolean(sWarnedPrefKey, false);

            SharedPreferences.Editor editor = preferences.edit();
            dataLeftThisMonth -= (mCurrentTxBytes + mCurrentRxBytes);
            editor.putLong(MobileDataFragment.sDataLeftThisMonthPrefKey, dataLeftThisMonth);

            if (100f * dataLeftThisMonth / dataPlan <= (100 - warningPercent)) {
                if (!warned) {
                    editor.putBoolean(sWarnedPrefKey, true);
                    showNotification();
                }
            } else if (warned) {
                editor.putBoolean(sWarnedPrefKey, false);
            }

            editor.apply();
        }

        private void showNotification() {
            Notification.Builder mBuilder =
                    new Notification.Builder(mContext)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(getString(R.string.data_warning_title))
                            .setContentText(getString(R.string.data_warning_content));

            Intent resultIntent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, mBuilder.build());
        }
    }
}
