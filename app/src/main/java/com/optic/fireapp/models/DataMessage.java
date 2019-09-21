package com.optic.fireapp.models;

import java.util.Map;

public class DataMessage {

    public String to;
    public String priority;
    public String ttl;
    public Map<String, String> data;
    private Map<String, String> notification;


    public DataMessage() {
    }

    public DataMessage(String to, Map<String, String> data) {
        this.to = to;
        this.data = data;
    }

    public DataMessage(String to, String priority, String ttl, Map<String, String> data) {
        this.to = to;
        this.priority = priority;
        this.ttl = ttl;
        this.data = data;
    }

    public DataMessage(String to, String priority, String ttl, Map<String, String> data, Map<String, String> notification) {
        this.to = to;
        this.priority = priority;
        this.ttl = ttl;
        this.data = data;
        this.notification = notification;
    }

    public Map<String, String> getNotification() {
        return notification;
    }

    public void setNotification(Map<String, String> notification) {
        this.notification = notification;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }
}
