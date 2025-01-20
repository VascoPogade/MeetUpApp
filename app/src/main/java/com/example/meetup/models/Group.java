package com.example.meetup.models;

import com.google.firebase.Timestamp;

import java.util.List;

public class Group {
    private List<String> members;
    private List<String> commonInterests;
    private Timestamp createdAt;
    private String bar;

    public Group() {}

    public Group(List<String> members, List<String> commonInterests, Timestamp createdAt, String bar) {
        this.members = members;
        this.commonInterests = commonInterests;
        this.createdAt = createdAt;
        this.bar = bar;
    }

    // Getters and setters
    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public List<String> getCommonInterests() {
        return commonInterests;
    }

    public void setCommonInterests(List<String> commonInterests) {
        this.commonInterests = commonInterests;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getBar() {
        return bar;
    }

    public void setBar(String bar) {
        this.bar = bar;
    }
}
