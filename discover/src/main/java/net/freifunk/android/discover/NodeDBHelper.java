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
import com.google.android.gms.maps.model.LatLng;
import net.freifunk.android.discover.model.Node;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class NodeDBHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "nodeDB.db";
    public static final String TABLE_NODES = "nodes";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_FIRMWARE = "firmware";
    public static final String COLUMN_GATEWAY = "gateway";
    public static final String COLUMN_CLIENT = "client";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";
    public static final String COLUMN_NODEID = "id";


    public NodeDBHelper(Context context, String name,
                       CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NODE_TABLE = "CREATE TABLE " +
                TABLE_NODES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_FIRMWARE + " TEXT,"
                + COLUMN_GATEWAY + " TEXT,"
                + COLUMN_CLIENT + " TEXT,"
                + COLUMN_LAT + " TEXT,"
                + COLUMN_LNG + " TEXT,"
                + COLUMN_NODEID + " TEXT,"
                + ")";
        db.execSQL(CREATE_NODE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODES);
        onCreate(db);
    }

    public void addNodes(Node node) {

        Map<String, String> flags = node.getFlags();
        String gateway = flags.get("gateway");
        String client = flags.get("client");

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, node.getName());
        values.put(COLUMN_FIRMWARE, node.getFirmware());
        values.put(COLUMN_GATEWAY, gateway);
        values.put(COLUMN_CLIENT, client);
        values.put(COLUMN_LAT, String.valueOf(node.getGeo().latitude));
        values.put(COLUMN_LNG, String.valueOf(node.getGeo().longitude));
        values.put(COLUMN_NODEID, node.getId());


        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_NODES, null, values);
        db.close();
    }

    public Node findNode(String nodeID) {
        String query = "Select * FROM " + TABLE_NODES + " WHERE " + COLUMN_NODEID + " =  \"" + nodeID + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        List<String> macs = new ArrayList<String>(1);
        Map<String,String> flags = new HashMap<String, String>();
        LatLng latlng = new LatLng(0,0);

        Node node = new Node(macs,"","",flags,latlng,"");

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            node.setName( cursor.getString(0) );
            node.setFirmware( cursor.getString(1) );
            flags.put("gateway", cursor.getString(2) );
            flags.put("flags", cursor.getString(3) );
            node.setFlags(flags);
            latlng = new LatLng(Double.parseDouble(cursor.getString(4)),Double.parseDouble(cursor.getString(5)));
            node.setGeo(latlng);
            node.setFirmware( cursor.getString(6) );
            cursor.close();
        } else {
            node = null;
        }
        db.close();
        return node;
    }

}