package com.example.plantcontrol.data;

import java.net.URL;
import java.util.Date;

public class Plant {
    String name;
    Boolean isInside;
    String imageStorageKey = null;
    Integer wateringInterval;
    Date plantDate = null;
    Date lastWateringDate;
    String description = null;

    public Plant(String name, Boolean isInside, String imageStorageKey, Integer wateringInterval, Date plantDate, Date lastWateringDate, String description) {
        this.name = name;
        this.isInside = isInside;
        this.imageStorageKey = imageStorageKey;
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

    public Plant() {
        this.name = null;
        this.isInside = null;
        this.wateringInterval = null;
        this.lastWateringDate = null;
    }

    public String getName() {
        return name;
    }


    public Boolean getInside() {
        return isInside;
    }

    public String getImageStorageKey() {
        return imageStorageKey;
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

    public void setInside(Boolean inside) {
        isInside = inside;
    }

    public void setImageStorageKey(String imageStorageKey) {
        this.imageStorageKey = imageStorageKey;
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
