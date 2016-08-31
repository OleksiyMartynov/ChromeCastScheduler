package com.beastpotato.cast.chromcastscheduler.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.beastpotato.cast.chromcastscheduler.models.ScheduledItem;
import com.beastpotato.cast.chromcastscheduler.receivers.AlarmReceiver;
import com.beastpotato.cast.chromcastscheduler.services.MyIntentService;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Oleksiy on 8/30/2016.
 */

public class Utils {
    public static void setAlarmsForScheduledItems(Context context, List<ScheduledItem> items) {
        for (ScheduledItem item : items) {
            setAlarmForScheduledItem(context, item);
        }
    }

    public static void setAlarmForScheduledItem(Context context, ScheduledItem item) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = getPendingIntent(context, item);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, item.hour);
        calendar.set(Calendar.MINUTE, item.minute);
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                alarmIntent);
    }


    public static void cancelAlarm(Context context, ScheduledItem item) {
        PendingIntent alarmIntent = getPendingIntent(context, item);
        cancelAlarm(context, alarmIntent);
    }

    public static void cancelAlarm(Context context, PendingIntent alarmIntent) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(alarmIntent);
    }

    public static PendingIntent getPendingIntent(Context context, ScheduledItem item) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(MyIntentService.EXTRA_ITEM_ID, item.id);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        return alarmIntent;
    }
}
