/*
 * IntroductionBaseFragment
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

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.lynn9388.datamonitor.R;

public abstract class SlideFragment<T> extends Fragment implements View.OnClickListener,
        ISlideBackgroundColorHolder {
    protected static final String ARGUMENTS_TITLE = "title";
    protected static final String ARGUMENTS_IMAGE = "image";
    protected static final String ARGUMENTS_BUTTON_TEXT = "button_text";
    protected static final String ARGUMENTS_BACKGROUND_COLOR = "background_color";

    private CharSequence mTitle;
    private int mImage;
    private CharSequence mButtonText;
    private int mBackgroundColor;

    private LinearLayout mContainer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mTitle = arguments.getCharSequence(ARGUMENTS_TITLE);
            mImage = arguments.getInt(ARGUMENTS_IMAGE);
            mButtonText = arguments.getCharSequence(ARGUMENTS_BUTTON_TEXT);
            mBackgroundColor = arguments.getInt(ARGUMENTS_BACKGROUND_COLOR);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_introduction, container, false);
        mContainer = (LinearLayout) view.findViewById(R.id.container);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        Button button = (Button) view.findViewById(R.id.button);

        titleView.setText(mTitle);
        imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), mImage));
        button.setText(mButtonText);
        button.setOnClickListener(this);

        return view;
    }

    @Override
    public int getDefaultBackgroundColor() {
        return mBackgroundColor;
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        mContainer.setBackgroundColor(backgroundColor);
    }

    public T getInstance(CharSequence title, int image, CharSequence buttonText,
                         int backgroundColor) {
        Bundle arguments = new Bundle();
        arguments.putCharSequence(ARGUMENTS_TITLE, title);
        arguments.putInt(ARGUMENTS_IMAGE, image);
        arguments.putCharSequence(ARGUMENTS_BUTTON_TEXT, buttonText);
        arguments.putInt(ARGUMENTS_BACKGROUND_COLOR, backgroundColor);
        this.setArguments(arguments);
        return (T) this;
    }
}
