package com.davenet.notely.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.davenet.notely.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val darkModeValues = resources.getStringArray(R.array.pref_theme_values)
        when (PreferenceManager.getDefaultSharedPreferences(this)
            .getString(getString(R.string.key_theme), getString(R.string.default_theme))) {
            darkModeValues[0] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            darkModeValues[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            darkModeValues[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.noteListFragment), drawer_layout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view.setupWithNavController(navController)
        nav_view.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.action_logout -> {
                        auth.signOut()
                        navController.navigate(R.id.action_noteListFragment_to_loginActivity)
//                        drawer_layout.closeDrawer(GravityCompat.START)
                        true
                    }
                    R.id.settingsFragment2 -> {
                        navController.navigate(R.id.action_noteListFragment_to_settingsFragment2)
                        true
                    }
                    else -> true
                }
            }
        val user = auth.currentUser
        if (user != null) {
            val navHeader = nav_view.getHeaderView(0)
            navHeader.currentUserEmail.text = user.email
        }
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}