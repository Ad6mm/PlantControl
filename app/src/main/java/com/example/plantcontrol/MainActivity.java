package com.example.plantcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import com.example.plantcontrol.notifications.ReminderBroadcast;
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

        cancelNotification();
        if (databaseConn.getPlantsData() != null) {
            plants = databaseConn.getPlantsData();
            long nextWateringTimeInMS = plants.getNextWateringTimeInMS();
            if (nextWateringTimeInMS != 0) createNotification(nextWateringTimeInMS);
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
                Toast toast = Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();

                plants.remove(position);
                adapter.notifyDataSetChanged();
                databaseConn.savePlantsData(plants);
                return true;
            }
        });
    }

    private void createNotification(long timeInMs) {
        //retrofit HTTP
        //open wheater
        createNotificationChannel();
        Intent intent = new Intent(MainActivity.this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeInMs, pendingIntent);

    }

    private void cancelNotification() {
        createNotificationChannel();
        Intent intent = new Intent(MainActivity.this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

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