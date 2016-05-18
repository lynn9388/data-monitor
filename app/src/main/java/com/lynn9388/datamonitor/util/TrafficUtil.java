/*
 * TrafficUtil
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

package com.lynn9388.datamonitor.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.lynn9388.datamonitor.MainActivity;
import com.lynn9388.datamonitor.dao.DaoMaster;
import com.lynn9388.datamonitor.dao.DaoSession;
import com.lynn9388.datamonitor.dao.TrafficLog;
import com.lynn9388.datamonitor.dao.TrafficLogDao;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrafficUtil {
    private static TrafficLogDao getTrafficLogDao(Context context) {
        DaoMaster.DevOpenHelper helper =
                new DaoMaster.DevOpenHelper(context, MainActivity.DATABASE_NAME, null);
        SQLiteDatabase database = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        DaoSession daoSession = daoMaster.newSession();
        return daoSession.getTrafficLogDao();
    }

    public static Date getStartOfDay(Date date) {
        return DateUtils.truncate(date, Calendar.DATE);
    }

    public static Date getEndOfDay(Date date) {
        return DateUtils.addMilliseconds(DateUtils.ceiling(date, Calendar.DATE), -1);
    }

    public static Date getStartOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return getStartOfDay(calendar.getTime());
    }

    public static Date getEndOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return getEndOfDay(calendar.getTime());
    }

    public static String getReadableValue(long bytes) {
        double value = Math.abs(bytes);
        String unit = "";
        if (value < 1024) {
            unit = "B";
        } else if ((value /= 1024.0) < 1024) {
            unit = "KB";
        } else if ((value /= 1024.0) < 1024) {
            unit = "MB";
        } else if ((value /= 1024.0) < 1024) {
            unit = "GB";
        }
        return String.format(Locale.getDefault(), "%.2f %s", value, unit);
    }

    public static long getTotalMobileDataBytes(Context context, Date start, Date end) {
        TrafficLogDao trafficLogDao = getTrafficLogDao(context);
        List<TrafficLog> logs = trafficLogDao.queryBuilder()
                .where(TrafficLogDao.Properties.Time.between(start, end))
                .list();

        long bytes = 0;
        for (TrafficLog log : logs) {
            bytes += log.getMobileRxBytes() + log.getMobileTxBytes();
        }
        return bytes;
    }
}
