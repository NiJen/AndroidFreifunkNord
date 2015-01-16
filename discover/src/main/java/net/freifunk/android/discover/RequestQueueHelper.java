package net.freifunk.android.discover;

import android.app.DownloadManager;
import android.content.Context;

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
    private AtomicInteger requestInProgress;

    private RequestQueueHelper(Context context)
    {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized RequestQueueHelper getInstance(Context context) {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (RequestQueueHelper == null && context != null) {
            RequestQueueHelper = new RequestQueueHelper(context);
            RequestQueueHelper.requestInProgress = new AtomicInteger(0);
        }

        return RequestQueueHelper;
    }

    public Request add(Request req)
    {
        requestInProgress.incrementAndGet();
        return RequestQueueHelper.mRequestQueue.add(req);
    }

    public void RequestDone()
    {
        requestInProgress.decrementAndGet();
    }


    public int size()
    {
        return requestInProgress.intValue();
    }

}
