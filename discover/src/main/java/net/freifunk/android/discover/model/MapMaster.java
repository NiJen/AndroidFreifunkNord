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
import java.util.Set;

/**
 * Created by NiJen on 30.04.14.
 */
public class MapMaster {
    private static final String TAG = "MapMaster";
    public static final Set<MapMaster> maps = Collections.synchronizedSet(new HashSet<MapMaster>());

    String mapName;
    String mapUrl;

    public MapMaster(String name, String mapUrl) {
        this.mapName = name;
        this.mapUrl = mapUrl;
    }

    public String getMapName() { return mapName; }

    public String getMapUrl() {
        return mapUrl;
    }

    /**
     * @brief Save the JSON data to the sd card
     */
    public void saveToSd(){};

    /**
     * @brief Load the JSON data from the sd card
     */
    public void loadFromSd(){};

    public String details() {
        final StringBuffer sb = new StringBuffer("MapMaster{");
        sb.append("name='").append(mapName).append('\'');
        sb.append(", mapUrl='").append(mapUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
