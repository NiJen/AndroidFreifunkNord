/*
 * GmapsFragment.java
 *
 * Original work Copyright (C) 2014  Philipp Dreimann
 * Modified work Copyright (C) 2014  Bjoern Petri
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

package net.freifunk.android.discover.map;


import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.SupportMapFragment;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import net.freifunk.android.discover.helper.Database;
import net.freifunk.android.discover.helper.EventBus;
import net.freifunk.android.discover.R;
import net.freifunk.android.discover.model.Node;
import net.freifunk.android.discover.model.NodeMap;
import net.freifunk.android.discover.model.NodeResult;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.squareup.otto.Subscribe;


public class GmapsFragment extends Fragment implements GoogleMap.OnMarkerClickListener {

    private static final String TAG = GmapsFragment.class.getName();

    private SupportMapFragment mapFragment;
    protected GoogleMap mMap;

    private SharedPreferences sharedPrefs = null;
    private Callbacks mCallbacks = sDummyCallbacks;
    private HashMap<String,Node> allNodes;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createMapFragmentIfNeeded();

        allNodes = new HashMap<String,Node>();

        EventBus.getInstance().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    @Override
    public void onDestroy() {
        EventBus.getInstance().unregister(this);
        super.onDestroy();
    }

    private void createMapFragmentIfNeeded() {
        FragmentManager fm = getChildFragmentManager();
        mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.container);
        if (mapFragment == null) {
            mapFragment = createMapFragment();
            FragmentTransaction tx = fm.beginTransaction();
            tx.add(R.id.container, mapFragment);
            tx.commit();
        }
    }

    protected SupportMapFragment createMapFragment() {
        return SupportMapFragment.newInstance();
    }


    private void setupMap() {

            mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

            ClusteringSettings clusteringSettings = new ClusteringSettings();
            clusteringSettings.addMarkersDynamically(true);

            clusteringSettings.clusterOptionsProvider(new GmapsClusterOptionsProvider(getResources()));

            clusteringSettings.clusterSize(200);
            mMap.setClustering(clusteringSettings);

            mMap.setMyLocationEnabled(true);
            mMap.setOnMarkerClickListener(this);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51, 9), 6));

    }

    private void setUpMapIfNeeded() {
        if (mMap== null) {
            mMap = mapFragment.getExtendedMap();
            if (mMap != null) {
                setupMap();
            }
        }
    }

    @Subscribe
    public void onNodeMapResult(NodeResult nodeResult) {
        NodeResult.NodeResultType resultType = nodeResult.getResultType();

        if (resultType == NodeResult.NodeResultType.LOAD_NODES || resultType == NodeResult.NodeResultType.UPDATE_NODES) {
            NodeMap map = nodeResult.getResult();
            if (map != null) {
                new UpdateMarker().executeAsyncTask(new NodeMap[]{map});
            }
        }
    }



    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onMarkerClicked(Object o) {
            Log.d(TAG, "sDummyCallbacks");
        }
    };



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        public void onMarkerClicked(Object o);
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
             View v = getActivity().getLayoutInflater().inflate(R.layout.info_window, null);

            if (marker != null && marker.getData() != null) {

                Node n = marker.getData();

                TableRow rowLastUpd = (TableRow) v.findViewById(R.id.tablerow_lastupd);
                TableRow rowHardware = (TableRow) v.findViewById(R.id.tablerow_hardware);
                TableRow rowFirmware = (TableRow) v.findViewById(R.id.tablerow_firmware);
                TableRow rowClients = (TableRow) v.findViewById(R.id.tablerow_clients);
                TableRow rowRxTx = (TableRow) v.findViewById(R.id.tablerow_rxtx);
                TableRow rowUptime = (TableRow) v.findViewById(R.id.tablerow_uptime);
                TableRow rowLoadAvg = (TableRow) v.findViewById(R.id.tablerow_loadavg);

                long lastUpdate = n.getLastUpdate();
                int clientcount = n.getClientCount();
                String firmware = n.getFirmware();
                String hardware = n.getHardware();
                double rxBytes = n.getRxBytes();
                double txBytes = n.getTxBytes();
                int uptime = n.getUptime();
                double loadAvg = n.getLoadavg();

               ImageView ivOnline = (ImageView) v.findViewById(R.id.iv_online);
               ivOnline.setImageResource(n.isOnline() ? R.drawable.ic_action_network_wifi_on : R.drawable.ic_action_network_wifi_off);

                TextView tvName = (TextView) v.findViewById(R.id.tv_name);
                String tvNameStr =  n.getName();
                tvName.setText(tvNameStr);

                TextView tvMapName = (TextView) v.findViewById(R.id.tv_mapname);
                tvMapName.setText(n.getMapname());

                if (lastUpdate > 0) {
                    SimpleDateFormat sdf;

                    if ((new Date().getTime() - lastUpdate)  > (60 * 60 *24 * 1000)) {
                        sdf = new SimpleDateFormat("dd.MM.yyyy H:mm");
                    }
                    else {
                        sdf = new SimpleDateFormat("HH:mm");
                    }

                    TextView tvLastUpd = (TextView) v.findViewById(R.id.tv_lastupd);
                    tvLastUpd.setText("" + sdf.format(new Date(lastUpdate)));
                    rowLastUpd.setVisibility(v.VISIBLE);
                }
                else {
                    rowLastUpd.setVisibility(v.GONE);
                }

                if (hardware != null && hardware.length() > 0) {
                    TextView tvHardware = (TextView) v.findViewById(R.id.tv_hardware);
                    tvHardware.setText(hardware);
                    rowHardware.setVisibility(v.VISIBLE);
                }
                else {
                    rowHardware.setVisibility(v.GONE);
                }

                if (firmware!= null && firmware.length() > 0) {
                    TextView tvFirmware = (TextView) v.findViewById(R.id.tv_firmware);
                    tvFirmware.setText(firmware);
                    rowFirmware.setVisibility(v.VISIBLE);
                }
                else {
                    rowFirmware.setVisibility(v.GONE);
                }

                if (clientcount >= 0) {
                    TextView tvClientCount = (TextView) v.findViewById(R.id.tv_clientcount);
                    tvClientCount.setText("" + clientcount);
                    rowClients.setVisibility(v.VISIBLE);
                }
                else {
                    rowClients.setVisibility(v.GONE);
                }


                if (rxBytes > 0 && txBytes > 0) {
                    TextView tvRxTx = (TextView) v.findViewById(R.id.tv_rxtx);
                    tvRxTx.setText("" + (int) (rxBytes / 1024 / 1024) + " MB / " + (int) (txBytes / 1024 / 1024) + " MB");
                    rowRxTx.setVisibility(v.VISIBLE);
                }
                else {
                    rowRxTx.setVisibility(v.GONE);
                }

                if (uptime > 0) {
                    TextView tvUptime = (TextView) v.findViewById(R.id.tv_uptime);
                    int day = (int)TimeUnit.SECONDS.toDays(uptime);
                    long hours = TimeUnit.SECONDS.toHours(uptime) - ( day * 24);
                    long minutes = TimeUnit.SECONDS.toMinutes(uptime) - (TimeUnit.SECONDS.toHours(uptime)* 60);
                    tvUptime.setText("" + day + "days " + hours + "h " + minutes +"m");
                    rowUptime.setVisibility(v.VISIBLE);
                }
                else {
                    rowUptime.setVisibility(v.GONE);
                }

                if (loadAvg > 0) {
                    TextView tvLoadAvg = (TextView) v.findViewById(R.id.tv_loadavg);
                    tvLoadAvg.setText("" + loadAvg);
                    rowLoadAvg.setVisibility(v.VISIBLE);
                }
                else {
                    rowLoadAvg.setVisibility(v.GONE);
                }
            }
            else {
                return null;
            }

            return v;
        }


    }

    private class UpdateMarker extends AsyncTask<NodeMap, Void, HashMap<String, ArrayList<Node>>> {


        @Override
        protected HashMap<String, ArrayList<Node>> doInBackground(NodeMap[] nodeMaps) {

            HashMap<String, ArrayList<Node>> nodeHashMap = new HashMap<String, ArrayList<Node>>();
            ArrayList<Node> addNodeList = new ArrayList<Node>();
            ArrayList<Node> rmNodeList = new ArrayList<Node>();

            for (NodeMap nodeMap : nodeMaps) {
                if (nodeMap.isActive()) {
                    for (Node node : nodeMap.getNodes().values()) {
                        if (!allNodes.containsValue(node)) {
                            addNodeList.add(node);
                        }
                    }
                    Log.d(TAG, "Adding " + addNodeList.size() + " new Nodes for map " + nodeMap.getMapName());
                }
                else {
                    for (Node node : nodeMap.getNodes().values()) {
                        if (allNodes.containsValue(node)) {
                            rmNodeList.add(node);
                        }
                    }
                }
            }

            nodeHashMap.put("add", addNodeList);
            nodeHashMap.put("remove", rmNodeList);

            return nodeHashMap;
        }


        @Override
        protected void onPostExecute(HashMap<String, ArrayList<Node>> nodeHasMap) {

            ArrayList<Node> addNodeList = nodeHasMap.get("add");
            ArrayList<Node> rmNodeList = nodeHasMap.get("remove");

            for (Node node : addNodeList) {
                Marker marker = mMap.addMarker(new MarkerOptions().position(node.getPosition()).title(node.getName()).data(node));
                node.setMarker(marker);
                allNodes.put(node.getName(), node);
            }

            for (Node node : rmNodeList) {
                Marker marker = node.getMarker();

                if (marker != null) {
                    marker.remove();
                    node.setMarker(null);
                }

                allNodes.remove(node.getName());
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB) // API 11
        public void executeAsyncTask(NodeMap[] params) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                this.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
            } else {
                super.execute(params);
            }
        }
    }

}
