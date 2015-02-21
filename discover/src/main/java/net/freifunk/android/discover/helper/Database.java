/*
 * DatabaseHelper.java
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

package net.freifunk.android.discover.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import net.freifunk.android.discover.model.Node;
import net.freifunk.android.discover.model.NodeMap;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Database extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 14;
    private static final String DATABASE_NAME = "freifunk.db";
    public static final String TABLE_NODES = "nodes";
    public static final String TABLE_MAPS = "maps";

    public static final String COLUMN_NODES_MAPNAME = "mapname";
    public static final String COLUMN_NODES_NAME = "name";
    public static final String COLUMN_NODES_HARDWARE = "hardware";
    public static final String COLUMN_NODES_FIRMWARE = "firmware";
    public static final String COLUMN_NODES_GATEWAY = "gateway";
    public static final String COLUMN_NODES_CLIENT = "client";
    public static final String COLUMN_NODES_ONLINE = "online";
    public static final String COLUMN_NODES_CLIENTCOUNT = "clientcount";
    public static final String COLUMN_NODES_RXBYTES = "rx_bytes";
    public static final String COLUMN_NODES_TXBYTES = "tx_bytes";
    public static final String COLUMN_NODES_UPTIME = "uptime";
    public static final String COLUMN_NODES_LOADAVG = "loadavg";
    public static final String COLUMN_NODES_LAT = "lat";
    public static final String COLUMN_NODES_LNG = "lng";
    public static final String COLUMN_NODES_NODEID = "id";
    public static final String COLUMN_NODES_LASTUPDATE = "lastUpdate";

    public static final String COLUMN_MAPS_MAPNAME = "mapname";
    public static final String COLUMN_MAPS_URL = "url";
    public static final String COLUMN_MAPS_ACTIVE = "active";
    public static final String COLUMN_MAPS_LASTUPDATE = "lastUpdate";

    private static Database database = null;

    private Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized Database getInstance(Context context) {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (database == null && context != null) {
            database = new Database(context);
        }

        return database;
    }

    public static synchronized Database getInstance() {
        return database;
    }

        @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NODE_TABLE = "CREATE TABLE " +
                TABLE_NODES + "(" +
                COLUMN_NODES_MAPNAME + " TEXT," +
                COLUMN_NODES_NAME + " TEXT," +
                COLUMN_NODES_HARDWARE + " TEXT," +
                COLUMN_NODES_FIRMWARE + " TEXT," +
                COLUMN_NODES_GATEWAY + " TEXT," +
                COLUMN_NODES_CLIENT + " TEXT," +
                COLUMN_NODES_ONLINE + " TEXT," +
                COLUMN_NODES_LAT + " TEXT," +
                COLUMN_NODES_LNG + " TEXT," +
                COLUMN_NODES_NODEID + " TEXT," +
                COLUMN_NODES_CLIENTCOUNT + " INTEGER," +
                COLUMN_NODES_RXBYTES + " REAL," +
                COLUMN_NODES_TXBYTES + " REAL," +
                COLUMN_NODES_UPTIME + " INTEGER," +
                COLUMN_NODES_LOADAVG + " REAL," +
                COLUMN_NODES_LASTUPDATE + " INTEGER" +
                ")";
        db.execSQL(CREATE_NODE_TABLE);

        String CREATE_MAP_TABLE = "CREATE TABLE " +
                TABLE_MAPS + "(" +
                COLUMN_MAPS_MAPNAME + " TEXT," +
                COLUMN_MAPS_URL + " TEXT," +
                COLUMN_MAPS_ACTIVE + " INTEGER," +
                COLUMN_MAPS_LASTUPDATE + " INTEGER" +
                ")";
        db.execSQL(CREATE_MAP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAPS);
        onCreate(db);
    }


    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAPS);
        onCreate(db);
    }

    public void addNodeMap(NodeMap map) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_MAPS_URL, map.getMapUrl());
        values.put(COLUMN_MAPS_ACTIVE, map.isActive() == true ? 1 : 0);
        values.put(COLUMN_MAPS_LASTUPDATE, new Date().getTime());

        int updateResult = db.update(TABLE_MAPS, values, COLUMN_MAPS_MAPNAME + " = \"" + map.getMapName() + "\"", null);

        if (updateResult == 0) {
            values.put(COLUMN_MAPS_MAPNAME, map.getMapName());

            Log.v("Database", "NodeMap added " + map.getMapName() + "/" + map.getMapUrl());
            db.insert(TABLE_MAPS, null, values);
        } else {
            Log.v("Database", "NodeMap updated " + map.getMapName() + "/" + map.getMapUrl());
        }
    }



    public HashMap<String, NodeMap> getAllNodeMaps() {
        String query = "Select * FROM " + TABLE_MAPS;
        SQLiteDatabase db = null;
        HashMap<String, NodeMap> mapList = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            mapList = new HashMap<String, NodeMap>();

            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    NodeMap map = new NodeMap(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAPS_MAPNAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAPS_URL)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAPS_ACTIVE)) == 1 ? true : false);
                    mapList.put(map.getMapName(), map);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return mapList;
    }


    public void addNode(Node node) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        Map<String, String> flags = node.getFlags();
        String gateway = flags.get("gateway");
        String client = flags.get("client");
        String online = flags.get("online");

        values.put(COLUMN_NODES_FIRMWARE, node.getFirmware());
        values.put(COLUMN_NODES_HARDWARE, node.getHardware());
        values.put(COLUMN_NODES_GATEWAY, gateway);
        values.put(COLUMN_NODES_CLIENT, client);
        values.put(COLUMN_NODES_ONLINE, online);
        values.put(COLUMN_NODES_LAT, String.valueOf(node.getGeo().latitude));
        values.put(COLUMN_NODES_LNG, String.valueOf(node.getGeo().longitude));
        values.put(COLUMN_NODES_NODEID, node.getId());
        values.put(COLUMN_NODES_CLIENTCOUNT, node.getClientCount());
        values.put(COLUMN_NODES_RXBYTES, node.getRxBytes());
        values.put(COLUMN_NODES_TXBYTES, node.getTxBytes());
        values.put(COLUMN_NODES_UPTIME, node.getUptime());
        values.put(COLUMN_NODES_LOADAVG, node.getLoadavg());
        values.put(COLUMN_NODES_LASTUPDATE, new Date().getTime());

        // try update first
        int result = db.update(TABLE_NODES, values, COLUMN_NODES_NAME + " = \"" + node.getName() + "\" AND  " + COLUMN_NODES_MAPNAME + " = \"" + node.getMapname() + "\"", null);

        if (result == 0) {
            values.put(COLUMN_NODES_MAPNAME, node.getMapname());
            values.put(COLUMN_NODES_NAME, node.getName());

            Log.v("Database", "Node added " + node.getId() + "/" + node.getName());
            db.insert(TABLE_NODES, null, values);
        } else {
            Log.v("Database", "Node updated " + node.getId() + "/" + node.getName());
        }
    }


    public List<Node> getAllNodesForMap(String mapName) {
        String query = "Select * FROM " + TABLE_NODES + " WHERE " + COLUMN_NODES_MAPNAME + "=\"" + mapName + "\"";
        SQLiteDatabase db = null;
        List<Node> nodeList = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            nodeList = new ArrayList<Node>();

            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    Map<String, String> flags = new HashMap<String, String>();
                    flags.put("gateway", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_GATEWAY)));
                    flags.put("client", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_CLIENT)));
                    flags.put("online", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_ONLINE)));

                    LatLng geo = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_LAT))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_LNG))));

                    Node node = new Node(new ArrayList<String>(1),
                            mapName,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_HARDWARE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_FIRMWARE)),
                            flags,
                            geo,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_NODEID)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NODES_CLIENTCOUNT)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_NODES_RXBYTES)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_NODES_TXBYTES)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NODES_UPTIME)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_NODES_LOADAVG)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NODES_LASTUPDATE))
                    );

                    nodeList.add(node);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return nodeList;

    }

    public int deleteAllNodesForMap(NodeMap map) {
        SQLiteDatabase db = null;
        db = this.getWritableDatabase();

        return db.delete(TABLE_NODES, COLUMN_MAPS_MAPNAME + " = ?", new String[] { map.getMapName() });
    }
}