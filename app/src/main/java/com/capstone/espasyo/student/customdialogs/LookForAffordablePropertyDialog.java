package com.capstone.espasyo.student.customdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.capstone.espasyo.R;

public class LookForAffordablePropertyDialog extends DialogFragment {

    private LookForAffordablePropertyDialog.ConfirmFilterAffordablePropertiesListener listener;
    private LayoutInflater inflater;

    //for buttons of the filter dialog
    private Button btnCancelFilterAffordableProperties, btnApplyFilterAffordableProperties;

    //this will be the affordable filter price. In case of changes, this will be the variable whose value will be changed
    private final int AffordablePropertiesFilterPrice = 1500;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.student_filter_affordable_properties_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        initializeDialogUI(view);

        AlertDialog createdFilterAffordablePropertiesDialog = builder.create();
        createdFilterAffordablePropertiesDialog.setView(view);

        btnApplyFilterAffordableProperties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createdFilterAffordablePropertiesDialog.dismiss();
                listener.getConfirmedAffordableFilterPrice(AffordablePropertiesFilterPrice);
            }
        });

        btnCancelFilterAffordableProperties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.cancelAffordablePropertiesFilter();
                createdFilterAffordablePropertiesDialog.dismiss();
            }
        });

        return createdFilterAffordablePropertiesDialog;
    }

    public void initializeDialogUI(View view) {
        //buttons
        btnApplyFilterAffordableProperties = view.findViewById(R.id.btnApplyFilterAffordableProperties);
        btnCancelFilterAffordableProperties = view.findViewById(R.id.btnCancelFilterAffordableProperties);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (LookForAffordablePropertyDialog.ConfirmFilterAffordablePropertiesListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+
                    "must implement ConfirmFilterAffordablePropertiesListener");
        }
    }

    public interface ConfirmFilterAffordablePropertiesListener {
        void getConfirmedAffordableFilterPrice(int affordableFilterPrice);
        void cancelAffordablePropertiesFilter();
    }

}
