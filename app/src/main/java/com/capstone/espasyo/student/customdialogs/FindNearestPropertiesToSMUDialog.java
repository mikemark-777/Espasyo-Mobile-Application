package com.capstone.espasyo.student.customdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.capstone.espasyo.R;

public class FindNearestPropertiesToSMUDialog extends DialogFragment {

    private FindNearestPropertiesToSMUDialog.ConfirmFindPropertiesNearestToSMUListener listener;
    private LayoutInflater inflater;

    //for buttons of the filter dialog
    private Button btnCancelFindPropertiesNearSMU, btnFindPropertiesNearSMU;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.student_find_properties_nearest_to_smu_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        initializeDialogUI(view);

        AlertDialog createdFindNearestPropertiesToSMUDialog = builder.create();
        createdFindNearestPropertiesToSMUDialog.setView(view);

        btnFindPropertiesNearSMU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.getConfirmedToFindPropertiesNearestSMU();
                createdFindNearestPropertiesToSMUDialog.dismiss();
            }
        });

        btnCancelFindPropertiesNearSMU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.cancelFindPropertiesNearestSMU();
                createdFindNearestPropertiesToSMUDialog.dismiss();
            }
        });

        return createdFindNearestPropertiesToSMUDialog;
    }

    public void initializeDialogUI(View view) {
        //buttons
        btnFindPropertiesNearSMU = view.findViewById(R.id.btnFindPropertiesNearSMU);
        btnCancelFindPropertiesNearSMU = view.findViewById(R.id.btnCancelFindPropertiesNearSMU);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FindNearestPropertiesToSMUDialog.ConfirmFindPropertiesNearestToSMUListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+
                    "must implement ConfirmFindPropertiesNearestToSMUListener");
        }
    }

    public interface ConfirmFindPropertiesNearestToSMUListener {
        void getConfirmedToFindPropertiesNearestSMU();
        void cancelFindPropertiesNearestSMU();
    }
}
