package net.freifunk.android.discover.model;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

/**
 * Created by NiJen on 30.04.14.
 */


public class MapMaster extends Observable {

   private static MapMaster mInstance;

    private final String TAG = "MapMaster";
    private final Set<NodeMap> maps = Collections.synchronizedSet(new HashSet<NodeMap>());

    private MapMaster() {

    }

    public static synchronized MapMaster getInstance() {
        if (mInstance == null) {
            mInstance = new MapMaster();
        }
        return mInstance;
    }


    /**
     * @brief Save the JSON data to the sd card
     */
    public void saveToSd(){};

    /**
     * @brief Load the JSON data from the sd card
     */
    public void loadFromSd(){};


    public void addMap(NodeMap m) {
        maps.add(m);
        setChanged();
        notifyObservers();
    }

    public Set<NodeMap> getMaps() {
        return maps;
    }
    public boolean isEmpty() {
        return (maps.size() == 0);
    }

}
