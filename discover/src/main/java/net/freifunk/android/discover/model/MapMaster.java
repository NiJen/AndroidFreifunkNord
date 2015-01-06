package net.freifunk.android.discover.model;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

/**
 * Created by NiJen on 30.04.14.
 */


public class MapMaster extends Observable {

   private static MapMaster mInstance;

    private final String TAG = "MapMaster";
    private final HashMap<String, NodeMap> maps = new HashMap<String, NodeMap>();


    private MapMaster() {

    }

    public static synchronized MapMaster getInstance() {
        if (mInstance == null) {
            mInstance = new MapMaster();
        }
        return mInstance;
    }


    public void addMap(NodeMap m) {

        // At the moment we assume that a later received map is more up to date
        maps.put(m.getMapName(), m);
        update();
    }

    public void update() {
        setChanged();
        notifyObservers();
    }

    public Collection<NodeMap> getMaps() {
        return maps.values();
    }

    public boolean isEmpty() {
        return (maps.size() == 0);
    }

}
