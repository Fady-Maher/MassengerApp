package com.example.massengerapp.classes;

public class LastMessage {
    String message, time, id, date;

    public LastMessage() {
    }

    public LastMessage(String message, String time, String id) {
        this.message = message;
        this.time = time;
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
