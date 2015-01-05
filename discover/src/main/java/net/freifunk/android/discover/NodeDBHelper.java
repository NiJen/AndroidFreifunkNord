package net.freifunk.android.discover;

/**
 * Created by NiJen on 13.10.2014.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import net.freifunk.android.discover.model.Node;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class NodeDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "nodeDB.db";
    public static final String TABLE_NODES = "nodes";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MAPNAME = "mapname";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_FIRMWARE = "firmware";
    public static final String COLUMN_GATEWAY = "gateway";
    public static final String COLUMN_CLIENT = "client";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";
    public static final String COLUMN_NODEID = "id";


    private static NodeDBHelper nodeDBHelper = null;

    private NodeDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized NodeDBHelper getInstance(Context context) {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (nodeDBHelper == null && context != null) {
            nodeDBHelper = new NodeDBHelper(context);
        }

        return nodeDBHelper;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NODE_TABLE = "CREATE TABLE " +
                TABLE_NODES + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_MAPNAME + " TEXT," +
                COLUMN_NAME + " TEXT," +
                COLUMN_FIRMWARE + " TEXT," +
                COLUMN_GATEWAY + " TEXT," +
                COLUMN_CLIENT + " TEXT," +
                COLUMN_LAT + " TEXT," +
                COLUMN_LNG + " TEXT," +
                COLUMN_NODEID + " TEXT" +
                ")";
        db.execSQL(CREATE_NODE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODES);
        onCreate(db);
    }

    public void addNode(Node node) {

        // TODO: add update functionality
        if (findNode(node.getId()) == null) {
            SQLiteDatabase db = null;

            Map<String, String> flags = node.getFlags();
            String gateway = flags.get("gateway");
            String client = flags.get("client");

            ContentValues values = new ContentValues();
            values.put(COLUMN_MAPNAME, node.getMapname());
            values.put(COLUMN_NAME, node.getName());
            values.put(COLUMN_FIRMWARE, node.getFirmware());
            values.put(COLUMN_GATEWAY, gateway);
            values.put(COLUMN_CLIENT, client);
            values.put(COLUMN_LAT, String.valueOf(node.getGeo().latitude));
            values.put(COLUMN_LNG, String.valueOf(node.getGeo().longitude));
            values.put(COLUMN_NODEID, node.getId());

            Log.d("NodeDBHelper", "Node added " + node.getId() + "/" + node.getName());

            try {
                db = this.getWritableDatabase();
                db.insert(TABLE_NODES, null, values);
            }
            finally {
                if (db != null) {
                    db.close();
                }
            }
        }
    }

    public Node findNode(String nodeID) {
        String query = "Select * FROM " + TABLE_NODES + "  WHERE " + COLUMN_NODEID + " = \"" + nodeID + "\"";

        SQLiteDatabase db = null;
        Cursor cursor = null;
        Node node = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                cursor.moveToFirst();

                Map<String, String> flags = new HashMap<String, String>();
                flags.put("gateway", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GATEWAY)));
                flags.put("client", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENT)));

                LatLng geo = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAT))),
                        Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LNG))));

                node = new Node(new ArrayList<String>(1),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MAPNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRMWARE)),
                        flags,
                        geo,
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            }
        }
        finally {
            if (cursor != null)
                cursor.close();
            if (db != null)
                db.close();
       }
        return node;
    }


    public List<Node> getAllNodesForMap(String mapname) {
        String query = "Select * FROM " + TABLE_NODES + " WHERE "+ COLUMN_MAPNAME + "=\"" + mapname + "\"";
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
                    flags.put("gateway", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GATEWAY)));
                    flags.put("client", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLIENT)));

                    LatLng geo = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAT))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LNG))));

                    Node node = new Node(new ArrayList<String>(1),
                            mapname,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRMWARE)),
                            flags,
                            geo,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));

                    Log.d("NodeDBHelper", "Node added to list of all nodes " + node.getId() + "/" + node.getName());

                    nodeList.add(node);
                } while (cursor.moveToNext());
            }
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return nodeList;

    }



}