package com.example.plantcontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantcontrol.adapters.PlantsAdapter;
import com.example.plantcontrol.data.FirebaseDatabase;
import com.example.plantcontrol.data.Plant;
import com.example.plantcontrol.data.Plants;
import com.example.plantcontrol.data.User;
import com.example.plantcontrol.notifications.ReminderBroadcast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Plants plants;
    private PlantsAdapter adapter;

    ListView listView;
    Button addPlantButton;
    TextView userNameTextView;
    ProgressBar progressBar;
    ImageView emptyPlant;
    ImageView settings;

    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = new FirebaseDatabase(getApplicationContext());

        userNameTextView = findViewById(R.id.welcomeUserTextView);
        listView = findViewById(R.id.listView);
        addPlantButton = findViewById(R.id.addPlantButton);
        progressBar = findViewById(R.id.progressBarMain);
        progressBar.setVisibility(View.VISIBLE);
        emptyPlant = findViewById(R.id.emptyPlant);
        settings = findViewById(R.id.settings);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class), b);
                } else {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                }
            }
        });

        addPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(MainActivity.this, AddPlantActivity.class), b);
                } else {
                    startActivity(new Intent(MainActivity.this, AddPlantActivity.class));
                }
            }
        });

        createNotificationChannel();
        cancelNotification();
        firebaseDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User tmpUser = snapshot.getValue(User.class);

                if (tmpUser != null) {
                    firebaseDatabase.setUser(tmpUser);
                    initializeView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Something wrong happened!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initializeView() {
        String userName = firebaseDatabase.getUserName();
        userNameTextView.setText(userNameTextView.getText().toString() + " " + userName + "!");

        if (!firebaseDatabase.getPlantsData().getPlants().isEmpty()) {
            emptyPlant.setVisibility(View.GONE);
            plants = firebaseDatabase.getPlantsData();
            long nextWateringInMS = plants.getNextWateringInMS();
            if (nextWateringInMS != 0) createNotification(nextWateringInMS);

            progressBar.setVisibility(View.GONE);
            adapter = new PlantsAdapter(this, plants.getPlants());
            listView.setAdapter(adapter);
            setUpListViewListener();
        } else {
            emptyPlant.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            plants = new Plants();
            savePlantsToDatabase(plants);
        }
    }

    private void savePlantsToDatabase(Plants plants) {
        User tmpUser = firebaseDatabase.getUser();
        tmpUser.plants = plants;

        firebaseDatabase.getReference().setValue(tmpUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Something wrong happened!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void setUpListViewListener() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                String currentDate = year + "-" + String.valueOf(month+1) + "-" + day;
                try {
                    Date currentDateObject = format.parse(currentDate);
                    Plant tmpPlant = plants.getPlants().get(position);
                    tmpPlant.setLastWateringDate(currentDateObject);
                    plants.updatePlant(position, tmpPlant);


                    firebaseDatabase.setPlantsData(plants);
                    firebaseDatabase.getReference().setValue(firebaseDatabase.getUser())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast toast =  Toast.makeText(getApplicationContext(), "Plant has been watered", Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                                        toast.show();
                                        adapter.notifyDataSetChanged();
                                        Bundle b = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle();
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            startActivity(new Intent(MainActivity.this, MainActivity.class), b);
                                        } else {
                                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Something wrong happened!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                    return true;
                } catch (ParseException e) {
                    e.printStackTrace();
                    return false;
                }

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle b = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(MainActivity.this, PlantDetailsActivity.class).putExtra("Plant", position), b);
                } else {
                    startActivity(new Intent(MainActivity.this, PlantDetailsActivity.class));
                }
            }
        });
    }

    private void createNotification(long timeInMs) {
        Intent intent = new Intent(MainActivity.this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeInMs, pendingIntent);

    }

    private void cancelNotification() {
        Intent intent = new Intent(MainActivity.this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void createNotificationChannel() {
        String channelName = "notificationChannel";
        String description = "Channel for watering notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("notifyWatering", channelName, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}