/*
 * SettingsFragment
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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.lynn9388.datamonitor.BuildConfig;
import com.lynn9388.datamonitor.NetworkReceiver;
import com.lynn9388.datamonitor.NetworkService;
import com.lynn9388.datamonitor.R;
import com.lynn9388.datamonitor.introduction.IntroductionActivity;
import com.lynn9388.datamonitor.preference.WarningSettingPreference;
import com.lynn9388.datamonitor.util.NetworkUtil;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String PREF_KEY_ENABLE_DATA_MONITORING = "pref_key_enable_data_monitoring";
    public static final String PREF_KEY_DATA_PLAN = "pref_key_data_plan";
    public static final String PREF_KEY_USED_DATA = "pref_key_used_data";
    public static final String PREF_KEY_USED_DATA_IN_LOG = "pref_key_used_data_in_log";
    public static final String PREF_KEY_USED_DATA_ERROR = "pref_key_used_data_error";

    public static final String PREF_KEY_WARNING_PERCENT = "pref_key_warning_percent";

    private static final String PREF_KEY_INTRODUCTION = "pref_key_introduction";
    private static final String PREF_KEY_VERSION = "pref_key_version";
    private static final String TAG = SettingsFragment.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        String dataPlan = sharedPreferences.getString(PREF_KEY_DATA_PLAN, "0");
        findPreference(PREF_KEY_DATA_PLAN).setSummary(dataPlan + " MB");

        String usedData = sharedPreferences.getString(PREF_KEY_USED_DATA, "0");
        findPreference(PREF_KEY_USED_DATA).setSummary(usedData + " MB");

        findPreference(PREF_KEY_INTRODUCTION).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(getActivity(), IntroductionActivity.class));
                        return false;
                    }
                });

        String verionName = BuildConfig.VERSION_NAME;
        findPreference(PREF_KEY_VERSION).setSummary("Version " + verionName);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PREF_KEY_ENABLE_DATA_MONITORING.equals(key)) {
            Context context = getActivity();
            boolean enabled = sharedPreferences.getBoolean(key, true);
            PackageManager packageManager = context.getPackageManager();
            ComponentName componentName = new ComponentName(context, NetworkReceiver.class);
            if (enabled) {
                Log.d(TAG, "NetworkReceiver state: enabled");
                packageManager.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
                if (NetworkUtil.isNetworkConnected(context)) {
                    sharedPreferences.edit()
                            .putBoolean(NetworkReceiver.PREF_KEY_NETWORK_CONNECTED, true)
                            .apply();
                    context.startService(new Intent(context, NetworkService.class));
                }
            } else {
                Log.d(TAG, "NetworkReceiver state: disabled");
                packageManager.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
                context.stopService(new Intent(context, NetworkService.class));
            }
        } else if (PREF_KEY_DATA_PLAN.equals(key)) {
            String value = sharedPreferences.getString(key, "0");
            findPreference(key).setSummary(value + " MB");

            ((WarningSettingPreference) findPreference(PREF_KEY_WARNING_PERCENT)).updateWarningValue();
        } else if (PREF_KEY_USED_DATA.equals(key)) {
            String value = sharedPreferences.getString(key, "0");
            findPreference(key).setSummary(value + " MB");

            String usedDataInLog = sharedPreferences.getString(PREF_KEY_USED_DATA_IN_LOG, "0");
            float usedDataError = Float.valueOf(value) - Float.valueOf(usedDataInLog);
            sharedPreferences.edit()
                    .putString(PREF_KEY_USED_DATA_ERROR, String.valueOf(usedDataError))
                    .apply();
        }
    }
}
