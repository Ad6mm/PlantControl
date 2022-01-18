package com.example.plantcontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;

import com.example.plantcontrol.data.FirebaseDatabase;
import com.example.plantcontrol.data.Plants;
import com.example.plantcontrol.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    public static String CITY_NAME = "cityName";
    Button returnButton, logout, clearData, saveCity;
    TextView cityName;

    FirebaseDatabase firebaseDatabase;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        returnButton = findViewById(R.id.settingsReturn);
        logout = findViewById(R.id.logout);
        clearData = findViewById(R.id.clearAppData);
        cityName = findViewById(R.id.cityNameTextView);
        saveCity = findViewById(R.id.saveCity);

        sharedPref = getApplicationContext().getSharedPreferences(WelcomeActivity.SP_NAME, Context.MODE_PRIVATE);
        firebaseDatabase = new FirebaseDatabase(getApplicationContext());

        if (sharedPref.getString(CITY_NAME, null) != null) {
            cityName.setText(sharedPref.getString(CITY_NAME, null));
        }

        saveCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPref.edit().putString(CITY_NAME, cityName.getText().toString()).commit();
                startActivity(new Intent(SettingsActivity.this, WelcomeActivity.class));
            }
        });

        clearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Delete all data");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        firebaseDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User tmpUser = snapshot.getValue(User.class);

                                if (tmpUser != null) {
                                    firebaseDatabase.setUser(tmpUser);

                                    Plants clearPlants = new Plants();
                                    firebaseDatabase.setPlantsData(clearPlants);

                                    firebaseDatabase.getReference().setValue(firebaseDatabase.getUser())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Bundle b = ActivityOptions.makeSceneTransitionAnimation(SettingsActivity.this).toBundle();
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                            startActivity(new Intent(SettingsActivity.this, MainActivity.class), b);
                                                        } else {
                                                            startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                                                        }
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), "Something wrong happened!", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getApplicationContext(), "Something wrong happened!", Toast.LENGTH_LONG).show();
                            }
                        });
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Log out");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseDatabase.logout();
                        sharedPref.edit().clear().commit();
                        Bundle b = ActivityOptions.makeSceneTransitionAnimation(SettingsActivity.this).toBundle();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startActivity(new Intent(SettingsActivity.this, WelcomeActivity.class), b);
                        } else {
                            startActivity(new Intent(SettingsActivity.this, WelcomeActivity.class));
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }});
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.super.onBackPressed();
            }
        });
    }
}