package com.example.plantcontrol.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.plantcontrol.R;
import com.example.plantcontrol.data.Plant;

import java.util.ArrayList;

public class PlantsAdapter extends ArrayAdapter<Plant> {

    public PlantsAdapter(Context context, ArrayList<Plant> plants) {
        super(context, 0, plants);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Plant plant = (Plant) getItem(position);
        //Uri imgUri=Uri.parse("data:text/html;charset=utf-8;base64,PCFET0...C9odG1sPg==");

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant, parent, false);
        }
        TextView plantName = (TextView) convertView.findViewById(R.id.textPlantNameView);
        TextView daysToWater = (TextView) convertView.findViewById(R.id.daysToWateringTextView);
        ImageView image = (ImageView) convertView.findViewById(R.id.imagePlantView);

        plantName.setText(plant.getName());
        daysToWater.setText(plant.getWateringInterval().toString());
        //image.setImageURI(imgUri);

        return convertView;
    }
}
