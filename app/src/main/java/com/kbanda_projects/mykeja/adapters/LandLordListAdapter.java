package com.kbanda_projects.mykeja.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kbanda_projects.mykeja.R;
import com.kbanda_projects.mykeja.models.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LandLordListAdapter extends RecyclerView.Adapter<LandLordListAdapter.LandLordListViewHolder> {
    private Context context;
    private List<User> userList;
    private OnLandLordClickedListener onLandLordClickedListener;

    public LandLordListAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    public void setOnLandLordClickedListener(OnLandLordClickedListener onLandLordClickedListener) {
        this.onLandLordClickedListener = onLandLordClickedListener;
    }

    @NonNull
    @Override
    public LandLordListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LandLordListViewHolder(
                LayoutInflater
                        .from(context)
                        .inflate(R.layout.item_landlords, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull LandLordListViewHolder holder, int position) {
        User user = userList.get(position);
        user.getUserId();
        String profileImage = user.getProfileImageUrl();
        String email = user.getEmail();
        user.getRole();

        String name = user.getFirstName() + " " + user.getLastName();

        if (email != null) {
            holder.email.setText(email);
        }
        holder.name.setText(name);

        Glide
                .with(context)
                .load(profileImage)
                .into(holder.imageView)
        ;

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class LandLordListViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView email;
        CircleImageView imageView;

        public LandLordListViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.landlordNameTV);
            email = itemView.findViewById(R.id.landlordEmailTV);
            imageView = itemView.findViewById(R.id.landLordProfileImage);

            itemView
                    .setOnClickListener(V -> {
                        int currentPosition = getAdapterPosition();
                        if (currentPosition != RecyclerView.NO_POSITION) {
                            onLandLordClickedListener.onClick(currentPosition);
                        }
                    })
            ;
        }
    }

    public interface OnLandLordClickedListener {
        void onClick(int position);
    }
}
