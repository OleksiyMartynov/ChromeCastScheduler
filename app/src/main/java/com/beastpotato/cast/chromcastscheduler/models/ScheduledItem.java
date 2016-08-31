package com.beastpotato.cast.chromcastscheduler.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Oleksiy on 8/25/2016.
 */

@DatabaseTable
public class ScheduledItem implements Parcelable {
    public static final Parcelable.Creator<ScheduledItem> CREATOR = new Parcelable.Creator<ScheduledItem>() {
        public ScheduledItem createFromParcel(Parcel source) {
            return new ScheduledItem(source);
        }

        public ScheduledItem[] newArray(int size) {
            return new ScheduledItem[size];
        }
    };
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

    protected ScheduledItem(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.deviceId = in.readString();
        this.deciveName = in.readString();
        this.url = in.readString();
        this.hour = in.readInt();
        this.minute = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.deviceId);
        dest.writeString(this.deciveName);
        dest.writeString(this.url);
        dest.writeInt(this.hour);
        dest.writeInt(this.minute);
    }
}
