package com.example.plantcontrol;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.plantcontrol.data.FirebaseDatabase;
import com.example.plantcontrol.data.Plant;
import com.example.plantcontrol.data.Plants;
import com.example.plantcontrol.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class PlantDetailsActivity extends AppCompatActivity {

    FirebaseDatabase firebaseDatabase;
    FirebaseStorage storage;
    StorageReference storageReference;
    String storageKey;

    Button returnButton, edit;
    ImageView delete, image;
    TextView plantName, wateringInterval, description, plantDate, lastWateringDate;
    Switch isIndoor;
    ImageView sun, home;
    ProgressBar progressBar;
    DatePicker plantDatePicker, wateringDatePicker;

    Plant plant;
    Boolean isEditing;
    String greenColor = "#006400";
    String blueColor = "#2196F3";
    String yellowColor = "#FFC107";
    Uri imageUri = null;
    int plantPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_details);
        isEditing = false;
        plantPosition = (int) getIntent().getSerializableExtra("Plant");

        firebaseDatabase = new FirebaseDatabase(getApplicationContext());
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

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
                Toast.makeText(getApplicationContext(), "Something wrong happened!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initializeView() {
        returnButton = findViewById(R.id.returnButtonDetails);
        edit = findViewById(R.id.editPlantViewDetails);
        delete = findViewById(R.id.deletePlantImageView);
        plantName = findViewById(R.id.plantNameDetails);
        wateringInterval = findViewById(R.id.wateringIntervalValueDetails);
        description = findViewById(R.id.descriptionTextDetails);
        plantDate = findViewById(R.id.plantDateDetails);
        lastWateringDate = findViewById(R.id.lastWateringDateDetails);
        isIndoor = findViewById(R.id.isIndoorSwitchDetails);
        sun = findViewById(R.id.sunDetails);
        home = findViewById(R.id.homeDetails);
        image = findViewById(R.id.imageViewDetails);
        progressBar = findViewById(R.id.progressBarDetails);
        plantDatePicker = findViewById(R.id.datePickerPlantDetails);
        wateringDatePicker = findViewById(R.id.datePickerWateringDetails);

        disableEdit();
        loadPlantData();
        addOnClickListeners();
        manageDatePickers();

        if (plant.getImageStorageKey() != null) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(plant.getImageStorageKey());
            image.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            imageRef.getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            RequestOptions requestOptions = new RequestOptions();
                            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(90));
                            Glide.with(PlantDetailsActivity.this)
                                    .load(uri)
                                    .apply(requestOptions)
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            progressBar.setVisibility(View.GONE);
                                            image.setVisibility(View.VISIBLE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            progressBar.setVisibility(View.GONE);
                                            image.setVisibility(View.VISIBLE);
                                            return false;
                                        }
                                    })
                                    .into(image);
                        }
                    });
        }
    }

    private void manageDatePickers() {
        Calendar wateringCalendar = Calendar.getInstance();
        wateringCalendar.setTime(plant.getLastWateringDate());

        Calendar plantDateCalendar = Calendar.getInstance();
        plantDateCalendar.setTime(plant.getPlantDate());

        plantDatePicker.init(plantDateCalendar.get(Calendar.YEAR), plantDateCalendar.get(Calendar.MONTH), plantDateCalendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                plantDatePicker.setVisibility(View.INVISIBLE);
                monthOfYear++;
                plantDate.setText(year + "-" + monthOfYear + "-" + dayOfMonth);
            }
        });

        plantDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plantDatePicker.setVisibility(View.VISIBLE);
            }
        });

        wateringDatePicker.init(wateringCalendar.get(Calendar.YEAR), wateringCalendar.get(Calendar.MONTH), wateringCalendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                wateringDatePicker.setVisibility(View.INVISIBLE);
                monthOfYear++;
                lastWateringDate.setText(year + "-" + monthOfYear + "-" + dayOfMonth);
            }
        });

        lastWateringDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wateringDatePicker.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadPlantData() {
        plant = firebaseDatabase.getPlantsData().getPlants().get(plantPosition);
        plantName.setText(plant.getName());
        wateringInterval.setText(String.valueOf(plant.getWateringInterval()));
        description.setText(plant.getDescription());
        isIndoor.setChecked(plant.getInside());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String plantDateString = dateFormat.format(plant.getPlantDate());
        plantDate.setText(plantDateString);

        String wateringDateString = dateFormat.format(plant.getLastWateringDate());
        lastWateringDate.setText(wateringDateString);

        if (!plant.getInside()) {
            home.setAlpha(0);
        } else {
            sun.setAlpha(0);
        }
    }

    private void addOnClickListeners() {
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PlantDetailsActivity.this);
                builder.setTitle("Remove plant");
                builder.setMessage("Are you sure?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Plants tmpPlants = firebaseDatabase.getPlantsData();
                        tmpPlants.remove(plantPosition);
                        firebaseDatabase.setPlantsData(tmpPlants);

                        firebaseDatabase.getReference().setValue(firebaseDatabase.getUser())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Something wrong happened!", Toast.LENGTH_LONG).show();
                                        } else {
                                            startActivity(new Intent(PlantDetailsActivity.this, MainActivity.class));
                                        }
                                    }
                                });
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlantDetailsActivity.super.onBackPressed();
            }
        });

        isIndoor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    home.setAlpha(255);
                    sun.setAlpha(0);
                } else {
                    home.setAlpha(0);
                    sun.setAlpha(255);
                }
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEditing) {
                    edit.setText("Save");
                    isEditing = true;
                    enableEdit();
                } else {
                    saveChanges();
                }
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
                        plant.setImageStorageKey(storageKey);

                        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(plant.getImageStorageKey());
                        image.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        RequestOptions requestOptions = new RequestOptions();
                                        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(90));
                                        Glide.with(PlantDetailsActivity.this)
                                                .load(uri)
                                                .apply(requestOptions)
                                                .listener(new RequestListener<Drawable>() {
                                                    @Override
                                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                        progressBar.setVisibility(View.GONE);
                                                        image.setVisibility(View.VISIBLE);
                                                        return false;
                                                    }

                                                    @Override
                                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                        progressBar.setVisibility(View.GONE);
                                                        image.setVisibility(View.VISIBLE);
                                                        return false;
                                                    }
                                                })
                                                .into(image);
                                    }
                                });
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

    private void saveChanges() {
        try {
            if (verifyNewPlant()) {
                firebaseDatabase.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User tmpUser = snapshot.getValue(User.class);

                        if (tmpUser != null) {
                            firebaseDatabase.setUser(tmpUser);

                            Plants updatedPlants = firebaseDatabase.getPlantsData();
                            updatedPlants.remove(plantPosition);
                            updatedPlants.add(plant);
                            firebaseDatabase.setPlantsData(updatedPlants);

                            firebaseDatabase.getReference().setValue(firebaseDatabase.getUser())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                startActivity(new Intent(PlantDetailsActivity.this, MainActivity.class));
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

    private void disableTextView(TextView textView) {
        textView.setFocusable(false);
        textView.setEnabled(false);
        textView.setTextColor(Color.BLACK);
    }

    private void enableTextView(TextView textView) {
        textView.setEnabled(true);
        textView.setFocusable(true);
        textView.setFocusableInTouchMode(true);
        textView.setTextColor(Color.BLACK);
    }

    private void disableEdit() {
        disableTextView(plantName);
        disableTextView(wateringInterval);
        wateringInterval.setTextColor(Color.parseColor(blueColor));
        disableTextView(description);
        disableTextView(plantDate);
        disableTextView(lastWateringDate);
        isIndoor.setEnabled(false);
        isIndoor.setTextColor(Color.BLACK);
        isIndoor.getThumbDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        isIndoor.getTrackDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }

    private void enableEdit() {
        delete.setVisibility(View.GONE);
        enableTextView(plantName);
        enableTextView(wateringInterval);
        wateringInterval.setTextColor(Color.parseColor(blueColor));
        enableTextView(description);
        enableTextView(plantDate);
        plantDate.setFocusableInTouchMode(false);
        enableTextView(lastWateringDate);
        lastWateringDate.setFocusableInTouchMode(false);
        isIndoor.setEnabled(true);
        isIndoor.setTextColor(Color.BLACK);
        isIndoor.getThumbDrawable().setColorFilter(Color.parseColor(greenColor), PorterDuff.Mode.MULTIPLY);
        isIndoor.getTrackDrawable().setColorFilter(Color.parseColor(greenColor), PorterDuff.Mode.MULTIPLY);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });
    }

    private boolean verifyNewPlant() throws MalformedURLException, ParseException {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String currentDate = year + "-" + String.valueOf(month+1) + "-" + day;

        String plantDateString = plantDatePicker.getYear() + "-" + plantDatePicker.getMonth() + "-" + plantDatePicker.getDayOfMonth();
        String wateringDateString = wateringDatePicker.getYear() + "-" + wateringDatePicker.getMonth() + "-" + wateringDatePicker.getDayOfMonth();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
        Date plantDate = format.parse(plantDateString);
        Date wateringDate = format.parse(wateringDateString);
        Date currentDateObject = format.parse(currentDate);

        if (plantName.getText().toString() != "" && Integer.valueOf(wateringInterval.getText().toString()) > 0 && !plantDate.after(currentDateObject) && !wateringDate.after(currentDateObject)) {
            plant.setName(plantName.getText().toString());
            plant.setWateringInterval(Integer.valueOf(wateringInterval.getText().toString()));
            plant.setPlantDate(plantDate);
            plant.setDescription(description.getText().toString());
            plant.setLastWateringDate(wateringDate);
            plant.setInside(isIndoor.isChecked());
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