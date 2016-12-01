package com.android.launcher3.much;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MuchNavBroadcast extends BroadcastReceiver {

    private static final String ACTION_NAVIGATION_BAR_SHOW = "much.display_navigation";
    private static final String ACTION_NAVIGATION_BAR_HIDE = "much.dis_hide_navigation";

    public static final String ACTION_SHOW_NAVIGATION_BAR = "ireadygo.intent.action.SHOW_NAVIGATION_BAR";
    public static final String ACTION_HIDE_NAVIGATION_BAR = "ireadygo.intent.action.HIDE_NAVIGATION_BAR";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent converted = new Intent();
        if (ACTION_NAVIGATION_BAR_SHOW.equals(intent.getAction())) {
            converted.setAction(ACTION_SHOW_NAVIGATION_BAR);
            sendLocalBroadcast(context, converted);
        } else if (ACTION_NAVIGATION_BAR_HIDE.equals(intent.getAction())) {
            converted.setAction(ACTION_HIDE_NAVIGATION_BAR);
            sendLocalBroadcast(context, converted);
        }
    }

    private void sendLocalBroadcast(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
