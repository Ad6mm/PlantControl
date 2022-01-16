package com.example.plantcontrol.data;

public class User {

    public String name, email;
    public Plants plants;

    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public User(String name, String email, Plants plants) {
        this.name = name;
        this.email = email;
        this.plants = plants;
    }
}
