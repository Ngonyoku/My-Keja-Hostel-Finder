package com.kbanda_projects.mykeja.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.kbanda_projects.mykeja.AdminAddEditHostel;
import com.kbanda_projects.mykeja.HostelDetailsActivity;
import com.kbanda_projects.mykeja.R;
import com.kbanda_projects.mykeja.UserProfileActivity;
import com.kbanda_projects.mykeja.adapters.HostelRecyclerViewAdapter;
import com.kbanda_projects.mykeja.models.Hostel;
import com.kbanda_projects.mykeja.models.User;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    //TODO: Use view binding
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private HostelRecyclerViewAdapter recyclerViewAdapter;
    private FloatingActionButton fabAddEditHostelInformation;
    private CircleImageView userProfileImage;
    private SearchView searchView;
    private List<Hostel> hostelList;
    private User user;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressDialog = new ProgressDialog(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // Inflate the layout for this fragment
        hostelList = new ArrayList<>();
        RecyclerView recyclerView = view.findViewById(R.id.mainRecyclerView);
        recyclerViewAdapter = new HostelRecyclerViewAdapter(requireActivity(), hostelList);
        searchView = view.findViewById(R.id.searchViewHome);
        userProfileImage = view.findViewById(R.id.profileImage);
        fabAddEditHostelInformation = view.findViewById(R.id.fabAddEditHostelInformation);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.setAdapter(recyclerViewAdapter);
        fetchHostelsFromDatabase();

        //TODO: Be able to search for Hostels

        if (user == null) userProfileImage.setVisibility(View.GONE);
        else userProfileImage.setVisibility(View.VISIBLE);

        fabAddEditHostelInformation.setVisibility(View.GONE);

        userProfileImage
                .setOnClickListener(v -> {
                    Intent intent = new Intent(requireActivity(), UserProfileActivity.class);
                    if (user != null) {
                        intent.putExtra("user", user);
                        requireActivity().startActivity(intent);
                    }
                })
        ;

        searchView
                .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        if (!s.isEmpty()) {
                            searchForHostelInDatabase(s);
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });

        recyclerViewAdapter
                .setOnHostelClickedListener(
                        new HostelRecyclerViewAdapter.OnHostelClickedListener() {
                            @Override
                            public void onClick(int position) {
                                Hostel currentHostel = hostelList.get(position);
                                Intent intent = new Intent(requireActivity(), HostelDetailsActivity.class);
                                intent.putExtra("currentHostel", currentHostel);
                                requireActivity()
                                        .startActivity(intent);
                            }

                            @Override
                            public void onLongClick(int position) {
                                //TODO: Display a bottom sheet that enables user to contact landlord
                            }
                        })
        ;
        fabAddEditHostelInformation
                .setOnClickListener(v -> {
                    startActivity(new Intent(requireActivity(), AdminAddEditHostel.class));
                })
        ;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchUserData();
    }

    private void fetchUserData() {
        if (currentUser != null) {
            Log.d(TAG, "onViewCreated: Loading User information");
            firebaseFirestore
                    .collection("Users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onViewCreated: Successfully retrieved user data from firebase");
                            if (task.getResult().exists()) { //Check if the document exists
                                user = task.getResult().toObject(User.class);
                                String imageUrl = user.getProfileImageUrl();
                                String userRole = user.getRole();
                                if (userRole.equals("ADMIN")) {
                                    fabAddEditHostelInformation.setVisibility(View.VISIBLE);
                                } else if (userRole.equals("USER")) {
                                    fabAddEditHostelInformation.setVisibility(View.GONE);
                                }
                                Log.d(TAG, "onViewCreated: User Information -> " + user.toString());
                                userProfileImage.setVisibility(View.VISIBLE);
                                if (imageUrl != null) {
                                    if (!imageUrl.isEmpty()) {
                                        Glide
                                                .with(requireActivity())
                                                .load(imageUrl)
                                                .centerCrop()
                                                .into(userProfileImage)
                                        ;
                                    }
                                }
                            } else {
                                createUserDocumentInFirestore();
                            }
                        } else {
                            Log.d(TAG, "onViewCreated: Failed to fetch user data from firebase -> " + task.getException().getMessage());
                        }
                    })
                    .addOnFailureListener(Throwable::printStackTrace)
            ;
        }
    }

    private void createUserDocumentInFirestore() {
        if (currentUser != null) {
            String currentUserUid = currentUser.getUid();
            User userDetails = new User();
            userDetails.setFirstName("");
            userDetails.setLastName("");
            userDetails.setProfileImageUrl("");
            userDetails.setUserId(currentUserUid);
            userDetails.setEmail(currentUser.getEmail());
            userDetails.setRole("USER");
            userDetails.setPhoneNumber("");

            firebaseFirestore
                    .collection("Users")
                    .document(currentUserUid)
                    .set(userDetails)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserDocumentInFirestore: User created successfully");
                        } else {
                            Log.d(TAG, "createUserDocumentInFirestore: Failed to create user document in firebase -> "
                                    + task.getResult());
                        }
                    })
            ;
        }
    }

    private void searchForHostelInDatabase(String query) {
        progressDialog.setMessage("Searching for hostel");
        progressDialog.create();
        progressDialog.show();
        firebaseFirestore
                .collection("Hostels")
                .whereArrayContains("tags", query)
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        hostelList.clear();
                        QuerySnapshot snapshot = task.getResult();
                        if (snapshot != null) {
                            if (!snapshot.isEmpty() && snapshot.size() > 0) {
                                Log.d(TAG, "searchForHostelInDatabase: Search successful");
                                for (DocumentSnapshot result : snapshot) {
                                    Hostel hostel = result.toObject(Hostel.class);
                                    hostelList.add(hostel);
                                    recyclerViewAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    } else {
                        new AlertDialog
                                .Builder(requireActivity())
                                .setTitle("Item not found :(")
                                .setMessage("It seems there are not items to match " + query + ". Try using another search term.")
                                .setCancelable(true)
                                .setPositiveButton("Ok", ((dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                }))
                                .create()
                                .show()
                        ;
                    }
                })
        ;
    }

    private void fetchHostelsFromDatabase() {
        Log.d(TAG, "fetchHostelsFromDatabase: Fetching results");
        progressDialog.setTitle("Fetching data");
        progressDialog.setMessage("Fetching hostels");
        progressDialog.create();
        progressDialog.show();

        firebaseFirestore
                .collection("Hostels")
                .whereEqualTo("vacant", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        progressDialog.dismiss();
                        if (error == null) {
                            if (value != null && !value.isEmpty()) {
                                if (value.size() > 0) {
                                    //Ensure you get the last post of the document
                                    for (DocumentChange documentChange : value.getDocumentChanges()) { //Loop over the documents
                                        if (documentChange.getType() == DocumentChange.Type.ADDED) { //Check if any data has been added
                                            Hostel hostelObject = documentChange.getDocument().toObject(Hostel.class);
//                                            hostelObject.setDocumentId(documentChange.getDocument().getId());
                                            hostelList.add(hostelObject);
//                                            recyclerViewAdapter.notifyDataSetChanged();
                                            recyclerViewAdapter.notifyDataSetChanged();
                                            Log.d(TAG, "onEvent: Listing -> " + hostelObject.toString());
                                        }
                                    }
                                }
                            } else {
                                new AlertDialog
                                        .Builder(requireActivity())
                                        .setMessage("It seems that there are no hostels present!")
                                        .setPositiveButton("OK", ((dialogInterface, i) -> {
                                        }))
                                ;
                            }
                        } else {
                            Log.d(TAG, "onEvent: Failed to load Hostels : -> " + error.getMessage());
                            new AlertDialog.Builder(requireActivity())
                                    .setTitle("Error")
                                    .setMessage("Failed to load hostels : ");
                        }
                    }
                })
        ;
    }
}