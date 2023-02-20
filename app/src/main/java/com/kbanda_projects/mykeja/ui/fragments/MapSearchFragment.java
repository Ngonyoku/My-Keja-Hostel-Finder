package com.kbanda_projects.mykeja.ui.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.kbanda_projects.mykeja.ui.HostelDetailsActivity;
import com.kbanda_projects.mykeja.R;
import com.kbanda_projects.mykeja.models.Hostel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MapSearchFragment extends Fragment {
    private static final String TAG = "MapSearchFragment";
    public static final int REQUEST_CODE_MAPS_PERMISSIONS = 524;
    private FusedLocationProviderClient locationProviderClient;
    private GoogleMap map;
    private FirebaseFirestore firebaseFirestore;
    private List<Hostel> hostelList;
    private SearchView searchView;

    //TODO: Display List of hostels

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
//            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            checkLocationPermissions(googleMap);
            map
                    .setOnMarkerClickListener(marker -> {
                        Intent intent = new Intent(requireActivity(), HostelDetailsActivity.class);
                        Hostel selectedHostel = (Hostel) marker.getTag();
                        intent.putExtra("currentHostel", selectedHostel);
                        requireActivity().startActivity(intent);
                        return false;
                    })
            ;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.map_search_toolbar, menu);
        MenuItem searchViewItem = menu.findItem(R.id.actionSearchHostel);
        searchView = (SearchView) searchViewItem.getActionView();
        searchView.setQueryHint("Find Hostel...");
        searchView
                .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        if (!s.isEmpty()) {
                            for (Hostel hostel : hostelList) {
                                if (hostel.getTags().contains(s)) {
                                    showHostelOnMap(map, hostel, BitmapDescriptorFactory.HUE_MAGENTA);
                                }
                            }
                        } else {
                            Toast.makeText(requireActivity(), "Nothing to search", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionGetCurrentLocation: {
                getUserCurrentLocation(map);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        hostelList = new ArrayList<>();
        firebaseFirestore = FirebaseFirestore.getInstance();
        locationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        View view = inflater.inflate(R.layout.fragment_map_search, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialToolbar toolbar = getActivity().findViewById(R.id.mainToolbar);
        toolbar.setTitle("Find on Map");
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @AfterPermissionGranted(REQUEST_CODE_MAPS_PERMISSIONS)
    private void checkLocationPermissions(GoogleMap googleMap) {
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        if (EasyPermissions.hasPermissions(requireActivity(), permissions)) {
            getUserCurrentLocation(googleMap);
        } else {
            EasyPermissions
                    .requestPermissions(
                            requireActivity(),
                            getString(R.string.app_name) + " requires this permissions in order to use this feature",
                            REQUEST_CODE_MAPS_PERMISSIONS,
                            permissions)
            ;
        }
    }

    /*
     * Check to see the user's current location
     * */
    @SuppressLint("MissingPermission")
    private void getUserCurrentLocation(GoogleMap googleMap) {
        LocationManager locationManager = (LocationManager)
                requireActivity().getSystemService(Context.LOCATION_SERVICE);
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
                            fetchHostelsFromDatabase();
                        }
                    });
        } else {
            //If the location has NOT yet been enabled... take user to location settings
            new AlertDialog.Builder(requireActivity())
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
                        requireActivity()
                )
        ;
    }

    private void fetchHostelsFromDatabase() {
        Log.d(TAG, "fetchHostelsFromDatabase: Fetching hostels from database");
        firebaseFirestore
                .collection("Hostels")
                .whereEqualTo("vacant", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error);
                            return;
                        }

                        if (value != null) {
                            hostelList.clear();
                            //Loop through the documents
                            for (DocumentChange documentChange : value.getDocumentChanges()) {
                                Hostel hostel = documentChange.getDocument().toObject(Hostel.class);
                                hostelList.add(hostel);
                                Log.d(TAG, "onEvent: Documents -> " + hostel);
                                showHostelOnMap(map, hostel);
                            }
                        } else {
                            Log.d(TAG, "onEvent: No listings available");
                        }
                    }
                })
        ;
    }

    /*
     * Display the hostels on map view
     * */
    private void showHostelOnMap(GoogleMap googleMap, Hostel hostel) {
        if (map != null) {
            Log.d(TAG, "showHostelOnMap: Hostel -> " + hostel.toString());
            Map<String, String> locationInfo = hostel.getLocationInfo();
            if (locationInfo != null) {
                String stringLat = locationInfo.get("latitude");
                String stringLon = locationInfo.get("longitude");
                if (stringLat != null && !stringLat.trim().isEmpty()) {
                    if (stringLon != null && !stringLon.trim().isEmpty()) {
                        double latitude = Double.parseDouble(Objects.requireNonNull(stringLat));
                        double longitude = Double.parseDouble(Objects.requireNonNull(locationInfo.get("longitude")));
                        String title = hostel.getName();
                        Log.d(TAG, "showHostelOnMap: Adding markers to map");
                        LatLng location = new LatLng(latitude, longitude);
                        Marker marker = googleMap.addMarker(
                                new MarkerOptions().position(location).title(title)
                        );
                        assert marker != null;
                        marker.setTag(hostel);
                    }
                }
            }
        }
    }

    private void showHostelOnMap(GoogleMap googleMap, Hostel hostel, float markerHue) {
        if (map != null) {
            Log.d(TAG, "showHostelOnMap: Hostel -> " + hostel.toString());
            Map<String, String> locationInfo = hostel.getLocationInfo();
            if (locationInfo != null) {
                String stringLat = locationInfo.get("latitude");
                String stringLon = locationInfo.get("longitude");
                if (stringLat != null && !stringLat.trim().isEmpty()) {
                    if (stringLon != null && !stringLon.trim().isEmpty()) {
                        double latitude = Double.parseDouble(Objects.requireNonNull(stringLat));
                        double longitude = Double.parseDouble(Objects.requireNonNull(locationInfo.get("longitude")));
                        String title = hostel.getName();
                        Log.d(TAG, "showHostelOnMap: Adding markers to map");
                        LatLng location = new LatLng(latitude, longitude);
                        Marker marker = googleMap.addMarker(
                                new MarkerOptions()
                                        .position(location)
                                        .title(title)
                                        .icon(BitmapDescriptorFactory.defaultMarker(markerHue))
                        );
                        assert marker != null;
                        marker.setTag(hostel);
                    }
                }
            }
        }
    }

    /*
     * Where the user is currently located at
     * */
    private void displayCurrentLocation(double latitude, double longitude) {
        if (map != null) {
            LatLng userLocation = new LatLng(latitude, longitude);
//            MarkerOptions your_current_location = new MarkerOptions()
//                    .position(userLocation)
//                    .title("Your current location")
//                    .snippet("This is where you are at right now")
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
//            map.addMarker(your_current_location
//            );
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(latitude, longitude),
                    10
            ));
        }
    }
}