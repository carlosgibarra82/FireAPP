package com.optic.fireapp.models;

/**
 * Created by User on 29/01/2018.
 */
public class Report {
    private String id_user;
    private String hour;
    private String image;
    private String date;
    private String description;
    private double lat;
    private double lng;
    private long timestamp;

    public Report() {

    }

    public Report(String id_user, String hour, String image, String date, String description, double lat, double lng, long timestamp) {
        this.id_user = id_user;
        this.hour = hour;
        this.image = image;
        this.date = date;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
