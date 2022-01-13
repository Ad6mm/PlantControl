package com.example.plantcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantcontrol.adapters.PlantsAdapter;
import com.example.plantcontrol.data.Plant;
import com.example.plantcontrol.data.Plants;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static String SP_NAME = "SharedPrefs";
    public static String USER_NAME = "userName";
    public static String PLANTS_DATA = "plantsDATA";
    SharedPreferences sharedPref;

    private Plants plants;
    private PlantsAdapter adapter;

    ListView listView;
    Button addPlantButton;
    TextView userNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userNameTextView = findViewById(R.id.welcomeUserTextView);
        listView = findViewById(R.id.listView);
        addPlantButton = findViewById(R.id.addPlantButton);

        sharedPref = getApplicationContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String userName = sharedPref.getString(USER_NAME, null);

        if (userName == null) {
            Intent welcomeViewIntent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(welcomeViewIntent);
        } else {
            userNameTextView.setText(userNameTextView.getText().toString() + " " + userName + "!");
        }

        addPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem(v);
            }
        });

        plants = new Plants();
        getPlantsData();
        adapter = new PlantsAdapter(this, plants.getPlants());
        listView.setAdapter(adapter);
        setUpListViewListener();



     /*   Button resetButton = null;
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPref = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.commit();

                Intent welcomeViewIntent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(welcomeViewIntent);
            }
        });*/
    }

    private void setUpListViewListener() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = getApplicationContext();
                Toast toast =  Toast.makeText(context, "Item removed!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();

                plants.remove(position);
                adapter.notifyDataSetChanged();
                savePlantsData();
                return true;
            }
        });
    }

    private void addItem(View v) {
        Plant plant = new Plant("Kwiatek", true, 3, new Date());
        adapter.add(plant);
        savePlantsData();
    }

    private void savePlantsData() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PLANTS_DATA, new Gson().toJson(plants));
        editor.commit();
    }

    private void getPlantsData() {
        String data = sharedPref.getString(PLANTS_DATA, null);
        if (data != null) {
            plants = new Gson().fromJson(sharedPref.getString(PLANTS_DATA, null), Plants.class);
        }
    }
}