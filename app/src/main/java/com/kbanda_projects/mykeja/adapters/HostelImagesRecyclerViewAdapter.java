package com.kbanda_projects.mykeja.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kbanda_projects.mykeja.R;

import java.util.List;

public class HostelImagesRecyclerViewAdapter extends RecyclerView.Adapter<HostelImagesRecyclerViewAdapter.HostelImageViewHolder> {
    private List<String> imagesList;
    private OnHostelImageClickedListener onHostelImageClickedListener;
    private Context context;

    public interface OnHostelImageClickedListener {
        void onClick(int imagePosition);
    }

    public HostelImagesRecyclerViewAdapter(List<String> imagesList, Context context) {
        this.imagesList = imagesList;
        this.context = context;
    }

    public void setOnHostelImageClickedListener(OnHostelImageClickedListener onHostelImageClickedListener) {
        this.onHostelImageClickedListener = onHostelImageClickedListener;
    }

    @NonNull
    @Override
    public HostelImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HostelImageViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.item_hostel_image, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull HostelImageViewHolder holder, int position) {
        String currentImageUrl = imagesList.get(position);
        if (currentImageUrl != null) {
            Glide
                    .with(context)
                    .load(currentImageUrl)
                    .centerCrop()
                    .into(holder.imageView)
            ;
        }
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    class HostelImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public HostelImageViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.hostelImage);
            itemView
                    .setOnClickListener(v -> {
                        int imagePosition = getAdapterPosition();
                        if (imagePosition != RecyclerView.NO_POSITION) {
                            onHostelImageClickedListener
                                    .onClick(imagePosition);
                        }
                    })
            ;
        }
    }
}
