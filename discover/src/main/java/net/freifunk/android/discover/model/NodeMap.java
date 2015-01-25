package net.freifunk.android.discover.model;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;

import net.freifunk.android.discover.DatabaseHelper;
import net.freifunk.android.discover.RequestQueueHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by bjoern petri on 12/4/14.
 */
public class NodeMap implements Parcelable {

    private static final String TAG = "NodeMap";

    Context context;
    String mapName;
    String mapUrl;
    boolean active;

    CopyOnWriteArrayList<Node> nodeList;
    boolean addedToMap;

    public NodeMap(String name, String mapUrl, boolean active) {
        this.mapName = name;
        this.mapUrl = mapUrl;
        this.nodeList = new CopyOnWriteArrayList<Node>();
        this.active = active;
        this.addedToMap = false;
    }


    private NodeMap(Parcel parcel) {
        this.mapName = parcel.readString();
        this.mapUrl = parcel.readString();
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

    public CopyOnWriteArrayList<Node> getNodes() {
        return this.nodeList;
    }

    public void addNodes(ArrayList<Node> nodeList) {
        for(Node node : nodeList) {
            if(!this.nodeList.contains(node)) {
                this.nodeList.add(node);
            }
        }
    }

    public void setAddedToMap(boolean addedToMap) {
        this.addedToMap = addedToMap;
    }

    public boolean alreadyAddedToMap() {
        return this.addedToMap;
    }


    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }

    public void loadNodes() {
        final RequestQueueHelper requestHelper = RequestQueueHelper.getInstance();

        /* load from database */
        LoadNodesDatabaseTask loadNodesDatabaseTask = new LoadNodesDatabaseTask();
        loadNodesDatabaseTask.execute(new NodeMap[] { this });


        // TODO: can we reference to the Map from within the Callback, similar to
        // the nodeList ?


        /* load from web */
        NodesResponse nr = new NodesResponse(this, new NodesResponse.Callbacks() {
            @Override
            public void onNodeAvailable(Node node) {
                if (node.getGeo() != null && !nodeList.contains(node)) {
                    nodeList.add(node);
                }
            }

            @Override
            public void onResponseFinished(NodeMap map) {
                try {
                    MapMaster mapMaster = MapMaster.getInstance();
                    Log.d(TAG, "Finished loading + " + map.getMapName());
                    mapMaster.updateMap(map);

                    SaveNodesDatabaseTask saveNodesDatabaseTask = new SaveNodesDatabaseTask();
                    saveNodesDatabaseTask.execute(new NodeMap[]{map});
                }
                finally {

                    Log.d(TAG, "NodeResponse for  + " + map.getMapName() + " finished.");
                    // we set the RequestDone when the saveNodesDatabaseTask has been done
                }
            }
        });

        JsonObjectRequest request = new JsonObjectRequest(this.mapUrl, null, nr, nr);

        RetryPolicy policy = new DefaultRetryPolicy(30000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);

        requestHelper.add(request);
    }



    @SuppressWarnings("StringBufferReplaceableByString")
    public String details() {
        final StringBuffer sb = new StringBuffer("MapMaster{");
        sb.append("name='").append(mapName).append('\'');
        sb.append(", mapUrl='").append(mapUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mapName);
        parcel.writeString(mapUrl);
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



    private class LoadNodesDatabaseTask extends AsyncTask<NodeMap, Object, NodeMap[]> {

        @Override
        protected NodeMap[] doInBackground(NodeMap[] nodeMaps) {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(NodeMap.this.context);

            for (NodeMap nodeMap : nodeMaps) {
                ArrayList<Node> nodeList = (ArrayList<Node>) databaseHelper.getAllNodesForMap(nodeMap.getMapName());
                nodeMap.addNodes(nodeList);
            }

            return nodeMaps;
        }

        @Override
        protected void onPostExecute(NodeMap[] nodeMaps) {
            MapMaster mapMaster = MapMaster.getInstance();
            for (NodeMap nodeMap : nodeMaps) {
                int size = nodeMap.getNodes().size();
                Log.e(TAG, "Finished database loading  " + nodeMap.getMapName() + " (" + size + " entries)");

                // only update if there are nodes available
                if (size > 0) {
                    mapMaster.updateMap(nodeMap);
                }
                else {
                    Log.d(TAG, "no entries found for " + nodeMap.getMapName() + " in database ");
                }

            }
        }
    }



    private class SaveNodesDatabaseTask extends AsyncTask<NodeMap, Object, NodeMap[]> {

        @Override
        protected NodeMap[] doInBackground(NodeMap[] nodeMaps) {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(NodeMap.this.context);

            for (NodeMap nodeMap : nodeMaps) {
                CopyOnWriteArrayList<Node> nodeList = nodeMap.getNodes();

                for (Node node : nodeList) {
                    databaseHelper.addNode(node);
                }
            }

            return nodeMaps;
        }

        @Override
        protected void onPostExecute(NodeMap[] nodeMaps) {
            RequestQueueHelper requestHelper = RequestQueueHelper.getInstance();

            for (NodeMap nodeMap : nodeMaps) {
                Log.d(TAG, "Finished saving + " + nodeMap.getMapName());
            }

            requestHelper.RequestDone();

        }
    }

}