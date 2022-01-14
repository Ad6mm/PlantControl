package com.example.plantcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;

import com.example.plantcontrol.data.DatabaseConn;
import com.example.plantcontrol.data.Plant;
import com.example.plantcontrol.data.Plants;

import java.util.Date;

public class AddPlantActivity extends AppCompatActivity {

    Button addPlantButton;
    Plants plants;
    DatabaseConn databaseConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);

        databaseConn = new DatabaseConn(getApplicationContext());
        addPlantButton = findViewById(R.id.addNewPlantButton);

        addPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Plant plant = new Plant("KwiatekTest", true, 6, new Date());
                databaseConn.addSinglePlant(plant);
                Intent mainViewIntent = new Intent(AddPlantActivity.this, MainActivity.class);
                startActivity(mainViewIntent);
            }
        });
    }
}