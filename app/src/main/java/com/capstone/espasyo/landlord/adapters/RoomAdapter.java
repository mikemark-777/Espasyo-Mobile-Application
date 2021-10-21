package com.capstone.espasyo.landlord.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.espasyo.R;
import com.capstone.espasyo.models.Room;

import java.util.ArrayList;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private Context context;
    private ArrayList<Room> roomList;
    //private PropertyAdapter.OnPropertyListener onPropertyListener;

    final String AVAILABLE = "Available";
    final String UNAVAILABLE = "Unavailable";

    public RoomAdapter(Context context, ArrayList<Room> roomList) {
        this.context = context;
        this.roomList = roomList;
    }

    @NonNull
    @Override
    public RoomAdapter.RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.landlord_room_item, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomAdapter.RoomViewHolder holder, int position) {
        Room room = roomList.get(position);

        if(room.getIsAvailable()) {
            holder.availability.setText(AVAILABLE);
            //set color of availability text when it is available
            holder.availability.setTextColor(context.getResources().getColor(R.color.espasyo_green_200));
        } else {
            holder.availability.setText(UNAVAILABLE);
            //set color of availability text when it is unavailable
            holder.availability.setTextColor(context.getResources().getColor(R.color.espasyo_red_2500));
        }
        holder.roomName.setText(room.getRoomName());
        holder.roomPrice.setText(String.valueOf(room.getPrice()));
        holder.numberOfPerson.setText(String.valueOf(room.getNumberOfPersons()));
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {

        TextView roomName, roomPrice, numberOfPerson, availability;
        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.roomName);
            roomPrice = itemView.findViewById(R.id.roomPrice);
            availability = itemView.findViewById(R.id.availability);
            numberOfPerson = itemView.findViewById(R.id.numberOfPersons);
        }
    }

}
