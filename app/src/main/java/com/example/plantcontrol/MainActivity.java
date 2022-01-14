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
import com.example.plantcontrol.data.DatabaseConn;
import com.example.plantcontrol.data.Plant;
import com.example.plantcontrol.data.Plants;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPref;

    private Plants plants;
    private PlantsAdapter adapter;

    ListView listView;
    Button addPlantButton;
    TextView userNameTextView;
    DatabaseConn databaseConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userNameTextView = findViewById(R.id.welcomeUserTextView);
        listView = findViewById(R.id.listView);
        addPlantButton = findViewById(R.id.addPlantButton);

        databaseConn = new DatabaseConn(getApplicationContext());
        String userName = databaseConn.getUserName();

        if (userName == null) {
            Intent welcomeViewIntent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(welcomeViewIntent);
        } else {
            userNameTextView.setText(userNameTextView.getText().toString() + " " + userName + "!");
        }

        addPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addPlantViewIntent = new Intent(MainActivity.this, AddPlantActivity.class);
                startActivity(addPlantViewIntent);
            }
        });

        if (databaseConn.getPlantsData() != null) {
            plants = databaseConn.getPlantsData();
        } else {
            plants = new Plants();
            databaseConn.savePlantsData(plants);
        }

        adapter = new PlantsAdapter(this, plants.getPlants());
        listView.setAdapter(adapter);
        setUpListViewListener();
    }

    private void setUpListViewListener() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = getApplicationContext();
                Toast toast =  Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();

                plants.remove(position);
                adapter.notifyDataSetChanged();
                databaseConn.savePlantsData(plants);
                return true;
            }
        });
    }
}