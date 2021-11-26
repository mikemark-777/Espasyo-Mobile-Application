package com.capstone.espasyo.student.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.capstone.espasyo.R;
import com.squareup.picasso.Picasso;

public class StudentPreviewImageActivity extends AppCompatActivity {

    private ImageView previewImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_activity_preview_image);

        Intent intent = getIntent();
        previewImageView = findViewById(R.id.previewImageView);
        displayBusinessPermit(intent);

    }

    public void displayBusinessPermit(Intent intent) {

        String previewImageURL = intent.getStringExtra("previewImage");

        Picasso.get()
                .load(previewImageURL)
                .placeholder(R.drawable.img_gallery)
                .into(previewImageView);

        previewImageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        previewImageView.setAdjustViewBounds(true);
    }

}