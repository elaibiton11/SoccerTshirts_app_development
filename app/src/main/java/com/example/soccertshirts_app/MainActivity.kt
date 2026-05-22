package com.example.soccertshirts_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.soccertshirts_app.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Wire BottomNavigationView to NavController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        // Get NavController from the NavHostFragment directly (safer during initial setup)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? androidx.navigation.fragment.NavHostFragment
        val navController = navHostFragment?.navController
        if (navController != null) {
            NavigationUI.setupWithNavController(bottomNav, navController)
        } else {
            // Fallback: try activity-based lookup (may still fail if fragment isn't attached yet)
            try {
                val fallback = findNavController(R.id.nav_host_fragment)
                NavigationUI.setupWithNavController(bottomNav, fallback)
            } catch (e: Exception) {
                // Log the issue; avoid crashing the activity on startup
                e.printStackTrace()
            }
        }
    }
}