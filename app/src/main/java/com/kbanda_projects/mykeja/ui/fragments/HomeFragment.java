package com.kbanda_projects.mykeja.ui.fragments;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.kbanda_projects.mykeja.ui.HostelDetailsActivity;
import com.kbanda_projects.mykeja.R;
import com.kbanda_projects.mykeja.adapters.HostelRecyclerViewAdapter;
import com.kbanda_projects.mykeja.models.Hostel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    //TODO: Use view binding
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private HostelRecyclerViewAdapter recyclerViewAdapter;
    private List<Hostel> hostelList;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        setHasOptionsMenu(true);
        currentUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(requireActivity());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.hostels_toolbar, menu);
        MenuItem searchViewItem = menu.findItem(R.id.actionSearchHostel);
//        SearchView searchView = (SearchView) searchViewItem.getActionView();
//        searchView.setQueryHint("Search Hostel...");
//        searchView
//                .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                    @Override
//                    public boolean onQueryTextSubmit(String s) {
//                        List<Hostel> searchHostelResults = new ArrayList<>();
//                        if (!s.trim().isEmpty()) {
//                            if (!hostelList.isEmpty()) {
//                                for (Hostel hostel : hostelList) {
//                                    if (hostel.getTags().contains(s)) {
//                                        searchHostelResults.add(hostel);
//                                    }
//                                }
//
//                                hostelList.clear();
//                                if (!searchHostelResults.isEmpty()) {
//                                    for (Hostel hostel : searchHostelResults) {
//                                        hostelList.add(hostel);
//                                        recyclerViewAdapter.notifyDataSetChanged();
//                                    }
//                                }
//                            }
//                        } else {
//                            Toast.makeText(requireActivity(), "Nothing to search", Toast.LENGTH_SHORT).show();
//                        }
//                        return true;
//                    }
//
//                    @Override
//                    public boolean onQueryTextChange(String s) {
//                        return false;
//                    }
//                });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionRefreshHostels: {
                fetchHostelsFromDatabase();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // Inflate the layout for this fragment
        hostelList = new ArrayList<>();
        RecyclerView recyclerView = view.findViewById(R.id.mainRecyclerView);
        recyclerViewAdapter = new HostelRecyclerViewAdapter(requireActivity(), hostelList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.setAdapter(recyclerViewAdapter);
        fetchHostelsFromDatabase();

        recyclerViewAdapter
                .setOnHostelClickedListener(
                        new HostelRecyclerViewAdapter.OnHostelClickedListener() {
                            @Override
                            public void onClick(int position) {
                                Hostel currentHostel = hostelList.get(position);
                                Intent intent = new Intent(requireActivity(), HostelDetailsActivity.class);
                                intent.putExtra("currentHostel", currentHostel);
//                                intent.putExtra("documentId")
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
                                    hostelList.clear();
                                    for (DocumentChange documentChange : value.getDocumentChanges()) { //Loop over the documents
                                        if (documentChange.getType() == DocumentChange.Type.ADDED) { //Check if any data has been added
                                            Hostel hostelObject = documentChange.getDocument().toObject(Hostel.class);
                                            hostelObject.setDocumentId(documentChange.getDocument().getId());
                                            hostelList.add(hostelObject);

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