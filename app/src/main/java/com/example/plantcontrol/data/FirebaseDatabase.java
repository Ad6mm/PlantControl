package com.example.plantcontrol.data;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.plantcontrol.MainActivity;
import com.example.plantcontrol.WelcomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class FirebaseDatabase {
    private FirebaseUser databaseUser;
    private DatabaseReference reference;
    private String userID;

    private User user;
    private Context context;

    public FirebaseDatabase(Context context) {
        databaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = databaseUser.getUid();
        reference = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Users").child(userID);
        this.context = context;
    }

    public DatabaseReference getReference() { return reference; }
    public String getUserID() { return userID; }

    public String getUserName() {
        return user.name;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public String getEmail() {
        return user.email;
    }

    public Plants getPlantsData() { return user.plants; }

    public void setPlantsData(Plants plants) {  this.user.plants = plants; }

    public boolean logout() {
        FirebaseAuth.getInstance().signOut();
        return true;
    }
}
