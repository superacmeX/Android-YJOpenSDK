package com.superacm.demo.player;

import android.os.*;
import android.util.*;

public class PlayerParam implements Parcelable {
    private final static String TAG = "PlayerParam";

    public static final String PLAY_PARAMS = "play_params";

    public String deviceName;
    public String productKey;
    public long startSec;
    public long endSec;

    public String cvodUrl;
    public String ext;

    public void setTimeRange(long startSec, long endSec) {
        this.startSec = startSec;
        this.endSec = endSec;
    }

    public void setCvodUrl(String url, String ext) {
        this.cvodUrl    = url;
        this.ext        = ext;
    }

    public PlayerParam(String deviceName, String productKey) {
        this.deviceName = deviceName;
        this.productKey = productKey;
    }

    protected PlayerParam(Parcel in) {
        deviceName = in.readString();
        productKey = in.readString();

        startSec = in.readLong();
        endSec = in.readLong();

        cvodUrl = in.readString();
        ext = in.readString();

        Log.i(TAG, String.format("unmarshal param %s/%s %d-%d, cvod=%s/%s",
                deviceName, productKey, startSec, endSec, cvodUrl, ext));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceName);
        dest.writeString(productKey);

        dest.writeLong(startSec);
        dest.writeLong(endSec);

        dest.writeString(cvodUrl);
        dest.writeString(ext);
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
