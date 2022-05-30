package com.kbanda_projects.mykeja.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kbanda_projects.mykeja.R;
import com.kbanda_projects.mykeja.models.Hostel;

import java.util.List;

public class BookMarksRecyclerViewAdapter extends RecyclerView.Adapter<BookMarksRecyclerViewAdapter.BookMarksViewHolder> {
    private Context context;
    private List<Hostel> bookMarkList;

    public BookMarksRecyclerViewAdapter(Context context, List<Hostel> bookMarkList) {
        this.context = context;
        this.bookMarkList = bookMarkList;
    }

    @NonNull
    @Override
    public BookMarksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookMarksViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.item_hostel_bookmarks, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull BookMarksViewHolder holder, int position) {
        Hostel hostelBookmark = bookMarkList.get(position);
        String name = hostelBookmark.getName();
        String rentPrice = hostelBookmark.getRentPricePerMonth();

        List<String> imageUrls = hostelBookmark.getImageUrls();
        if (name != null && !name.trim().isEmpty()) {
            holder.hostelName.setText(name);
            if (rentPrice != null && !rentPrice.isEmpty()) {
                holder.hostelRentPrice.setText(rentPrice);
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    String firstImageUrl = imageUrls.get(0);
                    Glide
                            .with(context)
                            .load(firstImageUrl)
                            .centerCrop()
                            .fitCenter()
                            .placeholder(R.color.teal_700)
                            .into(holder.hostelFirstImage)
                    ;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return bookMarkList.size();
    }

    class BookMarksViewHolder extends RecyclerView.ViewHolder {
        private TextView hostelName;
        private TextView hostelRentPrice;
        private ImageView hostelFirstImage;

        public BookMarksViewHolder(@NonNull View itemView) {
            super(itemView);
            hostelName = itemView.findViewById(R.id.hostelNameTV);
            hostelRentPrice = itemView.findViewById(R.id.hostelRentPriceTV);
            hostelFirstImage = itemView.findViewById(R.id.hostelFirstImage);
        }
    }
}
