package com.kbanda_projects.mykeja.models;

public class BookMark {
    private String userId;
    private String hostelId;

    public BookMark(String userId, String hostelId) {
        this.userId = userId;
        this.hostelId = hostelId;
    }

    public BookMark() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHostelId() {
        return hostelId;
    }

    public void setHostelId(String hostelId) {
        this.hostelId = hostelId;
    }

    @Override
    public String toString() {
        return "BookMark{" +
                "userId='" + userId + '\'' +
                ", hostelId='" + hostelId + '\'' +
                '}';
    }
}
