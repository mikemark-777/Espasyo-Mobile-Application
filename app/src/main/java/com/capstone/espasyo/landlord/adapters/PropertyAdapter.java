package com.capstone.espasyo.landlord.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.Property;

import java.util.ArrayList;
import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    private Context context;
    private ArrayList<Property> ownedPropertyList;
    private OnPropertyListener onPropertyListener;

    public PropertyAdapter(Context context, ArrayList<Property> propertyList, OnPropertyListener onPropertyListener) {
        this.context = context;
        this.ownedPropertyList = propertyList;
        this.onPropertyListener = onPropertyListener;
    }

    @NonNull
    @Override
    public PropertyAdapter.PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.landlord_property_item, parent, false);
        return new PropertyViewHolder(view, onPropertyListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyAdapter.PropertyViewHolder holder, int position) {
        Property property = ownedPropertyList.get(position);
        holder.propertyName.setText(property.getName());
        holder.propertyAddress.setText(property.getAddress());
        holder.propertyType.setText(property.getPropertyType());
        holder.landlordName.setText(property.getLandlordName());
        holder.landlordContactNumber.setText(property.getLandlordPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return ownedPropertyList.size();
    }

    public static class PropertyViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {

        TextView propertyName, propertyAddress, propertyType, landlordName, landlordContactNumber;
        OnPropertyListener onPropertyListener;

        public PropertyViewHolder(@NonNull View itemView, OnPropertyListener onPropertyListener) {
            super(itemView);
            propertyName = itemView.findViewById(R.id.propertyName);
            propertyAddress = itemView.findViewById(R.id.propertyAddress);
            propertyType = itemView.findViewById(R.id.propertyType);
            landlordName = itemView.findViewById(R.id.landlordName);
            landlordContactNumber = itemView.findViewById(R.id.landlordContactNumber);
            this.onPropertyListener = onPropertyListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onPropertyListener.onPropertyClick(getAdapterPosition());
        }
    }

    public interface OnPropertyListener {
        void onPropertyClick(int position);
    }

}