/*
 * NodesResponse.java
 *
 * Copyright (C) 2014  Philipp Dreimann
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

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;

import net.freifunk.android.discover.RequestQueueHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NodesResponse implements Response.Listener<JSONObject>, Response.ErrorListener  {
    private final String TAG = "NodesResponse";
    private final NodeMap mCallingMap;
    private final Callbacks mCallback;

    public NodesResponse(NodeMap map, Callbacks callbacks) {
        this.mCallingMap = map;
        this.mCallback = callbacks;
    }

    public interface Callbacks {
        void onNodeAvailable(Node n);
        void onResponseFinished(NodeMap map);
    }

    @Override
    public void onResponse(JSONObject jsonObject) {
        try {
            JSONArray node_list = jsonObject.getJSONArray("nodes");
            for (int i = 0; i < node_list.length(); i++) {

                String name = "";
                String hardware = "";
                String firmware = "";
                int clientCount = -1;
                int uptime = -1;
                double rx_bytes = -1;
                double tx_bytes = -1;
                double loadavg = -1;

                JSONObject node = node_list.getJSONObject(i);
                List<String> mac_list = new ArrayList<String>(1);
                // MAC
                if ( node.has("macs")  )
                {
                    String[] macs = ((String) node.get("macs")).split(",");
                    mac_list = new ArrayList<String>(macs.length);
                    for (String mac : macs) {
                        mac_list.add(mac.trim());
                    }
                }else {
                   mac_list.add(" ");
                }
                // Name
                if (node.has("name") && !node.isNull("name")) {
                    name = ((String) node.get("name")).trim();
                }

                // Hardware
                if (node.has("hardware") && !node.isNull("hardware")) {
                    hardware = ((String) node.get("hardware")).trim();
                }
                else if (node.has("model") && !node.isNull("model")) {
                    hardware = ((String) node.get("model")).trim();
                }

                if (node.has("firmware") && !node.isNull("firmware")) {
                    firmware = String.valueOf(node.get("firmware")).trim();
                }

                if (node.has("clientcount") && !node.isNull("clientcount")) {
                    clientCount = node.getInt("clientcount");
                }

                if (node.has("rx_bytes") && !node.isNull("rx_bytes")) {
                    rx_bytes = node.getDouble("rx_bytes");
                }

                if (node.has("tx_bytes") && !node.isNull("tx_bytes")) {
                    tx_bytes = node.getDouble("tx_bytes");
                }

                if (node.has("uptime") && !node.isNull("uptime")) {
                    uptime = node.getInt("uptime");
                }

                if (node.has("loadavg") && !node.isNull("loadavg")) {
                    loadavg = node.getDouble("loadavg");
                }
                // Flags
                Map<String, String> flags = new HashMap<String, String>();
                JSONObject jflags = node.getJSONObject("flags");
                Iterator flag = jflags.keys();
                while (flag.hasNext()) {
                    String key = (String) flag.next();
                    String val = String.valueOf(jflags.get(key));
                    flags.put(key, val);
                }

                // Geo
                LatLng pos = null;
 //               Log.d(TAG, String.valueOf(node.get("geo")));
                if (!String.valueOf(node.get("geo")).equals("null")) {
                    JSONArray geo = node.getJSONArray("geo");
                    Double lat = (Double) geo.get(0);
                    Double lng = (Double) geo.get(1);
                    pos = new LatLng(lat, lng);
                }

                // Id
                String id = ((String) node.get("id")).trim();

                Node n = new Node(mac_list,this.mCallingMap.getMapName(), name, hardware, firmware, flags, pos, id, clientCount, rx_bytes, tx_bytes, uptime, loadavg);
                Node.nodes.add(n);
                mCallback.onNodeAvailable(n);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Seems something went wrong while loading Nodes for " + this.mCallingMap.getMapName() + ":" + e.toString());
        }
        finally {
            mCallback.onResponseFinished(this.mCallingMap);
        }
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
            RequestQueueHelper requestHelper = RequestQueueHelper.getInstance();

            Log.e(TAG, volleyError.toString());
            requestHelper.RequestDone();
        }
}
