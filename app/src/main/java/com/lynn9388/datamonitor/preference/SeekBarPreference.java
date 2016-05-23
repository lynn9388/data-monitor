/*
 * SeekBarPreference
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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lynn9388.datamonitor.R;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = SeekBarPreference.class.getName();

    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 100;
    private static final int DEFAULT_INTERVAL = 1;
    private static final int DEFAULT_PROGRESS = 50;

    private int mMinValue;
    private int mMaxValue;
    private int mInterval;
    private int mProgress;
    private String mTitle;
    private String mSummary;

    private SeekBar mSeekBarView;

    public SeekBarPreference(Context context) {
        super(context);
        init(null);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setLayoutResource(R.layout.preference_seek_bar);

        if (attrs == null) {
            mMinValue = DEFAULT_MIN_VALUE;
            mMaxValue = DEFAULT_MAX_VALUE;
            mInterval = DEFAULT_INTERVAL;
            mProgress = DEFAULT_PROGRESS;
        } else {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
            try {
                mMinValue = a.getInt(R.styleable.SeekBarPreference_minValue, DEFAULT_MIN_VALUE);
                mMaxValue = a.getInt(R.styleable.SeekBarPreference_maxValue, DEFAULT_MAX_VALUE);
                mInterval = a.getInt(R.styleable.SeekBarPreference_interval, DEFAULT_INTERVAL);
                mTitle = a.getString(R.styleable.SeekBarPreference_android_title);
                mSummary = a.getString(R.styleable.SeekBarPreference_android_summary);
                mProgress = a.getInt(R.styleable.SeekBarPreference_android_defaultValue,
                        DEFAULT_PROGRESS);
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);

        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        mSeekBarView = (SeekBar) view.findViewById(R.id.seek_bar);

        titleView.setText(mTitle);
        summaryView.setText(mSummary);
        setMaxValue(mMaxValue);
        mSeekBarView.setOnSeekBarChangeListener(this);
    }

    @Override
    protected boolean persistInt(int value) {
        if (value < mMinValue) {
            value = mMinValue;
        }

        if (value > mMaxValue) {
            value = mMaxValue;
        }
        mProgress = value;
        return super.persistInt(value);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
        mProgress = restorePersistedValue ? getPersistedInt(mProgress)
                : (Integer) defaultValue;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int newValue = mMinValue + progress;

        if (newValue < mMinValue) {
            newValue = mMinValue;
        } else if (newValue > mMaxValue) {
            newValue = mMaxValue;
        } else if (mInterval != 1 && newValue % mInterval != 0) {
            newValue = Math.round(((float) newValue) / mInterval) * mInterval;
        }

        if (!callChangeListener(newValue)) {
            seekBar.setProgress(mProgress - mMinValue);
        } else {
            mProgress = newValue;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        notifyChanged();
        setProgress(mProgress);
    }

    public int getMinValue() {
        return mMinValue;
    }

    public void setMinValue(int minValue) {
        mMinValue = minValue;
        setMaxValue(mMaxValue);
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        mMaxValue = maxValue;

        if (mSeekBarView != null) {
            if (mMinValue <= 0 && maxValue >= 0) {
                mSeekBarView.setMax(maxValue - mMinValue);
            } else {
                mSeekBarView.setMax(maxValue);
            }

            mSeekBarView.setProgress(mProgress - mMinValue);
        }
    }

    public int getInterval() {
        return mInterval;
    }

    public void setInterval(int interval) {
        mInterval = interval;
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        persistInt(progress);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }
}
