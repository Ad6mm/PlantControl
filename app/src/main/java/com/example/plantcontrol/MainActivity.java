package com.example.plantcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static String SP_NAME = "SharedPrefs";
    public static String USER_NAME = "userName";
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView userNameTextView = findViewById(R.id.welcomeTextView);

        sharedPref = getApplicationContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String userName = sharedPref.getString("userName", null);

        if (userName == null) {
            Intent welcomeViewIntent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(welcomeViewIntent);
        } else {
            userNameTextView.setText(userNameTextView.getText().toString() + " " + userName + "!");
        }




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
}