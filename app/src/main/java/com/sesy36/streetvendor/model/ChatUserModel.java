package com.sesy36.streetvendor.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class ChatUserModel {

    private String id, lastMessage, messageCount;
    private List<String> uid;

    @ServerTimestamp
    private Date time;


    public ChatUserModel() {

    }


    public ChatUserModel(String id, String lastMessage, String messageCount, List<String> uid, Date time) {
        this.id = id;
        this.lastMessage = lastMessage;
        this.messageCount = messageCount;
        this.uid = uid;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(String messageCount) {
        this.messageCount = messageCount;
    }

    public List<String> getUid() {
        return uid;
    }

    public void setUid(List<String> uid) {
        this.uid = uid;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
