package com.beastpotato.cast.chromcastscheduler.models;

/**
 * Created by Oleksiy on 8/25/2016.
 */

public class ScheduledItem {
    public int id;
    public String name;
    public String deviceId;
    public String deciveName;
    public String url;
    public int hour, minute;

    public ScheduledItem(String name, String deviceId, String deviceName, String url, int hour, int minute) {
        this.name = name;
        this.deviceId = deviceId;
        this.deciveName = deviceName;
        this.url = url;
        this.hour = hour;
        this.minute = minute;
    }
}
