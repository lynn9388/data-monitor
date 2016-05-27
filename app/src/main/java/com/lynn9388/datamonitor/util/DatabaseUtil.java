/*
 * DatabaseUtil
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

public class DatabaseUtil {
    private static final String TAG = DatabaseUtil.class.getSimpleName();
    private static DaoMaster mDaoMaster;

    public static DaoSession getDaoSession(Context context) {
        if (mDaoMaster == null) {
            DaoMaster.DevOpenHelper helper =
                    new DaoMaster.DevOpenHelper(context, MainActivity.DATABASE_NAME, null);
            SQLiteDatabase database = helper.getWritableDatabase();
            mDaoMaster = new DaoMaster(database);
        }
        return mDaoMaster.newSession();
    }
}
