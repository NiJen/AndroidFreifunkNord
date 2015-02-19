/*
 * NodeMap.java
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

package net.freifunk.android.discover.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;

import net.freifunk.android.discover.helper.RequestQueue;
import net.freifunk.android.discover.async.LoadNode;
import net.freifunk.android.discover.async.NodesResponse;
import net.freifunk.android.discover.async.SaveNode;

import java.util.ArrayList;
import java.util.HashMap;

public class NodeMap implements  Parcelable{

    private static final String TAG = NodeMap.class.getName();

    Context context;
    String mapName;
    String mapUrl;
    boolean active;

    HashMap<String, Node> nodeList;

    public NodeMap(String name, String mapUrl, boolean active) {
        this.mapName = name;
        this.mapUrl = mapUrl;
        this.nodeList = new HashMap<String, Node>();
        this.active = active;
    }

    private NodeMap(Parcel parcel) {
        this.mapName = parcel.readString();
        this.mapUrl = parcel.readString();
        this.active = (parcel.readInt() == 1)  ? true : false;
    }

    public String getMapName() {
        return this.mapName;
    }

    public String getMapUrl() {
        return this.mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public HashMap<String, Node> getNodes() {
        return this.nodeList;
    }


    public void addNodes(ArrayList<Node> nodeList) {
        for (Node node : nodeList) {
            this.nodeList.put(node.getName(), node);
        }
    }


    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }

    public void loadNodes() {
        new LoadNode().executeAsyncTask(new NodeMap[]{this});
    }

    public void saveNodes() {
        new SaveNode().executeAsyncTask(new NodeMap[]{this});
    }


    public void updateNodes() {

        RequestQueue requestHelper = RequestQueue.getInstance();

        /* load from web */
        NodesResponse nr = new NodesResponse(this);
        JsonObjectRequest request = new JsonObjectRequest(this.mapUrl, null, nr, nr);

        RetryPolicy policy = new DefaultRetryPolicy(30000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);

        requestHelper.add(request);
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mapName);
        parcel.writeString(mapUrl);
        parcel.writeInt((active == true) ? 1 : 0);
    }
    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<NodeMap> CREATOR = new Parcelable.Creator<NodeMap>() {
        public NodeMap createFromParcel(Parcel in) {
            return new NodeMap(in);
        }
        public NodeMap[] newArray(int size) {
            return new NodeMap[size];
        }
    };

}