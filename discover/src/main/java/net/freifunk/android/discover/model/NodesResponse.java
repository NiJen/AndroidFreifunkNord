package net.freifunk.android.discover.model;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;

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
    private final Callbacks mCallback;

    public NodesResponse(Callbacks callbacks) {
        this.mCallback = callbacks;
    }

    public interface Callbacks {
        void onNodeAvailable(Node n);
    }

    @Override
    public void onResponse(JSONObject jsonObject) {
        try {
            JSONArray node_list = jsonObject.getJSONArray("nodes");
            for (int i = 0; i < node_list.length(); i++) {
                JSONObject node = node_list.getJSONObject(i);

                // MAC
                String[] macs = ((String) node.get("macs")).split(",");
                List<String> mac_list = new ArrayList<String>(macs.length);
                for (String mac: macs) {
                    mac_list.add(mac.trim());
                }

                // Name
                String name = ((String) node.get("name")).trim();

                // Firmware
                String firmware = String.valueOf(node.get("firmware")).trim();

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
                Log.d(TAG, String.valueOf(node.get("geo")));
                if (!String.valueOf(node.get("geo")).equals("null")) {
                    JSONArray geo = node.getJSONArray("geo");
                    Double lat = (Double) geo.get(0);
                    Double lng = (Double) geo.get(1);
                    pos = new LatLng(lat, lng);
                }

                // Id
                String id = ((String) node.get("id")).trim();

                Node n = new Node(mac_list, name, firmware, flags, pos, id);
                Log.d(TAG, n.toString());
                Node.nodes.add(n);
                mCallback.onNodeAvailable(n);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
            Log.e(TAG, volleyError.toString());
        }
}
