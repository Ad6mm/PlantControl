package com.example.plantcontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {

    public static String SP_NAME = "SharedPrefs";
    public static String USER_ID = "userId";
    private SharedPreferences sharedPref;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    private EditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome2);

        sharedPref = getApplicationContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        if (sharedPref.getString(USER_ID, null) != null) {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            return;
        }


        Button button = findViewById(R.id.loginButton);
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        Button registerButton = findViewById(R.id.register);

        progressBar = findViewById(R.id.progressBarLogin);
        mAuth = FirebaseAuth.getInstance();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
               /* DatabaseConn databaseConn = new DatabaseConn(getApplicationContext());
                databaseConn.setUserName(emailInput.getText().toString());
                Intent mainViewIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(mainViewIntent); */
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = ActivityOptions.makeSceneTransitionAnimation(WelcomeActivity.this).toBundle();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(WelcomeActivity.this, RegisterUser.class), b);
                } else {
                    startActivity(new Intent(WelcomeActivity.this, RegisterUser.class));
                }
            }
        });
    }

    private void userLogin() {
        String emailValue = email.getText().toString().trim();
        String passwordValue = password.getText().toString().trim();

        if (emailValue.isEmpty()) {
            email.setError("Email is required!");
            email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            email.setError("Please enter a valid email!");
            email.requestFocus();
            return;
        }

        if (passwordValue.isEmpty()) {
            password.setError("Password cannot by empty!");
            password.requestFocus();
            return;
        }

        if (passwordValue.length() < 6) {
            password.setError("Min password length is 6 characters!");
            password.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(emailValue, passwordValue)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(USER_ID, FirebaseAuth.getInstance().getCurrentUser().getUid());
                            editor.commit();

                            Bundle b = ActivityOptions.makeSceneTransitionAnimation(WelcomeActivity.this).toBundle();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                startActivity(new Intent(WelcomeActivity.this, MainActivity.class), b);
                            } else {
                                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(WelcomeActivity.this, "Failed to login, please check you credentials!", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                    }
                });
    }
}