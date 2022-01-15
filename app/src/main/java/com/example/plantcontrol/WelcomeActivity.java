package com.example.plantcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.plantcontrol.data.DatabaseConn;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome2);

        Button button = findViewById(R.id.loginButton);
        EditText emailInput = findViewById(R.id.emailInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button registerButton = findViewById(R.id.register);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailInput.getText().toString().equals("")) {
                    emailInput.setError("Email cannot by empty!");
                } else if (passwordInput.getText().toString().equals("")) {
                    passwordInput.setError("Password cannot by empty!");
                } else {
                    DatabaseConn databaseConn = new DatabaseConn(getApplicationContext());
                    databaseConn.setUserName(emailInput.getText().toString());
                    Intent mainViewIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(mainViewIntent);
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, RegisterUser.class));
            }
        });
    }
}