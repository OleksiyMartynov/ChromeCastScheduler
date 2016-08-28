package com.beastpotato.cast.chromcastscheduler.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Oleksiy on 8/25/2016.
 */

@DatabaseTable
public class ScheduledItem {
    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField
    public String name;
    @DatabaseField
    public String deviceId;
    @DatabaseField
    public String deciveName;
    @DatabaseField
    public String url;
    @DatabaseField
    public int hour;
    @DatabaseField
    public int minute;

    public ScheduledItem() {
    }

    public ScheduledItem(String name, String deviceId, String deviceName, String url, int hour, int minute) {
        this.name = name;
        this.deviceId = deviceId;
        this.deciveName = deviceName;
        this.url = url;
        this.hour = hour;
        this.minute = minute;
    }
}
