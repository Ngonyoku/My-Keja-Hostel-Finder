package com.kbanda_projects.mykeja;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.kbanda_projects.mykeja.adapters.HostelImagesRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class AdminAddEditHostel extends AppCompatActivity {
    private static final String TAG = "AdminAddEditHostel";
    private static final int REQUEST_CODE_SELECT_PROFILE_IMAGE = 702;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage firebaseStorage;

    private RecyclerView imagesRecyclerView;
    private HostelImagesRecyclerViewAdapter imagesRecyclerViewAdapter;
    private List<String> imagesList;

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
        imagesRecyclerView = findViewById(R.id.hostelImageRecyclerView);

        imagesList = new ArrayList<>();
        imagesRecyclerViewAdapter = new HostelImagesRecyclerViewAdapter(imagesList, this);

        imagesRecyclerView.setHasFixedSize(true);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(
                this,
                RecyclerView.HORIZONTAL,
                false)
        );
        imagesRecyclerView.setAdapter(imagesRecyclerViewAdapter);

        findViewById(R.id.fabAddHostelImages)
                .setOnClickListener(v -> {
                    imageChooser();
                })
        ;
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
                        imagesList.add(imageStringUri);
                        imagesRecyclerViewAdapter.notifyItemInserted(imagesList.size() + 1);
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
                saveHostel();
                return true;
            }
            case R.id.actionDeleteHostel: {
                deleteHostel();
                return true;
            }
            case R.id.actionGetCurrentLocation: {
                getCurrentLocation();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void getCurrentLocation() {

    }

    private void deleteHostel() {
    }

    private void saveHostel() {
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