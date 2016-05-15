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
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
import android.os.IBinder;
import android.util.Log;

import com.lynn9388.datamonitor.dao.DaoMaster;
import com.lynn9388.datamonitor.dao.DaoSession;
import com.lynn9388.datamonitor.dao.TrafficLog;
import com.lynn9388.datamonitor.dao.TrafficLogDao;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NetworkService extends Service {
    private static final String TAG = NetworkReceiver.class.getName();

    private TrafficLogDao mTrafficLogDao;
    private LogTimerTask mLogTimerTask;

    private long mLastMobileRxBytes;
    private long mLastMobileTxBytes;
    private long mLastTotalRxBytes;
    private long mLastTotalTxBytes;

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Create network log service");
        DaoMaster.DevOpenHelper helper =
                new DaoMaster.DevOpenHelper(this, MainActivity.DATABASE_NAME, null);
        SQLiteDatabase database = helper.getWritableDatabase();
        DaoMaster master = new DaoMaster(database);
        DaoSession session = master.newSession();
        mTrafficLogDao = session.getTrafficLogDao();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroy network log service");
        mLogTimerTask.cancel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLastMobileRxBytes = TrafficStats.getMobileRxBytes();
        mLastMobileTxBytes = TrafficStats.getMobileTxBytes();
        mLastTotalRxBytes = TrafficStats.getTotalRxBytes();
        mLastTotalTxBytes = TrafficStats.getTotalTxBytes();

        mLogTimerTask = new LogTimerTask();
        new Timer().scheduleAtFixedRate(mLogTimerTask, 10 * 1000, 10 * 1000);
        return START_STICKY;
    }

    private final class LogTimerTask extends TimerTask {
        @Override
        public void run() {
            long currentMobileRxBytes = TrafficStats.getMobileRxBytes();
            long currentMobileTxBytes = TrafficStats.getMobileTxBytes();
            long currentTotalRxBytes = TrafficStats.getTotalRxBytes();
            long currentTotalTxBytes = TrafficStats.getTotalTxBytes();

            TrafficLog trafficLog = null;
            if (currentMobileRxBytes == 0 && currentMobileTxBytes == 0) {
                trafficLog = new TrafficLog(null, new Date(), 0, 0,
                        currentTotalRxBytes - mLastTotalRxBytes,
                        currentTotalTxBytes - mLastTotalTxBytes,
                        currentTotalRxBytes, currentTotalTxBytes);
            } else {
                trafficLog = new TrafficLog(null, new Date(),
                        currentMobileRxBytes - mLastMobileRxBytes,
                        currentMobileTxBytes - mLastMobileTxBytes,
                        0, 0, currentTotalRxBytes, currentTotalTxBytes);
            }
            mTrafficLogDao.insert(trafficLog);

            mLastMobileRxBytes = currentMobileRxBytes;
            mLastMobileTxBytes = currentMobileTxBytes;
            mLastTotalRxBytes = currentTotalRxBytes;
            mLastTotalTxBytes = currentTotalTxBytes;
        }
    }
}
