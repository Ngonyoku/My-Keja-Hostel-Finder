package com.kbanda_projects.mykeja.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Hostel implements Serializable {
    private String name;
    private String ownerId;
    private String propertyType;
    private String ratings;
    private String rentPricePerMonth;
    private String roomType;
    private List<String> tags;
    private String totalRoomsAvailable;
    private String description;
    private boolean hasField;
    private boolean hasParking;
    private boolean hasWifi;
    private List<String> imageUrls;
    private boolean isVacant;
    private Map<String, String> locationInfo;

    public Hostel(
            String description,
            boolean hasField,
            boolean hasParking,
            boolean hasWifi,
            List<String> imageUrls,
            boolean isVacant,
            Map<String, String> locationInfo,
            String name, String ownerId,
            String propertyType,
            String ratings,
            String rentPricePerMonth,
            String roomType,
            List<String> tags,
            String totalRoomsAvailable
    ) {
        this.description = description;
        this.hasField = hasField;
        this.hasParking = hasParking;
        this.hasWifi = hasWifi;
        this.imageUrls = imageUrls;
        this.isVacant = isVacant;
        this.locationInfo = locationInfo;
        this.name = name;
        this.ownerId = ownerId;
        this.propertyType = propertyType;
        this.ratings = ratings;
        this.rentPricePerMonth = rentPricePerMonth;
        this.roomType = roomType;
        this.tags = tags;
        this.totalRoomsAvailable = totalRoomsAvailable;
    }

    public Hostel(String ownerId, String name, List<String> imageUrls) {
        this.imageUrls = imageUrls;
        this.name = name;
        this.ownerId = ownerId;
    }

    public Hostel() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHasField() {
        return hasField;
    }

    public void setHasField(boolean hasField) {
        this.hasField = hasField;
    }

    public boolean isHasParking() {
        return hasParking;
    }

    public void setHasParking(boolean hasParking) {
        this.hasParking = hasParking;
    }

    public boolean isHasWifi() {
        return hasWifi;
    }

    public void setHasWifi(boolean hasWifi) {
        this.hasWifi = hasWifi;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public boolean isVacant() {
        return isVacant;
    }

    public void setVacant(boolean vacant) {
        isVacant = vacant;
    }

    public Map<String, String> getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(Map<String, String> locationInfo) {
        this.locationInfo = locationInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getRatings() {
        return ratings;
    }

    public void setRatings(String ratings) {
        this.ratings = ratings;
    }

    public String getRentPricePerMonth() {
        return rentPricePerMonth;
    }

    public void setRentPricePerMonth(String rentPricePerMonth) {
        this.rentPricePerMonth = rentPricePerMonth;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getTotalRoomsAvailable() {
        return totalRoomsAvailable;
    }

    public void setTotalRoomsAvailable(String totalRoomsAvailable) {
        this.totalRoomsAvailable = totalRoomsAvailable;
    }

    @Override
    public String toString() {
        return "Hostel{" +
                "description='" + description + '\'' +
                ", hasField=" + hasField +
                ", hasParking=" + hasParking +
                ", hasWifi=" + hasWifi +
                ", imageUrls=" + imageUrls +
                ", isVacant=" + isVacant +
                ", locationInfo=" + locationInfo +
                ", name='" + name + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", propertyType='" + propertyType + '\'' +
                ", ratings='" + ratings + '\'' +
                ", rentPricePerMonth='" + rentPricePerMonth + '\'' +
                ", roomType='" + roomType + '\'' +
                ", tags=" + tags +
                ", totalRoomsAvailable='" + totalRoomsAvailable + '\'' +
                '}';
    }
}
