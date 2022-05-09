package com.kbanda_projects.mykeja;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kbanda_projects.mykeja.adapters.HostelImagesRecyclerViewAdapter;
import com.kbanda_projects.mykeja.models.Hostel;
import com.kbanda_projects.mykeja.models.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class HostelDetailsActivity extends AppCompatActivity {
    private static final String TAG = "HostelDetailsActivity";
    public static final int REQUEST_CODE_CALL = 923;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private TextView nameOfHostelTV;
    private TextView rentPriceTV;
    private TextView descriptionTV;
    private TextView ratingsCountTV;
    private Chip roomType;
    private Chip numberOfRooms;
    private Chip wifi;
    private Chip parking;

    private View landlordView;
    private TextView landlordNameTV;
    private Button buttonCallLandLord;
    //    private Button buttonEmailLandLord;
    private Button buttonWhatsAppLandLord;

    private HostelImagesRecyclerViewAdapter imagesRecyclerViewAdapter;
    private List<String> hostelImagesList;

    private String landLordPhoneNumber = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostel_details);

        setSupportActionBar(findViewById(R.id.hostelDetailsToolbar));
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        hostelImagesList = new ArrayList<>();
        nameOfHostelTV = findViewById(R.id.hostelNameTV);
        rentPriceTV = findViewById(R.id.hostelRentPriceTV);
        descriptionTV = findViewById(R.id.hostelDescriptionTV);
        ratingsCountTV = findViewById(R.id.ratingsCountTV);
        roomType = findViewById(R.id.roomTypeChip);
        numberOfRooms = findViewById(R.id.numberOfRoomsChip);
        wifi = findViewById(R.id.wifiChip);
        parking = findViewById(R.id.parkingChip);
        RecyclerView imagesRecyclerView = findViewById(R.id.hostelImageRecyclerView);
        landlordView = findViewById(R.id.landlordInformationView);

        buttonCallLandLord = findViewById(R.id.buttonCall);
//        buttonEmailLandLord = findViewById(R.id.buttonEmail);
        buttonWhatsAppLandLord = findViewById(R.id.buttonWhatsApp);
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
        buttonCallLandLord
                .setOnClickListener(v -> {
                    checkLocationPermissions();
                })
        ;
        buttonWhatsAppLandLord
                .setOnClickListener(v -> {
                    whatsAppLandLord();
                })
        ;
    }

    @AfterPermissionGranted(REQUEST_CODE_CALL)
    private void checkLocationPermissions() {
        String[] permissions = {
                Manifest.permission.CALL_PHONE
        };
        if (EasyPermissions.hasPermissions(this, permissions)) {
            callLandLord();
        } else {
            EasyPermissions
                    .requestPermissions(
                            this,
                            getString(R.string.app_name) + " requires this permissions in order to make a call",
                            REQUEST_CODE_CALL,
                            permissions)
            ;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions
                .onRequestPermissionsResult(
                        requestCode,
                        permissions,
                        grantResults,
                        this
                )
        ;
    }

    private void callLandLord() {
        if (landLordPhoneNumber != null) {
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + landLordPhoneNumber)));
        }
    }

    private void whatsAppLandLord() {
        if (landLordPhoneNumber != null) {
            Uri uri = Uri.parse("smsto:" + landLordPhoneNumber);
            Intent i = new Intent(Intent.ACTION_SENDTO, uri);
            i.setPackage("com.whatsapp");
            startActivity(Intent.createChooser(i, "WhatsApp Hostel Owner"));
        }
    }

    private void loadHostelInformation(Hostel hostel) {
        boolean hasParking = hostel.isHasParking();
        boolean hasWifi = hostel.isHasWifi();
        String rent = "Ksh. " + hostel.getRentPricePerMonth() + " per month";

        nameOfHostelTV.setText(hostel.getName());
        descriptionTV.setText(hostel.getDescription());
        ratingsCountTV.setText(hostel.getRatings());
        rentPriceTV.setText(rent);
        roomType.setText(hostel.getRoomType());
        String totalRoomsAvailable = hostel.getTotalRoomsAvailable() + " rooms available";
        numberOfRooms.setText(totalRoomsAvailable);

        if (hasParking) parking.setVisibility(View.VISIBLE);
        if (hasWifi) parking.setVisibility(View.VISIBLE);

        hostelImagesList.clear();
        hostelImagesList.addAll(hostel.getImageUrls());
        imagesRecyclerViewAdapter.notifyDataSetChanged();

        imagesRecyclerViewAdapter
                .setOnHostelImageClickedListener(imagePosition -> {
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
            landLordPhoneNumber = user.getPhoneNumber();
            if (landLordPhoneNumber != null)
                if (!landLordPhoneNumber.isEmpty()) {
                    buttonCallLandLord.setVisibility(View.VISIBLE);
                }
            landlordView.setVisibility(View.VISIBLE);

//            buttonEmailLandLord.setVisibility(View.VISIBLE);
            landlordNameTV.setText(landLordName);
        }
    }
}