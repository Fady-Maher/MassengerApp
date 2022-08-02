package com.example.massengerapp.customadapter;

public class Person {

    private String img_profile,name,email,pass,date,id,tokenID ;

    public Person() {
    }

    public Person(String img_profile, String name, String date, String id,String token) {
        this.img_profile = img_profile;
        this.name = name;
        this.date = date;
        this.id = id;
        this.tokenID = token;
    }

    public Person(String img_profile, String name, String email, String pass, String date, String id) {
        this.img_profile = img_profile;
        this.name = name;
        this.email = email;
        this.pass = pass;
        this.date = date;
        this.id = id;
    }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
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

}
