/*
 * Main.java
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

package net.freifunk.android.discover;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Subscribe;

import net.freifunk.android.discover.async.LoadNodeMap;
import net.freifunk.android.discover.async.NodeMapResponse;
import net.freifunk.android.discover.helper.Database;
import net.freifunk.android.discover.helper.EventBus;
import net.freifunk.android.discover.helper.RequestQueue;
import net.freifunk.android.discover.map.GmapsFragment;
import net.freifunk.android.discover.model.MapMaster;
import net.freifunk.android.discover.model.NodeMap;
import net.freifunk.android.discover.model.NodeResult;

import java.util.HashMap;


public class Main extends ActionBarActivity implements GmapsFragment.Callbacks {

    private Menu optionsMenu;
    private static final int RESULT_SETTINGS = 1;
    private static final String TAG = Main.class.getName();

    private Handler updateHandler;
    private Runnable updateTask;

    private static volatile HashMap<String, NodeMap> mapsLoading = null;
    private static volatile HashMap<String, NodeMap> mapsSaving = null;

    //final String URL = "https://raw.githubusercontent.com/NiJen/AndroidFreifunkNord/master/MapUrls.json";
    final String URL = "http://localhost:8080/ffdbg/MapUrls.json";

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private static void setDefaultUncaughtExceptionHandler() {
        try {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread t, Throwable e) {

                    Log.e("EXCEPTION", "Uncaught Exception detected in thread {}" + t + " -- " + e);
                    Log.e("EXCEPTION", "STACK", e);

                    //e.getStackTrace()
                }
            });
        } catch (SecurityException e) {
            Log.e("EXCEPTION", "Could not set the Default Uncaught Exception Handler", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultUncaughtExceptionHandler();

        setContentView(R.layout.activity_main);

        mTitle = getTitle();
        updateHandler = new Handler();
        updateTask = new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                MapMaster mapMaster = MapMaster.getInstance();

                int sync_frequency = Integer.parseInt(sharedPrefs.getString("sync_frequency", "0"));

                for (NodeMap map : mapMaster.getMaps().values()) {
                    EventBus.getInstance().post(new NodeResult(NodeResult.NodeResultType.LOAD_NODES, map));
                }

                if (sync_frequency > 0)
                    updateHandler.postDelayed(updateTask, sync_frequency * 60 * 1000);
            }
        };




        mapsLoading = new HashMap<String, NodeMap>(20);
        mapsSaving = new HashMap<String, NodeMap>(20);

        /* initialize singletons */
        EventBus.getInstance().register(this);

        Database database = Database.getInstance(this.getApplicationContext());
        RequestQueue requestHelper = RequestQueue.getInstance(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, GmapsFragment.newInstance()).commit();

        new LoadNodeMap().executeAsyncTask();
    }

    @Override
    protected void onDestroy() {
        EventBus.getInstance().unregister(this);
        super.onDestroy();
    }


    private LatLng getLocation() {
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());

        // Showing status
        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else { // Google Play Services are available

            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location
            Location location = locationManager.getLastKnownLocation(provider);
            return new LatLng(location.getLatitude(), location.getLongitude());
        }
        return new LatLng(0, 0);
    }


    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.optionsMenu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        restoreActionBar();

        return true;
    }

    public void setRefreshActionButtonState(final boolean refreshing) {

        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu.findItem(R.id.action_reload);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.activity_main_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case R.id.action_reload:
                if (mapsSaving.isEmpty() && updateTask != null) {
                    updateHandler.removeCallbacks(updateTask);
                    updateTask.run();
                } else {
                    Log.w(TAG, "No action performed at the moment - QueueSize is " + mapsSaving.size());
                }
                break;

            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMarkerClicked(Object o) {
    }


    @Subscribe
    public synchronized void onNodeMapResult(NodeResult nodeResult) {

        NodeResult.NodeResultType result = nodeResult.getResultType();
        MapMaster mapMaster = MapMaster.getInstance();

        /* database loading of map finished */
        if (result == NodeResult.NodeResultType.LOAD_MAP) {

            for (NodeMap map : nodeResult.getResults().values()) {
                mapMaster.put(map.getMapName(), map);

                if (map.isActive()) {
                    mapsLoading.put(map.getMapName(), map);
                    map.loadNodes();
                }
            }
        }
        /* web loading of map finished */
        else if (result == NodeResult.NodeResultType.UPDATE_MAP) {
            for (NodeMap map : nodeResult.getResults().values()) {
                if (map.isActive()) {
                    mapsSaving.put(map.getMapName(), map);
                    map.updateNodes();
                }
            }
        }
        /* database loading of nodes finished */
        else if (result == NodeResult.NodeResultType.LOAD_NODES) {
            NodeMap map = nodeResult.getResult();

            if (mapsLoading.containsValue(map)) {
                mapsLoading.remove(map.getMapName());

                if (mapsLoading.isEmpty()) {
                    setRefreshActionButtonState(true);
                    RequestQueue requestHelper = RequestQueue.getInstance();

                    /* load from web */
                    NodeMapResponse nr = new NodeMapResponse(mapMaster.getMaps());
                    JsonObjectRequest request = new JsonObjectRequest(URL, null, nr, nr);

                    requestHelper.add(request);
                }
            }
        }
        /* web loading of nodes finished */
        else if (result == NodeResult.NodeResultType.UPDATE_NODES) {
            NodeMap map = nodeResult.getResult();
            map.saveNodes();
        }
        /* database saving of nodes finished */
        else if (result == NodeResult.NodeResultType.SAVE_NODES) {
            mapsSaving.remove(nodeResult.getResult().getMapName());

            if (mapsSaving.isEmpty()) {
                setRefreshActionButtonState(false);
            }
        }
    }
}
