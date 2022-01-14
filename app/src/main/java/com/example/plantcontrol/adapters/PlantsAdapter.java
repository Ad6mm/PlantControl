package com.example.plantcontrol.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.plantcontrol.R;
import com.example.plantcontrol.data.Plant;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;

public class PlantsAdapter extends ArrayAdapter<Plant> {

    Bitmap bitmap;

    public PlantsAdapter(Context context, ArrayList<Plant> plants) {
        super(context, 0, plants);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Plant plant = (Plant) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant, parent, false);
        }
        TextView plantName = (TextView) convertView.findViewById(R.id.textPlantNameView);
        TextView daysToWater = (TextView) convertView.findViewById(R.id.daysToWateringTextView);
        ImageView image = (ImageView) convertView.findViewById(R.id.imagePlantView);
        ProgressBar progressBar = convertView.findViewById(R.id.progressBar2);

        plantName.setText(plant.getName());
        daysToWater.setText(plant.getWateringInterval().toString());
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
                            Glide.with(getContext())
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

        return convertView;
    }
}
