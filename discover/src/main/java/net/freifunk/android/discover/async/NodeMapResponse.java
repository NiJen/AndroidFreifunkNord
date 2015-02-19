/*
 * NodeMapResponseAsyncTask.java
 *
 * Copyright (C) 2015 Bjoern Petri
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

package net.freifunk.android.discover.async;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import net.freifunk.android.discover.helper.Database;
import net.freifunk.android.discover.helper.EventBus;
import net.freifunk.android.discover.model.NodeMap;
import net.freifunk.android.discover.model.NodeResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class NodeMapResponse extends AsyncTask<JSONObject, Object, HashMap<String, NodeMap> > implements Response.Listener<JSONObject>, Response.ErrorListener   {

    private static final String TAG = NodeMapResponse.class.getName();

    private final HashMap<String, NodeMap> maps;


    public NodeMapResponse(HashMap<String, NodeMap> maps) {
        this.maps = maps;
    }


    @Override
    protected HashMap<String, NodeMap>  doInBackground(JSONObject... jsonObjects) {


            for(JSONObject jsonObject : jsonObjects) {

                try {
                    Database database = Database.getInstance();

                    Iterator mapKeys = jsonObject.keys();

                    while (mapKeys.hasNext()) {
                        String mapName = mapKeys.next().toString();
                        String mapUrl = jsonObject.getString(mapName);

                        NodeMap m = this.maps.get(mapName);

                        if (m == null) {
                            m = new NodeMap(mapName, mapUrl, true);
                            this.maps.put(mapName, m);
                        } else {
                            m.setMapUrl(mapUrl);
                        }

                        database.addNodeMap(m);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                }
            }
        return this.maps;
       }

    @Override
    protected void onPostExecute(HashMap<String, NodeMap> maps) {
        EventBus.getInstance().post(new NodeResult(NodeResult.NodeResultType.UPDATE_MAP,maps));
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {

    }

    @Override
    public void onResponse(JSONObject jsonObject) {
        this.executeAsyncTask(jsonObject);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB) // API 11
    public void executeAsyncTask(JSONObject params) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
        }
        else {
            super.execute(params);
        }
    }

}
