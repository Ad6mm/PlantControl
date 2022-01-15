package com.example.plantcontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.plantcontrol.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private Button cancel, register;
    private EditText name, email, password;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        cancel = findViewById(R.id.cancelRegister);
        cancel.setOnClickListener(this);

        register = findViewById(R.id.registerApply);
        register.setOnClickListener(this);

        name = findViewById(R.id.editTextTextPersonName);

        email = findViewById(R.id.editTextTextEmailAddress);

        password = findViewById(R.id.editTextTextPassword);

        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancelRegister:
                startActivity(new Intent(this, WelcomeActivity.class));
                break;
            case R.id.registerApply:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String emailValue = email.getText().toString().trim();
        String nameValue = name.getText().toString().trim();
        String passwordValue = password.getText().toString().trim();

        if (emailValue.isEmpty())  {
            email.setError("Email is required!");
            email.requestFocus();
            return;
        }

        if (nameValue.isEmpty()) {
            name.setError("Name is required!");
            name.requestFocus();
            return;
        }

        if (passwordValue.isEmpty()) {
            password.setError("Password is required!");
            password.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            email.setError("Please provide valid email!");
            email.requestFocus();
            return;
        }

        if (passwordValue.length() < 6) {
            password.setError("Min password length should be 6 characters!!");
            password.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(emailValue, passwordValue)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(nameValue, emailValue);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast toast = Toast.makeText(RegisterUser.this, "User has been register successfully!", Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                                        toast.show();
                                        progressBar.setVisibility(View.GONE);

                                    } else {
                                        Toast toast = Toast.makeText(RegisterUser.this, "Failed to register!", Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                                        toast.show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        } else {
                            Toast toast = Toast.makeText(RegisterUser.this, "Failed to register!", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}