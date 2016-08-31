package com.beastpotato.cast.chromcastscheduler.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.beastpotato.cast.chromcastscheduler.services.MyIntentService;

/**
 * Created by Oleksiy on 8/30/2016.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MyIntentService.startActionRunItem(context, intent.getIntExtra(MyIntentService.EXTRA_ITEM_ID, -1));
    }
}
