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

import org.apache.http.HttpEntityEnclosingRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommunityResponse implements Response.Listener<JSONObject>, Response.ErrorListener  {
    private final String TAG = "CommunityResponse";
    private final Callbacks mCallback;
    private final String url;

    public CommunityResponse(String url, Callbacks callbacks) {;
        this.mCallback = callbacks;
        this.url = url;
    }


    public interface Callbacks {
        void onResponseFinished(NodeMap community);
    }

    @Override
    public void onResponse(JSONObject jsonObject) {

        String name = null;
        String mapUrl = null;

        NodeMap nodemap = null;

        try {
            name = jsonObject.getString("name");

            JSONArray nodeMaps = jsonObject.getJSONArray("nodeMaps");

            for (int i = 0; i < nodeMaps.length() && mapUrl == null; i++) {

                JSONObject nodeMap = nodeMaps.getJSONObject(i);

                String type = nodeMap.getString("technicalType");

                // we only support ffmap for now
                if (type.equals("ffmap")) {
                    String tUrl = nodeMap.getString("url");

                    if (tUrl != null) {

                        try {
                            URL u = new URL(tUrl);
                            String uPath = u.getPath();

                            if (uPath.contains("/")) {
                                mapUrl = u.getProtocol() + "://" + u.getHost() + uPath.substring(0, uPath.lastIndexOf('/')) + "/nodes.json";
                            }
                            else {
                                mapUrl =  u.getProtocol() + "://" + u.getHost() + "/nodes.json";
                            }


                        } catch (MalformedURLException e) {
                            Log.w(TAG, "tURL of " + name + " is malformed (" + tUrl +") " + e.getMessage() );
                        }
                    }

                    Log.d(TAG, "mapUrl is " + mapUrl);
                }
                else
                {
                    Log.d(TAG, "type is " + type);
                }

            }

            if (name != null && mapUrl != null) {
                nodemap = new NodeMap(name, mapUrl);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Seems something went wrong while loading Community from " + url + " : " + e.toString());
        }
        finally {
            mCallback.onResponseFinished(nodemap);
        }
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
            RequestQueueHelper requestHelper = RequestQueueHelper.getInstance();

            Log.e(TAG, volleyError.toString());
            requestHelper.RequestDone();
        }
}
