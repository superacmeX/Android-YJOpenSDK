package com.acme.opensdk.demo.ui.player;

import android.os.*;

public class PlayerParam implements Parcelable {
    public String deviceName;
    public String productKey;


    protected PlayerParam(Parcel in) {
        deviceName = in.readString();
        productKey = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceName);
        dest.writeString(productKey);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PlayerParam> CREATOR = new Creator<PlayerParam>() {
        @Override
        public PlayerParam createFromParcel(Parcel in) {
            return new PlayerParam(in);
        }

        @Override
        public PlayerParam[] newArray(int size) {
            return new PlayerParam[size];
        }
    };
}
