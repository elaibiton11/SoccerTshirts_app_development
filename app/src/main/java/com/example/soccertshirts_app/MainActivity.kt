package com.example.soccertshirts_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.soccertshirts_app.data.repository.AuthRepository
import com.example.soccertshirts_app.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.MenuItem

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

        // Intercept BottomNavigation selections so we can handle logout specially
        bottomNav.setOnItemSelectedListener { menuItem: MenuItem ->
            // Determine a nav controller to use
            val nc = navController ?: try {
                findNavController(R.id.nav_host_fragment)
            } catch (e: Exception) {
                null
            }

            when (menuItem.itemId) {
                R.id.navigation_logout -> {
                    // Perform logout via repository (keeps existing functionality intact)
                    AuthRepository().logout()
                    // Navigate to welcomeFragment and clear back stack
                    nc?.navigate(R.id.welcomeFragment, null, NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build())
                    true
                }
                else -> {
                    // Delegate navigation for other items (home, add/edit, profile)
                    if (nc != null) {
                        NavigationUI.onNavDestinationSelected(menuItem, nc)
                        true
                    } else {
                        false
                    }
                }
            }
        }
    }
}