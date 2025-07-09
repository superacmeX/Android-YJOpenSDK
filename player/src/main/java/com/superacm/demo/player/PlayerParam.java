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
    
    // 新增参数
    public String deviceId;
    public String channelId;
    public int quality; // 视频质量
    public boolean enableAudio; // 是否启用音频
    public boolean enableVideo; // 是否启用视频
    public int timeout; // 超时时间

    public void setTimeRange(long startSec, long endSec) {
        this.startSec = startSec;
        this.endSec = endSec;
    }

    public void setCvodUrl(String url, String ext) {
        this.cvodUrl = url;
        this.ext = ext;
    }

    public PlayerParam(String deviceName, String productKey) {
        this.deviceName = deviceName;
        this.productKey = productKey;
        this.enableAudio = true;
        this.enableVideo = true;
        this.quality = 0; // 默认质量
        this.timeout = 30000; // 默认30秒超时
    }
    
    public PlayerParam(String deviceName, String productKey, String deviceId) {
        this(deviceName, productKey);
        this.deviceId = deviceId;
    }

    protected PlayerParam(Parcel in) {
        deviceName = in.readString();
        productKey = in.readString();
        startSec = in.readLong();
        endSec = in.readLong();
        cvodUrl = in.readString();
        ext = in.readString();
        deviceId = in.readString();
        channelId = in.readString();
        quality = in.readInt();
        enableAudio = in.readByte() != 0;
        enableVideo = in.readByte() != 0;
        timeout = in.readInt();

        Log.i(TAG, String.format("unmarshal param %s/%s %d-%d, cvod=%s/%s, deviceId=%s, quality=%d, audio=%b, video=%b",
                deviceName, productKey, startSec, endSec, cvodUrl, ext, deviceId, quality, enableAudio, enableVideo));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceName);
        dest.writeString(productKey);
        dest.writeLong(startSec);
        dest.writeLong(endSec);
        dest.writeString(cvodUrl);
        dest.writeString(ext);
        dest.writeString(deviceId);
        dest.writeString(channelId);
        dest.writeInt(quality);
        dest.writeByte((byte) (enableAudio ? 1 : 0));
        dest.writeByte((byte) (enableVideo ? 1 : 0));
        dest.writeInt(timeout);
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
    
    @Override
    public String toString() {
        return String.format("PlayerParam{deviceName='%s', productKey='%s', deviceId='%s', startSec=%d, endSec=%d, cvodUrl='%s'}",
                deviceName, productKey, deviceId, startSec, endSec, cvodUrl);
    }
}
