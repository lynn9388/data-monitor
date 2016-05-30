/*
 * AppInfo
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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class AppInfo {
    public Drawable icon;
    public String mAppName;
    public String mPackageName;
    public Long mSendBytes;
    public long mReceiveBytes;

    public AppInfo(Context context, String packageName, long sendBytes, long receiveBytes) {
        PackageManager packageManager = context.getPackageManager();
        try {
            icon = packageManager.getApplicationIcon(packageName);
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            mAppName = String.valueOf(packageManager.getApplicationLabel(applicationInfo));
        } catch (PackageManager.NameNotFoundException e) {
            icon = null;
            mAppName = "";
            e.printStackTrace();
        }
        mPackageName = packageName;
        mSendBytes = sendBytes;
        mReceiveBytes = receiveBytes;
    }
}
