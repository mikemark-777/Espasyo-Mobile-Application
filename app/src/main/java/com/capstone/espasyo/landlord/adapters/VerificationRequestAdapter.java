package com.capstone.espasyo.landlord.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.VerificationRequest;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class VerificationRequestAdapter extends RecyclerView.Adapter<VerificationRequestAdapter.VerificationRequestViewHolder> {

    private Context context;
    private ArrayList<VerificationRequest> ownedPropertyVerifications;
    private OnVerificationRequestListener onVerificationRequestListener;

    public VerificationRequestAdapter(Context context, ArrayList<VerificationRequest> ownedPropertyVerifications, OnVerificationRequestListener onVerificationRequestListener) {
        this.context = context;
        this.ownedPropertyVerifications = ownedPropertyVerifications;
        this.onVerificationRequestListener = onVerificationRequestListener;
    }

    @NonNull
    @Override
    public VerificationRequestAdapter.VerificationRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate((R.layout.landlord_verification_request_item), parent, false);
        return new VerificationRequestViewHolder(view, onVerificationRequestListener);
    }

    @Override
    public void onBindViewHolder(@NonNull VerificationRequestViewHolder holder, int position) {
        VerificationRequest verificationRequest = ownedPropertyVerifications.get(position);
        String propertyName = verificationRequest.getPropertyName();
        String dateSubmitted = verificationRequest.getDateSubmitted();
        String dateVerified = verificationRequest.getDateVerified();
        String status = verificationRequest.getStatus();
        String classification = verificationRequest.getClassification();
        
        if (status.equals("verified")) {
            holder.verifiedIconDisplay.setImageResource(R.drawable.icon_verified);
        } else if (status.equals("unverified")) {
            holder.verifiedIconDisplay.setImageResource(R.drawable.icon_unverified);
        } else if (status.equals("declined")) {
            holder.verifiedIconDisplay.setImageResource(R.drawable.icon_declined);
        } else if (status.equals("expired")) {
            holder.verifiedIconDisplay.setImageResource(R.drawable.icon_expired);
        }

        holder.dateVerified.setText(dateVerified);
        holder.propertyName.setText(propertyName);
        holder.dateSubmitted.setText(dateSubmitted);

        if (classification.equals("new")) {
            holder.classification.setText("New");
        } else if (classification.equals("renew")) {
            holder.classification.setText("Renew");
        }
    }


    @Override
    public int getItemCount() {
        return ownedPropertyVerifications.size();
    }

    public static class VerificationRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView propertyName, dateSubmitted, dateVerified, classification;
        ImageView verifiedIconDisplay;
        OnVerificationRequestListener onVerificationRequestListener;

        public VerificationRequestViewHolder(@NonNull View itemView, OnVerificationRequestListener onVerificationRequestListener) {
            super(itemView);
            propertyName = itemView.findViewById(R.id.propertyName_verification);
            dateSubmitted = itemView.findViewById(R.id.dateSubmitted_verification);
            dateVerified = itemView.findViewById(R.id.dateVerified_verification);
            verifiedIconDisplay = itemView.findViewById(R.id.verifiedIconDisplay);
            classification = itemView.findViewById(R.id.classification_verification);
            this.onVerificationRequestListener = onVerificationRequestListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onVerificationRequestListener.onVerificationRequestClick(getAdapterPosition());
        }
    }

    public interface OnVerificationRequestListener {
        void onVerificationRequestClick(int position);
    }

}
