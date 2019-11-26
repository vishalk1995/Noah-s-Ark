package com.mobilecomputing.noahsark.connectionUtil;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class PSQLUtil{
    private static PSQLUtil instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private PSQLUtil(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized PSQLUtil getInstance(Context context) {
        if (instance == null) {
            instance = new PSQLUtil(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        Log.i("REST", "adding request: "+req.toString());
        getRequestQueue().add(req);
    }
}