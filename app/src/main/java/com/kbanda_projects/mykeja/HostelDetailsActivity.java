package com.kbanda_projects.mykeja;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kbanda_projects.mykeja.adapters.HostelImagesRecyclerViewAdapter;
import com.kbanda_projects.mykeja.models.Hostel;
import com.kbanda_projects.mykeja.models.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HostelDetailsActivity extends AppCompatActivity {
    private static final String TAG = "HostelDetailsActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private TextView nameOfHostelTV;
    private TextView rentPriceTV;
    private TextView descriptionTV;
    private TextView ratingsCountTV;

    private View landlordView;
    private TextView landlordNameTV;
    private Button buttonCallLandLord;
    private Button buttonEmailLandLord;

    private HostelImagesRecyclerViewAdapter imagesRecyclerViewAdapter;
    private List<String> hostelImagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostel_details);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        hostelImagesList = new ArrayList<>();
        nameOfHostelTV = findViewById(R.id.hostelNameTV);
        rentPriceTV = findViewById(R.id.hostelRentPriceTV);
        descriptionTV = findViewById(R.id.hostelDescriptionTV);
        ratingsCountTV = findViewById(R.id.ratingsCountTV);
        RecyclerView imagesRecyclerView = findViewById(R.id.hostelImageRecyclerView);
        landlordView = findViewById(R.id.landlordInformationView);

        buttonCallLandLord = findViewById(R.id.buttonCall);
        buttonEmailLandLord = findViewById(R.id.buttonEmail);
        landlordNameTV = findViewById(R.id.landLordNameTV);

        imagesRecyclerViewAdapter = new HostelImagesRecyclerViewAdapter(hostelImagesList, this);

        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        imagesRecyclerView.setHasFixedSize(true);

        imagesRecyclerView.setAdapter(imagesRecyclerViewAdapter);

        if (getIntent().getExtras() != null) {
            Hostel currentHostelFromIntent = (Hostel) getIntent().getSerializableExtra("currentHostel");

            loadHostelInformation(currentHostelFromIntent);
            getUserInformationFromFirebase(currentHostelFromIntent);
        }
    }

    private void loadHostelInformation(Hostel hostel) {
        String rent = "Ksh. " + hostel.getRentPricePerMonth() + " per month";

        nameOfHostelTV.setText(hostel.getName());
        descriptionTV.setText(hostel.getDescription());
        ratingsCountTV.setText(hostel.getRatings());
        rentPriceTV.setText(rent);

        hostelImagesList.clear();
        hostelImagesList.addAll(hostel.getImageUrls());
        imagesRecyclerViewAdapter.notifyDataSetChanged();

        imagesRecyclerViewAdapter
                .setOnHostelImageClickedListener(imagePosition -> {
                    //ToDo: View Image in full Screen
                    Intent intent = new Intent(this, ImageSliderActivity.class);
                    intent.putExtra("hostelImages", (Serializable) hostelImagesList);
                    startActivity(intent);
                });
    }

    private void getUserInformationFromFirebase(Hostel hostelInformation) {
        String landlordId = hostelInformation.getOwnerId();
        if (!landlordId.isEmpty()) {
            firestore
                    .collection("Users")
                    .document(landlordId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            User user = task.getResult().toObject(User.class);
                            if (user != null) {
                                Log.d(TAG, "getUserInformationFromFirebase: LandLord info -> " + user);
                                loadLandLordInformationToViews(user);
                            }
                        } else {
                            Log.d(TAG, "getUserInformationFromFirebase: Failed to fetch user data -> " + task.getException());
                        }
                    })
            ;
        }
    }

    private void loadLandLordInformationToViews(User user) {
        if (user != null) {
            String firstName = user.getFirstName();
            String lastName = user.getLastName();
            String landLordName = firstName + " " + lastName;
            landlordView.setVisibility(View.VISIBLE);
            buttonCallLandLord.setVisibility(View.VISIBLE);
            buttonEmailLandLord.setVisibility(View.VISIBLE);
            landlordNameTV.setText(landLordName);
        }
    }
}