package com.kbanda_projects.mykeja;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kbanda_projects.mykeja.databinding.ActivityMainBinding;
import com.kbanda_projects.mykeja.fragments.BookMarkFragment;
import com.kbanda_projects.mykeja.fragments.HomeFragment;
import com.kbanda_projects.mykeja.fragments.LandlordsFragment;
import com.kbanda_projects.mykeja.fragments.MapSearchFragment;
import com.kbanda_projects.mykeja.models.User;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding activityMainBinding;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build()
    );

    private HomeFragment homeFragment;
    private MapSearchFragment mapSearchFragment;
    private BookMarkFragment bookMarkFragment;
    public static final int RC_SIGN_IN = 145;
    private FirebaseUser currentUser;
    private FirebaseFirestore firebaseFirestore;
    private User currentUserObject;
    private LandlordsFragment landlordsFragment;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        progressDialog = new ProgressDialog(this);
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore
                .getInstance();
        homeFragment = new HomeFragment();
        bookMarkFragment = new BookMarkFragment();
        mapSearchFragment = new MapSearchFragment();

        replaceFragment(homeFragment);

        drawerLayout = activityMainBinding.drawerLayout;
        navigationView = activityMainBinding.mainNavigationView;

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.openNavDrawer,
                R.string.closeNavDrawer
        );

        drawerLayout
                .addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle
                .syncState();
        navigationView.setNavigationItemSelectedListener(this);

        authStateListener = firebaseAuth -> {
            currentUser = firebaseAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Please Sign In in order to continue using the app", Toast.LENGTH_SHORT).show();
                startActivityForResult(
                        AuthUI
                                .getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(false)
                                .setAvailableProviders(providers)
                                .setTheme(R.style.Theme_MyKeja)
                                .build(),
                        RC_SIGN_IN
                );
            } else {
                fetchUserInformationFromDatabase();
            }
        };
    }

    @Override
    protected void onStart() {
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
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment);
        fragmentTransaction.commit();
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

    private void fetchUserInformationFromDatabase() {
        if (currentUser != null) {
            firebaseFirestore
                    .collection("Users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                currentUserObject = task.getResult().toObject(User.class);
                                checkProfile(currentUserObject.getFirstName(), "First Name");
                                checkProfile(currentUserObject.getLastName(), "Last Name");
                                checkProfile(currentUserObject.getEmail(), "Email Address");
                                checkProfile(currentUserObject.getPhoneNumber(), "Phone Number");

                                String userRole = currentUserObject.getRole();
                                if (userRole != null) {
                                    if (userRole.equals("ADMIN")) {
                                        navigationView.getMenu().setGroupVisible(R.id.navigationGroupAdmin, true);
                                    } else {
                                        navigationView.getMenu().setGroupVisible(R.id.navigationGroupAdmin, false);
                                    }
                                }
                                loadUserDataInNavigationView(currentUserObject);
                            } else {
                                registerUserToDatabase();
                            }
                        } else {
                            Log.d(TAG, "fetchUserInformationFromDatabase: Failed to load user information.");
                        }
                    })
            ;
        }

    }

    private void registerUserToDatabase() {
        if (currentUser != null) {
            User newUserInstance = new User();//New user Instance
            newUserInstance.setFirstName(currentUser.getDisplayName());
            newUserInstance.setEmail(currentUser.getEmail());
            newUserInstance.setUserId(currentUser.getUid());
            if (isNetworkAvailable()) {
                firebaseFirestore
                        .collection("Users")
                        .document(currentUser.getUid())
                        .set(newUserInstance)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                return;
                            } else {
                                Log.d(TAG, "registerUserToDatabase: Failed to add user to database");
                            }
                        })
                ;
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("No Internet")
                        .setMessage("Please ensure you have a stable internet connection" +
                                " in order to have a smooth experience using the app!")
                        .setPositiveButton("connect", ((dialogInterface, i) -> {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }))
                        .create()
                        .show()
                ;
            }
        }
    }

    private void checkProfile(String value, String parameter) {
        if (value != null) {
            if (!value.trim().isEmpty()) {
                return;
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Incomplete Profile!")
                        .setIcon(R.drawable.ic_error)
                        .setMessage(
                                "You haven't yet entered you " + parameter +
                                        " . Please complete your profile in order to proceed using the app."
                        )
                        .setPositiveButton("Complete", ((dialogInterface, i) -> {
                            openUserProfileActivity();
                        }))
                        .setCancelable(false)
                        .create()
                        .show()
                ;
            }
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Incomplete Profile!")
                    .setIcon(R.drawable.ic_error)
                    .setMessage(
                            "You haven't yet entered you " + parameter +
                                    " . Please complete your profile in order to proceed using the app."
                    )
                    .setPositiveButton("Complete", ((dialogInterface, i) -> {
                        openUserProfileActivity();
                    }))
                    .create()
                    .show()
            ;
        }
    }

    private void openUserProfileActivity() {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("user", (Serializable) currentUserObject);
        startActivity(intent);
    }

    private void loadUserDataInNavigationView(User currentUser) {
        if (currentUser != null) {
            View view = navigationView.getHeaderView(0);
            ImageView profileImage = view.findViewById(R.id.userProfileImage);
            TextView userNameTextView = view.findViewById(R.id.usernameTV);

            String firstName = currentUser.getFirstName();
            String lastName = currentUser.getLastName();
            if (firstName != null && lastName != null) {
                String username = firstName + " " + lastName;
                userNameTextView.setText(username);
                String profileImageUrl = currentUser.getProfileImageUrl();
                if (profileImageUrl != null) {
                    if (!profileImageUrl.trim().isEmpty()) {
                        Glide
                                .with(this)
                                .load(profileImageUrl)
                                .centerCrop()
                                .fitCenter()
                                .into(profileImage)
                        ;
                    }
                }
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionHome: {
                replaceFragment(homeFragment);
                return true;
            }
            case R.id.actionMapSearch: {
                replaceFragment(mapSearchFragment);
                return true;
            }
            case R.id.actionBookMark: {
                replaceFragment(bookMarkFragment);
                return true;
            }
            case R.id.actionProfile: {
                openUserProfileActivity();
                return true;
            }
            case R.id.actionAddHostelAdmin: {
                startActivity(new Intent(this, AdminAddEditHostel.class));
                return true;
            }
            case R.id.actionFeedback: {
                openFeedbackBottomSheet();
                return true;
            }
            case R.id.actionManageLandLordsAdmin: {
                landlordsFragment = new LandlordsFragment();
                replaceFragment(landlordsFragment);
                return true;
            }
            case R.id.actionLogout: {
                new AlertDialog
                        .Builder(this)
                        .setIcon(R.drawable.ic_power)
                        .setTitle("Log Out")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", ((dialogInterface, i) -> logout()))
                        .setCancelable(true)
                        .create()
                        .show()
                ;
                return true;
            }
            default: {
                replaceFragment(homeFragment);
                return false;
            }
        }
    }

    private void openFeedbackBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.layout_feedback_bottomsheet);

        TextInputLayout feedbackTextInputLayout = bottomSheetDialog.findViewById(R.id.feedbackTIL);
        MaterialButton sendButton = bottomSheetDialog.findViewById(R.id.sendFeedbackBTN);

        //TODO: Send feedback
        sendButton
                .setOnClickListener(v -> {
                    Map<String, String> feedback = new HashMap<>();
                    if (currentUser != null) {

                        progressDialog.setTitle("Feedback");
                        progressDialog.setMessage("Sending feedback!");
                        progressDialog.create();
                        progressDialog.show();

                        String userFeedback = feedbackTextInputLayout.getEditText().getText().toString();
                        if (!userFeedback.trim().isEmpty()) {
                            feedback.put("userId", currentUser.getUid());
                            feedback.put("comment", userFeedback);
                            feedback.put("timeInMillis", String.valueOf(System.currentTimeMillis()));
                            firebaseFirestore
                                    .collection("Feedback")
                                    .add(feedback)
                                    .addOnCompleteListener(task -> {
                                        progressDialog.dismiss();
                                        bottomSheetDialog.dismiss();
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "openFeedbackBottomSheet: Feedback Sent");
                                            Toast.makeText(this, "Feedback sent!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.d(TAG, "openFeedbackBottomSheet: Failed to send feedback!");
                                            new AlertDialog.Builder(this)
                                                    .setMessage("Failed to send your feedback! " + task.getException().getMessage())
                                                    .setIcon(R.drawable.ic_error)
                                                    .show()
                                            ;
                                        }
                                    })
                            ;
                        }
                    } else {
                        Toast.makeText(this, "Cannot send feedback! Please ensure you are logged in.", Toast.LENGTH_SHORT).show();
                    }
                })
        ;

        bottomSheetDialog.create();
        bottomSheetDialog.show();
    }

    private void logout() {
        firebaseAuth.signOut();
        finish();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
