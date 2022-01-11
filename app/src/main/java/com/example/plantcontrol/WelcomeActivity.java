package com.example.plantcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome2);

        Button button = findViewById(R.id.button);
        EditText nameInput = findViewById(R.id.nameInput);
        TextView emptyName = findViewById(R.id.emptyName);
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(MainActivity.SP_NAME, Context.MODE_PRIVATE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameInput.getText().toString().equals("")) {
                    emptyName.setVisibility(View.VISIBLE);
                } else {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(MainActivity.USER_NAME, nameInput.getText().toString());
                    editor.commit();
                    Intent mainViewIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(mainViewIntent);
                }
            }
        });
    }
}