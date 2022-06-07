package com.kbanda_projects.mykeja.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kbanda_projects.mykeja.R;
import com.kbanda_projects.mykeja.models.Feedback;
import com.kbanda_projects.mykeja.models.User;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedbackRecyclerViewAdapter extends RecyclerView.Adapter<FeedbackRecyclerViewAdapter.FeedbackViewHolder> {
    private static final String TAG = "FeedbackRecyclerViewAdapter";
    private List<Feedback> feedbackList;
    private Context context;
    private OnFeedbackClickedListener onFeedbackClickedListener;

    private FirebaseFirestore firebaseFirestore;

    public interface OnFeedbackClickedListener {
        void onClick(int position, String userPhoneNumber);
    }

    public FeedbackRecyclerViewAdapter(List<Feedback> feedbackList, Context context) {
        this.feedbackList = feedbackList;
        this.context = context;
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public void setOnFeedbackClickedListener(OnFeedbackClickedListener onFeedbackClickedListener) {
        this.onFeedbackClickedListener = onFeedbackClickedListener;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FeedbackViewHolder(
                LayoutInflater.from(context)
                        .inflate(R.layout.item_feedback, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);
        String comment = feedback.getComment();
        String timeInMillis = feedback.getTimeInMillis();
        holder.feedbackComment.setText(comment);
        String timeStamp = convertCurrentTimeInMillisToStringTimeStamp(feedback);
        if (timeStamp != null) {
            holder.timeStamp.setText(timeStamp);
        }
        if (feedback.getUserId() != null) {
            String userId = feedback.getUserId();
            fetchUserDataFromDatabase(userId, holder, feedback);
        }
    }

    private void fetchUserDataFromDatabase(String userId, FeedbackViewHolder feedbackViewHolder, Feedback feedback) {
        if (userId != null) {
            if (!userId.isEmpty()) {
                firebaseFirestore
                        .collection("Users")
                        .document(userId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                User user = task.getResult().toObject(User.class);
                                Log.d("FeedbackRecyclerView", "fetchUserDataFromDatabase: User data -> " + user.toString());
                                if (user.getProfileImageUrl() != null) {
                                    Glide
                                            .with(context)
                                            .load(user.getProfileImageUrl())
                                            .placeholder(R.drawable.ic_user_placeholder)
                                            .centerCrop()
                                            .into(feedbackViewHolder.profileImage)
                                    ;
                                }
                                if (user.getPhoneNumber() != null) {
                                    String phoneNumber = user.getPhoneNumber();
                                    feedbackViewHolder
                                            .itemView
                                            .setOnClickListener(v -> {
                                                int adapterPosition = feedbackViewHolder.getAdapterPosition();
                                                if (!phoneNumber.isEmpty()) {
                                                    if (adapterPosition != RecyclerView.NO_POSITION) {
                                                        onFeedbackClickedListener
                                                                .onClick(adapterPosition, phoneNumber);
                                                    } else {
                                                        onFeedbackClickedListener
                                                                .onClick(adapterPosition, "");
                                                    }
                                                }
                                            })
                                    ;
                                }
                                if (user.getFirstName() != null) {
                                    String name = user.getFirstName();
                                    feedbackViewHolder.name.setText(name);
                                }
                            } else {
                                Log.d("FeedbackRecyclerView", "fetchUserDataFromDatabase: Failed to load user info -> " + task.getException().getMessage());
                            }
                        })
                ;
            }
        }
    }

    private String convertCurrentTimeInMillisToStringTimeStamp(Feedback feedback) {
        if (feedback.getTimeInMillis() != null) {
            Date date = new Date(Long.parseLong(feedback.getTimeInMillis()));
            DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            String timeStamp = simpleDateFormat.format(date);
            Log.d("FeedbackRecyclerView", "convertCurrentTimeInMillisToStringTimeStamp: Timestamp -> " + timeStamp);
            return timeStamp;
        } else {
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    class FeedbackViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private CircleImageView profileImage;
        private TextView timeStamp;
        private TextView feedbackComment;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userNameTV);
            profileImage = itemView.findViewById(R.id.userProfileImage);
            feedbackComment = itemView.findViewById(R.id.feebackCommentTV);
            timeStamp = itemView.findViewById(R.id.feedbackTimeStampTV);
        }
    }
}
