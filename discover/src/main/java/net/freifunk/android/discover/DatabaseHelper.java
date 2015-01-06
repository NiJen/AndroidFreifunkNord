package net.freifunk.android.discover;

/**
 * Created by NiJen on 13.10.2014.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import net.freifunk.android.discover.model.Node;
import net.freifunk.android.discover.model.NodeMap;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "freifunk.db";
    public static final String TABLE_NODES = "nodes";
    public static final String TABLE_MAPS = "maps";

    public static final String COLUMN_NODES_ID = "_id";
    public static final String COLUMN_NODES_MAPNAME = "mapname";
    public static final String COLUMN_NODES_NAME = "name";
    public static final String COLUMN_NODES_FIRMWARE = "firmware";
    public static final String COLUMN_NODES_GATEWAY = "gateway";
    public static final String COLUMN_NODES_CLIENT = "client";
    public static final String COLUMN_NODES_LAT = "lat";
    public static final String COLUMN_NODES_LNG = "lng";
    public static final String COLUMN_NODES_NODEID = "id";

    public static final String COLUMN_MAPS_MAPNAME = "mapname";
    public static final String COLUMN_MAPS_URL = "url";

    private static DatabaseHelper databaseHelper = null;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (databaseHelper == null && context != null) {
            databaseHelper = new DatabaseHelper(context);
        }

        return databaseHelper;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NODE_TABLE = "CREATE TABLE " +
                TABLE_NODES + "(" +
                COLUMN_NODES_ID + " INTEGER PRIMARY KEY," +
                COLUMN_NODES_MAPNAME + " TEXT," +
                COLUMN_NODES_NAME + " TEXT," +
                COLUMN_NODES_FIRMWARE + " TEXT," +
                COLUMN_NODES_GATEWAY + " TEXT," +
                COLUMN_NODES_CLIENT + " TEXT," +
                COLUMN_NODES_LAT + " TEXT," +
                COLUMN_NODES_LNG + " TEXT," +
                COLUMN_NODES_NODEID + " TEXT" +
                ")";
        db.execSQL(CREATE_NODE_TABLE);

        String CREATE_MAP_TABLE = "CREATE TABLE " +
                TABLE_MAPS + "(" +
                COLUMN_MAPS_MAPNAME + " TEXT," +
                COLUMN_MAPS_URL + " TEXT" +
                ")";
        db.execSQL(CREATE_MAP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAPS);
        onCreate(db);
    }



    public void addNodeMap(NodeMap map) {

        // TODO: add update functionality
        if (findNodeMap(map.getMapName()) == null) {
            SQLiteDatabase db = null;

            ContentValues values = new ContentValues();
            values.put(COLUMN_MAPS_MAPNAME, map.getMapName());
            values.put(COLUMN_MAPS_URL, map.getMapUrl() );

            Log.d("DatabaseHelper", "NodeMap added " + map.getMapName() + "/" + map.getMapUrl());

            db = this.getWritableDatabase();
            db.insert(TABLE_MAPS, null, values);
    }
    }



    public NodeMap findNodeMap(String mapName) {
        String query = "Select * FROM " + TABLE_MAPS + "  WHERE " + COLUMN_MAPS_MAPNAME + " = \"" + mapName + "\"";

        SQLiteDatabase db = null;
        Cursor cursor = null;
        NodeMap map = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                cursor.moveToFirst();

                map = new NodeMap(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAPS_MAPNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAPS_URL)));
            }
            else
            {
                Log.e("DatabaseHelper", "Map with name " + mapName+ " not found.");
            }
        }
        finally {
            if (cursor != null)
                cursor.close();
        }
        return map;
    }



    public List<NodeMap> getAllNodeMaps() {
        String query = "Select * FROM " + TABLE_MAPS;
        SQLiteDatabase db = null;
        List<NodeMap> mapList = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            mapList = new ArrayList<NodeMap>();

            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    NodeMap map = new NodeMap(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAPS_MAPNAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAPS_URL)));

                    mapList.add(map);
                } while (cursor.moveToNext());
            }
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return mapList;
    }



    public void addNode(Node node) {

        // TODO: add update functionality
        if (findNode(node.getId()) == null) {
            SQLiteDatabase db = null;

            Map<String, String> flags = node.getFlags();
            String gateway = flags.get("gateway");
            String client = flags.get("client");

            ContentValues values = new ContentValues();
            values.put(COLUMN_NODES_MAPNAME, node.getMapname());
            values.put(COLUMN_NODES_NAME, node.getName());
            values.put(COLUMN_NODES_FIRMWARE, node.getFirmware());
            values.put(COLUMN_NODES_GATEWAY, gateway);
            values.put(COLUMN_NODES_CLIENT, client);
            values.put(COLUMN_NODES_LAT, String.valueOf(node.getGeo().latitude));
            values.put(COLUMN_NODES_LNG, String.valueOf(node.getGeo().longitude));
            values.put(COLUMN_NODES_NODEID, node.getId());

            Log.d("DatabaseHelper", "Node added " + node.getId() + "/" + node.getName());

            db = this.getWritableDatabase();
            db.insert(TABLE_NODES, null, values);
        }
    }

    public Node findNode(String nodeID) {
        String query = "Select * FROM " + TABLE_NODES + "  WHERE " + COLUMN_NODES_NODEID + " = \"" + nodeID + "\"";

        SQLiteDatabase db = null;
        Cursor cursor = null;
        Node node = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                cursor.moveToFirst();

                Map<String, String> flags = new HashMap<String, String>();
                flags.put("gateway", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_GATEWAY)));
                flags.put("client", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_CLIENT)));

                LatLng geo = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_LAT))),
                        Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_LNG))));

                node = new Node(new ArrayList<String>(1),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_MAPNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_FIRMWARE)),
                        flags,
                        geo,
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_ID)));
            }
            else
            {
                Log.e("DatabaseHelper", "Node with ID " + nodeID + " not found.");
            }
        }
        finally {
            if (cursor != null)
                cursor.close();
       }
        return node;
    }


    public List<Node> getAllNodesForMap(String mapname) {
        String query = "Select * FROM " + TABLE_NODES + " WHERE "+ COLUMN_NODES_MAPNAME + "=\"" + mapname + "\"";
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

                    LatLng geo = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_LAT))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_LNG))));

                    Node node = new Node(new ArrayList<String>(1),
                            mapname,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_FIRMWARE)),
                            flags,
                            geo,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NODES_NODEID)));

                    Log.d("DatabaseHelper", "Node added to list of all nodes " + node.getId() + "/" + node.getName());

                    nodeList.add(node);
                } while (cursor.moveToNext());
            }
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return nodeList;

    }

}