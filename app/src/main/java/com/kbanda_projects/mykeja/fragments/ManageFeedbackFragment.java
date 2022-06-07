package com.kbanda_projects.mykeja.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kbanda_projects.mykeja.R;
import com.kbanda_projects.mykeja.adapters.FeedbackRecyclerViewAdapter;
import com.kbanda_projects.mykeja.models.Feedback;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ManageFeedbackFragment extends Fragment {
    private static final String TAG = "ManageFeedbackFragment";
    public static final int CALL_REQUEST_CODE = 923; //
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    private RecyclerView recyclerView;
    private List<Feedback> feedbackList;
    private FeedbackRecyclerViewAdapter recyclerViewAdapter;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        feedbackList = new ArrayList<>();
        return inflater.inflate(R.layout.fragment_manage_feedback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialToolbar toolbar = getActivity().findViewById(R.id.mainToolbar);
        toolbar.setTitle("Manage Feedback");
        progressDialog = new ProgressDialog(requireActivity());
        currentUser = firebaseAuth.getCurrentUser();
        recyclerView = view.findViewById(R.id.feedbackRecyclerView);
        recyclerViewAdapter = new FeedbackRecyclerViewAdapter(feedbackList, requireActivity());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        recyclerViewAdapter
                .setOnFeedbackClickedListener(new FeedbackRecyclerViewAdapter.OnFeedbackClickedListener() {
                    @Override
                    public void onClick(int position, String userPhoneNumber) {
                        new AlertDialog.Builder(requireActivity())
                                .setMessage("Contact user")
                                .setPositiveButton("call", ((dialogInterface, i) -> {
                                    checkLocationPermissions(userPhoneNumber);
                                }))
                                .create()
                                .show()
                        ;
                    }
                })
        ;
        recyclerView.setAdapter(recyclerViewAdapter);

        fetchFeedbackFromDatabase();
        super.onViewCreated(view, savedInstanceState);
    }

    @AfterPermissionGranted(CALL_REQUEST_CODE)
    private void checkLocationPermissions(String phoneNumber) {
        String[] permissions = {
                Manifest.permission.CALL_PHONE
        };
        if (EasyPermissions.hasPermissions(requireActivity(), permissions)) {
            if (phoneNumber != null) {
                if (!phoneNumber.isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
                } else {
                    Toast.makeText(requireActivity(), "No phone number!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireActivity(), "No phone number!", Toast.LENGTH_SHORT).show();
            }

        } else {
            EasyPermissions
                    .requestPermissions(
                            this,
                            getString(R.string.app_name) + " requires this permissions in order to make a call",
                            CALL_REQUEST_CODE,
                            permissions)
            ;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    private void fetchFeedbackFromDatabase() {
        if (currentUser != null) {
            Log.d(TAG, "fetchFeedbackFromDatabase: Fetching data from database");
            progressDialog.setMessage("Fetching feedback! Please ensure you have a stable internet connection");
            progressDialog.show();

            firestore
                    .collection("Feedback")
                    .get()
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            if (!task.getResult().getDocuments().isEmpty()) {
                                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                feedbackList.clear();
                                for (DocumentSnapshot documentSnapshot : documents) {
                                    Feedback feedback = documentSnapshot.toObject(Feedback.class);
                                    Log.d(TAG, "fetchFeedbackFromDatabase: Feedback -> " + feedback.toString());
                                    feedbackList.add(feedback);
                                    recyclerViewAdapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            Log.d(TAG, "fetchFeedbackFromDatabase: Failed to get Jornals -> " + task.getException().getMessage());
                        }
                    })
            ;
        }
    }
}