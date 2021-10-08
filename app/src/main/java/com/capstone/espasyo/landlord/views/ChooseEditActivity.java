package com.capstone.espasyo.landlord.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.Property;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class ChooseEditActivity extends AppCompatActivity {

    private Property selectedProperty;

    private CardView editPropertyCardView;
    private CardView editRoomsCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_choose_edit);

        Intent intent = getIntent();

        selectedProperty = intent.getParcelableExtra("property");

        initializeCardViews();

        editPropertyCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(selectedProperty != null) {
                    Intent intent = new Intent(ChooseEditActivity.this, EditPropertyActivity.class);
                    intent.putExtra("property", selectedProperty);
                    startActivity(intent);
               }
            }
        });

        editRoomsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChooseEditActivity.this, "editRoomsCardView clicked!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initializeCardViews() {
        editPropertyCardView = findViewById(R.id.editPropertyCardView);
        editRoomsCardView = findViewById(R.id.editRoomsCardView);
    }

}