package com.superacm.demo.home.video.data;

public class EventDetailBean {
    // 事件id
    private String eventId;
    // 产品编码key
    private String productKey;
    // 产品编码名，与产品编码key配套，设备唯一
    private String deviceName;
    // 在平台的设备唯一标识符
    private String deviceId;
    // 告警名称，如人形侦测，移动侦测等
    private String alarmName;
    // 事件类型。1（alert）；2（info）；3（error）……
    private Integer eventType;
    // 告警类型。1（表示移动侦测）；2（表示声音侦测）；3（表示人形侦测）……
    private Integer alarmType;
    // 设备别名,可设置的名称
    private String deviceNickname;
    // 事件名称
    private String eventName;
    // 事件标识
    private String identifier;
    // 设备上报事件的具体内容
    private String payload;
    // 图片url
    private String picUrl;
    // 视频url
    private String videoUrl;
    // 发生时间，unix时间戳,服务器时区,东八区
    private long time;
    // 事件发生时间，格式为yyyy-MM-ss HH:mm:ss。
    private String eventTime;
    // 事件发生UTC时间，格式为yyyy-MM-ssTHH:mm:ssZ。
    private String eventTimeUTC;
    // 图标url
    private String deviceIcon;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getProductKey() {
        return productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAlarmName() {
        return alarmName;
    }

    public void setAlarmName(String alarmName) {
        this.alarmName = alarmName;
    }

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public Integer getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(Integer alarmType) {
        this.alarmType = alarmType;
    }

    public String getDeviceNickname() {
        return deviceNickname;
    }

    public void setDeviceNickname(String deviceNickname) {
        this.deviceNickname = deviceNickname;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventTimeUTC() {
        return eventTimeUTC;
    }

    public void setEventTimeUTC(String eventTimeUTC) {
        this.eventTimeUTC = eventTimeUTC;
    }

    public String getDeviceIcon() {
        return deviceIcon;
    }

    public void setDeviceIcon(String deviceIcon) {
        this.deviceIcon = deviceIcon;
    }

    @Override
    public String toString() {
        return "EventDetailBean{" +
                "eventId='" + eventId + '\'' +
                ", productKey='" + productKey + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", alarmName='" + alarmName + '\'' +
                ", eventType=" + eventType +
                ", alarmType=" + alarmType +
                ", deviceNickname='" + deviceNickname + '\'' +
                ", eventName='" + eventName + '\'' +
                ", identifier='" + identifier + '\'' +
                ", payload='" + payload + '\'' +
                ", picUrl='" + picUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", time=" + time +
                ", eventTime='" + eventTime + '\'' +
                ", eventTimeUTC='" + eventTimeUTC + '\'' +
                ", deviceIcon='" + deviceIcon + '\'' +
                '}';
    }
}
