package net.freifunk.android.discover.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;

import net.freifunk.android.discover.DatabaseHelper;
import net.freifunk.android.discover.RequestQueueHelper;

import java.util.ArrayList;

/**
 * Created by bjoern petri on 12/4/14.
 */
public class NodeMap {

    private static final String TAG = "NodeMap";

    Context context;
    String mapName;
    String mapUrl;
    ArrayList<Node> nodeList;
    boolean addedToMap;

    public NodeMap(String name, String mapUrl) {
        this.mapName = name;
        this.mapUrl = mapUrl;
        this.nodeList = new ArrayList<Node>();
        this.addedToMap = false;
    }

    public String getMapName() {
        return this.mapName;
    }

    public String getMapUrl() {
        return this.mapUrl;
    }

    public ArrayList<Node> getNodes() {
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

    public void loadNodes() {
        LoadNodesDatabaseTask loadNodesDatabaseTask = new LoadNodesDatabaseTask();
        loadNodesDatabaseTask.execute(new NodeMap[] { this });

        RequestQueue rq = RequestQueueHelper.getRequestQueue(null);
        // TODO: can we reference to the Map from within the Callback, similar to
        // the nodeList ?
        NodesResponse nr = new NodesResponse(this, new NodesResponse.Callbacks() {
            @Override
            public void onNodeAvailable(Node node) {
                if (node.getGeo() != null) {
                    nodeList.add(node);
                }
            }

            @Override
            public void onResponseFinished(NodeMap map) {
                MapMaster mapMaster = MapMaster.getInstance();
                Log.d(TAG, "Finished loading + " +  map.getMapName());
                mapMaster.addMap(map);

                SaveNodesDatabaseTask saveNodesDatabaseTask = new SaveNodesDatabaseTask();
                saveNodesDatabaseTask.execute(new NodeMap[] { map });
            }
        });

        JsonObjectRequest request = new JsonObjectRequest(this.mapUrl, null, nr, nr);

        RetryPolicy policy = new DefaultRetryPolicy(30000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);

        rq.add(request);
    }

    public String details() {
        final StringBuffer sb = new StringBuffer("MapMaster{");
        sb.append("name='").append(mapName).append('\'');
        sb.append(", mapUrl='").append(mapUrl).append('\'');
        sb.append('}');
        return sb.toString();
    }


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
                Log.d(TAG, "Finished database loading  " + nodeMap.getMapName());
                // only update if there are nodes available
                if (nodeMap.getNodes().size() > 0) {
                    mapMaster.addMap(nodeMap);
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
                ArrayList<Node> nodeList = nodeMap.getNodes();

                for (Node node : nodeList) {
                    databaseHelper.addNode(node);
                }
            }

            return nodeMaps;
        }

        @Override
        protected void onPostExecute(NodeMap[] nodeMaps) {
            for (NodeMap nodeMap : nodeMaps) {
                Log.d(TAG, "Finished saving + " + nodeMap.getMapName());
            }
        }
    }

}
