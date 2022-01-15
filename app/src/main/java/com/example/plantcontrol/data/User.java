package com.example.plantcontrol.data;

public class User {

    public String name, email;
    public Plants plants;

    public User() {
        this.plants = new Plants();
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.plants = new Plants();
    }
}
