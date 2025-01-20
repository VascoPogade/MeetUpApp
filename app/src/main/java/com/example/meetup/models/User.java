package com.example.meetup.models;

import java.util.List;

public class User {
    private String uid;
    private String name;
    private int age;
    private List<String> interests;

    public User() {}

    public User(String uid, String name, int age, List<String> interests) {
        this.uid = uid;
        this.name = name;
        this.age = age;
        this.interests = interests;
    }

    // Getters and setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
}