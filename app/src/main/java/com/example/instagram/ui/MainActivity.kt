package com.example.instagram.ui

import android.content.Context
import android.os.Bundle
import android.text.BoringLayout
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.instagram.ImageUtils
import com.example.instagram.R
import com.example.instagram.databinding.ActivityMainBinding
import com.example.instagram.setupWithNavController
import com.example.instagram.ui.profile.ProfileViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import org.w3c.dom.Text

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var currentNavigationController: LiveData<NavController>? = null

    private lateinit var binding: ActivityMainBinding

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }


//        Log.e(TAG, "onCreate: screen width: ${ImageUtils.getScreenWidth(this)}")
//        Log.e(TAG, "onCreate: screen height: ${ImageUtils.getScreenHeight(this)}")

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)

        val navGraphIds = listOf(
            R.navigation.nav_home,
            R.navigation.nav_explore,
            R.navigation.nav_notification,
            R.navigation.nav_profile,
        )

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment,
            intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
            navController.addOnDestinationChangedListener { navController, destination, _ ->
                Log.e(
                    TAG,
                    "setupBottomNavigationBar: ${resources.getResourceName(destination.id)}",
                )
                binding.toolBar.title = if (destination.id == R.id.homeFragment) {
                    "Instagram"
                } else {
                    ""
                }


                if (destination.id == R.id.exploreFragment) {
                    binding.searchViewHolder.visibility = View.VISIBLE
                    binding.searchViewHolder.setOnClickListener {
                        navController.navigate(R.id.action_exploreFragment_to_searchFragment)
                    }

                } else {
                    binding.searchViewHolder.visibility = View.GONE

                }

                if (destination.id == R.id.searchFragment) {
                    binding.searchView.visibility = View.VISIBLE
                    val searchEditText =
                        binding.searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
                    searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    searchEditText.requestFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
                } else {
                    binding.searchView.visibility = View.GONE
                }

            }
        })
        currentNavigationController = controller

    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavigationController?.value?.navigateUp() ?: false
    }
}