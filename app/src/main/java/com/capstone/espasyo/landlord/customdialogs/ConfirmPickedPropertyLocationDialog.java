package com.capstone.espasyo.landlord.customdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.capstone.espasyo.R;

public class ConfirmPickedPropertyLocationDialog extends DialogFragment {

    private ConfirmedLocationDialogListener listener;
    private LayoutInflater inflater;
    private EditText textInputLocation_street,
                     textInputLocation_barangay,
                     textInputLocation_municipality,
                     textInputLocation_landmark;

    private Button btnChangePropertyLocation,
                   btnConfirmPickedPropertyLocation;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.landlord_pick_property_location_confirmation_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        initializeDialogUI(view);

        Bundle args = getArguments();
        String street = args.getString("street");
        String barangay = args.getString("barangay");
        String municipality = args.getString("municipality");
        String landmark = args.getString("landmark");
        double latitude = args.getDouble("latitude");
        double longitude = args.getDouble("longitude");

        textInputLocation_street.setText(street);
        textInputLocation_barangay.setText(barangay);
        textInputLocation_municipality.setText(municipality);
        textInputLocation_landmark.setText(landmark);

        AlertDialog createdConfirmLocationDialog = builder.create();
        createdConfirmLocationDialog.setView(view);

        btnConfirmPickedPropertyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Must have Input validations
                //TODO: Must check if user has set the latitude and longitude, if not, inform

                //get the data from the textviews and the given latitude and longitude
                String street = textInputLocation_street.getText().toString();
                String barangay = textInputLocation_barangay.getText().toString();
                String municipality = textInputLocation_municipality.getText().toString();
                String landmark = textInputLocation_landmark.getText().toString();
                double finalLatitude = latitude;
                double finalLongitude = longitude;

                listener.getConfirmedLocationData(street, barangay, municipality, landmark, finalLatitude, finalLongitude);

            }
        });

        btnChangePropertyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return createdConfirmLocationDialog;
    }

    public void initializeDialogUI(View view) {
        //textviews to display location
        textInputLocation_street = view.findViewById(R.id.text_input_location_street);
        textInputLocation_barangay = view.findViewById(R.id.text_input_location_barangay);
        textInputLocation_municipality = view.findViewById(R.id.text_input_location_municipality);
        textInputLocation_landmark = view.findViewById(R.id.text_input_location_landmark);

        //buttons
        btnChangePropertyLocation = view.findViewById(R.id.btnChangeLocation);
        btnConfirmPickedPropertyLocation = view.findViewById(R.id.btnConfirmPickedPropertyLocation);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ConfirmedLocationDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+
            "must implement ConfirmedLocationDialogListener");
        }
    }

    public interface ConfirmedLocationDialogListener {
        void getConfirmedLocationData(String street, String barangay, String municipality, String landmark, double latitude, double longitude);
    }
}
