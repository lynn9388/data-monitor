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

import com.lynn9388.datamonitor.dao.TrafficLog;
import com.lynn9388.datamonitor.dao.TrafficLogDao;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrafficUtil {
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

    public static Date getSeveralDaysAgo(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -days);
        return calendar.getTime();
    }

    public static Date getOneHourLater(Date date) {
        return new Date(date.getTime() + 3600000);
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

    public static long getTotalDataUsage(Context context, NetworkUtil.NetworkType networkType,
                                         Date start, Date end) {
        TrafficLogDao trafficLogDao = DatabaseUtil.getDaoSession(context).getTrafficLogDao();
        List<TrafficLog> logs = trafficLogDao.queryBuilder()
                .where(TrafficLogDao.Properties.NetworkType.eq(networkType.toString()),
                        TrafficLogDao.Properties.Time.between(start, end))
                .list();

        long bytes = 0;
        for (TrafficLog log : logs) {
            bytes += log.getSendBytes() + log.getReceiveBytes();
        }

        return bytes;
    }

    public static List<TrafficLog> getTrafficLogs(Context context, Date start, Date end) {
        TrafficLogDao trafficLogDao = DatabaseUtil.getDaoSession(context).getTrafficLogDao();
        return trafficLogDao.queryBuilder()
                .where(TrafficLogDao.Properties.Time.between(start, end))
                .list();
    }
}
