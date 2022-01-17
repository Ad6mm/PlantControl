package com.example.plantcontrol;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantcontrol.data.DatabaseConn;
import com.example.plantcontrol.data.FirebaseDatabase;
import com.example.plantcontrol.data.Plant;
import com.example.plantcontrol.data.Plants;
import com.example.plantcontrol.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.SingleDateSelector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class AddPlantActivity extends AppCompatActivity {


    String storageKey;
    String currentDate;
    Plant newPlant = new Plant();
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage storage;
    StorageReference storageReference;

    Button addPlantButton;
    Button cancelButton;
    EditText plantName;
    ImageView plantPic;
    Uri imageUri = null;
    Switch isIndoor;
    EditText wateringInterval;
    TextView plantDate;
    TextView lastWateringDate;
    EditText description;
    DatePicker plantDatePicker;
    DatePicker wateringDatePicker;
    ImageView sun;
    ImageView home;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);

        firebaseDatabase = new FirebaseDatabase(getApplicationContext());
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        addPlantButton = findViewById(R.id.addNewPlantButton);
        plantPic = findViewById(R.id.imageView);
        plantName = findViewById(R.id.plantName);
        isIndoor = findViewById(R.id.isIndoorSwitch);
        wateringInterval = findViewById(R.id.wateringIntervalValue);
        plantDate = findViewById(R.id.plantDate);
        lastWateringDate = findViewById(R.id.lastWateringDate);
        description = findViewById(R.id.descriptionText);
        plantDatePicker = findViewById(R.id.datePickerPlant);
        wateringDatePicker = findViewById(R.id.datePickerWatering);
        cancelButton = findViewById(R.id.cancelNewPlantView);
        sun = findViewById(R.id.sun);
        home = findViewById(R.id.home);

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        currentDate = year + "-" + month+1 + "-" + day;

        plantDate.setText(currentDate);
        lastWateringDate.setText(currentDate);

        plantDatePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                plantDatePicker.setVisibility(View.INVISIBLE);
                plantDate.setText(year + "-" + monthOfYear+1 + "-" + dayOfMonth);
            }
        });

        plantDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plantDatePicker.setVisibility(View.VISIBLE);
            }
        });

        wateringDatePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                wateringDatePicker.setVisibility(View.INVISIBLE);
                lastWateringDate.setText(year + "-" + monthOfYear+1 + "-" + dayOfMonth);
            }
        });

        lastWateringDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wateringDatePicker.setVisibility(View.VISIBLE);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPlantActivity.super.onBackPressed();
            }
        });

        isIndoor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    home.setVisibility(View.VISIBLE);
                    sun.setVisibility(View.INVISIBLE);
                } else {
                    sun.setVisibility(View.VISIBLE);
                    home.setVisibility(View.INVISIBLE);
                }
            }
        });

        addPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (verifyNewPlant()) {
                        firebaseDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User tmpUser = snapshot.getValue(User.class);

                                if (tmpUser != null) {
                                    firebaseDatabase.setUser(tmpUser);

                                    Plants updatedPlants = firebaseDatabase.getPlantsData();
                                    updatedPlants.add(newPlant);
                                    firebaseDatabase.setPlantsData(updatedPlants);

                                    firebaseDatabase.getReference().setValue(firebaseDatabase.getUser())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        startActivity(new Intent(AddPlantActivity.this, MainActivity.class));
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
                    }
                } catch (MalformedURLException | ParseException e) {
                    e.printStackTrace();
                }

            }
        });

        plantPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });
    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            plantPic.setImageURI(imageUri);
            uploadPicture();
        }
    }

    private void uploadPicture() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading image...");
        progressDialog.show();

        final String randomKey = "images/" + UUID.randomUUID().toString();
        StorageReference imageRef = storageReference.child(randomKey);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        storageKey = randomKey;
                        Context context = getApplicationContext();
                        Toast toast =  Toast.makeText(context, "Image uploaded", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                        toast.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Context context = getApplicationContext();
                        Toast toast =  Toast.makeText(context, "Upload failed!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                        toast.show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progressPercent = (100.00 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Percentage: " + (int) progressPercent + "%");
                    }
                });
    }

    private boolean verifyNewPlant() throws MalformedURLException, ParseException {
        if (imageUri != null) {
            newPlant.setImageStorageKey(storageKey);
        }

        String plantDateString = plantDatePicker.getYear() + "-" + plantDatePicker.getMonth() + "-" + plantDatePicker.getDayOfMonth();
        String wateringDateString = wateringDatePicker.getYear() + "-" + wateringDatePicker.getMonth() + "-" + wateringDatePicker.getDayOfMonth();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
        Date plantDate = format.parse(plantDateString);
        Date wateringDate = format.parse(wateringDateString);
        Date currentDateObject = format.parse(currentDate);

        if (plantName.getText().toString() != "" && Integer.valueOf(wateringInterval.getText().toString()) > 0 && !plantDate.after(currentDateObject) && !wateringDate.after(currentDateObject)) {
            newPlant.setName(plantName.getText().toString());
            newPlant.setWateringInterval(Integer.valueOf(wateringInterval.getText().toString()));
            newPlant.setPlantDate(plantDate);
            newPlant.setDescription(description.getText().toString());
            newPlant.setLastWateringDate(wateringDate);
            newPlant.setInside(isIndoor.isChecked());
            return true;
        } else {
            Context context = getApplicationContext();
            Toast toast =  Toast.makeText(context, "Incorrect data", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, 270);
            toast.show();
            return false;
        }
    }
}