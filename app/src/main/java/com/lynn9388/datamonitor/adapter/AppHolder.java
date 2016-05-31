/*
 * AppHolder
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

package com.lynn9388.datamonitor.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lynn9388.datamonitor.MainActivity;
import com.lynn9388.datamonitor.R;
import com.lynn9388.datamonitor.fragment.AppDetailFragment;

public abstract class AppHolder extends RecyclerView.ViewHolder {
    public static final String ACTION_BAR_TITLE = "action_bar_title";
    public static final String PACKAGE_NAME = "package_name";

    public ImageView mIconView;
    public TextView mAppNameView;
    public TextView mPackageNameView;
    public TextView mDataSendView;
    public TextView mDataReceiveView;

    private Context mContext;

    public AppHolder(Context context, View itemView) {
        super(itemView);
        mContext = context;

        mIconView = (ImageView) itemView.findViewById(R.id.icon);
        mAppNameView = (TextView) itemView.findViewById(R.id.app_name);
        mPackageNameView = (TextView) itemView.findViewById(R.id.package_name);
        mDataSendView = (TextView) itemView.findViewById(R.id.data_send);
        mDataReceiveView = (TextView) itemView.findViewById(R.id.data_receive);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView packageNameView = (TextView) v.findViewById(R.id.package_name);
                String packageName = packageNameView.getText().toString();
                String appName = mAppNameView.getText().toString();

                MainActivity mainActivity = (MainActivity) mContext;
                FragmentTransaction fragmentTransaction =
                        mainActivity.getSupportFragmentManager().beginTransaction();
                fragmentTransaction.addToBackStack(null);
                Bundle bundle = new Bundle();
                bundle.putString(ACTION_BAR_TITLE, getActionBarTitle());
                bundle.putString(PACKAGE_NAME, packageName);
                AppDetailFragment appDetailFragment = new AppDetailFragment();
                appDetailFragment.setArguments(bundle);
                fragmentTransaction.add(R.id.main_activity_content, appDetailFragment).commit();
                mainActivity.getSupportActionBar().setTitle(appName);
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TextView packageNameView = (TextView) v.findViewById(R.id.package_name);
                String packageName = packageNameView.getText().toString();
                showAppDetails(packageName);
                return true;
            }
        });
    }

    private void showAppDetails(String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        mContext.startActivity(intent);
    }

    abstract String getActionBarTitle();
}
