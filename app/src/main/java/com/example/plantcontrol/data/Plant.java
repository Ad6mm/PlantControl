package com.example.plantcontrol.data;

import android.net.Uri;

import java.util.Date;

public class Plant {
    String name;
    String customName = null;
    Boolean isInside;
    Uri imageUri = null;
    Integer wateringInterval;
    Date plantDate = null;
    Date lastWateringDate;
    String description = null;

    public Plant(String name, String customName, Boolean isInside, Uri imageUri, Integer wateringInterval, Date plantDate, Date lastWateringDate, String description) {
        this.name = name;
        this.customName = customName;
        this.isInside = isInside;
        this.imageUri = imageUri;
        this.wateringInterval = wateringInterval;
        this.plantDate = plantDate;
        this.lastWateringDate = lastWateringDate;
        this.description = description;
    }

    public Plant(String name, Boolean isInside, Integer wateringInterval, Date lastWateringDate) {
        this.name = name;
        this.isInside = isInside;
        this.wateringInterval = wateringInterval;
        this.lastWateringDate = lastWateringDate;
    }

    public String getName() {
        return name;
    }

    public String getCustomName() {
        return customName;
    }

    public Boolean getInside() {
        return isInside;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public Integer getWateringInterval() {
        return wateringInterval;
    }

    public Date getPlantDate() {
        return plantDate;
    }

    public Date getLastWateringDate() {
        return lastWateringDate;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public void setInside(Boolean inside) {
        isInside = inside;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public void setWateringInterval(Integer wateringInterval) {
        this.wateringInterval = wateringInterval;
    }

    public void setPlantDate(Date plantDate) {
        this.plantDate = plantDate;
    }

    public void setLastWateringDate(Date lastWateringDate) {
        this.lastWateringDate = lastWateringDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
