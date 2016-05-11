/*
 * IntroductionActivity
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

import android.graphics.Color;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.lynn9388.datamonitor.R;

public class IntroductionActivity extends AppIntro {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setGoBackLock(true);
        setColorTransitionsEnabled(true);
        setProgressButtonEnabled(false);
        setSeparatorColor(Color.TRANSPARENT);

        addSlide(AppIntroFragment.newInstance(getString(R.string.welcome_title),
                getString(R.string.welcome_message),
                R.drawable.ic_welcome,
                Color.parseColor("#009688")));
        addSlide(new CertificateSlide().getInstance(getString(R.string.install_certificate_title),
                R.drawable.ic_install_certificate,
                getString(R.string.install_certificate_button_start),
                Color.parseColor("#8BC34A")));
        addSlide(new ServiceSlide().getInstance(getString(R.string.start_service_title),
                R.drawable.ic_start_service,
                getString(R.string.start_service_button_start),
                Color.parseColor("#448AFF")));
        addSlide(new DoneSlide().getInstance(getString(R.string.complete_title),
                R.drawable.ic_done,
                getString(R.string.complete_message),
                Color.parseColor("#4CAF50")));
    }
}
