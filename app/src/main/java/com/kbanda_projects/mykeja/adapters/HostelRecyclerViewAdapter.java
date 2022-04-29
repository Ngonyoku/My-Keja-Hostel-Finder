package com.kbanda_projects.mykeja.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.kbanda_projects.mykeja.R;
import com.kbanda_projects.mykeja.models.Hostel;

import java.util.List;

public class HostelRecyclerViewAdapter extends RecyclerView.Adapter<HostelRecyclerViewAdapter.HostelViewHolder> {
    private Context context;
    private List<Hostel> hostels;
    private OnHostelClickedListener onHostelClickedListener;

    public interface OnHostelClickedListener {
        void onClick(int position);

        void onLongClick(int position);
    }

    public HostelRecyclerViewAdapter(Context context, List<Hostel> hostels) {
        this.context = context;
        this.hostels = hostels;
    }

    public void setOnHostelClickedListener(OnHostelClickedListener onHostelClickedListener) {
        this.onHostelClickedListener = onHostelClickedListener;
    }

    @NonNull
    @Override
    public HostelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HostelViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.item_hostel, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull HostelViewHolder holder, int position) {
        Hostel currentHostel = hostels.get(position);
        String rentPricePerMonth = "Ksh. " + currentHostel.getRentPricePerMonth() + " per month";
        boolean isVacant = currentHostel.isVacant();

        holder
                .hostelName
                .setText(currentHostel.getName())
        ;

        holder
                .hostelRentPrice
                .setText(rentPricePerMonth)
        ;

        holder
                .roomType
                .setText(currentHostel.getRoomType())
        ;
        holder
                .ratings
                .setText((!currentHostel.getRatings().isEmpty() || currentHostel.getRatings() != null)
                        ? currentHostel.getRatings() : "0")
        ;

        String totalRoomsAvailable = currentHostel.getTotalRoomsAvailable() + " rooms available";
        holder
                .numberOfRooms
                .setText(totalRoomsAvailable);

        String imageUrl = currentHostel.getImageUrls().get(0);
        if (imageUrl != null) {
            Glide
                    .with(context)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.color.teal_700)
                    .into(holder.hostelFirstImage)
            ;
        }
    }

    @Override
    public int getItemCount() {
        return hostels.size();
    }

    class HostelViewHolder extends RecyclerView.ViewHolder {
        private final TextView hostelName;
        private final TextView hostelRentPrice;
        private final TextView vacancyStatus;
        private final TextView ratings;
        private final ImageView hostelFirstImage;
        private final Chip roomType;
        private final Chip numberOfRooms;

        public HostelViewHolder(@NonNull View itemView) {
            super(itemView);

            hostelName = itemView.findViewById(R.id.hostelNameTV);
            hostelRentPrice = itemView.findViewById(R.id.hostelRentPriceTV);
            vacancyStatus = itemView.findViewById(R.id.hostelVacancyStatus);
            ratings = itemView.findViewById(R.id.ratingsTV);
            hostelFirstImage = itemView.findViewById(R.id.hostelFirstImage);
            roomType = itemView.findViewById(R.id.roomTypeChip);
            numberOfRooms = itemView.findViewById(R.id.numberOfRoomsChip);

            itemView
                    .setOnClickListener(view -> {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            onHostelClickedListener
                                    .onClick(pos);
                            onHostelClickedListener
                                    .onLongClick(pos);
                        }
                    })
            ;
        }
    }
}
