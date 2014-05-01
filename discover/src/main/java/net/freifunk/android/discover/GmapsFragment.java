/*
 * GmapsFragment.java
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

package net.freifunk.android.discover;


import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import net.freifunk.android.discover.model.Community;
import net.freifunk.android.discover.model.Node;
import net.freifunk.android.discover.model.NodesResponse;
import net.freifunk.android.discover.model.MapMaster;

import java.util.HashMap;

import com.google.maps.android.clustering.ClusterManager;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 *
 */
public class GmapsFragment extends SupportMapFragment implements GoogleMap.OnMarkerClickListener {

    public static final String ARG_TYPE = "type_id";
    public static final String COMMUNITY_TYPE = "type_community";
    public static final String NODES_TYPE = "type_nodes";
    private static final String TAG = "GmapsFragment";
    private HashMap<Marker, Object> markerMap;
    private Callbacks mCallbacks = sDummyCallbacks;
    private ClusterManager<Node> mClusterManager;

    public static GmapsFragment newInstance(String type) {
        GmapsFragment fragment = new GmapsFragment();
        Bundle b = new Bundle();
        b.putString(ARG_TYPE, type);
        fragment.setArguments(b);
        return fragment;
    }

    public GmapsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments().containsKey(ARG_TYPE)) {
            getMap().setOnMarkerClickListener(this);
            String type = (String) getArguments().get(ARG_TYPE);
            if (type.equals(COMMUNITY_TYPE)) {
                createCommunityMap();
            } else if (type.equals(NODES_TYPE)) {
                createNodesMap();
            }
        }


    }

    private void createNodesMap() {
        markerMap = new HashMap<Marker, Object>();

        mClusterManager = new ClusterManager<Node>(getActivity(), getMap());
        getMap().setOnCameraChangeListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);

        getMap().setMyLocationEnabled(true);
        LatLng germany = new LatLng(51, 9);
        getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(germany, 6));

        RequestQueue rq = Volley.newRequestQueue(this.getActivity().getApplicationContext());
        NodesResponse nr = new NodesResponse(new NodesResponse.Callbacks() {
            @Override
            public void onNodeAvailable(Node node) {
                if (node.getGeo() != null) {
                    //Marker marker = getMap().addMarker(new MarkerOptions().title(node.getName()).position(node.getGeo()));
                    //markerMap.put(marker, node);

                    mClusterManager.addItem(node);

                }
            }
        });
        for (MapMaster m: MapMaster.maps) {
            rq.add(new JsonObjectRequest(m.getMapUrl(), null, nr, nr));
       }
    }


    private void createCommunityMap() {
        markerMap = new HashMap<Marker, Object>();
        getMap().setMyLocationEnabled(true);
        LatLng germany = new LatLng(51, 9);
        getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(germany, 6));

        final IconGenerator ig = new IconGenerator(getActivity().getApplicationContext());
        ig.setStyle(IconGenerator.STYLE_GREEN);

        for (Community c: Community.communities) {
            GoogleMap map = getMap();
            MarkerOptions markerOptions = new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromBitmap(ig.makeIcon(c.getAddressCity()))).
                    position(new LatLng(c.getLat(), c.getLon())).
                    anchor(ig.getAnchorU(), ig.getAnchorV());

            Marker marker = map.addMarker(markerOptions);
            markerMap.put(marker, c);
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick");
        mCallbacks.onMarkerClicked(markerMap.get(marker));
        return true;
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

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        public void onMarkerClicked(Object o);
    }
}