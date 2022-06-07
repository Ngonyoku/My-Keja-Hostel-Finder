package com.kbanda_projects.mykeja.models;

public class Feedback {
    private String comment;
    private String timeInMillis;
    private String userId;
    private boolean viewed;

    public Feedback(String comment, String timeInMillis, String userId, boolean viewed) {
        this.comment = comment;
        this.timeInMillis = timeInMillis;
        this.userId = userId;
        this.viewed = viewed;
    }

    public Feedback() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(String timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "comment='" + comment + '\'' +
                ", timeInMillis='" + timeInMillis + '\'' +
                ", userId='" + userId + '\'' +
                ", viewed=" + viewed +
                '}';
    }
}
