package com.kbanda_projects.mykeja.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.kbanda_projects.mykeja.R;
import com.kbanda_projects.mykeja.adapters.LandLordsRecyclerViewAdapter;
import com.kbanda_projects.mykeja.models.User;

import java.util.ArrayList;
import java.util.List;

public class LandlordsFragment extends Fragment {
    private RecyclerView recyclerView;
    private LandLordsRecyclerViewAdapter recyclerViewAdapter;
    private List<User> userList;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_landlords, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialToolbar toolbar = getActivity().findViewById(R.id.mainToolbar);
        toolbar.setTitle("Manage Landlords");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.landLordRecyclerView);
        recyclerViewAdapter = new LandLordsRecyclerViewAdapter(requireActivity(), userList);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.setAdapter(recyclerViewAdapter);

        fetchLandLordDataFromDatabase();
        super.onViewCreated(view, savedInstanceState);
    }

    private void fetchLandLordDataFromDatabase() {
        if (currentUser != null) {
            firebaseFirestore
                    .collection("Users")
                    .whereEqualTo("role", "LANDLORD")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error == null) {
                                if (value != null) {
                                    if (!value.isEmpty()) {
                                        userList.clear();
                                        for (DocumentChange documentChange : value.getDocumentChanges()) {
                                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                                User user = documentChange.getDocument().toObject(User.class);
                                                userList.add(user);
                                                recyclerViewAdapter.notifyItemInserted(userList.size() + 1);
                                            }
                                        }
                                    } else {
                                        new AlertDialog.Builder(requireActivity())
                                                .setTitle("Error")
                                                .setMessage("No landlords available")
                                                .create()
                                                .show()
                                        ;
                                    }
                                }
                            }
                        }
                    })
            ;
        }
    }
}