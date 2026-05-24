package com.example.soccertshirts_app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.soccertshirts_app.data.repository.AuthRepository
import com.example.soccertshirts_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup Top Toolbar
        setSupportActionBar(binding.toolbar)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.profileFragment, R.id.welcomeFragment)
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        // Setup Bottom Navigation
        binding.bottomNav.setupWithNavController(navController)

        // Handle Toolbar Menu (Logout for Profile or other screens)
        binding.toolbar.inflateMenu(R.menu.top_app_bar_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    performLogout()
                    true
                }
                else -> false
            }
        }

        // Manage visibility of navigation bars
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.welcomeFragment, R.id.loginFragment, R.id.registerFragment -> {
                    binding.bottomNav.visibility = View.GONE
                    binding.appBarLayout.visibility = View.GONE
                }
                // Hide global AppBarLayout for main app screens to use custom/clean headers
                R.id.homeFragment, R.id.profileFragment, R.id.addEditJerseyFragment -> {
                    binding.bottomNav.visibility = View.VISIBLE
                    binding.appBarLayout.visibility = View.GONE
                }
                else -> {
                    // Show global toolbar for sub-screens like Details and Comments
                    binding.bottomNav.visibility = View.VISIBLE
                    binding.appBarLayout.visibility = View.VISIBLE
                    binding.toolbar.visibility = View.VISIBLE
                    
                    // Hide logout by default on sub-screens
                    binding.toolbar.menu.findItem(R.id.action_logout)?.isVisible = false
                }
            }
        }
    }

    fun performLogout() {
        AuthRepository().logout()
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(
            R.id.welcomeFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
        )
    }
}
