package com.kbanda_projects.mykeja.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.kbanda_projects.mykeja.HostelDetailsActivity;
import com.kbanda_projects.mykeja.R;
import com.kbanda_projects.mykeja.adapters.HostelRecyclerViewAdapter;
import com.kbanda_projects.mykeja.models.Hostel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FirebaseFirestore firebaseFirestore;
    private HostelRecyclerViewAdapter recyclerViewAdapter;
    private SearchView searchView;
    private List<Hostel> hostelList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // Inflate the layout for this fragment
        firebaseFirestore = FirebaseFirestore.getInstance();
        hostelList = new ArrayList<>();
        RecyclerView recyclerView = view.findViewById(R.id.mainRecyclerView);
        recyclerViewAdapter = new HostelRecyclerViewAdapter(requireActivity(), hostelList);
        searchView = view.findViewById(R.id.searchViewHome);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.setAdapter(recyclerViewAdapter);
        fetchHostelsFromDatabase();

        //TODO: Be able to search for Hostels

        recyclerViewAdapter.setOnHostelClickedListener(
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
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void searchForHostelInDatabase(String query) {
        firebaseFirestore
                .collection("Hostels")
                .whereEqualTo("tags", query)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error);
                            return;
                        }

                        if (value != null) {
                            hostelList.clear();
                            for (DocumentChange documentChange : value.getDocumentChanges()) {
                                Hostel hostel = documentChange.getDocument().toObject(Hostel.class);
                                hostelList.add(hostel);
                                recyclerViewAdapter.notifyDataSetChanged();
                                Log.d(TAG, "onEvent: Documents -> " + hostel);
                            }
                        } else {
                            Log.d(TAG, "onEvent: No listings available");
                        }
                    }
                })
        ;
    }

    private void fetchHostelsFromDatabase() {
        Log.d(TAG, "fetchHostelsFromDatabase: Fetching results");
        firebaseFirestore
                .collection("Hostels")
                .whereEqualTo("isVacant", true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error);
                            return;
                        }

                        if (value != null) {
                            hostelList.clear();
                            for (DocumentChange documentChange : value.getDocumentChanges()) {
                                Hostel hostel = documentChange.getDocument().toObject(Hostel.class);
                                hostelList.add(hostel);
                                recyclerViewAdapter.notifyDataSetChanged();
                                Log.d(TAG, "onEvent: Documents -> " + hostel);
                            }
                        } else {
                            Log.d(TAG, "onEvent: No listings available");
                        }
                    }
                })
        ;
    }
}