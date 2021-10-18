package com.capstone.espasyo.landlord.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.capstone.espasyo.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class EditLocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMapEdit;
    private SupportMapFragment mapFragment_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landlord_activity_edit_location_picker);
        mapFragment_edit = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment_edit);
        mapFragment_edit.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMapEdit = googleMap;

        gMapEdit.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng BayombongDefault = new LatLng(16.4845001, 121.1563895);
        gMapEdit.addMarker(new MarkerOptions().position(BayombongDefault).title("Bayombong")).showInfoWindow();
        gMapEdit.moveCamera(CameraUpdateFactory.newLatLngZoom(BayombongDefault, 16.0f));
    }
}