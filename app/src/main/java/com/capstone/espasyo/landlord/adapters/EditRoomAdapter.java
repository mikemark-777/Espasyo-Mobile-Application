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
import com.capstone.espasyo.models.Room;

import java.util.ArrayList;

public class EditRoomAdapter extends RecyclerView.Adapter<EditRoomAdapter.EditRoomViewHolder> {

    private Context context;
    private ArrayList<Room> roomList;
    private OnRoomListener onRoomListener;

    final String AVAILABLE = "Available";
    final String UNAVAILABLE = "Unavailable";

    public EditRoomAdapter(Context context, ArrayList<Room> roomList, OnRoomListener onRoomListener) {
        this.context = context;
        this.roomList = roomList;
        this.onRoomListener = onRoomListener;
    }

    @NonNull
    @Override
    public EditRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.landlord_room_item, parent, false);
        return new EditRoomAdapter.EditRoomViewHolder(view, onRoomListener);
    }

    @Override
    public void onBindViewHolder(@NonNull EditRoomViewHolder holder, int position) {

        Room room = roomList.get(position);

        //check the availability of the room and display appropriate data
        if (room.getIsAvailable()) {
            holder.availability.setText(AVAILABLE);
            //set color of availability text when it is available
            holder.availability.setTextColor(context.getResources().getColor(R.color.espasyo_green_200));
        } else {
            holder.availability.setText(UNAVAILABLE);
            //set color of availability text when it is unavailable
            holder.availability.setTextColor(context.getResources().getColor(R.color.espasyo_red_500));
        }

        //check the availability of the facilities included (bathroom and kitchen) and display the appropriate data
        if (room.getHasBathRoom()) {
            holder.hasBathroom.setImageResource(R.drawable.icon_bathroom);
        } else {
            holder.hasBathroom.setImageResource(R.drawable.icon_no_bathroom);
        }

        if (room.getHasKitchen()) {
            holder.hasKitchen.setImageResource(R.drawable.icon_kitchen);
        } else {
            holder.hasKitchen.setImageResource(R.drawable.icon_no_kitchen);
        }

        holder.roomName.setText(room.getRoomName());
        holder.roomPrice.setText(String.valueOf(room.getPrice()));
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }


    public static class EditRoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView roomName, roomPrice, availability;
        ImageView hasBathroom, hasKitchen;
        OnRoomListener onRoomListener;

        public EditRoomViewHolder(@NonNull View itemView, OnRoomListener onRoomListener) {
            super(itemView);
            roomName = itemView.findViewById(R.id.roomName);
            roomPrice = itemView.findViewById(R.id.roomPrice);
            availability = itemView.findViewById(R.id.availability);
            hasBathroom = itemView.findViewById(R.id.icon_hasBathroom_roomItem);
            hasKitchen = itemView.findViewById(R.id.icon_hasKitchen_roomItem);
            this.onRoomListener = onRoomListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onRoomListener.onRoomClick(getAdapterPosition());
        }
    }

    public interface OnRoomListener {
        void onRoomClick(int position);
    }
}
