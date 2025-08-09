package com.shahar.stoxie

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.activity.OnBackPressedCallback
import com.shahar.stoxie.databinding.ActivityMainBinding

/**
 * MainActivity - Single Activity Architecture
 * 
 * Hosts the NavHostFragment for navigation and manages the BottomNavigationView.
 * Implements single activity pattern where all navigation is handled through fragments.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupBackButtonHandling()
    }

    /**
     * Connects NavHostFragment with BottomNavigationView for seamless navigation.
     * Menu item IDs must match fragment destination IDs for automatic navigation.
     */
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Connect BottomNavigationView to NavController for automatic navigation
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    /**
     * Disables back button to maintain app flow through bottom navigation.
     */
    private fun setupBackButtonHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Back button disabled - users must use bottom navigation
            }
        })
    }
}
