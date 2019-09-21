package com.optic.fireapp.models;

/**
 * Created by User on 29/01/2018.
 */
public class Message {
    private String id_admin;
    private String id_chat;
    private String id_user;
    private String message;
    private boolean seenByadmin;
    private boolean seenByuser;
    private boolean sendByUser;
    private boolean sendByAdmin;
    private long timestamp;

    public Message() {

    }

    public Message(String id_admin, String id_chat, String id_user, String message, boolean seenByadmin, boolean seenByuser, boolean sendByUser, boolean sendByAdmin, long timestamp) {
        this.id_admin = id_admin;
        this.id_chat = id_chat;
        this.id_user = id_user;
        this.message = message;
        this.seenByadmin = seenByadmin;
        this.seenByuser = seenByuser;
        this.sendByUser = sendByUser;
        this.sendByAdmin = sendByAdmin;
        this.timestamp = timestamp;
    }

    public String getId_admin() {
        return id_admin;
    }

    public void setId_admin(String id_admin) {
        this.id_admin = id_admin;
    }

    public String getId_chat() {
        return id_chat;
    }

    public void setId_chat(String id_chat) {
        this.id_chat = id_chat;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeenByadmin() {
        return seenByadmin;
    }

    public void setSeenByadmin(boolean seenByadmin) {
        this.seenByadmin = seenByadmin;
    }

    public boolean isSeenByuser() {
        return seenByuser;
    }

    public void setSeenByuser(boolean seenByuser) {
        this.seenByuser = seenByuser;
    }

    public boolean isSendByUser() {
        return sendByUser;
    }

    public void setSendByUser(boolean sendByUser) {
        this.sendByUser = sendByUser;
    }

    public boolean isSendByAdmin() {
        return sendByAdmin;
    }

    public void setSendByAdmin(boolean sendByAdmin) {
        this.sendByAdmin = sendByAdmin;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
