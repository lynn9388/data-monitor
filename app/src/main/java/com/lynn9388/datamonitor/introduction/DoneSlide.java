/*
 * DoneIntroduction
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

package com.lynn9388.datamonitor.introduction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

import com.lynn9388.datamonitor.MainActivity;

public class DoneSlide extends SlideFragment<DoneSlide> {
    @Override
    public void onClick(View v) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (!preferences.getBoolean(MainActivity.PREF_KEY_HAS_OPENED, false)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(MainActivity.PREF_KEY_HAS_OPENED, true);
            editor.apply();
            startActivity(new Intent(getContext(), MainActivity.class));
        }
        getActivity().finish();
    }
}
