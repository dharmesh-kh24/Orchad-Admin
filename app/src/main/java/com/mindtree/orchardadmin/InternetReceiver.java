package com.mindtree.orchardadmin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by M1036144 on 10-Apr-16.
 */
public class InternetReceiver extends BroadcastReceiver {
    public static String isConnected = Constants.CONNECTED;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        CheckInternet checkInternet = new CheckInternet(context);
        if (checkInternet.isConnectingToInternet()) {
            Log.i(Constants.NET, Constants.CONNECTED);
            Intent i = new Intent();
            i.setAction(isConnected);
            context.sendBroadcast(i);
        } else {
            Log.i(Constants.NET, Constants.NOT_CONNECTED);
        }
    }
}
