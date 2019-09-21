package com.optic.fireapp.models;

public class Service {

    private String id;
    private String name_geofire;
    private String name;

    public Service(String id, String name_geofire, String name) {
        this.id = id;
        this.name_geofire = name_geofire;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName_geofire() {
        return name_geofire;
    }

    public void setName_geofire(String name_geofire) {
        this.name_geofire = name_geofire;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
