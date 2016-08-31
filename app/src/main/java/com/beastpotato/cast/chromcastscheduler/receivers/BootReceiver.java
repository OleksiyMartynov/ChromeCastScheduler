package com.beastpotato.cast.chromcastscheduler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.beastpotato.cast.chromcastscheduler.managers.DatabaseManager;
import com.beastpotato.cast.chromcastscheduler.models.ScheduledItem;
import com.beastpotato.cast.chromcastscheduler.utils.Utils;

import java.util.List;

/**
 * Created by Oleksiy on 8/30/2016.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            List<ScheduledItem> items = DatabaseManager.getInstance(context).getScheduledItems();
            Utils.setAlarmsForScheduledItems(context, items);
        }
    }
}
