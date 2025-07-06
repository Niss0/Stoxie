package com.shahar.stoxie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.activity.OnBackPressedCallback
import com.shahar.stoxie.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Find the NavHostFragment from our layout.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // 2. Get its NavController.
        val navController = navHostFragment.navController

        // 3. This is the magic line. It automatically connects the BottomNavigationView
        // to the NavController. It will handle switching fragments when you tap an icon
        // because the menu item IDs match the fragment destination IDs.
        binding.bottomNavigationView.setupWithNavController(navController)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // By leaving this empty, we effectively disable the back button.
            }
        })
    }
}
