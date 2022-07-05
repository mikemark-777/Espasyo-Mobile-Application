package com.capstone.espasyo.student.customdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.capstone.espasyo.R;
import com.google.android.material.textfield.TextInputLayout;

public class StudentFilterDialog extends DialogFragment {

    private ConfirmFilterDataListener listener;
    private LayoutInflater inflater;

    //for radio property type
    private RadioGroup propertyTypeRadioGroup;
    private RadioButton selectedPropertyTypeRadioButton;
    private TextInputLayout textInputMinimumPriceLayout, textInputMaximumPriceLayout, textInputExclusivityLayout;
    private AutoCompleteTextView textInputMinimumPrice, textInputMaximumPrice, textInputExclusivity;

    //for minimum and maximum price
    String[] minimumPrices = {"0","500", "1000", "1500", "2000", "2500", "3000", "3500", "4000", "4500", "5000", "5500", "6000", "6500", "7000", "7500", "8000"};
    String[] maximumPrices = {"0","500", "1000", "1500", "2000", "2500", "3000", "3500", "4000", "4500", "5000", "5500", "6000", "6500", "7000", "7500", "8000"};
    String[] exclusivities = {"Male Only", "Female Only", "Male and Female"};
    ArrayAdapter<String> minimumPriceAdapter;
    ArrayAdapter<String> maximumPriceAdapter;
    ArrayAdapter<String> exclusivityAdapter;

    //for number of persons per room
    private TextView textInputNumberOfPersons;
    private Button increment, decrement;
    private int numberOfPersons = 1;

    //for buttons of the filter dialog
    private Button btnCancelFilter, btnApplyFilters;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.student_filter_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        initializeDialogUI(view);

        // Increment and Decrement number of persons per room --------------------
        increment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOfPersons < 9) {
                    numberOfPersons++;
                    textInputNumberOfPersons.setText(String.valueOf(numberOfPersons));
                }
            }
        });

        decrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOfPersons > 1) {
                    numberOfPersons--;
                    textInputNumberOfPersons.setText(String.valueOf(numberOfPersons));
                }
            }
        });

        AlertDialog createdFilterDialog = builder.create();
        createdFilterDialog.setView(view);

        btnApplyFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String propertyType = getSelectedPropertyType(view);
                int minPrice = getMinimumValueFromString(textInputMinimumPrice.getText().toString().trim());
                int maxPrice = getMaximumValueFromString(textInputMaximumPrice.getText().toString().trim());
                int numberOfPersons = Integer.parseInt(textInputNumberOfPersons.getText().toString());
                String exclusivity = textInputExclusivity.getText().toString().trim();

                createdFilterDialog.dismiss();
                listener.getConfirmedFilterData(propertyType, minPrice, maxPrice, numberOfPersons, exclusivity);

            }
        });

        btnCancelFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.cancelFilter();
                createdFilterDialog.dismiss();
            }
        });
        return createdFilterDialog;
    }

    public void initializeDialogUI(View view) {
        //radio groups
        propertyTypeRadioGroup = view.findViewById(R.id.propertyTypeRadioGroup);

        //price drop down (textInputLayout and autoCompleteText)
        textInputMinimumPriceLayout = view.findViewById(R.id.text_input_minimumPrice_layout_filter);
        textInputMaximumPriceLayout = view.findViewById(R.id.text_input_maximumPrice_layout_filter);
        textInputExclusivityLayout = view.findViewById(R.id.text_input_exclusivity_layout);
        textInputMinimumPrice = view.findViewById(R.id.text_input_minimumPrice_filter);
        textInputMaximumPrice = view.findViewById(R.id.text_input_maximumPrice_filter);
        textInputExclusivity = view.findViewById(R.id.text_input_exclusivity);

        //minimum price
        minimumPriceAdapter = new ArrayAdapter<String>(getActivity(), R.layout.landlord_minimum_price_list_item, minimumPrices);
        textInputMinimumPrice.setAdapter(minimumPriceAdapter);
        minimumPriceAdapter.notifyDataSetChanged();
        //maximum price
        maximumPriceAdapter = new ArrayAdapter<String>(getActivity(), R.layout.landlord_maximum_price_list_item, maximumPrices);
        textInputMaximumPrice.setAdapter(maximumPriceAdapter);
        maximumPriceAdapter.notifyDataSetChanged();
        //exclusivity
        exclusivityAdapter = new ArrayAdapter<String>(getActivity(), R.layout.landlord_exclusivity_list_item, exclusivities);
        textInputExclusivity.setAdapter(exclusivityAdapter);
        exclusivityAdapter.notifyDataSetChanged();

        //number of persons per room
        textInputNumberOfPersons = view.findViewById(R.id.text_input_numberOfPersons_filter);
        increment = view.findViewById(R.id.increment_filter);
        decrement = view.findViewById(R.id.decrement_filter);

        //buttons
        btnApplyFilters = view.findViewById(R.id.btnApplyFilters);
        btnCancelFilter = view.findViewById(R.id.btnCancelFilters);
    }

    public String getSelectedPropertyType(View v) {
        int radioID = propertyTypeRadioGroup.getCheckedRadioButtonId();
        selectedPropertyTypeRadioButton = v.findViewById(radioID);

        return selectedPropertyTypeRadioButton.getText().toString();
    }

    // input validations

    public boolean isMinimumPriceEmpty(String minPrice) {
        if (minPrice.isEmpty()) {
            textInputMinimumPriceLayout.setError("");
            return true;
        } else {
            textInputMinimumPriceLayout.setError(null);
            return false;
        }
    }

    public boolean isMaximumPriceEmpty(String maxPrice) {
        if (maxPrice.isEmpty()) {
            textInputMaximumPriceLayout.setError("");
            return true;
        } else {
            textInputMaximumPriceLayout.setError(null);
            return false;
        }
    }

    public boolean isMinimumGreaterThanMaximum(String minPrice, String maxPrice) {
        int min = Integer.parseInt(minPrice);
        int max = Integer.parseInt(maxPrice);

        if (min > max) {
            textInputMinimumPriceLayout.setError("");
            return true;
        } else {
            textInputMinimumPriceLayout.setError(null);
            return false;
        }
    }

    public boolean arePriceValid(String minimumPrice, String maximumPrice) {
        boolean minPriceResult = isMinimumPriceEmpty(minimumPrice);
        boolean maxPriceResult = isMaximumPriceEmpty(maximumPrice);

        if (minPriceResult && maxPriceResult) {
            return true;
        } else {
            return false;
        }
    }

    public int getMinimumValueFromString(String stringMinValue) {
        if(stringMinValue.isEmpty()) {
            return 0;
        } else {
            return Integer.parseInt(stringMinValue);
        }
    }

    public int getMaximumValueFromString(String stringMaxValue) {
        if(stringMaxValue.isEmpty()) {
            return 0;
        } else {
            return Integer.parseInt(stringMaxValue);
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ConfirmFilterDataListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+
            "must implement ConfirmedLocationDialogListener");
        }
    }

    public interface ConfirmFilterDataListener {
        void getConfirmedFilterData(String propertyType, int minimumPrice, int maximumPrice, int numberOfPersons, String exclusivity);
        void cancelFilter();
    }
}
