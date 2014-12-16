package net.freifunk.android.discover.model;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

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

    public void setAddedToMap(boolean addedToMap) {
        this.addedToMap = addedToMap;
    }

    public boolean alreadyAddedToMap() {
        return this.addedToMap;
    }

    public void loadNodes() {
        RequestQueue rq = RequestQueueHelper.getRequestQueue(null);
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
                Log.d(TAG, "Finished loading + " + map.getMapName());
                mapMaster.addMap(map);
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


}
