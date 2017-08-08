package com.villip.bluelinergpstracker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by villip on 25.07.2017.
 */

public class GPSPost {
    @SerializedName("imei")
    @Expose
    private String imei;
    @SerializedName("longitude")
    @Expose
    private double longitude;
    @SerializedName("latitude")
    @Expose
    private double latitude;
    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("speed")
    @Expose
    private String speed;

    public GPSPost() {
    }

    public GPSPost(String imei, double x, double y, String strTime, String strSpeed) {
        this.imei = imei;
        longitude = x;
        latitude = y;
        time = strTime;
        speed = strSpeed;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }
}
