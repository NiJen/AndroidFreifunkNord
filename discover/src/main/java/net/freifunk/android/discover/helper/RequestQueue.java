/*
 * RequestQueueHelper.java
 *
 * Copyright (C) 2015  Bjoern Petri
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

package net.freifunk.android.discover.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.Volley;

import net.freifunk.android.discover.Main;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestQueue {

    private static RequestQueue RequestQueue = null;
    private com.android.volley.RequestQueue mRequestQueue = null;
    private Context context;
    private SharedPreferences sharedPrefs;
    private ConnectivityManager connManager;
    private AtomicInteger requestInProgress;
    private Main main;

    private RequestQueue(Context context)
    {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized RequestQueue getInstance() {
        return RequestQueue;
    }

    public static synchronized RequestQueue getInstance(Main main) {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (RequestQueue == null && main != null) {
            RequestQueue = new RequestQueue(main.getApplicationContext());
            RequestQueue.context = main.getApplicationContext();
            RequestQueue.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(main);
            RequestQueue.connManager = (ConnectivityManager) main.getSystemService(main.getBaseContext().CONNECTIVITY_SERVICE);
            RequestQueue.main = main;
            RequestQueue.requestInProgress = new AtomicInteger(0);
        }

        return RequestQueue;
    }

    public Request add(Request req)
    {
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean sync_wifi = sharedPrefs.getBoolean("sync_wifi", true);

         if (connManager.getActiveNetworkInfo() != null && (sync_wifi || mWifi.isConnected())) {

            RetryPolicy policy = new DefaultRetryPolicy(30000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            req.setRetryPolicy(policy);

        return RequestQueue.mRequestQueue.add(req);
        }

      return null;
    }

}
