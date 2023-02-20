package com.kbanda_projects.mykeja.ui.fragments;

import android.app.ProgressDialog;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.kbanda_projects.mykeja.R;
import com.kbanda_projects.mykeja.adapters.BookMarksRecyclerViewAdapter;
import com.kbanda_projects.mykeja.models.Hostel;

import java.util.ArrayList;
import java.util.List;

public class BookMarkFragment extends Fragment {
    private static final String TAG = "BookMarkFragment";
    private RecyclerView recyclerView;
    private BookMarksRecyclerViewAdapter recyclerViewAdapter;
    private List<Hostel> bookmarkList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        return inflater.inflate(R.layout.fragment_book_mark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(requireActivity());
        bookmarkList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.bookMarkRecyclerView);
        recyclerViewAdapter = new BookMarksRecyclerViewAdapter(requireActivity(), bookmarkList);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.setAdapter(recyclerViewAdapter);

        fetchHostelsFromDatabase();
        super.onViewCreated(view, savedInstanceState);
    }

    private void fetchHostelsFromDatabase() {
        if (currentUser != null) {
            Log.d(TAG, "fetchHostelsFromDatabase: Fetching results");
            progressDialog.setTitle("Fetching data");
            progressDialog.setMessage("Fetching hostels...");
            progressDialog.create();
            progressDialog.show();

            firebaseFirestore
                    .collection("Hostels")
                    .whereArrayContains("bookMarks", currentUser.getUid())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            progressDialog.dismiss();
                            if (error == null) {
                                if (value != null && !value.isEmpty()) {
                                    if (value.size() > 0) {
                                        //Ensure you get the last post of the document
                                        bookmarkList.clear();
                                        for (DocumentChange documentChange : value.getDocumentChanges()) { //Loop over the documents
                                            if (documentChange.getType() == DocumentChange.Type.ADDED) { //Check if any data has been added
                                                Hostel hostelObject = documentChange.getDocument().toObject(Hostel.class);
//                                            hostelObject.setDocumentId(documentChange.getDocument().getId());
                                                bookmarkList.add(hostelObject);
                                                recyclerViewAdapter.notifyItemInserted(bookmarkList.size());
                                                Log.d(TAG, "onEvent: Listing -> " + hostelObject.toString());
                                            }
                                        }
                                        Log.d(TAG, "onEvent: BookMarks -> " + bookmarkList);
                                    }
                                } else {
                                    new AlertDialog
                                            .Builder(requireActivity())
                                            .setMessage("It seems that there are no bookmarks present!")
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
}