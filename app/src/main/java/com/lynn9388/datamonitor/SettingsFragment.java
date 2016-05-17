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

package com.lynn9388.datamonitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.lynn9388.datamonitor.util.TrafficUtil;

import java.util.Date;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String PREF_KEY_DATA_PLAN = "pref_key_data_plan";
    public static final String PREF_KEY_USED_DATA = "pref_key_used_data";
    public static final String PREF_KEY_USED_DATA_ERROR = "pref_key_used_data_error";
    public static final String PREF_KEY_VERSION = "pref_key_version";

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
        findPreference(PREF_KEY_DATA_PLAN).setSummary(dataPlan + "MB");

        String usedData = sharedPreferences.getString(PREF_KEY_USED_DATA, "0");
        findPreference(PREF_KEY_USED_DATA).setSummary(usedData + "MB");

        String verionName = BuildConfig.VERSION_NAME;
        findPreference(PREF_KEY_VERSION).setSummary("Version " + verionName);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PREF_KEY_DATA_PLAN.equals(key) || PREF_KEY_USED_DATA.equals(key)) {
            String value = sharedPreferences.getString(key, "0");
            findPreference(key).setSummary(value + "MB");

            if (PREF_KEY_USED_DATA.equals(key)) {
                long usedDataInLog = TrafficUtil.getTotalMobileDataBytes(getActivity(),
                        TrafficUtil.getStartOfDay(new Date()), TrafficUtil.getEndOfDay(new Date()));
                long usedDataError = Long.valueOf(value) - usedDataInLog;
                sharedPreferences.edit()
                        .putString(PREF_KEY_USED_DATA_ERROR, String.valueOf(usedDataError))
                        .apply();
            }
        }
    }
}
