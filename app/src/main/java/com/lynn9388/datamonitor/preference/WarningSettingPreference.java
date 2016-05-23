/*
 * WarningSettingPreference
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

package com.lynn9388.datamonitor.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import com.lynn9388.datamonitor.fragment.SettingsFragment;

import java.util.Locale;

public class WarningSettingPreference extends SeekBarPreference {
    private SharedPreferences mSharedPreferences;
    private int mDataPlan;

    public WarningSettingPreference(Context context) {
        super(context);
        init();
    }

    public WarningSettingPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WarningSettingPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WarningSettingPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mDataPlan = Integer
                .valueOf(mSharedPreferences.getString(SettingsFragment.PREF_KEY_DATA_PLAN, "0"));
    }

    @Override
    public void onBindView(View view) {
        setSummary(getWarningValue(getProgress()));
        super.onBindView(view);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        super.onProgressChanged(seekBar, progress, fromUser);
        setSummary(getWarningValue(progress));
    }

    private String getWarningValue(int progress) {
        return String.format(Locale.getDefault(), "%d MB %d%%", mDataPlan * progress / 100, progress);
    }

    public void updateWarningValue() {
        mDataPlan = Integer
                .valueOf(mSharedPreferences.getString(SettingsFragment.PREF_KEY_DATA_PLAN, "0"));
        setSummary(getWarningValue(getProgress()));
    }
}
