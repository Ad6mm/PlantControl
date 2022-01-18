package com.example.plantcontrol;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.plantcontrol.adapters.PlantsAdapter;
import com.example.plantcontrol.data.FirebaseDatabase;
import com.example.plantcontrol.data.Plant;
import com.example.plantcontrol.data.Plants;
import com.example.plantcontrol.data.User;
import com.example.plantcontrol.notifications.ReminderBroadcast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Plants plants;
    private PlantsAdapter adapter;

    ListView listView;
    Button addPlantButton;
    TextView userNameTextView;
    ProgressBar progressBar;
    ImageView emptyPlant;
    ImageView settings, weather;

    FirebaseDatabase firebaseDatabase;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = new FirebaseDatabase(getApplicationContext());
        sharedPref = getApplicationContext().getSharedPreferences(WelcomeActivity.SP_NAME, Context.MODE_PRIVATE);

        userNameTextView = findViewById(R.id.welcomeUserTextView);
        listView = findViewById(R.id.listView);
        addPlantButton = findViewById(R.id.addPlantButton);
        progressBar = findViewById(R.id.progressBarMain);
        progressBar.setVisibility(View.VISIBLE);
        emptyPlant = findViewById(R.id.emptyPlant);
        settings = findViewById(R.id.settings);
        weather = findViewById(R.id.weatherIcon);

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
        getWeather();
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


    private void getWeather() {
        String cityName = sharedPref.getString(SettingsActivity.CITY_NAME, null);

        if (cityName != null && cityName != "") {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url ="https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=fc1003b10bc286bef51c5b40c09e511d";

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Uri uri;
                                String imageURL;
                                JSONObject jsonObject = new JSONObject(response);
                                int weatherCode = jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id");
                                if (weatherCode >= 200 && weatherCode <=232) {
                                    imageURL = "https://openweathermap.org/img/wn/11d@2x.png";
                                } else if (weatherCode >= 300 && weatherCode <= 531) {
                                    imageURL = "https://openweathermap.org/img/wn/09d@2x.png";
                                } else if (weatherCode >= 600 && weatherCode <= 622) {
                                    imageURL = "https://openweathermap.org/img/wn/13d@2x.png";
                                } else if (weatherCode >= 700 && weatherCode <= 781) {
                                    imageURL = "https://openweathermap.org/img/wn/50d@2x.png";
                                } else if (weatherCode == 800) {
                                    imageURL = "https://openweathermap.org/img/wn/01d@2x.png";
                                } else {
                                    imageURL = "https://openweathermap.org/img/wn/03d@2x.png";
                                }

                                uri = Uri.parse(imageURL);
                                RequestOptions requestOptions = new RequestOptions();
                                requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(90));
                                Glide.with(MainActivity.this)
                                        .load(uri)
                                        .apply(requestOptions)
                                        .listener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                                return false;
                                            }
                                        })
                                        .into(weather);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast toast =  Toast.makeText(getApplicationContext(), "Something went wrong or city name is incorrect!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }
            });
// Add the request to the RequestQueue.
            queue.add(stringRequest);
        }
    }
}