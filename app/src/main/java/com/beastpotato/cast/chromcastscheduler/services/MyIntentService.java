package com.beastpotato.cast.chromcastscheduler.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.media.MediaRouter;
import android.widget.Toast;

import com.beastpotato.cast.chromcastscheduler.managers.CastManager;
import com.beastpotato.cast.chromcastscheduler.managers.DatabaseManager;
import com.beastpotato.cast.chromcastscheduler.models.ScheduledItem;
import com.beastpotato.cast.chromcastscheduler.receivers.AlarmReceiver;
import com.beastpotato.cast.chromcastscheduler.utils.Utils;

import java.util.List;

public class MyIntentService extends IntentService {
    public static final String ACTION_RUN_ITEM = "action_run_item";
    public static final String EXTRA_ITEM_ID = "extra_item_id";

    public MyIntentService() {
        super("MyIntentService");
    }

    public static void startActionRunItem(Context context, int itemId) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_RUN_ITEM);
        intent.putExtra(EXTRA_ITEM_ID, itemId);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RUN_ITEM.equals(action)) {
                final int param1 = intent.getIntExtra(EXTRA_ITEM_ID, -1);
                handleActionRunItem(param1);
            }
        }
    }

    private void handleActionRunItem(final int itemId) {
        if (itemId != -1) {
            final Context context = getApplicationContext();
            final ScheduledItem item = DatabaseManager.getInstance(context).getScheduledItem(itemId);
            if (item != null) {
                CastManager.OnDeviceListUpdateListener deviceListUpdateListener = null;
                final CastManager.OnDeviceListUpdateListener finalDeviceListUpdateListener = deviceListUpdateListener;
                deviceListUpdateListener = new CastManager.OnDeviceListUpdateListener() { //will get all devices if not already scanned
                    @Override
                    public void onDeviceListUpdate(List<MediaRouter.RouteInfo> list, MediaRouter.RouteInfo deviceDelta) {
                        for (MediaRouter.RouteInfo routeInfo : list) {
                            if (routeInfo.getId().equals(item.deviceId)) {
                                CastManager.getInstance(context).removeOnDeviceListChangeListener(finalDeviceListUpdateListener);
                                try {
                                    CastManager.getInstance(getApplicationContext()).playVideo(getApplicationContext(), item.url, item.deviceId);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Failed to run scheduled item.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                };
                CastManager.getInstance(context).addOnDeviceListUpdateListener(deviceListUpdateListener);
                processDeviceList(CastManager.getInstance(context).getDeviceRoutes(), item);// process if already scanned for devices.

            } else {// item been deleted from db
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra(MyIntentService.EXTRA_ITEM_ID, itemId);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
                Utils.cancelAlarm(getApplicationContext(), alarmIntent);
            }
        }
    }

    private void processDeviceList(List<MediaRouter.RouteInfo> list, ScheduledItem item) {
        for (MediaRouter.RouteInfo routeInfo : list) {
            if (routeInfo.getId().equals(item.deviceId)) {
                try {
                    CastManager.getInstance(getApplicationContext()).playVideo(getApplicationContext(), item.url, item.deviceId);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Failed to run scheduled item.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
