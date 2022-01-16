package com.example.plantcontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.example.plantcontrol.data.FirebaseDatabase;
import com.example.plantcontrol.data.Plants;
import com.example.plantcontrol.data.User;
import com.example.plantcontrol.notifications.ReminderBroadcast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Plants plants;
    private PlantsAdapter adapter;

    User user;

    ListView listView;
    Button addPlantButton;
    TextView userNameTextView;
    //DatabaseConn databaseConn;
    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = new FirebaseDatabase(getApplicationContext());
        firebaseDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User tmpUser = snapshot.getValue(User.class);

                if (tmpUser != null) {
                    user = tmpUser;
                    firebaseDatabase.setUser(tmpUser);
                    initializeView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Something wrong happened!", Toast.LENGTH_LONG).show();
            }
        });
        //databaseConn = new DatabaseConn(getApplicationContext());
        //databaseConn.clearDatabase();
        //String userName = databaseConn.getUserName();
    }

    private void initializeView() {
        userNameTextView = findViewById(R.id.welcomeUserTextView);
        listView = findViewById(R.id.listView);
        addPlantButton = findViewById(R.id.addPlantButton);

        String userName = user.name;//firebaseDatabase.getUserName();
        Log.d("DEBUG===========", "onCreate: " + userName);

        userNameTextView.setText(userNameTextView.getText().toString() + " " + userName + "!");

        addPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addPlantViewIntent = new Intent(MainActivity.this, AddPlantActivity.class);
                startActivity(addPlantViewIntent);
            }
        });

        cancelNotification();
        //if (databaseConn.getPlantsData() != null) {
        //if (firebaseDatabase.getPlantsData() != null) {
        if (user.plants != null) {
            //plants = databaseConn.getPlantsData();
            //plants = firebaseDatabase.getPlantsData();
            plants = user.plants;
            long nextWateringTimeInMS = plants.getNextWateringTimeInMS();
            if (nextWateringTimeInMS != 0) createNotification(nextWateringTimeInMS);
        } else {
            plants = new Plants();
            //databaseConn.savePlantsData(plants);
            firebaseDatabase.savePlantsData(plants);
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
                //databaseConn.savePlantsData(plants);
                firebaseDatabase.savePlantsData(plants);
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