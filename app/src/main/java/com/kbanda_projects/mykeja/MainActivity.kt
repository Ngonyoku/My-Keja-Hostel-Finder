package com.kbanda_projects.mykeja

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.kbanda_projects.mykeja.databinding.ActivityMainBinding
import com.kbanda_projects.mykeja.fragments.BookMarkFragment
import com.kbanda_projects.mykeja.fragments.HomeFragment
import com.kbanda_projects.mykeja.fragments.MapSearchFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        loadFragment(HomeFragment())
        binding
                .bottomNavigationView
                .setOnNavigationItemSelectedListener {
                    when (it.itemId) {
                        R.id.homeFragment -> {
                            loadFragment(HomeFragment())
                            return@setOnNavigationItemSelectedListener true
                        }
                        R.id.mapSearchFragment -> {
                            loadFragment(MapSearchFragment())
                            return@setOnNavigationItemSelectedListener true
                        }
                        R.id.bookMarkFragment -> {
                            loadFragment(BookMarkFragment())
                            return@setOnNavigationItemSelectedListener true
                        }
                        else -> {
                            loadFragment(HomeFragment())
                            return@setOnNavigationItemSelectedListener true
                        }
                    }
                }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}