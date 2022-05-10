package com.kbanda_projects.mykeja;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.kbanda_projects.mykeja.adapters.HostelImagesRecyclerViewAdapter;
import com.kbanda_projects.mykeja.models.Hostel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class AdminAddEditHostel extends AppCompatActivity {
    private static final String TAG = "AdminAddEditHostel";
    private static final int REQUEST_CODE_SELECT_PROFILE_IMAGE = 702;
    public static final int REQUEST_CODE_GET_LOCATION_PERMISSION = 345;
    private FusedLocationProviderClient locationProviderClient;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage firebaseStorage;
    private ProgressDialog progressDialog;

    private RecyclerView imagesRecyclerView;
    private TextInputLayout hostelNameTIL;
    private TextInputLayout hostelDescriptionTIL;
    private TextInputLayout hostelRentTIL;
    private AutoCompleteTextView roomTypeAutoCompleteTV;
    private SwitchMaterial hasWifiSwitch;
    private SwitchMaterial hasParkingSwitch;
    //    private Spinner roomTypeSpinner;
    private HostelImagesRecyclerViewAdapter imagesRecyclerViewAdapter;
    private List<String> hostelImageList;
    private Hostel currentHostelFromIntent;
    private String[] roomTypes;
    private TextView latitudeTV;
    private TextView longitudeTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_edit_hostel);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        setSupportActionBar(findViewById(R.id.adminAddEditHostelToolbar));
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);

        imagesRecyclerView = findViewById(R.id.hostelImageRecyclerView);
        hostelNameTIL = findViewById(R.id.hostelNameTIL);
        hostelDescriptionTIL = findViewById(R.id.hostelDescriptionTIL);
        hostelRentTIL = findViewById(R.id.hostelRentPriceTIL);
        hasWifiSwitch = findViewById(R.id.hasWifi);
        hasParkingSwitch = findViewById(R.id.hasParking);
        roomTypeAutoCompleteTV = findViewById(R.id.roomTypeAutoTV);
        latitudeTV = findViewById(R.id.latitudeTV);
        longitudeTV = findViewById(R.id.longitudeTV);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//        roomTypeSpinner = findViewById(R.id.roomTypeSpinner);

        hostelImageList = new ArrayList<>();
        imagesRecyclerViewAdapter = new HostelImagesRecyclerViewAdapter(hostelImageList, this);

        imagesRecyclerView.setHasFixedSize(true);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(
                this,
                RecyclerView.HORIZONTAL,
                false)
        );
        imagesRecyclerView.setAdapter(imagesRecyclerViewAdapter);

        if (getIntent().getExtras() != null) {
            currentHostelFromIntent = (Hostel) getIntent().getSerializableExtra("currentHostel");
            loadHostelInformation(currentHostelFromIntent);
            imagesRecyclerViewAdapter
                    .setOnHostelImageClickedListener(imagePosition -> {
                        Intent intent = new Intent(this, ImageSliderActivity.class);
                        intent.putExtra("hostelImages", (Serializable) hostelImageList);
                        startActivity(intent);
                    })
            ;
        }

        findViewById(R.id.fabAddHostelImages)
                .setOnClickListener(v -> {
                    imageChooser();
                })
        ;
        roomTypes = new String[]{"Self-contained", "single room", "mixed"};
        ArrayAdapter spinnerAdapter = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                roomTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomTypeAutoCompleteTV.setAdapter(spinnerAdapter);
        roomTypeAutoCompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(AdminAddEditHostel.this, "Item: " + roomTypes[i], Toast.LENGTH_SHORT).show();
            }
        });
    }

    @AfterPermissionGranted(REQUEST_CODE_GET_LOCATION_PERMISSION)
    private void checkLocationPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        if (EasyPermissions.hasPermissions(this, permissions)) {
            getCurrentLocation();
        } else {
            EasyPermissions
                    .requestPermissions(
                            this,
                            getString(R.string.app_name) + " requires this permissions in order to use this feature",
                            REQUEST_CODE_GET_LOCATION_PERMISSION,
                            permissions
                    )
            ;
        }
    }

    private void loadHostelInformation(Hostel hostel) {
        if (hostel != null) {
            String name = hostel.getName();
            String description = hostel.getDescription();
            String rentPricePerMonth = hostel.getRentPricePerMonth();
            boolean hasParking = hostel.isHasParking();
            boolean hasWifi = hostel.isHasWifi();

            hostelImageList.clear();
            hostelImageList.addAll(hostel.getImageUrls());
            imagesRecyclerViewAdapter.notifyDataSetChanged();

            hasWifiSwitch.setChecked(hasWifi);
            hasParkingSwitch.setChecked(hasParking);

            hostelNameTIL.getEditText().setText(name);
            hostelDescriptionTIL.getEditText().setText(description);
            hostelRentTIL.getEditText().setText(rentPricePerMonth);
        }
    }

    private void imageChooser() {
        Options options = Options
                .init()
                .setRequestCode(REQUEST_CODE_SELECT_PROFILE_IMAGE)
                .setCount(10)
                .setMode(Options.Mode.Picture);
        Pix.start(this, options);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_PROFILE_IMAGE) {
            if (data != null) {
                ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
                if (!returnValue.isEmpty()) {
                    for (String imageStringUri : returnValue) {
                        hostelImageList.add(imageStringUri);
                        imagesRecyclerViewAdapter.notifyItemInserted(hostelImageList.size() + 1);
                    }
                } else {
                    Toast.makeText(this, "No Image Selected!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_edit_hostel, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionSaveHostel: {
                String name = hostelNameTIL.getEditText().getText().toString();
                String description = hostelDescriptionTIL.getEditText().getText().toString();
                String rentPrice = hostelRentTIL.getEditText().getText().toString();
                String latitude = latitudeTV.getText().toString();
                String longitude = longitudeTV.getText().toString();
                Map<String, String> locationInfo = new HashMap<>();
                locationInfo.put("latitude", latitude);
                locationInfo.put("longitude", longitude);
                Hostel hostel = new Hostel();
                hostel.setName(name);
                hostel.setDescription(description);
                hostel.setRentPricePerMonth(rentPrice);
                hostel.setRatings("0");
                hostel.setHasParking(hasParkingSwitch.isChecked());
                hostel.setHasWifi(hasWifiSwitch.isChecked());
                hostel.setLocationInfo(locationInfo);
                hostel.setTotalRoomsAvailable("0");

                hostel.setTags(Arrays.asList(name, name.toUpperCase(), name.toLowerCase()));
                saveHostel(hostel);
                return true;
            }
            case R.id.actionDeleteHostel: {
                deleteHostel();
                return true;
            }
            case R.id.actionGetCurrentLocation: {
                checkLocationPermissions();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //If/When the location service in enabled, get the last location
            locationProviderClient
                    .getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            //Initialize location
                            Location location = task.getResult();
                            if (location != null) {
                                //Display the user location on map
                                displayCurrentLocation(
                                        location.getLatitude(),
                                        location.getLongitude()
                                );
                            } else {
                                //Initialize Location Request if location is Null
                                LocationRequest locationRequest = new LocationRequest()
                                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                                        .setInterval(1000)
                                        .setFastestInterval(10000)
                                        .setNumUpdates(1);
                                LocationCallback callback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        Location location1 = locationResult.getLastLocation();
                                        displayCurrentLocation(
                                                location1.getLatitude(),
                                                location1.getLongitude()
                                        );
                                    }
                                };
                                locationProviderClient
                                        .requestLocationUpdates(
                                                locationRequest,
                                                callback,
                                                Looper.myLooper()
                                        )
                                ;
                            }
                        }
                    });
        } else {
            //If the location has NOT yet been enabled... take user to location settings
            new AlertDialog.Builder(this)
                    .setTitle("Get Current Location")
                    .setIcon(R.drawable.ic_map)
                    .setMessage("Please switch on your device's location so we can determine the hostels around you")
                    .setPositiveButton("OK", ((dialogInterface, i) -> {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }))
                    .create()
                    .show()
            ;
        }
    }

    private void displayCurrentLocation(double latitude, double longitude) {

        latitudeTV.setText(String.valueOf(latitude));
        longitudeTV.setText(String.valueOf(longitude));
    }

    private void deleteHostel() {
    }

    private void saveHostel(Hostel hostel) {
        if (currentUser != null) {
            if (currentHostelFromIntent != null) {

            } else {
                hostel.setOwnerId(currentUser.getUid());
                progressDialog.setTitle("Saving");
                progressDialog.setMessage("Saving hostel details");
                progressDialog.setCancelable(false);
                progressDialog.create();
                progressDialog.show();
                firebaseFirestore
                        .collection("Hostels")
                        .add(hostel)
                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: Saved hostel successfylly");
                                    Toast.makeText(AdminAddEditHostel.this, "Saved sucessfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Log.d(TAG, "onComplete: Failed to save hostel");
                                }
                            }
                        })
                        .addOnFailureListener(Throwable::printStackTrace)
                ;
            }
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
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(this, Options.init().setRequestCode(REQUEST_CODE_SELECT_PROFILE_IMAGE));
                } else {
                    Toast.makeText(this, "Approve permissions to open enable image selection", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}