package com.kbanda_projects.mykeja;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kbanda_projects.mykeja.databinding.ActivityMainBinding;
import com.kbanda_projects.mykeja.fragments.BookMarkFragment;
import com.kbanda_projects.mykeja.fragments.HomeFragment;
import com.kbanda_projects.mykeja.fragments.MapSearchFragment;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding activityMainBinding;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build()
    );

    private HomeFragment homeFragment;
    private BookMarkFragment bookMarkFragment;
    public static final int RC_SIGN_IN = 145;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        bookMarkFragment = new BookMarkFragment();
        homeFragment = new HomeFragment();
        replaceFragment(homeFragment);

        activityMainBinding
                .bottomNavigationView
                .setOnItemSelectedListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.homeFragment: {
                            replaceFragment(homeFragment);
                            break;
                        }
                        case R.id.mapSearchFragment: {
                            replaceFragment(new MapSearchFragment());
                            break;
                        }
                        case R.id.bookMarkFragment: {
                            replaceFragment(bookMarkFragment);
                            break;
                        }
                    }
                    return true;
                })
        ;

        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
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
}
