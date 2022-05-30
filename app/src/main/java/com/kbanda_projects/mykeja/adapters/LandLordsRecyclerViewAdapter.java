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

public class LandLordsRecyclerViewAdapter extends RecyclerView.Adapter<LandLordsRecyclerViewAdapter.LandLordViewHolder> {
    private Context context;
    private List<User> userList;

    public LandLordsRecyclerViewAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public LandLordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LandLordViewHolder(
                LayoutInflater.from(context)
                        .inflate(R.layout.item_landlords, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull LandLordViewHolder holder, int position) {
        User currentUser = userList.get(position);
        String firstName = currentUser.getFirstName();
        String lastName = currentUser.getLastName();
        String username = firstName + " " + lastName;
        holder.name.setText(username);
        holder.email.setText(currentUser.getEmail());
        holder.phoneNumber.setText(currentUser.getPhoneNumber());

        if (currentUser.getProfileImageUrl() != null) {
            if (!currentUser.getProfileImageUrl().trim().isEmpty()) {
                Glide
                        .with(context)
                        .load(currentUser.getProfileImageUrl())
                        .centerCrop()
                        .fitCenter()
                        .into(holder.profileImage)
                ;
            }
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class LandLordViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private CircleImageView profileImage;
        private TextView email;
        private TextView phoneNumber;

        public LandLordViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.landlordNameTV);
            profileImage = itemView.findViewById(R.id.landLordProfileImage);
            phoneNumber = itemView.findViewById(R.id.landlordPhoneNumberTV);
            email = itemView.findViewById(R.id.landlordEmailTV);
        }
    }
}
