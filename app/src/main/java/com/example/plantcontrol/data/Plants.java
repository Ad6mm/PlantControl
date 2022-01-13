package com.example.plantcontrol.data;

import java.util.ArrayList;

public class Plants {
    private ArrayList<Plant> plants;

    public Plants(ArrayList<Plant> plants) {
        this.plants = plants;
    }

    public Plants() {
        this.plants = new ArrayList<Plant>();
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
}