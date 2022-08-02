package com.example.massengerapp.customadapter;

public class People {

    private String img_profile,name,date,id,message;

    public People() {
    }

    public People(String img_profile, String name, String date, String id, String message) {
        this.img_profile = img_profile;
        this.name = name;
        this.date = date;
        this.id = id;
        this.message = message;
    }

    public String getImg_profile() {
        return img_profile;
    }

    public void setImg_profile(String img_profile) {
        this.img_profile = img_profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
