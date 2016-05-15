/*
 * DaoGenerator
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

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * Generates entities and DAOs for app.
 */
public class DaoGenerator {
    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(1, "com.lynn9388.datamonitor.dao");
        addTrafficLogEntity(schema);
        new de.greenrobot.daogenerator.DaoGenerator().generateAll(schema, "../app/src/main/java");
    }

    private static void addTrafficLogEntity(Schema schema) {
        Entity trafficLog = schema.addEntity("TrafficLog");
        trafficLog.addIdProperty();
        trafficLog.addDateProperty("time").notNull();
        // Number of bytes received across mobile networks in the past 10 seconds.
        trafficLog.addLongProperty("mobileRxBytes").notNull();
        // Number of bytes transmitted across mobile networks in the past 10 seconds.
        trafficLog.addLongProperty("mobileTxBytes").notNull();
        // Number of bytes received across mobile networks in the past 10 seconds.
        trafficLog.addLongProperty("wifiRxBytes").notNull();
        // Number of bytes transmitted across mobile networks in the past 10 seconds.
        trafficLog.addLongProperty("wifiTxBytes").notNull();
        // Number of bytes received since device boot.
        trafficLog.addLongProperty("totalRxBytes").notNull();
        // Number of bytes transmitted since device boot.
        trafficLog.addLongProperty("totalTxBytes").notNull();
    }
}
