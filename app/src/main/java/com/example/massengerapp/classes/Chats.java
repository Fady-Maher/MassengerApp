package com.example.massengerapp.classes;

public class Chats {
    String message, date,senderid ,recieveid, image, nameSender , record ;
    public Chats() {
    }

    public Chats(String message, String date, String senderid, String recieveid, String image, String nameSender,String record) {
        this.message = message;
        this.date = date;
        this.senderid = senderid;
        this.recieveid = recieveid;
        this.image = image;
        this.nameSender = nameSender;
        this.record = record;
    }


    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getRecieveid() {
        return recieveid;
    }

    public void setRecieveid(String recieveid) {
        this.recieveid = recieveid;
    }

    public String getNameSender() {
        return nameSender;
    }

    public void setNameSender(String nameSender) {
        this.nameSender = nameSender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String time) {
        this.date = time;
    }

    public String getSenderid() {
        return senderid;
    }

    public void setSenderid(String senderid) {
        this.senderid = senderid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
