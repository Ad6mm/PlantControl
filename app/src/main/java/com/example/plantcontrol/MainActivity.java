package com.example.plantcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantcontrol.data.Plant;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static String SP_NAME = "SharedPrefs";
    public static String USER_NAME = "userName";
    SharedPreferences sharedPref;

    private ArrayList<Plant> items;
    private ArrayAdapter<Plant> itemsAdapter;

    ListView listView;
    Button addPlantButton;
    TextView userNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userNameTextView = findViewById(R.id.welcomeTextView);
        listView = findViewById(R.id.listView);
        addPlantButton = findViewById(R.id.addPlantButton);

        sharedPref = getApplicationContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String userName = sharedPref.getString("userName", null);

        if (userName == null) {
            Intent welcomeViewIntent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(welcomeViewIntent);
        } else {
            userNameTextView.setText(userNameTextView.getText().toString() + " " + userName + "!");
        }

        addPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem(v);
            }
        });

        items = new ArrayList<>();
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(itemsAdapter);
        setUpListViewListener();



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

    private void setUpListViewListener() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Context context = getApplicationContext();
                Toast toast =  Toast.makeText(context, "Item removed!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();

                items.remove(position);
                itemsAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    private void addItem(View v) {
        Plant plant = new Plant("Kwiatek", true, 3, new Date());
        itemsAdapter.add(plant);
    }
}