package com.kbanda_projects.mykeja.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kbanda_projects.mykeja.R;
import com.kbanda_projects.mykeja.databinding.ActivityUserProfileBinding;
import com.kbanda_projects.mykeja.models.User;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.EasyPermissions;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";
    public static final int REQUEST_CODE_SELECT_PROFILE_IMAGE = 32;
    private User user;

    //TODO: Compress image before upload.

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage firebaseStorage;
    private ActivityUserProfileBinding activityUserProfileBinding;
    private EditText emailAddressTilEditText;
    private EditText firstNameTILEditText;
    private EditText lastNameTILEditText;
    private EditText phoneNumberTilEditText;

    private ProgressDialog progressDialog;
    private String profileImageStringUri;
    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityUserProfileBinding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(activityUserProfileBinding.getRoot());

        setSupportActionBar(activityUserProfileBinding.toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        profileImage = activityUserProfileBinding.profileImage;

        phoneNumberTilEditText = activityUserProfileBinding.phoneNumberTil.getEditText();
        emailAddressTilEditText = activityUserProfileBinding.emailAddressTil.getEditText();
        firstNameTILEditText = activityUserProfileBinding.firstNameTIL.getEditText();
        lastNameTILEditText = activityUserProfileBinding.lastNameTIL.getEditText();

        if (getIntent().getExtras() != null) {
            user = (User) getIntent().getSerializableExtra("user");
            if (user != null) {
                String email = user.getEmail();
                if (email != null) {
                    emailAddressTilEditText.setText(email);
                }
                String firstName = user.getFirstName();
                if (firstName != null) {
                    firstNameTILEditText.setText(firstName);
                } else {
                    firstNameTILEditText.setError("Your First Name is required.");
                }
                String lastName = user.getLastName();
                if (lastName != null) {
                    lastNameTILEditText.setText(lastName);
                }
                String phoneNumber = user.getPhoneNumber();
                if (phoneNumber != null) {
                    phoneNumberTilEditText.setText(phoneNumber);
                } else {
                    phoneNumberTilEditText.setError("Please enter your phone number");
                }
                String imageUrl = user.getProfileImageUrl();
                if (imageUrl != null) {
                    if (!imageUrl.isEmpty()) {
                        Glide
                                .with(this)
                                .load(imageUrl)
                                .centerCrop()
                                .into(profileImage)
                        ;
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_setup_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionAddProfileImage: {
                imageChooser();
                return true;
            }
            case R.id.actionSaveProfile: {
                saveProfile();
                return true;
            }
            case R.id.actionLogout: {
                logout();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        AuthUI
                .getInstance()
                .signOut(this)
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                finish();
                            } else {
                                Log.d(TAG, "logout: Failed to logout -> " + task.getException());
                                Toast.makeText(this, "Failed to log out. Please try again later!", Toast.LENGTH_SHORT).show();
                            }
                        })
        ;
        finish();
    }

    @Override
    protected void onResume() {
        if (!isNetworkAvailable()) {
            new AlertDialog.Builder(this)
                    .setTitle("No Internet")
                    .setMessage("This device is NOT connected to the internet")
                    .setPositiveButton("connect", ((dialogInterface, i) -> {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }))
                    .create()
                    .show()
            ;
        }
        super.onResume();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    private void saveProfile() {
        if (currentUser != null) {
            String email = emailAddressTilEditText.getText().toString();
            String phoneNumber = phoneNumberTilEditText.getText().toString();
            String firstName = firstNameTILEditText.getText().toString();
            String lastName = lastNameTILEditText.getText().toString();

            progressDialog.setMessage("Saving Information. Please wait");
            progressDialog.create();
            progressDialog.show();
            User userDetails = new User(firstName, lastName, email, phoneNumber);
            if (user != null) {
                if (user.getRole() != null) {
                    userDetails.setRole(user.getRole());
                } else {
                    userDetails.setRole("USER");
                }

                if (profileImageStringUri != null) {
                    //Upload image to firebase
                    final StorageReference reference = firebaseStorage
                            .getReference(currentUser.getUid())
                            .child(System.currentTimeMillis()
                                    + "."
                                    + getFileExtension(Uri.parse(profileImageStringUri))
                            );
                    Uri imageFile = Uri.fromFile(new File(profileImageStringUri));
                    UploadTask uploadTask = reference.putFile(imageFile);
                    uploadTask
                            .continueWithTask(task -> {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return reference.getDownloadUrl();
                            })
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    String url = task.getResult().toString();
                                    Log.d(TAG, "saveProfile: Download URL extracted -> " + url);
                                    userDetails.setProfileImageUrl(url);
                                    saveToFirebaseDatabase(userDetails);
                                } else {
                                    progressDialog.dismiss();
                                    Log.d(TAG, "saveProfile: Failed to get Download URL -> " + task.getException().getMessage());
                                }
                            })
                    ;
                } else {
                    saveToFirebaseDatabase(userDetails);
                }
            }
        }
    }

    private String getFileExtension(Uri uri) {
        String extension;
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        return extension;
    }

    private void saveToFirebaseDatabase(User userDetails) {
        if (currentUser != null) {
            userDetails.setUserId(currentUser.getUid());
            firebaseFirestore
                    .collection("Users")
                    .document(currentUser.getUid())
                    .set(userDetails, SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "saveProfile: User data saved to database");
                            Toast.makeText(this, "Information saved", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.d(TAG, "saveProfile: Failed to save user details");
                        }
                    })
            ;
        }
    }

    private void imageChooser() {
        Options options = Options
                .init()
                .setRequestCode(REQUEST_CODE_SELECT_PROFILE_IMAGE)
                .setCount(1)
                .setMode(Options.Mode.Picture);
        Pix.start(this, options);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_PROFILE_IMAGE) {
            if (data != null) {
                ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
                if (returnValue != null) {
                    profileImageStringUri = returnValue.get(0);
                    if (profileImageStringUri != null && !profileImageStringUri.isEmpty()) {
                        Glide
                                .with(this)
                                .load(profileImageStringUri)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .centerCrop()
                                .into(profileImage)
                        ;
                    }
                }
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