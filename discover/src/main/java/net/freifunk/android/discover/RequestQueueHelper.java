package net.freifunk.android.discover;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.Volley;

import net.freifunk.android.discover.model.NodeMap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import net.freifunk.android.discover.Main.*;

/**
 * Created by bjoern petri on 12/16/14.
 */
public class RequestQueueHelper {

    private static RequestQueueHelper RequestQueueHelper = null;
    private RequestQueue mRequestQueue = null;
    private Context context;
    private SharedPreferences sharedPrefs;
    private ConnectivityManager connManager;
    private AtomicInteger requestInProgress;
    private Main main;

    private RequestQueueHelper(Context context)
    {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized RequestQueueHelper getInstance() {
        return RequestQueueHelper;
    }

    public static synchronized RequestQueueHelper getInstance(Main main) {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (RequestQueueHelper == null && main != null) {
            RequestQueueHelper = new RequestQueueHelper(main.getApplicationContext());
            RequestQueueHelper.context = main.getApplicationContext();
            RequestQueueHelper.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(main);
            RequestQueueHelper.connManager = (ConnectivityManager) main.getSystemService(main.getBaseContext().CONNECTIVITY_SERVICE);            ;
            RequestQueueHelper.main = main;
            RequestQueueHelper.requestInProgress = new AtomicInteger(0);
        }

        return RequestQueueHelper;
    }

    public Request add(Request req)
    {
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean sync_wifi = sharedPrefs.getBoolean("sync_wifi", true);

        if (connManager.getActiveNetworkInfo() != null && (sync_wifi == false || mWifi.isConnected() == true)) {
            if (requestInProgress.incrementAndGet() > 0) {
                main.runOnUiThread(new Runnable() {
                    public void run() {
                        main.setRefreshActionButtonState(true);
                    }
                });
            }
           return RequestQueueHelper.mRequestQueue.add(req);
        }

        return null;
    }

    public void RequestDone()
    {
        if (requestInProgress.decrementAndGet() == 0) {
            main.runOnUiThread(new Runnable() {
                public void run() {
                    main.setRefreshActionButtonState(false);
                }
            });
        }
    }


    public int size()
    {
        return requestInProgress.intValue();
    }

}
