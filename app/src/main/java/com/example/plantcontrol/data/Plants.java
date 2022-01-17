package com.example.plantcontrol.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class Plants {
    private ArrayList<Plant> plants;

    public Plants(ArrayList<Plant> plants) {
        this.plants = plants;
    }

    public Plants() {
        this.plants = new ArrayList<>();
    }

    public void setPlants(ArrayList<Plant> plants) {
        this.plants = plants;
    }

    public ArrayList<Plant> getPlants() {
        return plants;
    }

    public void remove(int position) {
        plants.remove(position);
    }

    public void add(Plant plant) {
        plants.add(plant);
    }

    public void updatePlant(int position, Plant plant) {
        plants.get(position).setPlant(plant);
    }

    public long getNextWateringInMS() {
        AtomicLong minTime = new AtomicLong(-1);
        AtomicLong tempTime = new AtomicLong();
        plants.forEach(plant -> {
            tempTime.set(calculateTimeInMsToWatering(plant));
            if (minTime.get() < 0) {
                minTime.set(tempTime.get());
            } else if (tempTime.get() > 0 && tempTime.get() < minTime.get()) {
                minTime.set(tempTime.get());
            }
        });
        return minTime.get();
    }

    private long calculateTimeInMsToWatering(Plant plant) {
        Date currentDate = new Date();

        Calendar c = Calendar.getInstance();
        c.setTime(plant.getLastWateringDate());
        c.add(Calendar.DAY_OF_MONTH, plant.getWateringInterval());
        long msDifference = getDifferenceMs(currentDate, c.getTime());
        if (msDifference > 0) return msDifference;
        else return 0;
    }

    private static long getDifferenceMs(Date d1, Date d2) {
        return d2.getTime() - d1.getTime();
    }
}