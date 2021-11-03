package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.capstone.espasyo.R;
import com.squareup.picasso.Picasso;

public class PreviewBusinessPermitImage extends AppCompatActivity {

    private ImageView previewBusinessPermitImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_preview_business_permit_image);

        Intent intent = getIntent();
        previewBusinessPermitImageView = findViewById(R.id.businessPermitPreview);
        displayBusinessPermit(intent);

    }

    public void displayBusinessPermit(Intent intent) {

        String businessPermitImageURL = intent.getStringExtra("businessPermit");

        Picasso.get()
                .load(businessPermitImageURL)
                .placeholder(R.drawable.img_upload_business_permit)
                .into(previewBusinessPermitImageView);

        previewBusinessPermitImageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        previewBusinessPermitImageView.setAdjustViewBounds(true);
    }

}