/*
 * NetworkReceiver
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.lynn9388.datamonitor.util.NetworkUtil;

public class NetworkReceiver extends BroadcastReceiver {
    public static final String PREF_KEY_NETWORK_CONNECTED = "pref_key_network_connected";
    private static final String TAG = NetworkReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            editor.putBoolean(PREF_KEY_NETWORK_CONNECTED, false);
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean netwrokConnected = preferences.getBoolean(PREF_KEY_NETWORK_CONNECTED, false);

            if (!netwrokConnected && NetworkUtil.isNetworkConnected(context)) {
                Log.d(TAG, "Network state changed: disconnected -> connected");
                editor.putBoolean(PREF_KEY_NETWORK_CONNECTED, true);
                context.startService(new Intent(context, NetworkService.class));
            } else if (netwrokConnected && !NetworkUtil.isNetworkConnected(context)) {
                Log.d(TAG, "Network state changed: connected -> disconnected");
                editor.putBoolean(PREF_KEY_NETWORK_CONNECTED, false);
                context.stopService(new Intent(context, NetworkService.class));
            }
        } else if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
            String packageName = intent.getData().getEncodedSchemeSpecificPart();
            Log.d(TAG, "Package added: " + uid + " " + packageName);
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
            String packageName = intent.getData().getEncodedSchemeSpecificPart();
            Log.d(TAG, "Package removed: " + uid + " " + packageName);
        }

        editor.apply();
    }
}
