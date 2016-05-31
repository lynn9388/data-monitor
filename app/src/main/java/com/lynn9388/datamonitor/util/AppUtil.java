/*
 * AppUtil
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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.lynn9388.datamonitor.dao.App;
import com.lynn9388.datamonitor.dao.AppDao;
import com.lynn9388.datamonitor.dao.AppLog;
import com.lynn9388.datamonitor.dao.DaoSession;

import java.util.ArrayList;
import java.util.List;

public class AppUtil {
    private static final String TAG = AppUtil.class.getSimpleName();

    public static List<String> getPackageNamesWithInternetPermission(Context context) {
        List<String> packageNames = new ArrayList<>();
        PackageManager manager = context.getPackageManager();
        List<PackageInfo> infos = manager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        for (PackageInfo info : infos) {
            String[] permissions = info.requestedPermissions;
            if (permissions != null) {
                for (String permission : permissions) {
                    if ("android.permission.INTERNET".equals(permission)) {
                        packageNames.add(info.packageName);
                        break;
                    }
                }
            }
        }
        return packageNames;
    }

    public static int getUid(Context context, String packageName)
            throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).uid;
    }

    public static void insertApp(Context context, String packageName) {
        DaoSession daoSession = DatabaseUtil.getDaoSession(context);
        daoSession.insert(new App(null, packageName, 0, 0));
    }

    public static void deleteAppAndLogs(Context context, String packageName) {
        DaoSession daoSession = DatabaseUtil.getDaoSession(context);
        AppDao appDao = daoSession.getAppDao();
        List<App> apps = appDao.queryBuilder()
                .where(AppDao.Properties.PackageName.eq(packageName))
                .list();
        for (App app : apps) {
            app.resetLogs();
            List<AppLog> logs = app.getLogs();
            for (AppLog log : logs) {
                daoSession.delete(log);
            }
            daoSession.delete(app);
        }
    }

    public static void updateApps(Context context) {
        DaoSession daoSession = DatabaseUtil.getDaoSession(context);
        AppDao appDao = daoSession.getAppDao();

        List<App> apps = appDao.queryBuilder().list();
        List<String> packageNames = getPackageNamesWithInternetPermission(context);

        for (App app : apps) {
            String packageName = app.getPackageName();
            if (packageNames.contains(packageName)) {
                packageNames.remove(packageNames.indexOf(packageName));
            } else {
                deleteAppAndLogs(context, packageName);
            }
        }

        for (String packageName : packageNames) {
            daoSession.insert(new App(null, packageName, 0, 0));
        }
    }
}
