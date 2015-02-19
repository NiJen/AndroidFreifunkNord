/*
 * NodesResponse.java
 *
 * Original work Copyright (C) 2014  Philipp Dreimann
 * Modified work Copyright (C) 2015  Bjoern Petri
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
import com.google.android.gms.maps.model.LatLng;

import net.freifunk.android.discover.helper.EventBus;
import net.freifunk.android.discover.model.Node;
import net.freifunk.android.discover.model.NodeMap;
import net.freifunk.android.discover.model.NodeResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NodesResponse extends AsyncTask<JSONObject, Object, NodeMap> implements Response.Listener<JSONObject>, Response.ErrorListener   {
    private final String TAG = "NodesResponse";
    private final NodeMap mCallingMap;

    public NodesResponse(NodeMap map) {
        this.mCallingMap = map;
    }

    @Override
    protected NodeMap doInBackground(JSONObject... jsonObjects) {

        for(JSONObject jsonObject : jsonObjects) {
            try {
                Log.e(TAG, "onResponse RUNNING on " + Thread.currentThread().getName());

                JSONArray node_list = jsonObject.getJSONArray("nodes");
                HashMap<String, Node> nodes = this.mCallingMap.getNodes();

                String name;
                String hardware;
                String firmware;
                int clientCount;
                int uptime;
                double rx_bytes;
                double tx_bytes;
                double loadavg;

                for (int i = 0; i < node_list.length(); i++) {

                    name = "";
                    hardware = "";
                    firmware = "";
                    clientCount = -1;
                    uptime = -1;
                    rx_bytes = -1;
                    tx_bytes = -1;
                    loadavg = -1;

                    JSONObject node = node_list.getJSONObject(i);

                    // Name
                    if (node.has("name") && !node.isNull("name")) {
                        name = ((String) node.get("name")).trim();
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


                    Node n = nodes.get(name);

                    if (n == null) {

                        // Id
                        String id = ((String) node.get("id")).trim();

                        List<String> mac_list = new ArrayList<String>(1);
                        // MAC
                        if (node.has("macs")) {
                            String[] macs = ((String) node.get("macs")).split(",");
                            mac_list = new ArrayList<String>(macs.length);
                            for (String mac : macs) {
                                mac_list.add(mac.trim());
                            }
                        } else {
                            mac_list.add(" ");
                        }

                        // Hardware
                        if (node.has("hardware") && !node.isNull("hardware")) {
                            hardware = ((String) node.get("hardware")).trim();
                        } else if (node.has("model") && !node.isNull("model")) {
                            hardware = ((String) node.get("model")).trim();
                        }

                        // Firmware
                        if (node.has("firmware") && !node.isNull("firmware")) {
                            firmware = String.valueOf(node.get("firmware")).trim();
                        }

                        // we add the node only, when it has some geo information
                        if (node.has("geo") && !String.valueOf(node.get("geo")).equals("null")) {
                            JSONArray geo = node.getJSONArray("geo");
                            Double lat = (Double) geo.get(0);
                            Double lng = (Double) geo.get(1);
                            LatLng pos = new LatLng(lat, lng);

                            n = new Node(mac_list, this.mCallingMap.getMapName(), name, hardware, firmware, flags, pos, id, clientCount, rx_bytes, tx_bytes, uptime, loadavg, new Date().getTime());

                            nodes.put(name, n);
                        }
                    } else {
                        /* incremental data update */
                        n.setFirmware(firmware);
                        n.setUptime(uptime);
                        n.setLoadavg(loadavg);
                        n.setLastUpdate(new Date().getTime());
                        n.setClientCount(clientCount);
                        n.setRxBytes(rx_bytes);
                        n.setTxBytes(tx_bytes);
                        n.setFlags(flags);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Seems something went wrong while loading Nodes for " + this.mCallingMap.getMapName() + ":" + e.toString());
            }
        }

        return this.mCallingMap;
    }

    @Override
    protected void onPostExecute(NodeMap nodeMap) {
        EventBus.getInstance().post(new NodeResult(NodeResult.NodeResultType.UPDATE_NODES,nodeMap));
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

    @Override
    public void onResponse(JSONObject jsonObject) {
        this.executeAsyncTask(jsonObject);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
            Log.e(TAG, volleyError.toString());
    }
}
