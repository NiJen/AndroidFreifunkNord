/*
 * MapMaster.java
 *
 * Original work Copyright (C) 2014 NiJen
 * Modified work Copyright (C) 2015 Bjoern Petri
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package net.freifunk.android.discover.model;

import java.util.HashMap;


public class MapMaster {

private static MapMaster mInstance;

    private final String TAG = MapMaster.class.getName();
    private static volatile HashMap<String, NodeMap> maps = null;

    private MapMaster() {
        maps = new HashMap<String, NodeMap>(20);
    }

    public static synchronized MapMaster getInstance() {
        if (mInstance == null) {
            mInstance = new MapMaster();
        }
        return mInstance;
    }


    public void put(String key, NodeMap value) {
        maps.put(key, value);
    }


    public HashMap<String, NodeMap> getMaps() {
        return maps;
    }

}
