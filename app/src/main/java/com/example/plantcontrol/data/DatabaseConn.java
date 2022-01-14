package com.example.plantcontrol.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.plantcontrol.MainActivity;
import com.google.gson.Gson;

public class DatabaseConn {
    public static String SP_NAME = "SharedPrefs";
    public static String USER_NAME = "userName";
    public static String PLANTS_DATA = "plantsDATA";

    private SharedPreferences sharedPref;
    private Context context;

    private String getStringFromSharedPreferences(String key) {
        return sharedPref.getString(key, null);
    }

    public DatabaseConn(Context context) {
        this.context = context;
        this.sharedPref = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);;
    }

    public String getUserName() {
        return getStringFromSharedPreferences(USER_NAME);
    }

    public void setUserName(String userName) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(USER_NAME, userName);
        editor.commit();
    }

    public Plants getPlantsData() {
        Plants plants = null;
        String data = getStringFromSharedPreferences(PLANTS_DATA);
        if (data != null) {
            plants = new Gson().fromJson(sharedPref.getString(PLANTS_DATA, null), Plants.class);
        }
        return plants;
    }

    public void savePlantsData(Plants plants) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PLANTS_DATA, new Gson().toJson(plants));
        editor.commit();
    }

    public void addSinglePlant(Plant plant) {
        Plants plants = getPlantsData();
        plants.add(plant);
        savePlantsData(plants);
    }
}
