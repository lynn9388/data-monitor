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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lynn9388.datamonitor.R;

public abstract class AppHolder extends RecyclerView.ViewHolder {
    public ImageView mIconView;
    public TextView mAppNameView;
    public TextView mPackageNameView;
    public TextView mDataSendView;
    public TextView mDataReceiveView;

    public AppHolder(View itemView) {
        super(itemView);
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
                AppHolder.this.onClick(packageName);
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TextView packageNameView = (TextView) v.findViewById(R.id.package_name);
                String packageName = packageNameView.getText().toString();
                AppHolder.this.onLongClick(packageName);
                return true;
            }
        });
    }

    abstract void onClick(String packageName);

    abstract void onLongClick(String packageName);
}
