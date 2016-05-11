/*
 * ServiceIntroduction
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

import android.view.View;
import android.widget.Toast;

import com.github.paolorotolo.appintro.ISlidePolicy;
import com.lynn9388.datamonitor.R;

public class ServiceSlide extends SlideFragment<ServiceSlide> implements ISlidePolicy {
    @Override
    public boolean isPolicyRespected() {
        return true;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        Toast.makeText(getContext(), getString(R.string.not_start_service_error),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {

    }
}
