package com.beastpotato.cast.chromcastscheduler.models;

/**
 * Created by Oleksiy on 8/25/2016.
 */

public class ScheduledItem {
    public int id;
    public String name;
    public String url;
    public int hour, minute;

    public ScheduledItem(String name, String url, int hour, int minute) {
        this.name = name;
        this.url = url;
        this.hour = hour;
        this.minute = minute;
    }
}
